package se.lindhen.qrgame

import android.os.Bundle
import android.view.MenuItem
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import java.io.PrintWriter
import java.io.StringWriter

class StackTraceActivity : AppCompatActivity() {

    companion object {
        const val EXTRA_EXCEPTION = "exception"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_stack_trace)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        val serializableExtra = intent.getSerializableExtra(EXTRA_EXCEPTION) as RuntimeException
        findViewById<TextView>(R.id.stack_trace_text).text = getPrintedStackTrace(serializableExtra)
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

    private fun getPrintedStackTrace(exception: RuntimeException): String {
        val sw = StringWriter()
        val pw = PrintWriter(sw)
        exception.printStackTrace(pw)
        return sw.toString()
    }

}