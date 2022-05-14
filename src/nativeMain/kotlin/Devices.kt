@ThreadLocal
val devices by lazy {
    mutableListOf<Device>()
}