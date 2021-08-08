package se.lindhen.qrgame.db

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

@Database(entities = [Game::class], version = 2)
@TypeConverters(Converters::class)
abstract class QrGameDatabase: RoomDatabase() {
    abstract fun gameDao(): GameDao
}
