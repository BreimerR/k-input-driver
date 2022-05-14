import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.newSingleThreadContext
import kotlinx.coroutines.runBlocking
import kotlin.native.concurrent.isFrozen

class InputService {

    fun run() {

        val device: Device = devices.getOrNull(0) ?: return println("No devices found")

        var i = 1

        /* while (i < devices.size) {
             val otherDevice = devices[i++]

             val path = otherDevice.path ?: continue // skipping events with no path details

             @OptIn(ExperimentalCoroutinesApi::class)
             runBlocking(newSingleThreadContext("Worker: $path")) {
                 otherDevice.get { it: ButtonsSensor? ->
                     val sensor = it ?: throw RuntimeException("Missing button sensor")
                     println()
                 }
                 EventsService(otherDevice.copy()).listen()
             }

         }*/

        EventsService(device).listen()

    }

}