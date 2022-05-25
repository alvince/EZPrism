package cn.alvince.droidprism.log

interface IPageName {
    val name: String
}

@Suppress("FunctionName") // Create IPageName via function
fun PageNameOf(name: String): IPageName =
    object : IPageName {
        override val name: String get() = name
    }
