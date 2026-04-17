package git.pef.mendelu.cz.booknest.ui.screens.MapScreen.utils

import android.content.Context
import android.graphics.Bitmap
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.DrawableCompat
import com.google.android.gms.maps.MapsInitializer
import com.google.android.gms.maps.model.BitmapDescriptor
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import kotlin.math.roundToInt

internal fun bitmapDescriptorFromVector(
    context: Context,
    vectorResId: Int
): BitmapDescriptor {
    MapsInitializer.initialize(context)
    val drawable = ContextCompat.getDrawable(context, vectorResId)
        ?: return BitmapDescriptorFactory.defaultMarker()
    val wrapped = DrawableCompat.wrap(drawable)
    val sizePx = (32 * context.resources.displayMetrics.density).toInt()
    val intrinsicWidth = if (wrapped.intrinsicWidth > 0) wrapped.intrinsicWidth else sizePx
    val intrinsicHeight = if (wrapped.intrinsicHeight > 0) wrapped.intrinsicHeight else sizePx
    val scale = minOf(
        sizePx.toFloat() / intrinsicWidth.toFloat(),
        sizePx.toFloat() / intrinsicHeight.toFloat()
    )
    val width = (intrinsicWidth * scale).toInt().coerceAtLeast(1)
    val height = (intrinsicHeight * scale).toInt().coerceAtLeast(1)
    wrapped.setBounds(0, 0, width, height)
    val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
    val canvas = android.graphics.Canvas(bitmap)
    wrapped.draw(canvas)
    return BitmapDescriptorFactory.fromBitmap(bitmap)
}

internal fun formatDistance(meters: Float): String {
    return if (meters < 1000f) {
        "${meters.roundToInt()}m"
    } else {
        val km = meters / 1000f
        "${String.format("%.1f", km)}Km"
    }
}
