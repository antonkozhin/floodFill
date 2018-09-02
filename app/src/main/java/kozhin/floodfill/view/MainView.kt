package kozhin.floodfill.view

import android.graphics.Bitmap

interface MainView {

    fun showResult(bitmap: Bitmap)

    fun showFirstResult(bitmap: Bitmap)

    fun showSecondResult(bitmap: Bitmap)

    fun showMessage(resId: Int, vararg formatArgs: Any)

    fun showMessage(message: String)

    fun lockUI()

    fun unlockUI()

}