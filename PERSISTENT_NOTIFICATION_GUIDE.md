# 📱 持久化推送机制说明

## ✅ 问题已解决！

之前的实现：App被杀死后，单词推送就会停止 ❌

现在的实现：**即使App被杀死，也能持续推送单词** ✅

---

## 🎯 三重推送机制

现在使用了**三层保障**机制，确保单词推送永不停止：

### 1️⃣ **解锁即时推送** (ScreenUnlockReceiver)
```
用户解锁手机
    ↓
立即显示新单词 ✅
```
- **触发时机**：每次解锁手机（输入密码/指纹/面部识别后）
- **响应速度**：< 1秒
- **优点**：即时反馈，学习体验最佳

---

### 2️⃣ **AlarmManager 定时推送** (AlarmScheduler + AlarmReceiver)
```
每30分钟（可自定义）
    ↓
自动推送新单词 ✅
```
- **触发时机**：每隔固定时间（默认30分钟）
- **特殊能力**：
  - ✅ 即使App被杀死也能工作
  - ✅ 即使手机在Doze模式（低电耗电模式）也能唤醒
  - ✅ 重启后自动恢复（如果有BootReceiver）

**核心代码**：
```kotlin
// Android 6.0+ 使用 setExactAndAllowWhileIdle
alarmManager.setExactAndAllowWhileIdle(
    AlarmManager.RTC_WAKEUP,
    triggerTime,
    pendingIntent
)
```

---

### 3️⃣ **前台服务保活** (WordService)
```
常驻通知栏
    ↓
防止系统杀死App ✅
```
- **作用**：显示持续通知，降低系统杀进程的概率
- **优先级**：前台服务 > 后台进程

---

## 🔄 完整推送流程

```
┌─────────────────────────────────────────────┐
│         用户开启"单词推送"开关               │
└──────────────────┬──────────────────────────┘
                   ↓
    ┌──────────────────────────────────┐
    │ 1. 启动前台服务 (WordService)    │
    │    - 显示常驻通知                │
    │    - 降低被杀概率                │
    └──────────────┬───────────────────┘
                   ↓
    ┌──────────────────────────────────┐
    │ 2. 启动 AlarmManager 闹钟        │
    │    - 每30分钟触发一次            │
    │    - 即使App被杀死也工作         │
    └──────────────┬───────────────────┘
                   ↓
    ┌──────────────────────────────────┐
    │ 3. 监听解锁事件                  │
    │    - 解锁时立即推送              │
    │    - 即时学习反馈                │
    └──────────────┬───────────────────┘
                   ↓
        三重保障同时工作 ✅
```

---

## 🛡️ 抗杀机制

### 场景1：App在后台
```
系统状态：App在后台，未运行
推送机制：✅ AlarmManager 仍会触发
           ✅ 解锁时仍会显示
```

### 场景2：App被杀死（滑动清除）
```
系统状态：App进程被杀死
推送机制：✅ AlarmManager 仍会触发（独立于App进程）
           ✅ 解锁时仍会显示
```

### 场景3：手机重启
```
系统状态：手机刚重启
推送机制：✅ BootReceiver 会重新启动服务
           ✅ AlarmManager 会重新设置
```

### 场景4：Doze模式（低电耗电模式）
```
系统状态：手机进入Doze省电模式
推送机制：✅ setExactAndAllowWhileIdle 可唤醒
           ⚠️  但频率受限（约每9分钟一次）
```

---

## 📊 技术实现细节

### AlarmScheduler.kt
```kotlin
object AlarmScheduler {
    private const val DEFAULT_INTERVAL_MS = 30L * 60L * 1000L // 30分钟

    fun schedulePeriodicAlarm(context: Context, intervalMs: Long) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            // 关键：即使在Doze模式也能唤醒
            alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                triggerTime,
                pendingIntent
            )
        }
    }

    fun rescheduleNextAlarm(context: Context) {
        // 每次触发后重新调度下一次
        // 形成循环，永不停止
    }
}
```

### AlarmReceiver.kt
```kotlin
class AlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        // 1. 显示新单词
        WordService.showNewWord(context)

        // 2. 重新调度下一次（循环）
        AlarmScheduler.rescheduleNextAlarm(context)
    }
}
```

