package com.wakeMyPCs.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.jcraft.jsch.MAC
import kotlinx.coroutines.flow.Flow

@Dao
interface PcsDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(pc: Pc)

    @Update
    suspend fun update(pc: Pc)

    @Delete
    suspend fun delete(pc: Pc)

    @Query("SELECT * from pcs WHERE id = :id OR Name = :name OR MAC = :mac")
    fun getPc(id: Int = -1, name: String = "", mac: String = ""): Pc

    @Query("SELECT * from pcs ORDER BY name ASC")
    fun getAllPcs(): List<Pc>
}