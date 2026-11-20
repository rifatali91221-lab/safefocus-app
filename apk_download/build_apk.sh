#!/usr/bin/env bash
set -e
gradle assembleDebug
mkdir -p apk_download
cp -f app/build/outputs/apk/debug/app-debug.apk apk_download/SafeFocus-debug.apk
echo "APK exported to apk_download/SafeFocus-debug.apk"
