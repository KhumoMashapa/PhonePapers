package com.khumomashapa.mywallpapers.fragments

import android.annotation.SuppressLint
import android.app.DownloadManager
import android.content.Context
import android.net.ConnectivityManager
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.android.billingclient.api.*
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.database.*
import com.khumomashapa.mywallpapers.dataset.Abstract
import com.khumomashapa.mywallpapers.R
import com.khumomashapa.mywallpapers.billing.Security
import com.khumomashapa.mywallpapers.adapters.AbstractAdapter
import java.io.IOException
import kotlin.collections.ArrayList
import androidx.core.net.toUri


@Suppress("UNREACHABLE_CODE")
class AbstractWallpapers: Fragment(), AbstractAdapter.OnItemClickListenerAbstract {

    private val sharedPreferences by lazy {
        requireContext().getSharedPreferences("abstract_subscription_prefs", Context.MODE_PRIVATE)
    }
    var isSuccess = false

    lateinit var abstractRecyclerView: RecyclerView
    lateinit var reverseLayoutButton: com.melnykov.fab.FloatingActionButton
    lateinit var abstractList: ArrayList<Abstract>

    private var recyclerAdapterAbstract: AbstractAdapter? = null
    private var myRef3: DatabaseReference? = null

    var abstractData: String = ""

    var positionItem = 0

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_abstract_wallpaper, container, false)

        abstractRecyclerView = requireActivity().findViewById(R.id.abstract_recyclerView)

    }

    @SuppressLint("NotifyDataSetChanged")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        abstractRecyclerView = view.findViewById(R.id.abstract_recyclerView)
        reverseLayoutButton = view.findViewById(R.id.reverse_layout_button)

        val layoutManager = GridLayoutManager(requireContext(), 3)
        //layoutManager.reverseLayout = true // Reverse the layout direction
        abstractRecyclerView.layoutManager = layoutManager
        abstractRecyclerView.setHasFixedSize(true)

        myRef3 = FirebaseDatabase.getInstance().reference
        abstractList = ArrayList()
        GetDataFromFirebase()

        /* reverseLayoutButton.setOnClickListener {
             layoutManager.reverseLayout = !layoutManager.reverseLayout // Toggle reverseLayout
             recyclerAdapterAbstract?.notifyDataSetChanged() // Notify adapter of changes
             layoutManager.scrollToPosition(0) // Scroll to top after reversing
         }

         */

        reverseLayoutButton.setOnClickListener {
            layoutManager.reverseLayout = !layoutManager.reverseLayout
            recyclerAdapterAbstract?.notifyDataSetChanged()

            if (layoutManager.reverseLayout) {
                layoutManager.scrollToPosition(abstractList.size - 1) // Scroll to last position
            } else {
                layoutManager.scrollToPosition(0) // Scroll to top
            }
        }

        abstractRecyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
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
        val query: Query = myRef3!!.child("Abstract")

        query.addValueEventListener(object : ValueEventListener{ // Use addValueEventListener
            override fun onDataChange(snapshot: DataSnapshot) {
                abstractList.clear() // Clear the list before adding new data

                for (dataSnapshot: DataSnapshot in snapshot.children) {
                    val abstract = Abstract()
                    abstract.abstract = dataSnapshot.child("abstract").value.toString()
                    abstractList.add(abstract)
                }

                if (recyclerAdapterAbstract == null) {
                    recyclerAdapterAbstract = AbstractAdapter(requireActivity(), abstractList, this@AbstractWallpapers)
                    abstractRecyclerView.adapter = recyclerAdapterAbstract} else {
                    recyclerAdapterAbstract!!.notifyDataSetChanged() // Notify adapter of changes
                }
            }

            override fun onCancelled(error: DatabaseError) {
                // Handle errors, e.g., display an error message
                Log.e("AbstractWallpapers", "Firebase error: ${error.message}")
                Toast.makeText(requireContext(), "Failed to load data", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun showPaywallBottomSheet() {
        val paywallFragment = AbstractPaywallFragment()
        paywallFragment.show(parentFragmentManager, "PaywallFragment")
    }


    override fun onItemClick(item: String, pos:Int) {
        abstractData = item
        positionItem = pos

        if (!isNetworkAvailable()) {
            Toast.makeText(requireContext(), "Please check your internet connection", Toast.LENGTH_SHORT).show()
            return
        }

        isSuccess = sharedPreferences.getBoolean("is_subscribed", false)

        if (isSuccess){
            startDownloading()
            Toast.makeText(requireActivity(), "Saved to /storage/emulated/0/Pictures/AbstractWallpaper", Toast.LENGTH_LONG).show()

        }else{

            if (positionItem < 21){
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

        val request = DownloadManager.Request(abstractData.toUri())
        request.setAllowedNetworkTypes(DownloadManager.Request.NETWORK_WIFI or DownloadManager.Request.NETWORK_MOBILE)
        request.setTitle("Abstract Wallpaper")
        request.setDescription("Your image is downloading")
        request.allowScanningByMediaScanner()
        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
        request.setDestinationInExternalPublicDir(Environment.DIRECTORY_PICTURES, "AbstractWallpapers.jpg")
        val manager = activity?.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
        manager.enqueue(request)

        Toast.makeText(requireActivity(), "Download is starting...", Toast.LENGTH_LONG).show()
    }
}