package com.example.areacalc

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity

/**
 * Halaman detail anggota, berisi tombol Edit dan Hapus.
 */
class DetailActivity : AppCompatActivity() {

    companion object {
        const val EXTRA_ID = "id"
    }

    private lateinit var db: DatabaseHelper
    private var anggotaId: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_detail)

        db = DatabaseHelper(this)

        val tvNama = findViewById<TextView>(R.id.tvDetailNama)
        val tvJurusan = findViewById<TextView>(R.id.tvDetailJurusan)
        val tvPosisi = findViewById<TextView>(R.id.tvDetailPosisi)
        val btnEdit = findViewById<Button>(R.id.btnEdit)
        val btnHapus = findViewById<Button>(R.id.btnHapus)
        val btnKembali = findViewById<Button>(R.id.btnKembali)

        anggotaId = intent.getIntExtra(EXTRA_ID, 0)
        val anggota = db.getAnggotaById(anggotaId)

        if (anggota == null) {
            Toast.makeText(this, "Data tidak ditemukan", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        tvNama.text = anggota.nama
        tvJurusan.text = anggota.jurusan
        tvPosisi.text = anggota.posisi

        btnEdit.setOnClickListener {
            val intent = Intent(this, FormActivity::class.java)
            intent.putExtra(FormActivity.EXTRA_ID, anggotaId)
            startActivity(intent)
        }

        btnHapus.setOnClickListener {
            AlertDialog.Builder(this)
                .setTitle("Konfirmasi")
                .setMessage("Yakin ingin menghapus \"${anggota.nama}\"?")
                .setPositiveButton("Ya") { _, _ ->
                    db.deleteAnggota(anggotaId)
                    Toast.makeText(this, "Data berhasil dihapus", Toast.LENGTH_SHORT).show()
                    finish()
                }
                .setNegativeButton("Tidak", null)
                .show()
        }

        btnKembali.setOnClickListener {
            finish()
        }
    }
}
