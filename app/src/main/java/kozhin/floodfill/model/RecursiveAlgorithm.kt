package kozhin.floodfill.model

import android.graphics.Bitmap
import android.graphics.Point
import io.reactivex.Observable

/**
 * Bad
 */
data class RecursiveAlgorithm(val image: Bitmap) : BaseAlgorithm(image) {

    override fun fill(startX: Int, startY: Int, targetColor: Int) : Observable<Point> {
        return step(startX, startY, targetColor)
                .flatMap {
                    val up = maxMin(startY + 1, 0, height - 1)
                    val down = maxMin(startY - 1, 0, height - 1)
                    val left = maxMin(startX - 1, 0, width - 1)
                    val right = maxMin(startX + 1, 0, width - 1)
                    Observable.mergeArray(
                            Observable.just(it),
                            fill(startX, up, targetColor),
                            fill(startX, down, targetColor),
                            fill(left, startY, targetColor),
                            fill(right, startY, targetColor))
                }
    }

    private fun step(x: Int, y: Int, targetColor: Int) : Observable<Point> {
        return Observable.create {
            val color = image.getPixel(x, y)
            if (color != targetColor) it.onComplete()
            it.onNext(Point(x, y))
            it.onComplete()
        }
    }

    private fun maxMin(value: Int, min: Int, max: Int): Int = Math.max(min, Math.min(value, max))

}