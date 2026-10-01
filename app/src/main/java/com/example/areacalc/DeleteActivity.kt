package com.example.areacalc

import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity

/**
 * Activity untuk menghapus data anggota (Delete).
 * Menerima ID anggota melalui Intent dengan extra "id".
 */
class DeleteActivity : AppCompatActivity() {

    companion object {
        const val EXTRA_ID = "id"
    }

    private lateinit var db: DatabaseHelper
    private var anggotaId: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_delete)

        db = DatabaseHelper(this)

        val tvHapusNama = findViewById<TextView>(R.id.tvHapusNama)
        val tvHapusJurusan = findViewById<TextView>(R.id.tvHapusJurusan)
        val tvHapusPosisi = findViewById<TextView>(R.id.tvHapusPosisi)
        val btnKonfirmasiHapus = findViewById<Button>(R.id.btnKonfirmasiHapus)
        val btnBatal = findViewById<Button>(R.id.btnBatal)

        anggotaId = intent.getIntExtra(EXTRA_ID, 0)

        // Muat data berdasarkan ID
        val anggota = db.getAnggotaById(anggotaId)
        if (anggota == null) {
            Toast.makeText(this, "Data tidak ditemukan!", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        // Tampilkan data yang akan dihapus
        tvHapusNama.text = anggota.nama
        tvHapusJurusan.text = anggota.jurusan
        tvHapusPosisi.text = anggota.posisi

        btnKonfirmasiHapus.setOnClickListener {
            AlertDialog.Builder(this)
                .setTitle("Konfirmasi Hapus")
                .setMessage("Yakin ingin menghapus data \"${anggota.nama}\"?")
                .setPositiveButton("Ya") { _, _ ->
                    val hasil = db.deleteAnggota(anggotaId)
                    if (hasil > 0) {
                        Toast.makeText(this, "Data berhasil dihapus", Toast.LENGTH_SHORT).show()
                        finish()
                    } else {
                        Toast.makeText(this, "Gagal menghapus data!", Toast.LENGTH_SHORT).show()
                    }
                }
                .setNegativeButton("Tidak", null)
                .show()
        }

        btnBatal.setOnClickListener {
            finish()
        }
    }
}
