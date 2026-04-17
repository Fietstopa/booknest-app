package git.pef.mendelu.cz.booknest.ui.screens.MapScreen.books

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.OptIn
import androidx.camera.core.CameraSelector
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import dagger.hilt.android.EntryPointAccessors
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.auth.FirebaseAuth
import com.google.mlkit.vision.barcode.BarcodeScannerOptions
import git.pef.mendelu.cz.booknest.R
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.Text
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import git.pef.mendelu.cz.booknest.communication.BookApiProvider
import git.pef.mendelu.cz.booknest.communication.BookRemoteRepositoryImpl
import git.pef.mendelu.cz.booknest.communication.CommunicationResult
import git.pef.mendelu.cz.booknest.navigation.INavigationRouter
import git.pef.mendelu.cz.booknest.database.BooknestDatabase
import git.pef.mendelu.cz.booknest.database.entities.AddedBookEntity
import git.pef.mendelu.cz.booknest.database.entities.HistoryEntryEntity
import git.pef.mendelu.cz.booknest.sync.NetworkMonitor
import java.util.concurrent.Executors
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@OptIn(ExperimentalGetImage::class)
@Composable
internal fun LibraryBookScannerScreen(
    navRouter: INavigationRouter,
    libraryId: String,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val firestore = remember { FirebaseFirestore.getInstance() }
    val booksRepository = remember { BookRemoteRepositoryImpl(BookApiProvider.create()) }
    val scope = rememberCoroutineScope()
    val currentUser = FirebaseAuth.getInstance().currentUser
    val entryPoint = EntryPointAccessors.fromApplication(
        context,
        ScannerEntryPoint::class.java
    )
    val database = entryPoint.database()
    val networkMonitor = entryPoint.networkMonitor()

    val permissionGranted = remember { mutableStateOf(false) }
    val errorMessage = remember { mutableStateOf<String?>(null) }
    val isSaving = remember { mutableStateOf(false) }
    val lastIsbn = remember { mutableStateOf<String?>(null) }
    val bookNotFoundMessage = stringResource(R.string.error_book_not_found)
    val unknownTitle = stringResource(R.string.unknown_title)
    val saveBookFailedMessage = stringResource(R.string.error_failed_to_save_book)
    val searchFailedMessage = stringResource(R.string.error_search_failed)
    val connectionErrorMessage = stringResource(R.string.error_connection)
    val unexpectedErrorMessage = stringResource(R.string.error_unexpected)

    val previewView = remember { PreviewView(context) }
    val cameraExecutor = remember { Executors.newSingleThreadExecutor() }

    DisposableEffect(Unit) {
        onDispose { cameraExecutor.shutdown() }
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        permissionGranted.value = granted
    }

    LaunchedEffect(Unit) {
        permissionGranted.value = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED
        if (!permissionGranted.value) {
            permissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    LaunchedEffect(permissionGranted.value) {
        if (!permissionGranted.value) return@LaunchedEffect

        val cameraProvider = context.getCameraProvider()
        val preview = Preview.Builder().build().also {
            it.setSurfaceProvider(previewView.surfaceProvider)
        }
        val analysis = ImageAnalysis.Builder()
            .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
            .build()

        val scannerOptions = BarcodeScannerOptions.Builder()
            .setBarcodeFormats(
                Barcode.FORMAT_EAN_13,
                Barcode.FORMAT_EAN_8,
                Barcode.FORMAT_UPC_A,
                Barcode.FORMAT_UPC_E
            )
            .build()
        val scanner = BarcodeScanning.getClient(scannerOptions)
        val textRecognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)

        analysis.setAnalyzer(cameraExecutor) { imageProxy ->
            val mediaImage = imageProxy.image
            if (mediaImage == null || isSaving.value) {
                imageProxy.close()
                return@setAnalyzer
            }
            var closed = false
            fun closeOnce() {
                if (!closed) {
                    closed = true
                    imageProxy.close()
                }
            }
            fun handleIsbn(isbn: String) {
                if (isSaving.value || isbn == lastIsbn.value) return
                lastIsbn.value = isbn
                isSaving.value = true
                errorMessage.value = null
                scope.launch {
                    if (!networkMonitor.checkOnline()) {
                        errorMessage.value = connectionErrorMessage
                        lastIsbn.value = null
                        isSaving.value = false
                        return@launch
                    }
                    when (val response = booksRepository.searchBooks("isbn:$isbn")) {
                        is CommunicationResult.Success -> {
                            val item = response.data.items?.firstOrNull()
                            if (item == null) {
                                errorMessage.value = bookNotFoundMessage
                                lastIsbn.value = null
                                isSaving.value = false
                                return@launch
                            }
                            val info = item.volumeInfo
                            val thumbnail = info.imageLinks?.thumbnail?.replace("http://", "https://")
                            val bookData = mapOf(
                                "volumeId" to item.id,
                                "title" to (info.title ?: unknownTitle),
                                "authors" to (info.authors ?: emptyList<String>()),
                                "thumbnail" to thumbnail,
                                "addedByUid" to currentUser?.uid,
                                "addedByName" to (
                                    currentUser?.displayName
                                        ?.takeIf { it.isNotBlank() }
                                        ?: currentUser?.email?.substringBefore("@")
                                ),
                                "createdAt" to com.google.firebase.firestore.FieldValue.serverTimestamp()
                            )
                            firestore.collection("libraries")
                                .document(libraryId)
                                .collection("books")
                                .document(item.id)
                                .set(bookData)
                                .addOnSuccessListener {
                                    currentUser?.uid?.let { uid ->
                                        firestore.collection("users")
                                            .document(uid)
                                            .collection("addedBooks")
                                            .document(item.id)
                                            .set(
                                                bookData + mapOf(
                                                    "libraryId" to libraryId
                                                )
                                            )
                                    }
                                    onBack()
                                }
                                .addOnFailureListener {
                                    errorMessage.value = it.message ?: saveBookFailedMessage
                                    lastIsbn.value = null
                                    isSaving.value = false
                                }
                            val createdAt = System.currentTimeMillis()
                            database.addedBookDao().upsert(
                                AddedBookEntity(
                                    id = item.id,
                                    title = info.title ?: unknownTitle,
                                    authors = info.authors ?: emptyList(),
                                    thumbnail = thumbnail,
                                    libraryId = libraryId,
                                    addedByUid = currentUser?.uid,
                                    createdAtMillis = createdAt
                                )
                            )
                            database.historyDao().upsert(
                                HistoryEntryEntity(
                                    id = "book_${item.id}",
                                    type = "book_added",
                                    bookId = item.id,
                                    bookTitle = info.title ?: unknownTitle,
                                    libraryId = libraryId,
                                    libraryName = null,
                                    createdAtMillis = createdAt
                                )
                            )
                        }
                        is CommunicationResult.Error -> {
                            errorMessage.value = response.error.message ?: searchFailedMessage
                            lastIsbn.value = null
                            isSaving.value = false
                        }
                        is CommunicationResult.ConnectionError -> {
                            errorMessage.value = connectionErrorMessage
                            lastIsbn.value = null
                            isSaving.value = false
                        }
                        is CommunicationResult.Exception -> {
                            errorMessage.value = response.exception.message ?: unexpectedErrorMessage
                            lastIsbn.value = null
                            isSaving.value = false
                        }
                    }
                }
            }
            val image = InputImage.fromMediaImage(
                mediaImage,
                imageProxy.imageInfo.rotationDegrees
            )
            scanner.process(image)
                .addOnSuccessListener { barcodes ->
                    val raw = barcodes.firstOrNull { it.rawValue != null }?.rawValue
                    val isbn = raw?.filter { it.isDigit() }
                    if (!isbn.isNullOrBlank()) {
                        handleIsbn(isbn)
                        closeOnce()
                    } else {
                        textRecognizer.process(image)
                            .addOnSuccessListener { visionText: Text ->
                                extractIsbnFromText(visionText.text)?.let { handleIsbn(it) }
                            }
                            .addOnCompleteListener { closeOnce() }
                    }
                }
                .addOnFailureListener {
                    textRecognizer.process(image)
                        .addOnSuccessListener { visionText: Text ->
                            extractIsbnFromText(visionText.text)?.let { handleIsbn(it) }
                        }
                        .addOnCompleteListener { closeOnce() }
                }
        }

        cameraProvider.unbindAll()
        cameraProvider.bindToLifecycle(
            lifecycleOwner,
            CameraSelector.DEFAULT_BACK_CAMERA,
            preview,
            analysis
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF1F1B16))
    ) {
        if (!permissionGranted.value) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = stringResource(R.string.camera_permission_required),
                        color = Color(0xFFF5E7CD)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Button(onClick = {
                        permissionLauncher.launch(Manifest.permission.CAMERA)
                    }) {
                        Text(text = stringResource(R.string.grant_permission))
                    }
                }
            }
        } else {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(12.dp)
            ) {
                AndroidView(
                    factory = { previewView },
                    modifier = Modifier.fillMaxSize()
                )
                errorMessage.value?.let { message ->
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .align(Alignment.BottomCenter)
                            .background(Color(0xFFB3261E))
                            .padding(12.dp)
                    ) {
                        Text(text = message, color = Color.White)
                    }
                }
            }
        }
    }
}

