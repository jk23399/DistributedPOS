package com.jun.simplepos.ui.receipt

data class ReceiptData(
    val orderId: Int,
    val restaurantName: String,
    val restaurantAddress: String,
    val tableNumber: String,
    val dateTime: String,
    val items: List<ReceiptItem>,
    val subtotal: Double,
    val discount: Double,
    val discountRate: Double,
    val tax: Double,
    val taxRate: Double,
    val gratuity: Double,
    val gratuityRate: Double,
    val total: Double
)

data class ReceiptItem(
    val name: String,
    val quantity: Int,
    val unitPrice: Double,
    val totalPrice: Double
)

object ReceiptFormatter {
    fun formatCustomerReceipt(data: ReceiptData): String {
        return buildString {
            appendLine(center(data.restaurantName, 40))
            appendLine(center(data.restaurantAddress, 40))
            appendLine("=".repeat(40))
            appendLine("Date: ${data.dateTime}")
            appendLine("Table: ${data.tableNumber}")
            appendLine("Order #${data.orderId}")
            appendLine("=".repeat(40))

            data.items.forEach { item ->
                appendLine("${item.quantity}x ${item.name}")
                appendLine("    ${formatPrice(item.unitPrice)} x ${item.quantity} = ${formatPrice(item.totalPrice)}")
            }

            appendLine("-".repeat(40))
            appendLine("Subtotal:".padEnd(30) + formatPrice(data.subtotal))

            if (data.discountRate > 0) {
                appendLine("Discount (${formatPercent(data.discountRate)}):".padEnd(30) + "-${formatPrice(data.discount)}")
            }

            appendLine("Tax (${formatPercent(data.taxRate)}):".padEnd(30) + formatPrice(data.tax))

            if (data.gratuityRate > 0) {
                appendLine("Gratuity (${formatPercent(data.gratuityRate)}):".padEnd(30) + formatPrice(data.gratuity))
            }

            appendLine("=".repeat(40))
            appendLine("TOTAL:".padEnd(30) + formatPrice(data.total))
            appendLine("=".repeat(40))
            appendLine()
            appendLine(center("Thank you for your visit!", 40))
        }
    }

    fun formatKitchenReceipt(data: ReceiptData): String {
        return buildString {
            appendLine(center("*** KITCHEN ORDER ***", 40))
            appendLine("=".repeat(40))
            appendLine("Time: ${data.dateTime}")
            appendLine("Table: ${data.tableNumber}")
            appendLine("Order #${data.orderId}")
            appendLine("=".repeat(40))
            appendLine()

            data.items.forEach { item ->
                appendLine("${item.quantity}x".padEnd(5) + item.name)
                appendLine()
            }

            appendLine("=".repeat(40))
        }
    }

    fun center(text: String, width: Int): String {
        if (text.length >= width) return text
        val padding = (width - text.length) / 2
        return " ".repeat(padding.coerceAtLeast(0)) + text
    }

    private fun formatPrice(amount: Double): String {
        return "$%.2f".format(amount)
    }

    private fun formatPercent(rate: Double): String {
        return "%.1f%%".format(rate * 100)
    }
}