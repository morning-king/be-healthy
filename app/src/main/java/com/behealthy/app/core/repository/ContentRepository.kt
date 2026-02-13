package com.behealthy.app.core.repository

import com.behealthy.app.core.database.dao.PoemDao
import com.behealthy.app.core.database.dao.QuoteDao
import com.behealthy.app.core.database.entity.PoemEntity
import com.behealthy.app.core.database.entity.QuoteEntity
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ContentRepository @Inject constructor(
    private val quoteDao: QuoteDao,
    private val poemDao: PoemDao
) {
    fun getAllQuotes(): Flow<List<QuoteEntity>> = quoteDao.getAllQuotes()
    fun getAllPoems(): Flow<List<PoemEntity>> = poemDao.getAllPoems()

    suspend fun initializeDataIfNeeded() {
        if (quoteDao.getCount() == 0) {
            quoteDao.insertAll(initialQuotes)
        }
        if (poemDao.getCount() == 0) {
            poemDao.insertAll(initialPoems)
        }
    }

    private val initialQuotes = listOf(
        // Motivation
        QuoteEntity(content = "世上无难事，只要肯登攀。", source = "毛泽东", category = "Motivation", tags = "chinese,motivation"),
        QuoteEntity(content = "千里之行，始于足下。", source = "老子", category = "Motivation", tags = "chinese,motivation,philosophy"),
        QuoteEntity(content = "Believe you can and you're halfway there.", source = "Theodore Roosevelt", category = "Motivation", tags = "western,motivation"),
        QuoteEntity(content = "The only way to do great work is to love what you do.", source = "Steve Jobs", category = "Motivation", tags = "western,motivation,work"),
        QuoteEntity(content = "不积跬步，无以至千里；不积小流，无以成江海。", source = "荀子", category = "Motivation", tags = "chinese,motivation,philosophy"),
        QuoteEntity(content = "Success is not final, failure is not fatal: it is the courage to continue that counts.", source = "Winston Churchill", category = "Motivation", tags = "western,motivation"),
        QuoteEntity(content = "长风破浪会有时，直挂云帆济沧海。", source = "李白", category = "Motivation", tags = "chinese,motivation,poetry"),
        QuoteEntity(content = "Don't watch the clock; do what it does. Keep going.", source = "Sam Levenson", category = "Motivation", tags = "western,motivation"),
        QuoteEntity(content = "天行健，君子以自强不息。", source = "周易", category = "Motivation", tags = "chinese,philosophy"),
        QuoteEntity(content = "Act as if what you do makes a difference. It does.", source = "William James", category = "Motivation", tags = "western,motivation"),

        // Health
        QuoteEntity(content = "身体是革命的本钱。", source = "毛泽东", category = "Health", tags = "chinese,health"),
        QuoteEntity(content = "Health is the greatest gift, contentment the greatest wealth, faithfulness the best relationship.", source = "Buddha", category = "Health", tags = "western,health,philosophy"),
        QuoteEntity(content = "流水不腐，户枢不蠹。", source = "吕氏春秋", category = "Health", tags = "chinese,health"),
        QuoteEntity(content = "Early to bed and early to rise makes a man healthy, wealthy and wise.", source = "Benjamin Franklin", category = "Health", tags = "western,health"),
        QuoteEntity(content = "养生之道，常欲小劳。", source = "孙思邈", category = "Health", tags = "chinese,health"),
        QuoteEntity(content = "A healthy outside starts from the inside.", source = "Robert Urich", category = "Health", tags = "western,health"),
        QuoteEntity(content = "食不语，寝不言。", source = "论语", category = "Health", tags = "chinese,health,habit"),
        QuoteEntity(content = "To keep the body in good health is a duty... otherwise we shall not be able to keep our mind strong and clear.", source = "Buddha", category = "Health", tags = "western,health"),
        QuoteEntity(content = "笑一笑，十年少。", source = "民间谚语", category = "Health", tags = "chinese,health,happiness"),
        QuoteEntity(content = "Physical fitness is not only one of the most important keys to a healthy body, it is the basis of dynamic and creative intellectual activity.", source = "John F. Kennedy", category = "Health", tags = "western,health"),

        // Life
        QuoteEntity(content = "Life is what happens when you're busy making other plans.", source = "John Lennon", category = "Life", tags = "western,life"),
        QuoteEntity(content = "人生得意须尽欢，莫使金樽空对月。", source = "李白", category = "Life", tags = "chinese,life,poetry"),
        QuoteEntity(content = "In the end, it's not the years in your life that count. It's the life in your years.", source = "Abraham Lincoln", category = "Life", tags = "western,life"),
        QuoteEntity(content = "采菊东篱下，悠然见南山。", source = "陶渊明", category = "Life", tags = "chinese,life,poetry,nature"),
        QuoteEntity(content = "Get busy living or get busy dying.", source = "Stephen King", category = "Life", tags = "western,life"),
        QuoteEntity(content = "路漫漫其修远兮，吾将上下而求索。", source = "屈原", category = "Life", tags = "chinese,life,philosophy"),
        QuoteEntity(content = "You only live once, but if you do it right, once is enough.", source = "Mae West", category = "Life", tags = "western,life"),
        QuoteEntity(content = "海内存知己，天涯若比邻。", source = "王勃", category = "Life", tags = "chinese,life,friendship"),
        QuoteEntity(content = "Life is really simple, but we insist on making it complicated.", source = "Confucius", category = "Life", tags = "western,life,philosophy"),
        QuoteEntity(content = "非淡泊无以明志，非宁静无以致远。", source = "诸葛亮", category = "Life", tags = "chinese,life,philosophy")
    )

    private val initialPoems = listOf(
        // Tang Dynasty
        PoemEntity(content = "床前明月光，疑是地上霜。\n举头望明月，低头思故乡。", author = "李白", title = "静夜思", dynasty = "Tang", category = "Classic", tags = "tang,moon,homesick"),
        PoemEntity(content = "春眠不觉晓，处处闻啼鸟。\n夜来风雨声，花落知多少。", author = "孟浩然", title = "春晓", dynasty = "Tang", category = "Classic", tags = "tang,spring,nature"),
        PoemEntity(content = "白日依山尽，黄河入海流。\n欲穷千里目，更上一层楼。", author = "王之涣", title = "登鹳雀楼", dynasty = "Tang", category = "Classic", tags = "tang,scenery,ambition"),
        PoemEntity(content = "红豆生南国，春来发几枝。\n愿君多采撷，此物最相思。", author = "王维", title = "相思", dynasty = "Tang", category = "Classic", tags = "tang,love,nature"),
        PoemEntity(content = "慈母手中线，游子身上衣。\n临行密密缝，意恐迟迟归。\n谁言寸草心，报得三春晖。", author = "孟郊", title = "游子吟", dynasty = "Tang", category = "Classic", tags = "tang,mother,love"),
        PoemEntity(content = "空山新雨后，天气晚来秋。\n明月松间照，清泉石上流。", author = "王维", title = "山居秋暝", dynasty = "Tang", category = "Nature", tags = "tang,autumn,nature"),
        PoemEntity(content = "朝辞白帝彩云间，千里江陵一日还。\n两岸猿声啼不住，轻舟已过万重山。", author = "李白", title = "早发白帝城", dynasty = "Tang", category = "Travel", tags = "tang,travel,scenery"),
        PoemEntity(content = "独在异乡为异客，每逢佳节倍思亲。\n遥知兄弟登高处，遍插茱萸少一人。", author = "王维", title = "九月九日忆山东兄弟", dynasty = "Tang", category = "Family", tags = "tang,festival,homesick"),
        PoemEntity(content = "清明时节雨纷纷，路上行人欲断魂。\n借问酒家何处有？牧童遥指杏花村。", author = "杜牧", title = "清明", dynasty = "Tang", category = "Festival", tags = "tang,festival,spring"),
        PoemEntity(content = "离离原上草，一岁一枯荣。\n野火烧不尽，春风吹又生。", author = "白居易", title = "赋得古原草送别", dynasty = "Tang", category = "Nature", tags = "tang,nature,farewell"),

        // Song Dynasty
        PoemEntity(content = "明月几时有？把酒问青天。\n不知天上宫阙，今夕是何年。", author = "苏轼", title = "水调歌头", dynasty = "Song", category = "Classic", tags = "song,moon,philosophy"),
        PoemEntity(content = "大江东去，浪淘尽，千古风流人物。", author = "苏轼", title = "念奴娇·赤壁怀古", dynasty = "Song", category = "History", tags = "song,history,hero"),
        PoemEntity(content = "莫听穿林打叶声，何妨吟啸且徐行。\n竹杖芒鞋轻胜马，谁怕？一蓑烟雨任平生。", author = "苏轼", title = "定风波", dynasty = "Song", category = "Life", tags = "song,rain,optimism"),
        PoemEntity(content = "昨夜雨疏风骤，浓睡不消残酒。\n试问卷帘人，却道海棠依旧。\n知否，知否？应是绿肥红瘦。", author = "李清照", title = "如梦令", dynasty = "Song", category = "Nature", tags = "song,spring,flowers"),
        PoemEntity(content = "寻寻觅觅，冷冷清清，凄凄惨惨戚戚。", author = "李清照", title = "声声慢", dynasty = "Song", category = "Emotion", tags = "song,sadness,autumn"),
        PoemEntity(content = "怒发冲冠，凭栏处、潇潇雨歇。", author = "岳飞", title = "满江红", dynasty = "Song", category = "Patriotism", tags = "song,war,hero"),
        PoemEntity(content = "众里寻他千百度，蓦然回首，那人却在，灯火阑珊处。", author = "辛弃疾", title = "青玉案·元夕", dynasty = "Song", category = "Love", tags = "song,festival,love"),
        PoemEntity(content = "生当作人杰，死亦为鬼雄。\n至今思项羽，不肯过江东。", author = "李清照", title = "夏日绝句", dynasty = "Song", category = "History", tags = "song,history,hero"),
        PoemEntity(content = "竹外桃花三两枝，春江水暖鸭先知。", author = "苏轼", title = "惠崇春江晚景", dynasty = "Song", category = "Nature", tags = "song,spring,nature"),
        PoemEntity(content = "千门万户曈曈日，总把新桃换旧符。", author = "王安石", title = "元日", dynasty = "Song", category = "Festival", tags = "song,newyear,festival"),

        // Others
        PoemEntity(content = "风萧萧兮易水寒，壮士一去兮不复还。", author = "荆轲", title = "易水歌", dynasty = "Pre-Qin", category = "History", tags = "pre-qin,history,hero"),
        PoemEntity(content = "采采芣苢，薄言采之。采采芣苢，薄言有之。", author = "佚名", title = "芣苢", dynasty = "Pre-Qin", category = "Nature", tags = "pre-qin,labor,nature"),
        PoemEntity(content = "关关雎鸠，在河之洲。窈窕淑女，君子好逑。", author = "佚名", title = "关雎", dynasty = "Pre-Qin", category = "Love", tags = "pre-qin,love,classic"),
        PoemEntity(content = "对酒当歌，人生几何！譬如朝露，去日苦多。", author = "曹操", title = "短歌行", dynasty = "Han", category = "Life", tags = "han,life,philosophy"),
        PoemEntity(content = "枯藤老树昏鸦，小桥流水人家，古道西风瘦马。", author = "马致远", title = "天净沙·秋思", dynasty = "Yuan", category = "Nature", tags = "yuan,autumn,homesick")
    )
}
