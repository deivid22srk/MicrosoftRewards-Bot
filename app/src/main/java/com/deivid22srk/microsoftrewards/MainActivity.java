package com.deivid22srk.microsoftrewards;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
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

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private static final int OVERLAY_PERMISSION_REQUEST_CODE = 1000;
    private static final int NOTIFICATION_PERMISSION_REQUEST_CODE = 1001;

    private ActivityMainBinding binding;
    private SearchAdapter searchAdapter;
    private List<SearchItem> searchItems;
    
    private boolean isAutomationRunning = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setupToolbar();
        setupRecyclerView();
        setupClickListeners();
        
        // IA Search Generator integrado - não precisa de inicialização
    }

    private void setupToolbar() {
        setSupportActionBar(binding.toolbar);
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
        
        // Gerar pesquisas com IA
        searchItems.clear();
        List<SearchItem> generatedSearches = SmartSearchGenerator.generateSmartSearches(count);
        
        searchItems.addAll(generatedSearches);
        
        searchAdapter.notifyDataSetChanged();
        binding.searchesCard.setVisibility(View.VISIBLE);
        binding.startButton.setEnabled(true);
        
        Toast.makeText(this, getString(R.string.searches_generated, count), Toast.LENGTH_SHORT).show();
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
        
        // Iniciar serviço de automação
        Intent automationIntent = new Intent(this, SearchAutomationService.class);
        automationIntent.putExtra("searchItems", new ArrayList<>(searchItems));
        startService(automationIntent);
        
        Toast.makeText(this, R.string.automation_started, Toast.LENGTH_SHORT).show();
    }

    private void stopAutomation() {
        isAutomationRunning = false;
        updateUI();
        
        // Parar serviços
        stopService(new Intent(this, FloatingButtonService.class));
        stopService(new Intent(this, SearchAutomationService.class));
        
        Toast.makeText(this, R.string.automation_stopped, Toast.LENGTH_SHORT).show();
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
            .setTitle(R.string.overlay_permission_required)
            .setMessage(R.string.overlay_permission_message)
            .setPositiveButton(R.string.grant_permission, (dialog, which) -> {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, 
                        Uri.parse("package:" + getPackageName()));
                    startActivityForResult(intent, OVERLAY_PERMISSION_REQUEST_CODE);
                }
            })
            .setNegativeButton(R.string.cancel, null)
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
                Toast.makeText(this, "Permissão necessária para continuar", Toast.LENGTH_LONG).show();
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
                Toast.makeText(this, "Permissão de notificação é necessária para funcionamento completo", Toast.LENGTH_LONG).show();
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