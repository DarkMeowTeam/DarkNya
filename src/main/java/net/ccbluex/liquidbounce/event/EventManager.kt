package net.ccbluex.liquidbounce.event

import net.ccbluex.liquidbounce.features.module.modules.client.EventManage
import net.ccbluex.liquidbounce.utils.ClientUtils
import java.util.*

class EventManager {

    private val registry = HashMap<Class<out Event>, MutableList<EventHook>>()

    /**
     * Register [listener]
     */
    fun registerListener(listener: Listenable) {
        for (method in listener.javaClass.declaredMethods) {
            if (method.isAnnotationPresent(EventTarget::class.java) && method.parameterTypes.size == 1) {
                if (!method.isAccessible)
                    method.isAccessible = true

                val eventClass = method.parameterTypes[0] as Class<out Event>
                val eventTarget = method.getAnnotation(EventTarget::class.java)

                val invokableEventTargets = registry.getOrDefault(eventClass, ArrayList())
                invokableEventTargets.add(EventHook(listener, method, eventTarget))
                registry[eventClass] = invokableEventTargets
            }
        }
    }

    /**
     * Unregister listener
     *
     * @param listenable for unregister
     */
    fun unregisterListener(listenable: Listenable) {
        for ((key, targets) in registry) {
            targets.removeIf { it.eventClass == listenable }

            registry[key] = targets
        }
    }

    /**
     * Call event to listeners
     *
     * @param event to call
     */
    fun callEvent(event: Event) {
        val targets = registry[event.javaClass] ?: return

        for (invokableEventTarget in targets) {
            val now = System.currentTimeMillis()
            try {
                if (!invokableEventTarget.eventClass.handleEvents() && !invokableEventTarget.isIgnoreCondition)
                    continue

                invokableEventTarget.method.invoke(invokableEventTarget.eventClass, event)
            } catch (throwable: Throwable) {
                EventManage.onException(throwable)
            }
            val used : Long = System.currentTimeMillis() - now
            if (used > 100) EventManage.onLag(used, event, invokableEventTarget)
        }
    }
}
