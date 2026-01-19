package com.jun.simplepos

import android.app.Application
import com.jun.simplepos.data.AppDatabase
import com.jun.simplepos.data.MenuItemRepository
import com.jun.simplepos.ui.pos.TaxState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class PosApplication : Application() {

    val database by lazy { AppDatabase.getDatabase(this) }
    val repository by lazy { MenuItemRepository(database.menuItemDao(), database.tableInfoDao()) }

    private val _taxStates = MutableStateFlow(
        listOf(
            TaxState("NY", 0.08875),
            TaxState("CA", 0.0725),
            TaxState("TX", 0.0625),
            TaxState("None", 0.0)
        )
    )
    val taxStates = _taxStates.asStateFlow()

    private val _gratuityOptions = MutableStateFlow(listOf(0.0, 0.15, 0.18, 0.20))
    val gratuityOptions = _gratuityOptions.asStateFlow()

    private val _discountOptions = MutableStateFlow(listOf(0.0, 0.05, 0.10, 0.15, 0.20))
    val discountOptions = _discountOptions.asStateFlow()

    val selectedTaxState = MutableStateFlow(TaxState("AZ", 0.086))
    val selectedGratuityRate = MutableStateFlow(gratuityOptions.value.first())
    val selectedDiscountRate = MutableStateFlow(discountOptions.value.first())

    fun addCustomTaxState(name: String, rate: Double) {
        val newTaxState = TaxState(name, rate / 100)
        _taxStates.value = (_taxStates.value + newTaxState).distinct()
        selectedTaxState.value = newTaxState
    }

    fun addCustomGratuity(rate: Double) {
        val newRate = rate / 100
        _gratuityOptions.value = (_gratuityOptions.value + newRate).distinct().sorted()
        selectedGratuityRate.value = newRate
    }

    fun addCustomDiscount(rate: Double) {
        val newRate = rate / 100
        _discountOptions.value = (_discountOptions.value + newRate).distinct().sorted()
        selectedDiscountRate.value = newRate
    }
}