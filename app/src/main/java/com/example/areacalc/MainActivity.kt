package com.example.areacalc

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val etPanjang = findViewById<EditText>(R.id.etPanjang)
        val etLebar = findViewById<EditText>(R.id.etLebar)
        val btnHitung = findViewById<Button>(R.id.btnHitung)
        val tvHasil = findViewById<TextView>(R.id.tvHasil)

        btnHitung.setOnClickListener {
            val panjangText = etPanjang.text.toString()
            val lebarText = etLebar.text.toString()

            if (panjangText.isEmpty() || lebarText.isEmpty()) {
                Toast.makeText(this, "Masukkan panjang dan lebar", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val panjang = panjangText.toDouble()
            val lebar = lebarText.toDouble()
            val luas = panjang * lebar

            tvHasil.text = "Luas: $luas"
        }
    }
}
