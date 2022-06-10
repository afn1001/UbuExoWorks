package com.example.ubuexoworks

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity


class Login : AppCompatActivity(), AdapterView.OnItemSelectedListener {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.login_activity)

        val spinner: Spinner = findViewById(R.id.sp_idiomas)
        // Create an ArrayAdapter using the string array and a default spinner layout
        ArrayAdapter.createFromResource(
            this,
            R.array.idiomas,
            android.R.layout.simple_spinner_item
        ).also { adapter ->
            // Specify the layout to use when the list of choices appears
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            // Apply the adapter to the spinner
            spinner.adapter = adapter
        }

        spinner.onItemSelectedListener = this;

    }

    override fun onItemSelected(parent: AdapterView<*>, view: View?, pos: Int, id: Long) {
        // An item was selected. You can retrieve the selected item using
        // parent.getItemAtPosition(pos)

        if(pos==0) {
            Toast.makeText(this, "Español",Toast.LENGTH_SHORT).show()
        } else if (pos==1) {
            Toast.makeText(this, "Inglés",Toast.LENGTH_SHORT).show()
        }
    }

    override fun onNothingSelected(parent: AdapterView<*>) {
        // Another interface callback
    }

    fun irArecordarClave(view: View) {
        val intent = Intent(this, RecordarClave::class.java)
        startActivity(intent)
        finish()
    }

    fun irAMain(view: View) {
        if(validarCredenciales()) {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish()
        } else {
            val fallo = findViewById<TextView>(R.id.txt_falloClave)
            fallo.setText("Las credenciales no son válidas")
        }
    }

    private fun validarCredenciales() : Boolean {
        val correo = findViewById<EditText>(R.id.et_correo)
        val esCorrectoElCorreo = correo.text.toString().equals("ubu")
        val contraseña = findViewById<EditText>(R.id.et_contraseña)
        val esCorrectaLaContraseña = contraseña.text.toString().equals("ppp")
        return esCorrectoElCorreo && esCorrectaLaContraseña
    }

}