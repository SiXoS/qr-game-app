package se.lindhen.qrgame.db

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.*

@Entity
data class Game (
    @PrimaryKey(autoGenerate = true) val id: Int,
    @ColumnInfo(index = true) val hash: Int,
    @ColumnInfo(typeAffinity = ColumnInfo.BLOB) val code: ByteArray,
    @ColumnInfo var name: String?,
    @ColumnInfo var highScore: Int,
    @ColumnInfo val scanned: Date
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Game

        if (id != other.id) return false
        if (hash != other.hash) return false
        if (!code.contentEquals(other.code)) return false
        if (scanned != other.scanned) return false

        return true
    }

    override fun hashCode(): Int {
        var result = id
        result = 31 * result + hash
        result = 31 * result + code.contentHashCode()
        result = 31 * result + scanned.hashCode()
        return result
    }
}