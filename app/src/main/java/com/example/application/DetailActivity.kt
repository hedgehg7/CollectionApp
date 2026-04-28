package com.example.application

import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.application.data.AppDatabase
import com.example.application.databinding.ActivityDetailBinding
import kotlinx.coroutines.launch
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

class DetailActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDetailBinding
    private var itemId: Int = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        itemId = intent.getIntExtra("id", -1)

        loadItem()

        binding.deleteButton.setOnClickListener {
            deleteItem()
        }

        binding.editButton.setOnClickListener {
            val intent = Intent(this, AddItemActivity::class.java)
            intent.putExtra("id", itemId)
            startActivity(intent)
        }
    }

    private fun loadItem() {
        val db = AppDatabase.getDatabase(this)

        lifecycleScope.launch {
            val item = db.itemDao().getAll().find { it.id == itemId } ?: return@launch

            binding.nameText.text = item.name
            binding.descText.text = item.description

            val date = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())
                .format(Date(item.purchaseDate))

            val days = ((System.currentTimeMillis() - item.purchaseDate)
                    / (1000 * 60 * 60 * 24)).toInt()

            binding.daysText.text = "В коллекции: $days дней"
            binding.dateText.text = "Куплено: $date"

            binding.priceText.text = "Р ${item.currentPrice}"

            // 📊 проценты
            val percent = calculatePercent(item.purchasePrice, item.currentPrice)

            val sign = if (percent >= 0) "▲" else "▼"
            val color = if (percent >= 0) Color.GREEN else Color.RED

            binding.profitText.text = "${sign} ${String.format("%.1f", percent)}%"
            binding.profitText.setTextColor(color)

            // 🖼 фото (ВАЖНО ИСПРАВЛЕНИЕ)
            if (item.imageUri.isNotEmpty()) {
                val file = File(item.imageUri)
                if (file.exists()) {
                    binding.detailImage.setImageURI(Uri.fromFile(file))
                } else {
                    binding.detailImage.setImageResource(android.R.drawable.ic_menu_gallery)
                }
            } else {
                binding.detailImage.setImageResource(android.R.drawable.ic_menu_gallery)
            }
        }
    }

    private fun deleteItem() {
        val db = AppDatabase.getDatabase(this)

        lifecycleScope.launch {
            val item = db.itemDao().getAll().find { it.id == itemId } ?: return@launch
            db.itemDao().delete(item)
            finish()
        }
    }

    private fun calculatePercent(purchase: Double, current: Double): Double {
        if (purchase == 0.0) return 0.0
        return ((current - purchase) / purchase) * 100
    }
}