package com.example.areacalc

/**
 * Model data Anggota UKM Voli ITS.
 */
data class Anggota(
    var id: Int = 0,
    var nama: String = "",
    var jurusan: String = "",
    var posisi: String = ""
) {
    override fun toString(): String = "$nama - $jurusan"
}
