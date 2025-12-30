package vadimerenkov.aucards.widgets

import android.content.Context
import android.net.Uri
import coil3.Bitmap
import coil3.imageLoader
import coil3.request.CachePolicy
import coil3.request.ErrorResult
import coil3.request.ImageRequest
import coil3.request.SuccessResult
import coil3.toBitmap

suspend fun loadBitmap(uri: Uri?, context: Context): Bitmap? {
	val request = ImageRequest.Builder(context).data(uri).apply {
		memoryCachePolicy(CachePolicy.DISABLED)
		diskCachePolicy(CachePolicy.DISABLED)
	}.build()

	return when (val result = context.imageLoader.execute(request)) {
		is ErrorResult -> {
			null
		}
		is SuccessResult -> {
			result.image.toBitmap()
		}
	}
}