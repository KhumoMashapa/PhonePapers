package com.khumomashapa.mywallpapers

import android.Manifest
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.content.Intent
import android.content.pm.PackageManager
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.WindowManager
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import androidx.viewpager.widget.ViewPager
import com.google.android.material.tabs.TabLayout
import com.khumomashapa.mywallpapers.adapters.TabAdapter

class MainActivity : AppCompatActivity(){

    lateinit var tabLayout: TabLayout
    lateinit var viewPager: ViewPager

    val  REQUEST_CODE = 200

    lateinit var subscription: ImageView

    private var permissions = arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE)
    private var permissionGranted = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        supportActionBar?.hide()

        enableEdgeToEdge()

        window.setFlags(WindowManager.LayoutParams.FLAG_SECURE, WindowManager.LayoutParams.FLAG_SECURE)

        permissionGranted = ActivityCompat.checkSelfPermission(this, permissions[0]) == PackageManager.PERMISSION_GRANTED

        if(!permissionGranted)
            ActivityCompat.requestPermissions(this, permissions, REQUEST_CODE)

        tabLayout = findViewById(R.id.subscription_tablayout)
        viewPager = findViewById(R.id.subscription_viewPager)
        subscription = findViewById(R.id.subscription_management)

        subscription.setOnClickListener {
            val subscriptionUrl = "https://play.google.com/store/account/subscriptions\n"
            val intent = Intent(Intent.ACTION_VIEW).apply {
                data = subscriptionUrl.toUri()
            }
            startActivity(intent)

        }

        tabLayout.addTab(tabLayout.newTab().setText("Abstract"))
        tabLayout.addTab(tabLayout.newTab().setText("Nature"))
        //tabLayout.addTab(tabLayout.newTab().setText("Ruins"))
        //tabLayout.addTab(tabLayout.newTab().setText("Scenery"))
        tabLayout.tabGravity = TabLayout.GRAVITY_FILL

        val adapter = TabAdapter(this, supportFragmentManager, tabLayout.tabCount)
        viewPager.adapter = adapter

        viewPager.addOnPageChangeListener(TabLayout.TabLayoutOnPageChangeListener(tabLayout))
        tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab) {
                viewPager.currentItem = tab.position
            }
            override fun onTabUnselected(tab: TabLayout.Tab) {}
            override fun onTabReselected(tab: TabLayout.Tab) {}
        })
    }


    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                permissionGranted = true
                // Permission granted, you can proceed with storage-related tasks
                Toast.makeText(this, "Storage permission granted", Toast.LENGTH_SHORT).show()
            } else {
                permissionGranted = false
                // Permission denied. You might want to show a message to the user
                // explaining why the permission is needed and how to grant it from settings.
                Toast.makeText(this, "Storage permission denied", Toast.LENGTH_SHORT).show()
            }
        }
    }
}