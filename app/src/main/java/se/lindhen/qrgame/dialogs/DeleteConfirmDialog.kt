package se.lindhen.qrgame.dialogs

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import se.lindhen.qrgame.GameHistoryActivity
import se.lindhen.qrgame.R

class DeleteConfirmDialog constructor(private var gameName: String? = null, private var position: Int? = null): QrGameDialog() {

    companion object {
        const val BUNDLE_GAME_NAME = "game_name"
        const val BUNDLE_POSITION = "position"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        gameName = getFromStateOrFail(gameName, savedInstanceState?.getString(BUNDLE_GAME_NAME), BUNDLE_GAME_NAME)
        position = getFromStateOrFail(position, savedInstanceState?.getInt(BUNDLE_POSITION), BUNDLE_POSITION)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putString(BUNDLE_GAME_NAME, gameName!!)
        outState.putInt(BUNDLE_POSITION, position!!)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val view = super.onCreateView(inflater, container, savedInstanceState)
        view.findViewById<Button>(R.id.delete_dialog_button).setOnClickListener {
            (activity as GameHistoryActivity).onDelete(position!!)
            dismiss()
        }
        view.findViewById<TextView>(R.id.delete_dialog_message).text = getString(R.string.confirm_delete, gameName!!)
        return view
    }

    override fun getCancelButtonId() = R.id.delete_dialog_cancel

    override fun getLayoutId() = R.layout.dialog_delete_confirm

}
