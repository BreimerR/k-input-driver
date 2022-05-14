import interop.io.execute
import interop.io.exit
import interop.io.readChar
import kotlinx.cinterop.ByteVarOf
import kotlinx.cinterop.CPointer
import kotlinx.cinterop.staticCFunction
import kotlinx.cinterop.toKString


class Configs {
    enum class Command(val identifier: String) {
        MARKER("marker"),
        LISTEN("listen"),
        IDENTIFIER("identifier"),
        BUTTON_UP("button_up"),
        BUTTON("button"),
        KEY_UP("key_up"),
        BUTTON_DOWN("button_down"),
        KEY_DOWN("key_down"),
        COMMAND("command");

        companion object {
            operator fun invoke(command: String) = when (command) {
                MARKER.identifier -> MARKER
                LISTEN.identifier -> LISTEN
                IDENTIFIER.identifier -> IDENTIFIER
                BUTTON_UP.identifier -> BUTTON_UP
                BUTTON.identifier -> BUTTON
                KEY_UP.identifier -> KEY_UP
                BUTTON_DOWN.identifier -> BUTTON_DOWN
                KEY_DOWN.identifier -> KEY_DOWN
                COMMAND.identifier -> COMMAND
                else -> null
            }
        }
    }

    private var configPath = ""
    private val configCommands by lazy {
        mutableListOf(
            "marker",
            "listen",
            "identifier",
            "button_up",
            "button",
            "key_up",
            "button_down",
            "key_down",
            "command"
        )
    }

    var line = ""
    var row = 0
    var onComment = false
    var prevLine = ""
    var currentDevice: Device? = null

    var lineParser: ((line: String, row: Int) -> Unit)? = null

    operator fun invoke() {
        lineParser?.invoke(line, row)
        prevLine = line
        line = ""
        lineParser = null
    }

    fun parseMarker(line: String, row: Int) {
        currentDevice?.let {
            currentDevice = null
        }

        try {
            val (_, identifier) = line.trim(':').split(":").map { it.trim() }

            Device(identifier).also { device -> // THIS IS libinput specific
                val result = execute(
                    """libinput list-devices | grep -A 1 $identifier""",
                    staticCFunction { error ->
                        println(error?.toKString() ?: "Undefined exception")
                    })

                val (_, kernel) = result?.toKString()?.split("\n") ?: return
                val (name, path) = kernel.split(":").map { it.trim(' ') }
                currentDevice = device.also {
                    it.path = path
                    devices += it
                }
            }
        } catch (e: Exception) {
            exit(
                """Invalid configuration marker line Row:$row: $line. 
                   |Expecting marker:<device identifier name>
                   |Use evtest 
                   |libinput list-devices to get device name""".trimMargin()
            )
        }

    }

    private fun parseCommand(line: String, row: Int) {
        val device = currentDevice ?: return

        val (command, key) = prevLine.split(":").map { it.trim() }
        val (_, shellCommand) = line.split(":").map { it.trim() }

        when (command) {
            Command(command)?.identifier -> device.get { it: ButtonsSensor? ->
                val sensor = it ?: throw RuntimeException(
                    """No button sensor defined.
                    | button: <Int> 
                    | Can only be used after defining listen: <buttons|keys>
                """.trimMargin()
                )

                sensor.set(
                    key.toIntOrNull() ?: throw RuntimeException(
                        """button: <key>: Key should be an integer
                    | in ${configs.prevLine}
                """.trimMargin()
                    ),
                    command = Command(command)!!,
                    shellCommand
                )

            }
            else -> {
                println("TODO: Event not yet configured for please contribute to project by funding | code base")
            }
        }
    }

    fun parseButton(line: String, row: Int) {
        val device = currentDevice ?: return

        device.get { it: ButtonsSensor? ->
            val sensor = it ?: throw RuntimeException("Can't parse buttons when listen: buttons isn't used")
            val (_, key) = line.split(":").map { it.trim(' ') }
            sensor.add(key.toIntOrNull() ?: throw RuntimeException("Use integer values for buttons"))
        }

    }

