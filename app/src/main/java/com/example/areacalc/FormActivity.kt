package com.example.areacalc

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

/**
 * Halaman form untuk menambah atau mengedit anggota.
 */
class FormActivity : AppCompatActivity() {

    companion object {
        const val EXTRA_ID = "id"
    }

    private lateinit var db: DatabaseHelper
    private var anggotaId: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_form)

        db = DatabaseHelper(this)

        val etNama = findViewById<EditText>(R.id.etNama)
        val etJurusan = findViewById<EditText>(R.id.etJurusan)
        val etPosisi = findViewById<EditText>(R.id.etPosisi)
        val btnSimpan = findViewById<Button>(R.id.btnSimpan)
        val btnBatal = findViewById<Button>(R.id.btnBatal)

        anggotaId = intent.getIntExtra(EXTRA_ID, 0)

        if (anggotaId != 0) {
            title = "Edit Anggota"
            db.getAnggotaById(anggotaId)?.let { anggota ->
                etNama.setText(anggota.nama)
                etJurusan.setText(anggota.jurusan)
                etPosisi.setText(anggota.posisi)
            }
        } else {
            title = "Tambah Anggota"
        }

        btnSimpan.setOnClickListener {
            val nama = etNama.text.toString().trim()
            val jurusan = etJurusan.text.toString().trim()
            val posisi = etPosisi.text.toString().trim()

            if (nama.isEmpty() || jurusan.isEmpty() || posisi.isEmpty()) {
                Toast.makeText(this, "Semua field harus diisi!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val anggota = Anggota(id = anggotaId, nama = nama, jurusan = jurusan, posisi = posisi)

            if (anggotaId == 0) {
                db.insertAnggota(anggota)
                Toast.makeText(this, "Data berhasil disimpan", Toast.LENGTH_SHORT).show()
            } else {
                db.updateAnggota(anggota)
                Toast.makeText(this, "Data berhasil diperbarui", Toast.LENGTH_SHORT).show()
            }

            finish()
        }

        btnBatal.setOnClickListener {
            finish()
        }
    }
}
