package com.example.ubuexoworks

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.IBinder
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class RecordarClave : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.recordar_clave_layout)

        val btnEnviarCorreo = findViewById<TextView>(R.id.btn_enviarCorreo)
        btnEnviarCorreo.setOnClickListener {
            var recipient = "alexfnr96@gmail.com"
            var subject = "Cambio de contraseña"
            var message = "Esta es tu contraseña:"

            sendEmail(recipient,subject,message)
        }
    }

    private fun sendEmail(recipient: String, subject: String, message: String) {
        val mIntent = Intent(Intent.ACTION_SEND)
        mIntent.data = Uri.parse("mailto:")
        mIntent.type = "text/plain"

        mIntent.putExtra(Intent.EXTRA_EMAIL, arrayOf(recipient))
        mIntent.putExtra(Intent.EXTRA_SUBJECT, arrayOf(subject))
        mIntent.putExtra(Intent.EXTRA_TEXT, arrayOf(message))

        try {
        } catch(e: Exception) {
            Toast.makeText(this, e.message, Toast.LENGTH_SHORT).show()
        }

    }

    fun irALogin(view: View) {
        val intent = Intent(this, Login::class.java)
        startActivity(intent)
        overridePendingTransition(R.anim.fade_in, R.anim.fade_out)
        finish()
    }
}