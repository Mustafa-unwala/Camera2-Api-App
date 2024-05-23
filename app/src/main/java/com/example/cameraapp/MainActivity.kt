package com.example.cameraapp

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.os.Environment
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import java.io.File

class MainActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var fab: FloatingActionButton
    private val imageDirectory by lazy {
        getExternalFilesDir(Environment.DIRECTORY_PICTURES)
    }

    private lateinit var imageFiles: MutableList<File>
    private lateinit var imageAdapter: ImageAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        recyclerView = findViewById(R.id.recyclerView)
        fab = findViewById(R.id.fab)

        fab.setOnClickListener {
            startActivityForResult(Intent(this, Camera::class.java), REQUEST_IMAGE_CAPTURE)
        }

        recyclerView.layoutManager = LinearLayoutManager(this)
        loadImages()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == Activity.RESULT_OK) {
            loadImages()
        }
    }

    private fun loadImages() {
        imageFiles = imageDirectory?.listFiles { file -> file.extension == "jpg" }?.sortedBy { it.lastModified() }?.toMutableList() ?: mutableListOf()
        imageAdapter = ImageAdapter(imageFiles) { imageFile ->
            deleteImage(imageFile)
        }
        recyclerView.adapter = imageAdapter
    }

    private fun deleteImage(imageFile: File) {
        if (imageFile.exists()) {
            imageFile.delete()
            imageFiles.remove(imageFile)
            imageAdapter.notifyDataSetChanged()
            Toast.makeText(this, "Image deleted", Toast.LENGTH_SHORT).show()
        }
    }

    companion object {
        private const val REQUEST_IMAGE_CAPTURE = 1
    }
}
