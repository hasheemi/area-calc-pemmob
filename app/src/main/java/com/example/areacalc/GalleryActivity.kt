package com.example.areacalc

import android.Manifest
import android.content.ActivityNotFoundException
import android.content.ContentUris
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.Button
import android.widget.GridView
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import coil.load
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

class GalleryActivity : AppCompatActivity() {
    private val photos = mutableListOf<Photo>()
    private lateinit var adapter: PhotoAdapter
    private lateinit var status: TextView
    private lateinit var accessButton: Button
    private var loadJob: Job? = null

    private val permissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { loadPhotos() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(Color.BLACK)
        }
        root.addView(Button(this).apply {
            setText(R.string.back_to_camera)
            setOnClickListener { finish() }
        })
        status = TextView(this).apply {
            setTextColor(Color.WHITE)
            gravity = Gravity.CENTER
            setPadding(16, 16, 16, 16)
        }
        root.addView(status)
        accessButton = Button(this).apply {
            setText(R.string.gallery_access)
            setOnClickListener {
                val permissions = mutableListOf(readPermission())
                if (Build.VERSION.SDK_INT >= 34) permissions += Manifest.permission.READ_MEDIA_VISUAL_USER_SELECTED
                permissionLauncher.launch(permissions.toTypedArray())
            }
        }
        root.addView(accessButton)
        adapter = PhotoAdapter()
        root.addView(GridView(this).apply {
            numColumns = 3
            horizontalSpacing = 4
            verticalSpacing = 4
            stretchMode = GridView.STRETCH_COLUMN_WIDTH
            adapter = this@GalleryActivity.adapter
            setOnItemClickListener { _, _, position, _ ->
                try {
                    startActivity(Intent(Intent.ACTION_VIEW).apply {
                        setDataAndType(photos[position].uri, "image/jpeg")
                        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                    })
                } catch (exception: ActivityNotFoundException) {
                    Toast.makeText(this@GalleryActivity, R.string.no_viewer, Toast.LENGTH_SHORT).show()
                }
            }
        }, LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 0, 1f))
        setContentView(root)
    }

    override fun onResume() {
        super.onResume()
        loadPhotos()
    }

    private fun readPermission() = if (Build.VERSION.SDK_INT >= 33) {
        Manifest.permission.READ_MEDIA_IMAGES
    } else Manifest.permission.READ_EXTERNAL_STORAGE

    private fun loadPhotos() {
        loadJob?.cancel()
        val fullAccess = ContextCompat.checkSelfPermission(this, readPermission()) == PackageManager.PERMISSION_GRANTED
        accessButton.visibility = if (fullAccess) View.GONE else View.VISIBLE
        loadJob = lifecycleScope.launch {
            val result = withContext(Dispatchers.IO) { runCatching { queryPhotos() } }
            photos.clear()
            photos.addAll(result.getOrDefault(emptyList()))
            adapter.notifyDataSetChanged()
            status.setText(when {
                result.isFailure -> R.string.gallery_error
                !fullAccess -> R.string.gallery_limited
                photos.isEmpty() -> R.string.gallery_empty
                else -> R.string.saved_photos
            })
        }
    }

    @Suppress("DEPRECATION")
    private fun queryPhotos(): List<Photo> {
        val collection = MediaStore.Images.Media.EXTERNAL_CONTENT_URI
        val folder = "${Environment.DIRECTORY_DCIM}/pemmobcam/"
        val modern = Build.VERSION.SDK_INT >= 29
        val column = if (modern) MediaStore.Images.Media.RELATIVE_PATH else MediaStore.Images.Media.DATA
        val directory = if (modern) folder else {
            File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM), "pemmobcam").absolutePath + "/"
        }
        val selection = if (modern) "$column = ?" else "$column LIKE ?"
        val argument = if (modern) directory else "$directory%"
        val result = mutableListOf<Photo>()
        contentResolver.query(
            collection,
            arrayOf(MediaStore.Images.Media._ID, MediaStore.Images.Media.DISPLAY_NAME, column),
            selection, arrayOf(argument), "${MediaStore.Images.Media.DATE_ADDED} DESC, ${MediaStore.Images.Media._ID} DESC"
        )?.use { cursor ->
            while (cursor.moveToNext()) {
                // On legacy Android, exclude images in subdirectories.
                if (!modern && File(cursor.getString(2)).parentFile?.absolutePath != directory.trimEnd('/')) continue
                result += Photo(ContentUris.withAppendedId(collection, cursor.getLong(0)), cursor.getString(1))
            }
        }
        return result
    }

    private data class Photo(val uri: Uri, val name: String)

    private inner class PhotoAdapter : BaseAdapter() {
        override fun getCount() = photos.size
        override fun getItem(position: Int) = photos[position]
        override fun getItemId(position: Int) = ContentUris.parseId(photos[position].uri)

        override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
            val image = convertView as? ImageView ?: ImageView(this@GalleryActivity).apply {
                scaleType = ImageView.ScaleType.CENTER_CROP
                setBackgroundColor(Color.DKGRAY)
            }
            val size = ((parent.width.takeIf { it > 0 } ?: resources.displayMetrics.widthPixels) - 8) / 3
            image.layoutParams = android.widget.AbsListView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, size)
            image.contentDescription = photos[position].name
            image.load(photos[position].uri) { size(size, size) }
            return image
        }
    }
}
