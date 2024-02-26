 package net.ccbluex.liquidbounce.features.module.modules.combat;

import net.ccbluex.liquidbounce.DarkNya
import net.ccbluex.liquidbounce.event.*
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.features.module.ModuleCategory
import net.ccbluex.liquidbounce.features.module.ModuleInfo
import net.ccbluex.liquidbounce.ui.client.hud.element.elements.InfosUtils.Recorder
import net.ccbluex.liquidbounce.ui.client.hud.element.elements.Notification
import net.ccbluex.liquidbounce.ui.client.hud.element.elements.NotifyType
import net.ccbluex.liquidbounce.utils.EntityUtils
import net.ccbluex.liquidbounce.utils.misc.RandomUtils
import net.ccbluex.liquidbounce.utils.timer.MSTimer
import net.ccbluex.liquidbounce.features.value.BoolValue
import net.ccbluex.liquidbounce.features.value.IntegerValue
import net.ccbluex.liquidbounce.features.value.ListValue
import net.ccbluex.liquidbounce.features.value.TextValue
import net.ccbluex.liquidbounce.utils.extensions.getDistanceToEntityBox
import net.minecraft.entity.Entity
import net.minecraft.network.play.server.SPacketChat
import java.io.File

 @ModuleInfo(name = "AutoL", description = "AutoL", category = ModuleCategory.COMBAT)
class AutoL : Module() {
    private val enablement = BoolValue ("EnableAutoL", true)
    private val delayValue = IntegerValue("ChatDelay",3000,2400,6000)
    private val mode = ListValue("Mode", arrayOf("WithWords","RawWord","Clear","Custom","HeyGuy"),"Clear")
    private val waterMark = BoolValue ("WaterMark", true)
    private val enableHYTAtall = BoolValue ("Prefix@", true)
    private val textValue = TextValue("Text", "ExampleChat")
    private val chatTotalKill = BoolValue("ChatTotalKill",false)
    private val suffixTextBeforeRecord = TextValue("TextBeforeKillRecord","我已经击杀了")
    private val suffixTextAfterRecord = TextValue("TextAfterKillRecord","人!")
    private val showNotification = BoolValue("ShowNotificationsOnKill",false)

    // Target
    private val lastAttackTimer = MSTimer()
    var target: Entity? = null
    private var kill = 0
    private var tempkill = 0
    private var text = ""
    private var inCombat = false
    private val attackedEntityList = mutableListOf<Entity>()
    private val insultFile = File(DarkNya.fileManager.dir, "filter.json")
    private var insultWords = mutableListOf<String>()
    private val ms = MSTimer()
    private val delay = MSTimer()

    @EventTarget
    fun onAttack(event: AttackEvent) {
        val target = event.targetEntity

        if ((target is Entity) && EntityUtils.isSelected(target, true)) {
            this.target = target
            if (!attackedEntityList.contains(target)) {
                attackedEntityList.add(target)
            }
        }
        lastAttackTimer.reset()
    }

    @EventTarget
    fun onUpdate(event: UpdateEvent) {
        attackedEntityList.filter { it.isDead }.forEach {
            playerDeathEvent(it.name!!)
            attackedEntityList.remove(it)
        }

        inCombat = false

        if (!lastAttackTimer.hasTimePassed(1000)) {
            inCombat = true
            return
        }

        if (target != null) {
            if (mc.player!!.getDistanceToEntityBox(target!!) > 7 || !inCombat || target!!.isDead) {
                target = null
            } else {
                inCombat = true
            }
        }
    }

    @EventTarget
    fun onPacket(event: PacketEvent) {
        val packet = event.packet
        if (packet is SPacketChat) {
            val chat = packet.chatComponent.unformattedText
            if (chat.contains("起床战争") && chat.contains(">>") && chat.contains("游戏开始")) {
                attackedEntityList.clear()
            }
        }
    }

    @EventTarget
    fun onWorld(event: WorldEvent) {
        inCombat = false
        target = null
        attackedEntityList.clear()
    }

    private fun showNotifications (number: Int) {
        if (showNotification.get()) {
            DarkNya.hud.addNotification(
                Notification(
                    (if (number > 1) "$number Kills!" else "Kill!"),
                    ("You killed $number ${if (number > 1) "players" else "player"}"),
                    NotifyType.INFO
                )
            )
        }
    }
    private fun playerDeathEvent (name: String) {
        kill++
        Recorder.killCounts++
        tempkill++
        if (delay.hasTimePassed(delayValue.get().toLong())) {
            playerChat(name)
            delay.reset()
        }
        if (ms.hasTimePassed(5000)) {
            showNotifications(tempkill)
            tempkill = 0
            ms.reset()
        }
        if (!inCombat && tempkill != 0 && ms.hasTimePassed(5000)) {
            showNotifications(tempkill)
            tempkill = 0
            ms.reset()

        }
    }

