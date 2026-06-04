#!/bin/bash
# Extract AOSP GSI system.img to vendor/nova/aosp-prebuilt/
# Usage: ./extract-gsi.sh <path-to-system.img>
set -e

if [ $# -ne 1 ]; then
    echo "Usage: $0 <path-to-system.img>"
    exit 1
fi

IMG="$1"
PREBUILT="$(cd "$(dirname "$0")/.." && pwd)/aosp-prebuilt"
TMPDIR="/tmp/nova-system-extract-$$"

if [ ! -f "$IMG" ]; then
    echo "ERROR: $IMG not found"
    exit 1
fi

echo "==> Extracting $IMG"
echo "==> Target: $PREBUILT"

# Cleanup first
rm -rf "$PREBUILT"
mkdir -p "$PREBUILT"/{framework,lib64,lib,apex,etc,bin,usr,app,priv-app,vendor,product}
mkdir -p "$TMPDIR"

# 7z handles ext2/3/4 without root
7z x "$IMG" -o"$TMPDIR" 2>&1 | tail -5

# Copy out
echo "==> Copying framework/"
cp -a "$TMPDIR/system/framework/." "$PREBUILT/framework/"
echo "==> Copying lib64/"
cp -a "$TMPDIR/system/lib64/." "$PREBUILT/lib64/"
echo "==> Copying lib/"
cp -a "$TMPDIR/system/lib/." "$PREBUILT/lib/" 2>/dev/null || true
echo "==> Copying apex/"
cp -a "$TMPDIR/system/apex/." "$PREBUILT/apex/" 2>/dev/null || true
echo "==> Copying etc/"
cp -a "$TMPDIR/system/etc/." "$PREBUILT/etc/"
echo "==> Copying bin/"
cp -a "$TMPDIR/system/bin/." "$PREBUILT/bin/"
echo "==> Copying usr/"
cp -a "$TMPDIR/system/usr/." "$PREBUILT/usr/" 2>/dev/null || true
echo "==> Copying app/"
cp -a "$TMPDIR/system/app/." "$PREBUILT/app/"
echo "==> Copying priv-app/"
cp -a "$TMPDIR/system/priv-app/." "$PREBUILT/priv-app/"

# Cleanup
rm -rf "$TMPDIR"

echo "==> Extracted to $PREBUILT ($(du -sh $PREBUILT | awk '{print $1}'))"
echo "==> Run Phase A proof gate next."
