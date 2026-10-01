package com.example.areacalc

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

/**
 * Activity untuk mengubah data anggota (Update).
 * Menerima ID anggota melalui Intent dengan extra "id".
 */
class EditActivity : AppCompatActivity() {

    companion object {
        const val EXTRA_ID = "id"
    }

    private lateinit var db: DatabaseHelper
    private var anggotaId: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit)

        db = DatabaseHelper(this)

        val tvIdEdit = findViewById<TextView>(R.id.tvIdEdit)
        val etNama = findViewById<EditText>(R.id.etNama)
        val etJurusan = findViewById<EditText>(R.id.etJurusan)
        val etPosisi = findViewById<EditText>(R.id.etPosisi)
        val btnUpdate = findViewById<Button>(R.id.btnUpdate)
        val btnBatal = findViewById<Button>(R.id.btnBatal)

        anggotaId = intent.getIntExtra(EXTRA_ID, 0)

        // Muat data lama berdasarkan ID
        val anggota = db.getAnggotaById(anggotaId)
        if (anggota == null) {
            Toast.makeText(this, "Data tidak ditemukan!", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        // Tampilkan data lama (pre-filled)
        tvIdEdit.text = "ID: ${anggota.id}"
        etNama.setText(anggota.nama)
        etJurusan.setText(anggota.jurusan)
        etPosisi.setText(anggota.posisi)

        btnUpdate.setOnClickListener {
            val nama = etNama.text.toString().trim()
            val jurusan = etJurusan.text.toString().trim()
            val posisi = etPosisi.text.toString().trim()

            // Validasi input tidak boleh kosong
            if (nama.isEmpty() || jurusan.isEmpty() || posisi.isEmpty()) {
                Toast.makeText(this, "Semua field harus diisi!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val anggotaBaru = Anggota(
                id = anggotaId,
                nama = nama,
                jurusan = jurusan,
                posisi = posisi
            )

            val hasil = db.updateAnggota(anggotaBaru)
            if (hasil > 0) {
                Toast.makeText(this, "Data berhasil diperbarui", Toast.LENGTH_SHORT).show()
                finish()
            } else {
                Toast.makeText(this, "Gagal memperbarui data!", Toast.LENGTH_SHORT).show()
            }
        }

        btnBatal.setOnClickListener {
            finish()
        }
    }
}
