#!/usr/bin/env python3
"""Phase C compatibility analyzer. Run: python3 tools/phase-c-analysis.py"""

import subprocess, sys, re
from pathlib import Path

ROOT = Path("/mnt/mydata/projects2/0/aosp-full")
PREBUILT = ROOT / "vendor/nova/aosp-prebuilt"
NOVA = ROOT / "vendor/nova"
DEXDUMP = ROOT / "prebuilts/sdk/tools/linux/bin/dexdump"

def get_natives(jar):
    r = subprocess.run([str(DEXDUMP), str(jar)], capture_output=True, timeout=120)
    lines = r.stdout.decode("latin-1").split("\n")
    natives, cur = {}, None
    for i, line in enumerate(lines):
        cm = re.search(r"Class descriptor\s+:\s+'L([^;]+);'", line)
        if cm: cur = cm.group(1).replace("/", ".")
        nm = re.search(r"name\s+:\s+'([^']+)'", line)
        if nm and cur and i + 2 < len(lines) and "NATIVE" in lines[i + 2]:
            natives.setdefault(cur, []).append(nm.group(1))
    return natives

OUR_STUBS = {"SystemClock","Binder","Process","MotionEvent","KeyEvent",
             "Canvas","Paint","Bitmap","BitmapFactory","GLES20","GLUtils",
             "SystemProperties","Log","MessageQueue","NativeUtils"}

def main():
    print("=" * 60)
    print("PHASE C COMPATIBILITY ANALYSIS")
    print("=" * 60)

    # 1. Native methods inventory
    print("\n[1] AOSP framework.jar native methods...", end=" ", flush=True)
    natives = get_natives(PREBUILT / "framework/framework.jar")
    total = sum(len(v) for v in natives.values())
    print(f"{total} in {len(natives)} classes")

    top = sorted(natives.items(), key=lambda x: -len(x[1]))
    print("\nTop classes by native count:")
    for c, m in top[:25]:
        our = "  [HAVE STUB]" if c.split(".")[-1] in OUR_STUBS else ""
        print(f"  {c}: {len(m)}{our}")
    if len(top) > 25:
        print(f"  ... +{len(top)-25} more classes")

    # 2. Missing stub coverage
    needed = [(c, len(m)) for c, m in top if c.split(".")[-1] not in OUR_STUBS]
    print(f"\n[2] Classes WITHOUT JNI stubs ({len(needed)} of {len(natives)}):")
    for c, n in needed[:15]:
        print(f"  {c}: {n} natives")

    # 3. Launcher conflicts
    print("\n[3] Launcher calls to bridge-only methods:")
    lf = NOVA / "nova-framework/src/nova/internal/Launcher.java"
    for line in open(lf):
        for p in ["novaSet","novaGet","getContentView",
                  "setApplication","ActivityThread."]:
            if p in line and "//" not in line.split(p)[0]:
                print(f"  {line.strip()}")

    # 4. Activity init requirements
    print("\n[4] Activity.onCreate requirements (package scan):")
    act_methods = natives.get("android.app.Activity", [])
    print(f"  Activity native methods: {len(act_methods)}")
    infra_reqs = ["ContextImpl", "ActivityInfo", "Application", "Window",
                  "FragmentController", "Instrumentation", "Resources"]
    for req in infra_reqs:
        cls = f"android.app.{req}" if req != "ActivityInfo" else f"android.content.pm.{req}"
        cls2 = f"android.content.{req}" if req == "Resources" else cls
        has_n = cls2 in natives or cls in natives
        cnt = len(natives.get(cls2, [])) + len(natives.get(cls, []))
        print(f"  {req}: {'needs natives: ' + str(cnt) if has_n else 'pure Java'}")

    # Summary
    print(f"\n{'='*60}")
    print(f"CONCLUSION")
    print(f"{'='*60}")
    print(f"  AOSP native methods: {total}")
    print(f"  Classes:            {len(natives)}")
    print(f"  Missing stubs:      {len(needed)} classes ({sum(n for _, n in needed)} methods)")
    print(f"  Launcher conflicts: {sum(1 for line in open(lf) for p in ['novaSet','getContentView','setApplication','ActivityThread.'] if p in line and '//' not in line.split(p)[0])}")

    print(f"\n  Priority for Phase C:")
    print(f"  1. Fix Launcher (remove bridge-only calls: getContentView, setApplication, etc.)")
    print(f"  2. Add JNI stubs for top missing classes")
    print(f"  3. Activity attach() wrapper for proper initialization")
    print(f"  4. Mini PackageManager/Resources for getApplicationInfo()")

if __name__ == "__main__":
    main()
