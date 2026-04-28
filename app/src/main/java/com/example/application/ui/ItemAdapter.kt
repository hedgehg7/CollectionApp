package com.example.application.ui

import android.net.Uri
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.application.databinding.ItemRowBinding
import com.example.application.data.CollectionItem
import java.io.File

class ItemAdapter(
    private var items: List<CollectionItem>,
    private val onClick: (CollectionItem) -> Unit
) : RecyclerView.Adapter<ItemAdapter.ViewHolder>() {

    inner class ViewHolder(val binding: ItemRowBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemRowBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ViewHolder(binding)
    }

    override fun getItemCount() = items.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {

        val item = items[position]

        holder.binding.nameText.text = item.name
        holder.binding.priceText.text = "Р ${item.currentPrice}"

        holder.itemView.setOnClickListener {
            onClick(item)
        }

        // 🔥 ИСПРАВЛЕННАЯ ЗАГРУЗКА ИЗОБРАЖЕНИЯ (БЕЗ CRASH)
        val uriString = item.imageUri

        val file = java.io.File(item.imageUri)

        if (file.exists()) {
            holder.binding.itemImage.setImageURI(android.net.Uri.fromFile(file))
        } else {
            holder.binding.itemImage.setImageResource(android.R.color.darker_gray)
        }
    }

    fun update(newItems: List<CollectionItem>) {
        items = newItems
        notifyDataSetChanged()
    }
}