    private fun parseEvent(line: String, row: Int) {
        val device = currentDevice ?: throw RuntimeException(
            """Define a device first with identifier: <RegularExpression|Full Device Name>"""
        )
        try {
            val (_, listenFor) = line.split(":").map { it.trim() }
            when (listenFor) {
                "radial" -> {
                    device.listener = object : MotionEventListener {
                        override var x: Int = 0
                        override var y: Int = 0
                        override var friction = 1f // no friction at all

                        override fun onXUpdate(x: Int) {

                        }
                    }
                }
                "buttons" -> {
                    device.add(ButtonsSensor())
                }
                "abs" -> {

                }
                else -> {
                    val sensors = listenFor.split(",").map { it.trim(' ') }

                    for (sensorIdentifier in sensors) {
                        when (sensorIdentifier) {
                            "x" -> {
                                device.get<Sensor2D, Sensor?> {
                                    it
                                } ?: (device.get<PressureSensor, Sensor2_5D?> {
                                    Sensor2_5D(
                                        pressure = it ?: PressureSensor()
                                    )
                                } ?: Sensor2D()).also { newSensor ->
                                    device.add(newSensor)
                                }
                            }
                            "y" -> {
                                device.get<Sensor2D, Sensor?> {
                                    it
                                } ?: (device.get<PressureSensor, Sensor2_5D?> {
                                    it?.let {
                                        device.remove(it)
                                        Sensor2_5D(
                                            pressure = it
                                        )
                                    }
                                } ?: Sensor2D()).also { newSensor ->
                                    device.add(newSensor)
                                }
                            }
                            "z" -> {
                                device.get<Sensor3D, Sensor?> {
                                    it
                                } ?: (device.get<Sensor2D, Sensor?> {
                                    it?.let {
                                        device.remove(it)
                                        Sensor3D(
                                            it
                                        )
                                    }
                                } ?: device.get<PressureSensor, Sensor4D?> {
                                    it?.let {
                                        device remove it
                                        Sensor4D(
                                            pressure = it
                                        )
                                    }
                                } ?: Sensor3D()).also { newSensor ->
                                    device add newSensor
                                }

                            }
                            "pressure" -> {
                                device.get<PressureSensor, PressureSensor?> {
                                    it
                                } ?: (device.get<Sensor3D, Sensor4D?> {
                                    it?.let {
                                        device remove it
                                        Sensor4D(
                                            it
                                        )
                                    }
                                } ?: device.get<Sensor2D, Sensor2_5D?> {
                                    it?.let {
                                        device remove it
                                        Sensor2_5D(it)
                                    }
                                } ?: PressureSensor()).also {
                                    device add it
                                }
                            }
                        }
                    }
                }
            }

        } catch (e: Exception) {
            println(
                """|
                   |Failed to parse listener for $line Row$row
                """.trimMargin()
            )
        }

    }

    /**
     * Being a config file don't enforce strict rules
     * unless it's to deal with wrongly setup devices
     **/
    fun read(from: String = configPath) {
        configPath = from
        readChar(configPath, staticCFunction<Byte, CPointer<ByteVarOf<Byte>>?, Unit> { char, error ->
            if (error != null) configs.exit(error.toKString())

            when (val character = char.toInt().toChar()) {
                '\n' -> {
                    configs.row += 1
                    configs()
                    configs.onComment = false
                }
                '#' -> {
                    configs.onComment = true
                }
                else -> {
                    if (!configs.onComment) {
                        configs.line += character

                        val command = configs.line.trim()

                        if (command in configs.configCommands) when (command) {
                            "marker", "identifier" -> {
                                configs.lineParser = configs::parseMarker
                            }
                            "listen" -> {
                                configs.lineParser = configs::parseEvent
                            }
                            "button_up", "key_up", "button_down", "key_down", "button" -> {
                                configs.lineParser = configs::parseButton
                            }
                            "command" -> {
                                configs.lineParser = configs::parseCommand
                            }
                        }
                    }
                }
            }
        })

        invoke()

    }

    private fun exit(message: String) {
        println(message)
        exit(-1)
    }

}

@ThreadLocal
val configs = Configs()