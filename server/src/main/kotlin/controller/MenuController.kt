package com.example.posserver.controller

import com.example.posserver.entity.MenuItem
import com.example.posserver.repository.MenuItemRepository
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/menus")
class MenuController(
    private val repository: MenuItemRepository
) {
    @GetMapping
    fun getAllMenus(): List<MenuItem> = repository.findAll()

    @PostMapping
    fun saveMenu(@RequestBody menu: MenuItem): MenuItem {
        return repository.save(menu)
    }

    @DeleteMapping("/{id}")
    fun deleteMenu(@PathVariable id: Int) {
        repository.deleteById(id)
    }
}