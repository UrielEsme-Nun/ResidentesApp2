package com.example.residentesapp2

import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.os.StrictMode
import android.os.StrictMode.ThreadPolicy
import android.util.Log
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.residentesapp2.ui.theme.ResidentesApp2Theme
import com.google.zxing.BarcodeFormat
import com.google.zxing.qrcode.QRCodeWriter
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.net.HttpURLConnection
import java.net.URL
import java.time.LocalDate
import android.graphics.Color as AndroidColor
import androidx.compose.ui.graphics.Color as ComposeColor


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val policy = ThreadPolicy.Builder().permitAll().build()
        StrictMode.setThreadPolicy(policy)

        setContent {
            ResidentesApp2Theme {
                AppNavigation()
            }
        }

    }
}

@Composable
fun QRCode(data: String, modifier: Modifier = Modifier) {
    val size = 512
    val bitmap = remember(data) {
        val bits = QRCodeWriter().encode(data, BarcodeFormat.QR_CODE, size, size)
        Bitmap.createBitmap(size, size, Bitmap.Config.RGB_565).apply {
            for (x in 0 until size) {
                for (y in 0 until size) {
                    setPixel(x, y, if (bits[x, y]) AndroidColor.BLACK else AndroidColor.WHITE)
                }
            }
        }
    }

    AndroidView(
        factory = { ImageView(it).apply { setImageBitmap(bitmap) } },
        modifier = modifier.size(200.dp)
    )
}

@Composable
fun LoginScreen(navController: NavHostController) {
    var id by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Acceso a la App",
            style = MaterialTheme.typography.headlineMedium,
            color = ComposeColor.Black
        )

        Spacer(modifier = Modifier.height(24.dp))

        OutlinedTextField(
            value = id,
            onValueChange = { id = it },
            label = { Text("ID", color = ComposeColor.Black) },
            modifier = Modifier.fillMaxWidth(),
            textStyle = LocalTextStyle.current.copy(color = ComposeColor.Black),
            enabled = !isLoading
        )

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Contraseña", color = ComposeColor.Black) },
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth(),
            textStyle = LocalTextStyle.current.copy(color = ComposeColor.Black),
            enabled = !isLoading
        )

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = {
                if (id.isNotBlank() && password.isNotBlank()) {
                    isLoading = true
                    coroutineScope.launch {
                        val result = ApiService.loginResidente(id, password)
                        isLoading = false

                        if (result != null) {
                            // Codifica el objeto como JSON para pasarlo por la ruta
                            val residentJson = Json.encodeToString(result)
                            navController.navigate("home/${Uri.encode(residentJson)}")
                        } else {
                            Toast.makeText(context, "ID o contraseña incorrectos", Toast.LENGTH_LONG).show()
                        }
                    }
                } else {
                    Toast.makeText(context, "Ingresa ID y contraseña", Toast.LENGTH_SHORT).show()
                }
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = !isLoading
        ) {
            if (isLoading) {
                CircularProgressIndicator(color = ComposeColor.White, modifier = Modifier.size(24.dp))
            } else {
                Text("Iniciar sesión")
            }
        }
    }
}

@Composable
fun HomeScreen(user: Resident, navController: NavHostController) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("ID: ${user.id}", style = MaterialTheme.typography.bodyLarge, color = ComposeColor.Black)
        Text("Nombre: ${user.nombre}", style = MaterialTheme.typography.bodyLarge, color = ComposeColor.Black)
        Text("Apellidos: ${user.apellidos}", style = MaterialTheme.typography.bodyLarge, color = ComposeColor.Black)
        Text("Domicilio: ${user.domicilio}", style = MaterialTheme.typography.bodyLarge, color = ComposeColor.Black)
        Text("Teléfono: ${user.telefono}", style = MaterialTheme.typography.bodyLarge, color = ComposeColor.Black)

        Spacer(modifier = Modifier.height(24.dp))

        Text("Código QR del residente:", style = MaterialTheme.typography.titleLarge, color = ComposeColor.Black)
        Spacer(modifier = Modifier.height(16.dp))
        QRCode(data = user.codigoQR)

        Spacer(modifier = Modifier.height(24.dp))
        Button(onClick = { navController.navigate("vehicles/${user.id}") }, modifier = Modifier.fillMaxWidth()) {
            Text("Vehículos")
        }

        Spacer(modifier = Modifier.height(8.dp))
        Button(onClick = { navController.navigate("guests/${user.id}") }, modifier = Modifier.fillMaxWidth()) {
            Text("Invitados")
        }
    }
}

