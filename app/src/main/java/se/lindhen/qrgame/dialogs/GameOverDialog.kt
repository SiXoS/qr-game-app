package se.lindhen.qrgame.dialogs

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import se.lindhen.qrgame.GameActivity
import se.lindhen.qrgame.R
import java.lang.StringBuilder

class GameOverDialog constructor(private var won: Boolean? = null, private var score: Int? = null, private var highScore: Int? = null): QrGameDialog() {

    companion object {
        const val BUNDLE_WON = "won"
        const val BUNDLE_SCORE = "score"
        const val BUNDLE_HIGH_SCORE = "high_score"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        won = getFromStateOrFail(won, savedInstanceState?.get(BUNDLE_WON) as Boolean?, BUNDLE_WON)
        score = score ?: savedInstanceState?.get(BUNDLE_SCORE) as Int?
        highScore = highScore ?: savedInstanceState?.get(BUNDLE_HIGH_SCORE) as Int?
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putBoolean(BUNDLE_WON, won!!)
        score?.let { outState.putInt(BUNDLE_SCORE, it) }
        highScore?.let { outState.putInt(BUNDLE_HIGH_SCORE, it) }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val view = inflater.inflate(getLayoutId(), container)
        view.findViewById<Button>(getCancelButtonId()).setOnClickListener {
            (activity as GameActivity).onCloseGame()
            dismiss()
        }
        view.findViewById<Button>(R.id.game_over_play_again).setOnClickListener {
            (activity as GameActivity).onPlayAgain()
            dismiss()
        }
        view.findViewById<TextView>(R.id.game_over_message).text = getMessage()
        return view
    }

    override fun getCancelButtonId() = R.id.game_over_close_game

    override fun getLayoutId() = R.layout.dialog_game_over

    private fun getMessage(): String {
        val messageBuilder = StringBuilder()
        messageBuilder.appendln(if (won!!) getString(R.string.you_won_exclamation) else getString(R.string.you_lost))
        if (score != null && highScore != null) {
            if (score!! > highScore!!) {
                messageBuilder.append(getString(R.string.new_high_score)).append(' ').append(score!!).appendln()
            } else {
                messageBuilder
                    .append(getString(R.string.score)).append(": ").append(score!!).appendln()
                    .append(getString(R.string.high_score)).append(": ").append(highScore!!).appendln()
            }
        }
        return messageBuilder.toString()
    }
}
