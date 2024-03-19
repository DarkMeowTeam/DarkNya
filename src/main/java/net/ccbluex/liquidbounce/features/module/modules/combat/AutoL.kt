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
import net.ccbluex.liquidbounce.value.BoolValue
import net.ccbluex.liquidbounce.value.IntegerValue
import net.ccbluex.liquidbounce.value.ListValue
import net.ccbluex.liquidbounce.value.TextValue
import net.ccbluex.liquidbounce.utils.extensions.getDistanceToEntityBox
import net.minecraft.entity.Entity
import net.minecraft.network.play.server.SPacketChat
import java.io.File

 @ModuleInfo(name = "AutoL", description = "AutoL", category = ModuleCategory.COMBAT)
class AutoL : Module() {
    private val enablement = BoolValue ("EnableAutoL", true)
    private val delayValue = IntegerValue("ChatDelay",3000,2400,6000)
    private val mode = ListValue("Mode", arrayOf("WithWords","RawWord","Clear","Custom"),"Clear")
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
