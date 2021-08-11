package se.lindhen.qrgame

import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.room.Room
import se.lindhen.qrgame.db.Game
import se.lindhen.qrgame.db.GameDao
import se.lindhen.qrgame.db.QrGameDatabase
import se.lindhen.qrgame.dialogs.DeleteConfirmDialog
import se.lindhen.qrgame.dialogs.RenameDialog
import java.time.OffsetDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter


class GameHistoryActivity : AppCompatActivity() {

    private val dateFormat = DateTimeFormatter.ofPattern("d MMMM uuuu")
    private lateinit var games: MutableList<Game>
    private lateinit var gameDao: GameDao
    private lateinit var gameHistoryAdapter: GameHistoryAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_game_history)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        gameDao = getDb().gameDao()
        games = gameDao.getAll().toMutableList()

        val gameHistoryList: ListView = findViewById(R.id.game_history_list)
        gameHistoryAdapter = GameHistoryAdapter(games)
        gameHistoryList.adapter = gameHistoryAdapter
        gameHistoryList.onItemClickListener = AdapterView.OnItemClickListener { _, _, position, _ ->
            startGameActivity(games[position].code)
        }

    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                finish()
                return true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun getDb(): QrGameDatabase {
        return Room.databaseBuilder(applicationContext, QrGameDatabase::class.java, "qr-game")
            .allowMainThreadQueries()
            .build()
    }

    private fun startGameActivity(rawBytes: ByteArray) {
        val intent = Intent(this, GameActivity::class.java)
            .putExtra(GameActivity.EXTRA_BYTECODE_PARAMETER, rawBytes)
        startActivity(intent)
    }

    private fun rename(position: Int) {
        val game = games[position]
        val previousName = game.name ?: game.hash.toString(16)
        RenameDialog(previousName, position, game.id)
            .show(supportFragmentManager, "rename_dialog")
    }

    fun onRename(position: Int, id: Int, newName: String) {
        val game = games[position]
        if (game.id == id) {
            game.name = newName
            gameDao.update(game)
            gameHistoryAdapter.notifyDataSetChanged()
        }
    }

    private fun delete(position: Int) {
        val game = games[position]
        DeleteConfirmDialog(game.name ?: game.hash.toString(16), position, game.id)
            .show(supportFragmentManager, "delete_confirm_dialog")
    }

    fun onDelete(position: Int, id: Int) {
        val game = games[position]
        if (game.id == id) {
            gameDao.delete(game)
            games.removeAt(position)
            gameHistoryAdapter.notifyDataSetChanged()
        }
    }

    private fun showQr(position: Int, id: Int) {
        val game = games[position]
        if (game.id == id) {
            val intent = Intent(this, ShowQrActivity::class.java)
            intent.putExtra(ShowQrActivity.EXTRA_GAME_NAME_PARAMETER, game.name)
            intent.putExtra(ShowQrActivity.EXTRA_BYTECODE_PARAMETER, game.code)
            startActivity(intent)
        }
    }

    inner class GameHistoryAdapter(val games: List<Game>) : BaseAdapter() {

        override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
            val view = convertView ?: layoutInflater.inflate(R.layout.game_history_entry, parent, false)

            val game = games[position]
            view.findViewById<TextView>(R.id.game_history_name).text = game.name ?: game.hash.toString(16)
            view.findViewById<TextView>(R.id.game_history_date).text = dateFormat.format(OffsetDateTime.ofInstant(game.scanned.toInstant(), ZoneId.systemDefault()))
            view.findViewById<ImageButton>(R.id.game_history_delete).setOnClickListener { delete(position) }
            view.findViewById<ImageButton>(R.id.game_history_rename).setOnClickListener { rename(position) }
            view.findViewById<ImageButton>(R.id.game_history_qr).setOnClickListener { showQr(position, game.id) }
            return view
        }

        override fun getItem(position: Int): Any {
            return games[position]
        }

        override fun getItemId(position: Int): Long {
            return games[position].id.toLong()
        }

        override fun getCount(): Int {
            return games.size
        }

    }
}


