
package op.wawa.utils.sound

import me.utils.FileUtils
import net.ccbluex.liquidbounce.DarkNya
import java.io.File

class TipSoundManager {
    var enableSound : TipSoundPlayer
    var disableSound : TipSoundPlayer

    init {
        val enableSoundFile = File(DarkNya.fileManager.soundsDir,"enable.wav")
        val disableSoundFile = File(DarkNya.fileManager.soundsDir,"disable.wav")

        if(!enableSoundFile.exists())
            FileUtils.unpackFile(enableSoundFile,"assets/minecraft/darknya/sound/enable.wav")

        if(!disableSoundFile.exists())
            FileUtils.unpackFile(disableSoundFile,"assets/minecraft/darknya/sound/disable.wav")

        enableSound = TipSoundPlayer(enableSoundFile)
        disableSound = TipSoundPlayer(disableSoundFile)
    }
}