package se.lindhen.qrgame

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.VibrationEffect
import android.os.Vibrator
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.view.View.GONE
import android.view.WindowInsets
import android.view.WindowInsetsController
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.room.Room
import se.lindhen.qrgame.bytecode.QgDecompiler
import se.lindhen.qrgame.db.Game
import se.lindhen.qrgame.db.QrGameDatabase
import se.lindhen.qrgame.dialogs.ErrorDialog
import se.lindhen.qrgame.dialogs.GameOverDialog
import se.lindhen.qrgame.program.InputManager
import se.lindhen.qrgame.program.Program
import java.lang.RuntimeException
import java.time.Instant
import java.util.*


class GameActivity : AppCompatActivity() {
    private lateinit var anyButtonView: TextView
    private lateinit var vibration: Vibrator
    private lateinit var highScoreTextField: TextView
    private lateinit var game: Game
    private lateinit var db: QrGameDatabase
    private lateinit var glSurface: GameGLSurfaceView
    private val maxSideButtonWidth = 170
    private lateinit var scoreTextField: TextView
    private lateinit var byteCode: ByteArray
    private lateinit var program: Program
    private var hasHitAButton = false

    companion object {
        const val EXTRA_BYTECODE_PARAMETER = "bytecode"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_game)
        vibration = getSystemService(Context.VIBRATOR_SERVICE) as Vibrator

