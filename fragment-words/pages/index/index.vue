<template>
  <view class="container">
    <view class="header">
      <text class="title">📚 碎片单词</text>
      <text class="subtitle">轻量级无压力英语学习</text>
    </view>

    <view class="content">
      <view class="card">
        <text class="card-title">🔐 锁屏单词通知</text>
        <text class="card-desc">开启后，单词会在通知栏和锁屏显示</text>
        <view class="status-row" v-if="lockScreenEnabled">
          <text class="status-text">✅ 已开启</text>
          <button class="stop-btn" @tap="disableLockScreen">关闭</button>
        </view>
        <button v-else class="action-btn" @tap="enableLockScreen">
          🚀 开启锁屏单词
        </button>
      </view>

      <view class="card">
        <text class="card-title">模拟锁屏单词</text>
        <text class="card-desc">点击下方按钮模拟解锁手机时的单词卡片效果</text>
        <button class="action-btn secondary" @tap="showWordCard">
          ✨ 显示单词卡片
        </button>
      </view>

      <view class="card">
        <text class="card-title">使用说明</text>
        <text class="info-text">• 开启后，单词会显示在手机锁屏通知中</text>
        <text class="info-text">• 点击通知可以打开应用</text>
        <text class="info-text">• 可以设置定时刷新，自动学习新单词</text>
      </view>

      <view class="card" @tap="goToNotebook">
        <text class="card-title">📖 我的生词本</text>
        <text class="card-desc">当前有 {{ unknownWords.length }} 个生词</text>
      </view>
    </view>
  </view>
</template>

<script>
import wordData from '@/static/data/daily_use.json'
import notification from '@/utils/notification.js'

export default {
  data() {
    return {
      unknownWords: [],
      words: [],
      lockScreenEnabled: false
    }
  },
  onLoad() {
    this.words = wordData.words
    this.loadUnknownWords()
    this.checkLockScreenStatus()
  },
  onShow() {
    this.loadUnknownWords()
  },
  onUnload() {
    // 页面卸载时停止定时刷新
    if (this.lockScreenEnabled) {
      notification.stopAutoRefresh()
    }
  },
  methods: {
    // 检查锁屏状态
    checkLockScreenStatus() {
      const enabled = uni.getStorageSync('lock_screen_enabled')
      this.lockScreenEnabled = enabled || false

      if (this.lockScreenEnabled) {
        // 如果已开启，显示当前单词
        this.showCurrentNotification()
      }
    },

    // 显示当前通知
    showCurrentNotification() {
      const savedWord = uni.getStorageSync('current_word')
      if (savedWord) {
        try {
          const word = JSON.parse(savedWord)
          notification.showWordNotification(word)
        } catch (e) {
          console.error('解析当前单词失败', e)
        }
      }
    },

    // 开启锁屏单词
    async enableLockScreen() {
      // #ifdef APP-PLUS
      // 检查通知权限
      const hasPermission = notification.checkPermission()

      if (!hasPermission) {
        const granted = await notification.requestPermission()
        if (!granted) {
          uni.showModal({
            title: '需要通知权限',
            content: '请在系统设置中允许应用发送通知，以便在锁屏显示单词',
            confirmText: '去设置',
            cancelText: '取消',
            success: (res) => {
              if (res.confirm) {
                // 打开系统设置
                const main = plus.android.runtimeMainActivity()
                const Intent = plus.android.importClass("android.content.Intent")
                const Settings = plus.android.importClass("android.provider.Settings")
                const intent = new Intent()
                intent.setAction(Settings.ACTION_APPLICATION_NOTIFICATION_SETTINGS)
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                main.startActivity(intent)
              }
            }
          })
          return
        }
      }

      // 显示确认对话框
      uni.showModal({
        title: '开启锁屏单词',
        content: '开启后：\n\n• 单词会在通知栏持续显示\n• 锁屏后可以看到单词\n• 可以设置定时自动刷新\n• 每小时学习一个新单词',
        confirmText: '开启',
        cancelText: '取消',
        success: (res) => {
          if (res.confirm) {
            this.startLockScreen()
          }
        }
      })
      // #endif

      // #ifndef APP-PLUS
      uni.showToast({
        title: '此功能仅在 App 中可用',
        icon: 'none'
      })
      // #endif
    },

    // 启动锁屏功能
    startLockScreen() {
      // #ifdef APP-PLUS
      try {
        // 选择一个随机单词
        const randomWord = this.words[Math.floor(Math.random() * this.words.length)]

        // 显示通知
        const success = notification.showWordNotification(randomWord)

        if (success) {
          // 保存状态
          this.lockScreenEnabled = true
          uni.setStorageSync('lock_screen_enabled', true)
          uni.setStorageSync('current_word', JSON.stringify(randomWord))

          // 开启定时刷新（每60分钟）
          notification.startAutoRefresh(60, this.words)

          uni.showToast({
            title: '锁屏单词已开启！',
            icon: 'success',
            duration: 2000
          })

          // 提示用户查看
          setTimeout(() => {
            uni.showModal({
              title: '查看效果',
              content: '请锁屏手机，然后在锁屏界面或通知栏查看单词！',
              showCancel: false
            })
          }, 500)
        } else {
          uni.showToast({
            title: '开启失败',
            icon: 'none'
          })
        }
      } catch (e) {
        console.error('开启锁屏失败', e)
        uni.showToast({
          title: '开启失败：' + e.message,
          icon: 'none'
        })
      }
      // #endif
    },

    // 关闭锁屏单词
    disableLockScreen() {
      // #ifdef APP-PLUS
      uni.showModal({
        title: '关闭锁屏单词',
        content: '确定要关闭锁屏单词功能吗？',
        success: (res) => {
          if (res.confirm) {
            // 清除通知
            notification.clearNotification()
            notification.stopAutoRefresh()

            // 清除状态
            this.lockScreenEnabled = false
            uni.removeStorageSync('lock_screen_enabled')
            uni.removeStorageSync('current_word')

            uni.showToast({
              title: '已关闭',
              icon: 'success'
            })
          }
        }
      })
      // #endif
    },

    // 显示模拟的单词卡片（App内页面）
    showWordCard() {
      uni.navigateTo({
        url: '/pages/wordcard/wordcard'
      })
    },

    goToNotebook() {
      uni.navigateTo({
        url: '/pages/notebook/notebook'
      })
    },

    loadUnknownWords() {
      try {
        const words = uni.getStorageSync('unknown_words')
        if (words) {
          this.unknownWords = JSON.parse(words)
        }
      } catch (e) {
        console.error('加载生词本失败', e)
      }
    }
  }
}
</script>

