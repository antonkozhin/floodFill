package kozhin.floodfill.model

import android.graphics.Bitmap
import android.graphics.Point
import io.reactivex.Observable
import java.util.*

data class QueueAlgorithm(val image: Bitmap) : BaseAlgorithm(image) {

    override fun fill(startX: Int, startY: Int, targetColor: Int) : Observable<Point> {
        return Observable.create {
            val queue = LinkedList<Point>()
            var point: Point? = Point(startX, startY)
            while (point != null) {
                val color = image.getPixel(point.x, point.y)
                if (color == targetColor) {
                    it.onNext(Point(point.x, point.y))
                    val up = maxMin(point.y + 1, 0, height - 1)
                    queue.add(Point(point.x, up))
                    val down = maxMin(point.y - 1, 0, height - 1)
                    queue.add(Point(point.x, down))
                    val left = maxMin(point.x - 1, 0, width - 1)
                    queue.add(Point(left, point.y))
                    val right = maxMin(point.x + 1, 0, width - 1)
                    queue.add(Point(right, point.y))
                }
                point = queue.pollFirst()
            }
            it.onComplete()
        }
    }

    private fun maxMin(value: Int, min: Int, max: Int): Int = Math.max(min, Math.min(value, max))

}