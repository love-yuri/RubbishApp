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
data class User(
    @PrimaryKey val username: String,
    @ColumnInfo(name = "password") val password: String,
    @ColumnInfo(name = "avatar") val avatar: String,
)

@Dao
interface UserDao {
    @Query("SELECT * FROM user")
    fun getAll(): List<User>

    @Query("select * from user where username = :username")
    fun findByUserName(username: String): User?

    @Insert
    fun insert(user: User)

    @Update
    fun update(user: User)

    @Delete
    fun delete(user: User)
}

@Database(entities = [User::class], version = 2)
abstract class UserDatabase : RoomDatabase() {
    abstract fun dao(): UserDao
}

class UserService(
    context: Context
) : AutoCloseable {
    private val db = Room.databaseBuilder(
        context,
        UserDatabase::class.java,
        DBNAME.USER
    ).allowMainThreadQueries().fallbackToDestructiveMigration().build()
    val dao = db.dao()


    override fun close() {
        db.close()
    }
}

object CurrentUser {
    lateinit var username: String
    lateinit var password: String
    lateinit var avatar: String
}