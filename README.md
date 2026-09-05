# SimplePiano

这是一个最小可用的 Android 原生电子琴示例（Kotlin），使用合成正弦波发声，支持多点触控和音量控制。

如何运行：
1. 在你的电脑上安装 Android Studio（推荐）或让其他人用 Android Studio 打开此仓库。  
2. 打开项目（File → Open，选择仓库根目录），等待 Gradle 同步。  
3. 连接 Android 真机（开启开发者模式与 USB 调试）或启动模拟器。  
4. 点击 Run（绿色三角）以安装并运行应用。

说明：
- 代码包名：com.example.simplepiano
- 该实现使用合成音（正弦波），无需额外音频文件。  
- 若你需要更真实的钢琴音色，我可以把项目改为使用采样文件（需要加入 wav 资源）。

---

Minor update to trigger CI build: pushed by GitHub Copilot Chat Assistant on 2026-09-05.
