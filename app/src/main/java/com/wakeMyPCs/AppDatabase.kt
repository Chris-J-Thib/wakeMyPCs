import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.wakeMyPCs.Pc
import com.wakeMyPCs.PcDao

@Database(entities = [Pc::class], version = 1)
abstract class AppDatabase: RoomDatabase() {
    abstract fun pcDao(): PcDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(
            context: Context
        ): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context,
                    AppDatabase::class.java,
                    "app_database"
                )
                    .createFromAsset("database/PcsDB.db")
                    .build()
                INSTANCE = instance

                instance
            }
        }
    }
}
