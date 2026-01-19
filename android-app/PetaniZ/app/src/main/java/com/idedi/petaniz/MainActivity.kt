package com.idedi.petaniz

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import java.io.File
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError

private val SOIL_KERING = 15
private val SOIL_NORMAL = 75

class MainActivity : AppCompatActivity() {

    private lateinit var photoFile: File
    private lateinit var photoUri: Uri
    private lateinit var db: DatabaseReference
    private var soilValue: Long = 0

    // Kamera
    private val cameraLauncher =
        registerForActivityResult(ActivityResultContracts.TakePicture()) { success ->
            if (success) openResult(photoUri)
        }

    // Galeri
    private val galleryLauncher =
        registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
            uri?.let { openResult(it) }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Firebase reference
        val database = FirebaseDatabase.getInstance(
            "https://petaniz-default-rtdb.asia-southeast1.firebasedatabase.app"
        )
        db = database.reference

        // View
        val tvTemp = findViewById<TextView>(R.id.tvTemp)
        val tvHumidity = findViewById<TextView>(R.id.tvHumidity)
        val tvSoil = findViewById<TextView>(R.id.tvSoil)
        val tvSoilStatus = findViewById<TextView>(R.id.tvSoilStatus)
        val btnPompa = findViewById<com.google.android.material.materialswitch.MaterialSwitch>(R.id.btnPompa)

        db.child("sensor").addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {

                Log.d("FIREBASE", "DATA = ${snapshot.value}")
                val suhu = snapshot.child("suhu").getValue(Double::class.java)
                val udara = snapshot.child("kelembaban_udara").getValue(Long::class.java)
                val tanah = snapshot.child("kelembaban_tanah").getValue(Long::class.java)

                suhu?.let {
                    tvTemp.text = "${it}°C"
                }

                udara?.let {
                    tvHumidity.text = "${it}%"
                }

                tanah?.let {
                    soilValue = it
                    tvSoil.text = "${it}%"

                    when {
                        it < SOIL_KERING -> {
                            tvSoilStatus.text = "Kering"
                            btnPompa.isEnabled = true   // AUTO → BOLEH
                        }

                        it < SOIL_NORMAL -> {
                            tvSoilStatus.text = "Normal"
                            btnPompa.isEnabled = true    // MANUAL BOLEH
                        }

                        else -> {
                            tvSoilStatus.text = "Basah"
                            btnPompa.isEnabled = false   // DIKUNCI
                        }
                    }
                }

            }

            override fun onCancelled(error: DatabaseError) {}
        })

        db.child("pompa").child("status")
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val status = snapshot.getValue(String::class.java)
                    btnPompa.isChecked = status == "ON"
                }

                override fun onCancelled(error: DatabaseError) {}
            })

        btnPompa.setOnCheckedChangeListener { _, isChecked ->
            if (!btnPompa.isEnabled) return@setOnCheckedChangeListener

            val status = if (isChecked) "ON" else "OFF"
            db.child("pompa").child("status").setValue(status)
        }

        val Camera = findViewById<ImageView>(R.id.icCamera)

        Camera.setOnClickListener {
            showImagePicker()
        }

        WindowCompat.setDecorFitsSystemWindows(window, false)

        val content = findViewById<View>(R.id.content)
//        val fab = findViewById<View>(R.id.fabCamera)

        ViewCompat.setOnApplyWindowInsetsListener(content) { view, insets ->
            val statusBar = insets.getInsets(WindowInsetsCompat.Type.statusBars()).top
            val navBar = insets.getInsets(WindowInsetsCompat.Type.navigationBars()).bottom

            view.setPadding(
                view.paddingLeft,
                statusBar,
                view.paddingRight,
                navBar   // ⬅️ INI YANG SEBELUMNYA KURANG
            )
            insets
        }

//        ViewCompat.setOnApplyWindowInsetsListener(fab) { view, insets ->
//            val navBar = insets.getInsets(WindowInsetsCompat.Type.navigationBars()).bottom
//
//            view.translationY = -navBar.toFloat()
//            insets
//        }

    }

    // Dialog pilihan kamera / galeri
    private fun showImagePicker() {
        val items = arrayOf("📷 Kamera", "🖼 Galeri")

        AlertDialog.Builder(this)
            .setTitle("Pilih Sumber Gambar")
            .setItems(items) { _, which ->
                when (which) {
                    0 -> openCamera()
                    1 -> galleryLauncher.launch("image/*")
                }
            }
            .show()
    }

    private fun openCamera() {
        photoFile = File.createTempFile(
            "cabai_", ".jpg",
            getExternalFilesDir("Pictures")
        )

        photoUri = FileProvider.getUriForFile(
            this,
            "${packageName}.provider",
            photoFile
        )

        cameraLauncher.launch(photoUri)
    }

    private fun openResult(uri: Uri) {
        val intent = Intent(this, ResultActivity::class.java)
        intent.putExtra("image_uri", uri.toString())
        startActivity(intent)
    }
}
