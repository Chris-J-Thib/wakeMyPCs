import androidx.room.ColumnInfo
import androidx.room.Dao
import androidx.room.Database
import androidx.room.Delete
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.Room
import androidx.room.RoomDatabase

// Step 1: Define the Entity Class
@Entity(tableName = "pcs")
data class Pc(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    @ColumnInfo(name = "name") val name: String,
    @ColumnInfo(name = "mac") val mac: String
)

// Step 2: Create the Data Access Object (DAO)
@Dao
interface PcDao {
    @Query("SELECT * FROM pcs")
    fun getAllPcs(): List<Pc>

    @Insert
    fun insertPc(pc: Pc)

    @Delete
    fun deletePc(pc: Pc)
}

// Step 3: Create the Database Class
@Database(entities = [Pc::class], version = 1)
abstract class AppDatabase : RoomDatabase() {
    abstract fun PcDao(): Any
}