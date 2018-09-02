package kozhin.floodfill.model

import android.graphics.Bitmap

abstract class BaseAlgorithm(image: Bitmap) : Algorithm {

    protected var width = 0
    protected var height = 0

    init {
        width = image.width
        height = image.height
    }

}