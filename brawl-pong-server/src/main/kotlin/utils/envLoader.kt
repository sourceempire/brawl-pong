package io.sourceempire.brawlpong.utils

import java.io.BufferedReader
import java.io.File

var envLoaded = false

/**
 * Load values from env file (format key=value) into both system properties
 * and as a Map
 */
fun loadEnv() {
    val env = File(".env")
    if (!envLoaded && env.exists() && !env.isDirectory) {
        setEnvProperties(env)
        loadTestEnv()
        envLoaded = true
    }
}

private fun loadTestEnv() {
    if (getEnvProperty("IS_TEST").toBoolean()) {
        val testEnv = File(".env.test")
        setEnvProperties(testEnv)
    }
}

private fun setEnvProperties (envFile: File) {
    val variables = loadEnvFile(envFile)
    variables.forEach { (key, value) ->
        System.setProperty(key, value)
    }
}

private fun loadEnvFile(env: File): MutableMap<String, String> {
    val variables = HashMap<String, String>()
    val bufferedReader: BufferedReader = env.bufferedReader()

    bufferedReader.use {
        val inputString = it.readText()
        it.close()

        val lines = inputString.split("\n")
        // TODO replace with regex
        lines.filter { line ->
            if (line.contains("=")) {
                val trimmed = line.trimStart()
                !trimmed.startsWith("#")
            } else {
                false
            }
        }.map { line ->
            val splitOnComment = line.trim().split(Regex("#"), limit = 2)
            splitOnComment[0].split("=", limit = 2)
        }.forEach { variables[it[0]] = it[1] }
    }
    return variables
}

/**
 * Get  variables which is in .env.example but not .env, and vice versa
 */
fun getEnvFileDisparities(): List<Map<String, String>> {
    val availableVars = loadEnvFile(File(".env.example"))
    val availableVarsDuplicate = HashMap(availableVars)
    val actualVars = loadEnvFile(File(".env"))
    val optionalAvailableVars = HashMap<String, String>()

    for ((key, _) in actualVars) {
        availableVars.remove(key)
    }

    val iterator = availableVars.iterator()
    while (iterator.hasNext()) {
        val (key, value) = iterator.next()
        if (value.startsWith("<optional>")) {
            optionalAvailableVars[key] = value.removePrefix("<optional>").trimStart()
            iterator.remove()
        }
    }

    for ((key, _) in availableVarsDuplicate) {
        actualVars.remove(key)
    }

    return listOf(availableVars, optionalAvailableVars, actualVars)
}


/**
 * Get variable from either system property of environmental variable
 */
fun getEnvProperty(key: String): String =
    System.getProperty(key) ?: System.getenv(key) ?: ""

fun getEnvProperty(key: String, default: String): String =
    System.getProperty(key) ?: System.getenv(key) ?: default

fun getEnvProperty(key: String, default: Int): Int =
    when {
        System.getProperty(key) != null -> System.getProperty(key).toInt()
        System.getenv(key) != null -> System.getenv(key).toInt()
        else -> default
    }

fun getEnvProperty(key: String, default: Boolean): Boolean =
    when {
        System.getProperty(key) != null -> System.getProperty(key).toBoolean()
        System.getenv(key) != null -> System.getenv(key).toBoolean()
        else -> default
    }

fun getEnvPropertyList(key: String): List<String> =
    parseList(System.getProperty(key)) ?: parseList(System.getenv(key)) ?: listOf()

fun getEnvPropertySet(key: String): Set<String> =
    parseSet(System.getProperty(key)) ?: parseSet(System.getenv(key)) ?: setOf()

fun getEnvPropertySetMap(key: String): Map<String, String> =
    parseMap(System.getProperty(key)) ?: parseMap(System.getenv(key)) ?: mapOf()

private fun parseList(str: String?): List<String>? {
    if (str == null) return null

    return if (str.length > 2 && str.first() == '[' && str.first() == ']') {
        str.slice(IntRange(1, str.length-1)).split(",").map { it.trim() }
    } else {
        listOf()
    }
}

private fun parseSet(str: String?): Set<String>? {
    if (str == null) return null

    return if (str.length > 2 && str.first() == '{' && str.first() == '}') {
        str.slice(IntRange(1, str.length-1)).split(",").map { it.trim() }.toSet()
    } else {
        setOf()
    }
}

private fun parseMap(str: String?): Map<String, String>? {
    if (str == null) return null

    return if (str.length > 2 && str.first() == '{' && str.first() == '}') {
        str.slice(IntRange(1, str.length-1)).split(",").map {
            val k = it.split(":", limit = 1)
            k[0].trim() to k[1].trim()
        }.toMap()
    } else {
        mapOf()
    }
}
