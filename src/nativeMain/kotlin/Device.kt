import platform.posix.system

data class Device(
    val identifier: String
) {

    var path: String? = null

    val pendingEvents by lazy {
        mutableListOf<Event>()
    }

    var listener: EventListener? = null

    val sensors by lazy {
        mutableListOf<Sensor>()
    }

    fun upgradeSensor(sensor: Sensor, newSensor: Sensor) {
        sensors.remove(sensor)
        sensors.add(newSensor)
    }

    infix fun add(event: Event) {
        pendingEvents += event
    }

    fun fire() {
        for (event in pendingEvents) {
            when (event) {
                is ButtonPress -> when (val size = pendingEvents.size) {
                    1 -> {
                        get { it: ButtonsSensor? ->
                            val sensor = it ?: return@get

                            sensor[event.key]?.let { events ->
                                val handlers = events.filter { (state, command) ->
                                    state == event.state
                                }

                                for ((_, command) in handlers) {
                                    system(command)
                                }

                            }
                        }
                    }
                    else -> {
                        println("This is not an actual button")
                    }
                }
            }
        }
    }

    infix fun add(sensor: Sensor) {
        if (sensor in sensors) return
        sensors.add(sensor)
    }

    inline fun <reified T : Sensor, R> get(onRetrieve: (T?) -> R): R = onRetrieve(sensors.filterIsInstance<T>().firstOrNull())
    infix fun remove(sensor: Sensor) {
        if (sensor !in sensors) return
        sensors.remove(sensor)
    }

}

interface Sensor {

}

interface MotionSensors : Sensor {
    /*After what amount of change should the command fire*/
    var friction: Float
}

open class Sensor2D(
    var x: Int = 0,
    var y: Int = 0
) : MotionSensors {
    override var friction = 0f

    constructor(sensor: Sensor2D) : this(sensor.x, sensor.y)
    constructor(sensor: Sensor3D) : this(sensor.x, sensor.y)

}

// define north east west and south buttons
data class RadialSensor(
    /*Represents min key value*/
    val north: Int,
    val east: Int,
    val west: Int,
    val south: Int
)

open class Sensor3D(
    x: Int = 0,
    y: Int = 0,
    val z: Int = 0
) : Sensor2D(x, y) {
    constructor(sensor: Sensor2D) : this(sensor.x, sensor.y)
}

class Sensor2_5D(x: Int = 0, y: Int = 0, val pressure: PressureSensor = PressureSensor(0)) :
    Sensor2D(x, y) {
    constructor(sensor: Sensor2D) : this(
        sensor.x,
        sensor.y
    )
}

class Sensor4D(x: Int = 0, y: Int = 0, z: Int = 0, val pressure: PressureSensor = PressureSensor(0)) :
    Sensor3D(x, y, z) {
    constructor(sensor: Sensor3D, pressure: PressureSensor = PressureSensor(0)) : this(
        sensor.x,
        sensor.y,
        sensor.z,
        pressure
    )

    constructor(sensor: Sensor2D, pressure: PressureSensor = PressureSensor(0)) : this(
        sensor.x,
        sensor.y,
        0,
        pressure
    )

}

class ButtonsSensor : Sensor {

    private val keys by lazy {
        mutableMapOf<Int, MutableMap<ButtonPress.State, String>>()
    }

    fun add(key: Int) {
        keys[key] = mutableMapOf()
    }

    fun set(key: Int, command: Configs.Command, value: String) {
        keys[key]?.let { it[ButtonPress.State(command.identifier)] = value }
    }

    operator fun get(key: Int) = keys[key]

}

data class PressureSensor(var pressure: Int = 0, override var friction: Float = 0f) : MotionSensors