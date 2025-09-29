package com.rewardsbot

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.rewardsbot.service.FloatingButtonService
import com.rewardsbot.service.SearchService
import com.rewardsbot.ui.theme.MicrosoftRewardsBotTheme
import com.rewardsbot.viewmodel.MainViewModel

class MainActivity : ComponentActivity() {
    
    private val overlayPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        // Permission result handled in ViewModel
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MicrosoftRewardsBotTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    MainScreen()
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(viewModel: MainViewModel = viewModel()) {
    val context = LocalContext.current
    val searchCount by viewModel.searchCount.collectAsState()
    val isRunning by viewModel.isRunning.collectAsState()
    val hasOverlayPermission by viewModel.hasOverlayPermission.collectAsState()
    val hasAccessibilityPermission by viewModel.hasAccessibilityPermission.collectAsState()
    
    LaunchedEffect(Unit) {
        viewModel.checkPermissions(context)
    }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Header
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer
            )
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Microsoft Rewards Bot",
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Automatize suas pesquisas do Microsoft Rewards",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
        }
        
        // Search Count Input
        Card(
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(20.dp)
            ) {
                Text(
                    text = "Configurações",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
                
                OutlinedTextField(
                    value = searchCount.toString(),
                    onValueChange = { value ->
                        value.toIntOrNull()?.let { count ->
                            if (count > 0 && count <= 100) {
                                viewModel.updateSearchCount(count)
                            }
                        }
                    },
                    label = { Text("Número de pesquisas") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth(),
                    supportingText = { Text("Entre 1 e 100 pesquisas") }
                )
            }
        }
        
        // Permissions Status
        if (!hasOverlayPermission || !hasAccessibilityPermission) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer
                )
            ) {
                Column(
                    modifier = Modifier.padding(20.dp)
                ) {
                    Text(
                        text = "Permissões Necessárias",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onErrorContainer,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )
                    
                    if (!hasOverlayPermission) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Sobreposição de tela",
                                color = MaterialTheme.colorScheme.onErrorContainer,
                                modifier = Modifier.weight(1f)
                            )
                            Button(
                                onClick = {
                                    requestOverlayPermission(context)
                                }
                            ) {
                                Text("Habilitar")
                            }
                        }
                    }
                    
                    if (!hasAccessibilityPermission) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Serviço de acessibilidade",
                                color = MaterialTheme.colorScheme.onErrorContainer,
                                modifier = Modifier.weight(1f)
                            )
                            Button(
                                onClick = {
                                    requestAccessibilityPermission(context)
                                }
                            ) {
                                Text("Habilitar")
                            }
                        }
                    }
                }
            }
        }
        
        // Start/Stop Button
        FilledTonalButton(
            onClick = {
                if (isRunning) {
                    stopSearchService(context)
                    viewModel.setRunning(false)
                } else {
                    if (hasOverlayPermission && hasAccessibilityPermission) {
                        startSearchService(context, searchCount)
                        viewModel.setRunning(true)
                    }
                }
            },
            enabled = hasOverlayPermission && hasAccessibilityPermission,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
        ) {
            Icon(
                imageVector = if (isRunning) Icons.Default.Stop else Icons.Default.PlayArrow,
                contentDescription = null,
                modifier = Modifier.padding(end = 8.dp)
            )
            Text(
                text = if (isRunning) "Parar Pesquisas" else "Iniciar Pesquisas",
                style = MaterialTheme.typography.titleMedium
            )
        }
        
        // Status Info
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = if (isRunning) 
                    MaterialTheme.colorScheme.secondaryContainer 
                else 
                    MaterialTheme.colorScheme.surfaceVariant
            )
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = if (isRunning) "🤖 Bot Ativo" else "⏸️ Bot Parado",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = if (isRunning) 
                        "O bot está executando pesquisas automaticamente" 
                    else 
                        "Pressione 'Iniciar Pesquisas' para começar",
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
        }
    }
}

private fun requestOverlayPermission(context: Context) {
    val intent = Intent(
        Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
        Uri.parse("package:${context.packageName}")
    )
    context.startActivity(intent)
}

private fun requestAccessibilityPermission(context: Context) {
    val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)
    context.startActivity(intent)
}

private fun startSearchService(context: Context, searchCount: Int) {
    val serviceIntent = Intent(context, SearchService::class.java)
    serviceIntent.putExtra("search_count", searchCount)
    context.startService(serviceIntent)
    
    val floatingIntent = Intent(context, FloatingButtonService::class.java)
    context.startService(floatingIntent)
}

private fun stopSearchService(context: Context) {
    val serviceIntent = Intent(context, SearchService::class.java)
    context.stopService(serviceIntent)
    
    val floatingIntent = Intent(context, FloatingButtonService::class.java)
    context.stopService(floatingIntent)
}