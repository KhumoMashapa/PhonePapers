package com.khumomashapa.mywallpapers.previews

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.WindowManager
import android.widget.ImageView
import com.bumptech.glide.Glide
import com.khumomashapa.mywallpapers.R


class AbstractPreview : AppCompatActivity() {

    private lateinit var previewImage: ImageView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_abstract_preview)

        previewImage = findViewById(R.id.abstract_preview_image)

        window.setFlags(WindowManager.LayoutParams.FLAG_SECURE, WindowManager.LayoutParams.FLAG_SECURE)

        val previewImage: ImageView = findViewById(R.id.abstract_preview_image)
        val bundle :Bundle? = intent.extras
        val preview = bundle!!.getString("abstract")

        Glide.with(this)
            .load(preview)
            .into(previewImage)
    }
}