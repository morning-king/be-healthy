package com.behealthy.app.feature.mood

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.behealthy.app.core.database.entity.MoodRecordEntity
import com.behealthy.app.core.repository.MoodRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay
import java.time.LocalDate
import java.time.YearMonth
import javax.inject.Inject

data class Poem(val content: String, val author: String)

@HiltViewModel
class MoodViewModel @Inject constructor(
    private val moodRepository: MoodRepository,
    private val weatherRepository: com.behealthy.app.core.repository.WeatherRepository
) : ViewModel() {

    private val _refreshTrigger = MutableStateFlow(0)
    
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    val moodRecords: StateFlow<List<MoodRecordEntity>> = combine(moodRepository.getAllMoods(), _refreshTrigger) { moods, _ ->
        moods.groupBy { it.date }
            .mapValues { (_, records) -> records.maxByOrNull { it.id }!! }
            .values.toList()
            .sortedByDescending { it.date }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _selectedDate = MutableStateFlow(LocalDate.now())
    val selectedDate: StateFlow<LocalDate> = _selectedDate

    private val _currentMonth = MutableStateFlow(YearMonth.now())

    val weatherForSelectedDate = _selectedDate.flatMapLatest { date ->
        weatherRepository.getWeatherForDate(date)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val weatherForCurrentMonth = _currentMonth.flatMapLatest { month ->
        flow {
            emit(weatherRepository.getWeatherForMonth(month.year, month.monthValue))
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyMap())

    private val favoritePoems = listOf(
        // 王维 (5首)
        Poem("行到水穷处，坐看云起时。", "王维"),
        Poem("空山新雨后，天气晚来秋。", "王维"),
        Poem("明月松间照，清泉石上流。", "王维"),
        Poem("劝君更尽一杯酒，西出阳关无故人。", "王维"),
        Poem("大漠孤烟直，长河落日圆。", "王维"),
        
        // 李白 (5首)
        Poem("长风破浪会有时，直挂云帆济沧海。", "李白"),
        Poem("天生我材必有用，千金散尽还复来。", "李白"),
        Poem("两岸猿声啼不住，轻舟已过万重山。", "李白"),
        Poem("君不见黄河之水天上来，奔流到海不复回。", "李白"),
        Poem("举杯邀明月，对影成三人。", "李白"),
        
        // 李清照 (4首)
        Poem("生当作人杰，死亦为鬼雄。", "李清照"),
        Poem("知否，知否？应是绿肥红瘦。", "李清照"),
        Poem("花自飘零水自流，一种相思，两处闲愁。", "李清照"),
        Poem("莫道不销魂，帘卷西风，人比黄花瘦。", "李清照"),
        
        // 李煜 (10首) - 新增权重提高
        Poem("问君能有几多愁？恰似一江春水向东流。", "李煜"),
        Poem("剪不断，理还乱，是离愁。别是一般滋味在心头。", "李煜"),
        Poem("独自莫凭栏，无限江山，别时容易见时难。", "李煜"),
        Poem("流水落花春去也，天上人间。", "李煜"),
        Poem("林花谢了春红，太匆匆。无奈朝来寒雨晚来风。", "李煜"),
        Poem("胭脂泪，相留醉，几时重。自是人生长恨水长东。", "李煜"),
        Poem("春花秋月何时了？往事知多少。", "李煜"),
        Poem("小楼昨夜又东风，故国不堪回首月明中。", "李煜"),
        Poem("雕栏玉砌应犹在，只是朱颜改。", "李煜"),
        Poem("无言独上西楼，月如钩。寂寞梧桐深院锁清秋。", "李煜"),
        
        // 苏轼 (60首) - 新增权重提高
        Poem("大江东去，浪淘尽，千古风流人物。", "苏轼"),
        Poem("竹杖芒鞋轻胜马，谁怕？一蓑烟雨任平生。", "苏轼"),
        Poem("人生如梦，一尊还酹江月。", "苏轼"),
        Poem("但愿人长久，千里共婵娟。", "苏轼"),
        Poem("十年生死两茫茫，不思量，自难忘。", "苏轼"),
        Poem("明月几时有？把酒问青天。", "苏轼"),
        Poem("不识庐山真面目，只缘身在此山中。", "苏轼"),
        Poem("欲把西湖比西子，淡妆浓抹总相宜。", "苏轼"),
        Poem("春宵一刻值千金，花有清香月有阴。", "苏轼"),
        Poem("横看成岭侧成峰，远近高低各不同。", "苏轼"),
        
        // 苏轼 - 豪放词 (15首)
        Poem("老夫聊发少年狂，左牵黄，右擎苍。", "苏轼"),
        Poem("会挽雕弓如满月，西北望，射天狼。", "苏轼"),
        Poem("谁道人生无再少？门前流水尚能西！", "苏轼"),
        Poem("休将白发唱黄鸡。", "苏轼"),
        Poem("一点浩然气，千里快哉风。", "苏轼"),
        Poem("人生如逆旅，我亦是行人。", "苏轼"),
        Poem("此心安处是吾乡。", "苏轼"),
        Poem("人间有味是清欢。", "苏轼"),
        Poem("浮名浮利，虚苦劳神。", "苏轼"),
        Poem("江山如画，一时多少豪杰。", "苏轼"),
        Poem("多情应笑我，早生华发。", "苏轼"),
        Poem("一蓑烟雨任平生。", "苏轼"),
        Poem("也无风雨也无晴。", "苏轼"),
        Poem("诗酒趁年华。", "苏轼"),
        Poem("此身如传舍，何处是吾乡？", "苏轼"),
        
        // 苏轼 - 写景咏物 (15首)
        Poem("黑云翻墨未遮山，白雨跳珠乱入船。", "苏轼"),
        Poem("卷地风来忽吹散，望湖楼下水如天。", "苏轼"),
        Poem("荷尽已无擎雨盖，菊残犹有傲霜枝。", "苏轼"),
        Poem("一年好景君须记，最是橙黄橘绿时。", "苏轼"),
        Poem("簌簌衣巾落枣花，村南村北响缲车。", "苏轼"),
        Poem("牛衣古柳卖黄瓜。", "苏轼"),
        Poem("殷勤昨夜三更雨，又得浮生一日凉。", "苏轼"),
        Poem("东风袅袅泛崇光，香雾空蒙月转廊。", "苏轼"),
        Poem("只恐夜深花睡去，故烧高烛照红妆。", "苏轼"),
        Poem("微雨如酥，草色遥看近却无。", "苏轼"),
        Poem("休辞醉倒，花不看开人易老。", "苏轼"),
        Poem("春未老，风细柳斜斜。", "苏轼"),
        Poem("试上超然台上看，半壕春水一城花。", "苏轼"),
        Poem("烟雨暗千家。", "苏轼"),
        Poem("且将新火试新茶，诗酒趁年华。", "苏轼"),
        
        // 苏轼 - 哲理人生 (15首)
        Poem("人有悲欢离合，月有阴晴圆缺，此事古难全。", "苏轼"),
        Poem("但愿人长久，千里共婵娟。", "苏轼"),
        Poem("人生如逆旅，我亦是行人。", "苏轼"),
        Poem("此心安处是吾乡。", "苏轼"),
        Poem("浮名浮利，虚苦劳神。", "苏轼"),
        Poem("且陶陶、乐尽天真。", "苏轼"),
        Poem("几时归去，作个闲人。", "苏轼"),
        Poem("对一张琴，一壶酒，一溪云。", "苏轼"),
        Poem("世事一场大梦，人生几度秋凉？", "苏轼"),
        Poem("夜来风叶已鸣廊，看取眉头鬓上。", "苏轼"),
        Poem("酒贱常愁客少，月明多被云妨。", "苏轼"),
        Poem("中秋谁与共孤光。", "苏轼"),
        Poem("把盏凄然北望。", "苏轼"),
        Poem("古今如梦，何曾梦觉，但有旧欢新怨。", "苏轼"),
        Poem("异时对，黄楼夜景，为余浩叹。", "苏轼"),
        
        // 苏轼 - 友情送别 (5首)
        Poem("醉笑陪公三万场，不用诉离觞。", "苏轼"),
        Poem("人生如逆旅，我亦是行人。", "苏轼"),
        Poem("且尽十分芳酒，共倾一梦浮生。", "苏轼"),
        Poem("明日酒醒何处，杨柳岸晓风残月。", "苏轼"),
        Poem("相逢一醉是前缘，风雨散、飘然何处。", "苏轼")
    )

    private val otherPoems = listOf(
        // 毛泽东
        Poem("雄关漫道真如铁，而今迈步从头越。", "毛泽东"),
        Poem("天若有情天亦老，人间正道是沧桑。", "毛泽东"),
        Poem("数风流人物，还看今朝。", "毛泽东"),
        
        // 杜甫
        Poem("会当凌绝顶，一览众山小。", "杜甫"),
        Poem("随风潜入夜，润物细无声。", "杜甫"),
        Poem("安得广厦千万间，大庇天下寒士俱欢颜。", "杜甫"),
        
        // 苏东坡
        Poem("大江东去，浪淘尽，千古风流人物。", "苏东坡"),
        Poem("竹杖芒鞋轻胜马，谁怕？一蓑烟雨任平生。", "苏东坡"),
        Poem("人生如梦，一尊还酹江月。", "苏东坡"),
        Poem("但愿人长久，千里共婵娟。", "苏东坡"),
        
        // 其他著名诗人
        Poem("同是天涯沦落人，相逢何必曾相识。", "白居易"),
        Poem("乱花渐欲迷人眼，浅草才能没马蹄。", "白居易"),
        Poem("商女不知亡国恨，隔江犹唱后庭花。", "杜牧"),
        Poem("停车坐爱枫林晚，霜叶红于二月花。", "杜牧"),
        Poem("春蚕到死丝方尽，蜡炬成灰泪始干。", "李商隐"),
        Poem("身无彩凤双飞翼，心有灵犀一点通。", "李商隐"),
        Poem("众里寻他千百度，蓦然回首，那人却在，灯火阑珊处。", "辛弃疾"),
        Poem("醉里挑灯看剑，梦回吹角连营。", "辛弃疾")
    )

    private val _currentPoem = MutableStateFlow(getRandomPoem())
    val currentPoem: StateFlow<Poem> = _currentPoem

    fun saveMood(date: LocalDate, moodText: String, note: String, audioPath: String? = null, audioDuration: Long = 0) {
        viewModelScope.launch {
            _isLoading.value = true
            val mood = MoodRecordEntity(
                date = date.toString(),
                mood = moodText,
                note = note,
                audioPath = audioPath,
                audioDuration = audioDuration
            )
            moodRepository.saveMood(mood)
            _refreshTrigger.value += 1
            delay(1000) // Show loading animation
            _isLoading.value = false
        }
    }
    
    fun updateDate(date: LocalDate) {
        _selectedDate.value = date
    }

    fun updateMonth(month: YearMonth) {
        _currentMonth.value = month
    }

    fun refreshPoem() {
        _currentPoem.value = getRandomPoem()
    }

    private fun getRandomPoem(): Poem {
        // Updated weight distribution for rich Su Shi poetry collection:
        // - Li Yu poems (10 total) get 2x weight (20 effective)
        // - Su Shi poems (60 total) get 3x weight (180 effective) 
        // - Other poets get normal weight (19 total)
        // - Total effective weight: 219
        // - Li Yu/Su Shi combined probability: 200/219 ≈ 91%
        // - Su Shi alone probability: 180/219 ≈ 82%
        
        val allPoems = favoritePoems + otherPoems
        val liYuPoems = allPoems.filter { it.author == "李煜" }
        val suShiPoems = allPoems.filter { it.author == "苏轼" }
        val otherPoemsFiltered = allPoems.filter { it.author != "李煜" && it.author != "苏轼" }
        
        val randomValue = kotlin.random.Random.nextDouble()
        return when {
            randomValue < (180.0 / 219.0) -> suShiPoems.random()  // 82% Su Shi
            randomValue < (200.0 / 219.0) -> liYuPoems.random() // 9% Li Yu  
            else -> otherPoemsFiltered.random()                  // 9% Others
        }
    }
}
