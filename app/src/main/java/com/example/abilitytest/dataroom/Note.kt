package com.example.abilitytest.dataroom

import android.content.Context
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
import androidx.room.Update
import com.example.abilitytest.DBNAME

@Entity
data class Note(
    @PrimaryKey(autoGenerate = true) val id: Int?,
    @ColumnInfo(name = "content") var content: String,
    @ColumnInfo(name = "update") val update: String,
    @ColumnInfo(name = "username") val username: String,
)

@Dao
interface NoteDao {
    @Query("SELECT * FROM note where username = :username")
    fun getAll(username: String): List<Note>

    @Query("select * from note where id = :id")
    fun findById(id: Int): Note?

    @Insert
    fun insert(note: Note)

    @Update
    fun update(note: Note)

    @Delete
    fun delete(note: Note)
}

@Database(entities = [Note::class], version = 3)
abstract class NoteDatabase : RoomDatabase() {
    abstract fun dao(): NoteDao
}

class NoteService(
    context: Context
) : AutoCloseable {
    private val db = Room.databaseBuilder(
        context,
        NoteDatabase::class.java,
        DBNAME.NOTE
    ).allowMainThreadQueries().fallbackToDestructiveMigration().build()
    val dao = db.dao()

    override fun close() {
        db.close()
    }
}