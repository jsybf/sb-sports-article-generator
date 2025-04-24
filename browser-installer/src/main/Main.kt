package io.gitp.browserinstaller

import com.microsoft.playwright.CLI

fun main() {
    CLI.main(arrayOf("install", "--with-deps", "chromium"))
}
