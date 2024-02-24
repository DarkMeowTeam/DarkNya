package net.ccbluex.liquidbounce.utils.misc

import java.util.Calendar

class TipsUtils {
    companion object {
        @JvmStatic
        fun getTips(): String {
            val calendar = Calendar.getInstance()
            val hour = calendar.get(Calendar.HOUR_OF_DAY)

            return when (hour) {
                in 0..3 -> "欸?!这么晚了还要开纪..当心身体哦~"
                in 4..4 -> "4点了...是不是该做每日委托了..."
                in 5..11 -> "早上好,又是出击原神的一天"
                in 12..13 -> "中午好,主人记得吃午饭喵~"
                in 14..17 -> "下午好,开纪累了要不要出去放松一下呢~"
                in 18..23 -> "晚上好,爱你~"

                else -> "今天有什么能为主人服务的吗~"
            }
        }
    }
}