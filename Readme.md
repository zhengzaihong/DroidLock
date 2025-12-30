特权管理你的设备。


> [!NOTE]
> ColorOS用户应在GitHub上的releases下载testkey版本

## 功能

- 系统
  - 选项：禁用摄像头、禁止截屏、全局静音、禁用USB信号...
  - 管理CA证书
  - _清除数据_
  - ...
- 网络
  - 添加/修改/删除 Wi-Fi
  - 网络统计
  - 最小Wi-Fi安全等级
  - VPN保持打开
  - 网络日志
  - ...
- 工作资料
  - 创建工作资料
  - 挂起个人应用
  - ...
- 应用管理
  - 挂起/隐藏应用
  - 阻止应用卸载
  - 安装/卸载应用
  - 授予/撤销权限
  - ...
- 用户限制
  - 网络：禁止配置移动网络、禁止配置Wi-Fi、禁用短信、禁止拨出电话...
  - 连接：禁用蓝牙、禁止配置定位、禁用USB文件传输、禁用打印...
  - 应用：禁止安装/卸载应用...
  - 用户：禁止添加/删除/切换用户...
  - 媒体：禁止调整亮度、禁止调整音量...
  - 其他：禁止修改账号、禁止修改语言、禁止恢复出厂设置、禁用调试功能...
- 用户管理
  - 用户信息
  - 启动/切换/停止/删除用户
  - 创建用户
  - ...
- 密码与锁屏
  - _重置密码_
  - 要求密码复杂度
  - 设置屏幕超时
  - ...

## 激活

- Shizuku (推荐)
- 在ADB命令行中执行命令: `dpm set-device-owner com.zzh.droidlock/.Receiver`

## FAQ

### 激活

#### 设备上有账号

```text
java.lang.IllegalStateException: Not allowed to set the device owner because there are already some accounts on the device
```

解决办法：
- 冻结持有这些账号的app。
- 删除这些账号。
- 使用LSPosed模块 [HookDPM](https://github.com/BinTianqi/HookDPM)。

#### 设备上有多个用户

```text
java.lang.IllegalStateException: Not allowed to set the device owner because there are already several users on the device
```

解决办法：
- 删除次级用户。
- 使用LSPosed模块[HookDPM](https://github.com/BinTianqi/HookDPM)。

> [!NOTE]
> 一些系统有应用克隆、儿童空间等功能，它们通常是用户。

#### MIUI

```text
java.lang.SecurityException: Neither user 2000 nor current process has android.permission.MANAGE_DEVICE_ADMINS.
```

解决办法：
- 在开发者设置中打开`USB调试（安全设置）`。
- 在root命令行中执行激活命令

#### ColorOS

```text
java.lang.IllegalStateException: Unexpected @ProvisioningPreCondition
```

解决办法：使用 OwnDroid testkey 版本

## API

| ID                     | Extra         | 最小安卓版本 |
|------------------------|---------------|:------:|
| HIDE                   | `package`     |        |
| UNHIDE                 | `package`     |        |
| SUSPEND                | `package`     |   7    |
| UNSUSPEND              | `package`     |   7    |
| ADD_USER_RESTRICTION   | `restriction` |        |
| CLEAR_USER_RESTRICTION | `restriction` |        |
| LOCK                   |               |        |

[可用的用户限制](https://developer.android.google.cn/reference/android/os/UserManager#constants_1)

```shell
# 一个在ADB shell中隐藏app的示例
am broadcast -a com.zzh.droidlock.action.HIDE -n com.zzh.droidlock/.ApiReceiver --es key abcdefg --es package com.example.app
```

```kotlin
// 一个在Kotlin中隐藏app的示例
val intent = Intent("com.zzh.droidlock.action.HIDE")
    .setComponent(ComponentName("com.zzh.droidlock", "com.zzh.droidlock.ApiReceiver"))
    .putExtra("key", "abcdefg")
    .putExtra("package", "com.example.app")
context.sendBroadcast(intent)
```

## 构建

你可以在命令行中使用Gradle以构建OwnDroid
```shell
# 使用testkey签名（默认）
./gradlew build
# 使用你的jks密钥签名
./gradlew build -PStoreFile="/path/to/your/jks/file" -PStorePassword="YOUR_KEYSTORE_PASSWORD" -PKeyPassword="YOUR_KEY_PASSWORD" -PKeyAlias="YOUR_KEY_ALIAS"
```
（在Windows系统中应使用`./gradlew.bat`)

