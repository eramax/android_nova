#!/usr/bin/env python3
"""Generate a minimal com.android.internal.R for Nova hybrid framework compilation."""

from __future__ import annotations

import re
import subprocess
from collections import defaultdict
from pathlib import Path

ROOT = Path(__file__).resolve().parents[4]
OUT = ROOT / "vendor/nova/nova-framework/src/com/android/internal/R.java"
ANDROID_R_OUT = ROOT / "vendor/nova/nova-framework/src/android/R.java"
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
ANDROID_R_PATTERN = re.compile(r"android\.R\.styleable\.([A-Za-z0-9_]+)")
INTERNAL_R_STYLEABLE_PATTERN = re.compile(r"\bR\.styleable\.([A-Za-z0-9_]+)")


def collect_refs() -> dict[str, set[str]]:
    refs: dict[str, set[str]] = defaultdict(set)
    android_styleable: set[str] = set()
    for base in SCAN_DIRS:
        if not base.is_dir():
            continue
        for path in base.rglob("*.java"):
            text = path.read_text(errors="replace")
            for outer, inner in PATTERN.findall(text):
                refs[outer].add(inner)
            for inner in ANDROID_R_PATTERN.findall(text):
                android_styleable.add(inner)
            if "import com.android.internal.R" in text or "import com.android.internal.R;" in text:
                for inner in INTERNAL_R_STYLEABLE_PATTERN.findall(text):
                    refs["styleable"].add(inner)
    refs["_android_styleable"] = android_styleable
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


def emit_android_r(styleable: set[str]) -> str:
    lines = [
        "package android;",
        "",
        "/** Auto-generated minimal android.R for Nova hybrid framework compilation. */",
        "public final class R {",
        "    private R() {}",
        "",
        "    public static final class styleable {",
        "        private styleable() {}",
        "",
    ]
    groups: dict[str, list[str]] = defaultdict(list)
    for inner in sorted(styleable):
        if "_" in inner:
            groups[inner.split("_", 1)[0]].append(inner)
    emitted: set[str] = set()
    array_groups: set[str] = set()
    for prefix, members in sorted(groups.items()):
        ordered = sorted(members)
        if len(ordered) <= 1:
            continue
        array_groups.add(prefix)
        lines.append(f"        public static final int[] {prefix} = new int[{len(ordered)}];")
        for idx, member in enumerate(ordered):
            lines.append(f"        public static final int {member} = {idx};")
            emitted.add(member)
        lines.append("")
    next_id = 0x01010000
    for inner in sorted(styleable):
        if inner in emitted:
            continue
        if inner in array_groups:
            continue
        lines.append(f"        public static final int {inner} = {next_id};")
        next_id += 1
    lines.append("    }")
    lines.append("}")
    lines.append("")
    return "\n".join(lines)


def main() -> None:
    refs = collect_refs()
    android_styleable = refs.pop("_android_styleable", set())
    OUT.parent.mkdir(parents=True, exist_ok=True)
    OUT.write_text(emit(refs))
    ANDROID_R_OUT.parent.mkdir(parents=True, exist_ok=True)
    ANDROID_R_OUT.write_text(emit_android_r(android_styleable))
    print(f"Wrote {OUT} with {sum(len(v) for v in refs.values())} symbols in {len(refs)} classes")
    print(f"Wrote {ANDROID_R_OUT} with {len(android_styleable)} styleable symbols")


if __name__ == "__main__":
    main()
