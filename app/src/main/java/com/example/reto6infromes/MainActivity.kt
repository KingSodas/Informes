package com.example.reto6infromes

import android.content.Intent
import android.graphics.Color
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import android.net.Uri
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import org.json.JSONArray
import kotlin.math.min

class MainActivity : AppCompatActivity() {

    private lateinit var spinnerCiudad: Spinner
    private lateinit var botonGenerarInforme: Button
    private lateinit var botonExportarPdf: Button
    private lateinit var contenedorInforme: LinearLayout
    private lateinit var textoCiudadSeleccionada: TextView
    private lateinit var textoNumeroOfertas: TextView
    private lateinit var textoSalarioTotal: TextView
    private lateinit var textoSalarioMedio: TextView
    private lateinit var textoSalarioMaximo: TextView

    private lateinit var textoSalarioMinimo: TextView

    private lateinit var textoConclusion: TextView
    private lateinit var barraValencia: ProgressBar
    private lateinit var barraMadrid: ProgressBar
    private lateinit var barraAlicante: ProgressBar

    private lateinit var barraBarcelona: ProgressBar

    private lateinit var textoCantidadValencia: TextView
    private lateinit var textoCantidadMadrid: TextView
    private lateinit var textoCantidadAlicante: TextView

    private lateinit var textoCantidadBarcelona: TextView
    private lateinit var contenedorOfertas: LinearLayout

    private val ofertas = ArrayList<Oferta>()
    private val codigoCrearPdf = 100

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Vincular vistas del XML
        spinnerCiudad = findViewById(R.id.spinnerCiudad)
        botonGenerarInforme = findViewById(R.id.botonGenerarInforme)
        botonExportarPdf = findViewById(R.id.botonExportarPdf)
        contenedorInforme = findViewById(R.id.contenedorInforme)
        textoCiudadSeleccionada = findViewById(R.id.textoCiudadSeleccionada)
        textoNumeroOfertas = findViewById(R.id.textoNumeroOfertas)
        textoSalarioTotal = findViewById(R.id.textoSalarioTotal)
        textoSalarioMedio = findViewById(R.id.textoSalarioMedio)
        textoSalarioMaximo = findViewById(R.id.textoSalarioMaximo)
        textoSalarioMinimo = findViewById(R.id.textoSalarioMinimo)
        textoConclusion = findViewById(R.id.textoConclusion)
        barraValencia = findViewById(R.id.barraValencia)
        barraMadrid = findViewById(R.id.barraMadrid)
        barraAlicante = findViewById(R.id.barraAlicante)
        barraBarcelona = findViewById(R.id.barraBarcelona)
        textoCantidadValencia = findViewById(R.id.textoCantidadValencia)
        textoCantidadMadrid = findViewById(R.id.textoCantidadMadrid)
        textoCantidadAlicante = findViewById(R.id.textoCantidadAlicante)
        textoCantidadBarcelona = findViewById(R.id.textoCantidadBarcelona)
        contenedorOfertas = findViewById(R.id.contenedorOfertas)

        configurarSpinner()
        ofertas.addAll(cargarOfertas())

        // Botón Generar Informe
        botonGenerarInforme.setOnClickListener {
            val ciudadSeleccionada = spinnerCiudad.selectedItem.toString()
            val ofertasFiltradas = filtrarOfertasPorCiudad(ciudadSeleccionada)
            mostrarResultado(ciudadSeleccionada, ofertasFiltradas)
        }

        // Botón Exportar PDF
        botonExportarPdf.setOnClickListener {
            val ciudadSeleccionada = spinnerCiudad.selectedItem.toString()
            val ofertasFiltradas = filtrarOfertasPorCiudad(ciudadSeleccionada)
            mostrarResultado(ciudadSeleccionada, ofertasFiltradas)
            contenedorInforme.post {
                abrirSelectorParaGuardarPdf()
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == codigoCrearPdf && resultCode == RESULT_OK) {
            val uriPdf = data?.data
            if (uriPdf != null) {
                exportarInformePdf(uriPdf)
            }
        }
    }

