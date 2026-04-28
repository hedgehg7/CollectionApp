package com.example.application.data

import androidx.room.*
import com.example.application.data.CollectionItem

@Dao
interface ItemDao {

    @Insert
    suspend fun insert(item: CollectionItem)

    @Delete
    suspend fun delete(item: CollectionItem)

    @Query("SELECT * FROM items ORDER BY id DESC")
    suspend fun getAll(): List<CollectionItem>

    @Query("SELECT SUM(currentPrice) FROM items")
    suspend fun getTotalValue(): Double?

    @Update
    suspend fun update(item: CollectionItem)
}