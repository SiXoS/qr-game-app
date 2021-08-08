package se.lindhen.qrgame.db

import androidx.room.*

@Dao
interface GameDao {

    @Query("SELECT * FROM game")
    fun getAll(): List<Game>

    @Query("SELECT * FROM game WHERE hash = :hash LIMIT 1")
    fun getByHashCode(hash: Int): Game?

    @Update
    fun update(game: Game)

    @Insert
    fun insert(game: Game)

    @Delete
    fun delete(game: Game)

}