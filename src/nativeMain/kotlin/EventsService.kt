import interop.io.*
import kotlinx.cinterop.*


@ThreadLocal
val listeners by lazy {
    mutableListOf<EventListener>()
}

class EventsService(private val device: Device) {

    fun listen() {

        val fileId = open(device.path, platform.posix.O_RDONLY);
        if (fileId == -1) return

        var readBuffer = memScoped { readBuffer(fileId).getPointer(this) }

        while (readBuffer.pointed.readBytes.toInt() != EOF) {

            val inputEvent = readBuffer.pointed.event

            val eventCode = inputEvent.code.toInt()
            val eventValue = inputEvent.value
            val eventType = inputEvent.type.toInt()
            val eventTime = inputEvent.time.tv_sec

            when (eventType) {
                EV_KEY -> { // Used for keyboards and devices like that
                    // DO event escalation
                    val event = ButtonPress(eventCode, eventValue);
                    device add event
                }
                EV_SYN -> { // This is called on event batch
                    // If input is detached the event listener should pause and resume on input restore
                    // i.e ef event times == 0
                    // If I hit this with no pending event's then close application
                    if (device.pendingEvents.size == 0) {
                        exit(-1)
                    }
                    device.fire()
                    device.pendingEvents.clear()

                }
                EV_REL -> { //  used to describe relative axis value changes i.e moving mouse 5 units left
                    println("REL")
                }
                EV_ABS -> { // This handles events at a point
                    println("ABS event")
                    when (eventCode) {
                        ABS_PRESSURE -> println("Pressure event: ${eventValue}")
                        ABS_X -> {
                            inputEvent.value.also { x ->
                                listeners.filterIsInstance<MotionEventListener>().forEach { listener ->
                                    listener.updateX(x)
                                }
                            }
                        }
                        ABS_Y -> {
                            inputEvent.value.also { y ->
                                listeners.filterIsInstance<MotionEventListener>().forEach { listener ->
                                    listener.updateX(y)
                                }
                            }
                        }
                        ABS_MT_TOUCH_MINOR -> {
                            println("Touch minor")
                        }
                        ABS_MT_TOUCH_MAJOR -> {
                            println("Major touch event")
                        }
                        else -> {
                            println("non-utilized event: $eventCode")
                        }
                    }
                }
                EV_MSC -> {
                    println(
                        """
                        |MSC
                        |code : $eventCode
                        |value: $eventValue
                        |time: $eventTime
                    """.trimMargin()
                    )
                }
                EV_SW -> {
                    println("SW")
                }
                EV_LED -> {

                }
                EV_SND -> {

                }
                EV_REP -> {
                    println("Repeating events")
                }
                EV_FF -> {

                }
                EV_PWR -> { // POWER BUTTON EVENTS

                }
                EV_FF_STATUS -> {

                }
                EV_MAX -> {

                }
                EV_CNT -> {

                }
                else -> {
                    println("Unrecognized event type")
                }
            }

            readBuffer = memScoped { readBuffer(fileId).getPointer(this) }

        }

    }

}