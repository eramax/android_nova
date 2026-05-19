#!/usr/bin/env bash
# missing_classes.sh <apk_path> [<apk_path2> ...]
# Scans APK DEX files and reports Android framework classes that are
# referenced but not yet implemented in nova-framework/src.
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
VENDOR="$SCRIPT_DIR/../nova-framework/src"
AOSP_ROOT="$SCRIPT_DIR/../../.."
DEXDUMP="${AOSP_ROOT}/out/host/linux-x86/bin/aapt2"  # not used, just for aapt2
TMPDIR_WORK="$(mktemp -d)"
trap 'rm -rf "$TMPDIR_WORK"' EXIT

if [ $# -eq 0 ]; then
    echo "Usage: $0 <apk> [<apk2> ...]"
    exit 1
fi

# Extract all referenced android.* class descriptors from all APK DEX files
ALL_REFS=""
for apk in "$@"; do
    # Unzip all dex files
    unzip -q "$apk" "*.dex" -d "$TMPDIR_WORK/$(basename "$apk" .apk)/" 2>/dev/null || true
done

# Use strings to extract class descriptors from all DEX files
ALL_REFS=$(
    find "$TMPDIR_WORK" -name "*.dex" -exec strings {} \; \
    | grep -oP 'Landroid/[a-zA-Z0-9_$/]+;' \
    | sort -u \
    | sed 's|^L||;s|;$||;s|/|.|g'
)

# Build set of implemented base classes (strip inner class suffix)
HAVE_FILES=$(find "$VENDOR" -name "*.java" | sed "s|$VENDOR/||;s|/|.|g;s|\.java$||" | sort -u)

echo "=== Missing Android framework classes ==="
echo "(referenced in APK but not in nova-framework/src)"
echo ""

MISSING_COUNT=0
PREV_PKG=""

while IFS= read -r cls; do
    # Skip support library — it's bundled in the APK, not a host dep
    case "$cls" in
        android.support.*) continue ;;
        android.annotation.*) continue ;;
    esac

    # Get base class (strip inner class)
    base="${cls%%\$*}"
    filepath=$(echo "$base" | tr '.' '/')

    # Check if implemented
    if ! find "$VENDOR" -path "*${filepath}.java" 2>/dev/null | grep -q .; then
        pkg=$(echo "$cls" | sed 's|\.[A-Z].*||')
        if [ "$pkg" != "$PREV_PKG" ]; then
            echo ""
            echo "  [$pkg]"
            PREV_PKG="$pkg"
        fi
        echo "    MISSING: $cls"
        MISSING_COUNT=$((MISSING_COUNT + 1))
    fi
done <<< "$ALL_REFS"

echo ""
echo "=== Total missing: $MISSING_COUNT classes ==="
