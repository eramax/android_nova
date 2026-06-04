#!/usr/bin/env python3
"""Parse `nova` run log, emit a deduped worklist.

Usage:
    python3 tools/hotset_from_log.py <nova-log-file>

Scans the full log text for:
  - UnsatisfiedLinkError  → need native impl (highest priority)
  - NullPointerException, NoSuchMethodError, ExceptionInInitializerError
    → Java bridge gaps

See plan-v11-master.md §6.5.4.
"""

import re
import sys
from collections import OrderedDict


def parse_log(path):
    with open(path, 'r', errors='replace') as f:
        text = f.read()

    lines = text.split('\n')
    worklist = OrderedDict()

    for i, line in enumerate(lines):
        # --- UnsatisfiedLinkError ---
        if 'UnsatisfiedLinkError' in line:
            # Scan following lines for "(Native Method)" pattern
            for j in range(1, 6):
                if i + j < len(lines):
                    m = re.search(r'at\s+(\S+)\.(\w+)\(Native Method\)', lines[i + j])
                    if m:
                        cls, meth = m.group(1), m.group(2)
                        key = f"{cls}.{meth}"
                        if key not in worklist:
                            worklist[key] = {
                                'type': 'UnsatisfiedLinkError',
                                'java_class': cls,
                                'method_name': meth,
                            }
                        break

        # --- NoSuchMethodError from framework.jar ---
        if 'NoSuchMethodError' in line:
            m = re.search(r'No\s+(?:static\s+)?method\s+(\S+)', line)
            if m:
                desc = m.group(1)
                key = f"NoSuchMethod: {desc}"
                if key not in worklist:
                    worklist[key] = {
                        'type': 'NoSuchMethodError',
                        'description': desc,
                    }

        # --- NullPointerException with stack ---
        if 'NullPointerException' in line and 'Caused by' not in line:
            trace = []
            for j in range(1, 6):
                if i + j < len(lines):
                    tl = lines[i + j].strip()
                    if tl.startswith('at '):
                        trace.append(tl)
            if trace:
                key = f"NPE: {trace[0]}"
                if key not in worklist:
                    worklist[key] = {
                        'type': 'NullPointerException',
                        'message': line.strip(),
                        'stack': trace[:3],
                    }

        # --- ExceptionInInitializerError ---
        if 'ExceptionInInitializerError' in line:
            for j in range(0, 4):
                if i + j < len(lines):
                    m = re.search(r'at\s+(\S+)\.<clinit>\(', lines[i + j])
                    if m:
                        cls = m.group(1)
                        key = f"clinit: {cls}"
                        if key not in worklist:
                            worklist[key] = {
                                'type': 'ExceptionInInitializerError',
                                'class': cls,
                            }
                        break

    return worklist


def print_worklist(worklist):
    if not worklist:
        print("=== No issues found (possibly all natives resolved) ===")
        return 0

    item_list = list(worklist.items())
    print(f"=== Worklist: {len(item_list)} items (in order) ===")
    print()
    for idx, (key, info) in enumerate(item_list, 1):
        t = info['type']
        print(f"#{idx}: [{t}]")
        if t == 'UnsatisfiedLinkError':
            print(f"   Class:  {info['java_class']}")
            print(f"   Method: {info['method_name']}")
            print(f"   Fix:    implement in libnova_jni + RegisterNatives")
        elif t == 'NullPointerException':
            print(f"   {info['message']}")
            for s in info['stack'][:2]:
                print(f"   {s}")
            print(f"   Fix:    bridge state missing — init before calling")
        elif t == 'NoSuchMethodError':
            print(f"   {info['description']}")
            print(f"   Fix:    guard with reflection fallback in Launcher")
        elif t == 'ExceptionInInitializerError':
            print(f"   class {info['class']}.<clinit> failed")
            print(f"   Fix:    <clinit> dependency cascade — fix root cause")
        print()

    # Summary
    types = {}
    for v in worklist.values():
        types[v['type']] = types.get(v['type'], 0) + 1
    print("=== Summary ===")
    for t, c in sorted(types.items(), key=lambda x: -x[1]):
        print(f"  {t}: {c}")
    print(f"  Total: {len(item_list)}")

    return int(any(v['type'] == 'UnsatisfiedLinkError' for v in worklist.values()))


if __name__ == '__main__':
    if len(sys.argv) < 2:
        print("Usage: python3 tools/hotset_from_log.py <nova-log-file>")
        sys.exit(1)
    sys.exit(parse_log.__doc__ and 0)
