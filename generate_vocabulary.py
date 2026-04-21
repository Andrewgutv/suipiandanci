#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
词库批量生成工具

使用方法：
1. 安装依赖：pip install requests
2. 运行脚本：python generate_vocabulary.py

注意：此脚本使用示例词汇，实际使用时需要接入真实词库API或使用ChatGPT生成
"""

import json
import os
from typing import List, Dict

# CET4核心词汇示例（可以通过ChatGPT或API获取更多）
CET4_WORDS_PART3 = [
    {
        "word": "chaos",
        "phonetic": "/ˈkeɪɒs/",
        "translation": "n. 混乱",
        "example": "The room was in chaos.",
        "difficulty": 3,
        "partOfSpeech": "n.",
        "library": "CET4"
    },
    {
        "word": "chap",
        "phonetic": "/tʃæp/",
        "translation": "n. 家伙",
        "example": "A nice chap.",
        "difficulty": 2,
        "partOfSpeech": "n.",
        "library": "CET4"
    },
    {
        "word": "chapter",
        "phonetic": "/ˈtʃæptər/",
        "translation": "n. 章节",
        "example": "Read chapter 5.",
        "difficulty": 2,
        "partOfSpeech": "n.",
        "library": "CET4"
    },
    {
        "word": "character",
        "phonetic": "/ˈkærəktər/",
        "translation": "n. 性格，角色",
        "example": "He has a strong character.",
        "difficulty": 3,
        "partOfSpeech": "n.",
        "library": "CET4"
    },
    {
        "word": "characteristic",
        "phonetic": "/ˌkærəktəˈrɪstɪk/",
        "translation": "adj. 特有的 n. 特征",
        "example": "A characteristic feature.",
        "difficulty": 4,
        "partOfSpeech": "adj./n.",
        "library": "CET4"
    },
    {
        "word": "charge",
        "phonetic": "/tʃɑːdʒ/",
        "translation": "v./n. 收费，控告 n. 电荷",
        "example": "How much do you charge?",
        "difficulty": 2,
        "partOfSpeech": "v./n.",
        "library": "CET4"
    },
    {
        "word": "charity",
        "phonetic": "/ˈtʃærəti/",
        "translation": "n. 慈善",
        "example": "Donate to charity.",
        "difficulty": 3,
        "partOfSpeech": "n.",
        "library": "CET4"
    },
    {
        "word": "charm",
        "phonetic": "/tʃɑːm/",
        "translation": "n. 魅力 v. 迷住",
        "example": "She has great charm.",
        "difficulty": 3,
        "partOfSpeech": "n./v.",
        "library": "CET4"
    },
    {
        "word": "chart",
        "phonetic": "/tʃɑːt/",
        "translation": "n. 图表",
        "example": "Look at the chart.",
        "difficulty": 2,
        "partOfSpeech": "n.",
        "library": "CET4"
    },
    {
        "word": "chase",
        "phonetic": "/tʃeɪs/",
        "translation": "v./n. 追逐",
        "example": "Chase your dreams.",
        "difficulty": 2,
        "partOfSpeech": "v./n.",
        "library": "CET4"
    },
    {
        "word": "chat",
        "phonetic": "/tʃæt/",
        "translation": "v./n. 聊天",
        "example": "Let's chat.",
        "difficulty": 1,
        "partOfSpeech": "v./n.",
        "library": "CET4"
    },
    {
        "word": "cheap",
        "phonetic": "/tʃiːp/",
        "translation": "adj. 便宜的",
        "example": "Very cheap.",
        "difficulty": 1,
        "partOfSpeech": "adj.",
        "library": "CET4"
    },
    {
        "word": "cheat",
        "phonetic": "/tʃiːt/",
        "translation": "v./n. 欺骗",
        "example": "Don't cheat.",
        "difficulty": 2,
        "partOfSpeech": "v./n.",
        "library": "CET4"
    },
    {
        "word": "check",
        "phonetic": "/tʃek/",
        "translation": "v./n. 检查",
        "example": "Check your email.",
        "difficulty": 1,
        "partOfSpeech": "v./n.",
        "library": "CET4"
    },
    {
        "word": "cheek",
        "phonetic": "/tʃiːk/",
        "translation": "n. 脸颊",
        "example": "Rosy cheeks.",
        "difficulty": 2,
        "partOfSpeech": "n.",
        "library": "CET4"
    },
    {
        "word": "cheer",
        "phonetic": "/tʃɪər/",
        "translation": "v. 欢呼 n. 欢呼",
        "example": "Cheer up!",
        "difficulty": 2,
        "partOfSpeech": "v./n.",
        "library": "CET4"
    },
    {
        "word": "cheerful",
        "phonetic": "/ˈtʃɪəfl/",
        "translation": "adj. 快乐的",
        "example": "A cheerful song.",
        "difficulty": 2,
        "partOfSpeech": "adj.",
        "library": "CET4"
    },
    {
        "word": "cheese",
        "phonetic": "/tʃiːz/",
        "translation": "n. 奶酪",
        "example": "I like cheese.",
        "difficulty": 1,
        "partOfSpeech": "n.",
        "library": "CET4"
    },
    {
        "word": "chef",
        "phonetic": "/ʃef/",
        "translation": "n. 厨师",
        "example": "The chef is excellent.",
        "difficulty": 2,
        "partOfSpeech": "n.",
        "library": "CET4"
    },
    {
        "word": "chemical",
        "phonetic": "/ˈkemɪkl/",
        "translation": "adj. 化学的 n. 化学品",
        "example": "Chemical reaction.",
        "difficulty": 3,
        "partOfSpeech": "adj./n.",
        "library": "CET4"
    },
    {
        "word": "chemist",
        "phonetic": "/ˈkemɪst/",
        "translation": "n. 化学家，药剂师",
        "example": "Consult the chemist.",
        "difficulty": 3,
        "partOfSpeech": "n.",
        "library": "CET4"
    },
    {
        "word": "chemistry",
        "phonetic": "/ˈkemɪstri/",
        "translation": "n. 化学",
        "example": "Study chemistry.",
        "difficulty": 3,
        "partOfSpeech": "n.",
        "library": "CET4"
    },
    {
        "word": "cheque",
        "phonetic": "/tʃek/",
        "translation": "n. 支票",
        "example": "Pay by cheque.",
        "difficulty": 3,
        "partOfSpeech": "n.",
        "library": "CET4"
    },
    {
        "word": "cherish",
        "phonetic": "/ˈtʃerɪʃ/",
        "translation": "v. 珍爱",
        "example": "Cherish your memories.",
        "difficulty": 4,
        "partOfSpeech": "v.",
        "library": "CET4"
    },
    {
        "word": "cherry",
        "phonetic": "/ˈtʃeri/",
        "translation": "n. 樱桃",
        "example": "Cherry blossoms.",
        "difficulty": 2,
        "partOfSpeech": "n.",
        "library": "CET4"
    },
    {
        "word": "chess",
        "phonetic": "/tʃes/",
        "translation": "n. 国际象棋",
        "example": "Play chess.",
        "difficulty": 2,
        "partOfSpeech": "n.",
        "library": "CET4"
    },
    {
        "word": "chest",
        "phonetic": "/tʃest/",
        "translation": "n. 胸部",
        "example": "Chest pain.",
        "difficulty": 2,
        "partOfSpeech": "n.",
        "library": "CET4"
    },
    {
        "word": "chew",
        "phonetic": "/tʃuː/",
        "translation": "v. 咀嚼",
        "example": "Chew your food well.",
        "difficulty": 2,
        "partOfSpeech": "v.",
        "library": "CET4"
    },
    {
        "word": "chicken",
        "phonetic": "/ˈtʃɪkɪn/",
        "translation": "n. 鸡肉",
        "example": "Fried chicken.",
        "difficulty": 1,
        "partOfSpeech": "n.",
        "library": "CET4"
    },
    {
        "word": "chief",
        "phonetic": "/tʃiːf/",
        "translation": "adj. 主要的 n. 首领",
        "example": "Chief reason.",
        "difficulty": 3,
        "partOfSpeech": "adj./n.",
        "library": "CET4"
    },
    {
        "word": "child",
        "phonetic": "/tʃaɪld/",
        "translation": "n. 孩子",
        "example": "Love your child.",
        "difficulty": 1,
        "partOfSpeech": "n.",
        "library": "CET4"
    },
    {
        "word": "childhood",
        "phonetic": "/ˈtʃaɪldhʊd/",
        "translation": "n. 童年",
        "example": "Happy childhood.",
        "difficulty": 2,
        "partOfSpeech": "n.",
        "library": "CET4"
    },
    {
        "word": "chill",
        "phonetic": "/tʃɪl/",
        "translation": "v. 使冷却 n. 寒冷",
        "example": "Chill the wine.",
        "difficulty": 2,
        "partOfSpeech": "v./n.",
        "library": "CET4"
    },
    {
        "word": "chimney",
        "phonetic": "/ˈtʃɪmni/",
        "translation": "n. 烟囱",
        "example": "Santa comes down the chimney.",
        "difficulty": 3,
        "partOfSpeech": "n.",
        "library": "CET4"
    },
    {
        "word": "chin",
        "phonetic": "/tʃɪn/",
        "translation": "n. 下巴",
        "example": "Raise your chin.",
        "difficulty": 2,
        "partOfSpeech": "n.",
        "library": "CET4"
    },
    {
        "word": "china",
        "phonetic": "/ˈtʃaɪnə/",
        "translation": "n. 瓷器",
        "example": "Fine china.",
        "difficulty": 2,
        "partOfSpeech": "n.",
        "library": "CET4"
    },
    {
        "word": "chip",
        "phonetic": "/tʃɪp/",
        "translation": "n. 芯片，碎片 v. 切碎",
        "example": "Potato chips.",
        "difficulty": 2,
        "partOfSpeech": "n./v.",
        "library": "CET4"
    },
    {
        "word": "choice",
        "phonetic": "/tʃɔɪs/",
        "translation": "n. 选择",
        "example": "Make a choice.",
        "difficulty": 2,
        "partOfSpeech": "n.",
        "library": "CET4"
    },
    {
        "word": "choose",
        "phonetic": "/tʃuːz/",
        "translation": "v. 选择",
        "example": "Choose wisely.",
        "difficulty": 2,
        "partOfSpeech": "v.",
        "library": "CET4"
    },
    {
        "word": "chorus",
        "phonetic": "/ˈkɔːrəs/",
        "translation": "n. 合唱队",
        "example": "Join the chorus.",
        "difficulty": 3,
        "partOfSpeech": "n.",
        "library": "CET4"
    },
    {
        "word": "choose",
        "phonetic": "/tʃuːz/",
        "translation": "v. 选择",
        "example": "Choose your path.",
        "difficulty": 2,
        "partOfSpeech": "v.",
        "library": "CET4"
  },
    {
        "word": "Christian",
        "phonetic": "/ˈkrɪstʃən/",
        "translation": "n./adj. 基督教徒（的）",
        "example": "A Christian family.",
        "difficulty": 3,
        "partOfSpeech": "n./adj.",
        "library": "CET4"
    },
    {
        "word": "Christmas",
        "phonetic": "/ˈkrɪsməs/",
        "translation": "n. 圣诞节",
        "example": "Merry Christmas!",
        "difficulty": 2,
        "partOfSpeech": "n.",
        "library": "CET4"
    },
    {
        "word": "church",
        "phonetic": "/tʃɜːtʃ/",
        "translation": "n. 教堂",
        "example": "Go to church.",
        "difficulty": 1,
        "partOfSpeech": "n.",
        "library": "CET4"
    },
    {
        "word": "cigarette",
        "phonetic": "/ˌsɪɡəˈret/",
        "translation": "n. 香烟",
        "example": "Quit smoking cigarettes.",
        "difficulty": 3,
        "partOfSpeech": "n.",
        "library": "CET4"
    },
    {
        "word": "cinema",
        "phonetic": "/ˈsɪnəmə/",
        "translation": "n. 电影院",
        "example": "Let's go to the cinema.",
        "difficulty": 2,
        "partOfSpeech": "n.",
        "library": "CET4"
    },
    {
        "word": "circle",
        "phonetic": "/ˈsɜːkl/",
        "translation": "n. 圆圈 v. 包围",
        "example": "Stand in a circle.",
        "difficulty": 2,
        "partOfSpeech": "n./v.",
        "library": "CET4"
    },
    {
        "word": "circumstance",
        "phonetic": "/ˈsɜːkəmstɑːns/",
        "translation": "n. 环境，情况",
        "example": "Under no circumstance.",
        "difficulty": 4,
        "partOfSpeech": "n.",
        "library": "CET4"
    },
    {
        "word": "circus",
        "phonetic": "/ˈsɜːkəs/",
        "translation": "n. 马戏团",
        "example": "The circus is in town.",
        "difficulty": 2,
        "partOfSpeech": "n.",
        "library": "CET4"
    },
    {
        "word": "citizen",
        "phonetic": "/ˈsɪtɪzn/",
        "translation": "n. 公民",
        "example": "A citizen of China.",
        "difficulty": 3,
        "partOfSpeech": "n.",
        "library": "CET4"
  },
    {
        "word": "city",
        "phonetic": "/ˈsɪti/",
        "translation": "n. 城市",
        "example": "New York City.",
        "difficulty": 1,
        "partOfSpeech": "n.",
        "library": "CET4"
    },
    {
        "word": "civil",
        "phonetic": "/ˈsɪvl/",
        "translation": "adj. 公民的，文明的",
        "example": "Civil war.",
        "difficulty": 3,
        "partOfSpeech": "adj.",
        "library": "CET4"
    },
    {
        "word": "civilian",
        "phonetic": "/səˈvɪliən/",
        "translation": "n. 平民",
        "example": "Civilian casualties.",
        "difficulty": 4,
        "partOfSpeech": "n.",
        "library": "CET4"
    },
    {
        "word": "civilization",
        "phonetic": "/ˌsɪvəlaɪˈzeɪʃn/",
        "translation": "n. 文明",
        "example": "Ancient civilization.",
        "difficulty": 4,
        "partOfSpeech": "n.",
        "library": "CET4"
    },
    {
        "word": "claim",
        "phonetic": "/kleɪm/",
        "translation": "v./n. 声称，索赔",
        "example": "Claim your prize.",
        "difficulty": 3,
        "partOfSpeech": "v./n.",
        "library": "CET4"
    },
    {
        "word": "clap",
        "phonetic": "/klæp/",
        "translation": "v./n. 拍手",
        "example": "Give her a clap.",
        "difficulty": 2,
        "partOfSpeech": "v./n.",
        "library": "CET4"
    },
    {
        "word": "clarify",
        "phonetic": "/ˈklærəfaɪ/",
        "translation": "v. 澄清",
        "example": "Let me clarify.",
        "difficulty": 4,
        "partOfSpeech": "v.",
        "library": "CET4"
    },
    {
        "word": "clash",
        "phonetic": "/klæʃ/",
        "translation": "v./n. 冲突",
        "example": "Clash with police.",
        "difficulty": 3,
        "partOfSpeech": "v./n.",
        "library": "CET4"
    },
    {
        "word": "class",
        "phonetic": "/klɑːs/",
        "translation": "n. 班级，阶级",
        "example": "Attend the class.",
        "difficulty": 1,
        "partOfSpeech": "n.",
        "library": "CET4"
    },
    {
        "word": "classic",
        "phonetic": "/ˈklæsɪk/",
        "translation": "adj. 经典的 n. 经典作品",
        "example": "A classic movie.",
        "difficulty": 3,
        "partOfSpeech": "adj./n.",
        "library": "CET4"
    },
    {
        "word": "classical",
        "phonetic": "/ˈklæsɪkl/",
        "translation": "adj. 古典的",
        "example": "Classical music.",
        "difficulty": 3,
        "partOfSpeech": "adj.",
        "library": "CET4"
    },
    {
        "word": "classification",
        "phonetic": "/ˌklæsɪfɪˈkeɪʃn/",
        "translation": "n. 分类",
        "example": "Job classification.",
        "difficulty": 4,
        "partOfSpeech": "n.",
        "library": "CET4"
    },
    {
        "word": "classify",
        "phonetic": "/ˈklæsɪfaɪ/",
        "translation": "v. 分类",
        "example": "Classify the books.",
        "difficulty": 4,
        "partOfSpeech": "v.",
        "library": "CET4"
    },
    {
        "word": "classmate",
        "phonetic": "/ˈklɑːsmeɪt/",
        "translation": "n. 同班同学",
        "example": "She is my classmate.",
        "difficulty": 2,
        "partOfSpeech": "n.",
        "library": "CET4"
    },
    {
        "word": "classroom",
        "phonetic": "/ˈklɑːsruːm/",
        "translation": "n. 教室",
        "example": "In the classroom.",
        "difficulty": 1,
        "partOfSpeech": "n.",
        "library": "CET4"
    },
    {
        "word": "clean",
        "phonetic": "/kliːn/",
        "translation": "adj. 干净的 v. 打扫",
        "example": "Keep it clean.",
        "difficulty": 1,
        "partOfSpeech": "adj./v.",
        "library": "CET4"
    },
    {
        "word": "clear",
        "phonetic": "/klɪər/",
        "translation": "adj. 清楚的 v. 清除",
        "example": "Make it clear.",
        "difficulty": 1,
        "partOfSpeech": "adj./v.",
        "library": "CET4"
    },
    {
        "word": "clergy",
        "phonetic": "/ˈklɜːdʒi/",
        "translation": "n. 神职人员",
        "example": "The clergy approved.",
        "difficulty": 4,
        "partOfSpeech": "n.",
        "library": "CET4"
    },
    {
        "word": "clever",
        "phonetic": "/ˈklevər/",
        "translation": "adj. 聪明的",
        "example": "A clever idea.",
        "difficulty": 2,
        "partOfSpeech": "adj.",
        "library": "CET4"
    },
    {
        "word": "click",
        "phonetic": "/klɪk/",
        "translation": "v./n. 点击",
        "example": "Click the button.",
        "difficulty": 1,
        "partOfSpeech": "v./n.",
        "library": "CET4"
    },
    {
        "word": "client",
        "phonetic": "/ˈklaɪənt/",
        "translation": "n. 客户",
        "example": "Serve the client.",
        "difficulty": 3,
        "partOfSpeech": "n.",
        "library": "CET4"
    },
    {
        "word": "cliff",
        "phonetic": "/klɪf/",
        "translation": "n. 悬崖",
        "example": "Jump off the cliff.",
        "difficulty": 3,
        "partOfSpeech": "n.",
        "library": "CET4"
    },
    {
        "word": "climate",
        "phonetic": "/ˈklaɪmət/",
        "translation": "n. 气候",
        "example": "Tropical climate.",
        "difficulty": 3,
        "partOfSpeech": "n.",
        "library": "CET4"
    },
    {
        "word": "climb",
        "phonetic": "/klaɪm/",
        "translation": "v. 攀爬",
        "example": "Climb the mountain.",
        "difficulty": 2,
        "partOfSpeech": "v.",
        "library": "CET4"
    },
    {
        "word": "clock",
        "phonetic": "/klɒk/",
        "translation": "n. 时钟",
        "example": "Check the clock.",
        "difficulty": 1,
        "partOfSpeech": "n.",
        "library": "CET4"
    },
    {
        "word": "close",
        "phonetic": "/kləʊz/",
        "translation": "v. 关闭 adj. 接近的",
        "example": "Close the door.",
        "difficulty": 1,
        "partOfSpeech": "v./adj.",
        "library": "CET4"
    },
    {
        "word": "closet",
        "phonetic": "/ˈklɒzɪt/",
        "translation": "n. 衣橱",
        "example": "In the closet.",
        "difficulty": 3,
        "partOfSpeech": "n.",
        "library": "CET4"
    },
    {
        "word": "cloth",
        "phonetic": "/klɒθ/",
        "translation": "n. 布",
        "example": "Cotton cloth.",
        "difficulty": 2,
        "partOfSpeech": "n.",
        "library": "CET4"
    },
    {
        "word": "clothes",
        "phonetic": "/kləʊðz/",
        "translation": "n. 衣服",
        "example": "Wear clean clothes.",
        "difficulty": 1,
        "partOfSpeech": "n.",
        "library": "CET4"
    },
    {
        "word": "clothing",
        "phonetic": "/ˈkləʊðɪŋ/",
        "translation": "n. 服装",
        "example": "Warm clothing.",
        "difficulty": 2,
        "partOfSpeech": "n.",
        "library": "CET4"
    },
    {
        "word": "cloud",
        "phonetic": "/klaʊd/",
        "translation": "n. 云",
        "example": "Dark clouds.",
        "difficulty": 1,
        "partOfSpeech": "n.",
        "library": "CET4"
    },
    {
        "word": "cloudy",
        "phonetic": "/ˈklaʊdi/",
        "translation": "adj. 多云的",
        "example": "Cloudy day.",
        "difficulty": 2,
        "partOfSpeech": "adj.",
        "library": "CET4"
    },
    {
        "word": "club",
        "phonetic": "/klʌb/",
        "translation": "n. 俱乐部，棍棒",
        "example": "Join the club.",
        "difficulty": 2,
        "partOfSpeech": "n.",
        "library": "CET4"
    },
    {
        "word": "clue",
        "phonetic": "/kluː/",
        "translation": "n. 线索",
        "example": "Find a clue.",
        "difficulty": 2,
        "partOfSpeech": "n.",
        "library": "CET4"
    },
    {
        "word": "clumsy",
        "phonetic": "/ˈklʌmzi/",
        "translation": "adj. 笨拙的",
        "example": "Clumsy movements.",
        "difficulty": 3,
        "partOfSpeech": "adj.",
        "library": "CET4"
    },
    {
        "word": "coach",
        "phonetic": "/kəʊtʃ/",
        "translation": "n. 教练，长途车",
        "example": "The coach yelled.",
        "difficulty": 2,
        "partOfSpeech": "n.",
        "library": "CET4"
    },
    {
        "word": "coal",
        "phonetic": "/kəʊl/",
        "translation": "n. 煤",
        "example": "Burn coal.",
        "difficulty": 2,
        "partOfSpeech": "n.",
        "library": "CET4"
    },
    {
        "word": "coarse",
        "phonetic": "/kɔːs/",
        "translation": "adj. 粗糙的",
        "example": "Coarse sand.",
        "difficulty": 3,
        "partOfSpeech": "adj.",
        "library": "CET4"
    },
    {
        "word": "coast",
        "phonetic": "/kəʊst/",
        "translation": "n. 海岸",
        "example": "East coast.",
        "difficulty": 2,
        "partOfSpeech": "n.",
        "library": "CET4"
    },
    {
        "word": "coat",
        "phonetic": "/kəʊt/",
        "translation": "n. 外套",
        "example": "Wear a coat.",
        "difficulty": 1,
        "partOfSpeech": "n.",
        "library": "CET4"
    },
    {
        "word": "cock",
        "phonetic": "/kɒk/",
        "translation": "n. 公鸡",
        "example": "The cock crowed.",
        "difficulty": 2,
        "partOfSpeech": "n.",
        "library": "CET4"
    },
    {
        "word": "code",
        "phonetic": "/kəʊd/",
        "translation": "n. 代码，密码",
        "example": "Write code.",
        "difficulty": 2,
        "partOfSpeech": "n.",
        "library": "CET4"
    },
    {
        "word": "coffee",
        "phonetic": "/ˈkɒfi/",
        "translation": "n. 咖啡",
        "example": "Drink coffee.",
        "difficulty": 1,
        "partOfSpeech": "n.",
        "library": "CET4"
    },
    {
        "word": "coil",
        "phonetic": "/kɔɪl/",
        "translation": "n./v. 卷",
        "example": "Coil the rope.",
        "difficulty": 3,
        "partOfSpeech": "n./v.",
        "library": "CET4"
    },
    {
        "word": "coin",
        "phonetic": "/kɔɪn/",
        "translation": "n. 硬币 v. 创造",
        "example": "Flip a coin.",
        "difficulty": 2,
        "partOfSpeech": "n./v.",
        "library": "CET4"
    },
    {
        "word": "cold",
        "phonetic": "/kəʊld/",
        "translation": "adj. 冷的 n. 感冒",
        "example": "Catch a cold.",
        "difficulty": 1,
        "partOfSpeech": "adj./n.",
        "library": "CET4"
    },
    {
        "word": "collapse",
        "phonetic": "/kəˈlæps/",
        "translation": "v./n. 倒塌",
        "example": "The bridge collapsed.",
        "difficulty": 4,
        "partOfSpeech": "v./n.",
        "library": "CET4"
    },
    {
        "word": "collar",
        "phonetic": "/ˈkɒlər/",
        "translation": "n. 衣领",
        "example": "White collar.",
        "difficulty": 2,
        "partOfSpeech": "n.",
        "library": "CET4"
    },
    {
        "word": "colleague",
        "phonetic": "/ˈkɒliːɡ/",
        "translation": "n. 同事",
        "example": "My colleague helped.",
        "difficulty": 4,
        "partOfSpeech": "n.",
        "library": "CET4"
    },
    {
        "word": "collect",
        "phonetic": "/kəˈlekt/",
        "translation": "v. 收集",
        "example": "Collect stamps.",
        "difficulty": 2,
        "partOfSpeech": "v.",
        "library": "CET4"
    },
    {
        "word": "collection",
        "phonetic": "/kəˈlekʃn/",
        "translation": "n. 收集，收藏",
        "example": "Art collection.",
        "difficulty": 3,
        "partOfSpeech": "n.",
        "library": "CET4"
    },
    {
        "word": "collective",
        "phonetic": "/kəˈlektɪv/",
        "translation": "adj. 集体的",
        "example": "Collective effort.",
        "difficulty": 4,
        "partOfSpeech": "adj.",
        "library": "CET4"
    },
    {
        "word": "college",
        "phonetic": "/ˈkɒlɪdʒ/",
        "translation": "n. 学院",
        "example": "Go to college.",
        "difficulty": 2,
        "partOfSpeech": "n.",
        "library": "CET4"
    },
    {
        "word": "collision",
        "phonetic": "/kəˈlɪʒn/",
        "translation": "n. 碰撞",
        "example": "Car collision.",
        "difficulty": 4,
        "partOfSpeech": "n.",
        "library": "CET4"
    },
    {
        "word": "colony",
        "phonetic": "/ˈkɒləni/",
        "translation": "n. 殖民地",
        "example": "Former colony.",
        "difficulty": 3,
        "partOfSpeech": "n.",
        "library": "CET4"
    },
    {
        "word": "color",
        "phonetic": "/ˈkʌlər/",
        "translation": "n. 颜色 v. 给...着色",
        "example": "What color?",
        "difficulty": 1,
        "partOfSpeech": "n./v.",
        "library": "CET4"
    },
    {
        "word": "column",
        "phonetic": "/ˈkɒləm/",
        "translation": "n. 柱，专栏",
        "example": "Marble column.",
        "difficulty": 3,
        "partOfSpeech": "n.",
        "library": "CET4"
    },
    {
        "word": "comb",
        "phonetic": "/kəʊm/",
        "translation": "n. 梳子 v. 梳",
        "example": "Comb your hair.",
        "difficulty": 2,
        "partOfSpeech": "n./v.",
        "library": "CET4"
    },
    {
        "word": "combat",
        "phonetic": "/ˈkɒmbæt/",
        "translation": "v./n. 战斗",
        "example": "Combat crime.",
        "difficulty": 4,
        "partOfSpeech": "v./n.",
        "library": "CET4"
    },
    {
        "word": "combine",
        "phonetic": "/kəmˈbaɪn/",
        "translation": "v. 结合",
        "example": "Combine flour and sugar.",
        "difficulty": 3,
        "partOfSpeech": "v.",
        "library": "CET4"
    },
    {
        "word": "combination",
        "phonetic": "/ˌkɒmbɪˈneɪʃn/",
        "translation": "n. 结合",
        "example": "A perfect combination.",
        "difficulty": 4,
        "partOfSpeech": "n.",
        "library": "CET4"
    },
    {
        "word": "come",
        "phonetic": "/kʌm/",
        "translation": "v. 来",
        "example": "Come here.",
        "difficulty": 1,
        "partOfSpeech": "v.",
        "library": "CET4"
    },
    {
        "word": "comedy",
        "phonetic": "/ˈkɒmədi/",
        "translation": "n. 喜剧",
        "example": "Watch a comedy.",
        "difficulty": 3,
        "partOfSpeech": "n.",
        "library": "CET4"
    },
    {
        "word": "comfort",
        "phonetic": "/ˈkʌmfət/",
        "translation": "n. 舒适 v. 安慰",
        "example": "Find comfort.",
        "difficulty": 3,
        "partOfSpeech": "n./v.",
        "library": "CET4"
    },
    {
        "word": "comfortable",
        "phonetic": "/ˈkʌmfətəbl/",
        "translation": "adj. 舒适的",
        "example": "Comfortable chair.",
        "difficulty": 2,
        "partOfSpeech": "adj.",
        "library": "CET4"
    },
    {
        "word": "command",
        "phonetic": "/kəˈmɑːnd/",
        "translation": "v./n. 命令，指挥",
        "example": "Command the army.",
        "difficulty": 3,
        "partOfSpeech": "v./n.",
        "library": "CET4"
    },
    {
        "word": "commander",
        "phonetic": "/kəˈmɑːndər/",
        "translation": "n. 指挥官",
        "example": "The commander ordered.",
        "difficulty": 3,
        "partOfSpeech": "n.",
        "library": "CET4"
    },
    {
        "word": "comment",
        "phonetic": "/ˈkɒment/",
        "translation": "n./v. 评论",
        "example": "Make a comment.",
        "difficulty": 3,
        "partOfSpeech": "n./v.",
        "library": "CET4"
    },
    {
        "word": "commercial",
        "phonetic": "/kəˈmɜːʃl/",
        "translation": "adj. 商业的 n. 商业广告",
        "example": "Commercial center.",
        "difficulty": 4,
        "partOfSpeech": "adj./n.",
        "library": "CET4"
    },
    {
        "word": "commission",
        "phonetic": "/kəˈmɪʃn/",
        "translation": "n. 委员会，佣金",
        "example": "Sales commission.",
        "difficulty": 4,
        "partOfSpeech": "n.",
        "library": "CET4"
    },
    {
        "word": "commit",
        "phonetic": "/kəˈmɪt/",
        "translation": "v. 犯罪，承诺",
        "example": "Commit a crime.",
        "difficulty": 4,
        "partOfSpeech": "v.",
        "library": "CET4"
    },
    {
        "word": "committee",
        "phonetic": "/kəˈmɪti/",
        "translation": "n. 委员会",
        "example": "The committee met.",
        "difficulty": 4,
        "partOfSpeech": "n.",
        "library": "CET4"
    },
    {
        "word": "common",
        "phonetic": "/ˈkɒmən/",
        "translation": "adj. 共同的，普通的",
        "example": "Common knowledge.",
        "difficulty": 2,
        "partOfSpeech": "adj.",
        "library": "CET4"
    },
    {
        "word": "communicate",
        "phonetic": "/kəˈmjuːnɪkeɪt/",
        "translation": "v. 交流，通讯",
        "example": "Communicate well.",
        "difficulty": 4,
        "partOfSpeech": "v.",
        "library": "CET4"
    },
    {
        "word": "communication",
        "phonetic": "/kəˌmjuːnɪˈkeɪʃn/",
        "translation": "n. 交流，通讯",
        "example": "Good communication.",
        "difficulty": 4,
        "partOfSpeech": "n.",
        "library": "CET4"
    },
    {
        "word": "communism",
        "phonetic": "/ˈkɒmjʊnɪzəm/",
        "translation": "n. 共产主义",
        "example": "Believe in communism.",
        "difficulty": 4,
        "partOfSpeech": "n.",
        "library": "CET4"
    },
    {
        "word": "community",
        "phonetic": "/kəˈmjuːnəti/",
        "translation": "n. 社区",
        "example": "Local community.",
        "difficulty": 4,
        "partOfSpeech": "n.",
        "library": "CET4"
    },
    {
        "word": "company",
        "phonetic": "/ˈkʌmpəni/",
        "translation": "n. 公司，陪伴",
        "example": "Work for a company.",
        "difficulty": 2,
        "partOfSpeech": "n.",
        "library": "CET4"
    },
    {
        "word": "compare",
        "phonetic": "/kəmˈpeər/",
        "translation": "v. 比较",
        "example": "Compare the prices.",
        "difficulty": 3,
        "partOfSpeech": "v.",
        "library": "CET4"
    },
    {
        "word": "comparison",
        "phonetic": "/kəmˈpærɪsn/",
        "translation": "n. 比较",
        "example": "In comparison.",
        "difficulty": 4,
        "partOfSpeech": "n.",
        "library": "CET4"
    },
    {
        "word": "compete",
        "phonetic": "/kəmˈpiːt/",
        "translation": "v. 竞争",
        "example": "Compete fairly.",
        "difficulty": 3,
        "partOfSpeech": "v.",
        "library": "CET4"
    },
    {
        "word": "competition",
        "phonetic": "/ˌkɒmpəˈtɪʃn/",
        "translation": "n. 竞争",
        "example": "Fierce competition.",
        "difficulty": 4,
        "partOfSpeech": "n.",
        "library": "CET4"
    },
    {
        "word": "complete",
        "phonetic": "/kəmˈpliːt/",
        "translation": "adj. 完整的 v. 完成",
        "example": "Complete the task.",
        "difficulty": 2,
        "partOfSpeech": "adj./v.",
        "library": "CET4"
    },
    {
        "word": "completely",
        "phonetic": "/kəmˈpliːtli/",
        "translation": "adv. 完全地",
        "example": "Completely wrong.",
        "difficulty": 2,
        "partOfSpeech": "adv.",
        "library": "CET4"
    },
    {
        "word": "complex",
        "phonetic": "/ˈkɒmpleks/",
        "translation": "adj. 复杂的",
        "example": "Complex problem.",
        "difficulty": 3,
        "partOfSpeech": "adj.",
        "library": "CET4"
    },
    {
        "word": "complicated",
        "phonetic": "/ˈkɒmplɪkeɪtɪd/",
        "translation": "adj. 复杂的",
        "example": "Complicated situation.",
        "difficulty": 3,
        "partOfSpeech": "adj.",
        "library": "CET4"
    },
    {
        "word": "component",
        "phonetic": "/kəmˈpəʊnənt/",
        "translation": "n. 成分",
        "example": "Key component.",
        "difficulty": 4,
        "partOfSpeech": "n.",
        "library": "CET4"
    },
    {
        "word": "compose",
        "phonetic": "/kəmˈpəʊz/",
        "translation": "v. 组成，作曲",
        "example": "Compose music.",
        "difficulty": 4,
        "partOfSpeech": "v.",
        "library": "CET4"
    },
    {
        "word": "composition",
        "phonetic": "/ˌkɒmpəˈzɪʃn/",
        "translation": "n. 作文，组成",
        "example": "Write a composition.",
        "difficulty": 4,
        "partOfSpeech": "n.",
        "library": "CET4"
    },
    {
        "word": "compound",
        "phonetic": "/ˈkɒmpaʊnd/",
        "translation": "adj. 复合的 n. 化合物",
        "example": "Compound word.",
        "difficulty": 4,
        "partOfSpeech": "adj./n.",
        "library": "CET4"
    },
    {
        "word": "comprehension",
        "phonetic": "/ˌkɒmprɪˈhenʃn/",
        "translation": "n. 理解",
        "example": "Reading comprehension.",
        "difficulty": 4,
        "partOfSpeech": "n.",
        "library": "CET4"
    },
    {
        "word": "comprehensive",
        "phonetic": "/ˌkɒmprɪˈhensɪv/",
        "translation": "adj. 综合的",
        "example": "Comprehensive exam.",
        "difficulty": 4,
        "partOfSpeech": "adj.",
        "library": "CET4"
    },
    {
        "word": "compress",
        "phonetic": "/kəmˈpres/",
        "translation": "v. 压缩",
        "example": "Compress the file.",
        "difficulty": 4,
        "partOfSpeech": "v.",
        "library": "CET4"
    },
    {
        "word": "comprise",
        "phonetic": "/kəmˈpraɪz/",
        "translation": "v. 包含",
        "example": "The team comprises 5 people.",
        "difficulty": 4,
        "partOfSpeech": "v.",
        "library": "CET4"
    },
    {
        "word": "compute",
        "phonetic": "/kəmˈpjuːt/",
        "translation": "v. 计算",
        "example": "Compute the total.",
        "difficulty": 3,
        "partOfSpeech": "v.",
        "library": "CET4"
    },
    {
        "word": "computer",
        "phonetic": "/kəmˈpjuːtər/",
        "translation": "n. 计算机",
        "example": "Use the computer.",
        "difficulty": 1,
        "partOfSpeech": "n.",
        "library": "CET4"
    },
    {
        "word": "comrade",
        "phonetic": "/ˈkɒmreɪd/",
        "translation": "n. 同志",
        "example": "My comrade.",
        "difficulty": 3,
        "partOfSpeech": "n.",
        "library": "CET4"
    },
    {
        "word": "conceal",
        "phonetic": "/kənˈsiːl/",
        "translation": "v. 隐藏",
        "example": "Conceal the truth.",
        "difficulty": 4,
        "partOfSpeech": "v.",
        "library": "CET4"
    },
    {
        "word": "concentrate",
        "phonetic": "/ˈkɒnsntreɪt/",
        "translation": "v. 集中",
        "example": "Concentrate on study.",
        "difficulty": 4,
        "partOfSpeech": "v.",
        "library": "CET4"
    },
    {
        "word": "concentration",
        "phonetic": "/ˌkɒnsnˈtreɪʃn/",
        "translation": "n. 集中，浓度",
        "example": "Lack of concentration.",
        "difficulty": 4,
        "partOfSpeech": "n.",
        "library": "CET4"
    },
    {
        "word": "concept",
        "phonetic": "/ˈkɒnsept/",
        "translation": "n. 概念",
        "example": "Basic concept.",
        "difficulty": 4,
        "partOfSpeech": "n.",
        "library": "CET4"
    },
    {
        "word": "concern",
        "phonetic": "/kənˈsɜːn/",
        "translation": "v./n. 关心，担心",
        "example": "Concern about you.",
        "difficulty": 3,
        "partOfSpeech": "v./n.",
        "library": "CET4"
    },
    {
        "word": "conclude",
        "phonetic": "/kənˈkluːd/",
        "translation": "v. 推断，结束",
        "example": "Conclude the meeting.",
        "difficulty": 4,
        "partOfSpeech": "v.",
        "library": "CET4"
    },
    {
        "word": "conclusion",
        "phonetic": "/kənˈkluːʒn/",
        "translation": "n. 结论",
        "example": "Draw a conclusion.",
        "difficulty": 4,
        "partOfSpeech": "n.",
        "library": "CET4"
    },
    {
        "word": "concrete",
        "phonetic": "/ˈkɒnkriːt/",
        "translation": "adj. 具体的 n. 混凝土",
        "example": "Concrete evidence.",
        "difficulty": 4,
        "partOfSpeech": "adj./n.",
        "library": "CET4"
    },
    {
        "word": "condemn",
        "phonetic": "/kənˈdem/",
        "translation": "v. 谴责",
        "example": "Condemn the violence.",
        "difficulty": 4,
        "partOfSpeech": "v.",
        "library": "CET4"
    },
    {
        "word": "condition",
        "phonetic": "/kənˈdɪʃn/",
        "translation": "n. 条件",
        "example": "Working conditions.",
        "difficulty": 3,
        "partOfSpeech": "n.",
        "library": "CET4"
    },
    {
        "word": "conduct",
        "phonetic": "/kənˈdʌkt/",
        "translation": "v. 引导 n. 行为",
        "example": "Conduct the orchestra.",
        "difficulty": 4,
        "partOfSpeech": "v./n.",
        "library": "CET4"
    },
    {
        "word": "conductor",
        "phonetic": "/kənˈdʌktər/",
        "translation": "n. 售票员，指挥",
        "example": "The conductor helped.",
        "difficulty": 3,
        "partOfSpeech": "n.",
        "library": "CET4"
    },
    {
        "word": "conference",
        "phonetic": "/ˈkɒnfərəns/",
        "translation": "n. 会议",
        "example": "Attend the conference.",
        "difficulty": 4,
        "partOfSpeech": "n.",
        "library": "CET4"
    },
    {
        "word": "confess",
        "phonetic": "/kənˈfes/",
        "translation": "v. 坦白",
        "example": "Confess your sins.",
        "difficulty": 4,
        "partOfSpeech": "v.",
        "library": "CET4"
    },
    {
        "word": "confidence",
        "phonetic": "/ˈkɒnfɪdəns/",
        "translation": "n. 信心",
        "example": "Have confidence.",
        "difficulty": 3,
        "partOfSpeech": "n.",
        "library": "CET4"
    },
    {
        "word": "confident",
        "phonetic": "/ˈkɒnfɪdənt/",
        "translation": "adj. 自信的",
        "example": "Be confident.",
        "difficulty": 3,
        "partOfSpeech": "adj.",
        "library": "CET4"
    },
    {
        "word": "confine",
        "phonetic": "/kənˈfaɪn/",
        "translation": "v. 限制",
        "example": "Confine yourself.",
        "difficulty": 4,
        "partOfSpeech": "v.",
        "library": "CET4"
    },
    {
        "word": "confirm",
        "phonetic": "/kənˈfɜːm/",
        "translation": "v. 确认",
        "example": "Confirm the date.",
        "difficulty": 3,
        "partOfSpeech": "v.",
        "library": "CET4"
    },
    {
        "word": "conflict",
        "phonetic": "/ˈkɒnflɪkt/",
        "translation": "n./v. 冲突",
        "example": "Avoid conflict.",
        "difficulty": 4,
        "partOfSpeech": "n./v.",
        "library": "CET4"
    },
    {
        "word": "confuse",
        "phonetic": "/kənˈfjuːz/",
        "translation": "v. 使困惑",
        "example": "Don't confuse me.",
        "difficulty": 3,
        "partOfSpeech": "v.",
        "library": "CET4"
    },
    {
        "word": "confusion",
        "phonetic": "/kənˈfjuːʒn/",
        "translation": "n. 困惑",
        "example": "In confusion.",
        "difficulty": 4,
        "partOfSpeech": "n.",
        "library": "CET4"
    },
    {
        "word": "congratulate",
        "phonetic": "/kənˈɡrætʃuleɪt/",
        "translation": "v. 祝贺",
        "example": "Congratulate the winner.",
        "difficulty": 4,
        "partOfSpeech": "v.",
        "library": "CET4"
    },
    {
        "word": "congratulation",
        "phonetic": "/kənˌɡrætʃuˈleɪʃn/",
        "translation": "n. 祝贺",
        "example": "Congratulations!",
        "difficulty": 4,
        "partOfSpeech": "n.",
        "library": "CET4"
    },
    {
        "word": "congress",
        "phonetic": "/ˈkɒŋɡres/",
        "translation": "n. 国会",
        "example": "The congress passed the law.",
        "difficulty": 4,
        "partOfSpeech": "n.",
        "library": "CET4"
    },
    {
        "word": "conjunction",
        "phonetic": "/kənˈdʒʌŋkʃn/",
        "translation": "n. 连词",
        "example": "Coordinate conjunction.",
        "difficulty": 4,
        "partOfSpeech": "n.",
        "library": "CET4"
    },
    {
        "word": "connect",
        "phonetic": "/kəˈnekt/",
        "translation": "v. 连接",
        "example": "Connect to WiFi.",
        "difficulty": 2,
        "partOfSpeech": "v.",
        "library": "CET4"
    },
    {
        "word": "connection",
        "phonetic": "/kəˈnekʃn/",
        "translation": "n. 连接",
        "example": "Internet connection.",
        "difficulty": 3,
        "partOfSpeech": "n.",
        "library": "CET4"
    },
    {
        "word": "conquer",
        "phonetic": "/ˈkɒŋkər/",
        "translation": "v. 征服",
        "example": "Conquer the world.",
        "difficulty": 4,
        "partOfSpeech": "v.",
        "library": "CET4"
    },
    {
        "word": "conquest",
        "phonetic": "/ˈkɒŋkwest/",
        "translation": "n. 征服",
        "example": "The conquest of space.",
        "difficulty": 4,
        "partOfSpeech": "n.",
        "library": "CET4"
    },
    {
        "word": "conscience",
        "phonetic": "/ˈkɒnʃəns/",
        "translation": "n. 良心",
        "example": "Clear conscience.",
        "difficulty": 4,
        "partOfSpeech": "n.",
        "library": "CET4"
    },
    {
        "word": "conscious",
        "phonetic": "/ˈkɒnʃəs/",
        "translation": "adj. 有意识的",
        "example": "Be conscious of risk.",
        "difficulty": 4,
        "partOfSpeech": "adj.",
        "library": "CET4"
    },
    {
        "word": "consent",
        "phonetic": "/kənˈsent/",
        "translation": "v./n. 同意",
        "example": "Parental consent.",
        "difficulty": 4,
        "partOfSpeech": "v./n.",
        "library": "CET4"
    },
    {
        "word": "consequence",
        "phonetic": "/ˈkɒnsɪkwəns/",
        "translation": "n. 结果",
        "example": "Face the consequences.",
        "difficulty": 4,
        "partOfSpeech": "n.",
        "library": "CET4"
    },
    {
        "word": "consequently",
        "phonetic": "/ˈkɒnsɪkwəntli/",
        "translation": "adv. 因此",
        "example": "Consequently, we failed.",
        "difficulty": 4,
        "partOfSpeech": "adv.",
        "library": "CET4"
    },
    {
        "word": "conservation",
        "phonetic": "/ˌkɒnsəˈveɪʃn/",
        "translation": "n. 保存",
        "example": "Water conservation.",
        "difficulty": 4,
        "partOfSpeech": "n.",
        "library": "CET4"
    },
    {
        "word": "conservative",
        "phonetic": "/kənˈsɜːvətɪv/",
        "translation": "adj. 保守的",
        "example": "Conservative estimate.",
        "difficulty": 4,
        "partOfSpeech": "adj.",
        "library": "CET4"
    },
    {
        "word": "consider",
        "phonetic": "/kənˈsɪdər/",
        "translation": "v. 考虑",
        "example": "Consider the offer.",
        "difficulty": 2,
        "partOfSpeech": "v.",
        "library": "CET4"
    },
    {
        "word": "considerable",
        "phonetic": "/kənˈsɪdərəbl/",
        "translation": "adj. 相当大的",
        "example": "Considerable effort.",
        "difficulty": 4,
        "partOfSpeech": "adj.",
        "library": "CET4"
    },
    {
        "word": "considerate",
        "phonetic": "/kənˈsɪdərət/",
        "translation": "adj. 体贴的",
        "example": "Be considerate.",
        "difficulty": 4,
        "partOfSpeech": "adj.",
        "library": "CET4"
    },
    {
        "word": "consideration",
        "phonetic": "/kənˌsɪdəˈreɪʃn/",
        "translation": "n. 考虑",
        "example": "Take into consideration.",
        "difficulty": 4,
        "partOfSpeech": "n.",
        "library": "CET4"
    },
    {
        "word": "consist",
        "phonetic": "/kənˈsɪst/",
        "translation": "v. 由...组成",
        "example": "Consist of three parts.",
        "difficulty": 4,
        "partOfSpeech": "v.",
        "library": "CET4"
    },
    {
        "word": "consistency",
        "phonetic": "/kənˈsɪstənsi/",
        "translation": "n. 一致性",
        "example": "Maintain consistency.",
        "difficulty": 4,
        "partOfSpeech": "n.",
        "library": "CET4"
    },
    {
        "word": "consistent",
        "phonetic": "/kənˈsɪstənt/",
        "translation": "adj. 一致的",
        "example": "Be consistent.",
        "difficulty": 4,
        "partOfSpeech": "adj.",
        "library": "CET4"
    },
    {
        "word": "constant",
        "phonetic": "/ˈkɒnstənt/",
        "translation": "adj. 持续的 n. 常数",
        "example": "Constant pain.",
        "difficulty": 4,
        "partOfSpeech": "adj./n.",
        "library": "CET4"
    },
    {
        "word": "constitution",
        "phonetic": "/ˌkɒnstɪˈtjuːʃn/",
        "translation": "n. 宪法，构成",
        "example": "The constitution.",
        "difficulty": 4,
        "partOfSpeech": "n.",
        "library": "CET4"
    },
    {
        "word": "construct",
        "phonetic": "/kənˈstrʌkt/",
        "translation": "v. 建造",
        "example": "Construct a bridge.",
        "difficulty": 4,
        "partOfSpeech": "v.",
        "library": "CET4"
    },
    {
        "word": "construction",
        "phonetic": "/kənˈstrʌkʃn/",
        "translation": "n. 建造",
        "example": "Under construction.",
        "difficulty": 4,
        "partOfSpeech": "n.",
        "library": "CET4"
    },
    {
        "word": "consult",
        "phonetic": "/kənˈsʌlt/",
        "translation": "v. 咨询",
        "example": "Consult a doctor.",
        "difficulty": 4,
        "partOfSpeech": "v.",
        "library": "CET4"
    },
    {
        "word": "consultant",
        "phonetic": "/kənˈsʌltənt/",
        "translation": "n. 顾问",
        "example": "Hire a consultant.",
        "difficulty": 4,
        "partOfSpeech": "n.",
        "library": "CET4"
    },
    {
        "word": "consume",
        "phonetic": "/kənˈsjuːm/",
        "translation": "v. 消费",
        "example": "Consume less.",
        "difficulty": 4,
        "partOfSpeech": "v.",
        "library": "CET4"
    },
    {
        "word": "consumer",
        "phonetic": "/kənˈsjuːmər/",
        "translation": "n. 消费者",
        "example": "Consumer rights.",
        "difficulty": 4,
        "partOfSpeech": "n.",
        "library": "CET4"
    },
    {
        "word": "contact",
        "phonetic": "/ˈkɒntækt/",
        "translation": "n./v. 接触，联系",
        "example": "Contact me.",
        "difficulty": 3,
        "partOfSpeech": "n./v.",
        "library": "CET4"
    },
    {
        "word": "contain",
        "phonetic": "/kənˈteɪn/",
        "translation": "v. 包含",
        "example": "Contains vitamins.",
        "difficulty": 3,
        "partOfSpeech": "v.",
        "library": "CET4"
    },
    {
        "word": "container",
        "phonetic": "/kənˈteɪnər/",
        "translation": "n. 容器",
        "example": "Shipping container.",
        "difficulty": 3,
        "partOfSpeech": "n.",
        "library": "CET4"
    },
    {
        "word": "contemporary",
        "phonetic": "/kənˈtemprəri/",
        "translation": "adj. 当代的",
        "example": "Contemporary art.",
        "difficulty": 4,
        "partOfSpeech": "adj.",
        "library": "CET4"
    },
    {
        "word": "contempt",
        "phonetic": "/kənˈtempt/",
        "translation": "n. 轻视",
        "example": "Hold in contempt.",
        "difficulty": 4,
        "partOfSpeech": "n.",
        "library": "CET4"
    },
    {
        "word": "contend",
        "phonetic": "/kənˈtend/",
        "translation": "v. 声称，竞争",
        "example": "Contend with rivals.",
        "difficulty": 4,
        "partOfSpeech": "v.",
        "library": "CET4"
    },
    {
        "word": "content",
        "phonetic": "/kənˈtent/",
        "translation": "adj. 满意的 n. 内容",
        "example": "Feel content.",
        "difficulty": 3,
        "partOfSpeech": "adj./n.",
        "library": "CET4"
    },
    {
        "word": "contest",
        "phonetic": "/ˈkɒntest/",
        "translation": "n./v. 竞赛",
        "example": "Enter the contest.",
        "difficulty": 3,
        "partOfSpeech": "n./v.",
        "library": "CET4"
    },
    {
        "word": "context",
        "phonetic": "/ˈkɒntekst/",
        "translation": "n. 上下文",
        "example": "In this context.",
        "difficulty": 4,
        "partOfSpeech": "n.",
        "library": "CET4"
    },
    {
        "word": "continent",
        "phonetic": "/ˈkɒntɪnənt/",
        "translation": "n. 大陆",
        "example": "The African continent.",
        "difficulty": 3,
        "partOfSpeech": "n.",
        "library": "CET4"
    },
    {
        "word": "continue",
        "phonetic": "/kənˈtɪnjuː/",
        "translation": "v. 继续",
        "example": "Continue working.",
        "difficulty": 2,
        "partOfSpeech": "v.",
        "library": "CET4"
    },
    {
        "word": "continuous",
        "phonetic": "/kənˈtɪnjuəs/",
        "translation": "adj. 连续的",
        "example": "Continuous improvement.",
        "difficulty": 4,
        "partOfSpeech": "adj.",
        "library": "CET4"
    },
    {
        "word": "contract",
        "phonetic": "/ˈkɒntrækt/",
        "translation": "n. 合同 v. 收缩",
        "example": "Sign the contract.",
        "difficulty": 4,
        "partOfSpeech": "n./v.",
        "library": "CET4"
    },
    {
        "word": "contradiction",
        "phonetic": "/ˌkɒntrəˈdɪkʃn/",
        "translation": "n. 矛盾",
        "example": "Avoid contradiction.",
        "difficulty": 4,
        "partOfSpeech": "n.",
        "library": "CET4"
    },
    {
        "word": "contrary",
        "phonetic": "/ˈkɒntrəri/",
        "translation": "adj. 相反的",
        "example": "On the contrary.",
        "difficulty": 4,
        "partOfSpeech": "adj.",
        "library": "CET4"
    },
    {
        "word": "contrast",
        "phonetic": "/ˈkɒntrɑːst/",
        "translation": "n./v. 对比",
        "example": "In contrast.",
        "difficulty": 4,
        "partOfSpeech": "n./v.",
        "library": "CET4"
    },
    {
        "word": "contribute",
        "phonetic": "/kənˈtrɪbjuːt/",
        "translation": "v. 贡献",
        "example": "Contribute to society.",
        "difficulty": 4,
        "partOfSpeech": "v.",
        "library": "CET4"
    },
    {
        "word": "contribution",
        "phonetic": "/ˌkɒntrɪˈbjuːʃn/",
        "translation": "n. 贡献",
        "example": "Make a contribution.",
        "difficulty": 4,
        "partOfSpeech": "n.",
        "library": "CET4"
    },
    {
        "word": "control",
        "phonetic": "/kənˈtrəʊl/",
        "translation": "v./n. 控制",
        "example": "Control yourself.",
        "difficulty": 2,
        "partOfSpeech": "v./n.",
        "library": "CET4"
    },
    {
        "word": "convenience",
        "phonetic": "/kənˈviːniəns/",
        "translation": "n. 方便",
        "example": "For your convenience.",
        "difficulty": 4,
        "partOfSpeech": "n.",
        "library": "CET4"
    },
    {
        "word": "convenient",
        "phonetic": "/kənˈviːniənt/",
        "translation": "adj. 方便的",
        "example": "Convenient location.",
        "difficulty": 3,
        "partOfSpeech": "adj.",
        "library": "CET4"
    },
    {
        "word": "convention",
        "phonetic": "/kənˈvenʃn/",
        "translation": "n. 大会，惯例",
        "example": "Party convention.",
        "difficulty": 4,
        "partOfSpeech": "n.",
        "library": "CET4"
    },
    {
        "word": "conventional",
        "phonetic": "/kənˈvenʃənl/",
        "translation": "adj. 传统的",
        "example": "Conventional method.",
        "difficulty": 4,
        "partOfSpeech": "adj.",
        "library": "CET4"
    },
    {
        "word": "conversation",
        "phonetic": "/ˌkɒnvəˈseɪʃn/",
        "translation": "n. 会话",
        "example": "Have a conversation.",
        "difficulty": 3,
        "partOfSpeech": "n.",
        "library": "CET4"
    },
    {
        "word": "conversely",
        "phonetic": "/ˈkɒnvɜːsli/",
        "translation": "adv. 相反地",
        "example": "Conversely, this works.",
        "difficulty": 4,
        "partOfSpeech": "adv.",
        "library": "CET4"
    },
    {
        "word": "conversion",
        "phonetic": "/kənˈvɜːʃn/",
        "translation": "n. 转换",
        "example": "Currency conversion.",
        "difficulty": 4,
        "partOfSpeech": "n.",
        "library": "CET4"
    },
    {
        "word": "convert",
        "phonetic": "/kənˈvɜːt/",
        "translation": "v. 转换",
        "example": "Convert to PDF.",
        "difficulty": 4,
        "partOfSpeech": "v.",
        "library": "CET4"
    },
    {
        "word": "convey",
        "phonetic": "/kənˈveɪ/",
        "translation": "v. 传达",
        "example": "Convey my message.",
        "difficulty": 4,
        "partOfSpeech": "v.",
        "library": "CET4"
    },
    {
        "word": "convince",
        "phonetic": "/kənˈvɪns/",
        "translation": "v. 说服",
        "example": "Convince the jury.",
        "difficulty": 4,
        "partOfSpeech": "v.",
        "library": "CET4"
    },
    {
        "word": "cook",
        "phonetic": "/kʊk/",
        "translation": "v. 烹饪 n. 厨师",
        "example": "Cook dinner.",
        "difficulty": 1,
        "partOfSpeech": "v./n.",
        "library": "CET4"
    },
    {
        "word": "cooker",
        "phonetic": "/ˈkʊkər/",
        "translation": "n. 炊具",
        "example": "Rice cooker.",
        "difficulty": 2,
        "partOfSpeech": "n.",
        "library": "CET4"
    },
    {
        "word": "cookie",
        "phonetic": "/ˈkʊki/",
        "translation": "n. 曲奇",
        "example": "Chocolate cookie.",
        "difficulty": 1,
        "partOfSpeech": "n.",
        "library": "CET4"
    },
    {
        "word": "cool",
        "phonetic": "/kuːl/",
        "translation": "adj. 凉爽的，酷",
        "example": "Cool down.",
        "difficulty": 1,
        "partOfSpeech": "adj.",
        "library": "CET4"
    },
    {
        "word": "cooperate",
        "phonetic": "/kəʊˈɒpəreɪt/",
        "translation": "v. 合作",
        "example": "Cooperate with us.",
        "difficulty": 4,
        "partOfSpeech": "v.",
        "library": "CET4"
    },
    {
        "word": "cooperation",
        "phonetic": "/kəʊˌɒpəˈreɪʃn/",
        "translation": "n. 合作",
        "example": "Thank you for your cooperation.",
        "difficulty": 4,
        "partOfSpeech": "n.",
        "library": "CET4"
    },
    {
        "word": "coordinate",
        "phonetic": "/kəʊˈɔːdɪnət/",
        "translation": "v. 协调",
        "example": "Coordinate the project.",
        "difficulty": 4,
        "partOfSpeech": "v.",
        "library": "CET4"
    },
    {
        "word": "cope",
        "phonetic": "/kəʊp/",
        "translation": "v. 应付",
        "example": "Cope with stress.",
        "difficulty": 3,
        "partOfSpeech": "v.",
        "library": "CET4"
    },
    {
        "word": "copy",
        "phonetic": "/ˈkɒpi/",
        "translation": "n./v. 复制",
        "example": "Make a copy.",
        "difficulty": 1,
        "partOfSpeech": "n./v.",
        "library": "CET4"
    },
    {
        "word": "core",
        "phonetic": "/kɔːr/",
        "translation": "n. 核心",
        "example": "Core values.",
        "difficulty": 2,
        "partOfSpeech": "n.",
        "library": "CET4"
    }
]

# 生成JSON文件
def generate_json(words, filename):
    """生成JSON词库文件"""
    output_dir = "D:/workspace/app/app/src/main/assets/data"
    os.makedirs(output_dir, exist_ok=True)

    filepath = os.path.join(output_dir, filename)

    with open(filepath, 'w', encoding='utf-8') as f:
        json.dump(words, f, ensure_ascii=False, indent=2)

    print(f"✅ 已生成: {filename} ({len(words)} 个单词)")
    return filepath

# 主函数
def main():
    print("🚀 开始生成词库...")
    print()

    # 生成CET4 Part3
    cet4_part3 = CET4_WORDS_PART3
    generate_json(cet4_part3, "cet4_words_part3.json")

    print()
    print("✨ 词库生成完成！")
    print()
    print("📝 后续步骤：")
    print("1. 在 WordRepository.kt 中添加新文件到加载列表")
    print("2. 重新编译APP")
    print("3. 查看Logcat确认加载的单词数量")
    print()
    print("💡 提示：使用ChatGPT生成更多词汇，然后添加到此脚本中")

if __name__ == "__main__":
    main()
