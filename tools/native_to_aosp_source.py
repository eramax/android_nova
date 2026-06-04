#!/usr/bin/env python3
"""Locate AOSP JNI source file for a register function or native method.

Usage:
    python3 tools/native_to_aosp_source.py <register_android_os_Xxx>
    python3 tools/native_to_aosp_source.py <JavaClass.method>

Examples:
    python3 tools/native_to_aosp_source.py register_android_os_MessageQueue
    python3 tools/native_to_aosp_source.py MessageQueue.nativeInit

Reference: plan-v11-master.md §6.5.4
"""

import os
import re
import sys
import fnmatch

AOSP_ROOT = os.environ.get('AOSP_ROOT', '/mnt/mydata/projects2/0/aosp-full')


def find_file(pattern):
    """Find .cpp files matching pattern under frameworks/."""
    results = []
    for dirpath, dirnames, filenames in os.walk(os.path.join(AOSP_ROOT, 'frameworks')):
        for fn in filenames:
            if fnmatch.fnmatch(fn, pattern):
                results.append(os.path.join(dirpath, fn))
    return results


def get_gmethods(content):
    """Extract the gMethods table content from a JNI source file."""
    # Look for `static const JNINativeMethod gMethods[]`
    m = re.search(
        r'(static\s+const\s+JNINativeMethod\s+gMethods\[\].*?)\n\s*\};',
        content, re.DOTALL
    )
    if m:
        return m.group(1)

    # Alternative: `static const JNINativeMethod gMethods[] = {`
    m = re.search(
        r'JNINativeMethod\s+gMethods\[\]\s*=\s*\{.*?\n\s*\};',
        content, re.DOTALL
    )
    if m:
        return m.group(0)
    return None


def find_register_function(name):
    """Find register function definition and its gMethods table."""
    cpps = find_file('*.cpp')
    for fpath in cpps:
        with open(fpath, 'r', errors='ignore') as f:
            content = f.read()

        pattern = rf'int\s+{re.escape(name)}\s*\('
        m = re.search(pattern, content)
        if not m:
            continue

        lines = content.split('\n')
        line_no = next(i + 1 for i, l in enumerate(lines) if re.search(pattern, l))

        # Also find the implementation file (android_os_ClassName.cpp)
        # by looking for the register function body
        body = re.search(
            rf'int\s+{re.escape(name)}\s*\(.*?^}}',
            content, re.MULTILINE | re.DOTALL
        )

        gmethods = get_gmethods(content)

        return fpath, line_no, gmethods, body.group(0) if body else ''

    return None, 0, None, ''


def find_implementation_file(register_func_name):
    """Find the file that actually IMPLEMENTS the register function
    (not just declares it in AndroidRuntime.cpp's gRegJNI table)."""
    cls_match = re.search(r'register_android_(?:os|view|graphics|opengl|util)_(\w+)',
                          register_func_name)
    if cls_match:
        cls_name = cls_match.group(1)
        # AOSP JNI files are named android_os_{ClassName}.cpp or android_util_{ClassName}.cpp
        candidates = find_file(f'android_*_{cls_name}.cpp')
        if candidates:
            return candidates[0]

        # Try with underscores (e.g. BitmapFactory -> android_graphics_BitmapFactory.cpp)
        candidates = find_file(f'android_*_{cls_name}.cpp')
        if candidates:
            return candidates[0]

        # Try framesworks/base/core/jni/
        base = os.path.join(AOSP_ROOT, 'frameworks', 'base', 'core', 'jni')
        for fn in os.listdir(base):
            if re.search(rf'android_\w*{cls_name}', fn, re.IGNORECASE):
                return os.path.join(base, fn)
    return None


def print_context(fpath, line_no, lines_around=5):
    """Print lines around a given line number."""
    with open(fpath, 'r', errors='ignore') as f:
        lines = f.read().split('\n')
    start = max(0, line_no - lines_around - 1)
    end = min(len(lines), line_no + lines_around)
    for i in range(start, end):
        marker = '>>>' if i == line_no - 1 else '   '
        print(f'  {marker} {i+1}: {lines[i]}')


def main():
    if len(sys.argv) < 2:
        print("Usage:")
        print("  python3 tools/native_to_aosp_source.py register_android_os_Xxx")
        print("  python3 tools/native_to_aosp_source.py <JavaClass.method>")
        sys.exit(0)

    query = sys.argv[1]

    # Mode 1: register function
    if query.startswith('register_'):
        fpath, line_no, gmethods, body = find_register_function(query)
        if not fpath:
            # Try finding implementation by class name
            impl = find_implementation_file(query)
            if impl:
                fpath = impl
                with open(fpath, 'r', errors='ignore') as f:
                    content = f.read()
                line_no = 1
                gmethods = get_gmethods(content)
                body = ''
            else:
                print(f"Not found: {query}")
                sys.exit(1)

        print(f"--- {query} ---")
        print(f"File: {fpath}")
        print()

        if gmethods:
            print("gMethods table:")
            print(gmethods)
        print()

        if body:
            print("Function body (first 30 lines):")
            for l in body.split('\n')[:30]:
                print(f"  {l}")
        return

    # Mode 2: Class.method format
    if '.' in query:
        parts = query.rsplit('.', 1)
        java_class, method_name = parts[0], parts[1]
        short_cls = java_class.rsplit('.', 1)[-1]

        print(f"--- {java_class}.{method_name} ---")

        # Find implementation files
        # AOSP native files pattern: android_{package}_{Class}.cpp
        pkg = ''
        if 'os' in java_class:
            pkg = 'os'
        elif 'view' in java_class:
            pkg = 'view'
        elif 'graphics' in java_class:
            pkg = 'graphics'
        elif 'util' in java_class:
            pkg = 'util'
        elif 'opengl' in java_class:
            pkg = 'opengl'

        candidates = find_file(f'android_{pkg}_{short_cls}.cpp')
        if not candidates:
            # Broader search
            candidates = find_file(f'*{short_cls}*.cpp')

        for cpp in candidates:
            with open(cpp, 'r', errors='ignore') as f:
                content = f.read()

            # Find the gMethods entry
            in_gmethods = False
            for line_no, line in enumerate(content.split('\n'), 1):
                if 'JNINativeMethod' in line and 'gMethods' in line:
                    in_gmethods = True
                    continue
                if in_gmethods:
                    if f'"{method_name}"' in line:
                        print(f"File: {cpp}:{line_no}")
                        print(f"gMethods entry: {line.strip()}")

                        # Find register function and C function name
                        reg_m = re.search(r'int\s+(register_\w+)\s*\(', content)
                        if reg_m:
                            print(f"Register function: {reg_m.group(1)}")
                        fn_m = re.search(r'\(void\*\)(\w+)', line)
                        if fn_m:
                            print(f"C function: {fn_m.group(1)}")

                        print()
                        print_context(cpp, line_no)
                        print()
                        break
                    if '};' in line:
                        in_gmethods = False

        if not candidates:
            print(f"No implementation file found for {short_cls}")
            print(f"Try: python3 tools/native_to_aosp_source.py register_android_{pkg}_{short_cls}")
        return

    print(f"Unknown query format: {query}")
    print("Use: 'register_android_os_ClassName' or 'JavaClass.methodName'")


if __name__ == '__main__':
    main()
