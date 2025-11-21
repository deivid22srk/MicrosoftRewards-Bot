package com.deivid22srk.microsoftrewards;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.deivid22srk.microsoftrewards.adapter.SearchAdapter;
import com.deivid22srk.microsoftrewards.databinding.ActivityMainBinding;
import com.deivid22srk.microsoftrewards.model.SearchItem;
import com.deivid22srk.microsoftrewards.service.FloatingButtonService;
import com.deivid22srk.microsoftrewards.service.SearchAutomationService;
import com.deivid22srk.microsoftrewards.utils.SmartSearchGenerator;
import com.deivid22srk.microsoftrewards.utils.GeminiSearchGenerator;
import com.deivid22srk.microsoftrewards.utils.AppConfig;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private static final int OVERLAY_PERMISSION_REQUEST_CODE = 1000;
    private static final int NOTIFICATION_PERMISSION_REQUEST_CODE = 1001;

    private ActivityMainBinding binding;
    private SearchAdapter searchAdapter;
    private List<SearchItem> searchItems;
    private AppConfig config;
    
    private boolean isAutomationRunning = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        config = AppConfig.getInstance(this);
        
        setupToolbar();
        setupRecyclerView();
        setupClickListeners();
        
        // IA Search Generator integrado - não precisa de inicialização
    }

    private void setupToolbar() {
        setSupportActionBar(binding.toolbar);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_scheduler) {
            openScheduler();
            return true;
        } else if (id == R.id.action_settings) {
            openAdvancedSettings();
            return true;
        } else if (id == R.id.action_about) {
            showAboutDialog();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
    
    private void openScheduler() {
        Intent intent = new Intent(this, SchedulerActivity.class);
        startActivity(intent);
    }
    
    private void openAdvancedSettings() {
        Intent intent = new Intent(this, AdvancedSettingsActivity.class);
        startActivity(intent);
    }
    
    private void showAboutDialog() {
        new AlertDialog.Builder(this)
            .setTitle("🤖 Microsoft Rewards Bot Advanced")
            .setMessage("Versão 2.0 - IA Revolucionária\\n\\n" +
                       "✨ Recursos:\\n" +
                       "• IA tipo ChatGPT com contexto avançado\\n" +
                       "• Configurações totalmente personalizáveis\\n" +
                       "• Suporte a múltiplos navegadores\\n" +
                       "• URLs válidas para Microsoft Rewards\\n" +
                       "• Sistema anti-repetição global\\n\\n" +
                       "Desenvolvido com ❤️ por Capy AI")
            .setPositiveButton("OK", null)
            .setNeutralButton("⚙️ Configurações", (dialog, which) -> openAdvancedSettings())
            .show();
    }

    private void setupRecyclerView() {
        searchItems = new ArrayList<>();
        searchAdapter = new SearchAdapter(searchItems);
        binding.searchesRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        binding.searchesRecyclerView.setAdapter(searchAdapter);
    }

    private void setupClickListeners() {
        binding.generateButton.setOnClickListener(v -> generateSearches());
        binding.startButton.setOnClickListener(v -> startAutomation());
        binding.stopButton.setOnClickListener(v -> stopAutomation());
    }

    private void generateSearches() {
        String countText = binding.searchCountEditText.getText().toString().trim();
        
        if (countText.isEmpty()) {
            binding.searchCountInputLayout.setError("Digite um número");
            return;
        }
        
        int count;
        try {
            count = Integer.parseInt(countText);
            if (count < 1 || count > 100) {
                binding.searchCountInputLayout.setError("Entre 1 e 100 pesquisas");
                return;
            }
        } catch (NumberFormatException e) {
            binding.searchCountInputLayout.setError("Número inválido");
            return;
        }
        
        binding.searchCountInputLayout.setError(null);
        
        // Mostrar loading
        binding.generateButton.setEnabled(false);
        binding.generateButton.setText("Gerando...");
        
        // Limpar pesquisas anteriores
        searchItems.clear();
        searchAdapter.notifyDataSetChanged();
        
        // Verificar modo de geração
        AppConfig.SearchGenerationMode mode = config.getSearchGenerationMode();
        boolean isOnlineMode = mode == AppConfig.SearchGenerationMode.ONLINE_GEMINI && config.hasValidGeminiApiKey();
        
        if (isOnlineMode) {
            // Geração online com Gemini AI
            generateSearchesWithGemini(count);
        } else {
            // Geração offline padrão
            generateSearchesOffline(count);
        }
    }
    
    private void generateSearchesWithGemini(int count) {
        GeminiSearchGenerator.generateSearchesWithGemini(count, this, config.getGeminiApiKey(), 
            new GeminiSearchGenerator.OnSearchGeneratedListener() {
                @Override
                public void onSuccess(List<SearchItem> searches) {
                    runOnUiThread(() -> {
                        onSearchesGenerated(searches, "🤖 Gemini 2.5 Flash");
                    });
                }
                
                @Override
                public void onError(String errorMessage) {
                    runOnUiThread(() -> {
                        android.util.Log.w("MainActivity", "Falha no Gemini, usando offline: " + errorMessage);
                        Toast.makeText(MainActivity.this, "⚠️ Falha no Gemini, usando geração local", Toast.LENGTH_SHORT).show();
                        generateSearchesOffline(count);
                    });
                }
            });
    }
    
    private void generateSearchesOffline(int count) {
        // Executar em thread separada para não bloquear UI
        new Thread(() -> {
            try {
                List<SearchItem> generatedSearches;
                
                AppConfig.AIMode aiMode = config.getAIMode();
                switch (aiMode) {
                    case CHATGPT:
                        generatedSearches = SmartSearchGenerator.generateOfflineIntelligentSearches(count, this);
                        break;
                    case ADVANCED:
                    case CUSTOM:
                        generatedSearches = SmartSearchGenerator.generateOfflineIntelligentSearches(count, this);
                        break;
                    default:
                        generatedSearches = SmartSearchGenerator.generateSmartSearches(count);
                        break;
                }
                
                runOnUiThread(() -> {
                    onSearchesGenerated(generatedSearches, "💻 Local");
                });
                
            } catch (Exception e) {
                runOnUiThread(() -> {
                    binding.generateButton.setEnabled(true);
                    binding.generateButton.setText("Gerar Pesquisas");
                    Toast.makeText(MainActivity.this, "❌ Erro na geração: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
            }
        }).start();
    }
    
    private void onSearchesGenerated(List<SearchItem> generatedSearches, String generationMode) {
        searchItems.clear();
        searchItems.addAll(generatedSearches);
        
        searchAdapter.notifyDataSetChanged();
        binding.searchesCard.setVisibility(View.VISIBLE);
        binding.startButton.setEnabled(true);
        
        // Restaurar botão
        binding.generateButton.setEnabled(true);
        binding.generateButton.setText("Gerar Pesquisas");
        
        // Mostrar preview das configurações
        String configPreview = String.format(
            "✅ %d pesquisas geradas\n🔧 Modo: %s\n⏰ Intervalo: %ds\n📱 Browser: %s", 
            generatedSearches.size(), 
            generationMode,
            config.getSearchInterval(),
            config.getBrowserApp().getDisplayName()
        );
        
        Toast.makeText(this, configPreview, Toast.LENGTH_LONG).show();
    }

    private void startAutomation() {
        if (searchItems.isEmpty()) {
            Toast.makeText(this, "Gere pesquisas primeiro", Toast.LENGTH_SHORT).show();
            return;
        }

        // Verificar permissões necessárias
        if (!hasOverlayPermission()) {
            requestOverlayPermission();
            return;
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) 
                != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, 
                    new String[]{Manifest.permission.POST_NOTIFICATIONS}, 
                    NOTIFICATION_PERMISSION_REQUEST_CODE);
                return;
            }
        }

        // Iniciar serviços
        startAutomationServices();
    }

    private void startAutomationServices() {
        isAutomationRunning = true;
        updateUI();
        
        // Iniciar serviço de botão flutuante
        Intent floatingIntent = new Intent(this, FloatingButtonService.class);
        floatingIntent.putExtra("searchItems", new ArrayList<>(searchItems));
        startService(floatingIntent);
        
        // Iniciar serviço de automação avançado
        Intent automationIntent = new Intent(this, SearchAutomationService.class);
        automationIntent.putExtra("searchItems", new ArrayList<>(searchItems));
        startService(automationIntent);
        
        String startMessage = String.format(
            "🚀 Automação iniciada!\\n⚙️ Config: %s | %ds | %s", 
            config.getAIMode().getDisplayName(),
            config.getSearchInterval(),
            config.getBrowserApp().getDisplayName()
        );
        
        Toast.makeText(this, startMessage, Toast.LENGTH_LONG).show();
    }

    private void stopAutomation() {
        isAutomationRunning = false;
        updateUI();
        
        // Parar serviços
        stopService(new Intent(this, FloatingButtonService.class));
        stopService(new Intent(this, SearchAutomationService.class));
        
        Toast.makeText(this, "🛑 Automação interrompida", Toast.LENGTH_SHORT).show();
    }

    private void updateUI() {
        if (isAutomationRunning) {
            binding.progressCard.setVisibility(View.VISIBLE);
            binding.startButton.setEnabled(false);
            binding.stopButton.setEnabled(true);
            binding.generateButton.setEnabled(false);
            binding.searchCountEditText.setEnabled(false);
        } else {
            binding.progressCard.setVisibility(View.GONE);
            binding.startButton.setEnabled(!searchItems.isEmpty());
            binding.stopButton.setEnabled(false);
            binding.generateButton.setEnabled(true);
            binding.searchCountEditText.setEnabled(true);
        }
    }

    private boolean hasOverlayPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            return Settings.canDrawOverlays(this);
        }
        return true;
    }

    private void requestOverlayPermission() {
        new AlertDialog.Builder(this)
            .setTitle("🔒 Permissão de Overlay")
            .setMessage("O app precisa de permissão para exibir elementos sobre outros apps para mostrar o progresso durante a automação.")
            .setPositiveButton("Conceder", (dialog, which) -> {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, 
                        Uri.parse("package:" + getPackageName()));
                    startActivityForResult(intent, OVERLAY_PERMISSION_REQUEST_CODE);
                }
            })
            .setNegativeButton("Cancelar", null)
            .setCancelable(false)
            .show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        
        if (requestCode == OVERLAY_PERMISSION_REQUEST_CODE) {
            if (hasOverlayPermission()) {
                startAutomation();
            } else {
                Toast.makeText(this, "⚠️ Permissão necessária para usar o botão flutuante", Toast.LENGTH_LONG).show();
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        
        if (requestCode == NOTIFICATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permissão de notificação concedida, verificar overlay
                if (hasOverlayPermission()) {
                    startAutomationServices();
                } else {
                    requestOverlayPermission();
                }
            } else {
                Toast.makeText(this, "⚠️ Permissão de notificação recomendada para melhor funcionamento", Toast.LENGTH_LONG).show();
                // Mesmo sem notificação, permitir continuar se tiver overlay
                if (hasOverlayPermission()) {
                    startAutomationServices();
                } else {
                    requestOverlayPermission();
                }
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (isAutomationRunning) {
            stopAutomation();
        }
    }
}