    private fun playerChat(name: String) {
        if (enablement.get()) {
            when (mode.get().toLowerCase()) {
                "custom" -> {
                    text = (textValue.get())
                    text = (text.replace("%name%",name))
                    text = ("$text ")
                }
                "clear" -> {
                    text = ("Ｌ $name")
                }
                "rawwords" -> {
                    text = (getRandomOne())
                }
                "withwords" -> {
                    text = ("Ｌ $name | " + getRandomOne())
                }
                "heyguy" -> {
                    val random = (Math.random() * 7).toInt()
                    when(random){
                        1 -> text = ("Ｌ $name | 嗨，我是风动，这是我的neibu神器，3000收neibu是我的秘密武器，花钱一分钟，赚钱两个月，不要告诉别人哦")
                        2 -> text = ("Ｌ $name | 嗨，我是Pro，这是我的neibu神器，200整30个conf是我的秘密武器，花钱一分钟，赚钱两年半，不要告诉别人哦")
                        3 -> text = ("Ｌ $name | 嗨，我是瓦瓦，这是我的pride+神器，grimvel是我的秘密武器，vel一分钟，死号两小时，不要告诉_RyF哦")
                        4 -> text = ("Ｌ $name | 嗨，我是_RyF,这是我的彩色字节，彩色字节是我的秘密武器，出击一分钟，殴打两小时，不要告诉瓦瓦哦")
                        5 -> text = ("Ｌ $name | 嗨，我是狼牙，这是我的混淆神器，NellyObf是我的秘密武器，花钱一秒钟，抽烟一辈子，不要告诉paimon哦")
                        6 -> text = ("Ｌ $name | 嗨，我是原批，这是我的启动神器，你说的对，但是原神启动是我的秘密武器，启动十分钟，充电五小时，不要告诉别人哦")
                        7 -> text = ("Ｌ $name | 嗨，我是执剑，这是我的圈钱神器，圈钱造谣是我的秘密武器，圈钱一秒钟，高兴一个月，不要告诉别人哦")
                        8 -> text = ("Ｌ $name | 嗨，我是林鸿坤，这是我的圈钱神器，skid卡顿是我的秘密武器，女装一秒钟，被开一个月，不要告诉别人哦")
                        9 -> text = ("Ｌ $name | 嗨，我是小七，这是我的恋爱神器，刘子源是我的秘密武器，变声一秒钟，分手一个月，不要告诉别人哦")
                        10 -> text = ("Ｌ $name | 嗨，我是141，这是我的脱盒神器，肉钩是我的秘密武器，制作一小时，坐牢一个月，不要告诉别人哦")
                        11 -> text = ("Ｌ $name | 嗨，我是鸡屎，这是我的开端神器，Jsmh是我的秘密武器，开源一小时，死亡一个月，不要告诉别人哦")
                        12 -> text = ("Ｌ $name | 嗨，我是王航，这是我的神权神器，ban是我的秘密武器，登机一秒钟，神权一辈子，不要告诉别人哦")
                        13 -> text = ("Ｌ $name | 嗨，我是Start:新年快乐，这是我的残疾神器，0Hacker是我的秘密武器，圈钱五分钟，跑路两小时，不要告诉别人哦")
                        // https://github.com/catx-feitu/catx-feitu/blob/main/fun/0mc_fw_client.md
                        14 -> text = ("Ｌ $name | 嗨，我是loyisa，这是我的测试神器，mc点loyisa点cn是我的秘密武器，拉参五分钟，绕过两小时，不要告诉别人哦")
                        15 -> text = ("Ｌ $name | 嗨，我是余志文，这是我的圆孔神器，已上线是我的秘密武器，圆孔五分钟，死亡两小时，不要告诉别人哦")
                        16 -> text = ("Ｌ $name | 嗨，我是Maragele，这是我的助焊剂，flux是我的秘密武器，圆孔五分钟，出道两小时，不要告诉黄天柱哦")
                        17 -> text = ("Ｌ $name | 嗨，我是黄天柱，这是我的开户神器，李佳乐是我的秘密武器，开户五分钟，被开两小时，不要告诉Maragele哦")

                        18 -> text = ("Ｌ $name | 嗨，我是Netease，这是我的起诉神器，吕四寒是我的秘密武器，发韩五分钟，丢弃两小时，不要告诉dev哦")

                        19 -> text = ("Ｌ $name | 嗨，我是GrimAC，这是我的模拟神器，vl是我的秘密武器，开记五分钟，死亡三十天，不要告诉lowiq哦")
                        20 -> text = ("Ｌ $name | 嗨，我是Spartan，这是我的检测神器，vl是我的秘密武器，开记五分钟，死亡三十天，不要告诉lowiq哦")
                        21 -> text = ("Ｌ $name | 嗨，我是Vulcan，这是我的提权神器，GUI判断管理界面是我的秘密武器，铁砧五分钟，op两小时，不要告诉GrimAC哦")
                        22 -> text = ("Ｌ $name | 嗨，我是Vanilla，这是我的踢人神器，fly kick是我的秘密武器，fly五分钟，kick两小时，不要告诉Staff哦")

                        23 -> text = ("Ｌ $name | 嗨，我是纳西妲，这是我的神权神器，会说话是我的秘密武器，发言五分钟，神权两小时，不要告诉王航哦")
                    }
                }
            }
            replaceFilterWords()
            if (chatTotalKill.get()) text = ("$text | " + suffixTextBeforeRecord.get() + kill + suffixTextAfterRecord.get())
            if (waterMark.get()) text = ("[${DarkNya.CLIENT_NAME}] $text")
            if (enableHYTAtall.get()) text = ("@a$text")
            mc.player!!.sendChatMessage(text)
        }
    }

    private fun getRandomOne(): String {
        return insultWords[RandomUtils.nextInt(0, insultWords.size - 1)]
    }

    fun resetAttackedList() {
        attackedEntityList.clear()
    }

    private fun replaceFilterWords() {
        text = (text.replace("%L%","Ｌ"))
        text = (text.replace("%l%","Ｌ"))

    }


    override val tag: String
        get() = "Kills $kill"
}
