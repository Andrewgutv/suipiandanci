/**
 * 锁屏单词通知管理工具
 * 使用纯 JS 实现，可以云打包
 */

class WordNotification {
  constructor() {
    this.notificationId = 1001
    this.currentWord = null
    this.timer = null
  }

  /**
   * 显示单词通知
   * @param {Object} word - 单词对象
   */
  showWordNotification(word) {
    // #ifdef APP-PLUS
    try {
      this.currentWord = word

      // 创建通知内容
      const content = `${word.phonetic}\n${word.translation}`

      // 使用 plus.push 创建本地通知
      plus.push.createMessage(
        word.word,           // 标题
        content,             // 内容
        {                    // 选项
          payload: JSON.stringify({
            type: 'word_card',
            word: word.word,
            phonetic: word.phonetic,
            translation: word.translation,
            example: word.example
          }),
          options: {
            cover: false,           // 不覆盖之前的通知
            when: new Date(),       // 通知时间
            sound: 'system',        // 系统提示音
            icon: '/static/logo.png'  // 通知图标
          }
        }
      )

      // 同时创建一个持续显示的通知（Android）
      this.createPersistentNotification(word)

      return true
    } catch (e) {
      console.error('创建通知失败:', e)
      return false
    }
    // #endif

    // #ifndef APP-PLUS
    console.log('通知功能仅在 App 中可用')
    return false
    // #endif
  }

