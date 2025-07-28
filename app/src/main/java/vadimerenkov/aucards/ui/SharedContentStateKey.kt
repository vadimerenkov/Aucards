package vadimerenkov.aucards.ui

data class SharedContentStateKey(
	val id: Int,
	val type: ContentType,
	val target: Target
)

enum class ContentType {
	CARD,
	TEXT
}

enum class Target {
	EDIT,
	VIEW
}
