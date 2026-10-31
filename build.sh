#!/usr/bin/env bash
# Gradle 없이 SDK 도구(aapt2, javac, d8, apksigner)만으로 APK를 만든다.
set -euo pipefail

SDK="${ANDROID_HOME:-${ANDROID_SDK_ROOT:?ANDROID_HOME not set}}"
BT="$SDK/build-tools/$(ls "$SDK/build-tools" | sort -V | tail -n1)"
PLATFORM="$SDK/platforms/$(ls "$SDK/platforms" | sort -V | tail -n1)"
ANDROID_JAR="$PLATFORM/android.jar"
echo "build-tools: $BT"
echo "platform:    $PLATFORM"

ROOT="$(cd "$(dirname "$0")" && pwd)"
APP="$ROOT/app"
OUT="$ROOT/build"
rm -rf "$OUT" && mkdir -p "$OUT/res" "$OUT/classes" "$OUT/dex"

"$BT/aapt2" compile --dir "$APP/res" -o "$OUT/res.zip"
"$BT/aapt2" link -o "$OUT/unsigned.apk" -I "$ANDROID_JAR" \
  --manifest "$APP/AndroidManifest.xml" --java "$OUT/gen" "$OUT/res.zip"

javac -encoding UTF-8 --release 11 -classpath "$ANDROID_JAR" -d "$OUT/classes" \
  $(find "$APP/java" "$OUT/gen" -name '*.java')

"$BT/d8" --min-api 29 --lib "$ANDROID_JAR" --output "$OUT/dex" $(find "$OUT/classes" -name '*.class')
(cd "$OUT/dex" && zip -q -j "$OUT/unsigned.apk" classes.dex)

"$BT/zipalign" -f -p 4 "$OUT/unsigned.apk" "$OUT/aligned.apk"

KEYSTORE="${KEYSTORE:-$OUT/debug.keystore}"
if [ ! -f "$KEYSTORE" ]; then
  keytool -genkeypair -keystore "$KEYSTORE" -storepass android -keypass android \
    -alias key -keyalg RSA -keysize 2048 -validity 10000 -dname "CN=ChargeToggle" >/dev/null
fi
"$BT/apksigner" sign --ks "$KEYSTORE" --ks-pass pass:android --key-pass pass:android \
  --out "$OUT/ChargeToggle.apk" "$OUT/aligned.apk"
"$BT/apksigner" verify "$OUT/ChargeToggle.apk"
echo "OK -> $OUT/ChargeToggle.apk"
