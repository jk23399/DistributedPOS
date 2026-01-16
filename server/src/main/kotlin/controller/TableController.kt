package com.example.posserver.controller

import com.example.posserver.entity.TableInfo
import com.example.posserver.repository.TableInfoRepository
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/tables")
class TableController(
    private val repository: TableInfoRepository
) {
    @GetMapping
    fun getAllTables(): List<TableInfo> = repository.findAll()

    @PostMapping
    fun saveTable(@RequestBody table: TableInfo): TableInfo {
        return repository.save(table)
    }

    @DeleteMapping("/{id}")
    fun deleteTable(@PathVariable id: Int) {
        repository.deleteById(id)
    }
}