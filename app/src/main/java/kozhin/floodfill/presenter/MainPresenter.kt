package kozhin.floodfill.presenter

import android.graphics.Bitmap
import io.reactivex.Observable
import kozhin.floodfill.model.AlgorithmResult
import kozhin.floodfill.model.AlgorithmType
import kozhin.floodfill.model.Size
import kozhin.floodfill.view.MainView

interface MainPresenter {

    val bitmap1: Bitmap

    val bitmap2: Bitmap

    val algorithmType1: AlgorithmType

    val algorithmType2: AlgorithmType

    var fillSpeed: Int

    var width: Int

    var height: Int

    fun bindView(view: MainView)

    fun generateBitmap(): Bitmap

    fun selectFirstAlgorithm(position: Int)

    fun selectSecondAlgorithm(position: Int)

    fun startFloodFilling(outLocation: IntArray,
                            maxImgX: Int,
                            maxImgY: Int,
                            bitmap: Bitmap,
                            absX: Float,
                            absY: Float) : Observable<AlgorithmResult>

    fun setSize(widthString: String, heightString: String) : Observable<Size>

}