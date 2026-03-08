<template>
  <view class="container">
    <view class="header">
      <text class="title">📖 我的生词本</text>
      <text class="subtitle">共 {{ unknownWords.length }} 个生词</text>
    </view>

    <view v-if="unknownWords.length === 0" class="empty-state">
      <text class="empty-icon">📝</text>
      <text class="empty-text">生词本是空的</text>
      <text class="empty-hint">在单词卡片中点击"不认识"来添加生词</text>
    </view>

    <scroll-view v-else scroll-y class="word-list">
      <view
        v-for="(word, index) in unknownWords"
        :key="index"
        class="word-item"
        @tap="showWordDetail(word)"
      >
        <view class="word-main">
          <text class="word-text">{{ word.word }}</text>
          <text class="word-phonetic">{{ word.phonetic }}</text>
        </view>
        <view class="word-detail">
          <text class="word-translation">{{ word.translation }}</text>
        </view>
        <view class="word-actions">
          <button class="btn-delete" @tap.stop="deleteWord(index)">删除</button>
        </view>
      </view>
    </scroll-view>

    <view v-if="unknownWords.length > 0" class="footer">
      <button class="btn-clear" @tap="clearAll">清空生词本</button>
      <button class="btn-back" @tap="goBack">返回首页</button>
    </view>
  </view>
</template>

<script>
export default {
  data() {
    return {
      unknownWords: []
    }
  },
  onLoad() {
    this.loadUnknownWords()
  },
  onShow() {
    this.loadUnknownWords()
  },
  methods: {
    loadUnknownWords() {
      try {
        const words = uni.getStorageSync('unknown_words')
        if (words) {
          this.unknownWords = JSON.parse(words)
        } else {
          this.unknownWords = []
        }
      } catch (e) {
        console.error('加载生词本失败', e)
        this.unknownWords = []
      }
    },

    showWordDetail(word) {
      uni.showModal({
        title: word.word,
        content: `${word.phonetic}\n\n${word.translation}\n\n例句：\n${word.example}`,
        showCancel: false,
        confirmText: '关闭'
      })
    },

    deleteWord(index) {
      uni.showModal({
        title: '确认删除',
        content: `确定要删除"${this.unknownWords[index].word}"吗？`,
        success: (res) => {
          if (res.confirm) {
            this.unknownWords.splice(index, 1)
            this.saveUnknownWords()
            uni.showToast({
              title: '已删除',
              icon: 'success'
            })
          }
        }
      })
    },

    clearAll() {
      if (this.unknownWords.length === 0) {
        return
      }

      uni.showModal({
        title: '确认清空',
        content: `确定要清空所有${this.unknownWords.length}个生词吗？`,
        success: (res) => {
          if (res.confirm) {
            this.unknownWords = []
            this.saveUnknownWords()
            uni.showToast({
              title: '已清空',
              icon: 'success'
            })
          }
        }
      })
    },

    saveUnknownWords() {
      try {
        uni.setStorageSync('unknown_words', JSON.stringify(this.unknownWords))
      } catch (e) {
        console.error('保存生词本失败', e)
        uni.showToast({
          title: '保存失败',
          icon: 'none'
        })
      }
    },

    goBack() {
      uni.navigateBack()
    }
  }
}
</script>

<style scoped>
.container {
  min-height: 100vh;
  background: #f8f8f8;
  padding-bottom: 120rpx;
}

.header {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  padding: 60rpx 40rpx 40rpx;
  text-align: center;
}

.title {
  display: block;
  font-size: 48rpx;
  font-weight: bold;
  color: #ffffff;
  margin-bottom: 16rpx;
}

.subtitle {
  display: block;
  font-size: 28rpx;
  color: rgba(255, 255, 255, 0.9);
}

.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 120rpx 60rpx;
}

.empty-icon {
  font-size: 120rpx;
  margin-bottom: 30rpx;
}

.empty-text {
  display: block;
  font-size: 36rpx;
  color: #333333;
  margin-bottom: 16rpx;
}

.empty-hint {
  display: block;
  font-size: 28rpx;
  color: #999999;
  text-align: center;
}

.word-list {
  padding: 30rpx;
}

.word-item {
  background: #ffffff;
  border-radius: 16rpx;
  padding: 30rpx;
  margin-bottom: 24rpx;
  box-shadow: 0 4rpx 12rpx rgba(0, 0, 0, 0.05);
}

.word-main {
  display: flex;
  align-items: baseline;
  margin-bottom: 16rpx;
}

.word-text {
  font-size: 40rpx;
  font-weight: bold;
  color: #333333;
  margin-right: 20rpx;
}

.word-phonetic {
  font-size: 28rpx;
  color: #999999;
  font-family: 'Lucida Sans Unicode', 'Arial Unicode MS', sans-serif;
}

.word-detail {
  margin-bottom: 20rpx;
}

.word-translation {
  font-size: 30rpx;
  color: #666666;
  line-height: 1.6;
}

.word-actions {
  display: flex;
  justify-content: flex-end;
  padding-top: 16rpx;
  border-top: 1px solid #f0f0f0;
}

.btn-delete {
  background: #f0f0f0;
  color: #ff4d4f;
  border: none;
  border-radius: 8rpx;
  padding: 12rpx 24rpx;
  font-size: 28rpx;
}

.footer {
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  background: #ffffff;
  padding: 24rpx 40rpx;
  box-shadow: 0 -4rpx 12rpx rgba(0, 0, 0, 0.05);
  display: flex;
  gap: 24rpx;
}

.btn-clear,
.btn-back {
  flex: 1;
  padding: 24rpx;
  border: none;
  border-radius: 12rpx;
  font-size: 32rpx;
  font-weight: bold;
}

.btn-clear {
  background: #fff1f0;
  color: #ff4d4f;
  border: 1px solid #ffccc7;
}

.btn-back {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: #ffffff;
}
</style>
