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
import androidx.fragment.app.DialogFragment
import se.lindhen.qrgame.R

abstract class QrGameDialog: DialogFragment() {

    protected fun <T> getFromStateOrFail(
        valueFromConstructor: T?,
        valueFromBundle: T?,
        fieldName: String
    ): T {
        return if (valueFromConstructor == null) {
            if (valueFromBundle == null) {
                Log.e("RenameDialog", "$fieldName not specified and not found in bundle")
                throw IllegalStateException("Required value for field $fieldName missing")
            } else {
                valueFromBundle
            }
        } else {
            valueFromConstructor
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val view = inflater.inflate(getLayoutId(), container)
        view.findViewById<Button>(getCancelButtonId()).setOnClickListener {
            dismiss()
        }
        return view
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState)
        dialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.setCanceledOnTouchOutside(false)
        return dialog
    }

    override fun getTheme(): Int {
        return R.style.Theme_QrGame_Dialog
    }

    abstract fun getCancelButtonId(): Int

    abstract fun getLayoutId(): Int

}