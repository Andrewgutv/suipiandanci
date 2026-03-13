"""
生成CET4核心词汇数据并导入MySQL
"""
import pymysql
import random
from datetime import datetime

# CET4核心词汇（100个示例）
cet4_words = [
    # 第1批：基础动词
    ("abandon", "/ə'bændən/", "v. 遗弃；放弃", "He decided to abandon the project.", 4),
    ("ability", "/ə'biliti/", "n. 能力；本领", "She has the ability to solve complex problems.", 3),
    ("abnormal", "/æb'nɔːml/", "adj. 反常的", "The weather is abnormal this year.", 4),
    ("aboard", "/ə'bɔːd/", "adv. 在船(车)上", "Welcome aboard the flight!", 3),
    ("abolish", "/ə'bɔliʃ/", "v. 废除，取消", "Slavery was abolished in the 19th century.", 5),

    # 第2批：学习相关
    ("academic", "/ˌækə'demik/", "adj. 学术的", "She pursues her academic interests.", 4),
    ("accelerate", "/ək'seləreit/", "v. 加速", "The car began to accelerate.", 4),
    ("accept", "/ək'sept/", "v. 接受", "I accept your apology.", 2),
    ("access", "/'ækses/", "n. 接近；通道", "Students have access to the library.", 3),
    ("accident", "/'æksidənt/", "n. 意外的事", "He was injured in an accident.", 3),

    # 第3批：重要形容词
    ("accurate", "/'ækjurət/", "adj. 准确的", "Please give me an accurate report.", 4),
    ("accustomed", "/ə'kʌstəmd/", "adj. 惯常的", "I am accustomed to cold weather.", 4),
    ("achieve", "/ə'tʃiːv/", "v. 实现，完成", "Work hard to achieve your goals.", 3),
    ("acquire", "/ə'kwaiə/", "v. 取得，获得", "She acquired a new language skill.", 4),
    ("adapt", "/ə'dæpt/", "v. 使适应", "Animals adapt to their environment.", 4),

    # 第4批：常用动词
    ("addition", "/ə'diʃən/", "n. 加，增加", "In addition, we need more time.", 3),
    ("adequate", "/'ædikwət/", "adj. 足够的", "Is the funding adequate?", 4),
    ("adjust", "/ə'dʒʌst/", "v. 调整，调节", "Please adjust the mirror.", 3),
    ("administration", "/ədˌminis'treiʃən/", "n. 管理", "The administration is efficient.", 4),
    ("admit", "/əd'mit/", "v. 承认", "He admitted his mistake.", 3),

    # 第5批：高级词汇
    ("adopt", "/ə'dɔpt/", "v. 收养；采用", "They decided to adopt a child.", 4),
    ("adult", "/'ædʌlt/", "n. 成年人", "Adults should be responsible.", 3),
    ("advance", "/əd'vaːns/", "v. 前进，提高", "Technology continues to advance.", 3),
    ("advantage", "/əd'vaːntidʒ/", "n. 优点，优势", "What is the advantage?", 3),
    ("adventure", "/əd'ventʃə/", "n. 冒险", "Life is an adventure.", 3),

    # 第6批：B开头
    ("background", "/'bækgraund/", "n. 背景", "Tell me about your background.", 3),
    ("bacterium", "/bæk'tiəriəm/", "n. 细菌", "Bacteria can cause disease.", 5),
    ("balance", "/'bæləns/", "n. 平衡", "Work-life balance is important.", 3),
    ("barrier", "/'bæriə/", "n. 障碍", "Language can be a barrier.", 4),
    ("benefit", "/'benifit/", "n. 利益 v. 有益于", "Exercise benefits health.", 3),

    # 第7批：C开头
    ("candidate", "/'kændidət/", "n. 候选人", "He is a candidate for the job.", 4),
    ("capacity", "/kə'pæsəti/", "n. 容量；能力", "The stadium has a large capacity.", 4),
    ("career", "/kə'riə/", "n. 生涯，职业", "She built a successful career.", 3),
    ("casual", "/'kæʒuəl/", "adj. 偶然的；随便的", "The dress code is casual.", 3),
    ("category", "/'kætəɡəri/", "n. 种类，类别", "Which category does it belong to?", 4),

    # 第8批：D开头
    ("debate", "/di'beit/", "n./v. 辩论", "Let's debate this topic.", 4),
    ("decade", "/'dekeid/", "n. 十年", "A decade has passed.", 3),
    ("decline", "/di'klain/", "v. 下降；衰退", "Sales declined this year.", 4),
    ("define", "/di'fain/", "v. 定义", "Please define this term.", 3),
    ("delicate", "/'delikət/", "adj. 精致的；微妙的", "This is a delicate situation.", 4),

    # 第9批：E开头
    ("economy", "/i'kɔnəmi/", "n. 经济", "The economy is growing.", 3),
    ("efficient", "/i'fiʃənt/", "adj. 效率高的", "This method is efficient.", 4),
    ("element", "/'elimənt/", "n. 成分；要素", "Trust is an essential element.", 4),
    ("emotion", "/i'məuʃən/", "n. 情感，情绪", "Don't let emotions control you.", 3),
    ("emphasize", "/'emfəsaiz/", "v. 强调", "I emphasize the importance.", 4),

    # 第10批：F开头
    ("factor", "/'fæktə/", "n. 因素", "Many factors influence success.", 3),
    ("feature", "/'fiːtʃə/", "n. 特征，特色", "This feature is unique.", 3),
    ("federal", "/'fedərəl/", "adj. 联邦的", "Federal laws apply nationwide.", 4),
    ("fertile", "/'fəːtail/", "adj. 肥沃的", "The soil is fertile.", 4),
    ("flexible", "/'fleksəbl/", "adj. 易弯曲的；灵活的", "Be flexible in your approach.", 4),

    # 第11批：G开头
    ("gap", "/ɡæp/", "n. 缺口；差距", "Bridge the gap between rich and poor.", 3),
    ("general", "/'dʒenərəl/", "adj. 总的；一般的", "In general, I agree.", 2),
    ("generate", "/'dʒenəreit/", "v. 发生，产生", "Solar panels generate electricity.", 4),
    ("genius", "/'dʒiːniəs/", "n. 天才", "Einstein was a genius.", 4),
    ("genuine", "/'dʒenjuin/", "adj. 真的；真诚的", "Is this genuine leather?", 4),

    # 第12批：H开头
    ("habit", "/'hæbit/", "n. 习惯", "Develop good study habits.", 2),
    ("harvest", "/'haːvist/", "n. 收获，收成", "The harvest was bountiful.", 3),
    ("hazard", "/'hæzəd/", "n. 危险", "Smoking is a health hazard.", 4),
    ("headline", "/'hedlain/", "n. 大字标题", "Read the headline news.", 3),
    ("hesitate", "/'heziteit/", "v. 犹豫", "Don't hesitate to ask.", 4),

    # 第13批：I开头
    ("identify", "/ai'dentifai/", "v. 认出，识别", "Can you identify this bird?", 4),
    ("ignore", "/iɡ'nɔː/", "v. 忽视", "Don't ignore the problem.", 3),
    ("illegal", "/i'liːɡəl/", "adj. 非法的", "Speeding is illegal.", 3),
    ("illustrate", "/'iləstreit/", "v. 说明，图解", "Let me illustrate with an example.", 4),
    ("immigrant", "/'imiɡrənt/", "n. 移民", "Many immigrants seek better lives.", 4),

    # 第14批：J-K开头
    ("junior", "/'dʒuːniə/", "adj. 年少的；下级的", "He is my junior colleague.", 3),
    ("justify", "/'dʒʌstifai/", "v. 证明...是正当的", "Can you justify your decision?", 4),
    ("keen", "/kiːn/", "adj. 热心的；敏锐的", "He is keen on learning.", 3),
    ("knowledge", "/'nɔli dʒ/", "n. 知识", "Knowledge is power.", 2),
    ("kernel", "/'kəːnl/", "n. 果仁；核心", "This is the kernel of the issue.", 5),

    # 第15批：L开头
    ("label", "/'leibəl/", "n. 标签", "Check the product label.", 3),
    ("lack", "/læk/", "v./n. 缺乏", "They lack resources.", 3),
    ("launch", "/lɔːntʃ/", "v. 发射；发动", "They launched a new product.", 4),
    ("league", "/liːɡ/", "n. 同盟；联盟", "Which football league do you watch?", 3),
    ("leisure", "/'leʒə/", "n. 空闲时间", "Enjoy your leisure time.", 4),

    # 第16批：M开头
    ("machinery", "/mə'ʃiːnəri/", "n. 机器，机械", "The factory uses modern machinery.", 4),
    ("maintain", "/mein'tein/", "v. 维持；维修", "Maintain your car regularly.", 4),
    ("management", "/'mænidʒmənt/", "n. 管理", "Good management is essential.", 3),
    ("manufacture", "/ˌmænju'fæktʃə/", "v. 制造", "They manufacture cars.", 4),
    ("margin", "/'maːdʒin/", "n. 页边空白；边缘", "Write notes in the margin.", 4),

    # 第17批：N开头
    ("narrow", "/'nærəu/", "adj. 狭窄的", "The road is narrow.", 3),
    ("nation", "/'neiʃən/", "n. 国家", "China is a great nation.", 2),
    ("native", "/'neitiv/", "adj. 本土的", "English is his native language.", 3),
    ("nature", "/'neitʃə/", "n. 自然；本性", "Respect nature.", 2),
    ("navel", "/'neivəl/", "n. 肚脐", "", 5),

    # 第18批：O开头
    ("object", "/'ɔbdʒikt/", "n. 物体；对象", "What is that object?", 2),
    ("observe", "/əb'zəːv/", "v. 观察", "Observe the experiment carefully.", 4),
    ("obtain", "/əb'tein/", "v. 获得", "Obtain a visa before traveling.", 4),
    ("obvious", "/'ɔbviəs/", "adj. 明显的", "It's obvious that he's lying.", 3),
    ("occasion", "/ə'keiʒən/", "n. 场合", "Dress appropriately for the occasion.", 4),

    # 第19批：P开头
    ("participate", "/paː'tisipeit/", "v. 参与", "Participate in class discussions.", 4),
    ("partner", "/'paːtnə/", "n. 伙伴；搭档", "He's my business partner.", 3),
    ("patient", "/'peiʃənt/", "adj. 耐心的", "Be patient with children.", 3),
    ("pattern", "/'pætn/", "n. 模式；图案", "Follow the pattern.", 3),
    ("penalty", "/'penəlti/", "n. 惩罚", "The penalty for speeding is high.", 4),

    # 第20批：Q-R开头
    ("quality", "/'kwɔləti/", "n. 质量", "Quality matters more than quantity.", 3),
    ("quantity", "/'kwɔntəti/", "n. 数量", "Check the quantity.", 3),
    ("quote", "/kwəut/", "v. 引用", "Quote the source.", 3),
    ("racial", "/'reiʃəl/", "adj. 种族的", "We support racial equality.", 4),
    ("radiation", "/ˌreidi'eiʃən/", "n. 辐射", "Nuclear radiation is dangerous.", 5),
]

