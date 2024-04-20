package com.wakeMyPCs.data

interface PcsRepository {
    fun getAllPcs(): List<Pc>
    fun getPc(id: Int = -1, name: String = "", mac: String = ""): Pc?
    suspend fun insertPc(pc: Pc)
    suspend fun deletePc(pc: Pc)
    suspend fun updatePc(pc: Pc)
}