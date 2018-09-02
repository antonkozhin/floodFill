package kozhin.floodfill.model

import android.graphics.Bitmap
import android.graphics.Point
import io.reactivex.Observable
import java.util.*

data class NonRecursiveAlgorithm(val image: Bitmap) : BaseAlgorithm(image) {

    override fun fill(startX: Int, startY: Int, targetColor: Int) : Observable<Point> {
        return Observable.create {
            val queue = LinkedList<Point>()
            var point: Point? = Point(startX, startY)
            do {
                var x = point!!.x
                val y = point.y
                while (x > 0 && image.getPixel(x - 1, y) == targetColor) x--
                var spanUp = false
                var spanDown = false

                while (x < width && image.getPixel(x, y) == targetColor) {
                    it.onNext(Point(x, y))

                    if (!spanUp && y > 0 && image.getPixel(x, y - 1) == targetColor) {
                        queue.add(Point(x, y - 1))
                        spanUp = true
                    } else if (spanUp && y > 0 && image.getPixel(x, y - 1) != targetColor) {
                        spanUp = false
                    }

                    if (!spanDown && y < height - 1 && image.getPixel(x, y + 1) == targetColor) {
                        queue.add(Point(x, y + 1))
                        spanDown = true
                    } else if (spanDown && y < height - 1 && image.getPixel(x, y + 1) != targetColor) {
                        spanDown = false
                    }
                    x++
                }
                point = queue.pollFirst()
            } while (point != null)
            it.onComplete()
        }
    }

}