def insert_words():
    """插入单词数据到MySQL"""

    # 连接数据库
    conn = pymysql.connect(
        host='localhost',
        port=3307,
        user='root',
        password='admin123',
        database='fragment_words',
        charset='utf8mb4'
    )

    try:
        cursor = conn.cursor()

        # 插入CET4单词
        print("开始插入CET4单词...")
        insert_count = 0

        for word_data in cet4_words:
            word, phonetic, translation, example, difficulty = word_data

            sql = """
            INSERT INTO word (word, phonetic, translation, example, vocab_id, difficulty, create_time, update_time)
            VALUES (%s, %s, %s, %s, %s, %s, NOW(), NOW())
            """

            try:
                cursor.execute(sql, (word, phonetic, translation, example, 1, difficulty))
                insert_count += 1

                if insert_count % 20 == 0:
                    print(f"已插入 {insert_count} 个单词...")
                    conn.commit()

            except pymysql.IntegrityError:
                # 单词已存在，跳过
                pass

        conn.commit()
        print(f"✓ 成功插入 {insert_count} 个CET4单词！")

        # 更新词库的单词数量
        cursor.execute("UPDATE vocab SET word_count = (SELECT COUNT(*) FROM word WHERE vocab_id = 1) WHERE id = 1")
        conn.commit()
        print("✓ 已更新CET4词库的单词数量")

        # 查询统计信息
        cursor.execute("SELECT COUNT(*) FROM word WHERE vocab_id = 1")
        count = cursor.fetchone()[0]
        print(f"\n当前CET4词库共有 {count} 个单词")

    except Exception as e:
        print(f"错误: {e}")
        conn.rollback()
    finally:
        conn.close()

if __name__ == "__main__":
    print("=" * 60)
    print("锁屏背单词 - 词库数据导入工具")
    print("=" * 60)
    insert_words()
    print("\n导入完成！")
