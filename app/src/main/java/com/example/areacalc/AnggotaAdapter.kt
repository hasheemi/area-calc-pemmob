package com.example.areacalc

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

/**
 * Adapter RecyclerView untuk menampilkan daftar anggota.
 * Setiap item memiliki tombol Edit dan Hapus yang mengirim ID anggota.
 */
class AnggotaAdapter(
    private var daftarAnggota: MutableList<Anggota>,
    private val mode: String,
    private val onEditClick: (Anggota) -> Unit,
    private val onHapusClick: (Anggota) -> Unit
) : RecyclerView.Adapter<AnggotaAdapter.AnggotaViewHolder>() {

    class AnggotaViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvItemNama: TextView = itemView.findViewById(R.id.tvItemNama)
        val tvItemJurusan: TextView = itemView.findViewById(R.id.tvItemJurusan)
        val tvItemPosisi: TextView = itemView.findViewById(R.id.tvItemPosisi)
        val btnItemEdit: Button = itemView.findViewById(R.id.btnItemEdit)
        val btnItemHapus: Button = itemView.findViewById(R.id.btnItemHapus)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AnggotaViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_anggota, parent, false)
        return AnggotaViewHolder(view)
    }

    override fun onBindViewHolder(holder: AnggotaViewHolder, position: Int) {
        val anggota = daftarAnggota[position]

        holder.tvItemNama.text = anggota.nama
        holder.tvItemJurusan.text = "Jurusan: ${anggota.jurusan}"
        holder.tvItemPosisi.text = "Posisi: ${anggota.posisi}"

        holder.btnItemEdit.setOnClickListener {
            onEditClick(anggota)
        }

        holder.btnItemHapus.setOnClickListener {
            onHapusClick(anggota)
        }

        // Sesuaikan tombol dengan mode yang dipilih dari menu utama
        when (mode) {
            ListActivity.MODE_EDIT -> {
                holder.btnItemEdit.visibility = View.VISIBLE
                holder.btnItemHapus.visibility = View.GONE
            }
            ListActivity.MODE_HAPUS -> {
                holder.btnItemEdit.visibility = View.GONE
                holder.btnItemHapus.visibility = View.VISIBLE
            }
            else -> {
                holder.btnItemEdit.visibility = View.VISIBLE
                holder.btnItemHapus.visibility = View.VISIBLE
            }
        }
    }

    override fun getItemCount(): Int = daftarAnggota.size

    /**
     * Perbarui seluruh data adapter.
     */
    fun perbaruiData(dataBaru: List<Anggota>) {
        daftarAnggota.clear()
        daftarAnggota.addAll(dataBaru)
        notifyDataSetChanged()
    }
}
