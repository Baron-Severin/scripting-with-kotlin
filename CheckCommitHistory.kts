package kotlinscript

import java.nio.file.Paths
import java.util.concurrent.TimeUnit
import kotlin.system.exitProcess

val log = execute("git log --pretty=oneline")
val mostRecentMessage: String = log.split("\n")
        .first()
        .trimStart{ it != ' ' }
        .trim()

val githubKeywords = listOf(
        "close",
        "closes",
        "closed",
        "fix",
        "fixes",
        "fixed",
        "resolve",
        "resolves",
        "resolved",
        "issue"
)

val errorMessage = """
    |Most recent commit message is invalid
    |
    |Allowed formats:
    |   [GitHub keyword] #[issue number]:
    |   No issue:
    |
    |Common GitHub keywords: Closes, Fixes, Issue
    |
    |Actual commit message:
    |$mostRecentMessage
""".trimMargin()

val split = mostRecentMessage
        .toLowerCase()
        .split("\\s".toRegex())

if (split.size < 2) fail()

val first = split.first()
val second = split[1]
val firstTwo = "$first $second"

if (firstTwo == "no issue:") succeed()
if (!githubKeywords.contains(first)) fail()
val pattern = "#\\d+:".toRegex()
if (!pattern.matches(second)) fail()
succeed()


fun succeed() {
    exitProcess(0)
}
fun fail() {
    println(errorMessage)
    exitProcess(1)
}

// Taken from https://stackoverflow.com/questions/35421699/how-to-invoke-external-command-from-within-kotlin-code
fun execute(input: String): String {
    val currentDirectory = Paths.get("").toAbsolutePath().normalize().toFile()

    val parts = input.split("\\s".toRegex())
    val proc = ProcessBuilder(parts)
            .directory(currentDirectory)
            .redirectOutput(ProcessBuilder.Redirect.PIPE)
            .redirectError(ProcessBuilder.Redirect.PIPE)
            .start()
    proc.waitFor(10, TimeUnit.SECONDS)

    return proc.inputStream.bufferedReader().readText()
}
