package com.deivid22srk.microsoftrewards;

import android.os.Bundle;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;

import com.deivid22srk.microsoftrewards.databinding.ActivityAdvancedSettingsBinding;
import com.deivid22srk.microsoftrewards.utils.AppConfig;

/**
 * 🛠️ Tela de Configurações Avançadas
 * Interface completa para todas as configurações do app
 */
public class AdvancedSettingsActivity extends AppCompatActivity {
    
    private ActivityAdvancedSettingsBinding binding;
    private AppConfig config;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAdvancedSettingsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        
        config = AppConfig.getInstance(this);
        
        setupToolbar();
        setupTimeSettings();
        setupUrlSettings();
        setupBrowserSettings();
        setupAutomationSettings();
        setupAISettings();
        setupSecuritySettings();
        setupButtons();
        
        loadCurrentSettings();
    }
    
    private void setupToolbar() {
        setSupportActionBar(binding.toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("Configurações Avançadas");
    }
    
    private void setupTimeSettings() {
        // Configurar SeekBars para controles de tempo
        binding.seekBarSearchInterval.setMax(30);
        binding.seekBarSearchInterval.setProgress(config.getSearchInterval());
        binding.seekBarSearchInterval.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (progress < 1) progress = 1;
                binding.textSearchIntervalValue.setText(progress + "s");
            }
            
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}
            
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });
        
        binding.seekBarResultDisplay.setMax(10);
        binding.seekBarResultDisplay.setProgress(config.getResultDisplayTime());
        binding.seekBarResultDisplay.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (progress < 1) progress = 1;
                binding.textResultDisplayValue.setText(progress + "s");
            }
            
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}
            
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });
    }
    
    private void setupUrlSettings() {
        // Spinner para engines de busca
        String[] engines = {"Bing (Padrão)", "Google", "DuckDuckGo", "Personalizada"};
        ArrayAdapter<String> engineAdapter = new ArrayAdapter<>(this, 
            android.R.layout.simple_spinner_dropdown_item, engines);
        binding.spinnerSearchEngine.setAdapter(engineAdapter);
        
        binding.spinnerSearchEngine.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, android.view.View view, int position, long id) {
                boolean isCustom = position == 3;
                binding.layoutCustomUrl.setVisibility(isCustom ? android.view.View.VISIBLE : android.view.View.GONE);
                binding.layoutUrlParameters.setVisibility(position == 0 ? android.view.View.VISIBLE : android.view.View.GONE);
            }
            
            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });
        
        // Configurar campos de URL
        binding.editCustomUrlTemplate.setHint("https://example.com/search?q=%s");
        binding.editPcParameter.setText(config.getUrlPcParameter());
        binding.editFormParameter.setText(config.getUrlFormParameter());
    }
    
    private void setupBrowserSettings() {
        // Spinner para apps de navegador
        String[] browsers = {"Chrome", "Chrome Beta", "Microsoft Bing", "Microsoft Edge", "Firefox", "Opera"};
        ArrayAdapter<String> browserAdapter = new ArrayAdapter<>(this, 
            android.R.layout.simple_spinner_dropdown_item, browsers);
        binding.spinnerBrowserApp.setAdapter(browserAdapter);
        
        // Checkboxes para opções de navegador
        binding.checkboxChromeFallback.setChecked(config.isChromeFallbackEnabled());
        binding.checkboxIncognitoMode.setChecked(config.isIncognitoModeEnabled());
    }
    
    private void setupAutomationSettings() {
        // Configurações de automação
        binding.checkboxRandomDelay.setChecked(config.isRandomDelayEnabled());
        binding.checkboxAutoCloseTabs.setChecked(config.isAutoCloseTabsEnabled());
        
        binding.seekBarMinDelay.setMax(15);
        binding.seekBarMinDelay.setProgress(config.getMinRandomDelay());
        binding.seekBarMinDelay.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (progress < 1) progress = 1;
                binding.textMinDelayValue.setText(progress + "s");
                
                // Garantir que max >= min
                if (binding.seekBarMaxDelay.getProgress() < progress) {
                    binding.seekBarMaxDelay.setProgress(progress + 1);
                    binding.textMaxDelayValue.setText((progress + 1) + "s");
                }
            }
            
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}
            
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });
        
        binding.seekBarMaxDelay.setMax(30);
        binding.seekBarMaxDelay.setProgress(config.getMaxRandomDelay());
        binding.seekBarMaxDelay.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (progress < 2) progress = 2;
                binding.textMaxDelayValue.setText(progress + "s");
                
                // Garantir que max >= min
                if (binding.seekBarMinDelay.getProgress() > progress) {
                    binding.seekBarMinDelay.setProgress(progress - 1);
                    binding.textMinDelayValue.setText((progress - 1) + "s");
                }
            }
            
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}
            
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });
        
        binding.checkboxRandomDelay.setOnCheckedChangeListener((buttonView, isChecked) -> {
            binding.layoutDelayRange.setVisibility(isChecked ? android.view.View.VISIBLE : android.view.View.GONE);
        });
    }
    
    private void setupAISettings() {
        // Spinner para modos de IA
        String[] aiModes = {"IA Básica", "IA Avançada", "IA Tipo ChatGPT", "Personalizada"};
        ArrayAdapter<String> aiAdapter = new ArrayAdapter<>(this, 
            android.R.layout.simple_spinner_dropdown_item, aiModes);
        binding.spinnerAiMode.setAdapter(aiAdapter);
        
        binding.checkboxContextualLearning.setChecked(config.isContextualLearningEnabled());
        binding.checkboxTemporalAwareness.setChecked(config.isTemporalAwarenessEnabled());
        
        binding.spinnerAiMode.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, android.view.View view, int position, long id) {
                boolean isAdvanced = position >= 2; // ChatGPT ou Personalizada
                binding.layoutAiAdvanced.setVisibility(isAdvanced ? android.view.View.VISIBLE : android.view.View.GONE);
            }
            
            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });
    }
    
    private void setupSecuritySettings() {
        binding.checkboxStealthMode.setChecked(config.isStealthModeEnabled());
        binding.checkboxRotateUserAgent.setChecked(config.isUserAgentRotationEnabled());
    }
    
    private void setupButtons() {
        binding.buttonSaveSettings.setOnClickListener(v -> saveSettings());
        binding.buttonResetDefaults.setOnClickListener(v -> resetToDefaults());
        binding.buttonExportConfig.setOnClickListener(v -> exportConfig());
        binding.buttonTestConfiguration.setOnClickListener(v -> testConfiguration());
    }
    
    private void loadCurrentSettings() {
        // Carregar valores atuais nas views
        binding.textSearchIntervalValue.setText(config.getSearchInterval() + "s");
        binding.textResultDisplayValue.setText(config.getResultDisplayTime() + "s");
        
        // Carregar engine de busca atual
        AppConfig.SearchEngine currentEngine = config.getSearchEngine();
        int enginePosition = 0;
        switch (currentEngine) {
            case BING: enginePosition = 0; break;
            case GOOGLE: enginePosition = 1; break;
            case DUCKDUCKGO: enginePosition = 2; break;
            case CUSTOM: enginePosition = 3; break;
        }
        binding.spinnerSearchEngine.setSelection(enginePosition);
        
        // Carregar browser atual
        AppConfig.BrowserApp currentBrowser = config.getBrowserApp();
        int browserPosition = 0;
        switch (currentBrowser) {
            case CHROME: browserPosition = 0; break;
            case CHROME_BETA: browserPosition = 1; break;
            case BING: browserPosition = 2; break;
            case EDGE: browserPosition = 3; break;
            case FIREFOX: browserPosition = 4; break;
            case OPERA: browserPosition = 5; break;
        }
        binding.spinnerBrowserApp.setSelection(browserPosition);
        
        // Carregar modo de IA atual
        AppConfig.AIMode currentAI = config.getAIMode();
        int aiPosition = 0;
        switch (currentAI) {
            case BASIC: aiPosition = 0; break;
            case ADVANCED: aiPosition = 1; break;
            case CHATGPT: aiPosition = 2; break;
            case CUSTOM: aiPosition = 3; break;
        }
        binding.spinnerAiMode.setSelection(aiPosition);
        
        // Configurar delays
        binding.textMinDelayValue.setText(config.getMinRandomDelay() + "s");
        binding.textMaxDelayValue.setText(config.getMaxRandomDelay() + "s");
        binding.layoutDelayRange.setVisibility(
            config.isRandomDelayEnabled() ? android.view.View.VISIBLE : android.view.View.GONE);
        
        // URL personalizada
        binding.editCustomUrlTemplate.setText(config.getCustomUrlTemplate());
        
        // Visibilidade de layouts
        boolean isCustomEngine = currentEngine == AppConfig.SearchEngine.CUSTOM;
        binding.layoutCustomUrl.setVisibility(isCustomEngine ? android.view.View.VISIBLE : android.view.View.GONE);
        binding.layoutUrlParameters.setVisibility(
            currentEngine == AppConfig.SearchEngine.BING ? android.view.View.VISIBLE : android.view.View.GONE);
        
        boolean isAdvancedAI = currentAI == AppConfig.AIMode.CHATGPT || currentAI == AppConfig.AIMode.CUSTOM;
        binding.layoutAiAdvanced.setVisibility(isAdvancedAI ? android.view.View.VISIBLE : android.view.View.GONE);
    }
    
    private void saveSettings() {
        try {
            // Salvar configurações de tempo
            config.setSearchInterval(binding.seekBarSearchInterval.getProgress());
            config.setResultDisplayTime(binding.seekBarResultDisplay.getProgress());
            
            // Salvar engine de busca
            int enginePos = binding.spinnerSearchEngine.getSelectedItemPosition();
            AppConfig.SearchEngine[] engines = {
                AppConfig.SearchEngine.BING, AppConfig.SearchEngine.GOOGLE, 
                AppConfig.SearchEngine.DUCKDUCKGO, AppConfig.SearchEngine.CUSTOM
            };
            config.setSearchEngine(engines[enginePos]);
            
            // Salvar browser
            int browserPos = binding.spinnerBrowserApp.getSelectedItemPosition();
            AppConfig.BrowserApp[] browsers = {
                AppConfig.BrowserApp.CHROME, AppConfig.BrowserApp.CHROME_BETA,
                AppConfig.BrowserApp.BING, AppConfig.BrowserApp.EDGE,
                AppConfig.BrowserApp.FIREFOX, AppConfig.BrowserApp.OPERA
            };
            config.setBrowserApp(browsers[browserPos]);
            
            // Salvar modo de IA
            int aiPos = binding.spinnerAiMode.getSelectedItemPosition();
            AppConfig.AIMode[] aiModes = {
                AppConfig.AIMode.BASIC, AppConfig.AIMode.ADVANCED,
                AppConfig.AIMode.CHATGPT, AppConfig.AIMode.CUSTOM
            };
            config.setAIMode(aiModes[aiPos]);
            
            // Salvar parâmetros de URL
            config.setUrlPcParameter(binding.editPcParameter.getText().toString().trim());
            config.setUrlFormParameter(binding.editFormParameter.getText().toString().trim());
            config.setCustomUrlTemplate(binding.editCustomUrlTemplate.getText().toString().trim());
            
            // Salvar configurações booleanas
            config.setChromeFallbackEnabled(binding.checkboxChromeFallback.isChecked());
            config.setIncognitoModeEnabled(binding.checkboxIncognitoMode.isChecked());
            config.setRandomDelayEnabled(binding.checkboxRandomDelay.isChecked());
            config.setAutoCloseTabsEnabled(binding.checkboxAutoCloseTabs.isChecked());
            config.setContextualLearningEnabled(binding.checkboxContextualLearning.isChecked());
            config.setTemporalAwarenessEnabled(binding.checkboxTemporalAwareness.isChecked());
            config.setStealthModeEnabled(binding.checkboxStealthMode.isChecked());
            config.setUserAgentRotationEnabled(binding.checkboxRotateUserAgent.isChecked());
            
            // Salvar delays
            config.setMinRandomDelay(binding.seekBarMinDelay.getProgress());
            config.setMaxRandomDelay(binding.seekBarMaxDelay.getProgress());
            
            Toast.makeText(this, "✅ Configurações salvas com sucesso!", Toast.LENGTH_SHORT).show();
            
        } catch (Exception e) {
            Toast.makeText(this, "❌ Erro ao salvar configurações: " + e.getMessage(), 
                         Toast.LENGTH_LONG).show();
        }
    }
    
    private void resetToDefaults() {
        new android.app.AlertDialog.Builder(this)
            .setTitle("Resetar Configurações")
            .setMessage("Tem certeza que deseja voltar às configurações padrão?")
            .setPositiveButton("Sim", (dialog, which) -> {
                config.resetToDefaults();
                loadCurrentSettings();
                Toast.makeText(this, "🔄 Configurações resetadas!", Toast.LENGTH_SHORT).show();
            })
            .setNegativeButton("Cancelar", null)
            .show();
    }
    
    private void exportConfig() {
        String configText = config.exportConfig();
        
        // Copiar para clipboard
        android.content.ClipboardManager clipboard = 
            (android.content.ClipboardManager) getSystemService(android.content.Context.CLIPBOARD_SERVICE);
        android.content.ClipData clip = android.content.ClipData.newPlainText("Config", configText);
        clipboard.setPrimaryClip(clip);
        
        Toast.makeText(this, "📋 Configurações copiadas para clipboard!", Toast.LENGTH_SHORT).show();
    }
    
    private void testConfiguration() {
        try {
            String testQuery = "teste configuração";
            String testUrl = config.buildSearchUrl(testQuery);
            
            new android.app.AlertDialog.Builder(this)
                .setTitle("🧪 Teste de Configuração")
                .setMessage("URL de teste gerada:\\n\\n" + testUrl + 
                          "\\n\\nIntervalo: " + config.getActualSearchInterval() + "s" +
                          "\\nApp: " + config.getBrowserApp().getDisplayName() +
                          "\\nIA: " + config.getAIMode().getDisplayName())
                .setPositiveButton("OK", null)
                .show();
                
        } catch (Exception e) {
            Toast.makeText(this, "❌ Erro no teste: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }
    
    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}