@Composable
fun AppNavigation() {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = "login") {
        composable("login") { LoginScreen(navController) }
        composable("home/{residentJson}") { backStackEntry ->
            val residentJson = backStackEntry.arguments?.getString("residentJson") ?: ""
            val user = Json.decodeFromString<Resident>(Uri.decode(residentJson))
            HomeScreen(user, navController)
        }
        composable("vehicles/{userId}") { backStackEntry ->
            val userId = backStackEntry.arguments?.getString("userId") ?: ""
            VehicleScreen(userId, navController)
        }
        composable("add_vehicle/{userId}") { backStackEntry ->
            val userId = backStackEntry.arguments?.getString("userId") ?: ""
            AddVehicleScreen(userId, navController)
        }
        composable("guests/{userId}") { backStackEntry ->
            val userId = backStackEntry.arguments?.getString("userId") ?: ""
            GuestScreen(userId, navController)
        }
        composable("add_guest/{userId}") { backStackEntry ->
            val userId = backStackEntry.arguments?.getString("userId") ?: ""
            AddGuestScreen(userId, navController)
        }
    }
}

@Serializable
data class Resident(
    val id: String,
    val nombre: String,
    val apellidos: String,
    val domicilio: String,
    val telefono: String,
    val codigoQR: String,
    val contrasena: String? = null  // <- agrega este campo
)

