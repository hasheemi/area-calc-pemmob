package com.example.areacalc

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.ListView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

/**
 * Halaman utama: menampilkan daftar semua anggota UKM Voli ITS.
 */
class MainActivity : AppCompatActivity() {

    private lateinit var db: DatabaseHelper
    private lateinit var adapter: ArrayAdapter<Anggota>
    private lateinit var lvAnggota: ListView
    private lateinit var tvKosong: TextView
    private val daftarAnggota = mutableListOf<Anggota>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        db = DatabaseHelper(this)
        lvAnggota = findViewById(R.id.lvAnggota)
        tvKosong = findViewById(R.id.tvKosong)
        val btnTambah = findViewById<Button>(R.id.btnTambah)

        adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, daftarAnggota)
        lvAnggota.adapter = adapter

        lvAnggota.onItemClickListener =
            AdapterView.OnItemClickListener { _, _, position, _ ->
                adapter.getItem(position)?.let { anggota ->
                    val intent = Intent(this, DetailActivity::class.java)
                    intent.putExtra(DetailActivity.EXTRA_ID, anggota.id)
                    startActivity(intent)
                }
            }

        btnTambah.setOnClickListener {
            startActivity(Intent(this, FormActivity::class.java))
        }
    }

    override fun onResume() {
        super.onResume()
        muatData()
    }

    private fun muatData() {
        daftarAnggota.clear()
        daftarAnggota.addAll(db.getAllAnggota())
        adapter.notifyDataSetChanged()

        if (daftarAnggota.isEmpty()) {
            lvAnggota.visibility = View.GONE
            tvKosong.visibility = View.VISIBLE
        } else {
            lvAnggota.visibility = View.VISIBLE
            tvKosong.visibility = View.GONE
        }
    }
}