<style scoped>
.container {
  min-height: 100vh;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  padding: 40rpx;
}

.header {
  text-align: center;
  margin-bottom: 60rpx;
  padding-top: 60rpx;
}

.title {
  display: block;
  font-size: 48rpx;
  font-weight: bold;
  color: #ffffff;
  margin-bottom: 20rpx;
}

.subtitle {
  display: block;
  font-size: 28rpx;
  color: rgba(255, 255, 255, 0.8);
}

.content {
  display: flex;
  flex-direction: column;
  gap: 30rpx;
}

.card {
  background: #ffffff;
  border-radius: 24rpx;
  padding: 40rpx;
  box-shadow: 0 8rpx 24rpx rgba(0, 0, 0, 0.1);
}

.card-title {
  display: block;
  font-size: 36rpx;
  font-weight: bold;
  color: #333333;
  margin-bottom: 20rpx;
}

.card-desc {
  display: block;
  font-size: 28rpx;
  color: #666666;
  margin-bottom: 30rpx;
  line-height: 1.6;
}

.action-btn {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: #ffffff;
  border: none;
  border-radius: 16rpx;
  padding: 24rpx;
  font-size: 32rpx;
  font-weight: bold;
  box-shadow: 0 4rpx 12rpx rgba(102, 126, 234, 0.4);
}

.action-btn.secondary {
  background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%);
}

.status-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-top: 20rpx;
}

.status-text {
  font-size: 28rpx;
  color: #4CAF50;
  font-weight: bold;
}

.stop-btn {
  background: #ff4444;
  color: #ffffff;
  border: none;
  border-radius: 12rpx;
  padding: 16rpx 32rpx;
  font-size: 28rpx;
}

.info-text {
  display: block;
  font-size: 28rpx;
  color: #666666;
  margin-bottom: 16rpx;
  line-height: 1.8;
}
</style>
