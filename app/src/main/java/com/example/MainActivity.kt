package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.auth.authStateFlow
import com.example.auth.configureAppCheck
import com.example.data.AlMaqraRepository
import com.example.ui.AlMaqraAppShell
import com.example.ui.AlMaqraViewModel
import com.example.ui.screens.AuthScreen
import com.example.ui.theme.AlMaqraTheme
import com.google.firebase.Firebase
import com.google.firebase.auth.auth

class MainActivity : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    configureAppCheck(this, intent)
    enableEdgeToEdge()
    setContent {
      AlMaqraRoot()
    }
  }
}

@Composable
fun AlMaqraRoot() {
  val auth = Firebase.auth
  val currentUser by auth.authStateFlow().collectAsStateWithLifecycle(initialValue = auth.currentUser)
  val user = currentUser

  if (user == null) {
    AlMaqraTheme {
      AuthScreen(onAuthSuccess = {})
    }
  } else {
    val context = LocalContext.current.applicationContext
    key(user.uid) {
      val vm: AlMaqraViewModel = viewModel(
        key = user.uid,
        factory = AlMaqraViewModel.Factory(
          repository = AlMaqraRepository(context),
          currentUserId = user.uid,
          displayName = user.displayName,
          email = user.email,
        ),
      )
      AlMaqraAppShell(viewModel = vm)
    }
  }
}
