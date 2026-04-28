package com.example.application

import android.net.Uri
import android.os.Bundle
import android.widget.ArrayAdapter
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.application.databinding.ActivityAddItemBinding
import com.example.application.data.AppDatabase
import com.example.application.data.CollectionItem
import kotlinx.coroutines.launch
import java.io.File

class AddItemActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAddItemBinding

    private var itemId: Int = -1
    private var imageUri: Uri? = null
    private var oldItem: CollectionItem? = null

    // ✅ ОДИН список категорий (без дублей)
    private val categories = listOf(
        "Монеты",
        "Марки",
        "Фигурки",
        "Другое"
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityAddItemBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val db = AppDatabase.getDatabase(this)

        // ✅ Spinner (ТОЛЬКО ОДИН раз!)
        val spinnerAdapter = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_item,
            categories
        )
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.categorySpinner.adapter = spinnerAdapter

        // 📌 получаем id
        itemId = intent.getIntExtra("id", -1)

        // 📌 загрузка при редактировании
        if (itemId != -1) {
            lifecycleScope.launch {
                oldItem = db.itemDao().getAll().find { it.id == itemId }

                oldItem?.let { item ->
                    binding.nameInput.setText(item.name)
                    binding.descInput.setText(item.description)
                    binding.currentInput.setText(item.currentPrice.toString())
                    binding.purchaseInput.setText(item.purchasePrice.toString())

                    if (item.imageUri.isNotEmpty()) {
                        binding.imagePreview.setImageURI(Uri.fromFile(File(item.imageUri)))
                    }

                    // ✅ выставляем категорию
                    val index = categories.indexOf(item.category)
                    if (index != -1) {
                        binding.categorySpinner.setSelection(index)
                    }
                }
            }
        }

        // 📌 выбор фото
        val imagePicker = registerForActivityResult(
            ActivityResultContracts.GetContent()
        ) { uri ->
            if (uri != null) {
                imageUri = uri
                binding.imagePreview.setImageURI(uri)
            }
        }

        binding.selectImageButton.setOnClickListener {
            imagePicker.launch("image/*")
        }

        // 📌 сохранение
        binding.saveButton.setOnClickListener {

            val isEdit = oldItem != null

            val selectedImage = imageUri

            val finalImagePath = when {
                selectedImage != null -> copyImageToInternalStorage(selectedImage)
                oldItem?.imageUri?.isNotEmpty() == true -> oldItem!!.imageUri
                else -> ""
            }

            val item = CollectionItem(
                id = oldItem?.id ?: 0,

                name = binding.nameInput.text.toString()
                    .takeIf { it.isNotBlank() }
                    ?: oldItem?.name ?: "",

                description = binding.descInput.text.toString()
                    .takeIf { it.isNotBlank() }
                    ?: oldItem?.description ?: "",

                purchasePrice = binding.purchaseInput.text.toString()
                    .toDoubleOrNull()
                    ?: oldItem?.purchasePrice ?: 0.0,

                currentPrice = binding.currentInput.text.toString()
                    .toDoubleOrNull()
                    ?: oldItem?.currentPrice ?: 0.0,

                imageUri = finalImagePath,

                // ✅ категория берётся ОТСЮДА
                category = categories[binding.categorySpinner.selectedItemPosition],

                purchaseDate = oldItem?.purchaseDate ?: System.currentTimeMillis()
            )

            lifecycleScope.launch {
                if (isEdit) {
                    db.itemDao().update(item)
                } else {
                    db.itemDao().insert(item)
                }

                finish()
            }
        }
    }

    // 📌 сохранение фото в память приложения
    private fun copyImageToInternalStorage(uri: Uri): String {
        val inputStream = contentResolver.openInputStream(uri)
        val file = File(filesDir, "img_${System.currentTimeMillis()}.jpg")
        val outputStream = file.outputStream()

        inputStream?.copyTo(outputStream)

        inputStream?.close()
        outputStream.close()

        return file.absolutePath
    }
}