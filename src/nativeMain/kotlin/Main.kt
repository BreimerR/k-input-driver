/**
 * Pass the device name as an argument
 * This will reduce the event loop
 * consuming too much cpu time
 * TODO for device addition and removal
 * https://www.tecmint.com/udev-for-device-detection-management-in-linux/
 **/
fun main(vararg args: String) {

    val commandsListenerListener = InputService()

    val file = "/opt/Projects/Kotlin/HuionHS610/master/src/nativeMain/resources/example.conf"
    configs.read(file)

    commandsListenerListener.run()

}