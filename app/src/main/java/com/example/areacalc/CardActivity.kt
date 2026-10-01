package com.example.areacalc

import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class CardActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_card)

        // Ambil view dengan prefix "card"
        val cardNama = findViewById<TextView>(R.id.cardNama)
        val cardJurusan = findViewById<TextView>(R.id.cardJurusan)
        val cardPosisi = findViewById<TextView>(R.id.cardPosisi)
        val cardBtnKembali = findViewById<Button>(R.id.cardBtnKembali)

        // Ambil data dari Intent
        val nama = intent.getStringExtra("EXTRA_NAMA") ?: "-"
        val jurusan = intent.getStringExtra("EXTRA_JURUSAN") ?: "-"
        val posisi = intent.getStringExtra("EXTRA_POSISI") ?: "-"

        // Tampilkan
        cardNama.text = nama
        cardJurusan.text = jurusan
        cardPosisi.text = posisi

        // Tombol kembali
        cardBtnKembali.setOnClickListener {
            finish()
        }
    }
}