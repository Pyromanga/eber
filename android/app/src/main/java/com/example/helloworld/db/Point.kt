package com.example.helloworld.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class Point(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val x: Float,
    val y: Float
)
