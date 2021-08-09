package se.lindhen.qrgame.dialogs

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.annotation.StringRes
import se.lindhen.qrgame.GameActivity
import se.lindhen.qrgame.R

class TerminatedDialog constructor(@StringRes private var reasonStringResource: Int? = null): QrGameDialog() {

    companion object {
        private const val BUNDLE_REASON = "reason"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        reasonStringResource = getFromStateOrFail(reasonStringResource, savedInstanceState?.get(BUNDLE_REASON) as Int?, BUNDLE_REASON)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putInt(BUNDLE_REASON, reasonStringResource!!)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val view = inflater.inflate(getLayoutId(), container)
        view.findViewById<Button>(getCancelButtonId()).setOnClickListener {
            (activity as GameActivity).onCloseGame()
            dismiss()
        }
        view.findViewById<TextView>(R.id.terminated_message).text = getString(reasonStringResource!!)
        return view
    }

    override fun getCancelButtonId() = R.id.terminated_close_game

    override fun getLayoutId() = R.layout.dialog_game_canceled

}
