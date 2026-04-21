# 词库扩展指南

## 📚 当前状态

**已实现**：
- ✅ 艾宾浩斯遗忘曲线算法（8个复习节点）
- ✅ 从JSON文件加载词库的功能
- ✅ 150个CET4核心词汇
- ✅ 数据库版本升级（支持学习进度追踪）

**下一步**：扩展词库到5000+单词

---

## 🎯 词库扩展方案

### 方案一：继续添加JSON文件（推荐）

#### 1. 创建更多词库文件

在 `app/src/main/assets/data/` 目录下创建：

```
app/src/main/assets/data/
├── cet4_words.json        (已完成，150词)
├── cet4_words_part2.json  (新建，500-1000词)
├── cet6_words.json        (新建，1000-1500词)
├── ielts_words.json       (新建，1000-1500词)
└── toefl_words.json       (新建，1000-1500词)
```

#### 2. JSON文件格式

每个JSON文件包含单词数组：

```json
[
  {
    "word": "abandon",
    "phonetic": "/əˈbændən/",
    "translation": "v. 抛弃，舍弃，放弃",
    "example": "We had to abandon the car and walk.",
    "difficulty": 2,
    "partOfSpeech": "v.",
    "library": "CET4"
  }
]
```

#### 3. 修改WordRepository加载新词库

在 `WordRepository.kt` 的 `loadDefaultWords()` 方法中添加：

```kotlin
private fun loadDefaultWords() {
    try {
        // 加载CET4词库（多个文件）
        val cet4Words = loadWordsFromJson("data/cet4_words.json")
        val cet4Words2 = loadWordsFromJson("data/cet4_words_part2.json")
        database.insertWords(cet4Words + cet4Words2)

        // 加载CET6词库
        val cet6Words = loadWordsFromJson("data/cet6_words.json")
        database.insertWords(cet6Words)

        // 加载IELTS词库
        val ieltsWords = loadWordsFromJson("data/ielts_words.json")
        database.insertWords(ieltsWords)

        // 加载TOEFL词库
        val toeflWords = loadWordsFromJson("data/toefl_words.json")
        database.insertWords(toeflWords)

        Log.d("WordRepository", "Total words loaded: ${cet4Words.size + cet6Words.size + ieltsWords.size + toeflWords.size}")
    } catch (e: Exception) {
        Log.e("WordRepository", "Error loading words: ${e.message}")
        val defaultWords = getAllDefaultWords()
        database.insertWords(defaultWords)
    }
}
```

---

### 方案二：使用网络爬虫/工具获取词库

#### 推荐工具：

1. **在线词库资源**：
   - 金山词霸API（免费）
   - 有道词典API
   - 扇贝单词API

2. **现有词库资源**：
   - GitHub搜索：`english vocabulary json`
   - ETS官方词表
   - 各类英语学习APP的词库导出

3. **Python爬虫脚本**：

```python
import requests
import json

# 示例：从在线词典获取CET4词汇
def fetch_cet4_words():
    url = "https://api.example.com/vocabulary/cet4"
    response = requests.get(url)
    words = response.json()

    formatted_words = []
    for word in words:
        formatted_words.append({
            "word": word["text"],
            "phonetic": word.get("phonetic", ""),
            "translation": word["translation"],
            "example": word.get("example", ""),
            "difficulty": 2,
            "partOfSpeech": word.get("pos", ""),
            "library": "CET4"
        })

    with open("cet4_words.json", "w", encoding="utf-8") as f:
        json.dump(formatted_words, f, ensure_ascii=False, indent=2)

fetch_cet4_words()
```

---

### 方案三：购买商业词库

#### 推荐资源：

1. ** Collins COBUILD** - 权威英语词汇库
2. **牛津高阶英汉双解词典** - 完整词库
3. **朗文当代英语词典** - LDOCE词库

---

## 📋 单词数据字段说明

| 字段 | 类型 | 必需 | 说明 |
|------|------|------|------|
| word | String | ✅ | 单词本身 |
| phonetic | String | ❌ | 音标（如：/əˈbændən/） |
| translation | String | ✅ | 中文翻译 |
| example | String | ❌ | 英文例句 |
| difficulty | Int | ❌ | 难度（1-5），默认1 |
| partOfSpeech | String | ❌ | 词性（如：v./n./adj.） |
| library | String | ✅ | 词库来源（CET4/CET6/IELTS等） |

---

## 🔧 快速添加单词

### 使用ChatGPT/Claude生成词库

**提示词示例**：

```
请为我生成500个CET4核心词汇，JSON格式：
- 包含word, phonetic, translation, example
- 选择最常用的500词
- 按字母顺序排列

格式：
[
  {
    "word": "abandon",
    "phonetic": "/əˈbændən/",
    "translation": "v. 抛弃，舍弃，放弃",
    "example": "We had to abandon the car and walk.",
    "difficulty": 2,
    "partOfSpeech": "v.",
    "library": "CET4"
  }
]
```

---

## ✅ 验证词库加载

### 1. 查看日志

运行APP后查看Logcat：

```
adb logcat | grep WordRepository
```

应该看到：

```
D/WordRepository: Loaded 150 words from CET4
D/WordRepository: Loaded 1000 words from CET6
```

### 2. 检查数据库

```kotlin
val count = database.getWordCount()
Log.d("Database", "Total words: $count")
```

---

## 📊 目标词库分布

| 词库 | 单词数量 | 文件名 |
|------|---------|--------|
| CET4 | 2000 | cet4_words.json + cet4_words_part2.json |
| CET6 | 2500 | cet6_words.json |
| IELTS | 3000 | ielts_words.json |
| TOEFL | 3000 | toefl_words.json |
| GRE | 5000 | gre_words.json |
| **总计** | **15500+** | |

---

## 🎁 额外优化建议

1. **添加词频统计**：标记单词的使用频率
2. **添加近义词/反义词**：帮助记忆
3. **添加词根词缀**：帮助理解单词构成
4. **添加图片/音频**：多媒体学习

---

## 📞 需要帮助？

如果遇到问题，请检查：
1. JSON文件格式是否正确
2. 文件是否在 `assets/data/` 目录
3. WordRepository是否正确引用文件路径
4. 查看Logcat的错误日志

---

**最后更新**：2025-01-XX
**当前版本**：v1.0
