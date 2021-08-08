package se.lindhen.qrgame.dialogs

import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.fragment.app.DialogFragment
import se.lindhen.qrgame.GameHistoryActivity
import se.lindhen.qrgame.R


class RenameDialog constructor(private var previousName: String? = null, private var position: Int? = null): QrGameDialog() {

    private lateinit var renameTextField: EditText

    companion object {
        const val BUNDLE_PREVIOUS_NAME = "previous_name"
        const val BUNDLE_POSITION = "position"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        previousName = getFromStateOrFail(previousName, savedInstanceState?.getString(BUNDLE_PREVIOUS_NAME), BUNDLE_PREVIOUS_NAME)
        position = getFromStateOrFail(position, savedInstanceState?.getInt(BUNDLE_POSITION), BUNDLE_POSITION)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putString(BUNDLE_PREVIOUS_NAME, previousName!!)
        outState.putInt(BUNDLE_POSITION, position!!)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val view = super.onCreateView(inflater, container, savedInstanceState)
        renameTextField = view.findViewById(R.id.rename_game_dialog_field)
        view.findViewById<TextView>(R.id.rename_game_dialog_text).text = getString(R.string.rename_game, previousName)
        view.findViewById<Button>(R.id.rename_game_button).setOnClickListener {
            (activity as GameHistoryActivity).onRename(position!!, renameTextField.text.toString())
            dismiss()
        }
        return view
    }

    override fun getCancelButtonId() = R.id.rename_game_cancel

    override fun getLayoutId() = R.layout.dialog_rename

}