        enableFullScreen()
        findViewById<ImageButton>(R.id.game_back).setOnClickListener { finish() }
        glSurface = findViewById(R.id.gl_surface)
        scoreTextField = findViewById(R.id.score_holder)
        highScoreTextField = findViewById(R.id.high_score_holder)
        anyButtonView = findViewById(R.id.game_any_button)
        if (decompileCode()) {
            db = getDb()
            game = createOrGetGameEntity(byteCode)
            connectButtons()
            highScoreTextField.text = game.highScore.toString()

            glSurface.setGameStateChangedListener(GameChangeListener())
            glSurface.startGame(program)
        }

    }

    private fun createOrGetGameEntity(byteCode: ByteArray): Game {
        val gameDao = db.gameDao()
        val hash = byteCode.contentHashCode()
        val game = gameDao.getByHashCode(hash)
        if (game != null)
            return game
        gameDao.insert(Game(0, hash, byteCode, null, 0, Date.from(Instant.now())))
        return gameDao.getByHashCode(hash)!!
    }

    private fun getDb(): QrGameDatabase {
        return Room.databaseBuilder(applicationContext, QrGameDatabase::class.java, "qr-game")
            .allowMainThreadQueries()
            .build()
    }

    private fun decompileCode(): Boolean {
        val byteCodeNullable = intent.getByteArrayExtra(EXTRA_BYTECODE_PARAMETER)
        if (byteCodeNullable == null) {
            Log.e("GameActivity", "No bytecode specified")
            finish()
        } else {
            byteCode = byteCodeNullable
            try {
                program = QgDecompiler(byteCode).decompile()
                return true
            } catch (e: RuntimeException) {
                showSevereErrorDialog("Failed to decompile game", e)
            }
        }
        return false
    }

    private fun showSevereErrorDialog(message: String, exception: RuntimeException) {
        ErrorDialog(message, exception)
            .show(supportFragmentManager, "failed_decompile")
    }

    fun onShowStackTrace(exception: RuntimeException) {
        val intent = Intent(this, StackTraceActivity::class.java)
            .putExtra(StackTraceActivity.EXTRA_EXCEPTION, exception)
        startActivity(intent)
        finish()
    }

    private fun enableFullScreen() {
        if (Build.VERSION.SDK_INT < 30) {
            window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_FULLSCREEN or
                    View.SYSTEM_UI_FLAG_IMMERSIVE or
                    View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION or
                    View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or
                    View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION or
                    View.SYSTEM_UI_FLAG_LAYOUT_STABLE
        } else {
            window.setDecorFitsSystemWindows(false)
            val insetsController = window.insetsController
            if (insetsController != null) {
                insetsController.hide(WindowInsets.Type.navigationBars() or WindowInsets.Type.statusBars() or WindowInsets.Type.displayCutout())
                insetsController.systemBarsBehavior = WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
            }
        }
    }

    private fun connectButtons() {
        connectButton(findViewById(R.id.left_top_button), InputManager.Input.LEFT_TOP)
        connectButton(findViewById(R.id.left_right_button), InputManager.Input.LEFT_RIGHT)
        connectButton(findViewById(R.id.left_bot_button), InputManager.Input.LEFT_BOTTOM)
        connectButton(findViewById(R.id.left_left_button), InputManager.Input.LEFT_LEFT)
        connectButton(findViewById(R.id.right_top_button), InputManager.Input.RIGHT_TOP)
        connectButton(findViewById(R.id.right_right_button), InputManager.Input.RIGHT_RIGHT)
        connectButton(findViewById(R.id.right_bot_button), InputManager.Input.RIGHT_BOTTOM)
        connectButton(findViewById(R.id.right_left_button), InputManager.Input.RIGHT_LEFT)
    }

    @SuppressLint("ClickableViewAccessibility", "NewApi")
    private fun connectButton(button: Button, input: InputManager.Input) {
        button.setOnTouchListener { v, event ->
            if (!hasHitAButton) {
                hasHitAButton = true
                anyButtonView.visibility = GONE
                glSurface.resume()
            }
            if (event.action == MotionEvent.ACTION_DOWN) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    vibration.vibrate(VibrationEffect.createPredefined(VibrationEffect.EFFECT_TICK))
                } else {
                    //deprecated in API 26
                    vibration.vibrate(10)
                }
            }
            when(event.action) {
                MotionEvent.ACTION_DOWN -> program.inputManager.triggerButton(input, true)
                MotionEvent.ACTION_UP -> program.inputManager.triggerButton(input, false)
            }
            v.performClick()
        }
    }

    override fun onStart() {
        super.onStart()
        adjustSideLayoutsToFillParent()
    }

    private fun adjustSideLayoutsToFillParent() {
        val leftButtons = findViewById<View>(R.id.left_buttons)
        val rightButtons = findViewById<View>(R.id.right_buttons)
        val rootLayout = findViewById<View>(R.id.root_layout)
        rootLayout.postDelayed({
            var correctWidth = (rootLayout.width - glSurface.width) / 2
            val maxWidthInPx = convertDpToPx(this, maxSideButtonWidth.toFloat())
            if (correctWidth > maxWidthInPx) {
                correctWidth = maxWidthInPx.toInt()
            }
            setLayoutWidth(leftButtons, correctWidth)
            setLayoutWidth(rightButtons, correctWidth)
            leftButtons.invalidate()
            rightButtons.invalidate()
        }, 100)
    }

    private fun setLayoutWidth(leftButtons: View, correctWidth: Int) {
        val layoutParams = leftButtons.layoutParams
        layoutParams.width = correctWidth
        leftButtons.layoutParams = layoutParams
    }

    private fun updateHighScore(score: Int) {
        if (score > game.highScore) {
            glSurface.pause()
            highScoreTextField.text = score.toString()
            game.highScore = score
            db.gameDao().update(game)
        }
    }

    private fun showGameOverDialog(won: Boolean, score: Int? = null, prevHighScore: Int? = null) {
        GameOverDialog(won, score, prevHighScore)
            .show(supportFragmentManager, "game_over_dialog")
    }

    fun onCloseGame() {
        finish()
    }

    fun onPlayAgain() {
        finish()
        startActivity(intent)
    }

    inner class GameChangeListener: GameStateChangeListener {

        private var lastScore = 0

        override fun won() {
            glSurface.pause()
            runOnUiThread {
                val prevHighScore = game.highScore
                updateHighScore(lastScore)
                showGameOverDialog(true, lastScore, prevHighScore)
            }
        }

        override fun lost() {
            glSurface.pause()
            runOnUiThread {
                if (program.trackScore) {
                    val prevHighScore = game.highScore
                    updateHighScore(lastScore)
                    showGameOverDialog(false, lastScore, prevHighScore)
                } else {
                    showGameOverDialog(false)
                }
            }
        }

        override fun scoreChange(score: Int) {
            lastScore = score
            runOnUiThread {
                scoreTextField.text = score.toString()
            }
        }

        override fun onError(exception: RuntimeException) {
            showSevereErrorDialog("Encountered exception when executing game", exception)
            glSurface.pause()
        }

    }
}