package com.jun.simplepos.ui.receipt

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.OutputStream
import java.net.Socket
import java.nio.charset.Charset

object PrinterManager {

    private const val TIMEOUT = 5000
    private const val PRINTER_WIDTH = 42

    suspend fun print(context: Context, receiptText: String): Result<Unit> = withContext(Dispatchers.IO) {
        return@withContext try {
            val prefs = context.getSharedPreferences("printer_settings", Context.MODE_PRIVATE)
            val ipAddress = prefs.getString("ip_address", "") ?: ""
            val port = prefs.getString("port", "9100")?.toIntOrNull() ?: 9100

            if (ipAddress.isBlank()) {
                return@withContext Result.failure(Exception("Printer IP not configured"))
            }

            val socket = Socket()
            socket.connect(java.net.InetSocketAddress(ipAddress, port), TIMEOUT)

            val outputStream: OutputStream = socket.getOutputStream()

            val commands = buildEscPosCommands(receiptText)
            outputStream.write(commands)
            outputStream.flush()

            outputStream.close()
            socket.close()

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun formatLine(leftText: String, rightText: String): String {
        val leftLen = leftText.toByteArray(Charset.forName("EUC-KR")).size
        val rightLen = rightText.length
        val spaceLen = PRINTER_WIDTH - leftLen - rightLen

        return if (spaceLen > 0) {
            leftText + " ".repeat(spaceLen) + rightText
        } else {
            leftText + "\n" + " ".repeat(PRINTER_WIDTH - rightLen) + rightText
        }
    }

    private fun buildEscPosCommands(text: String): ByteArray {
        val commands = mutableListOf<Byte>()

        commands.addAll(ESC_POS.INIT.toList())
        commands.addAll(listOf(0x1D, 0x21, 0x00))

        val lines = text.split("\n")
        lines.forEach { line ->
            val isHeader = line.contains("Shu Sushi") || line.contains("Table:")
            val isTotal = line.startsWith("TOTAL") || line.startsWith("Subtotal")
            var textToPrint = line

            if (isHeader) {
                commands.addAll(ESC_POS.ALIGN_CENTER.toList())
                commands.addAll(ESC_POS.BOLD_ON.toList())
                commands.addAll(listOf(0x1D, 0x21, 0x11))
                textToPrint = line.trim()

            } else if (isTotal) {
                commands.addAll(ESC_POS.ALIGN_LEFT.toList())
                commands.addAll(ESC_POS.BOLD_ON.toList())
                commands.addAll(listOf(0x1D, 0x21, 0x00))
            } else {
                commands.addAll(ESC_POS.ALIGN_LEFT.toList())
                commands.addAll(ESC_POS.BOLD_OFF.toList())
                commands.addAll(listOf(0x1D, 0x21, 0x00))
            }

            commands.addAll(textToPrint.toByteArray(Charset.forName("EUC-KR")).toList())
            commands.addAll(ESC_POS.FEED_LINE.toList())
        }

        commands.addAll(listOf(0x0A, 0x0A, 0x0A))
        commands.addAll(ESC_POS.CUT_PAPER.toList())

        return commands.toByteArray()
    }

    object ESC_POS {
        val INIT = byteArrayOf(0x1B, 0x40)
        val ALIGN_LEFT = byteArrayOf(0x1B, 0x61, 0x00)
        val ALIGN_CENTER = byteArrayOf(0x1B, 0x61, 0x01)
        val ALIGN_RIGHT = byteArrayOf(0x1B, 0x61, 0x02)
        val BOLD_ON = byteArrayOf(0x1B, 0x45, 0x01)
        val BOLD_OFF = byteArrayOf(0x1B, 0x45, 0x00)
        val FEED_LINE = byteArrayOf(0x0A)
        val CUT_PAPER = byteArrayOf(0x1D, 0x56, 0x00)
    }
}