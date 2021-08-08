package se.lindhen.qrgame.dialogs

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import se.lindhen.qrgame.GameActivity
import se.lindhen.qrgame.R
import java.lang.RuntimeException

class ErrorDialog constructor(private var message: String? = null, private var exception: RuntimeException?): QrGameDialog() {

    companion object {
        const val BUNDLE_MESSAGE = "error_message"
        const val BUNDLE_EXCEPTION = "exception"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        message = getFromStateOrFail(message, savedInstanceState?.getString(BUNDLE_MESSAGE), BUNDLE_MESSAGE)
        exception = getFromStateOrFail(exception, savedInstanceState?.getSerializable(BUNDLE_EXCEPTION), BUNDLE_EXCEPTION) as RuntimeException
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putString(BUNDLE_MESSAGE, message)
        outState.putSerializable(BUNDLE_EXCEPTION, exception)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val view = inflater.inflate(getLayoutId(), container)
        view.findViewById<Button>(getCancelButtonId()).setOnClickListener {
            (activity as GameActivity).onCloseGame()
            dismiss()
        }
        view.findViewById<Button>(R.id.show_stack_trace).setOnClickListener {
            (activity as GameActivity).onShowStackTrace(exception!!)
            dismiss()
        }
        view.findViewById<TextView>(R.id.error_dialog_message).text = message
        return view
    }

    override fun getCancelButtonId() = R.id.error_close_game

    override fun getLayoutId() = R.layout.dialog_error

}
