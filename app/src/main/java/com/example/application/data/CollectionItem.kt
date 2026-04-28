package com.example.application.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "items")
data class CollectionItem(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val purchaseDate: Long = 0,
    val name: String,
    val description: String,
    val purchasePrice: Double,
    val currentPrice: Double,
    val imageUri: String,
    val category: String = ""

)