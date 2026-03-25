package com.khumomashapa.mywallpapers.fragments

import android.annotation.SuppressLint
import android.app.DownloadManager
import android.content.Context
import android.net.ConnectivityManager
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Toast
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.android.billingclient.api.*
import com.google.firebase.database.*
import com.khumomashapa.mywallpapers.dataset.Nature
import com.khumomashapa.mywallpapers.R
import com.khumomashapa.mywallpapers.billing.Security
import com.khumomashapa.mywallpapers.adapters.NatureAdapter
import java.io.IOException
import java.util.*
import androidx.core.net.toUri

class NatureWallpapers : Fragment(), NatureAdapter.OnItemClickListenerAbstractNature {


    private val sharedPreferences by lazy {
        requireContext().getSharedPreferences("nature_subscription_prefs", Context.MODE_PRIVATE)
    }

    var isSuccess = false

    private lateinit var natureRecyclerview: RecyclerView
    lateinit var reverseLayoutButton: com.melnykov.fab.FloatingActionButton
    private lateinit var natureList: ArrayList<Nature>


    var natureData: String = ""

    var positionItem = 0

    private var dref: DatabaseReference? = null
    private var recyclerAdapterNature: NatureAdapter? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_nature_wallpapers, container, false)

    }

    @SuppressLint("NotifyDataSetChanged")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        natureRecyclerview = view.findViewById(R.id.nature_recyclerView)
        reverseLayoutButton = view.findViewById(R.id.reverse_layout_button)

        val layoutManager = GridLayoutManager(requireContext(), 3)
        //layoutManager.reverseLayout = true // Enable reverse layout
        natureRecyclerview.layoutManager = layoutManager
        natureRecyclerview.setHasFixedSize(true)

        dref = FirebaseDatabase.getInstance().reference
        natureList = ArrayList()
        GetDataFromFirebase()

        /* reverseLayoutButton.setOnClickListener {
            layoutManager.reverseLayout = !layoutManager.reverseLayout // Toggle reverseLayout
            recyclerAdapterNature?.notifyDataSetChanged() // Notify adapter of changes
            layoutManager.scrollToPosition(0) // Scroll to top after reversing
        }

         */

        reverseLayoutButton.setOnClickListener {
            layoutManager.reverseLayout = !layoutManager.reverseLayout
            recyclerAdapterNature?.notifyDataSetChanged()

            if (layoutManager.reverseLayout) {
                layoutManager.scrollToPosition(natureList.size - 1) // Scroll to last position

            } else {
                layoutManager.scrollToPosition(0) // Scroll to top
            }
        }

        natureRecyclerview.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                if (dy > 50) {

                    reverseLayoutButton.visibility = View.GONE

                } else if (dy < -50) {
                    reverseLayoutButton.visibility = View.VISIBLE
                }
            }
        })
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun GetDataFromFirebase() {
        val query: Query = dref!!.child("Nature")

        query.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                natureList.clear()

                for (dataSnapshot: DataSnapshot in snapshot.children) {
                    val nature = Nature()
                    nature.nature = dataSnapshot.child("nature").value.toString()
                    natureList.add(nature)
                }

                if (recyclerAdapterNature == null) {
                    recyclerAdapterNature = NatureAdapter(requireActivity(), natureList, this@NatureWallpapers)
                    natureRecyclerview.adapter = recyclerAdapterNature
                } else {
                    recyclerAdapterNature?.notifyDataSetChanged() // Null check before notifying
                }
            }

            override fun onCancelled(error: DatabaseError) {
                // Handle errors, e.g., display an error message
                Log.e("NatureWallpapers", "Firebase error: ${error.message}")
                Toast.makeText(requireContext(), "Failed to load data", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun showPaywallBottomSheet() {
        val paywallFragment = NaturePaywallFragment()
        paywallFragment.show(parentFragmentManager, "PaywallFragment")
    }

    override fun onItemClick(item: String, pos:Int) {
        natureData = item
        positionItem = pos

        if (!isNetworkAvailable()) {
            Toast.makeText(requireContext(), "Please check your internet connection", Toast.LENGTH_SHORT).show()
            return
        }

        isSuccess = sharedPreferences.getBoolean("is_subscribed", false)

        if (isSuccess){
            startDownloading()
            Toast.makeText(requireActivity(), "Saved to /storage/emulated/0/Pictures/NatureWallpapers", Toast.LENGTH_LONG).show()

        }else{

            if(positionItem < 10){
                startDownloading()

            }else{
                showPaywallBottomSheet()

            }
        }
    }

    private fun isNetworkAvailable(): Boolean {
        val connectivityManager = requireContext().getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val activeNetworkInfo = connectivityManager.activeNetworkInfo
        return activeNetworkInfo != null && activeNetworkInfo.isConnected
    }

    private fun startDownloading() {

        val request = DownloadManager.Request(natureData.toUri())
        request.setAllowedNetworkTypes(DownloadManager.Request.NETWORK_WIFI or DownloadManager.Request.NETWORK_MOBILE)
        request.setTitle("Abstract Nature Wallpaper")
        request.setDescription("Your image is downloading")
        request.allowScanningByMediaScanner()
        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
        request.setDestinationInExternalPublicDir(Environment.DIRECTORY_PICTURES, "AbstractNatureWallpapers.jpg")
        val manager = activity?.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
        manager.enqueue(request)

        Toast.makeText(requireActivity(), "Download is starting...", Toast.LENGTH_LONG).show()

    }
}
