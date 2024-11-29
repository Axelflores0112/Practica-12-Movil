package com.example.conexionfirebase

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class Formulario : AppCompatActivity() {
    private lateinit var id:EditText
    private lateinit var nom:EditText
    private lateinit var ape:EditText
    private lateinit var corr:EditText
    private lateinit var agregar:Button
    private lateinit var buscar:Button
    private lateinit var actualizar:Button
    private lateinit var borrar:Button
    private lateinit var lista: Button
    private lateinit var cerrar:Button
    private lateinit var contactos:Contacto

    private var coleccion = "contactos"
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_formulario)
        id = findViewById(R.id.id)
        nom = findViewById(R.id.nombre)
        ape = findViewById(R.id.apellido)
        corr = findViewById(R.id.correoForm)
        agregar = findViewById(R.id.agregar)
        buscar = findViewById(R.id.buscar)
        actualizar = findViewById(R.id.editar)
        borrar = findViewById(R.id.eliminar)
        cerrar = findViewById(R.id.cerrarSesion)
        lista = findViewById(R.id.lista)
        contactos = Contacto()

        agregar.setOnClickListener { movimientoContacto("Contacto registrado.") }
        buscar.setOnClickListener { buscarContacto() }
        actualizar.setOnClickListener { movimientoContacto("Contacto actualizado.") }
        borrar.setOnClickListener { borrarContacto() }
        cerrar.setOnClickListener { Logout() }
        lista.setOnClickListener {
            startActivity(Intent(this, Listado::class.java))
        }

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    private fun movimientoContacto(mensaje: String){
        val bdAgenda = FirebaseFirestore.getInstance()

        if(id.text.isNotBlank() && id.text.isNotEmpty() &&
            nom.text.isNotBlank() && nom.text.isNotEmpty() &&
            ape.text. isNotBlank() && ape.text. isNotEmpty() &&
            corr.text.isNotBlank () && corr. text. isNotEmpty()){

            val numero = id.text.toString().toInt()
            val nombre = nom. text. toString()
            val apellidos = ape.text. toString()
            val correo = corr. text. toString()
            val idDocumento = "$numero-$correo"

            bdAgenda.collection(coleccion).document(idDocumento)
                .set(hashMapOf(
                    "numero" to numero,
                    "nombre" to nombre,
                    "apellidos" to apellidos,
                    "correo" to correo
                ))
                .addOnSuccessListener{
                    Toast.makeText(this, mensaje, Toast.LENGTH_SHORT).show()
                    limpiarFormulario()
                }
                .addOnFailureListener{
                    Toast.makeText(this, "Error en la conexión.", Toast.LENGTH_SHORT).show()
                }
        } else {
            Toast.makeText(this, "Faltan datos.", Toast.LENGTH_SHORT).show()
        }
    }

    private fun buscarContacto(){
        val bdAgenda = FirebaseFirestore.getInstance()

        if(id.text.isNotBlank() && id.text.isNotEmpty() &&
            corr.text.isNotBlank() && corr.text.isNotEmpty()) {
            val numero = id.text.toString().toInt()
            val correo = corr.text.toString()
            val idDocumento = "$numero-$correo"

            bdAgenda.collection(coleccion).document(idDocumento)
                .get()
                .addOnSuccessListener { documento ->
                    if(documento.exists()){
                        val contacto = documento.toObject(Contacto::class.java)
                        nom.setText(contacto?.nombre)
                        ape.setText(contacto?.apellido)
                    } else {
                        Toast.makeText(this, "Contacto no encontrado.", Toast.LENGTH_SHORT).show()
                    }
                }
                .addOnFailureListener {
                    Toast.makeText(this, "Error en la conexión.", Toast.LENGTH_SHORT).show()
                }
        } else {
            Toast.makeText(this, "Faltan datos.", Toast.LENGTH_SHORT).show()
        }
    }
    private fun borrarContacto(){
        val bdAgenda = FirebaseFirestore.getInstance()

        if(id.text.isNotBlank() && id.text.isNotEmpty() &&
            corr.text.isNotBlank() && corr.text.isNotEmpty())
        {
            val numero = id.text.toString().toInt()
            val correo = corr.text.toString()
            val idDocumento = "$numero-$correo"

            bdAgenda.collection(coleccion).document(idDocumento)
                .delete()
                .addOnSuccessListener {
                    Toast.makeText(this, "Contacto eliminado.", Toast.LENGTH_SHORT).show()
                    limpiarFormulario()
                }
                .addOnFailureListener {
                    Toast.makeText(this, "Error en la conexión.", Toast.LENGTH_SHORT).show()
                }
        } else {
            Toast.makeText(this, "Faltan datos.", Toast.LENGTH_SHORT).show()
        }
    }

    private fun limpiarFormulario(){
        id.setText("")
        nom.setText("")
        ape.setText("")
        corr.setText("")
    }

    private fun Logout(){
        FirebaseAuth.getInstance().signOut()
        startActivity(Intent(this, MainActivity::class.java))
    }
}