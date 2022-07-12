package com.example.ubuexoworks


import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.os.Bundle
import android.provider.MediaStore
import android.util.Base64
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
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import java.io.ByteArrayOutputStream
import java.util.*
import kotlin.collections.ArrayList


class Dietas : Fragment() {

    private lateinit var resultLauncher: ActivityResultLauncher<Intent>
    private lateinit var ivPhoto: ImageView


    private lateinit var image : InputImage
    private lateinit var bitmap : Bitmap


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

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
        }


        return view
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