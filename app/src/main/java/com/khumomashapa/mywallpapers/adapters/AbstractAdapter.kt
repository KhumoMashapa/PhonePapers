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
import com.khumomashapa.mywallpapers.dataset.Abstract
import com.khumomashapa.mywallpapers.previews.AbstractPreview
import com.khumomashapa.mywallpapers.R


class AbstractAdapter(private val mContext: Context, private val abstractList: ArrayList<Abstract>, private val click: OnItemClickListenerAbstract) :
    RecyclerView.Adapter<AbstractAdapter.ViewHolder>() {

    interface OnItemClickListenerAbstract{
        fun onItemClick(item:String, pos: Int)

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.abstract_image_view, parent, false)
        return ViewHolder(view)

    }
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        Glide.with(mContext)
            .load(abstractList[position].abstract)
            .into(holder.imageView)

        holder.imageView.setOnClickListener {
            val intent = Intent(mContext, AbstractPreview::class.java)
            intent.putExtra("abstract", abstractList[position].abstract.toString())
            Toast.makeText(mContext, "Fullscreen view", Toast.LENGTH_SHORT).show()
            mContext.startActivity(intent)

        }

        holder.downloadBtn.setOnClickListener {
            click.onItemClick(abstractList[position].abstract!!,position)

        }
    }

    override fun getItemCount(): Int {
        return abstractList.size
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imageView: ImageView = itemView.findViewById(R.id.abstractImageView)
        val downloadBtn: ImageButton = itemView.findViewById(R.id.abstractDownloadBtn)

    }

    companion object
}