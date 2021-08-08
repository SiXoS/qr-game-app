package se.lindhen.qrgame

import android.graphics.Bitmap
import android.graphics.Color.BLACK
import android.graphics.Color.WHITE
import android.graphics.Point
import android.os.Build
import android.os.Bundle
import android.util.DisplayMetrics
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.zxing.common.BitMatrix
import se.lindhen.qrgame.qr.QrCreator

class ShowQrActivity : AppCompatActivity() {

    companion object {
        const val EXTRA_BYTECODE_PARAMETER = "bytecode"
        const val EXTRA_GAME_NAME_PARAMETER = "gamename"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_show_qr)

        val byteCode = intent.getByteArrayExtra(EXTRA_BYTECODE_PARAMETER)!!
        val gameName = intent.getStringExtra(EXTRA_GAME_NAME_PARAMETER)!!

        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        val bitMatrix = QrCreator.encode(byteCode, screenWidth())
        val bitMatrixToBitMap = bitMatrixToBitMap(bitMatrix)
        findViewById<ImageView>(R.id.qr_image).setImageBitmap(bitMatrixToBitMap)
        findViewById<TextView>(R.id.qr_title).text = gameName
    }

    private fun screenWidth(): Int {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            val size = Point()
            display?.getRealSize(size)
            size.x
        } else {
            val size = Point()
            windowManager.defaultDisplay.getRealSize(size)
            size.x
        }
    }

    private fun bitMatrixToBitMap(bitMatrix: BitMatrix): Bitmap {
        val width: Int = bitMatrix.width
        val height: Int = bitMatrix.height
        val pixels = IntArray(width * height)
        // All are 0, or black, by default
        // All are 0, or black, by default
        val bgColor = resources.getColor(R.color.activityBackground)
        for (y in 0 until height) {
            val offset = y * width
            for (x in 0 until width) {
                pixels[offset + x] = if (bitMatrix.get(x, y)) BLACK else bgColor
            }
        }

        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        bitmap.setPixels(pixels, 0, width, 0, 0, width, height)
        return bitmap
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

}