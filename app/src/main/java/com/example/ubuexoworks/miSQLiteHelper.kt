package com.example.ubuexoworks

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class miSQLiteHelper(context: Context) : SQLiteOpenHelper(
    context, "fichajes.db", null, 1) {

    override fun onCreate(db: SQLiteDatabase?) {
        val ordenCreacion = "CREATE TABLE fichajes " +
                "(id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "fecha TEXT, hora TEXT)"
        db!!.execSQL(ordenCreacion)
    }

    override fun onUpgrade(db: SQLiteDatabase?,
                           oldVersion: Int, newVersion: Int) {
        val ordenBorrado = "DROP TABLE IF EXISTS fichajes"
        db!!.execSQL(ordenBorrado)
        onCreate(db)
    }

    fun añadirFichajeSinConexion(fecha: String, hora: String) {
        val datos = ContentValues()
        datos.put("fecha", fecha)
        datos.put("hora", hora)

        val db = this.writableDatabase
        db.insert("fichajes", null, datos)
        db.close()
    }


}