package com.example.areacalc

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

/**
 * Activity untuk menampilkan daftar anggota (Read) menggunakan RecyclerView.
 * Juga menjadi pintu masuk untuk mode Edit dan Hapus.
 */
class ListActivity : AppCompatActivity() {

    companion object {
        const val EXTRA_MODE = "mode"
        const val MODE_LIHAT = "lihat"
        const val MODE_EDIT = "edit"
        const val MODE_HAPUS = "hapus"
    }

    private lateinit var db: DatabaseHelper
    private lateinit var adapter: AnggotaAdapter
    private lateinit var rvAnggota: RecyclerView
    private lateinit var tvKosong: TextView
    private lateinit var tvJumlahData: TextView

    private var mode: String = MODE_LIHAT

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_list)

        db = DatabaseHelper(this)
        mode = intent.getStringExtra(EXTRA_MODE) ?: MODE_LIHAT

        rvAnggota = findViewById(R.id.rvAnggota)
        tvKosong = findViewById(R.id.tvKosong)
        tvJumlahData = findViewById(R.id.tvJumlahData)
        val btnKembali = findViewById<Button>(R.id.btnKembali)

        // Siapkan RecyclerView
        adapter = AnggotaAdapter(
            daftarAnggota = mutableListOf(),
            mode = mode,
            onEditClick = { anggota ->
                val intent = Intent(this, EditActivity::class.java)
                intent.putExtra(EditActivity.EXTRA_ID, anggota.id)
                startActivity(intent)
            },
            onHapusClick = { anggota ->
                val intent = Intent(this, DeleteActivity::class.java)
                intent.putExtra(DeleteActivity.EXTRA_ID, anggota.id)
                startActivity(intent)
            }
        )

        rvAnggota.layoutManager = LinearLayoutManager(this)
        rvAnggota.adapter = adapter

        btnKembali.setOnClickListener {
            finish()
        }
    }

    override fun onResume() {
        super.onResume()
        // Refresh data setiap kali activity kembali aktif
        muatDataAnggota()
    }

    private fun muatDataAnggota() {
        val daftarAnggota = db.getAllAnggota()
        adapter.perbaruiData(daftarAnggota)

        tvJumlahData.text = "Total: ${daftarAnggota.size} anggota"

        if (daftarAnggota.isEmpty()) {
            rvAnggota.visibility = RecyclerView.GONE
            tvKosong.visibility = TextView.VISIBLE
        } else {
            rvAnggota.visibility = RecyclerView.VISIBLE
            tvKosong.visibility = TextView.GONE
        }
    }
}
