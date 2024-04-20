package com.wakeMyPCs.data

import kotlinx.coroutines.flow.Flow

class OfflinePcsRepository(private val pcsDao: PcsDao) : PcsRepository {
    override fun getAllPcs(): List<Pc> = pcsDao.getAllPcs()

    override fun getPc(id: Int, name: String, mac: String): Pc = pcsDao.getPc(id, name, mac)

    override suspend fun insertPc(pc: Pc) = pcsDao.insert(pc)

    override suspend fun deletePc(pc: Pc) = pcsDao.delete(pc)

    override suspend fun updatePc(pc: Pc) = pcsDao.update(pc)
}