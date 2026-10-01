package com.example.areacalc

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

/**
 * Helper SQLite murni (SQLiteOpenHelper) untuk data Anggota UKM Voli ITS.
 *
 * Nama database : db_ukm_voli
 * Versi         : 1
 * Tabel         : anggota
 *   - id       INTEGER PRIMARY KEY AUTOINCREMENT
 *   - nama     TEXT
 *   - jurusan  TEXT
 *   - posisi   TEXT
 */
class DatabaseHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        const val DATABASE_NAME = "db_ukm_voli"
        const val DATABASE_VERSION = 1

        const val TABLE_NAME = "anggota"
        const val COLUMN_ID = "id"
        const val COLUMN_NAMA = "nama"
        const val COLUMN_JURUSAN = "jurusan"
        const val COLUMN_POSISI = "posisi"

        private const val CREATE_TABLE =
            "CREATE TABLE $TABLE_NAME (" +
                    "$COLUMN_ID INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "$COLUMN_NAMA TEXT, " +
                    "$COLUMN_JURUSAN TEXT, " +
                    "$COLUMN_POSISI TEXT)"
    }

    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL(CREATE_TABLE)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS $TABLE_NAME")
        onCreate(db)
    }

    // ---------------------------------------------------------------------
    // CREATE
    // ---------------------------------------------------------------------
    fun insertAnggota(anggota: Anggota): Long {
        val db = writableDatabase
        val values = ContentValues().apply {
            put(COLUMN_NAMA, anggota.nama)
            put(COLUMN_JURUSAN, anggota.jurusan)
            put(COLUMN_POSISI, anggota.posisi)
        }
        val id = db.insert(TABLE_NAME, null, values)
        db.close()
        return id
    }

    // ---------------------------------------------------------------------
    // READ (semua data)
    // ---------------------------------------------------------------------
    fun getAllAnggota(): List<Anggota> {
        val daftarAnggota = mutableListOf<Anggota>()
        val db = readableDatabase
        val cursor: Cursor = db.query(
            TABLE_NAME,
            arrayOf(COLUMN_ID, COLUMN_NAMA, COLUMN_JURUSAN, COLUMN_POSISI),
            null,
            null,
            null,
            null,
            "$COLUMN_ID DESC"
        )

        if (cursor.moveToFirst()) {
            do {
                daftarAnggota.add(cursorToAnggota(cursor))
            } while (cursor.moveToNext())
        }
        cursor.close()
        db.close()
        return daftarAnggota
    }

    // ---------------------------------------------------------------------
    // READ (berdasarkan id)
    // ---------------------------------------------------------------------
    fun getAnggotaById(id: Int): Anggota? {
        val db = readableDatabase
        val cursor: Cursor = db.query(
            TABLE_NAME,
            arrayOf(COLUMN_ID, COLUMN_NAMA, COLUMN_JURUSAN, COLUMN_POSISI),
            "$COLUMN_ID = ?",
            arrayOf(id.toString()),
            null,
            null,
            null,
            "1"
        )

        var anggota: Anggota? = null
        if (cursor.moveToFirst()) {
            anggota = cursorToAnggota(cursor)
        }
        cursor.close()
        db.close()
        return anggota
    }

    // ---------------------------------------------------------------------
    // UPDATE
    // ---------------------------------------------------------------------
    fun updateAnggota(anggota: Anggota): Int {
        val db = writableDatabase
        val values = ContentValues().apply {
            put(COLUMN_NAMA, anggota.nama)
            put(COLUMN_JURUSAN, anggota.jurusan)
            put(COLUMN_POSISI, anggota.posisi)
        }
        val barisTerdampak = db.update(
            TABLE_NAME,
            values,
            "$COLUMN_ID = ?",
            arrayOf(anggota.id.toString())
        )
        db.close()
        return barisTerdampak
    }

    // ---------------------------------------------------------------------
    // DELETE
    // ---------------------------------------------------------------------
    fun deleteAnggota(id: Int): Int {
        val db = writableDatabase
        val barisTerhapus = db.delete(
            TABLE_NAME,
            "$COLUMN_ID = ?",
            arrayOf(id.toString())
        )
        db.close()
        return barisTerhapus
    }

    // ---------------------------------------------------------------------
    // Helper: konversi cursor menjadi objek Anggota
    // ---------------------------------------------------------------------
    private fun cursorToAnggota(cursor: Cursor): Anggota {
        return Anggota(
            id = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_ID)),
            nama = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_NAMA)) ?: "",
            jurusan = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_JURUSAN)) ?: "",
            posisi = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_POSISI)) ?: ""
        )
    }
}
