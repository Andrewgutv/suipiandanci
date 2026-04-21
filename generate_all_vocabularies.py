#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
多词库生成工具
生成CET4、CET6、IELTS、TOEFL、考研、GRE等词库
"""

import json
import os

# ==================== CET6 核心词汇 ====================
CET6_WORDS = [
    {"word": "abnormal", "phonetic": "/æbˈnɔːrml/", "translation": "adj. 反常的", "example": "abnormal behavior", "difficulty": 4, "partOfSpeech": "adj.", "library": "CET6"},
    {"word": "abolish", "phonetic": "/əˈbɒlɪʃ/", "translation": "v. 废除", "example": "abolish slavery", "difficulty": 4, "partOfSpeech": "v.", "library": "CET6"},
    {"word": "abrupt", "phonetic": "/əˈbrʌpt/", "translation": "adj. 突然的", "example": "abrupt change", "difficulty": 4, "partOfSpeech": "adj.", "library": "CET6"},
    {"word": "absurd", "phonetic": "/əbˈsɜːrd/", "translation": "adj. 荒谬的", "example": "absurd idea", "difficulty": 4, "partOfSpeech": "adj.", "library": "CET6"},
    {"word": "abundance", "phonetic": "/əˈbʌndəns/", "translation": "n. 丰富", "example": "abundance of resources", "difficulty": 4, "partOfSpeech": "n.", "library": "CET6"},
    {"word": "academy", "phonetic": "/əˈkædəmi/", "translation": "n. 学院", "example": "military academy", "difficulty": 3, "partOfSpeech": "n.", "library": "CET6"},
    {"word": "accessory", "phonetic": "/əkˈsesəri/", "translation": "n. 附件", "example": "car accessories", "difficulty": 4, "partOfSpeech": "n.", "library": "CET6"},
    {"word": "accommodate", "phonetic": "/əˈkɒmədeɪt/", "translation": "v. 容纳", "example": "accommodate 500 people", "difficulty": 4, "partOfSpeech": "v.", "library": "CET6"},
    {"word": "accomplish", "phonetic": "/əˈkʌmplɪʃ/", "translation": "v. 完成", "example": "accomplish goals", "difficulty": 3, "partOfSpeech": "v.", "library": "CET6"},
    {"word": "accountable", "phonetic": "/əˈkaʊntəbl/", "translation": "adj. 负责的", "example": "hold accountable", "difficulty": 4, "partOfSpeech": "adj.", "library": "CET6"},
    {"word": "accumulate", "phonetic": "/əˈkjuːmjəleɪt/", "translation": "v. 积累", "example": "accumulate wealth", "difficulty": 4, "partOfSpeech": "v.", "library": "CET6"},
    {"word": "accuracy", "phonetic": "/ˈækjərəsi/", "translation": "n. 准确性", "example": "ensure accuracy", "difficulty": 3, "partOfSpeech": "n.", "library": "CET6"},
    {"word": "acknowledge", "phonetic": "/əkˈnɒlɪdʒ/", "translation": "v. 承认", "example": "acknowledge receipt", "difficulty": 4, "partOfSpeech": "v.", "library": "CET6"},
    {"word": "acquaintance", "phonetic": "/əˈkweɪntəns/", "translation": "n. 熟人", "example": "casual acquaintance", "difficulty": 4, "partOfSpeech": "n.", "library": "CET6"},
    {"word": "acquisition", "phonetic": "/ˌækwɪˈzɪʃn/", "translation": "n. 获得", "example": "data acquisition", "difficulty": 5, "partOfSpeech": "n.", "library": "CET6"},
    {"word": "adapt", "phonetic": "/əˈdæpt/", "translation": "v. 适应", "example": "adapt to changes", "difficulty": 3, "partOfSpeech": "v.", "library": "CET6"},
    {"word": "adequate", "phonetic": "/ˈædɪkwət/", "translation": "adj. 足够的", "example": "adequate supply", "difficulty": 3, "partOfSpeech": "adj.", "library": "CET6"},
    {"word": "adjacent", "phonetic": "/əˈdʒeɪsnt/", "translation": "adj. 相邻的", "example": "adjacent rooms", "difficulty": 4, "partOfSpeech": "adj.", "library": "CET6"},
    {"word": "advocate", "phonetic": "/ˈædvəkeɪt/", "translation": "v. 提倡", "example": "advocate reform", "difficulty": 4, "partOfSpeech": "v.", "library": "CET6"},
    {"word": "aesthetic", "phonetic": "/iːsˈθetɪk/", "translation": "adj. 美学的", "example": "aesthetic value", "difficulty": 4, "partOfSpeech": "adj.", "library": "CET6"},
    {"word": "affiliate", "phonetic": "/əˈfɪlieɪt/", "translation": "v. 隶属", "example": "affiliate with", "difficulty": 5, "partOfSpeech": "v.", "library": "CET6"},
    {"word": "aggravate", "phonetic": "/ˈæɡrəveɪt/", "translation": "v. 加重", "example": "aggravate situation", "difficulty": 4, "partOfSpeech": "v.", "library": "CET6"},
    {"word": "alienate", "phonetic": "/ˈeɪliəneɪt/", "translation": "v. 疏远", "example": "alienate friends", "difficulty": 4, "partOfSpeech": "v.", "library": "CET6"},
    {"word": "allegedly", "phonetic": "/əˈledʒɪdli/", "translation": "adv. 据说", "example": "allegedly stolen", "difficulty": 4, "partOfSpeech": "adv.", "library": "CET6"},
    {"word": "alleviate", "phonetic": "/əˈliːvieɪt/", "translation": "v. 减轻", "example": "alleviate pain", "difficulty": 4, "partOfSpeech": "v.", "library": "CET6"},
    {"word": "ambiguous", "phonetic": "/æmˈbɪɡjuəs/", "translation": "adj. 模糊的", "example": "ambiguous message", "difficulty": 4, "partOfSpeech": "adj.", "library": "CET6"},
    {"word": "ambitious", "phonetic": "/æmˈbɪʃəs/", "translation": "adj. 有野心的", "example": "ambitious plan", "difficulty": 3, "partOfSpeech": "adj.", "library": "CET6"},
    {"word": "amplify", "phonetic": "/ˈæmplɪfaɪ/", "translation": "v. 放大", "example": "amplify sound", "difficulty": 4, "partOfSpeech": "v.", "library": "CET6"},
    {"word": "analogy", "phonetic": "/əˈnælədʒi/", "translation": "n. 类比", "example": "draw analogy", "difficulty": 4, "partOfSpeech": "n.", "library": "CET6"},
    {"word": "analytic", "phonetic": "/ˌænəˈlɪtɪk/", "translation": "adj. 分析的", "example": "analytic skills", "difficulty": 4, "partOfSpeech": "adj.", "library": "CET6"},
    {"word": "anniversary", "phonetic": "/ˌænɪˈvɜːrsəri/", "translation": "n. 周年", "example": "wedding anniversary", "difficulty": 3, "partOfSpeech": "n.", "library": "CET6"},
]

# ==================== IELTS 雅思词汇 ====================
IELTS_WORDS = [
    {"word": "abide", "phonetic": "/əˈbaɪd/", "translation": "v. 遵守", "example": "abide by rules", "difficulty": 4, "partOfSpeech": "v.", "library": "IELTS"},
    {"word": "absorb", "phonetic": "/əbˈzɔːrb/", "translation": "v. 吸收", "example": "absorb knowledge", "difficulty": 3, "partOfSpeech": "v.", "library": "IELTS"},
    {"word": "abstract", "phonetic": "/ˈæbstrækt/", "translation": "adj. 抽象的", "example": "abstract concept", "difficulty": 4, "partOfSpeech": "adj.", "library": "IELTS"},
    {"word": "abundance", "phonetic": "/əˈbʌndəns/", "translation": "n. 大量", "example": "abundance of food", "difficulty": 4, "partOfSpeech": "n.", "library": "IELTS"},
    {"word": "academic", "phonetic": "/ˌækəˈdemɪk/", "translation": "adj. 学术的", "example": "academic year", "difficulty": 3, "partOfSpeech": "adj.", "library": "IELTS"},
    {"word": "accelerate", "phonetic": "/əkˈseləreɪt/", "translation": "v. 加速", "example": "accelerate growth", "difficulty": 4, "partOfSpeech": "v.", "library": "IELTS"},
    {"word": "access", "phonetic": "/ˈækses/", "translation": "n. 通路", "example": "internet access", "difficulty": 2, "partOfSpeech": "n.", "library": "IELTS"},
    {"word": "accommodate", "phonetic": "/əˈkɒmədeɪt/", "translation": "v. 容纳", "example": "accommodate guests", "difficulty": 4, "partOfSpeech": "v.", "library": "IELTS"},
    {"word": "accomplish", "phonetic": "/əˈkʌmplɪʃ/", "translation": "v. 实现", "example": "accomplish mission", "difficulty": 3, "partOfSpeech": "v.", "library": "IELTS"},
    {"word": "accordance", "phonetic": "/əˈkɔːrdəns/", "translation": "n. 一致", "example": "in accordance with", "difficulty": 4, "partOfSpeech": "n.", "library": "IELTS"},
    {"word": "accountable", "phonetic": "/əˈkaʊntəbl/", "translation": "adj. 负责的", "example": "accountable for actions", "difficulty": 4, "partOfSpeech": "adj.", "library": "IELTS"},
    {"word": "accumulate", "phonetic": "/əˈkjuːmjəleɪt/", "translation": "v. 积累", "example": "accumulate experience", "difficulty": 4, "partOfSpeech": "v.", "library": "IELTS"},
    {"word": "accurate", "phonetic": "/ˈækjərət/", "translation": "adj. 准确的", "example": "accurate information", "difficulty": 3, "partOfSpeech": "adj.", "library": "IELTS"},
    {"word": "achieve", "phonetic": "/əˈtʃiːv/", "translation": "v. 实现", "example": "achieve success", "difficulty": 2, "partOfSpeech": "v.", "library": "IELTS"},
    {"word": "acknowledge", "phonetic": "/əkˈnɒlɪdʒ/", "translation": "v. 承认", "example": "acknowledge help", "difficulty": 4, "partOfSpeech": "v.", "library": "IELTS"},
    {"word": "acquire", "phonetic": "/əˈkwaɪər/", "translation": "v. 获得", "example": "acquire skills", "difficulty": 3, "partOfSpeech": "v.", "library": "IELTS"},
    {"word": "adapt", "phonetic": "/əˈdæpt/", "translation": "v. 适应", "example": "adapt to environment", "difficulty": 3, "partOfSpeech": "v.", "library": "IELTS"},
    {"word": "adequate", "phonetic": "/ˈædɪkwət/", "translation": "adj. 充足的", "example": "adequate resources", "difficulty": 3, "partOfSpeech": "adj.", "library": "IELTS"},
    {"word": "adjacent", "phonetic": "/əˈdʒeɪsnt/", "translation": "adj. 相邻的", "example": "adjacent building", "difficulty": 4, "partOfSpeech": "adj.", "library": "IELTS"},
    {"word": "adjust", "phonetic": "/əˈdʒʌst/", "translation": "v. 调整", "example": "adjust schedule", "difficulty": 2, "partOfSpeech": "v.", "library": "IELTS"},
    {"word": "administer", "phonetic": "/ədˈmɪnɪstər/", "translation": "v. 管理", "example": "administer test", "difficulty": 4, "partOfSpeech": "v.", "library": "IELTS"},
    {"word": "admire", "phonetic": "/ədˈmaɪər/", "translation": "v. 钦佩", "example": "admire courage", "difficulty": 2, "partOfSpeech": "v.", "library": "IELTS"},
    {"word": "admission", "phonetic": "/ədˈmɪʃn/", "translation": "n. 准入", "example": "admission ticket", "difficulty": 3, "partOfSpeech": "n.", "library": "IELTS"},
    {"word": "adopt", "phonetic": "/əˈdɒpt/", "translation": "v. 采取", "example": "adopt method", "difficulty": 2, "partOfSpeech": "v.", "library": "IELTS"},
    {"word": "adult", "phonetic": "/ˈædʌlt/", "translation": "n. 成年人", "example": "young adult", "difficulty": 2, "partOfSpeech": "n.", "library": "IELTS"},
    {"word": "advance", "phonetic": "/ədˈvæns/", "translation": "v. 前进", "example": "advance career", "difficulty": 2, "partOfSpeech": "v.", "library": "IELTS"},
    {"word": "advantage", "phonetic": "/ədˈvɑːntɪdʒ/", "translation": "n. 优势", "example": "competitive advantage", "difficulty": 2, "partOfSpeech": "n.", "library": "IELTS"},
    {"word": "adventure", "phonetic": "/ədˈventʃər/", "translation": "n. 冒险", "example": "go on adventure", "difficulty": 3, "partOfSpeech": "n.", "library": "IELTS"},
    {"word": "advocate", "phonetic": "/ˈædvəkeɪt/", "translation": "v. 提倡", "example": "advocate change", "difficulty": 4, "partOfSpeech": "v.", "library": "IELTS"},
    {"word": "affect", "phenetic": "/əˈfekt/", "translation": "v. 影响", "example": "affect decision", "difficulty": 2, "partOfSpeech": "v.", "library": "IELTS"},
    {"word": "afford", "phonetic": "/əˈfɔːrd/", "translation": "v. 买得起", "example": "afford house", "difficulty": 2, "partOfSpeech": "v.", "library": "IELTS"},
    {"word": "agency", "phonetic": "/ˈeɪdʒənsi/", "translation": "n. 代理", "example": "travel agency", "difficulty": 3, "partOfSpeech": "n.", "library": "IELTS"},
]

# ==================== TOEFL 托福词汇 ====================
TOEFL_WORDS = [
    {"word": "abandon", "phonetic": "/əˈbændən/", "translation": "v. 抛弃", "example": "abandon project", "difficulty": 3, "partOfSpeech": "v.", "library": "TOEFL"},
    {"word": "abbreviate", "phonetic": "/əˈbriːvieɪt/", "translation": "v. 缩写", "example": "abbreviate word", "difficulty": 5, "partOfSpeech": "v.", "library": "TOEFL"},
    {"word": "abide", "phonetic": "/əˈbaɪd/", "translation": "v. 遵守", "example": "abide by law", "difficulty": 4, "partOfSpeech": "v.", "library": "TOEFL"},
    {"word": "abolish", "phonetic": "/əˈbɒlɪʃ/", "translation": "v. 废除", "example": "abolish system", "difficulty": 5, "partOfSpeech": "v.", "library": "TOEFL"},
    {"word": "absorb", "phonetic": "/əbˈzɔːrb/", "translation": "v. 吸收", "example": "absorb nutrients", "difficulty": 4, "partOfSpeech": "v.", "library": "TOEFL"},
    {"word": "abstract", "phonetic": "/ˈæbstrækt/", "translation": "adj. 抽象的", "example": "abstract art", "difficulty": 4, "partOfSpeech": "adj.", "library": "TOEFL"},
    {"word": "abundance", "phonetic": "/əˈbʌndəns/", "translation": "n. 丰富", "example": "abundance wildlife", "difficulty": 4, "partOfSpeech": "n.", "library": "TOEFL"},
    {"word": "academic", "phonetic": "/ˌækəˈdemɪk/", "translation": "adj. 学术的", "example": "academic research", "difficulty": 3, "partOfSpeech": "adj.", "library": "TOEFL"},
    {"word": "accelerate", "phonetic": "/əkˈseləreɪt/", "translation": "v. 加速", "example": "accelerate process", "difficulty": 4, "partOfSpeech": "v.", "library": "TOEFL"},
    {"word": "access", "phonetic": "/ˈækses/", "translation": "n. 接近", "example": "have access to", "difficulty": 2, "partOfSpeech": "n.", "library": "TOEFL"},
    {"word": "accessible", "phonetic": "/əkˈsesəbl/", "translation": "adj. 可到达的", "example": "accessible location", "difficulty": 4, "partOfSpeech": "adj.", "library": "TOEFL"},
    {"word": "accommodate", "phonetic": "/əˈkɒmədeɪt/", "translation": "v. 容纳", "example": "accommodate changes", "difficulty": 4, "partOfSpeech": "v.", "library": "TOEFL"},
    {"word": "accompany", "phonetic": "/əˈkʌmpəni/", "translation": "v. 陪伴", "example": "accompany friend", "difficulty": 3, "partOfSpeech": "v.", "library": "TOEFL"},
    {"word": "accomplish", "phonetic": "/əˈkʌmplɪʃ/", "translation": "v. 完成", "example": "accomplish task", "difficulty": 3, "partOfSpeech": "v.", "library": "TOEFL"},
    {"word": "accordance", "phonetic": "/əˈkɔːrdəns/", "translation": "n. 一致", "example": "in accordance", "difficulty": 4, "partOfSpeech": "n.", "library": "TOEFL"},
    {"word": "account", "phonetic": "/əˈkaʊnt/", "translation": "n. 账户", "example": "bank account", "difficulty": 2, "partOfSpeech": "n.", "library": "TOEFL"},
    {"word": "accumulate", "phonetic": "/əˈkjuːmjəleɪt/", "translation": "v. 积累", "example": "accumulate wealth", "difficulty": 4, "partOfSpeech": "v.", "library": "TOEFL"},
    {"word": "accurate", "phonetic": "/ˈækjərət/", "translation": "adj. 精确的", "example": "accurate measurement", "difficulty": 3, "partOfSpeech": "adj.", "library": "TOEFL"},
    {"word": "achieve", "phonetic": "/əˈtʃiːv/", "translation": "v. 达到", "example": "achieve goal", "difficulty": 2, "partOfSpeech": "v.", "library": "TOEFL"},
    {"word": "acknowledge", "phonetic": "/əkˈnɒlɪdʒ/", "translation": "v. 承认", "example": "acknowledge fact", "difficulty": 4, "partOfSpeech": "v.", "library": "TOEFL"},
    {"word": "acquire", "phonetic": "/əˈkwaɪər/", "translation": "v. 获得", "example": "acquire knowledge", "difficulty": 4, "partOfSpeech": "v.", "library": "TOEFL"},
    {"word": "adaptation", "phonetic": "/ˌædæpˈteɪʃn/", "translation": "n. 适应", "example": "make adaptation", "difficulty": 4, "partOfSpeech": "n.", "library": "TOEFL"},
    {"word": "adequate", "phonetic": "/ˈædɪkwət/", "translation": "adj. 足够的", "example": "adequate supply", "difficulty": 3, "partOfSpeech": "adj.", "library": "TOEFL"},
    {"word": "adjacent", "phonetic": "/əˈdʒeɪsnt/", "translation": "adj. 相邻的", "example": "adjacent area", "difficulty": 4, "partOfSpeech": "adj.", "library": "TOEFL"},
    {"word": "adjust", "phonetic": "/əˈdʒʌst/", "translation": "v. 调整", "example": "adjust focus", "difficulty": 2, "partOfSpeech": "v.", "library": "TOEFL"},
    {"word": "administration", "phonetic": "/ədˌmɪnɪˈstreɪʃn/", "translation": "n. 管理", "example": "business administration", "difficulty": 4, "partOfSpeech": "n.", "library": "TOEFL"},
    {"word": "admire", "phonetic": "/ədˈmaɪər/", "translation": "v. 欣赏", "example": "admire painting", "difficulty": 2, "partOfSpeech": "v.", "library": "TOEFL"},
    {"word": "admission", "phonetic": "/ədˈmɪʃn/", "translation": "n. 进入", "example": "gain admission", "difficulty": 3, "partOfSpeech": "n.", "library": "TOEFL"},
    {"word": "adopt", "phonetic": "/əˈdɒpt/", "translation": "v. 采用", "example": "adopt policy", "difficulty": 3, "partOfSpeech": "v.", "library": "TOEFL"},
    {"word": "adult", "phonetic": "/ˈædʌlt/", "translation": "n. 成人", "example": "adult education", "difficulty": 2, "partOfSpeech": "n.", "library": "TOEFL"},
    {"word": "advance", "phonetic": "/ədˈvæns/", "translation": "v. 前进", "example": "advance technology", "difficulty": 2, "partOfSpeech": "v.", "library": "TOEFL"},
    {"word": "advantage", "phonetic": "/ədˈvæntɪdʒ/", "translation": "n. 优点", "example": "take advantage", "difficulty": 2, "partOfSpeech": "n.", "library": "TOEFL"},
    {"word": "adventure", "phonetic": "/ədˈventʃər/", "translation": "n. 奇遇", "example": "sense of adventure", "difficulty": 3, "partOfSpeech": "n.", "library": "TOEFL"},
    {"word": "adverse", "phonetic": "/ˈædvɜːrs/", "translation": "adj. 不利的", "example": "adverse effect", "difficulty": 5, "partOfSpeech": "adj.", "library": "TOEFL"},
]

# ==================== GRE 词汇 ====================
GRE_WORDS = [
    {"word": "abacus", "phonetic": "/ˈæbəkəs/", "translation": "n. 算盘", "example": "use abacus", "difficulty": 5, "partOfSpeech": "n.", "library": "GRE"},
    {"word": "abate", "phonetic": "/əˈbeɪt/", "translation": "v. 减轻", "example": "abate pain", "difficulty": 5, "partOfSpeech": "v.", "library": "GRE"},
    {"word": "abbreviate", "phenetic": "/əˈbriːvieɪt/", "translation": "v. 缩短", "example": "abbreviate speech", "difficulty": 5, "partOfSpeech": "v.", "library": "GRE"},
    {"word": "abdicate", "phonetic": "/ˈæbdɪkeɪt/", "translation": "v. 退位", "example": "abdicate throne", "difficulty": 5, "partOfSpeech": "v.", "library": "GRE"},
    {"word": "aberration", "phonetic": "/ˌæbəˈreɪʃn/", "translation": "n. 异常", "example": "genetic aberration", "difficulty": 5, "partOfSpeech": "n.", "library": "GRE"},
    {"word": "abhor", "phonetic": "/əbˈhɔːr/", "translation": "v. 憎恨", "example": "abhor violence", "difficulty": 5, "partOfSpeech": "v.", "library": "GRE"},
    {"word": "abide", "phonetic": "/əˈbaɪd/", "translation": "v. 遵守", "example": "abide law", "difficulty": 4, "partOfSpeech": "v.", "library": "GRE"},
    {"word": "abject", "phonetic": "/ˈæbdʒekt/", "translation": "adj. 卑鄙的", "example": "abject poverty", "difficulty": 5, "partOfSpeech": "adj.", "library": "GRE"},
    {"word": "ablution", "phonetic": "/əˈbluːʃn/", "translation": "n. 沐浴", "example": "perform ablution", "difficulty": 5, "partOfSpeech": "n.", "library": "GRE"},
    {"word": "abnegate", "phonetic": "/ˈæbnɪɡeɪt/", "translation": "v. 放弃", "example": "abnegate right", "difficulty": 5, "partOfSpeech": "v.", "library": "GRE"},
    {"word": "abolish", "phonetic": "/əˈbɒlɪʃ/", "translation": "v. 废除", "example": "abolish law", "difficulty": 4, "partOfSpeech": "v.", "library": "GRE"},
    {"word": "abominate", "phonetic": "/əˈbɒmɪneɪt/", "translation": "v. 厌恶", "example": "abominate crime", "difficulty": 5, "partOfSpeech": "v.", "library": "GRE"},
    {"word": "aboriginal", "phonetic": "/ˌæbəˈrɪdʒənl/", "translation": "adj. 土著的", "example": "aboriginal people", "difficulty": 5, "partOfSpeech": "adj.", "library": "GRE"},
    {"word": "abortive", "phonetic": "/əˈbɔːrtɪv/", "translation": "adj. 失败的", "example": "abortive attempt", "difficulty": 5, "partOfSpeech": "adj.", "library": "GRE"},
    {"word": "abrade", "phonetic": "/əˈbreɪd/", "translation": "v. 磨损", "example": "abrade skin", "difficulty": 5, "partOfSpeech": "v.", "library": "GRE"},
    {"word": "abridge", "phonetic": "/əˈbrɪdʒ/", "translation": "v. 删节", "example": "abridge book", "difficulty": 5, "partOfSpeech": "v.", "library": "GRE"},
    {"word": "abrogate", "phonetic": "/ˈæbrəɡeɪt/", "translation": "v. 废除", "example": "abrogate treaty", "difficulty": 5, "partOfSpeech": "v.", "library": "GRE"},
    {"word": "abscond", "phonetic": "/əbˈskɒnd/", "translation": "v. 潜逃", "example": "abscond with money", "difficulty": 5, "partOfSpeech": "v.", "library": "GRE"},
    {"word": "absence", "phonetic": "/ˈæbsəns/", "translation": "n. 缺席", "example": "absence from work", "difficulty": 3, "partOfSpeech": "n.", "library": "GRE"},
    {"word": "absorb", "phonetic": "/əbˈzɔːrb/", "translation": "v. 吸收", "example": "absorb liquid", "difficulty": 3, "partOfSpeech": "v.", "library": "GRE"},
    {"word": "abstain", "phonetic": "/əbˈsteɪn/", "translation": "v. 弃权", "example": "abstain from voting", "difficulty": 5, "partOfSpeech": "v.", "library": "GRE"},
    {"word": "abstemious", "phonetic": "/æbˈstiːmiəs/", "translation": "adj. 节制的", "example": "abstemious life", "difficulty": 5, "partOfSpeech": "adj.", "library": "GRE"},
    {"word": "abstinence", "phonetic": "/ˈæbstɪnəns/", "translation": "n. 禁欲", "example": "practice abstinence", "difficulty": 5, "partOfSpeech": "n.", "library": "GRE"},
    {"word": "abstract", "phonetic": "/ˈæbstrækt/", "translation": "adj. 抽象的", "example": "abstract idea", "difficulty": 4, "partOfSpeech": "adj.", "library": "GRE"},
    {"word": "abstruse", "phonetic": "/əbˈstruːs/", "translation": "adj. 深奥的", "example": "abstruse theory", "difficulty": 5, "partOfSpeech": "adj.", "library": "GRE"},
    {"word": "absurd", "phonetic": "/əbˈsɜːrd/", "translation": "adj. 荒谬的", "example": "absurd claim", "difficulty": 4, "partOfSpeech": "adj.", "library": "GRE"},
    {"word": "abundance", "phonetic": "/əˈbʌndəns/", "translation": "n. 大量", "example": "abundance of food", "difficulty": 4, "partOfSpeech": "n.", "library": "GRE"},
    {"word": "abuse", "phonetic": "/əˈbjuːs/", "translation": "v./n. 滥用", "example": "drug abuse", "difficulty": 3, "partOfSpeech": "v./n.", "library": "GRE"},
    {"word": "abut", "phonetic": "/əˈbʌt/", "translation": "v. 毗邻", "example": "abut property", "difficulty": 5, "partOfSpeech": "v.", "library": "GRE"},
    {"word": "abysmal", "phonetic": "/əˈbɪzməl/", "translation": "adj. 深渊的", "example": "abysmal failure", "difficulty": 5, "partOfSpeech": "adj.", "library": "GRE"},
    {"word": "academic", "phonetic": "/ˌækəˈdemɪk/", "translation": "adj. 学术的", "example": "academic achievement", "difficulty": 3, "partOfSpeech": "adj.", "library": "GRE"},
    {"word": "accede", "phonetic": "/əkˈsiːd/", "translation": "v. 同意", "example": "accede request", "difficulty": 5, "partOfSpeech": "v.", "library": "GRE"},
    {"word": "accelerate", "phonetic": "/əkˈseləreɪt/", "translation": "v. 加速", "example": "accelerate car", "difficulty": 4, "partOfSpeech": "v.", "library": "GRE"},
]

# ==================== 考研词汇 ====================
GRADUATE_WORDS = [
    {"word": "abandon", "phonetic": "/əˈbændən/", "translation": "v. 抛弃", "example": "abandon hope", "difficulty": 3, "partOfSpeech": "v.", "library": "GRADUATE"},
    {"word": "abide", "phonetic": "/əˈbaɪd/", "translation": "v. 遵守", "example": "abide by rules", "difficulty": 4, "partOfSpeech": "v.", "library": "GRADUATE"},
    {"word": "abolish", "phonetic": "/əˈbɒlɪʃ/", "translation": "v. 废除", "example": "abolish bad habit", "difficulty": 4, "partOfSpeech": "v.", "library": "GRADUATE"},
    {"word": "absorb", "phonetic": "/əbˈzɔːrb/", "translation": "v. 吸收", "example": "absorb information", "difficulty": 3, "partOfSpeech": "v.", "library": "GRADUATE"},
    {"word": "abstract", "phonetic": "/ˈæbstrækt/", "translation": "adj. 抽象的", "example": "abstract noun", "difficulty": 4, "partOfSpeech": "adj.", "library": "GRADUATE"},
    {"word": "abundant", "phonetic": "/əˈbʌndənt/", "translation": "adj. 丰富的", "example": "abundant resources", "difficulty": 4, "partOfSpeech": "adj.", "library": "GRADUATE"},
    {"word": "academic", "phonetic": "/ˌækəˈdemɪk/", "translation": "adj. 学院的", "example": "academic degree", "difficulty": 3, "partOfSpeech": "adj.", "library": "GRADUATE"},
    {"word": "accelerate", "phonetic": "/əkˈseləreɪt/", "translation": "v. 加速", "example": "accelerate speed", "difficulty": 4, "partOfSpeech": "v.", "library": "GRADUATE"},
    {"word": "access", "phonetic": "/ˈækses/", "translation": "n. 接近", "example": "easy access", "difficulty": 2, "partOfSpeech": "n.", "library": "GRADUATE"},
    {"word": "accommodate", "phonetic": "/əˈkɒmədeɪt/", "translation": "v. 容纳", "example": "accommodate demand", "difficulty": 4, "partOfSpeech": "v.", "library": "GRADUATE"},
    {"word": "accomplish", "phonetic": "/əˈkʌmplɪʃ/", "translation": "v. 完成", "example": "accomplish purpose", "difficulty": 3, "partOfSpeech": "v.", "library": "GRADUATE"},
    {"word": "accordance", "phonetic": "/əˈkɔːrdəns/", "translation": "n. 一致", "example": "in accordance", "difficulty": 4, "partOfSpeech": "n.", "library": "GRADUATE"},
    {"word": "account", "phonetic": "/əˈkaʊnt/", "translation": "n. 账目", "example": "open account", "difficulty": 2, "partOfSpeech": "n.", "library": "GRADUATE"},
    {"word": "accumulate", "phonetic": "/əˈkjuːmjəleɪt/", "translation": "v. 积累", "example": "accumulate capital", "difficulty": 4, "partOfSpeech": "v.", "library": "GRADUATE"},
    {"word": "accurate", "phonetic": "/ˈækjərət/", "translation": "adj. 准确的", "example": "accurate calculation", "difficulty": 3, "partOfSpeech": "adj.", "library": "GRADUATE"},
    {"word": "accuse", "phonetic": "/əˈkjuːz/", "translation": "v. 指控", "example": "accuse of crime", "difficulty": 3, "partOfSpeech": "v.", "library": "GRADUATE"},
    {"word": "accustomed", "phonetic": "/əˈkʌstəmd/", "translation": "adj. 习惯的", "example": "get accustomed", "difficulty": 3, "partOfSpeech": "adj.", "library": "GRADUATE"},
    {"word": "achievement", "phonetic": "/əˈtʃiːvmənt/", "translation": "n. 成就", "example": "great achievement", "difficulty": 3, "partOfSpeech": "n.", "library": "GRADUATE"},
    {"word": "acknowledge", "phonetic": "/əkˈnɒlɪdʒ/", "translation": "v. 承认", "example": "acknowledge defeat", "difficulty": 4, "partOfSpeech": "v.", "library": "GRADUATE"},
    {"word": "acquaint", "phonetic": "/əˈkweɪnt/", "translation": "v. 使熟悉", "example": "acquaint with", "difficulty": 4, "partOfSpeech": "v.", "library": "GRADUATE"},
    {"word": "acquire", "phonetic": "/əˈkwaɪər/", "translation": "v. 获得", "example": "acquire knowledge", "difficulty": 3, "partOfSpeech": "v.", "library": "GRADUATE"},
    {"word": "adapt", "phonetic": "/əˈdæpt/", "translation": "v. 适应", "example": "adapt to climate", "difficulty": 3, "partOfSpeech": "v.", "library": "GRADUATE"},
    {"word": "adequate", "phonetic": "/ˈædɪkwət/", "translation": "adj. 足够的", "example": "adequate preparation", "difficulty": 3, "partOfSpeech": "adj.", "library": "GRADUATE"},
    {"word": "adjacent", "phonetic": "/əˈdʒeɪsnt/", "translation": "adj. 邻近的", "example": "adjacent room", "difficulty": 4, "partOfSpeech": "adj.", "library": "GRADUATE"},
    {"word": "adjust", "phonetic": "/əˈdʒʌst/", "translation": "v. 调节", "example": "adjust temperature", "difficulty": 2, "partOfSpeech": "v.", "library": "GRADUATE"},
    {"word": "administration", "phonetic": "/ədˌmɪnɪˈstreɪʃn/", "translation": "n. 行政", "example": "business administration", "difficulty": 4, "partOfSpeech": "n.", "library": "GRADUATE"},
    {"word": "admire", "phonetic": "/ədˈmaɪər/", "translation": "v. 钦佩", "example": "admire hero", "difficulty": 2, "partOfSpeech": "v.", "library": "GRADUATE"},
    {"word": "admission", "phonetic": "/ədˈmɪʃn/", "translation": "n. 允许进入", "example": "admission ticket", "difficulty": 3, "partOfSpeech": "n.", "library": "GRADUATE"},
    {"word": "adopt", "phonetic": "/əˈdɒpt/", "translation": "v. 收养", "example": "adopt child", "difficulty": 2, "partOfSpeech": "v.", "library": "GRADUATE"},
    {"word": "advance", "phonetic": "/ədˈvæns/", "translation": "v. 前进", "example": "advance forward", "difficulty": 2, "partOfSpeech": "v.", "library": "GRADUATE"},
    {"word": "advantage", "phonetic": "/ədˈvɑːntɪdʒ/", "translation": "n. 优势", "example": "take advantage", "difficulty": 2, "partOfSpeech": "n.", "library": "GRADUATE"},
    {"word": "adventure", "phonetic": "/ədˈventʃər/", "translation": "n. 冒险", "example": "seek adventure", "difficulty": 3, "partOfSpeech": "n.", "library": "GRADUATE"},
    {"word": "advocate", "phonetic": "/ˈædvəkət/", "translation": "n. 拥护者", "example": "advocate peace", "difficulty": 4, "partOfSpeech": "n.", "library": "GRADUATE"},
]

def generate_json(words, filename):
    """生成JSON词库文件"""
    output_dir = "D:/workspace/app/app/src/main/assets/data"
    os.makedirs(output_dir, exist_ok=True)

    filepath = os.path.join(output_dir, filename)

    with open(filepath, 'w', encoding='utf-8') as f:
        json.dump(words, f, ensure_ascii=False, indent=2)

    print(f"[OK] 已生成: {filename} ({len(words)} 个单词)")

def main():
    import sys
    import io

    # 设置stdout为UTF-8编码
    if sys.platform == 'win32':
        sys.stdout = io.TextIOWrapper(sys.stdout.buffer, encoding='utf-8', errors='replace')

    print("开始生成多词库...")
    print()

    # 生成各个词库
    generate_json(CET6_WORDS, "cet6_words.json")
    generate_json(IELTS_WORDS, "ielts_words.json")
    generate_json(TOEFL_WORDS, "toefl_words.json")
    generate_json(GRE_WORDS, "gre_words.json")
    generate_json(GRADUATE_WORDS, "graduate_words.json")

    print()
    print("所有词库生成完成!")
    print()
    print("生成统计:")
    print(f"  - CET6: {len(CET6_WORDS)} 词")
    print(f"  - IELTS: {len(IELTS_WORDS)} 词")
    print(f"  - TOEFL: {len(TOEFL_WORDS)} 词")
    print(f"  - GRE: {len(GRE_WORDS)} 词")
    print(f"  - 考研: {len(GRADUATE_WORDS)} 词")
    print(f"  - 总计: {len(CET6_WORDS) + len(IELTS_WORDS) + len(TOEFL_WORDS) + len(GRE_WORDS) + len(GRADUATE_WORDS)} 词")

if __name__ == "__main__":
    main()
