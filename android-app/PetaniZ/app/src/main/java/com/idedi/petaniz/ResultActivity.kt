package com.idedi.petaniz

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.airbnb.lottie.LottieAnimationView

class ResultActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_result)

        val img = findViewById<ImageView>(R.id.imageResult)
        val txtDisease = findViewById<TextView>(R.id.txtDisease)
//        val txtDesc = findViewById<TextView>(R.id.txtDesc)
        val txtSolution = findViewById<TextView>(R.id.txtSolution)
        val loadingOverlay = findViewById<View>(R.id.loadingOverlay)
        val lottieLoading = findViewById<LottieAnimationView>(R.id.lottieLoading)

       val scaleAnim = AnimationUtils.loadAnimation(this, R.anim.scale)
       val rotateAnim = AnimationUtils.loadAnimation(this, R.anim.rotate)

        loadingOverlay.visibility = View.VISIBLE
        lottieLoading.playAnimation()


        // 🔑 ambil URI dari intent
        val uriString = intent.getStringExtra("image_uri")
        if (uriString == null) {
            finish()
            return
        }

        val uri = Uri.parse(uriString)

        val bitmap: Bitmap =
            if (uri.scheme == "content") {
                MediaStore.Images.Media.getBitmap(contentResolver, uri)
            } else {
                BitmapFactory.decodeFile(uri.path)
            }

        img.setImageBitmap(bitmap)

//        val classifier = CabaiClassifier(this)
//        val (label, score) = classifier.classify(bitmap)
//
//        txtDisease.text = formatLabel(label)
////        txtDesc.text = "Akurasi: ${(score * 100).toInt()}%"
//
//        txtSolution.text = DiseaseInfo.getSolution(label)
        Thread {
            Thread.sleep(8000)
            val classifier = CabaiClassifier(this)
            val (label, score) = classifier.classify(bitmap)

            runOnUiThread {
                lottieLoading.clearAnimation()
                loadingOverlay.visibility = View.GONE

                val CONFIDENCE_THRESHOLD = 0.50f

                if (score < CONFIDENCE_THRESHOLD) {
                    // ❌ Bukan daun / gambar tidak valid
                    txtDisease.text = "Gambar tidak valid"
                    txtSolution.text = "Silakan masukkan gambar daun cabai yang jelas dan fokus."
                } else {
                    // ✅ Daun terdeteksi
                    txtDisease.text = formatLabel(label)
                    txtSolution.text = DiseaseInfo.getSolution(label)
                }
            }
        }.start()
    }
}

object DiseaseInfo {
    fun getSolution(label: String): String {
        return when (label) {
            "Bacterial_Spot" -> "Penyakit ini biasanya ditandai dengan munculnya bintik-bintik kecil berwarna coklat tua atau hitam di permukaan daun. Seiring waktu, bintik bisa melebar dan membuat daun menguning, kering, lalu rontok. Penyakit ini cepat menyebar terutama saat kondisi lembap dan air sering mengenai daun, misalnya karena hujan atau penyiraman berlebihan.\n" +
                    "\n" +
                    "Untuk mengatasinya, sebaiknya segera buang daun yang sudah terinfeksi agar tidak menular ke bagian lain. Gunakan obat tanaman khusus antibakteri sesuai dosis anjuran. Hindari menyiram langsung ke daun dan pastikan tanaman memiliki sirkulasi udara yang baik agar daun tidak selalu basah."
            "Cercospora_Leaf_Spot" -> "Penyakit ini menyebabkan munculnya bercak berwarna abu-abu atau coklat muda dengan pinggiran lebih gelap pada daun. Jika dibiarkan, bercak akan semakin banyak, daun berubah warna menjadi kuning, lalu akhirnya gugur. Penyakit ini sering muncul saat tanaman berada di lingkungan yang terlalu lembap dan jarang terkena sinar matahari.\n" +
                    "\n" +
                    "Penanganannya bisa dilakukan dengan menyemprotkan fungisida atau obat jamur tanaman secara rutin. Daun-daun kering dan sisa tanaman di sekitar area tanam sebaiknya dibersihkan agar jamur tidak berkembang. Atur jarak tanam supaya tidak terlalu rapat dan pastikan tanaman mendapat cahaya matahari yang cukup."
            "Curl_Virus" -> "Penyakit ini membuat daun terlihat keriting, menggulung, dan ukurannya menjadi lebih kecil dari normal. Pertumbuhan tanaman jadi terhambat dan hasil panen bisa menurun drastis. Penyakit ini disebabkan oleh virus yang biasanya ditularkan oleh serangga seperti kutu putih atau kutu daun.\n" +
                    "\n" +
                    "Jika tanaman sudah terinfeksi parah, sebaiknya segera dicabut dan dimusnahkan agar tidak menular ke tanaman lain. Kendalikan serangga pembawa virus dengan insektisida nabati atau kimia sesuai kebutuhan. Gunakan bibit yang sehat sejak awal dan rutin mengecek tanaman untuk mendeteksi gejala lebih dini."
            "Healthy_Leaf" -> "Daun terlihat segar, berwarna hijau merata, dan tidak terdapat bercak, lubang, atau perubahan bentuk. Tanaman tumbuh dengan baik dan menunjukkan perkembangan yang normal. Kondisi ini menandakan perawatan tanaman sudah tepat dan lingkungan tumbuhnya mendukung.\n" +
                    "\n" +
                    "Agar kondisi ini tetap terjaga, lakukan penyiraman secara teratur namun tidak berlebihan. Berikan pupuk sesuai jadwal dan kebutuhan tanaman. Jaga kebersihan area sekitar tanaman dan lakukan pengecekan rutin agar jika muncul gejala penyakit bisa segera ditangani."
            "Nutrition_Deficiency" -> "Kondisi ini terjadi ketika tanaman kekurangan unsur hara penting seperti nitrogen, fosfor, atau kalium. Gejalanya bisa berupa daun menguning, pucat, pertumbuhan terhambat, dan hasil tanaman yang tidak maksimal. Kekurangan nutrisi sering terjadi karena pemupukan tidak rutin atau kondisi tanah yang kurang subur.\n" +
                    "\n" +
                    "Untuk mengatasinya, tanaman perlu diberi pupuk yang sesuai, seperti pupuk NPK atau pupuk organik. Perhatikan dosis agar tidak berlebihan. Cek kondisi tanah secara berkala dan lakukan pemupukan secara teratur supaya tanaman mendapatkan nutrisi yang cukup untuk tumbuh optimal."
            "White_Spot" -> "Penyakit ini ditandai dengan munculnya bercak putih atau keabu-abuan pada daun. Bercak tersebut bisa menyebar dan membuat daun menjadi lemah serta mudah rusak. Penyakit ini sering muncul pada kondisi lingkungan yang lembap dan kurang sinar matahari, serta bisa disebabkan oleh jamur.\n" +
                    "\n" +
                    "Penanganannya dapat dilakukan dengan mengurangi kelembapan di sekitar tanaman dan meningkatkan sirkulasi udara. Daun yang sudah terinfeksi sebaiknya segera dibuang. Jika diperlukan, gunakan obat jamur tanaman sesuai anjuran dan pastikan tanaman mendapatkan cahaya matahari yang cukup setiap hari."
            else -> "Informasi belum tersedia."
        }
    }
}

private fun formatLabel(label: String): String {
    return label
        .replace("_", " ")
        .lowercase()
        .split(" ")
        .joinToString(" ") { it.replaceFirstChar { c -> c.uppercase() } }
}



