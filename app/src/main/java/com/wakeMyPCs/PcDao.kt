import androidx.room.Dao
import androidx.room.Query
import com.wakeMyPCs.Email

@Dao
interface PcDao {
    @Query("SELECT * FROM email")
    suspend fun getAllPcs(): List<Email>
}