    private fun configurarSpinner() {
        val ciudades = arrayOf("Todas", "Valencia", "Madrid", "Alicante", "Barcelona")
        val adaptador = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_item,
            ciudades
        )
        adaptador.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerCiudad.adapter = adaptador
    }

    private fun cargarOfertas(): ArrayList<Oferta> {
        val listaOfertas = ArrayList<Oferta>()
        val textoJson = assets.open("ofertas.json").bufferedReader().use { it.readText() }
        val arrayJson = JSONArray(textoJson)

        for (i in 0 until arrayJson.length()) {
            val objetoJson = arrayJson.getJSONObject(i)
            val oferta = Oferta(
                objetoJson.getInt("id"),
                objetoJson.getString("puesto"),
                objetoJson.getString("empresa"),
                objetoJson.getString("ciudad"),
                objetoJson.getInt("salario")
            )
            listaOfertas.add(oferta)
        }
        return listaOfertas
    }

    private fun filtrarOfertasPorCiudad(ciudad: String): List<Oferta> {
        val resultado = if (ciudad == "Todas") {
            ofertas
        } else {
            ofertas.filter { it.ciudad == ciudad }
        }
        return resultado
    }

    private fun mostrarResultado(ciudad: String, ofertasFiltradas: List<Oferta>) {
        val numeroOfertas = ofertasFiltradas.size
        val salarioTotal = ofertasFiltradas.sumOf { it.salario }
        val salarioMedio = if (ofertasFiltradas.isNotEmpty()) {
            ofertasFiltradas.map { it.salario }.average()
        } else {
            0.0
        }
        val salarioMaximo = ofertasFiltradas.maxOfOrNull { it.salario } ?: 0
        val salarioMinimo = ofertasFiltradas.minOfOrNull { it.salario } ?: 0
        val mejorOferta = ofertasFiltradas.maxByOrNull { it.salario }

        textoSalarioMinimo.text = "Salario minimo: ${formatearSalario(salarioMinimo)}"

        if (mejorOferta != null) {
            textoConclusion.text = "Conclusión: La oferta mejor pagada es '${mejorOferta.puesto}' en ${mejorOferta.empresa} (${formatearSalario(mejorOferta.salario)})."
        } else {
            textoConclusion.text = "Conclusión: No hay ofertas para mostrar en este filtro."
        }

        textoCiudadSeleccionada.text = "Ciudad seleccionada: $ciudad"
        textoNumeroOfertas.text = "Numero de ofertas: $numeroOfertas"
        textoSalarioTotal.text = "Salario total: ${formatearSalario(salarioTotal)}"
        textoSalarioMedio.text = "Salario medio: ${formatearSalario(salarioMedio.toInt())}"
        textoSalarioMaximo.text = "Salario maximo: ${formatearSalario(salarioMaximo)}"

        mostrarGrafico(ofertasFiltradas)

        contenedorOfertas.removeAllViews()
        ofertasFiltradas.forEachIndexed { indice, oferta ->
            val textoOferta = TextView(this)
            textoOferta.text = "${indice + 1}. ${oferta.puesto}\n" +
                    "${oferta.empresa} - ${oferta.ciudad} - ${formatearSalario(oferta.salario)}"
            textoOferta.textSize = 16f
            textoOferta.setPadding(0, 0, 0, 24)
            contenedorOfertas.addView(textoOferta)
        }
    }

    private fun abrirSelectorParaGuardarPdf() {
        val ciudadActual = spinnerCiudad.selectedItem.toString()
        val intent = Intent(Intent.ACTION_CREATE_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = "application/pdf"
            putExtra(Intent.EXTRA_TITLE, "informe_$ciudadActual.pdf")
        }
        startActivityForResult(intent, codigoCrearPdf)
    }

    private fun exportarInformePdf(uriPdf: Uri) {
        val documentoPdf = PdfDocument()
        val paginaInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create()
        val paginaPdf = documentoPdf.startPage(paginaInfo)
        val canvas = paginaPdf.canvas

        val pinturaTitulo = Paint().apply {
            color = Color.BLACK
            textSize = 24f
            isFakeBoldText = true
        }

        canvas.drawColor(Color.WHITE)
        canvas.drawText("Informe de ofertas", 32f, 48f, pinturaTitulo)

        val anchoDisponible = 531f
        val altoDisponible = 760f
        val anchoInforme = contenedorInforme.width.toFloat()
        val altoInforme = contenedorInforme.height.toFloat()

        if (anchoInforme > 0f && altoInforme > 0f) {
            val escala = min(anchoDisponible / anchoInforme, altoDisponible / altoInforme)
            canvas.save()
            canvas.translate(32f, 72f)
            canvas.scale(escala, escala)
            contenedorInforme.draw(canvas)
            canvas.restore()
        }

        documentoPdf.finishPage(paginaPdf)

        try {
            contentResolver.openOutputStream(uriPdf)?.use { salida ->
                documentoPdf.writeTo(salida)
                Toast.makeText(this, "PDF guardado correctamente", Toast.LENGTH_LONG).show()
            } ?: Toast.makeText(this, "No se pudo abrir el archivo para guardar", Toast.LENGTH_LONG).show()
        } catch (e: Exception) {
            Toast.makeText(this, "Error al guardar el PDF: ${e.message}", Toast.LENGTH_LONG).show()
        } finally {
            documentoPdf.close()
        }
    }

    private fun mostrarGrafico(ofertasFiltradas: List<Oferta>) {
        val ofertasValencia = ofertasFiltradas.count { it.ciudad == "Valencia" }
        val ofertasMadrid = ofertasFiltradas.count { it.ciudad == "Madrid" }
        val ofertasAlicante = ofertasFiltradas.count { it.ciudad == "Alicante" }
        val ofertasBarcelona = ofertasFiltradas.count { it.ciudad == "Barcelona" }

        val valorMaximo = maxOf(ofertasValencia, ofertasMadrid, ofertasAlicante, ofertasBarcelona, 1)

        barraValencia.max = valorMaximo
        barraMadrid.max = valorMaximo
        barraAlicante.max = valorMaximo
        barraBarcelona.max = valorMaximo

        barraValencia.progress = ofertasValencia
        barraMadrid.progress = ofertasMadrid
        barraAlicante.progress = ofertasAlicante
        barraBarcelona.progress = ofertasBarcelona

        textoCantidadValencia.text = ofertasValencia.toString()
        textoCantidadMadrid.text = ofertasMadrid.toString()
        textoCantidadAlicante.text = ofertasAlicante.toString()
        textoCantidadBarcelona.text = ofertasBarcelona.toString()
    }

    private fun formatearSalario(salario: Int): String {
        return "%,d".format(salario).replace(",", ".") + " €"
    }
}