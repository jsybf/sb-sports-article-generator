internal fun readResourceFile(path: String): String = object {}::class.java.getResource(path)!!.readText()