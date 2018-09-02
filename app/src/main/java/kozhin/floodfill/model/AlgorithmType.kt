package kozhin.floodfill.model

enum class AlgorithmType {

    NON_RECURSIVE, QUEUE, RECURSIVE;

    companion object {
        private val map = AlgorithmType.values().associateBy(AlgorithmType::ordinal)
        fun fromInt(type: Int) = map[type]
    }

}