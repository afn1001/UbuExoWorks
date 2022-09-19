package com.example.ubuexoworks


import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import com.example.ubuexoworks.ClasesDeDatos.Gasto
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.converter.scalars.ScalarsConverterFactory
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.*
import kotlin.collections.ArrayList


class Dietas : Fragment() {
    private lateinit var retrofit: Retrofit
    private lateinit var service: ApiService

    private lateinit var resultLauncher: ActivityResultLauncher<Intent>
    private lateinit var ivPhoto: ImageView


    private lateinit var image : InputImage
    private lateinit var bitmap : Bitmap

    private var idUsuario: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view: View = inflater.inflate(R.layout.fragment_dietas, container, false)


        resultLauncher =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
                if (result.resultCode == Activity.RESULT_OK) {
                    handleCameraImage(result.data)
                }
            }


        //Lugar donde guardamos la foto
        ivPhoto = view.findViewById(R.id.imagen)

        //Boton que para sacar fotos
        val fab: FloatingActionButton = view.findViewById(R.id.fab)
        fab.setOnClickListener { view ->
            takePhoto()
        }

        //Texto reconocido
        val textoReconocido : TextView = view.findViewById(R.id.text_recognition)
        val textoTax : TextView = view.findViewById(R.id.text_tax)
        val textoTotal : TextView = view.findViewById(R.id.text_total)

        val textoDNI : TextView = view.findViewById(R.id.text_dni)

        //Se obtienen los datos del id de usuario
        val preferences: SharedPreferences = requireActivity().getSharedPreferences("Login", Context.MODE_PRIVATE)
        idUsuario = preferences.getInt("Id", 0).toString()

        //Creamos el servicio
        service = createApiService()

        val botonReconocerTexto : Button = view.findViewById(R.id.btn_reconocerTexto)
        botonReconocerTexto.setOnClickListener { view ->
            image = InputImage.fromBitmap(bitmap, 0)
            val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)

            recognizer.process(image)
                .addOnSuccessListener { visionText ->
                    //textoReconocido.setText(visionText.text)

                    val originalResult = findFloat(visionText.text)
                    Toast.makeText(context, originalResult.toString(), Toast.LENGTH_SHORT).show()
                    if (originalResult == null || originalResult.isEmpty())
                    else {
                        val totalF = Collections.max(originalResult)
                        val secondLargestF = findSecondLargestFloat(originalResult)
                        val total = totalF.toString()
                        val vat = if (secondLargestF == 0.0f) "0" else "%.2f".format(totalF - secondLargestF)
                        val receipts = Receipts(total, vat)

                        textoTax.setText(receipts.vat)
                        textoTotal.setText(receipts.total)

                    }

                }
                .addOnFailureListener { e ->
                    // Task failed with an exception
                    // ...
                }

            val botonEnviarGasto : Button = view.findViewById(R.id.btn_enviarGasto)
            botonEnviarGasto.setOnClickListener { view ->
                val file = bitmapToFile(bitmap, "gasto")
                enviarGasto(file)
            }
        }


        return view
    }


    fun bitmapToFile(bitmap: Bitmap, fileNameToSave: String): File? { // File name like "image.png"
        //create a file to write bitmap data
        var file: File? = null
        return try {
            file = File(Environment.getExternalStorageDirectory().toString() + File.separator + fileNameToSave)
            file.createNewFile()

            //Convert bitmap to byte array
            val bos = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.PNG, 0, bos) // YOU can also save it in JPEG
            val bitmapdata = bos.toByteArray()

            //write the bytes in file
            val fos = FileOutputStream(file)
            fos.write(bitmapdata)
            fos.flush()
            fos.close()
            file
        } catch (e: Exception) {
            e.printStackTrace()
            file // it will return null
        }
    }


    @RequiresApi(Build.VERSION_CODES.O)
    private fun enviarGasto(file: File?) {
        val fecha = LocalDateTime.now().format(DateTimeFormatter.ofPattern("MM/dd/yyyy"))

        val gasto = Gasto(file, idUsuario, fecha, "ticket","transporte", 1512.5, 262.5, "12345678A", "Cena empresa", "0001")

        val call = service.registrarGasto(gasto)

        call.enqueue(object : Callback<String> {
            override fun onResponse(call: Call<String>, response: Response<String>) {

                if(response.isSuccessful && response.body() != null) {
                    //Intentamos mapear el json a la clase Usuario
                    try {
                        val jsonUser = JSONObject(response.body()!!)
                        val token = jsonUser.optString("token")


                        if(token.equals("OK")) {
                            Toast.makeText(context, "Funciona", Toast.LENGTH_SHORT).show()
                        }
                    } catch (e: Exception) {
                        Log.d("registrarGasto", e.toString())
                        Toast.makeText(context, response.body(), Toast.LENGTH_SHORT).show()
                    }
                } else {
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

    fun findFloat(texto: String): ArrayList<Float> {
        //get digits from result
        if (this == null || texto.isEmpty()) return ArrayList<Float>()
        val originalResult = ArrayList<Float>()
        val matchedResults = Regex(pattern = "[+-]?([0-9]*[.])?[0-9]+").findAll(texto)
        if (matchedResults != null)
            for (txt in matchedResults) {
                if (txt.value.isFloatAndWhole()) originalResult.add(txt.value.toFloat())
            }
        return originalResult
    }

    private fun String.isFloatAndWhole() = this.matches("\\d*\\.\\d*".toRegex())

    private fun takePhoto() {
        if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.CAMERA) !== PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(requireActivity(), arrayOf(Manifest.permission.CAMERA), 102)
        } else {
            val cameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
            resultLauncher.launch(cameraIntent)
        }
    }

    private fun handleCameraImage(intent: Intent?) {
        bitmap = intent?.extras?.get("data") as Bitmap
        ivPhoto.setImageBitmap(bitmap)
    }

    private fun obtenerDNI(texto: CharSequence): String {
        var dni = ""
        for (i in 0..texto.length-9) {
            val subs = texto.substring(i,i+9)
            if(subs.get(0).isDigit() && subs.get(1).isDigit() && subs.get(2).isDigit() && subs.get(3).isDigit() && subs.get(4).isDigit()
                && subs.get(5).isDigit() && subs.get(6).isDigit() && subs.get(7).isDigit() && subs.get(8).isLetter()) {
                dni = subs
            }
        }
        return dni
    }



}