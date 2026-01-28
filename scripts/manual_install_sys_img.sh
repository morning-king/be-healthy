#!/bin/bash

# manual_install_sys_img.sh
# 辅助手动安装 Android System Image

set -e

# 1. 定义文件名和路径
ZIP_NAME="x86_64-36.1_r04.zip"
DOWNLOAD_URL="https://dl.google.com/android/repository/sys-img/google_apis_playstore/x86_64-36.1_r04.zip"
DOWNLOAD_PATH="$HOME/Downloads/$ZIP_NAME"
ANDROID_HOME="$HOME/Library/Android/sdk"

echo "🔍 正在检查下载文件: $DOWNLOAD_PATH"

# 2. 检查文件是否存在
if [ ! -f "$DOWNLOAD_PATH" ]; then
    echo "❌ 未找到文件！"
    echo "👉 请先在浏览器中下载该文件，并保存到 '下载(Downloads)' 文件夹中。"
    echo "🔗 下载地址: $DOWNLOAD_URL"
    exit 1
fi

echo "✅ 找到文件，准备安装..."

# 3. 创建临时解压目录
TEMP_DIR=$(mktemp -d)
echo "📦 正在解压 (可能需要几秒钟)..."
unzip -q "$DOWNLOAD_PATH" -d "$TEMP_DIR"

# 4. 解析 source.properties 获取正确的路径信息
# 假设解压后是一个名为 x86_64 的文件夹
SOURCE_PROP="$TEMP_DIR/x86_64/source.properties"

if [ ! -f "$SOURCE_PROP" ]; then
    echo "⚠️  解压结构不符合预期，尝试查找 source.properties..."
    SOURCE_PROP=$(find "$TEMP_DIR" -name "source.properties" | head -n 1)
    if [ -z "$SOURCE_PROP" ]; then
        echo "❌ 无法找到 source.properties，无法自动识别版本。"
        rm -rf "$TEMP_DIR"
        exit 1
    fi
fi

# 读取关键信息
get_prop() {
    grep "^$1=" "$SOURCE_PROP" | cut -d'=' -f2 | tr -d '[:space:]'
}

API_LEVEL=$(get_prop "AndroidVersion.ApiLevel")
TAG_ID=$(get_prop "SystemImage.TagId")
ABI=$(get_prop "SystemImage.Abi")

echo "ℹ️  识别到版本信息: API=$API_LEVEL, Tag=$TAG_ID, ABI=$ABI"

if [ -z "$API_LEVEL" ] || [ -z "$TAG_ID" ] || [ -z "$ABI" ]; then
    echo "❌ 版本信息识别不完整，退出。"
    rm -rf "$TEMP_DIR"
    exit 1
fi

# 5. 构建目标路径
# 规范路径: system-images/android-<ApiLevel>/<TagId>/<Abi>/
DEST_PARENT="$ANDROID_HOME/system-images/android-$API_LEVEL/$TAG_ID"
DEST_DIR="$DEST_PARENT/$ABI"

echo "🎯 目标安装路径: $DEST_DIR"

# 6. 安装 (移动文件)
mkdir -p "$DEST_PARENT"

# 如果目标目录已存在，先备份或删除
if [ -d "$DEST_DIR" ]; then
    echo "⚠️  目标目录已存在，正在清理旧文件..."
    rm -rf "$DEST_DIR"
fi

# 移动解压出的文件夹到目标位置
# 注意：我们需要移动的是包含 source.properties 的那个父文件夹 (通常是 x86_64)
SOURCE_DIR=$(dirname "$SOURCE_PROP")
mv "$SOURCE_DIR" "$DEST_DIR"

# 7. 清理
rm -rf "$TEMP_DIR"

echo "🎉 安装成功！"
echo "👉 请重启 Android Studio，或者在 Device Manager 中刷新，应该就能看到该系统镜像了。"
