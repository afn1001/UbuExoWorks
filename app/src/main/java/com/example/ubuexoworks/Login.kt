package com.example.ubuexoworks

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.textfield.TextInputLayout
import com.google.gson.Gson
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.converter.scalars.ScalarsConverterFactory


class Login : AppCompatActivity(), AdapterView.OnItemSelectedListener {

    private lateinit var retrofit: Retrofit
    private lateinit var service: ApiService
    private lateinit var user: Usuario
    private var email: String = ""
    private var password: String = ""
    private lateinit var fallo: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.login_layout)

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

        //Creamos el servicio
        service = createApiService()

        //Lógica para realizar el login
        val emailInput = findViewById<EditText>(R.id.et_correo)
        val passwordInput = findViewById<EditText>(R.id.et_contraseña)

        val loginButton = findViewById<Button>(R.id.btn_login)
        loginButton.setOnClickListener {
            email = emailInput.text.toString().trim()
            password = passwordInput.text.toString().trim()
            if(email.isNotEmpty()) {
                if(password.isNotEmpty()) {
                    executeLogin(email, password)
                } else {
                    Toast.makeText(this, "Contraseña vacía", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(this, "Email vacío", Toast.LENGTH_SHORT).show()
            }
        }

        //Animaciones
        val emailLayout = findViewById<TextInputLayout>(R.id.et_correoLayout)
        emailLayout.translationX = 1000f
        emailLayout.alpha = 0f
        emailLayout.animate().apply {
            duration = 1000
            startDelay = 300
            translationX(0f)
            alpha(1f)

        }.start()

        val passwordLayout = findViewById<TextInputLayout>(R.id.et_contraseñaLayout)
        passwordLayout.translationX = 1000f
        passwordLayout.alpha = 0f
        passwordLayout.animate().apply {
            duration = 1000
            startDelay = 500
            translationX(0f)
            alpha(1f)

        }.start()

        loginButton.translationX = 1000f
        loginButton.alpha = 0f
        loginButton.animate().apply {
            duration = 1000
            startDelay = 700
            translationX(0f)
            alpha(1f)

        }.start()

        val recuperarClave = findViewById<TextInputLayout>(R.id.til_recuperar_clave)
        recuperarClave.translationX = 1000f
        recuperarClave.alpha = 0f
        recuperarClave.animate().apply {
            duration = 1000
            startDelay = 700
            translationX(0f)
            alpha(1f)

        }.start()


    }

    private fun executeLogin(email: String, password: String) {
        val credenciales = Credenciales(email, password)

        val call = service.login(credenciales)

        call.enqueue(object : Callback<String> {
            override fun onResponse(call: Call<String>, response: Response<String>) {

                if(response.isSuccessful && response.body() != null) {
                    //Intentamos mapear el json a la clase Usuario
                    try {
                        val jsonUser = JSONObject(response.body()!!)
                        val jsonId = jsonUser.optString("token")

                        if(jsonId.equals("OK")) {
                            irAMain()
                        }
                    } catch (e: Exception) {
                        Log.d("login", e.toString())
                        Toast.makeText(this@Login, response.body(), Toast.LENGTH_SHORT).show()
                    }
                } else {
                    fallo = findViewById(R.id.txt_falloClave)
                    fallo.setText("Credenciales inválidas")
                }
            }


            override fun onFailure(call: Call<String>, t: Throwable) {
                Log.d("login", t.toString())
            }
        })
    }

    private fun createApiService() : ApiService {
        retrofit = Retrofit.Builder()
            .baseUrl("https://miubuapp.herokuapp.com/")
            .addConverterFactory(ScalarsConverterFactory.create())
            .addConverterFactory(GsonConverterFactory.create())
            .build()
        return retrofit.create(ApiService::class.java)
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
    }

    fun irArecordarClave(view: View) {
        val intent = Intent(this, RecordarClave::class.java)
        startActivity(intent)
        overridePendingTransition(R.anim.fade_in, R.anim.fade_out)
    }

    fun irAMain() {
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
    }

    private fun validarCredenciales() : Boolean {
        val correo = findViewById<EditText>(R.id.et_correo)
        val esCorrectoElCorreo = correo.text.toString().equals("ubu")
        val contraseña = findViewById<EditText>(R.id.et_contraseña)
        val esCorrectaLaContraseña = contraseña.text.toString().equals("ppp")
        return esCorrectoElCorreo && esCorrectaLaContraseña
    }

}