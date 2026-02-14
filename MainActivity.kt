package com.example.smarttipmanager

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.example.smarttipmanager.ui.theme.SmartTipTheme

data class Tip(
    val id: String = "",
    val userId: String = "",
    val bill: Double = 0.0,
    val percent: Double = 0.0,
    val tip: Double = 0.0,
    val total: Double = 0.0
)

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {

            var darkMode by remember { mutableStateOf(false) }

            SmartTipTheme(darkTheme = darkMode) {
                AppNavigation(
                    onToggleTheme = { darkMode = !darkMode }
                )
            }
        }
    }
}

@Composable
fun AppNavigation(onToggleTheme: () -> Unit) {

    val navController = rememberNavController()

    NavHost(navController, startDestination = "login") {

        composable("login") {
            LoginScreen(
                onLogin = { role ->
                    if (role == "admin")
                        navController.navigate("admin")
                    else
                        navController.navigate("user")
                }
            )
        }

        composable("user") {
            UserScreen(onToggleTheme)
        }

        composable("admin") {
            AdminScreen(onToggleTheme)
        }
    }
}

@Composable
fun LoginScreen(onLogin: (String) -> Unit) {

    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    val auth = FirebaseAuth.getInstance()
    val db = FirebaseFirestore.getInstance()

    Column(Modifier.fillMaxSize().padding(24.dp),
        verticalArrangement = Arrangement.Center) {

        Text("Smart Tip Manager", style = MaterialTheme.typography.headlineLarge)

        Spacer(Modifier.height(20.dp))

        OutlinedTextField(email, { email = it }, label = { Text("Email") })
        OutlinedTextField(password, { password = it }, label = { Text("Password") })

        Spacer(Modifier.height(20.dp))

        Button(onClick = {
            auth.signInWithEmailAndPassword(email, password)
                .addOnSuccessListener {
                    val uid = auth.currentUser!!.uid
                    db.collection("users").document(uid).get()
                        .addOnSuccessListener {
                            val role = it.getString("role") ?: "user"
                            onLogin(role)
                        }
                }
        }) {
            Text("Login")
        }
    }
}

@Composable
fun UserScreen(onToggleTheme: () -> Unit) {

    var bill by remember { mutableStateOf("") }
    var percent by remember { mutableStateOf("") }
    var tips by remember { mutableStateOf(listOf<Tip>()) }

    val auth = FirebaseAuth.getInstance()
    val db = FirebaseFirestore.getInstance()
    val uid = auth.currentUser?.uid ?: ""

    LaunchedEffect(Unit) {
        db.collection("tips")
            .whereEqualTo("userId", uid)
            .addSnapshotListener { value, _ ->
                tips = value?.documents?.map {
                    Tip(
                        id = it.id,
                        userId = it.getString("userId") ?: "",
                        bill = it.getDouble("bill") ?: 0.0,
                        percent = it.getDouble("percent") ?: 0.0,
                        tip = it.getDouble("tip") ?: 0.0,
                        total = it.getDouble("total") ?: 0.0
                    )
                } ?: emptyList()
            }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("User Dashboard") },
                actions = {
                    TextButton(onClick = onToggleTheme) {
                        Text("Theme")
                    }
                }
            )
        }
    ) { padding ->

        Column(
            Modifier.padding(padding).padding(16.dp)
        ) {

            OutlinedTextField(bill, { bill = it }, label = { Text("Bill") })
            OutlinedTextField(percent, { percent = it }, label = { Text("Tip %") })

            Spacer(Modifier.height(10.dp))

            Button(onClick = {
                val b = bill.toDoubleOrNull() ?: 0.0
                val p = percent.toDoubleOrNull() ?: 0.0
                val t = b * p / 100
                val total = b + t

                db.collection("tips").add(
                    mapOf(
                        "userId" to uid,
                        "bill" to b,
                        "percent" to p,
                        "tip" to t,
                        "total" to total
                    )
                )
            }) {
                Text("Add Tip")
            }

            Spacer(Modifier.height(20.dp))

            Text("Your Tips", style = MaterialTheme.typography.titleLarge)

            LazyColumn {
                items(tips) { tip ->
                    Card(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp)
                    ) {
                        Column(Modifier.padding(12.dp)) {
                            Text("Bill: ${tip.bill}")
                            Text("Tip: ${tip.tip}")
                            Text("Total: ${tip.total}")

                            Button(onClick = {
                                db.collection("tips").document(tip.id).delete()
                            }) {
                                Text("Delete")
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AdminScreen(onToggleTheme: () -> Unit) {

    var tips by remember { mutableStateOf(listOf<Tip>()) }
    val db = FirebaseFirestore.getInstance()

    LaunchedEffect(Unit) {
        db.collection("tips")
            .addSnapshotListener { value, _ ->
                tips = value?.documents?.map {
                    Tip(
                        id = it.id,
                        userId = it.getString("userId") ?: "",
                        bill = it.getDouble("bill") ?: 0.0,
                        percent = it.getDouble("percent") ?: 0.0,
                        tip = it.getDouble("tip") ?: 0.0,
                        total = it.getDouble("total") ?: 0.0
                    )
                } ?: emptyList()
            }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Admin Dashboard") },
                actions = {
                    TextButton(onClick = onToggleTheme) {
                        Text("Theme")
                    }
                }
            )
        }
    ) { padding ->

        LazyColumn(
            Modifier.padding(padding).padding(16.dp)
        ) {
            items(tips) { tip ->
                Card(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp)
                ) {
                    Column(Modifier.padding(12.dp)) {
                        Text("User: ${tip.userId}")
                        Text("Bill: ${tip.bill}")
                        Text("Tip: ${tip.tip}")
                        Text("Total: ${tip.total}")
                    }
                }
            }
        }
    }
}
