package vadimerenkov.aucards.ui

data class SharedContentStateKey(
	val id: Int,
	val type: ContentType
)

enum class ContentType {
	CARD,
	TEXT
}
