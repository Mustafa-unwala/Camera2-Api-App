package com.example.cameraapp

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import com.example.cameraapp.databinding.ActivityPreviewBinding
import java.io.File

class Preview: AppCompatActivity() {

    private lateinit var imageFile: File
    private lateinit var binding:ActivityPreviewBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding=ActivityPreviewBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val imagePath = intent.getStringExtra("imagePath")
        if (imagePath != null) {
            imageFile = File(imagePath)
            val bitmap = BitmapFactory.decodeFile(imageFile.absolutePath)
            val rotatedBitmap = rotateImage(bitmap, 90) // Rotate by 90 degrees clockwise
            binding.imageViewPreview.setImageBitmap(rotatedBitmap)
        }

        binding.buttonSave.setOnClickListener {
            val mediaScanIntent = Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE)
            val contentUri = Uri.fromFile(imageFile)
            mediaScanIntent.data = contentUri
            sendBroadcast(mediaScanIntent)
            finish()
            startActivity(Intent(this,MainActivity::class.java))
        }

        binding.buttonRetake.setOnClickListener {
            imageFile.delete()
            setResult(Activity.RESULT_CANCELED)
            finish()
        }
        binding.backArrow.setOnClickListener {
            imageFile.delete()
            finish()
        }
    }

    override fun onBackPressed() {
        super.onBackPressed()
        imageFile.delete()
    }

    private fun rotateImage(source: Bitmap, angle: Int): Bitmap {
        val matrix = Matrix()
        matrix.postRotate(angle.toFloat())
        return Bitmap.createBitmap(source, 0, 0, source.width, source.height, matrix, true)
    }

}
