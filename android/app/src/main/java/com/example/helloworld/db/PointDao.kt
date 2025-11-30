package com.example.helloworld.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query

@Dao
interface PointDao {
    @Insert
    fun insert(point: Point)

    @Query("SELECT * FROM Point")
    fun getAll(): List<Point>

    @Query("DELETE FROM Point")
    fun clear()
}