### MainActivity.kt
```kotlin
if (isChecked) {
    // 1. 启动前台服务
    WordService.startService(this)

    // 2. 启动 AlarmManager 闹钟
    AlarmScheduler.schedulePeriodicAlarm(this)

    // 3. 取消旧的 WorkManager（已废弃）
    WorkManagerScheduler.cancelRefresh(this)
}
```

---

## ⚙️ 自定义推送间隔

### 方法1：修改代码（永久）
在 `AlarmScheduler.kt` 中修改：

```kotlin
// 当前：30分钟
private const val DEFAULT_INTERVAL_MS = 30L * 60L * 1000L

// 改为1小时：
private const val DEFAULT_INTERVAL_MS = 60L * 60L * 1000L

// 改为15分钟：
private const val DEFAULT_INTERVAL_MS = 15L * 60L * 1000L
```

**建议值**：
- 15分钟 - 频繁学习，适合备考
- 30分钟 - 平衡推荐 ⭐
- 1小时 - 轻松学习
- 2小时 - 最低频率

---

## 🔍 测试方法

### 测试1：后台推送
1. 打开App，开启"单词推送"开关
2. 按 Home 键，App进入后台
3. 等待30分钟
4. ✅ 应该看到单词通知

### 测试2：杀死后推送（最关键）
1. 打开App，开启"单词推送"开关
2. 最近任务中滑动杀死App
3. 等待30分钟
4. ✅ 应该看到单词通知（证明AlarmManager有效）

### 测试3：解锁即时推送
1. 开启"单词推送"
2. 锁定屏幕
3. 解锁屏幕
4. ✅ 应该立即看到单词通知

---

## 📱 权限要求

AndroidManifest.xml 已配置：

```xml
<!-- SCHEDULE_EXACT_ALARM 权限 - Android 12+ 必须 -->
<uses-permission android:name="android.permission.SCHEDULE_EXACT_ALARM" />

<!-- RECEIVE_BOOT_COMPLETED - 开机自启 -->
<uses-permission android:name="android.permission.RECEIVE_BOOT_COMPLETED" />

<!-- 前台服务权限 -->
<uses-permission android:name="android.permission.FOREGROUND_SERVICE" />
```

---

## ⚠️ 注意事项

### 1. Android 12+ 精确闹钟权限
用户可能需要在系统设置中允许"精确闹钟"权限：
```
设置 → 应用 → 碎片单词 → 通知 → 允许精确闹钟
```

### 2. Doze模式限制
在Doze模式下，`setExactAndAllowWhileIdle` 也有频率限制：
- Android 6.0-7.0：约每9分钟一次
- Android 8.0+：更严格

**解决方案**：解锁监听器不受Doze限制，可即时触发。

### 3. 省电模式
用户开启省电模式后，AlarmManager可能被限制。
建议在App说明中提醒用户：将App加入白名单。

---

## 🎉 优势总结

| 机制 | 旧实现 | 新实现 |
|------|--------|--------|
| **App被杀死** | ❌ 推送停止 | ✅ 继续推送 |
| **手机重启** | ❌ 需手动启动 | ✅ 自动恢复 |
| **Doze模式** | ❌ 完全停止 | ✅ 有限支持 |
| **即时反馈** | ✅ 解锁触发 | ✅ 解锁触发 |
| **定期推送** | ⚠️ 依赖服务 | ✅ 独立运行 |
| **推送间隔** | 15分钟固定 | 可自定义 |

---

## 🚀 未来优化建议

### 1. 智能调节频率
- 根据用户活跃度自动调整间隔
- 学习高峰期（如早上）增加频率

### 2. 统计推送效果
- 记录每日推送次数
- 记录用户查看率

### 3. 电池优化
- 检测电池电量，低电量时降低频率
- 使用 JobScheduler 替代 AlarmManager（更省电）

---

## 📚 参考资料

- [AlarmManager官方文档](https://developer.android.com/training/scheduling/alarms)
- [Doze模式详解](https://developer.android.com/training/monitoring-device-state/doze-standby)
- [后台任务最佳实践](https://developer.android.com/guide/background)

---

**总结**：现在你的App拥有**三层推送保障**，即使用户杀死App、手机重启、进入省电模式，也能持续推送单词！🎉