@Composable
fun VehicleScreen(userId: String, navController: NavHostController) {
    val coroutineScope = rememberCoroutineScope()
    var vehicleList by remember { mutableStateOf<List<Vehicle>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }

    LaunchedEffect(userId) {
        isLoading = true
        vehicleList = ApiService.obtenerVehiculos(userId)
        isLoading = false
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Text("Vehículos del usuario $userId", style = MaterialTheme.typography.titleLarge, color = ComposeColor.Black)

        Spacer(modifier = Modifier.height(16.dp))

        if (isLoading) {
            CircularProgressIndicator()
        } else {
            if (vehicleList.isEmpty()) {
                Text("No hay vehículos registrados.", color = ComposeColor.Black)
            } else {
                vehicleList.forEach { vehicle ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        elevation = CardDefaults.cardElevation(4.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text("ID: ${vehicle.id}", color = ComposeColor.Black)
                            Text("Marca: ${vehicle.marca}", color = ComposeColor.Black)
                            Text("Modelo: ${vehicle.modelo}", color = ComposeColor.Black)
                            Text("Placas: ${vehicle.placas}", color = ComposeColor.Black)
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
        Button(onClick = { navController.navigate("add_vehicle/$userId") }, modifier = Modifier.fillMaxWidth()) {
            Text("Agregar vehículo")
        }

        OutlinedButton(onClick = { navController.popBackStack() }, modifier = Modifier.fillMaxWidth()) {
            Text("Regresar")
        }
    }
}

@Serializable
data class Vehicle(
    val id: String,
    val marca: String,
    val modelo: String,
    val placas: String,
    val residenteId: String
)

@Composable
fun AddVehicleScreen(userId: String, navController: NavHostController) {
    var marca by remember { mutableStateOf("") }
    var modelo by remember { mutableStateOf("") }
    var placas by remember { mutableStateOf("") }
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    var isSaving by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Top
    ) {
        Text("Agregar vehículo para $userId", style = MaterialTheme.typography.titleLarge, color = ComposeColor.Black)

        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(
            value = marca,
            onValueChange = { marca = it },
            label = { Text("Marca", color = ComposeColor.Black) },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(
            value = modelo,
            onValueChange = { modelo = it },
            label = { Text("Modelo", color = ComposeColor.Black) },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(
            value = placas,
            onValueChange = { placas = it },
            label = { Text("Placas", color = ComposeColor.Black) },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(24.dp))
        Button(
            onClick = {
                coroutineScope.launch {
                    isSaving = true
                    val newVehicle = Vehicle(
                        id = "VEH_${System.currentTimeMillis()}",
                        marca = marca,
                        modelo = modelo,
                        placas = placas,
                        residenteId = userId
                    )
                    val success = ApiService.agregarVehiculo(newVehicle)
                    isSaving = false
                    if (success) {
                        Toast.makeText(context, "Vehículo agregado", Toast.LENGTH_SHORT).show()
                        navController.popBackStack()
                    } else {
                        Toast.makeText(context, "Error al guardar", Toast.LENGTH_LONG).show()
                    }
                }
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = !isSaving
        ) {
            if (isSaving) {
                CircularProgressIndicator(color = ComposeColor.White, modifier = Modifier.size(24.dp))
            } else {
                Text("Guardar")
            }
        }

        Spacer(modifier = Modifier.height(8.dp))
        OutlinedButton(onClick = { navController.popBackStack() }, modifier = Modifier.fillMaxWidth()) {
            Text("Cancelar")
        }
    }
}

@Composable
fun GuestScreen(userId: String, navController: NavHostController) {
    var guestList by remember { mutableStateOf<List<Guest>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }

    LaunchedEffect(userId) {
        isLoading = true
        guestList = ApiService.obtenerInvitados(userId)
        isLoading = false
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Text("Invitados de $userId", style = MaterialTheme.typography.titleLarge, color = ComposeColor.Black)

        Spacer(modifier = Modifier.height(16.dp))

        if (isLoading) {
            CircularProgressIndicator()
        } else {
            if (guestList.isEmpty()) {
                Text("No hay invitados registrados.", color = ComposeColor.Black)
            } else {
                guestList.forEach { guest ->
                    val backgroundColor = when (guest.tipoInvitacion) {
                        "Permanente" -> ComposeColor(0xFF2196F3)
                        "Temporal" -> {
                            val today = LocalDate.now()
                            val fin = guest.fechaFin?.let { LocalDate.parse(it) }
                            if (fin != null && today.isAfter(fin)) ComposeColor(0xFFF44336) else ComposeColor(0xFF4CAF50)
                        }
                        else -> ComposeColor.LightGray
                    }

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        colors = CardDefaults.cardColors(containerColor = backgroundColor),
                        elevation = CardDefaults.cardElevation(4.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text("ID: ${guest.id}", color = ComposeColor.White)
                            Text("Nombre: ${guest.nombre} ${guest.apellidos}", color = ComposeColor.White)
                            Text("Tipo: ${guest.tipoInvitacion}", color = ComposeColor.White)
                            guest.fechaInicio?.let { Text("Desde: $it", color = ComposeColor.White) }
                            guest.fechaFin?.let { Text("Hasta: $it", color = ComposeColor.White) }
                            Spacer(modifier = Modifier.height(8.dp))
                            guest.codigoQR?.let { qrData ->
                                QRCode(data = qrData)
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
        Button(onClick = { navController.navigate("add_guest/$userId") }, modifier = Modifier.fillMaxWidth()) {
            Text("Agregar invitado")
        }

        OutlinedButton(onClick = { navController.popBackStack() }, modifier = Modifier.fillMaxWidth()) {
            Text("Regresar")
        }
    }
}

@Serializable
data class Guest(
    val id: String,
    val nombre: String,
    val apellidos: String,
    val tipoInvitacion: String,
    val fechaInicio: String,
    val fechaFin: String,
    val residenteId: String,
    val codigoQR: String? = null
)

@Composable
fun AddGuestScreen(userId: String, navController: NavHostController) {
    val generatedId = remember { "GUEST_${System.currentTimeMillis()}" }
    var nombre by remember { mutableStateOf("") }
    var apellidos by remember { mutableStateOf("") }
    var tipoInvitacion by remember { mutableStateOf("Permanente") }
    var fechaInicio by remember { mutableStateOf("") }
    var fechaFin by remember { mutableStateOf("") }

    val mostrarFechas = tipoInvitacion == "Temporal"
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    var isSaving by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Top
    ) {
        Text("Agregar invitado para $userId", style = MaterialTheme.typography.titleLarge, color = ComposeColor.Black)

        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(
            value = nombre,
            onValueChange = { nombre = it },
            label = { Text("Nombre", color = ComposeColor.Black) },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(
            value = apellidos,
            onValueChange = { apellidos = it },
            label = { Text("Apellidos", color = ComposeColor.Black) },
            modifier = Modifier.fillMaxWidth()
        )

        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Text("Tipo de invitación: ", color = ComposeColor.Black)
            Spacer(modifier = Modifier.width(16.dp))
            DropdownMenuTipoInvitacion(tipoActual = tipoInvitacion, onTipoSelected = { tipoInvitacion = it })
        }

        if (mostrarFechas) {
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = fechaInicio,
                onValueChange = { fechaInicio = it },
                label = { Text("Fecha inicio (YYYY-MM-DD)", color = ComposeColor.Black) },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = fechaFin,
                onValueChange = { fechaFin = it },
                label = { Text("Fecha fin (YYYY-MM-DD)", color = ComposeColor.Black) },
                modifier = Modifier.fillMaxWidth()
            )
        }

        Spacer(modifier = Modifier.height(24.dp))
        Button(
            onClick = {
                coroutineScope.launch {
                    isSaving = true
                    val newGuest = Guest(
                        id = generatedId,
                        nombre = nombre,
                        apellidos = apellidos,
                        tipoInvitacion = tipoInvitacion,
                        fechaInicio = if (mostrarFechas) fechaInicio else "",
                        fechaFin = if (mostrarFechas) fechaFin else "",
                        residenteId = userId
                    )
                    val success = ApiService.agregarInvitado(newGuest)
                    isSaving = false
                    if (success) {
                        Toast.makeText(context, "Invitado agregado", Toast.LENGTH_SHORT).show()
                        navController.popBackStack()
                    } else {
                        Toast.makeText(context, "Error al guardar", Toast.LENGTH_LONG).show()
                    }
                }
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = !isSaving
        ) {
            if (isSaving) {
                CircularProgressIndicator(color = ComposeColor.White, modifier = Modifier.size(24.dp))
            } else {
                Text("Guardar")
            }
        }

        Spacer(modifier = Modifier.height(8.dp))
        OutlinedButton(onClick = { navController.popBackStack() }, modifier = Modifier.fillMaxWidth()) {
            Text("Cancelar")
        }
    }
}

object ApiService {
    private const val BASE_URL = "https://ebfa6e76180c.ngrok-free.app"

    private val json = Json { ignoreUnknownKeys = true }

    suspend fun loginResidente(id: String, password: String): Resident? {
        val url = URL("$BASE_URL/api/Residentes/login")
        val body = json.encodeToString(mapOf("id" to id, "password" to password))


        return try {
            val connection = (url.openConnection() as HttpURLConnection).apply {
                requestMethod = "POST"
                doOutput = true
                setRequestProperty("Content-Type", "application/json")
            }

            connection.outputStream.use { it.write(body.toByteArray()) }

            // Logging para ver el estado del login
            Log.d("API", "Login response: ${connection.responseCode} ${connection.responseMessage}")

            if (connection.responseCode == HttpURLConnection.HTTP_OK) {
                val response = connection.inputStream.bufferedReader().use { it.readText() }
                json.decodeFromString<Resident>(response)
            } else {
                null
            }
        } catch (e: Exception) {
            Log.e("API", "Login error", e)
            null
        }
    }

    suspend fun obtenerVehiculos(userId: String): List<Vehicle> {
        val url = URL("$BASE_URL/api/Residentes/$userId/Vehiculos")

        return try {
            val connection = (url.openConnection() as HttpURLConnection).apply {
                requestMethod = "GET"
                setRequestProperty("Accept", "application/json")
            }

            if (connection.responseCode == 200 || connection.responseCode == 201) {
                val response = connection.inputStream.bufferedReader().use { it.readText() }
                json.decodeFromString(response)
            } else emptyList()
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }

    suspend fun agregarVehiculo(vehicle: Vehicle): Boolean {
        val url = URL("$BASE_URL/api/Vehiculos/Sencillo")
        val body = json.encodeToString(vehicle)

        Log.d("DEBUG_JSON", "JSON enviado: $body")


        return try {
            val connection = (url.openConnection() as HttpURLConnection).apply {
                requestMethod = "POST"
                doOutput = true
                setRequestProperty("Content-Type", "application/json")
            }

            connection.outputStream.use { it.write(body.toByteArray()) }

            connection.responseCode == 201
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    suspend fun obtenerInvitados(userId: String): List<Guest> {
        val url = URL("$BASE_URL/api/Residentes/$userId/Invitados")
        return try {
            val connection = (url.openConnection() as HttpURLConnection).apply {
                requestMethod = "GET"
                setRequestProperty("Accept", "application/json")
            }

            if (connection.responseCode == 200 || connection.responseCode == 201) {
                val response = connection.inputStream.bufferedReader().use { it.readText() }
                json.decodeFromString(response)
            } else emptyList()
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }

    suspend fun agregarInvitado(guest: Guest): Boolean {
        val url = URL("$BASE_URL/api/Invitados/Sencillo")
        val body = json.encodeToString(guest)

        Log.d("DEBUG_JSON", "JSON enviado: $body")

        return try {
            val connection = (url.openConnection() as HttpURLConnection).apply {
                requestMethod = "POST"
                doOutput = true
                setRequestProperty("Content-Type", "application/json")
            }

            connection.outputStream.use { it.write(body.toByteArray()) }

            connection.responseCode == 201
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
}

@Composable
fun DropdownMenuTipoInvitacion(tipoActual: String, onTipoSelected: (String) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    val opciones = listOf("Permanente", "Temporal")

    Box {
        OutlinedButton(onClick = { expanded = true }) {
            Text(tipoActual, color = ComposeColor.Black)
        }
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            opciones.forEach { opcion ->
                DropdownMenuItem(
                    text = { Text(opcion, color = ComposeColor.Black) },
                    onClick = {
                        onTipoSelected(opcion)
                        expanded = false
                    }
                )
            }
        }
    }
}