#!/usr/bin/env python3
"""Generate a minimal com.android.internal.R for Nova hybrid framework compilation."""

from __future__ import annotations

import re
import subprocess
from collections import defaultdict
from pathlib import Path

ROOT = Path(__file__).resolve().parents[4]
OUT = ROOT / "vendor/nova/nova-framework/src/com/android/internal/R.java"
SCAN_DIRS = [
    ROOT / "frameworks/base/core/java/android/view",
    ROOT / "frameworks/base/core/java/android/widget",
    ROOT / "frameworks/base/core/java/android/text",
    ROOT / "frameworks/base/core/java/android/animation",
    ROOT / "frameworks/base/core/java/com/android/internal/util",
    ROOT / "frameworks/base/core/java/com/android/internal/view/menu",
    ROOT / "frameworks/base/graphics/java",
]

PATTERN = re.compile(r"com\.android\.internal\.R\.([A-Za-z0-9_]+)\.([A-Za-z0-9_]+)")


def collect_refs() -> dict[str, set[str]]:
    refs: dict[str, set[str]] = defaultdict(set)
    for base in SCAN_DIRS:
        if not base.is_dir():
            continue
        for path in base.rglob("*.java"):
            text = path.read_text(errors="replace")
            for outer, inner in PATTERN.findall(text):
                refs[outer].add(inner)
    return refs


def emit(refs: dict[str, set[str]]) -> str:
    lines = [
        "package com.android.internal;",
        "",
        "/** Auto-generated minimal internal R for Nova hybrid framework compilation. */",
        "public final class R {",
        "    private R() {}",
        "",
    ]
    next_id = 0x01010000
    for outer in sorted(refs):
        lines.append(f"    public static final class {outer} {{")
        lines.append(f"        private {outer}() {{}}")
        lines.append("")
        if outer == "styleable":
            groups: dict[str, list[str]] = defaultdict(list)
            standalone: list[str] = []
            for inner in sorted(refs[outer]):
                if "_" in inner:
                    prefix = inner.split("_", 1)[0]
                    if any(inner.startswith(prefix + "_") for inner2 in refs[outer] if inner2 != inner):
                        groups[prefix].append(inner)
                        continue
                standalone.append(inner)
            emitted = set()
            group_names = set()
            for prefix, members in sorted(groups.items()):
                if len(members) <= 1:
                    continue
                group_names.add(prefix)
                ordered = sorted(members)
                lines.append(f"        public static final int[] {prefix} = new int[{len(ordered)}];")
                for idx, member in enumerate(ordered):
                    lines.append(f"        public static final int {member} = {idx};")
                    emitted.add(member)
                lines.append("")
            for inner in sorted(refs[outer]):
                if inner in emitted or inner in group_names:
                    continue
                lines.append(f"        public static final int {inner} = {next_id};")
                next_id += 1
        else:
            for inner in sorted(refs[outer]):
                lines.append(f"        public static final int {inner} = {next_id};")
                next_id += 1
        lines.append("    }")
        lines.append("")
    lines.append("}")
    lines.append("")
    return "\n".join(lines)


def main() -> None:
    refs = collect_refs()
    OUT.parent.mkdir(parents=True, exist_ok=True)
    OUT.write_text(emit(refs))
    print(f"Wrote {OUT} with {sum(len(v) for v in refs.values())} symbols in {len(refs)} classes")


if __name__ == "__main__":
    main()