private suspend fun android.content.Context.getCameraProvider(): ProcessCameraProvider {
    return suspendCancellableCoroutine { continuation ->
        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)
        cameraProviderFuture.addListener(
            {
                continuation.resume(cameraProviderFuture.get())
            },
            ContextCompat.getMainExecutor(this)
        )
    }
}

@androidx.annotation.VisibleForTesting
internal fun extractIsbnFromText(text: String): String? {
    val normalizedText = text.uppercase()
    val labelRegex = Regex("ISBN(?:-1[03])?[^0-9X]*([0-9X\\-\\s]{10,20})")
    val genericRegex = Regex("\\b[0-9X][0-9X\\-\\s]{8,20}[0-9X]\\b")

    val candidates = mutableListOf<String>()
    labelRegex.findAll(normalizedText).forEach { match ->
        candidates.add(match.groupValues[1])
    }
    genericRegex.findAll(normalizedText).forEach { match ->
        candidates.add(match.value)
    }

    val cleaned = candidates.mapNotNull { raw ->
        val digits = raw.filter { it.isDigit() || it == 'X' }
        when {
            digits.length == 13 && isValidIsbn13(digits) -> digits
            digits.length == 10 && isValidIsbn10(digits) -> digits
            else -> null
        }
    }
    return cleaned.firstOrNull { it.length == 13 } ?: cleaned.firstOrNull()
}

@androidx.annotation.VisibleForTesting
internal fun isValidIsbn13(value: String): Boolean {
    if (!value.all { it.isDigit() } || value.length != 13) return false
    val sum = value.take(12).mapIndexed { index, c ->
        val digit = c.digitToInt()
        if (index % 2 == 0) digit else digit * 3
    }.sum()
    val check = (10 - (sum % 10)) % 10
    return check == value.last().digitToInt()
}

@androidx.annotation.VisibleForTesting
internal fun isValidIsbn10(value: String): Boolean {
    if (value.length != 10) return false
    val digits = value.mapIndexed { index, c ->
        when {
            index == 9 && c == 'X' -> 10
            c.isDigit() -> c.digitToInt()
            else -> return false
        }
    }
    val sum = digits.mapIndexed { index, digit -> digit * (10 - index) }.sum()
    return sum % 11 == 0
}

@EntryPoint
@InstallIn(SingletonComponent::class)
interface ScannerEntryPoint {
    fun database(): BooknestDatabase
    fun networkMonitor(): NetworkMonitor
}
