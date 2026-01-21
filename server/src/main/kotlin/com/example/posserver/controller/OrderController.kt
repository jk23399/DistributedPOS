package com.example.posserver.controller

import com.example.posserver.entity.Order
import com.example.posserver.entity.OrderItem
import com.example.posserver.repository.OrderRepository
import com.example.posserver.repository.OrderItemRepository
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/orders")
class OrderController(
    private val orderRepo: OrderRepository,
    private val orderItemRepo: OrderItemRepository
) {
    @PostMapping
    fun createOrder(@RequestBody order: Order): Order {
        val newOrder = order.copy(id = null)
        return orderRepo.save(newOrder)
    }

    @GetMapping
    fun getAllOrders(): List<Order> = orderRepo.findAll()

    @PostMapping("/{orderId}/items")
    fun addOrderItems(@PathVariable orderId: Int, @RequestBody items: List<OrderItem>): List<OrderItem> {
        val itemsWithId = items.map { it.copy(orderId = orderId) }
        return orderItemRepo.saveAll(itemsWithId)
    }

    @GetMapping("/{orderId}/items")
    fun getOrderItems(@PathVariable orderId: Int): List<OrderItem> {
        return orderItemRepo.findAll().filter { it.orderId == orderId }
    }

    @PostMapping("/{id}/complete")
    fun completeOrder(@PathVariable id: Int): Order {
        val order = orderRepo.findById(id).orElseThrow()
        order.status = "PAID"
        return orderRepo.save(order)
    }
}