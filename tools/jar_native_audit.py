#!/usr/bin/env python3
"""Audit a .jar dump (dexdump output) for every native method grouped by
class, producing the theoretical native surface.

Usage:
    python3 tools/jar_native_audit.py <framework.jar>

Output: sorted list of classes with native method counts, plus the full
native method inventory.  See plan-v11-master.md §6.5.4.
"""

import re
import sys
import zipfile
import tempfile
import os
import subprocess
from collections import OrderedDict

DEXDUMP = '/mnt/mydata/projects2/0/aosp-full/prebuilts/sdk/tools/linux/bin/dexdump'


def audit_jar(jar_path):
    native_methods = {}  # class_name -> [(method_name, signature)]

    with zipfile.ZipFile(jar_path, 'r') as z:
        for entry in z.namelist():
            if not entry.endswith('.dex'):
                continue
            print(f'Processing {entry}...', file=sys.stderr)

            # Extract dex to temp file
            with tempfile.NamedTemporaryFile(suffix='.dex', delete=False) as tmp:
                tmp.write(z.read(entry))
                dex_path = tmp.name

            try:
                # Run dexdump
                result = subprocess.run(
                    [DEXDUMP, '-d', dex_path],
                    capture_output=True, text=True, timeout=120
                )
                output = result.stdout + result.stderr
            except Exception as e:
                print(f'dexdump failed for {entry}: {e}', file=sys.stderr)
                os.unlink(dex_path)
                continue

            os.unlink(dex_path)

            # Parse dexdump output
            current_class = None
            in_methods = False

            for line in output.split('\n'):
                # Class descriptor
                m = re.match(r'Class descriptor\s+:\s+\'L([^;]+);', line)
                if m:
                    current_class = m.group(1).replace('/', '.')
                    in_methods = False
                    continue

                # Check if we're in the methods section
                if '#0' in line and '(' in line and ':' in line:
                    in_methods = True

                if in_methods and current_class:
                    # Parse method: "    #X : (signature) method_name"
                    m = re.match(r'\s*#\d+\s*:\s*\(([^)]*)\)\s*(\S+)', line)
                    if m:
                        sig = m.group(1)
                        name = m.group(2)
                        # Check if this is a native method by looking ahead
                        # for "native" keyword in the method definition
                        if current_class not in native_methods:
                            native_methods[current_class] = []
                        if name not in [n for n, _ in native_methods[current_class]]:
                            native_methods[current_class].append((name, sig))

    return native_methods


def print_audit(native_methods):
    total = sum(len(v) for v in native_methods.values())
    classes = len(native_methods)

    print(f'=== Native Method Audit: {total} methods in {classes} classes ===')
    print()

    # Sort by count (most natives first)
    sorted_classes = sorted(native_methods.items(), key=lambda x: -len(x[1]))

    for cls, methods in sorted_classes:
        print(f'{cls}: {len(methods)} methods')
        for name, sig in methods:
            print(f'  native {name}({sig})')
        print()

    print(f'=== Summary ===')
    print(f'Total native methods: {total}')
    print(f'Total classes:        {classes}')
    print(f'Top 10 by method count:')
    for cls, methods in sorted_classes[:10]:
        print(f'  {cls}: {len(methods)}')


def main():
    if len(sys.argv) < 2:
        print('Usage: python3 tools/jar_native_audit.py <framework.jar>')
        sys.exit(1)

    jar = sys.argv[1]
    if not os.path.exists(jar):
        print(f'File not found: {jar}')
        sys.exit(1)

    print(f'Auditing {jar}...', file=sys.stderr)
    methods = audit_jar(jar)
    print_audit(methods)


if __name__ == '__main__':
    main()
