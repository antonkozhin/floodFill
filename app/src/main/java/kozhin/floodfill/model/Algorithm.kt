package kozhin.floodfill.model

import android.graphics.Point
import io.reactivex.Observable

interface Algorithm {

    fun fill(startX: Int, startY: Int, targetColor: Int) : Observable<Point>

}