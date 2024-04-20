package com.wakeMyPCs

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query

@Dao
interface PcDao {
    @Query("SELECT * FROM pcs")
    fun getAllPcs(): List<Pc>
}
