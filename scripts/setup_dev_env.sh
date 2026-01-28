#!/bin/bash

# setup_dev_env.sh
# 自动化配置 BeHealthy Android 开发环境

set -e  # 遇到错误立即停止

echo "🚀 [BeHealthy] 开始配置开发环境..."

# 1. 检查 Homebrew
if ! command -v brew &> /dev/null; then
    echo "❌ 未检测到 Homebrew，请手动安装 Homebrew 后重试: https://brew.sh/"
    exit 1
fi

echo "✅ 检测到 Homebrew"

# 2. 安装 JDK 17 (使用 Microsoft OpenJDK Cask，避免本地编译)
echo "☕️ [1/3] 正在检查并安装 JDK 17..."
if brew list --cask microsoft-openjdk@17 &>/dev/null; then
    echo "   Microsoft OpenJDK 17 已安装，跳过。"
else
    echo "   开始安装 Microsoft OpenJDK 17..."
    brew install --cask microsoft-openjdk@17
fi

# 3. 安装 Android Studio
echo "🤖 [2/3] 正在检查并安装 Android Studio..."
if [ -d "/Applications/Android Studio.app" ]; then
    echo "   Android Studio 已安装，跳过。"
else
    echo "   开始下载安装 Android Studio (这可能需要几分钟)..."
    # 使用 --force 覆盖安装，避免冲突
    brew install --cask android-studio
fi

# 4. 配置环境变量
echo "⚙️ [3/3] 配置环境变量..."
SHELL_CONFIG="$HOME/.zshrc"
[ -f "$HOME/.bash_profile" ] && SHELL_CONFIG="$HOME/.bash_profile"

# 追加配置到配置文件
if ! grep -q "ANDROID_HOME" "$SHELL_CONFIG"; then
    echo "" >> "$SHELL_CONFIG"
    echo "# Android Development Config" >> "$SHELL_CONFIG"
    echo 'export ANDROID_HOME=$HOME/Library/Android/sdk' >> "$SHELL_CONFIG"
    echo 'export PATH=$PATH:$ANDROID_HOME/emulator' >> "$SHELL_CONFIG"
    echo 'export PATH=$PATH:$ANDROID_HOME/platform-tools' >> "$SHELL_CONFIG"
    echo "✅ 已添加 ANDROID_HOME 到 $SHELL_CONFIG"
fi

# 更新 JAVA_HOME 配置逻辑
if ! grep -q "JAVA_HOME" "$SHELL_CONFIG"; then
    # 尝试自动定位 Microsoft JDK 17
    echo 'export JAVA_HOME="/Library/Java/JavaVirtualMachines/microsoft-17.jdk/Contents/Home"' >> "$SHELL_CONFIG"
    echo 'export PATH="$JAVA_HOME/bin:$PATH"' >> "$SHELL_CONFIG"
    echo "✅ 已添加 JAVA_HOME 到 $SHELL_CONFIG"
else
    # 如果已经存在 JAVA_HOME 但可能指向了错误的旧路径，建议用户检查，但这里不强行覆盖，以免破坏用户原有设置
    echo "ℹ️  检测到已存在 JAVA_HOME 配置，请确保它指向 JDK 17。"
fi

echo "🎉 环境配置脚本执行完毕！"
echo "👉 请执行 'source $SHELL_CONFIG' 使环境变量生效。"
echo "👉 请打开 Android Studio，完成 SDK 的初始化下载 (选择 Standard 安装)。"
