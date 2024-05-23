package com.example.cameraapp

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ImageAdapter(
    private val images: MutableList<File>,
    private val onDelete: (File) -> Unit
) : RecyclerView.Adapter<ImageAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val imageView: ImageView = view.findViewById(R.id.imageView)
        val imageNumber: TextView = view.findViewById(R.id.imageNumber)
        val imageDate: TextView = view.findViewById(R.id.imageDate)
        val buttonDelete: ImageView = view.findViewById(R.id.iconDelete)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_image, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val imageFile = images[position]
        val bitmap = BitmapFactory.decodeFile(imageFile.absolutePath)
        val rotatedBitmap = rotateImage(bitmap, 90) // Rotate by 90 degrees clockwise
        holder.imageView.setImageBitmap(rotatedBitmap)

        holder.imageNumber.text = "Image Number: ${position + 1}"
        holder.imageDate.text = "Date Taken: ${SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date(imageFile.lastModified()))}"

        holder.buttonDelete.setOnClickListener {
            onDelete(imageFile)
        }
    }

    override fun getItemCount() = images.size

    private fun rotateImage(source: Bitmap, angle: Int): Bitmap {
        val matrix = Matrix()
        matrix.postRotate(angle.toFloat())
        return Bitmap.createBitmap(source, 0, 0, source.width, source.height, matrix, true)
    }
}



