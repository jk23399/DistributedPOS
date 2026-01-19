package com.jun.simplepos.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverter
import androidx.room.TypeConverters
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

@Database(
    entities = [MenuItem::class, TableInfo::class, Order::class, OrderItem::class],
    version = 10,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {

    abstract fun menuItemDao(): MenuItemDao
    abstract fun tableInfoDao(): TableInfoDao
    abstract fun orderDao(): OrderDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "simple_pos_database"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}

class Converters {
    @TypeConverter
    fun fromModifierGroupList(value: List<ModifierGroup>?): String {
        return Gson().toJson(value)
    }

    @TypeConverter
    fun toModifierGroupList(value: String): List<ModifierGroup> {
        val listType = object : TypeToken<List<ModifierGroup>>() {}.type
        return Gson().fromJson(value, listType) ?: emptyList()
    }
}