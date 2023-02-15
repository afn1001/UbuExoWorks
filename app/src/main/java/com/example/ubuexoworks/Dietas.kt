package com.example.ubuexoworks


import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.util.Base64
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentManager
import com.example.awesomedialog.*
import com.example.ubuexoworks.ClasesDeDatos.Gasto
import com.example.ubuexoworks.ClasesDeDatos.Receipts
import com.example.ubuexoworks.Dialogs.ImagenDialog
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import kotlinx.serialization.descriptors.StructureKind
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.ByteArrayOutputStream
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.*
import kotlin.collections.ArrayList

/**
 * Clase de la pestaña "Dietas" que sirve para realizar el escaneo de tickets y su posterior envío
 * @author Alejandro Fraga Neila
 */
class Dietas : Fragment() {
    private lateinit var service: ApiService

    private lateinit var resultLauncher: ActivityResultLauncher<Intent>
    private lateinit var ivImagen: ImageView


    private lateinit var image : InputImage
    private lateinit var bitmap : Bitmap

    private var idUsuario: String = ""
    private var tokenUsuario: String? = ""

    private lateinit var etIva: EditText
    private lateinit var etTotal: EditText
    private lateinit var etDni: EditText
    private lateinit var etRazonSocial: EditText
    private lateinit var etTipoTicket: EditText
    private lateinit var etDescripcion: EditText
    private lateinit var etNumeroTicket: EditText

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view: View = inflater.inflate(R.layout.fragment_dietas, container, false)


        resultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
                if (result.resultCode == Activity.RESULT_OK) {
                    accionesCamara(result.data)
                }
            }


        //Se comprueba si hay imagen y, si hay, se envía para poder ampliarla
        ivImagen = view.findViewById(R.id.iw_imagen)
        ivImagen.setOnClickListener {
            if(ivImagen.drawable != null) {
                //Se pasa el texto en Base64 de la imagen
                val sp = context?.getSharedPreferences("Ubicacion", Context.MODE_PRIVATE)
                val ed = sp?.edit()
                ed?.putString("Imagen", encodeImage(bitmap))
                ed?.commit()

                val fragmentActivity = context as FragmentActivity
                val fragmentManager: FragmentManager = fragmentActivity.supportFragmentManager
                ImagenDialog().show(fragmentManager, "UbicacionFragment")
            }
        }

        //Boton que para sacar fotos
        val fab: FloatingActionButton = view.findViewById(R.id.fab)
        fab.setOnClickListener { view ->
            tomarFotografía()
        }

        //Obtenemos todos los EditText
        etIva = view.findViewById(R.id.et_tax)
        etTotal = view.findViewById(R.id.et_total)
        etDni = view.findViewById(R.id.et_dni)
        etRazonSocial = view.findViewById(R.id.et_razonSocial)
        etTipoTicket = view.findViewById(R.id.et_tipoTicket)
        etDescripcion = view.findViewById(R.id.et_descripcion)
        etNumeroTicket = view.findViewById(R.id.et_numeroTicket)


        //Se obtienen los datos del id de usuario
        val preferences: SharedPreferences = requireActivity().getSharedPreferences("Login", Context.MODE_PRIVATE)
        idUsuario = preferences.getInt("Id", 0).toString()
        //Se obtiene el token para poder realizar llamadas a la Api con seguridad
        tokenUsuario = preferences.getString("Token","")

        //Creamos el servicio
        service = (activity as MainActivity).createApiService()

        //Mediante OCR se obtienen los datos de la imagen y se añaden a los EditText
        val botonReconocerTexto : Button = view.findViewById(R.id.btn_reconocerTexto)
        botonReconocerTexto.setOnClickListener { view ->
            reconocerTexto()
        }


        val botonEnviarGasto : Button = view.findViewById(R.id.btn_enviarGasto)
        botonEnviarGasto.setOnClickListener { view ->
            if(todosLosCamposRellenos()) {
                val file = encodeImage(bitmap)
                enviarGasto(file)
            } else {
                AwesomeDialog.build(requireActivity())
                    .title("Envío de gasto fallido")
                    .body("No se ha podido enviar el gasto porque queda algún campo por completar")
                    .icon(R.drawable.ic_falla)
                    .onPositive("Aceptar") {
                        Log.d("TAG", "positive ")
                    }
            }
        }
        return view
    }

    private fun reconocerTexto() {
        //Si no hay imagen no se puede reconocer el texto
        if(ivImagen.drawable != null) {
            image = InputImage.fromBitmap(bitmap, 0)
            val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
            recognizer.process(image)
                .addOnSuccessListener { visionText ->
                    //Si no reconoce texto en la imagen muestra un mensaje
                    if(visionText.text != "") {
                        val palabrasTicket = visionText.text.split(" ","\n")
                        obtenerTextoQueSigue(palabrasTicket, "CIF:", etDni)
                        obtenerTextoQueSigue(palabrasTicket, "21%", etIva)
                        obtenerTextoQueSigue(palabrasTicket, "Total", etTotal)
                        obtenerTextoQueSigue(palabrasTicket, "núm:", etNumeroTicket)

                    } else {
                        AwesomeDialog.build(requireActivity())
                            .title("No hay texto")
                            .body("No se ha podido reconocer texto en la imagen")
                            .onPositive("Aceptar") {
                                Log.d("TAG", "positive ")
                            }
                    }

                }
        } else {
            AwesomeDialog.build(requireActivity())
                .title("No se ha podido reconocer")
                .body("No se ha podido reconocer texto porque no hay ninguna imagen")
                .onPositive("Aceptar") {
                    Log.d("TAG", "positive ")
                }
        }
    }

    private fun obtenerTextoQueSigue(palabrasTicket: List<String>, palabra: String, editText: EditText) {
        val posicion = palabrasTicket.lastIndexOf(palabra)
        if(posicion !=-1)
            editText.setText(palabrasTicket[posicion + 1])
    }

    private fun encodeImage(bitmap: Bitmap): String {
        val baos = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, baos)
        val b = baos.toByteArray()
        val temp = Base64.encodeToString(b, Base64.DEFAULT)
        Log.d("Codificación", temp)
        return temp
    }


    @RequiresApi(Build.VERSION_CODES.O)
    private fun enviarGasto(file: String) {
        context?.let { (activity as MainActivity)?.comprobarConexion(it) }
        val fecha = LocalDateTime.now().format(DateTimeFormatter.ofPattern("MM/dd/yyyy"))

        val gasto = Gasto(file, idUsuario.toInt(), fecha, etTipoTicket.text.toString(), etTotal.text.toString().toDouble(), etIva.text.toString().toDouble(),
            etDni.text.toString(), etRazonSocial.text.toString(), etDescripcion.text.toString(), etNumeroTicket.text.toString())
        val call = service.registrarGasto("Bearer " + tokenUsuario, gasto)

        call.enqueue(object : Callback<String> {
            override fun onResponse(call: Call<String>, response: Response<String>) {

                if(response.isSuccessful && response.body() != null) {

                    try {
                        AwesomeDialog.build(requireActivity())
                                .title("Gasto enviado")
                                .body("Gasto enviado con éxito")
                                .icon(R.drawable.ic_funciona)
                                .onPositive("Aceptar") {
                                    Log.d("TAG", "positive ")
                                }
                    } catch (e: Exception) {
                        Log.d("registrarGasto", e.toString())
                        Toast.makeText(context, response.body(), Toast.LENGTH_SHORT).show()
                    }
                } else {
                    AwesomeDialog.build(requireActivity())
                        .title("Fallo")
                        .body("Fallo al intentar enviar el gasto")
                        .icon(R.drawable.ic_falla)
                        .onPositive("Aceptar") {
                            Log.d("registraGasto", "positive ")
                        }
                }
            }


            override fun onFailure(call: Call<String>, t: Throwable) {
                Log.d("login", t.toString())
            }
        })
    }

    private fun tomarFotografía() {
        if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.CAMERA) !== PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(requireActivity(), arrayOf(Manifest.permission.CAMERA), 102)
        } else {
            val cameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
            resultLauncher.launch(cameraIntent)
        }
    }

    private fun accionesCamara(intent: Intent?) {
        bitmap = intent?.extras?.get("data") as Bitmap
        ivImagen.setImageBitmap(bitmap)
    }

    private fun todosLosCamposRellenos(): Boolean {
        if(ivImagen.drawable==null || etTipoTicket.text.toString()=="" || etTotal.text.toString().toDouble().equals("") || etIva.text.toString().toDouble().equals("") ||
        etDni.text.toString()=="" || etRazonSocial.text.toString()=="" || etDescripcion.text.toString()=="" || etNumeroTicket.text.toString()=="") {
            return false
        } else {
            return true
        }
    }

}