package com.example.areacalc

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

/**
 * Activity untuk menambah data anggota baru (Create).
 */
class AddActivity : AppCompatActivity() {

    private lateinit var db: DatabaseHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add)

        db = DatabaseHelper(this)

        val etNama = findViewById<EditText>(R.id.etNama)
        val etJurusan = findViewById<EditText>(R.id.etJurusan)
        val etPosisi = findViewById<EditText>(R.id.etPosisi)
        val btnSimpan = findViewById<Button>(R.id.btnSimpan)
        val btnBatal = findViewById<Button>(R.id.btnBatal)

        btnSimpan.setOnClickListener {
            val nama = etNama.text.toString().trim()
            val jurusan = etJurusan.text.toString().trim()
            val posisi = etPosisi.text.toString().trim()

            // Validasi input tidak boleh kosong
            if (nama.isEmpty() || jurusan.isEmpty() || posisi.isEmpty()) {
                Toast.makeText(this, "Semua field harus diisi!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val anggota = Anggota(
                nama = nama,
                jurusan = jurusan,
                posisi = posisi
            )

            val hasil = db.insertAnggota(anggota)
            if (hasil != -1L) {
                Toast.makeText(this, "Data berhasil disimpan", Toast.LENGTH_SHORT).show()
                finish()
            } else {
                Toast.makeText(this, "Gagal menyimpan data!", Toast.LENGTH_SHORT).show()
            }
        }

        btnBatal.setOnClickListener {
            finish()
        }
    }
}
