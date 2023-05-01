package com.example.a3d_printing_service_front

import android.annotation.SuppressLint
import android.graphics.BitmapFactory
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.Adapter
import com.example.a3d_printing_service_front.pojo.OrderPojo


class RecycleViewOrdersAdapter(private val names: MutableList<OrderPojo>, onClickListener: OnStateClickListener) : Adapter<RecycleViewOrdersAdapter.ViewHolder>() {

    interface OnStateClickListener {
        fun onStateClick(name: OrderPojo, position: Int)
    }

    private val listener: OnStateClickListener = onClickListener

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView){
        val textViewOrder: TextView = itemView.findViewById(R.id.textViewOrder)
        val imageViewOrder: ImageView = itemView.findViewById(R.id.imageViewOrder)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val itemView =
            LayoutInflater.from(parent.context)
                .inflate(R.layout.recycle_view_order_part, parent, false)

        return ViewHolder(itemView)
    }

    override fun getItemCount() = names.size

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val name = names[position]
        holder.textViewOrder.text = "№${name.id} | ${name.status}\n\n${name.description}"
        holder.imageViewOrder.setImageBitmap(BitmapFactory.decodeByteArray(name.photo, 0, name.photo.size))

        holder.itemView.setOnClickListener { listener.onStateClick(name, position) }
    }



}