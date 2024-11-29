package com.example.conexionfirebase

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Switch
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.widget.doOnTextChanged
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import kotlin.properties.Delegates

class MainActivity : AppCompatActivity() {
    private lateinit var usuario:EditText
    private lateinit var contra:EditText
    private lateinit var ingresar:Button
    private lateinit var limpiar:Button
    private lateinit var iniciar:Button
    private lateinit var switchNuevaCuenta: Switch

    private var correo by Delegates.notNull<String>()
    private var passwd by Delegates.notNull<String>()

    private lateinit var mAuth: FirebaseAuth
    private lateinit var googleSignInClient: GoogleSignInClient

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        usuario = findViewById(R.id.correo)
        contra = findViewById(R.id.contra)
        ingresar = findViewById(R.id.ingresar)
        limpiar = findViewById(R.id.limpiar)
        iniciar = findViewById(R.id.google)
        switchNuevaCuenta = findViewById(R.id.switchNuevaCuenta)

        ingresar.isEnabled = false
        //instancia para autenticacion
        mAuth = FirebaseAuth.getInstance()

        // Configura Google Sign-In
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()

        googleSignInClient = GoogleSignIn.getClient(this, gso)

        ingresar.setOnClickListener { validarFormulario() }
        limpiar.setOnClickListener { BorrarValores() }
        iniciar.setOnClickListener { signInWithGoogle() }
        usuario.doOnTextChanged { _, _, _, _ -> existeDominio() }
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    private fun validarFormulario() {
        correo = usuario.text.toString()
        passwd = contra.text.toString()

        if (switchNuevaCuenta.isChecked) {
            // Si el switch está activado, registrar una nueva cuenta
            registrarNuevaCuenta()
        } else {
            // Si el switch está desactivado, iniciar sesión
            validarCorreo()
        }
    }

    private fun validarCorreo(){
        correo = usuario.text.toString()
        passwd = contra.text.toString()
        mAuth.signInWithEmailAndPassword(correo, passwd)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    lanzarFormulario()
                } else {
                    Toast.makeText(this, "Datos incorrectos", Toast.LENGTH_SHORT).show()
                }
            }
        borrarValores()
    }

    private fun BorrarValores(){
        usuario.text.clear()
        contra.text.clear()
        usuario.requestFocus()
    }

    private fun existeDominio(){
        correo = usuario.text.toString()
        val existe = correo.indexOf("@")
        if (existe != -1){
            ingresar.isEnabled = true
        }else{
            ingresar.isEnabled = false
        }
    }
    private fun lanzarFormulario(){
        val formulario = Intent(this, Formulario::class.java)
        startActivity(formulario)
    }
    private fun borrarValores(){
        usuario.text = null
        contra.text = null
        usuario.requestFocus()
    }

    // Registrar una nueva cuenta
    private fun registrarNuevaCuenta() {
        if (correo.isNotEmpty() && passwd.isNotEmpty()) {
            mAuth.createUserWithEmailAndPassword(correo, passwd)
                .addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        Toast.makeText(baseContext, "Cuenta registrada exitosamente", Toast.LENGTH_SHORT).show()
                        lanzarFormulario() // Tras el registro exitoso, lanzar el formulario
                    } else {
                        Toast.makeText(baseContext, "Error al registrar la cuenta", Toast.LENGTH_SHORT).show()
                    }
                }
        } else {
            Toast.makeText(this, "Por favor complete ambos campos", Toast.LENGTH_SHORT).show()
        }
        borrarValores()
    }
    private fun signInWithGoogle() {
        googleSignInClient.revokeAccess().addOnCompleteListener {
            val signInIntent = googleSignInClient.signInIntent
            startActivityForResult(signInIntent, RC_SIGN_IN)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == RC_SIGN_IN) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            try {
                val account = task.getResult(ApiException::class.java)!!
                firebaseAuthWithGoogle(account)
            } catch (e: ApiException) {
                Toast.makeText(this, "Error al iniciar sesión con Google", Toast.LENGTH_SHORT).show()
            }
        }
    }
    private fun firebaseAuthWithGoogle(account: GoogleSignInAccount) {
        val credential = GoogleAuthProvider.getCredential(account.idToken, null)
        mAuth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    lanzarFormulario()
                } else {
                    Toast.makeText(this, "Error de autenticación con Google", Toast.LENGTH_SHORT).show()
                }
            }
    }
    companion object {
        private const val RC_SIGN_IN = 9001
    }
}