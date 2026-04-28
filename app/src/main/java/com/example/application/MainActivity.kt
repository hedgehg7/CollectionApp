package com.example.application

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import com.example.application.data.AppDatabase
import com.example.application.data.CollectionItem
import com.example.application.databinding.ActivityMainBinding
import com.example.application.ui.ItemAdapter
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var adapter: ItemAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        adapter = ItemAdapter(emptyList()) {
            val intent = Intent(this, DetailActivity::class.java)
            intent.putExtra("id", it.id)
            startActivity(intent)
        }

        binding.recyclerView.layoutManager = GridLayoutManager(this, 2)
        binding.recyclerView.adapter = adapter

        binding.addButton.setOnClickListener {
            startActivity(Intent(this, AddItemActivity::class.java))
        }
    }

    override fun onResume() {
        super.onResume()
        loadItems()
    }

    private fun loadItems() {
        val db = AppDatabase.getDatabase(this)

        lifecycleScope.launch {
            val items = db.itemDao().getAll()

            val total = items.sumOf { it.currentPrice }

            adapter.update(items)

            updateDashboard(items)
        }
    }

    // 📊 ДАШБОРД
    private fun updateDashboard(items: List<CollectionItem>) {

        val count = items.size

        val purchaseTotal = items.sumOf { it.purchasePrice }
        val currentTotal = items.sumOf { it.currentPrice }

        val profit = currentTotal - purchaseTotal

        val percent = if (purchaseTotal == 0.0) 0.0
        else ((currentTotal / purchaseTotal) - 1) * 100

        val bestItem = items.maxByOrNull {
            if (it.purchasePrice == 0.0) 0.0
            else (it.currentPrice - it.purchasePrice) / it.purchasePrice
        }

        val worstItem = items.minByOrNull {
            if (it.purchasePrice == 0.0) 0.0
            else (it.currentPrice - it.purchasePrice) / it.purchasePrice
        }

        binding.countText.text = "Предметов: $count"
        binding.currentTotalText.text = "Текущая стоимость: $currentTotal"

// 📊 цвет прибыли
        val profitColor = if (profit >= 0)
            android.graphics.Color.GREEN
        else
            android.graphics.Color.RED

        binding.profitTotalText.text = "Выгода: $profit"
        binding.profitTotalText.setTextColor(profitColor)

// 📈 цвет процента
        val percentColor = if (percent >= 0) android.graphics.Color.GREEN
        else android.graphics.Color.RED

        binding.percentTotalText.text =
            "Изменение: ${String.format("%.1f", percent)}%"
        binding.percentTotalText.setTextColor(percentColor)

// 🏆 лучшие / худшие
        binding.bestItemText.text =
            "Самый ценный предмет: ${bestItem?.name ?: "-"}"

        binding.worstItemText.text =
            "Сильнее упал в цене: ${worstItem?.name ?: "-"}"
    }
}