package com.example.areacalc

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity

/**
 * Halaman menu utama / Home.
 * Berisi tombol-tombol menu untuk mengakses fitur CRUD.
 */
class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val btnTambahAnggota = findViewById<Button>(R.id.btnTambahAnggota)
        val btnLihatAnggota = findViewById<Button>(R.id.btnLihatAnggota)
        val btnEditAnggota = findViewById<Button>(R.id.btnEditAnggota)
        val btnHapusAnggota = findViewById<Button>(R.id.btnHapusAnggota)
        val btnTentang = findViewById<Button>(R.id.btnTentang)
        val btnKeluar = findViewById<Button>(R.id.btnKeluar)

        // Tambah Anggota
        btnTambahAnggota.setOnClickListener {
            startActivity(Intent(this, AddActivity::class.java))
        }

        // Lihat Anggota
        btnLihatAnggota.setOnClickListener {
            val intent = Intent(this, ListActivity::class.java)
            intent.putExtra(ListActivity.EXTRA_MODE, ListActivity.MODE_LIHAT)
            startActivity(intent)
        }

        // Edit Anggota -> buka list dalam mode edit
        btnEditAnggota.setOnClickListener {
            val intent = Intent(this, ListActivity::class.java)
            intent.putExtra(ListActivity.EXTRA_MODE, ListActivity.MODE_EDIT)
            startActivity(intent)
        }

        // Hapus Anggota -> buka list dalam mode hapus
        btnHapusAnggota.setOnClickListener {
            val intent = Intent(this, ListActivity::class.java)
            intent.putExtra(ListActivity.EXTRA_MODE, ListActivity.MODE_HAPUS)
            startActivity(intent)
        }

        // Tentang Aplikasi
        btnTentang.setOnClickListener {
            AlertDialog.Builder(this)
                .setTitle("Tentang Aplikasi")
                .setMessage(
                    "Aplikasi Pendaftaran Anggota UKM Voli ITS\n\n" +
                            "Dibuat menggunakan:\n" +
                            "- Kotlin\n" +
                            "- SQLite (SQLiteOpenHelper)\n" +
                            "- RecyclerView\n\n" +
                            "Versi 1.0"
                )
                .setPositiveButton("Tutup", null)
                .show()
        }

        // Keluar
        btnKeluar.setOnClickListener {
            AlertDialog.Builder(this)
                .setTitle("Konfirmasi")
                .setMessage("Yakin ingin keluar dari aplikasi?")
                .setPositiveButton("Ya") { _, _ ->
                    finishAffinity()
                }
                .setNegativeButton("Tidak", null)
                .show()
        }
    }
}
