#!/bin/bash
# Deploy a real AOSP framework.jar built from a full AOSP checkout.
#
# Usage:
#   1. Build framework on full AOSP machine:
#      m framework-minus-apex
#   2. Copy framework.jar here:
#      scp out/target/product/*/system/framework/framework.jar user@host:/tmp/
#   3. Run this script:
#      ./scripts/deploy-real-framework.sh /tmp/framework.jar
#
# The real framework provides every android.* method implementation — no Stub! crashes.

set -e

SRC_JAR="${1:-/tmp/framework.jar}"
if [ ! -f "$SRC_JAR" ]; then
    echo "Usage: $0 <path-to-framework.jar>"
    echo "  Build framework with: m framework-minus-apex"
    exit 1
fi

NOVA_DIR="$(cd "$(dirname "$0")/.." && pwd)"
AOSP_DIR="/mnt/mydata/projects2/0/aosp-full"
OUT_DIR="$AOSP_DIR/out/host/linux-x86/framework"
D8_JAR="$AOSP_DIR/prebuilts/r8/r8.jar"
JAVA="$AOSP_DIR/prebuilts/jdk/jdk21/linux-x86/bin/java"
JAVA_HOME="$AOSP_DIR/prebuilts/jdk/jdk21/linux-x86"

# Also export for D8 which needs JDK home for bootstrap class resolution
export JAVA_HOME

echo "=== Deploying real framework DEX ==="
echo "Source: $SRC_JAR"
echo "Output: $OUT_DIR/real-framework-hostdex.jar"

# DEX the real framework (--lib must point to JDK home for bootstrap classes)
rm -rf /tmp/nova-dex-fw && mkdir -p /tmp/nova-dex-fw
$JAVA -cp "$D8_JAR" com.android.tools.r8.D8 --release \
    --lib $JAVA_HOME \
    --output /tmp/nova-dex-fw \
    "$SRC_JAR" 2>&1 | tail -3

# Package as jar
cd /tmp/nova-dex-fw && jar cf "$OUT_DIR/real-framework-hostdex.jar" classes.dex

# Also rebuild Nova's framework DEX
echo "=== Rebuilding Nova framework DEX ==="
NOVA_JAR="$AOSP_DIR/out/soong/.intermediates/vendor/nova/nova-framework/nova-framework-host/android_common/javac/nova-framework.jar"
rm -f /tmp/nova-dex-out/classes.dex
mkdir -p /tmp/nova-dex-out
$JAVA -cp "$D8_JAR" com.android.tools.r8.D8 --release \
    --lib $JAVA_HOME \
    --output /tmp/nova-dex-out \
    "$NOVA_JAR" 2>&1 | tail -1
cd /tmp/nova-dex-out && jar cf "$OUT_DIR/nova-framework-hostdex.jar" classes.dex

echo "=== DONE ==="
echo "Real framework: $OUT_DIR/real-framework-hostdex.jar ($(wc -c < "$OUT_DIR/real-framework-hostdex.jar") bytes)"
echo "Nova framework: $OUT_DIR/nova-framework-hostdex.jar ($(wc -c < "$OUT_DIR/nova-framework-hostdex.jar") bytes)"
echo ""
echo "The classpath will be: real-framework > nova-framework > android-stubs"
echo "Rebuild the native binary with: m nova"
echo "Then test: out/host/linux-x86/bin/nova --standalone ..."
