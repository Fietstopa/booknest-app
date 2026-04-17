package git.pef.mendelu.cz.booknest.sync

import android.content.Context
import android.net.Uri
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File
import javax.inject.Inject

class LocalImageStore @Inject constructor(
    @ApplicationContext private val context: Context
) {
    fun copyToLocalFile(source: Uri, prefix: String): String {
        val directory = File(context.filesDir, "images")
        if (!directory.exists()) {
            directory.mkdirs()
        }
        val target = File(directory, "${prefix}_${System.currentTimeMillis()}.jpg")
        context.contentResolver.openInputStream(source)?.use { input ->
            target.outputStream().use { output -> input.copyTo(output) }
        }
        return target.absolutePath
    }

    fun asFile(path: String): File {
        return File(path)
    }
}
