package com.example.areacalc

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import kotlin.math.pow

class CalcActivity : AppCompatActivity() {

    private lateinit var etBilangan1: EditText
    private lateinit var etBilangan2: EditText
    private lateinit var tvHasil: TextView

    private lateinit var btnTambah: Button
    private lateinit var btnKurang: Button
    private lateinit var btnKali: Button
    private lateinit var btnBagi: Button

    private lateinit var btnMod: Button
    private lateinit var btnPangkat: Button
    private lateinit var btnClear: Button
    private lateinit var btnSwap: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_calc)

        etBilangan1 = findViewById(R.id.etBilangan1)
        etBilangan2 = findViewById(R.id.etBilangan2)
        tvHasil = findViewById(R.id.tvHasil)

        btnTambah = findViewById(R.id.btnTambah)
        btnKurang = findViewById(R.id.btnKurang)
        btnKali = findViewById(R.id.btnKali)
        btnBagi = findViewById(R.id.btnBagi)

        btnMod = findViewById(R.id.btnMod)
        btnPangkat = findViewById(R.id.btnPangkat)
        btnClear = findViewById(R.id.btnClear)
        btnSwap = findViewById(R.id.btnSwap)

        // Operasi dasar
        btnTambah.setOnClickListener { hitung { a, b -> a + b } }
        btnKurang.setOnClickListener { hitung { a, b -> a - b } }
        btnKali.setOnClickListener   { hitung { a, b -> a * b } }
        btnBagi.setOnClickListener   {
            hitung { a, b ->
                if (b == 0.0) {
                    Toast.makeText(this, "Tidak bisa dibagi nol!", Toast.LENGTH_SHORT).show()
                    0.0
                } else a / b
            }
        }

        // Operasi tambahan
        btnMod.setOnClickListener {
            hitung { a, b ->
                if (b == 0.0) {
                    Toast.makeText(this, "Modulus nol tidak valid!", Toast.LENGTH_SHORT).show()
                    0.0
                } else a % b
            }
        }

        btnPangkat.setOnClickListener { hitung { a, b -> a.pow(b) } }

        btnClear.setOnClickListener {
            etBilangan1.text.clear()
            etBilangan2.text.clear()
            tvHasil.text = "Hasil: -"
            etBilangan1.requestFocus()
        }

        btnSwap.setOnClickListener {
            val val1 = etBilangan1.text.toString()
            val val2 = etBilangan2.text.toString()
            etBilangan1.setText(val2)
            etBilangan2.setText(val1)
            Toast.makeText(this, "Nilai ditukar", Toast.LENGTH_SHORT).show()
        }
    }

    private fun hitung(operasi: (Double, Double) -> Double) {
        val input1 = etBilangan1.text.toString().trim()
        val input2 = etBilangan2.text.toString().trim()

        if (input1.isEmpty() || input2.isEmpty()) {
            Toast.makeText(this, "Isi kedua bilangan terlebih dahulu", Toast.LENGTH_SHORT).show()
            return
        }

        val a = input1.toDoubleOrNull()
        val b = input2.toDoubleOrNull()

        if (a == null || b == null) {
            Toast.makeText(this, "Input harus berupa angka yang valid", Toast.LENGTH_SHORT).show()
            return
        }

        val hasil = operasi(a, b)
        tvHasil.text = "Hasil: ${formatAngka(hasil)}"
    }

    private fun formatAngka(angka: Double): String {
        return if (angka == angka.toLong().toDouble()) {
            angka.toLong().toString()
        } else {
            "%.4f".format(angka).trimEnd('0').trimEnd('.')
        }
    }
}