package com.example.ubuexoworks


import android.animation.AnimatorSet
import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.View
import android.view.ViewTreeObserver
import android.view.animation.AnticipateInterpolator
import android.widget.*
import androidx.annotation.DrawableRes
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import com.example.awesomedialog.*
import com.example.ubuexoworks.ClasesDeDatos.Credenciales
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.converter.scalars.ScalarsConverterFactory
import java.util.*

/**
 * Clase que permite realizar el inicio de sesión, registrar dispositivo, cambiar idioma e ir a recuperar la contraseña
 * @author Alejandro Fraga Neila
 */
class Login : AppCompatActivity(), AdapterView.OnItemSelectedListener {

    private lateinit var retrofit: Retrofit
    private lateinit var service: ApiService
    private var email: String = ""
    private var password: String = ""
    private var myImei: String = ""
    private var jsonId: Int = 0
    private var jsonToken: String = ""
    private lateinit var spinner: Spinner



    @SuppressLint("NewApi")
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.login_layout)

        spinner = findViewById(R.id.sp_idiomas)
        // Se crea un ArrayAdapter usando el array de strings con el idioma y el layout de un spinner simple
        ArrayAdapter.createFromResource(this, R.array.idiomas, android.R.layout.simple_spinner_item).also { adapter ->
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            // Se aplica el adaptador al spinner
            spinner.adapter = adapter
        }

        spinner.onItemSelectedListener = this;
        spinner.setSelection(0)

        //Creamos el servicio
        service = createApiService()

        //Lógica para realizar el login
        val emailInput = findViewById<EditText>(R.id.et_correo)
        val passwordInput = findViewById<EditText>(R.id.et_contraseña)

        //Obtenemos el número identificativo del móvil
        myImei=Settings.Secure.getString(getContentResolver(),
            Settings.Secure.ANDROID_ID)

        val loginButton = findViewById<Button>(R.id.btn_login)
        loginButton.setOnClickListener {
            email = emailInput.text.toString().trim()
            password = passwordInput.text.toString().trim()
            if(email.isNotEmpty()) {
                if(password.isNotEmpty()) {
                    ejecutarLogin(email, password, myImei)
                } else {
                    AwesomeDialog.build(this)
                        .title("Contraseña vacía")
                        .body("No ha introducido la contraseña")
                        .icon(R.drawable.ic_falla)
                        .onPositive("Aceptar") {
                            Log.d("Contraseña", "positive ")
                        }
                }
            } else {
                AwesomeDialog.build(this)
                    .title("Correo vacío")
                    .body("No se ha introducido el correo electrónico")
                    .icon(R.drawable.ic_falla)
                    .onPositive("Aceptar") {
                        Log.d("Correo", "positive ")
                    }
            }
        }

        val registrarButton = findViewById<Button>(R.id.btn_registrarDispositivo)
        registrarButton.setOnClickListener {
            email = emailInput.text.toString().trim()
            password = passwordInput.text.toString().trim()
            if(email.isNotEmpty()) {
                if(password.isNotEmpty()) {
                    registrarDispositivo(email, password, myImei)
                } else {
                    AwesomeDialog.build(this)
                        .title("Contraseña vacía")
                        .body("No ha introducido la contraseña")
                        .icon(R.drawable.ic_falla)
                        .onPositive("Aceptar") {
                            Log.d("Contraseña", "positive ")
                        }
                }
            } else {
                AwesomeDialog.build(this)
                    .title("Correo vacío")
                    .body("No se ha introducido el correo electrónico")
                    .icon(R.drawable.ic_falla)
                    .onPositive("Aceptar") {
                        Log.d("Correo", "positive ")
                    }
            }

        }


    }

    private fun ejecutarLogin(email: String, password: String, imei: String) {
        comprobarConexion(applicationContext)
        val credenciales = Credenciales(email, password, imei)
        val call = service.login(credenciales)

        call.enqueue(object : Callback<String> {
            override fun onResponse(call: Call<String>, response: Response<String>) {

                if(response.isSuccessful && response.body() != null) {
                    //Intentamos mapear el json a la clase Usuario
                    try {
                        val jsonUser = JSONObject(response.body()!!)
                        jsonId = jsonUser.optInt("idUsuario")
                        jsonToken = jsonUser.optString("token")

                        if(jsonId != 0 || jsonToken.isEmpty()) {
                            val sp = getSharedPreferences("Login", Context.MODE_PRIVATE)
                            val ed = sp.edit()
                            ed.putString("Token", jsonToken)
                            ed.putInt("Id", jsonId)
                            ed.commit()

                            irAMain()
                        }
                    } catch (e: Exception) {
                        Log.d("login", e.toString())
                    }
                } else {
                    AwesomeDialog.build(this@Login)
                        .title("Credenciales inválidas")
                        .body("Las credenciales introducidas no coinciden con ninguna almacenada en la base de datos")
                        .icon(R.drawable.ic_falla)
                        .onPositive("Aceptar") {
                            Log.d("Credenciales registrar", "positive ")
                        }
                }
            }


            override fun onFailure(call: Call<String>, t: Throwable) {
                Log.d("login", t.toString())
            }
        })
    }

    private fun registrarDispositivo(email: String, password: String, imei: String) {
        comprobarConexion(applicationContext)
        val credenciales = Credenciales(email, password, imei)
        val call = service.registraDispositivo(credenciales)

        call.enqueue(object : Callback<String> {
            override fun onResponse(call: Call<String>, response: Response<String>) {
                if(response.isSuccessful && response.body() != null) {
                    try {
                        val jsonUser = JSONObject(response.body()!!)
                        Toast.makeText(this@Login, jsonUser.optString("token"), Toast.LENGTH_SHORT).show()

                    } catch (e: Exception) {
                        Log.d("registroDispositivo", e.toString())
                        AwesomeDialog.build(this@Login)
                            .title("Dispositivo registrado")
                            .body("Se ha registrado el dispositivo al usuario")
                            .icon(R.drawable.ic_funciona)
                            .onPositive("Aceptar") {
                                Log.d("Registro", "positive ")
                            }
                    }
                } else {
                    AwesomeDialog.build(this@Login)
                        .title("Fallo en el registro")
                        .body("No se ha podido registrar el dispositivo porque las cedenciales no son correctas")
                        .icon(R.drawable.ic_falla)
                        .onPositive("Aceptar") {
                            Log.d("Registro fallo", "positive ")
                        }
                }
            }


            override fun onFailure(call: Call<String>, t: Throwable) {
                Log.d("registroDispositivo", t.toString())
            }
        })
    }

    /**
     * Permite crear el servicio para conectar con la API
     */
    fun createApiService() : ApiService {
        retrofit = Retrofit.Builder()
            .baseUrl("https://miubuapp.herokuapp.com/")
            .addConverterFactory(ScalarsConverterFactory.create())
            .addConverterFactory(GsonConverterFactory.create())
            .build()
        return retrofit.create(ApiService::class.java)
    }

    /**
     * Permite seleccionar el idioma
     */
    override fun onItemSelected(parent: AdapterView<*>, view: View?, pos: Int, id: Long) {
        if(pos==1) {
            cambiarIdioma(this, "es")
            finish()
            startActivity(intent)
        } else if (pos==2) {
            cambiarIdioma(this, "en")
            finish()
            startActivity(intent)
        }
    }

    /**
     * Permite cambiar el idioma
     */
    fun cambiarIdioma(activity: Activity, langCode: String) {
        val locale = Locale(langCode)
        Locale.setDefault(locale)
        val resources = activity.resources
        val config = resources.configuration
        config.setLocale(locale)
        resources.updateConfiguration(config, resources.displayMetrics)
    }

    override fun onNothingSelected(parent: AdapterView<*>) {
    }

    /**
     * Accede a la actividad para recuperar la contraseña
     */
    fun irARecordarClave(view: View) {
        val intent = Intent(this, RecordarClave::class.java)
        startActivity(intent)
        finish()
    }

    /**
     * Accede a la actividad Main una vez se realiza el login
     */
    fun irAMain() {
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
    }

    /**
     * Comprueba si el dispositivo dispone de conexión a internet
     */
    @SuppressLint("MissingPermission")
    fun comprobarConexion(context: Context) {
        val gestorConexion = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val capacidadesDeRed = gestorConexion.activeNetwork
        val infromacionDeRed = gestorConexion.getNetworkCapabilities(capacidadesDeRed)
        if (infromacionDeRed?.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) == true || infromacionDeRed?.hasTransport(
                NetworkCapabilities.TRANSPORT_CELLULAR) == true) {
        } else {
            AwesomeDialog.build(this)
                .title("Sin conexión")
                .body("No hay conexión a internet")
                .icon(R.drawable.ic_sinconexion)
                .onPositive("Aceptar") {
                    Log.d("TAG", "positive ")
                }
        }
    }

}