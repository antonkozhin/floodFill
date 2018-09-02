package kozhin.floodfill.presenter

import android.graphics.Bitmap
import android.graphics.Color
import io.reactivex.Observable
import io.reactivex.functions.BiFunction
import kozhin.floodfill.R
import kozhin.floodfill.model.*
import kozhin.floodfill.view.MainView
import java.util.*
import java.util.concurrent.TimeUnit

class MainPresenterImpl: MainPresenter {

    companion object {
        private val MAX_WIDTH = 512
        private val MAX_HEIGHT = 512
    }

    private lateinit var view: MainView
    private lateinit var algorithm: Algorithm

    override var fillSpeed = 60

    override var width = 64
        /*set(value) {
            if (value < 0 || value > MAX_WIDTH) {
                view.showMessage(R.string.error_width, MAX_WIDTH)
            }
        }*/

    override var height = 64
        /*set(value) {
            if (value < 0 || value > MAX_HEIGHT) {
                view.showMessage(R.string.error_height, MAX_HEIGHT)
            }
        }*/

    override var bitmap1: Bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565)
        private set

    override var bitmap2: Bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565)
        private set

    override var algorithmType1: AlgorithmType = AlgorithmType.NON_RECURSIVE
        private set

    override var algorithmType2: AlgorithmType = AlgorithmType.NON_RECURSIVE
        private set

    override fun bindView(view: MainView) {
        this.view = view
    }

    override fun generateBitmap(): Bitmap {
        val random = Random()
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565)
        for (x in 0 until width) {
            for (y in 0 until height) {
                val color = if (random.nextBoolean()) Color.BLACK else Color.WHITE
                bitmap.setPixel(x, y, color)
            }
        }
        bitmap1 = Bitmap.createBitmap(bitmap)
        bitmap2 = Bitmap.createBitmap(bitmap)
        return bitmap
    }

    override fun selectFirstAlgorithm(position: Int) {
        algorithmType1 = AlgorithmType.fromInt(position) ?: AlgorithmType.NON_RECURSIVE
    }

    override fun selectSecondAlgorithm(position: Int) {
        algorithmType2 = AlgorithmType.fromInt(position) ?: AlgorithmType.NON_RECURSIVE
    }

    override fun startFloodFilling(outLocation: IntArray,
                                     maxImgX: Int,
                                     maxImgY: Int,
                                     bitmap: Bitmap,
                                     absX: Float,
                                     absY: Float) : Observable<AlgorithmResult> {
        view.lockUI()
        val imgX = absX - outLocation[0]
        val imgY = absY - outLocation[1]
        val maxX = bitmap.width
        val maxY = bitmap.height
        val x = (maxX * imgX / maxImgX.toFloat()).toInt()
        val y = (maxY * imgY / maxImgY.toFloat()).toInt()
        val color = bitmap.getPixel(x, y)
        val isBlack = color == Color.BLACK
        val replacementColor = if (isBlack) Color.WHITE else Color.BLACK
        val observable1 = startAlgorithm(bitmap1, Position.FIRST, x, y, color, replacementColor)
        val observable2 = startAlgorithm(bitmap2, Position.SECOND, x, y, color, replacementColor)
        return Observable.merge(observable1, observable2)
                .doOnComplete { view.unlockUI() }
                .doOnError { view.unlockUI() }
    }

    private fun startAlgorithm(bitmap: Bitmap, position: Position, x: Int, y: Int, color: Int, replacementColor: Int) : Observable<AlgorithmResult> {
        val algorithmType = if (position == Position.FIRST) algorithmType1 else algorithmType2
        algorithm = when (algorithmType) {
            AlgorithmType.NON_RECURSIVE -> NonRecursiveAlgorithm(bitmap)
            AlgorithmType.QUEUE -> QueueAlgorithm(bitmap)
            AlgorithmType.RECURSIVE -> RecursiveAlgorithm(bitmap)
        }
        //val intervalObservable = Observable.interval(fillSpeed.toLong() , TimeUnit.SECONDS)
        return algorithm.fill(x, y, color)
                //.zipWith(intervalObservable, BiFunction { result : Point, _ : Long -> result})
                .flatMap {
                    bitmap.setPixel(it.x, it.y, replacementColor)
                    Observable.just(AlgorithmResult(bitmap, position))

                }
    }

    override fun setSize(widthString: String, heightString: String) : Observable<Size> {
        return Observable.create {
            val width = widthString.toIntOrNull()
            val height = heightString.toIntOrNull()
            if (width == null || width < 0 || width > MAX_WIDTH) {
                view.showMessage(R.string.error_width, MAX_WIDTH)
            } else if (height == null || height < 0 || height > MAX_HEIGHT) {
                view.showMessage(R.string.error_height, MAX_HEIGHT)
            } else {
                this.width = width
                this.height = height
                it.onNext(Size(this.width, this.height))
            }
            it.onComplete()
        }
    }

}