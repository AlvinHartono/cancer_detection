package com.dicoding.asclepius.view

import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import com.dicoding.asclepius.databinding.ActivityMainBinding
import com.dicoding.asclepius.helper.ImageClassifierHelper
import com.yalantis.ucrop.UCrop
import org.tensorflow.lite.task.vision.classifier.Classifications

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var imageClassifierHelper: ImageClassifierHelper


//    private var currentImageUri: Uri? = null
    private var currentResult: List<Classifications>? = null
    private val mainViewModel: MainViewModel by viewModels()

    private val launcherIntentCrop = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            val croppedUri = UCrop.getOutput(result.data!!)
            croppedUri?.let { imageUri ->
                mainViewModel.setCurrentImageUri(imageUri)
                showToast("Gambar berhasil dicrop")
                showImage()
                analyzeImage()
            }
        }
    }

    private val launcherIntentGallery = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            val selectedImg = result.data?.data as Uri
            showToast("Gambar berhasil dipilih")
            startUCrop(selectedImg)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        imageClassifierHelper = ImageClassifierHelper(
            context = this,
            classifierListener = object : ImageClassifierHelper.ClassifierListener {
                override fun onError(error: String) {
                    showToast("Terjadi kesalahan $error")
                }

                override fun onResults(results: List<Classifications>?, inferenceTime: Long) {
                    currentResult = results
                }

            },
        )
        mainViewModel.currentImageUri.observe(this) {
            showImage()
        }

        binding.galleryButton.setOnClickListener {
            startGallery()
        }
        binding.analyzeButton.setOnClickListener {
            moveToResult()
        }
    }

    private fun startUCrop(selectedImg: Uri) {
        val destinationUri =
            Uri.fromFile(cacheDir.resolve("cropped_image__${System.currentTimeMillis()}.jpg"))
        val options = UCrop.Options().apply {
            setCompressionQuality(80)
            setHideBottomControls(false)
            setFreeStyleCropEnabled(true)

        }
        val cropIntent = UCrop.of(selectedImg, destinationUri)
            .withOptions(options)
            .getIntent(this)

        launcherIntentCrop.launch(cropIntent)
    }

    private fun startGallery() {
        // TODO: Mendapatkan gambar dari Gallery.
        val intent = Intent()
        intent.action = Intent.ACTION_GET_CONTENT
        intent.type = "image/*"

        val chooser = Intent.createChooser(intent, "Choose a Picture")
        launcherIntentGallery.launch(chooser)
    }

    private fun showImage() {
        // Menampilkan gambar sesuai Gallery yang dipilih.
        mainViewModel.currentImageUri.value?.let { uri ->
            binding.previewImageView.setImageURI(uri)
            binding.analyzeButton.isEnabled = true
        } ?: run {
            binding.analyzeButton.isEnabled = false
        }
    }

    private fun analyzeImage() {
        // Menganalisa gambar yang berhasil ditampilkan.
        mainViewModel.currentImageUri.value?.let {
            imageClassifierHelper.classifyStaticImage(it)
        } ?: showToast("Pilih gambar terlebih dahulu")

    }

    private fun moveToResult() {
        val intent = Intent(this, ResultActivity::class.java).apply {
            mainViewModel.currentImageUri.value?.let {
                putExtra("IMAGE_URI", it.toString())
            }
            currentResult?.firstOrNull()?.let { classifications ->
                val topCategory = classifications.categories.maxByOrNull { it.score }
                if (topCategory != null) {
                    putExtra("CLASSIFICATION_RESULT_TEXT", topCategory.label)
                    putExtra("CONFIDENCE", topCategory.score)
                }
            }
        }
        startActivity(intent)
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}