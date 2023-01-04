package com.example.ubuexoworks


import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
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
import com.example.ubuexoworks.ClasesDeDatos.Gasto
import com.example.ubuexoworks.ClasesDeDatos.Receipts
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStream
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
    private lateinit var ivPhoto: ImageView


    private lateinit var image : InputImage
    private lateinit var bitmap : Bitmap

    private var idUsuario: String = ""
    private var tokenUsuario: String? = ""

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view: View = inflater.inflate(R.layout.fragment_dietas, container, false)


        resultLauncher =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
                if (result.resultCode == Activity.RESULT_OK) {
                    accionesImagenDeCamara(result.data)
                }
            }


        //Lugar donde guardamos la foto
        ivPhoto = view.findViewById(R.id.imagen)

        //Boton que para sacar fotos
        val fab: FloatingActionButton = view.findViewById(R.id.fab)
        fab.setOnClickListener { view ->
            tomarFotografía()
        }

        //Texto reconocido
        val textoReconocido : TextView = view.findViewById(R.id.text_recognition)
        val etTax : EditText = view.findViewById(R.id.et_tax)
        val etTotal : TextView = view.findViewById(R.id.et_preciototal)

        val etDNI : TextView = view.findViewById(R.id.et_dni)

        //Se obtienen los datos del id de usuario
        val preferences: SharedPreferences = requireActivity().getSharedPreferences("Login", Context.MODE_PRIVATE)
        idUsuario = preferences.getInt("Id", 0).toString()
        //Se obtiene el token para poder realizar llamadas a la Api con seguridad
        tokenUsuario = preferences.getString("Token","")

        //Creamos el servicio
        service = (activity as MainActivity).createApiService()

        val botonReconocerTexto : Button = view.findViewById(R.id.btn_reconocerTexto)
        botonReconocerTexto.setOnClickListener { view ->
            image = InputImage.fromBitmap(bitmap, 0)
            val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)

            recognizer.process(image)
                .addOnSuccessListener { visionText ->
                    //textoReconocido.setText(visionText.text)

                    val resultado = buscarFloat(visionText.text)
                    Toast.makeText(context, resultado.toString(), Toast.LENGTH_SHORT).show()
                    if (resultado == null || resultado.isEmpty())
                    else {
                        val totalF = Collections.max(resultado)
                        val secondLargestF = findSecondLargestFloat(resultado)
                        val total = totalF.toString()
                        val vat =
                            if (secondLargestF == 0.0f) "0" else "%.2f".format(totalF - secondLargestF)
                        val receipts = Receipts(total, vat)

                        etTax.setText(receipts.vat)
                        etTotal.setText(receipts.total)

                    }

                }
                .addOnFailureListener { e ->
                    // Task failed with an exception
                    // ...
                }
        }


        val botonEnviarGasto : Button = view.findViewById(R.id.btn_enviarGasto)
        botonEnviarGasto.setOnClickListener { view ->
            val file = bitmapToFile(bitmap)
            enviarGasto(file)
        }
        return view
    }

    /**
     * Transforma el archivo de imagen Bitmap en un archivo File
     * @param bitmap El bitmap de la imagen obtenida del ticket
     */
    fun bitmapToFile(bitmap: Bitmap): File? {
        val wrapper = ContextWrapper(requireContext())
        var file = wrapper.getDir("Images", Context.MODE_PRIVATE)
        file = File(file,"${UUID.randomUUID()}.jpg")
        val stream: OutputStream = FileOutputStream(file)
        bitmap.compress(Bitmap.CompressFormat.JPEG,25,stream)
        stream.flush()
        stream.close()
        return file
    }


    @RequiresApi(Build.VERSION_CODES.O)
    private fun enviarGasto(file: File?) {
        context?.let { (activity as MainActivity)?.comprobarConexion(it) }
        val fecha = LocalDateTime.now().format(DateTimeFormatter.ofPattern("MM/dd/yyyy"))

        val gasto = Gasto(file, idUsuario, fecha, "ticket","transporte", 1512.5, 262.5, "12345678A", "Cena empresa", "0001")
        Log.d("gasto", gasto.toString())
        val call = service.registrarGasto("Bearer " + tokenUsuario, gasto)

        call.enqueue(object : Callback<String> {
            override fun onResponse(call: Call<String>, response: Response<String>) {

                if(response.isSuccessful && response.body() != null) {
                    //Intentamos mapear el json a la clase Usuario
                    try {
                        val jsonUser = JSONObject(response.body()!!)
                        val token = jsonUser.optString("token")

                        Toast.makeText(context, "Funciona", Toast.LENGTH_SHORT).show()

                        if(token.equals("OK")) {
                            Toast.makeText(context, "Funciona", Toast.LENGTH_SHORT).show()
                        }
                    } catch (e: Exception) {
                        Log.d("registrarGasto", e.toString())
                        Toast.makeText(context, response.body(), Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(context, "Falla", Toast.LENGTH_SHORT).show()
                }
            }


            override fun onFailure(call: Call<String>, t: Throwable) {
                Log.d("login", t.toString())
            }
        })
    }

    private fun findSecondLargestFloat(input: ArrayList<Float>?): Float {
        if (input == null || input.isEmpty() || input.size == 1) return 0.0f
        else {
            try {
                val tempSet = HashSet(input)
                val sortedSet = TreeSet(tempSet)
                return sortedSet.elementAt(sortedSet.size - 2)
            } catch (e: Exception) {
                return 0.0f
            }
        }
    }

    /**
     * Se obtienen los float almacenados de un texto (texto obtenido del ticket)
     * @param texto Texto de la imagen realizada
     */
    fun buscarFloat(texto: String): ArrayList<Float> {
        if (this == null || texto.isEmpty()) return ArrayList<Float>()
        val resultado = ArrayList<Float>()
        val resultadosCoincidentes = Regex(pattern = "[+-]?([0-9]*[.])?[0-9]+").findAll(texto)
        if (resultadosCoincidentes != null)
            for (txt in resultadosCoincidentes) {
                if (txt.value.isFloatAndWhole()) resultado.add(txt.value.toFloat())
            }
        return resultado
    }

    private fun String.isFloatAndWhole() = this.matches("\\d*\\.\\d*".toRegex())

    private fun tomarFotografía() {
        if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.CAMERA) !== PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(requireActivity(), arrayOf(Manifest.permission.CAMERA), 102)
        } else {
            val cameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
            resultLauncher.launch(cameraIntent)
        }
    }

    private fun accionesImagenDeCamara(intent: Intent?) {
        bitmap = intent?.extras?.get("data") as Bitmap
        ivPhoto.setImageBitmap(bitmap)
    }



}