package com.deivid22srk.microsoftrewards;

import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.PowerManager;
import android.provider.Settings;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;
import androidx.core.content.ContextCompat;

import com.deivid22srk.microsoftrewards.service.SchedulerBroadcastReceiver;
import com.deivid22srk.microsoftrewards.utils.AppConfig;
import com.deivid22srk.microsoftrewards.utils.RootManager;
import com.google.android.material.textfield.TextInputEditText;

import java.util.Calendar;
import java.util.Locale;

public class SchedulerActivity extends AppCompatActivity {
    
    private AppConfig config;
    private SwitchCompat schedulerEnabledSwitch;
    private TextView scheduledTimeText;
    private TextInputEditText bingSearchCountEdit;
    private TextInputEditText chromeSearchCountEdit;
    private Button selectTimeButton;
    private Button testNowButton;
    private Button saveButton;
    private TextView statusText;
    
    private int selectedHour = 5;
    private int selectedMinute = 0;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scheduler);
        
        config = AppConfig.getInstance(this);
        
        setupToolbar();
        initializeViews();
        loadSettings();
        setupClickListeners();
        checkPermissions();
    }
    
    private void setupToolbar() {
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("⏰ Agendamento Automático");
        }
    }
    
    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
    
    private void initializeViews() {
        schedulerEnabledSwitch = findViewById(R.id.schedulerEnabledSwitch);
        scheduledTimeText = findViewById(R.id.scheduledTimeText);
        bingSearchCountEdit = findViewById(R.id.bingSearchCountEdit);
        chromeSearchCountEdit = findViewById(R.id.chromeSearchCountEdit);
        selectTimeButton = findViewById(R.id.selectTimeButton);
        testNowButton = findViewById(R.id.testNowButton);
        saveButton = findViewById(R.id.saveButton);
        statusText = findViewById(R.id.statusText);
    }
    
    private void loadSettings() {
        schedulerEnabledSwitch.setChecked(config.isSchedulerEnabled());
        selectedHour = config.getSchedulerHour();
        selectedMinute = config.getSchedulerMinute();
        updateTimeDisplay();
        
        bingSearchCountEdit.setText(String.valueOf(config.getBingSearchCount()));
        chromeSearchCountEdit.setText(String.valueOf(config.getChromeSearchCount()));
        
        updateStatusDisplay();
    }
    
    private void setupClickListeners() {
        schedulerEnabledSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            updateUIState(isChecked);
        });
        
        selectTimeButton.setOnClickListener(v -> showTimePicker());
        testNowButton.setOnClickListener(v -> testSearchesNow());
        saveButton.setOnClickListener(v -> saveSettings());
    }
    
    private void updateUIState(boolean enabled) {
        selectTimeButton.setEnabled(enabled);
        bingSearchCountEdit.setEnabled(enabled);
        chromeSearchCountEdit.setEnabled(enabled);
        testNowButton.setEnabled(enabled);
    }
    
    private void showTimePicker() {
        TimePickerDialog timePickerDialog = new TimePickerDialog(
            this,
            (view, hourOfDay, minute) -> {
                selectedHour = hourOfDay;
                selectedMinute = minute;
                updateTimeDisplay();
            },
            selectedHour,
            selectedMinute,
            true
        );
        timePickerDialog.setTitle("Selecione o horário");
        timePickerDialog.show();
    }
    
    private void updateTimeDisplay() {
        String timeStr = String.format(Locale.getDefault(), "%02d:%02d", selectedHour, selectedMinute);
        scheduledTimeText.setText(timeStr);
    }
    
    private void checkPermissions() {
        // Verificar ROOT primeiro
        checkRootAccess();
        
        // Verificar permissão de bateria
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
            String packageName = getPackageName();
            
            if (!pm.isIgnoringBatteryOptimizations(packageName)) {
                showBatteryOptimizationDialog();
            }
        }
        
        // Verificar permissão de alarmes exatos (Android 12+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
            if (!alarmManager.canScheduleExactAlarms()) {
                showExactAlarmDialog();
            }
        }
    }
    
    private void showBatteryOptimizationDialog() {
        new AlertDialog.Builder(this)
            .setTitle("⚡ Otimização de Bateria")
            .setMessage("Para garantir que as pesquisas sejam executadas automaticamente mesmo com a tela desligada, é necessário desativar a otimização de bateria para este app.\n\n⚠️ ATENÇÃO: Esta configuração requer permissões especiais do sistema.")
            .setPositiveButton("Configurar", (dialog, which) -> {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    Intent intent = new Intent();
                    intent.setAction(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS);
                    intent.setData(Uri.parse("package:" + getPackageName()));
                    try {
                        startActivity(intent);
                    } catch (Exception e) {
                        Toast.makeText(this, "Erro ao abrir configurações", Toast.LENGTH_SHORT).show();
                    }
                }
            })
            .setNegativeButton("Depois", null)
            .show();
    }
    
    private void showExactAlarmDialog() {
        new AlertDialog.Builder(this)
            .setTitle("⏰ Permissão de Alarmes")
            .setMessage("Para agendar pesquisas automáticas em horários exatos, é necessário permitir alarmes exatos.\n\nSem esta permissão, o horário pode ter uma variação de até 15 minutos.")
            .setPositiveButton("Configurar", (dialog, which) -> {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    Intent intent = new Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM);
                    intent.setData(Uri.parse("package:" + getPackageName()));
                    try {
                        startActivity(intent);
                    } catch (Exception e) {
                        Toast.makeText(this, "Erro ao abrir configurações", Toast.LENGTH_SHORT).show();
                    }
                }
            })
            .setNegativeButton("Depois", null)
            .show();
    }
    
    private void checkRootAccess() {
        RootManager rootManager = RootManager.getInstance();
        
        if (rootManager.isRootAvailable()) {
            if (!rootManager.isRootGranted()) {
                showRootRequestDialog();
            } else {
                Toast.makeText(this, "✅ ROOT disponível e ativo", Toast.LENGTH_SHORT).show();
            }
        } else {
            showNoRootWarning();
        }
    }
    
    private void showRootRequestDialog() {
        new AlertDialog.Builder(this)
            .setTitle("🔐 Permissões ROOT")
            .setMessage("Para garantir 100% de confiabilidade na execução automática (especialmente com tela desligada), o app precisa de permissões ROOT.\n\n✅ COM ROOT:\n• Executa SEMPRE no horário exato\n• Funciona com tela desligada\n• Não é afetado por economia de bateria\n\n⚠️ SEM ROOT:\n• Pode falhar em alguns dispositivos\n• Depende de otimizações do sistema\n\nDeseja conceder permissões ROOT agora?")
            .setPositiveButton("Conceder ROOT", (dialog, which) -> {
                new Thread(() -> {
                    RootManager rootManager = RootManager.getInstance();
                    boolean granted = rootManager.requestRootAccess();
                    
                    runOnUiThread(() -> {
                        if (granted) {
                            Toast.makeText(this, "✅ Permissões ROOT concedidas!", Toast.LENGTH_LONG).show();
                        } else {
                            Toast.makeText(this, "❌ Permissões ROOT negadas", Toast.LENGTH_LONG).show();
                            showNoRootWarning();
                        }
                    });
                }).start();
            })
            .setNegativeButton("Usar sem ROOT", (dialog, which) -> {
                showNoRootWarning();
            })
            .setCancelable(false)
            .show();
    }
    
    private void showNoRootWarning() {
        new AlertDialog.Builder(this)
            .setTitle("⚠️ Modo sem ROOT")
            .setMessage("O app funcionará sem ROOT, mas:\n\n" +
                       "• Pode NÃO executar em alguns dispositivos\n" +
                       "• Pode falhar com tela desligada\n" +
                       "• Depende de otimizações do Android\n\n" +
                       "Recomendações:\n" +
                       "1. Desative otimização de bateria\n" +
                       "2. Deixe o celular carregando\n" +
                       "3. Teste antes de confiar no agendamento\n\n" +
                       "Para melhor confiabilidade, considere usar ROOT.")
            .setPositiveButton("Entendi", null)
            .show();
    }
    
    private void testSearchesNow() {
        if (!validateInputs()) {
            return;
        }
        
        new AlertDialog.Builder(this)
            .setTitle("🧪 Testar Agora")
            .setMessage("Deseja executar as pesquisas agendadas agora para testar a configuração?\n\nIsso irá iniciar:\n• " + 
                bingSearchCountEdit.getText().toString() + " pesquisas no Bing\n• " + 
                chromeSearchCountEdit.getText().toString() + " pesquisas no Chrome")
            .setPositiveButton("Sim, Testar", (dialog, which) -> {
                // Salvar configurações temporariamente
                saveTempSettings();
                
                // Iniciar o serviço de teste
                Intent intent = new Intent(this, SchedulerBroadcastReceiver.class);
                intent.setAction("com.deivid22srk.microsoftrewards.TEST_SCHEDULED_SEARCH");
                sendBroadcast(intent);
                
                Toast.makeText(this, "🚀 Teste iniciado! Acompanhe o progresso.", Toast.LENGTH_LONG).show();
            })
            .setNegativeButton("Cancelar", null)
            .show();
    }
    
    private void saveTempSettings() {
        int bingCount = Integer.parseInt(bingSearchCountEdit.getText().toString().trim());
        int chromeCount = Integer.parseInt(chromeSearchCountEdit.getText().toString().trim());
        
        config.setBingSearchCount(bingCount);
        config.setChromeSearchCount(chromeCount);
    }
    
    private void saveSettings() {
        if (!validateInputs()) {
            return;
        }
        
        boolean enabled = schedulerEnabledSwitch.isChecked();
        int bingCount = Integer.parseInt(bingSearchCountEdit.getText().toString().trim());
        int chromeCount = Integer.parseInt(chromeSearchCountEdit.getText().toString().trim());
        
        // Salvar no AppConfig
        config.setSchedulerEnabled(enabled);
        config.setSchedulerTime(selectedHour, selectedMinute);
        config.setBingSearchCount(bingCount);
        config.setChromeSearchCount(chromeCount);
        
        if (enabled) {
            scheduleAlarm();
            Toast.makeText(this, "✅ Agendamento ativado com sucesso!", Toast.LENGTH_SHORT).show();
        } else {
            cancelAlarm();
            Toast.makeText(this, "✅ Agendamento desativado", Toast.LENGTH_SHORT).show();
        }
        
        updateStatusDisplay();
    }
    
    private boolean validateInputs() {
        String bingStr = bingSearchCountEdit.getText().toString().trim();
        String chromeStr = chromeSearchCountEdit.getText().toString().trim();
        
        if (bingStr.isEmpty() || chromeStr.isEmpty()) {
            Toast.makeText(this, "⚠️ Preencha todos os campos", Toast.LENGTH_SHORT).show();
            return false;
        }
        
        try {
            int bingCount = Integer.parseInt(bingStr);
            int chromeCount = Integer.parseInt(chromeStr);
            
            if (bingCount < 0 || bingCount > 100) {
                Toast.makeText(this, "⚠️ Pesquisas Bing: entre 0 e 100", Toast.LENGTH_SHORT).show();
                return false;
            }
            
            if (chromeCount < 0 || chromeCount > 100) {
                Toast.makeText(this, "⚠️ Pesquisas Chrome: entre 0 e 100", Toast.LENGTH_SHORT).show();
                return false;
            }
            
            if (bingCount == 0 && chromeCount == 0) {
                Toast.makeText(this, "⚠️ Configure pelo menos um tipo de pesquisa", Toast.LENGTH_SHORT).show();
                return false;
            }
            
        } catch (NumberFormatException e) {
            Toast.makeText(this, "⚠️ Números inválidos", Toast.LENGTH_SHORT).show();
            return false;
        }
        
        return true;
    }
    
    private void scheduleAlarm() {
        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(this, SchedulerBroadcastReceiver.class);
        intent.setAction("com.deivid22srk.microsoftrewards.SCHEDULED_SEARCH");
        
        PendingIntent pendingIntent = PendingIntent.getBroadcast(
            this,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );
        
        // Calcular próximo horário
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, selectedHour);
        calendar.set(Calendar.MINUTE, selectedMinute);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        
        // Se o horário já passou hoje, agendar para amanhã
        if (calendar.getTimeInMillis() <= System.currentTimeMillis()) {
            calendar.add(Calendar.DAY_OF_YEAR, 1);
        }
        
        // Agendar alarme exato e repetitivo
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                calendar.getTimeInMillis(),
                pendingIntent
            );
        } else {
            alarmManager.setExact(
                AlarmManager.RTC_WAKEUP,
                calendar.getTimeInMillis(),
                pendingIntent
            );
        }
        
        // Também agendar repetição diária
        alarmManager.setRepeating(
            AlarmManager.RTC_WAKEUP,
            calendar.getTimeInMillis(),
            AlarmManager.INTERVAL_DAY,
            pendingIntent
        );
    }
    
    private void cancelAlarm() {
        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(this, SchedulerBroadcastReceiver.class);
        intent.setAction("com.deivid22srk.microsoftrewards.SCHEDULED_SEARCH");
        
        PendingIntent pendingIntent = PendingIntent.getBroadcast(
            this,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );
        
        alarmManager.cancel(pendingIntent);
    }
    
    private void updateStatusDisplay() {
        if (config.isSchedulerEnabled()) {
            String timeStr = String.format(Locale.getDefault(), "%02d:%02d", 
                config.getSchedulerHour(), config.getSchedulerMinute());
            statusText.setText("✅ Agendamento Ativo\n⏰ Próxima execução: " + timeStr + 
                "\n🔍 Bing: " + config.getBingSearchCount() + " pesquisas" +
                "\n🌐 Chrome: " + config.getChromeSearchCount() + " pesquisas");
            statusText.setTextColor(ContextCompat.getColor(this, R.color.microsoft_green));
        } else {
            statusText.setText("⏸️ Agendamento Desativado");
            statusText.setTextColor(ContextCompat.getColor(this, R.color.microsoft_orange));
        }
    }
}
