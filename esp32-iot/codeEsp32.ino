// ================= PIN =================
#define DHTPIN 4
#define DHTTYPE DHT22
#define SOIL_PIN 34
#define RELAY_PIN 26   // relay aktif LOW

// ============== BATAS SOIL ============
#define SOIL_KERING 15     // < 15% = kering
#define SOIL_NORMAL 75     // >= 70% = normal/basah
#define TOLERANSI 5


#include <WiFi.h>
#include <FirebaseESP32.h>
#include "DHT.h"

// ============== WIFI ==================
#define WIFI_SSID "YOUR_WIFI"
#define WIFI_PASSWORD "YOUR_PASSWORD"

// ============== FIREBASE ==============
#define API_KEY "YOUR_FIREBASE_API_KEY"
#define DATABASE_URL "YOUR_FIREBASE_URL"

// ============== OBJEK =================
FirebaseData fbdo;
FirebaseAuth auth;
FirebaseConfig config;
DHT dht(DHTPIN, DHTTYPE);

// ============== TIMER =================
unsigned long lastDHT = 0;
unsigned long lastSoil = 0;

const unsigned long DHT_INTERVAL = 3000;   // 3 detik
const unsigned long SOIL_INTERVAL = 5000;  // 3 detik

// ============== RELAY =================
void pompaON() {
  digitalWrite(RELAY_PIN, HIGH); // relay aktif LOW
  Firebase.setString(fbdo, "/pompa/status", "ON");
}

void pompaOFF() {
  digitalWrite(RELAY_PIN, LOW);
  Firebase.setString(fbdo, "/pompa/status", "OFF");
}

void setup() {
  Serial.begin(115200);

  pinMode(RELAY_PIN, OUTPUT);
  pompaOFF();

  dht.begin();

  // WiFi
  WiFi.begin(WIFI_SSID, WIFI_PASSWORD);
  Serial.print("Connecting WiFi");
  while (WiFi.status() != WL_CONNECTED) {
    Serial.print(".");
    delay(500);
  }
  Serial.println("\nWiFi Connected");

  // Firebase
  config.api_key = API_KEY;
  config.database_url = DATABASE_URL;
  Firebase.signUp(&config, &auth, "", "");
  Firebase.begin(&config, &auth);
  Firebase.reconnectWiFi(true);

  Serial.println("Firebase siap");
}

void loop() {

  // ================== BACA SOIL ==================
  static int soil = 0;

  if (millis() - lastSoil >= SOIL_INTERVAL) {
    lastSoil = millis();

    int adc = analogRead(SOIL_PIN);
    soil = map(adc, 4095, 1500, 0, 100);
    soil = constrain(soil, 0, 100);

    Firebase.setInt(fbdo, "/sensor/kelembaban_tanah", soil);

    Serial.print("Soil: ");
    Serial.print(soil);
    Serial.println("%");
  }

  // ================== BACA DHT ==================
  if (millis() - lastDHT >= DHT_INTERVAL) {
    lastDHT = millis();

    float suhu = dht.readTemperature();
    float udara = dht.readHumidity();

    if (!isnan(suhu) && !isnan(udara)) {
      Firebase.setFloat(fbdo, "/sensor/suhu", suhu);
      Firebase.setFloat(fbdo, "/sensor/kelembaban_udara", udara);

      Serial.print("Suhu: ");
      Serial.print(suhu);
      Serial.print(" °C | Udara: ");
      Serial.print(udara);
      Serial.println(" %");
    } else {
      Serial.println("DHT gagal dibaca");
    }
  }

  // ================== BACA STATUS MANUAL ==================
  String statusFirebase = "OFF";
  if (Firebase.getString(fbdo, "/pompa/status")) {
    statusFirebase = fbdo.stringData();
  }

  // ================== LOGIKA FINAL ==================
  Serial.println("===== LOGIKA =====");

  // 🔴 TANAH NORMAL / BASAH → POMPA MATI & KUNCI
  if (soil >= SOIL_NORMAL - TOLERANSI) {
    pompaOFF();
    Serial.println("AUTO: Tanah NORMAL/BASAH → Pompa OFF (KUNCI)");
  }

  // 🟡 TANAH KERING → AUTO NYALA
  else if (soil < SOIL_KERING + TOLERANSI) {
    pompaON();
    Serial.println("AUTO: Tanah KERING → Pompa ON");
  }

  // 🟢 TANAH AMAN → MANUAL
  else {
    if (statusFirebase == "ON") {
      digitalWrite(RELAY_PIN, HIGH);
      Serial.println("MANUAL: Pompa ON");
    } else {
      digitalWrite(RELAY_PIN, LOW);
      Serial.println("MANUAL: Pompa OFF");
    }
  }

  Serial.println("---------------------------");
}
