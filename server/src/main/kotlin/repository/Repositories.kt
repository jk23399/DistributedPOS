package com.example.posserver.repository
import com.example.posserver.entity.*
import org.springframework.data.jpa.repository.JpaRepository

interface TableInfoRepository : JpaRepository<TableInfo, Int>

interface MenuItemRepository : JpaRepository<MenuItem, Int>

interface OrderRepository : JpaRepository<Order, Int>

interface OrderItemRepository : JpaRepository<OrderItem, Int>