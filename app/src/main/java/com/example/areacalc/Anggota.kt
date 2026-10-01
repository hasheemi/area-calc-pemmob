package com.example.areacalc

/**
 * Model data Anggota UKM Voli ITS.
 * id       : primary key dari tabel "anggota"
 * nama     : nama anggota
 * jurusan  : jurusan anggota
 * posisi   : posisi bermain (mis. Setter, Libero)
 */
data class Anggota(
    var id: Int = 0,
    var nama: String = "",
    var jurusan: String = "",
    var posisi: String = ""
)
