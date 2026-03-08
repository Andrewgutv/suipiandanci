<template>
  <view class="wordcard-container" :class="{ 'fade-out': isFadingOut }">
    <view class="word-card">
      <view class="word-icon">📚</view>

      <view class="word-content">
        <text class="word-text">{{ currentWord.word }}</text>
        <text class="phonetic">{{ currentWord.phonetic }}</text>

        <view v-if="showTranslation" class="translation-section">
          <text class="translation">{{ currentWord.translation }}</text>
          <text class="example">{{ currentWord.example }}</text>
        </view>
      </view>

      <view v-if="!showTranslation" class="action-buttons">
        <button class="btn-known" @tap="handleKnown">看过了</button>
        <button class="btn-unknown" @tap="handleUnknown">不认识</button>
      </view>

      <view v-else class="action-buttons">
        <button class="btn-add" @tap="addToNotebook">加入生词本</button>
      </view>
    </view>

    <view class="countdown-hint" v-if="!showTranslation && !isFadingOut">
      <text>{{ countdown }}秒后自动消失</text>
    </view>
  </view>
</template>

<script>
import wordData from '@/static/data/daily_use.json'

export default {
  data() {
    return {
      words: [],
      currentWord: {},
      showTranslation: false,
      isFadingOut: false,
      countdown: 3,
      countdownTimer: null,
      autoCloseTimer: null
    }
  },
  onLoad() {
    this.words = wordData.words
    this.showRandomWord()
    this.startCountdown()
  },
  onUnload() {
    this.clearTimers()
  },
  methods: {
    showRandomWord() {
      const randomIndex = Math.floor(Math.random() * this.words.length)
      this.currentWord = this.words[randomIndex]
      this.showTranslation = false
      this.isFadingOut = false
    },

    startCountdown() {
      this.countdown = 3
      this.countdownTimer = setInterval(() => {
        this.countdown--
        if (this.countdown <= 0) {
          this.clearTimers()
          this.autoClose()
        }
      }, 1000)
    },

    autoClose() {
      this.isFadingOut = true
      setTimeout(() => {
        uni.navigateBack()
      }, 500)
    },

    clearTimers() {
      if (this.countdownTimer) {
        clearInterval(this.countdownTimer)
        this.countdownTimer = null
      }
      if (this.autoCloseTimer) {
        clearTimeout(this.autoCloseTimer)
        this.autoCloseTimer = null
      }
    },

    handleKnown() {
      this.clearTimers()
      uni.showToast({
        title: '好的，继续加油！',
        icon: 'none',
        duration: 1500
      })
      setTimeout(() => {
        uni.navigateBack()
      }, 1500)
    },

    handleUnknown() {
      this.clearTimers()
      this.showTranslation = true
    },

    addToNotebook() {
      try {
        let unknownWords = []
        const stored = uni.getStorageSync('unknown_words')
        if (stored) {
          unknownWords = JSON.parse(stored)
        }

        // 检查是否已存在
        const exists = unknownWords.some(w => w.word === this.currentWord.word)
        if (!exists) {
          unknownWords.push({
            ...this.currentWord,
            addTime: new Date().toISOString()
          })
          uni.setStorageSync('unknown_words', JSON.stringify(unknownWords))

          uni.showToast({
            title: '已加入生词本',
            icon: 'success',
            duration: 1500
          })
        } else {
          uni.showToast({
            title: '已在生词本中',
            icon: 'none',
            duration: 1500
          })
        }

        setTimeout(() => {
          uni.navigateBack()
        }, 1500)
      } catch (e) {
        console.error('保存生词失败', e)
        uni.showToast({
          title: '保存失败',
          icon: 'none'
        })
      }
    }
  }
}
</script>

<style scoped>
.wordcard-container {
  min-height: 100vh;
  background: rgba(0, 0, 0, 0.6);
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 60rpx;
  transition: opacity 0.5s ease;
}

.fade-out {
  opacity: 0;
}

.word-card {
  background: #ffffff;
  border-radius: 32rpx;
  padding: 60rpx 50rpx;
  width: 100%;
  box-shadow: 0 16rpx 48rpx rgba(0, 0, 0, 0.2);
  animation: slideUp 0.3s ease;
}

@keyframes slideUp {
  from {
    transform: translateY(100rpx);
    opacity: 0;
  }
  to {
    transform: translateY(0);
    opacity: 1;
  }
}

.word-icon {
  text-align: center;
  font-size: 80rpx;
  margin-bottom: 30rpx;
}

.word-content {
  text-align: center;
  margin-bottom: 50rpx;
}

.word-text {
  display: block;
  font-size: 56rpx;
  font-weight: bold;
  color: #333333;
  margin-bottom: 20rpx;
  text-transform: capitalize;
}

.phonetic {
  display: block;
  font-size: 32rpx;
  color: #999999;
  margin-bottom: 20rpx;
  font-family: 'Lucida Sans Unicode', 'Arial Unicode MS', sans-serif;
}

.translation-section {
  margin-top: 30rpx;
  text-align: left;
  padding-top: 30rpx;
  border-top: 1px solid #f0f0f0;
}

.translation {
  display: block;
  font-size: 32rpx;
  color: #333333;
  line-height: 1.6;
  margin-bottom: 20rpx;
}

.example {
  display: block;
  font-size: 28rpx;
  color: #666666;
  line-height: 1.8;
  font-style: italic;
}

.action-buttons {
  display: flex;
  gap: 24rpx;
}

.btn-known,
.btn-unknown,
.btn-add {
  flex: 1;
  padding: 28rpx;
  border: none;
  border-radius: 16rpx;
  font-size: 32rpx;
  font-weight: bold;
}

.btn-known {
  background: #f0f0f0;
  color: #666666;
}

.btn-unknown {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: #ffffff;
}

.btn-add {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: #ffffff;
  width: 100%;
}

.countdown-hint {
  position: fixed;
  bottom: 100rpx;
  background: rgba(0, 0, 0, 0.5);
  padding: 16rpx 32rpx;
  border-radius: 40rpx;
}

.countdown-hint text {
  color: #ffffff;
  font-size: 28rpx;
}
</style>
