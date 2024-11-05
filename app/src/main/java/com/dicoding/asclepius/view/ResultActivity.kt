package com.dicoding.asclepius.view

import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.MenuItem
import com.dicoding.asclepius.R
import com.dicoding.asclepius.databinding.ActivityResultBinding
import org.tensorflow.lite.support.label.Category
import java.text.NumberFormat
import java.util.Locale

class ResultActivity : AppCompatActivity() {
    private lateinit var binding: ActivityResultBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityResultBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.mainToolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        // Menampilkan hasil gambar, prediksi, dan confidence score.
        val imageUriStr = intent.getStringExtra("IMAGE_URI")
        val classificationLabel = intent.getStringExtra("CLASSIFICATION_RESULT_TEXT")
        val confidence = intent.getFloatExtra("CONFIDENCE", 0f)

        imageUriStr.let {
            binding.resultImage.setImageURI(Uri.parse(it))
        }

        val resultBuilder = StringBuilder()

        val confidencePercentage = NumberFormat.getPercentInstance(Locale("id")).format(confidence)
        resultBuilder.append("Prediction Result: $classificationLabel\n")
        resultBuilder.append("Confidence Level: $confidencePercentage")

        binding.resultText.text = resultBuilder.toString()
    }


    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                onBackPressedDispatcher.onBackPressed()
                true
            }

            else -> super.onOptionsItemSelected(item)
        }
    }


}