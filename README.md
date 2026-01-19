# 🌱 PetaniZ – Smart Chili Plant Monitoring & AI-Based Disease Detection

PetaniZ adalah sistem monitoring tanaman cabai berbasis **IoT dan Artificial Intelligence (AI)**  
yang mampu memantau kondisi lingkungan tanaman secara real-time serta mendeteksi penyakit daun cabai melalui gambar.

---

## 🚀 Fitur Utama
- 📊 Monitoring suhu, kelembaban udara, dan kelembaban tanah
- 💧 Kontrol pompa air otomatis melalui Firebase
- 📷 Deteksi penyakit daun cabai menggunakan AI (CNN – MobileNetV2)
- 📱 Aplikasi Android dengan tampilan hasil diagnosis dan solusi perawatan
- ☁️ Realtime Database menggunakan Firebase

---

## 🧠 Teknologi yang Digunakan
### Artificial Intelligence
- TensorFlow & Keras
- CNN dengan **MobileNetV2 (Transfer Learning & Fine-Tuning)**
- TensorFlow Lite (Android)

### IoT
- ESP32
- Sensor DHT22
- Sensor Soil Moisture
- Relay & Pompa Air
- Baterai 18650 (khusus beban pompa)

### Backend & Cloud
- Firebase Realtime Database

### Mobile App
- Android (Kotlin)
- ViewBinding
- Lottie Animation

---

## 🖼️ Kelas Penyakit yang Dideteksi
- Bacterial Spot  
- Cercospora Leaf Spot  
- Curl Virus  
- White Spot  
- Nutrition Deficiency  
- Healthy Leaf  

---

## ⚙️ Cara Kerja Sistem
1. ESP32 membaca data sensor dan mengirimkannya ke Firebase
2. Aplikasi Android menampilkan data monitoring secara realtime
3. Pengguna mengunggah gambar daun cabai
4. Model AI menganalisis gambar secara lokal (on-device)
5. Aplikasi menampilkan hasil diagnosis dan solusi perawatan

---

## 📌 Catatan
- Model AI menggunakan **pretrained MobileNetV2** dan dilakukan **fine-tuning pada layer akhir**
  dengan dataset daun cabai untuk menyesuaikan kebutuhan klasifikasi. Proyek ini dikembangkan untuk keperluan pembelajaran, penelitian, dan portofolio, serta dapat dikembangkan lebih lanjut untuk skala industri.

---

## 👨‍💻 Author
**Edi Priyanto**  
Mahasiswa | IoT & AI Enthusiast
📌 Fokus: Android, IoT, Machine Learning

🔗 GitHub: https://github.com/ediprynto11
🔗 LinkedIn: 
