package com.jun.simplepos.data

import androidx.room.Entity
import androidx.room.PrimaryKey

data class ModifierOption(
    val name: String,
    val price: Double,
    val isSelected: Boolean = false
)

data class ModifierGroup(
    val title: String,
    val isRequired: Boolean,
    val maxSelection: Int,
    val options: List<ModifierOption>
)

@Entity(tableName = "menu_items")
data class MenuItem(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val name: String,
    val price: Double,
    val category: String,
    val description: String? = null,
    val unit: String? = null,
    val imageUrl: String? = null,
    val station: String = "Kitchen",
    val modifierGroups: List<ModifierGroup> = emptyList()
)
