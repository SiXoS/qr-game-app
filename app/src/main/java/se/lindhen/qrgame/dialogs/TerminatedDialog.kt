package se.lindhen.qrgame.dialogs

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import se.lindhen.qrgame.GameActivity
import se.lindhen.qrgame.R

class TerminatedDialog constructor(): QrGameDialog() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val view = inflater.inflate(getLayoutId(), container)
        view.findViewById<Button>(getCancelButtonId()).setOnClickListener {
            (activity as GameActivity).onCloseGame()
            dismiss()
        }
        return view
    }

    override fun getCancelButtonId() = R.id.terminated_close_game

    override fun getLayoutId() = R.layout.dialog_game_canceled

}
