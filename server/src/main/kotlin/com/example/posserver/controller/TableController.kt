package com.example.posserver.controller

import com.example.posserver.entity.TableInfo
import com.example.posserver.repository.TableInfoRepository
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*
import org.springframework.web.server.ResponseStatusException

@RestController
@RequestMapping("/api/tables")
class TableController(
    private val repository: TableInfoRepository
) {
    @GetMapping
    fun getAllTables(): List<TableInfo> = repository.findAll()

    @GetMapping("/{id}")
    fun getTable(@PathVariable id: Int): TableInfo {
        return repository.findById(id).orElseThrow {
            ResponseStatusException(HttpStatus.NOT_FOUND, "Table not found")
        }
    }

    @PostMapping
    fun saveTable(@RequestBody table: TableInfo): TableInfo {
        val newTable = table.copy(id = null)
        return repository.save(newTable)
    }

    /**
     * Update existing table with optimistic locking.
     * If version mismatch occurs, returns 409 Conflict.
     * Client must fetch latest data and retry.
     */
    @PutMapping("/{id}")
    fun updateTable(@PathVariable id: Int, @RequestBody table: TableInfo): TableInfo {
        val existing = repository.findById(id).orElseThrow {
            ResponseStatusException(HttpStatus.NOT_FOUND, "Table not found")
        }

        if (existing.version != table.version) {
            throw ResponseStatusException(
                HttpStatus.CONFLICT,
                "Version mismatch. Another device modified this table."
            )
        }

        return repository.save(table.copy(id = id))
    }

    @DeleteMapping("/{id}")
    fun deleteTable(@PathVariable id: Int) {
        repository.deleteById(id)
    }

}