package com.khumomashapa.mywallpapers.adapters

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.khumomashapa.mywallpapers.dataset.Nature
import com.khumomashapa.mywallpapers.previews.NaturePreview
import com.khumomashapa.mywallpapers.R
import java.util.*

class NatureAdapter(private val mContext: Context, private val natureList: ArrayList<Nature>, private val click: OnItemClickListenerAbstractNature) :
    RecyclerView.Adapter<NatureAdapter.ViewHolder>() {


    interface OnItemClickListenerAbstractNature{
        fun onItemClick(item:String, pos: Int)

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.nature_image_view, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {

        holder.imageView.setOnClickListener {
            val intent = Intent(mContext, NaturePreview::class.java)
            intent.putExtra("nature", natureList[position].nature.toString())
            Toast.makeText(mContext, "Fullscreen view", Toast.LENGTH_SHORT).show()
            mContext.startActivity(intent)
        }

        holder.downloadBtn.setOnClickListener {
            click.onItemClick(natureList[position].nature!!,position)


        }

        Glide.with(mContext)
            .load(natureList[position].nature)
            .into(holder.imageView)
    }

    override fun getItemCount(): Int {
        return natureList.size
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var imageView: ImageView = itemView.findViewById(R.id.NatureView)
        val downloadBtn: ImageButton = itemView.findViewById(R.id.natureDownloadBtn)

    }

    companion object {
        private const val Tag = "RecyclerView"
    }
}