  /**
   * 创建持续显示的通知（Android）
   */
  createPersistentNotification(word) {
    // #ifdef APP-PLUS
    try {
      // 获取 Activity
      const main = plus.android.runtimeMainActivity()
      const Context = plus.android.importClass("android.content.Context")
      const NotificationManager = plus.android.importClass("android.app.NotificationManager")
      const Notification = plus.android.importClass("android.app.Notification")
      const NotificationCompat = plus.android.importClass("androidx.core.app.NotificationCompat")
      const PendingIntent = plus.android.importClass("android.app.PendingIntent")

      // 获取 NotificationManager
      const nm = main.getSystemService(Context.NOTIFICATION_SERVICE)

      // 创建点击意图
      const Intent = plus.android.importClass("android.content.Intent")
      const intent = new Intent()
      intent.setClassName(main, "io.dcloud.PandoraEntry")
      intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)

      const flags = PendingIntent.FLAG_UPDATE_CURRENT
      const pendingIntent = PendingIntent.getActivity(main, 0, intent, flags)

      // 构建通知
      const builder = new NotificationCompat.Builder(main, "word_channel")

      builder.setContentTitle(word.word)
      builder.setContentText(`${word.phonetic} - ${word.translation}`)
      builder.setSmallIcon(main.getApplicationInfo().icon)
      builder.setOngoing(true)  // 持续通知，不可滑动删除
      builder.setAutoCancel(false)  // 点击不自动取消
      builder.setContentIntent(pendingIntent)
      builder.setPriority(NotificationCompat.PRIORITY_HIGH)
      builder.setVisibility(NotificationCompat.VISIBILITY_PUBLIC)  // 锁屏可见

      // 设置样式（大文本）
      const BigTextStyle = plus.android.importClass("android.app.Notification.BigTextStyle")
      const style = new BigTextStyle()
      style.bigText(`${word.word}\n${word.phonetic}\n\n${word.translation}\n\n例句：\n${word.example}`)
      builder.setStyle(style)

      // 创建通知渠道（Android 8.0+）
      const NotificationChannel = plus.android.importClass("android.app.NotificationChannel")
      const importance = NotificationManager.IMPORTANCE_HIGH

      const channelId = "word_channel"
      const channelName = "单词学习"
      const channel = new NotificationChannel(channelId, channelName, importance)
      channel.setDescription("在锁屏显示单词卡片")
      channel.setLockscreenVisibility(NotificationCompat.VISIBILITY_PUBLIC)

      nm.createNotificationChannel(channel)

      // 显示通知
      const notification = builder.build()
      nm.notify(this.notificationId, notification)

      console.log('持续通知已创建')
    } catch (e) {
      console.error('创建持续通知失败:', e)
    }
    // #endif
  }

  /**
   * 清除通知
   */
  clearNotification() {
    // #ifdef APP-PLUS
    try {
      const main = plus.android.runtimeMainActivity()
      const Context = plus.android.importClass("android.content.Context")
      const NotificationManager = plus.android.importClass("android.app.NotificationManager")

      const nm = main.getSystemService(Context.NOTIFICATION_SERVICE)
      nm.cancel(this.notificationId)

      // 同时清除 plus.push 的通知
      plus.push.clear()

      this.currentWord = null

      console.log('通知已清除')
    } catch (e) {
      console.error('清除通知失败:', e)
    }
    // #endif
  }

  /**
   * 开启定时刷新
   * @param {Number} interval - 刷新间隔（分钟）
   * @param {Array} words - 单词列表
   */
  startAutoRefresh(interval, words) {
    this.stopAutoRefresh()  // 先清除之前的定时器

    const intervalMs = interval * 60 * 1000

    this.timer = setInterval(() => {
      const randomWord = words[Math.floor(Math.random() * words.length)]
      this.showWordNotification(randomWord)
      console.log('定时刷新单词:', randomWord.word)
    }, intervalMs)

    console.log(`已开启定时刷新，间隔: ${interval} 分钟`)
  }

  /**
   * 停止定时刷新
   */
  stopAutoRefresh() {
    if (this.timer) {
      clearInterval(this.timer)
      this.timer = null
      console.log('已停止定时刷新')
    }
  }

  /**
   * 请求通知权限（Android 13+）
   */
  async requestPermission() {
    // #ifdef APP-PLUS
    return new Promise((resolve) => {
      try {
        const main = plus.android.runtimeMainActivity()
        const Build = plus.android.importClass("android.os.Build")

        // Android 13 (API 33) 及以上需要请求通知权限
        if (Build.VERSION.SDK_INT >= 33) {
          const Manifest = plus.android.importClass("android.Manifest")
          const ActivityCompat = plus.android.importClass("androidx.core.app.ActivityCompat")
          const Context = plus.android.importClass("android.content.Context")

          // 检查权限
          const checkResult = ActivityCompat.checkSelfPermission(
            main,
            Manifest.permission.POST_NOTIFICATIONS
          )

          if (checkResult !== 0) {  // PERMISSION_GRANTED = 0
            // 请求权限
            ActivityCompat.requestPermissions(
              main,
              [Manifest.permission.POST_NOTIFICATIONS],
              0
            )
            console.log('已请求通知权限')
            resolve(false)  // 权限尚未授予
          } else {
            console.log('通知权限已授予')
            resolve(true)  // 已有权限
          }
        } else {
          // Android 13 以下不需要请求权限
          resolve(true)
        }
      } catch (e) {
        console.error('请求权限失败:', e)
        resolve(false)
      }
    })
    // #endif

    // #ifndef APP-PLUS
    return Promise.resolve(false)
    // #endif
  }

  /**
   * 检查通知权限状态
   */
  checkPermission() {
    // #ifdef APP-PLUS
    try {
      const main = plus.android.runtimeMainActivity()
      const Build = plus.android.importClass("android.os.Build")

      if (Build.VERSION.SDK_INT >= 33) {
        const Manifest = plus.android.importClass("android.Manifest")
        const ActivityCompat = plus.android.importClass("androidx.core.app.ActivityCompat")

        const checkResult = ActivityCompat.checkSelfPermission(
          main,
          Manifest.permission.POST_NOTIFICATIONS
        )

        return checkResult === 0  // 0 = PERMISSION_GRANTED
      }

      return true  // Android 13 以下默认有权限
    } catch (e) {
      console.error('检查权限失败:', e)
      return false
    }
    // #endif

    // #ifndef APP-PLUS
    return false
    // #endif
  }
}

// 导出单例
export default new WordNotification()
