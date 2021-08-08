package se.lindhen.qrgame

import android.content.Context
import android.view.View
import android.view.ViewTreeObserver

inline fun View.waitForLayout(crossinline yourAction: () -> Unit) {
    val vto = viewTreeObserver
    vto.addOnGlobalLayoutListener(object : ViewTreeObserver.OnGlobalLayoutListener {
        override fun onGlobalLayout() {
            when {
                vto.isAlive -> {
                    vto.removeOnGlobalLayoutListener(this)
                    yourAction()
                }
                else -> viewTreeObserver.removeOnGlobalLayoutListener(this)
            }
        }
    })
}

fun convertDpToPx(context: Context, dp: Float): Float {
    return dp * context.resources.displayMetrics.density
}
