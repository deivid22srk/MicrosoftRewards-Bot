package com.deivid22srk.microsoftrewards.service;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;
import android.os.PowerManager;
import android.util.Log;

import androidx.core.app.NotificationCompat;

import com.deivid22srk.microsoftrewards.R;
import com.deivid22srk.microsoftrewards.model.SearchItem;
import com.deivid22srk.microsoftrewards.utils.AppConfig;
import com.deivid22srk.microsoftrewards.utils.RootManager;
import com.deivid22srk.microsoftrewards.utils.SmartSearchGenerator;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class ScheduledSearchService extends Service {
    
    private static final String TAG = "ScheduledSearchService";
    private static final String CHANNEL_ID = "scheduled_search_channel";
    private static final int NOTIFICATION_ID = 3000;
    
    private PowerManager.WakeLock wakeLock;
    private AppConfig config;
    private RootManager rootManager;
    private boolean isTest = false;
    private boolean useRoot = false;
    
    @Override
    public void onCreate() {
        super.onCreate();
        config = AppConfig.getInstance(this);
        rootManager = RootManager.getInstance();
        
        // Verificar ROOT de forma síncrona (necessário no service)
        rootManager.checkRootNow();
        
        // Verificar se tem ROOT
        useRoot = rootManager.isRootGranted();
        Log.d(TAG, useRoot ? "✅ ROOT disponível" : "⚠️ ROOT não disponível");
        
        // Se tiver ROOT, usar comandos ROOT para garantir execução
        if (useRoot) {
            Log.d(TAG, "🔐 Usando ROOT para garantir execução");
            rootManager.disableDozeMode();
            rootManager.wakeDevice();
            rootManager.disableBatteryOptimization(getPackageName());
        }
        
        // Adquirir WakeLock para funcionar com tela desligada
        PowerManager powerManager = (PowerManager) getSystemService(POWER_SERVICE);
        wakeLock = powerManager.newWakeLock(
            PowerManager.PARTIAL_WAKE_LOCK,
            "MicrosoftRewards::ScheduledSearchWakeLock"
        );
        wakeLock.acquire(30 * 60 * 1000L); // 30 minutos máximo
        
        Log.d(TAG, "🔋 WakeLock adquirido");
        
        createNotificationChannel();
    }
    
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null) {
            isTest = intent.getBooleanExtra("isTest", false);
        }
        
        // Iniciar como Foreground Service
        Notification notification = createNotification("Iniciando pesquisas agendadas...");
        startForeground(NOTIFICATION_ID, notification);
        
        Log.d(TAG, "🚀 Serviço iniciado (Teste: " + isTest + ")");
        
        // Executar em thread separada
        new Thread(this::executeScheduledSearches).start();
        
        return START_NOT_STICKY;
    }
    
    private void executeScheduledSearches() {
        try {
            int bingCount = config.getBingSearchCount();
            int chromeCount = config.getChromeSearchCount();
            
            Log.d(TAG, "📊 Configuração: Bing=" + bingCount + ", Chrome=" + chromeCount);
            
            // Fase 1: Pesquisas no Bing
            if (bingCount > 0) {
                updateNotification("Executando " + bingCount + " pesquisas no Bing...");
                executeBingSearches(bingCount);
            }
            
            // Aguardar entre browsers
            if (bingCount > 0 && chromeCount > 0) {
                Thread.sleep(5000); // 5 segundos entre browsers
            }
            
            // Fase 2: Pesquisas no Chrome
            if (chromeCount > 0) {
                updateNotification("Executando " + chromeCount + " pesquisas no Chrome...");
                executeChromeSearches(chromeCount);
            }
            
            // Concluído
            updateNotification("✅ Pesquisas concluídas! Bing: " + bingCount + " | Chrome: " + chromeCount);
            Log.d(TAG, "✅ Todas as pesquisas foram concluídas");
            
            // Aguardar 5 segundos antes de finalizar
            Thread.sleep(5000);
            
            // Re-agendar próximo alarme se não for teste
            if (!isTest && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                rescheduleNextAlarm();
            }
            
        } catch (Exception e) {
            Log.e(TAG, "❌ Erro ao executar pesquisas: " + e.getMessage(), e);
            updateNotification("❌ Erro ao executar pesquisas");
        } finally {
            stopForeground(true);
            stopSelf();
            
            // Liberar WakeLock
            if (wakeLock != null && wakeLock.isHeld()) {
                wakeLock.release();
                Log.d(TAG, "🔋 WakeLock liberado");
            }
        }
    }
    
    private void executeBingSearches(int count) {
        try {
            Log.d(TAG, "🔍 Gerando " + count + " pesquisas para Bing");
            
            // Gerar pesquisas
            List<SearchItem> searches = SmartSearchGenerator.generateOfflineIntelligentSearches(count, this);
            
            // Configurar para usar Bing
            AppConfig.SearchEngine originalEngine = config.getSearchEngine();
            AppConfig.BrowserApp originalBrowser = config.getBrowserApp();
            
            config.setSearchEngine(AppConfig.SearchEngine.BING);
            config.setBrowserApp(AppConfig.BrowserApp.BING);
            
            // Usar ROOT se disponível para abrir navegadores
            if (useRoot && rootManager != null && rootManager.isRootGranted()) {
                executeSearchesWithRoot(searches, "Bing", AppConfig.BrowserApp.BING);
            } else {
                // Iniciar automação normal
                Intent automationIntent = new Intent(this, SearchAutomationService.class);
                automationIntent.putExtra("searchItems", new ArrayList<>(searches));
                automationIntent.putExtra("scheduledMode", true);
                automationIntent.putExtra("browserName", "Bing");
                
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    startForegroundService(automationIntent);
                } else {
                    startService(automationIntent);
                }
                
                // Aguardar conclusão (estimativa: intervalo * quantidade)
                int waitTime = (config.getActualSearchInterval() + 3) * count * 1000; // +3s de margem
                Thread.sleep(waitTime);
            }
            
            // Restaurar configurações originais
            config.setSearchEngine(originalEngine);
            config.setBrowserApp(originalBrowser);
            
            Log.d(TAG, "✅ Pesquisas Bing concluídas");
            
        } catch (Exception e) {
            Log.e(TAG, "❌ Erro nas pesquisas Bing: " + e.getMessage(), e);
        }
    }
    
    private void executeChromeSearches(int count) {
        try {
            Log.d(TAG, "🔍 Gerando " + count + " pesquisas para Chrome");
            
            // Gerar pesquisas
            List<SearchItem> searches = SmartSearchGenerator.generateOfflineIntelligentSearches(count, this);
            
            // Configurar para usar Chrome COM MOTOR BING
            AppConfig.SearchEngine originalEngine = config.getSearchEngine();
            AppConfig.BrowserApp originalBrowser = config.getBrowserApp();
            
            config.setSearchEngine(AppConfig.SearchEngine.BING); // USAR BING NO CHROME
            config.setBrowserApp(AppConfig.BrowserApp.CHROME);
            
            // Usar ROOT se disponível para abrir navegadores
            if (useRoot && rootManager != null && rootManager.isRootGranted()) {
                executeSearchesWithRoot(searches, "Chrome", AppConfig.BrowserApp.CHROME);
            } else {
                // Iniciar automação normal
                Intent automationIntent = new Intent(this, SearchAutomationService.class);
                automationIntent.putExtra("searchItems", new ArrayList<>(searches));
                automationIntent.putExtra("scheduledMode", true);
                automationIntent.putExtra("browserName", "Chrome");
                
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    startForegroundService(automationIntent);
                } else {
                    startService(automationIntent);
                }
                
                // Aguardar conclusão
                int waitTime = (config.getActualSearchInterval() + 3) * count * 1000;
                Thread.sleep(waitTime);
            }
            
            // Restaurar configurações originais
            config.setSearchEngine(originalEngine);
            config.setBrowserApp(originalBrowser);
            
            Log.d(TAG, "✅ Pesquisas Chrome concluídas");
            
        } catch (Exception e) {
            Log.e(TAG, "❌ Erro nas pesquisas Chrome: " + e.getMessage(), e);
        }
    }
    
    /**
     * Executa pesquisas usando ROOT (mais confiável)
     */
    private void executeSearchesWithRoot(List<SearchItem> searches, String browserName, AppConfig.BrowserApp browser) {
        Log.d(TAG, "🔐 Executando " + searches.size() + " pesquisas com ROOT no " + browserName);
        
        try {
            String packageName = browser.getPackageName();
            String component = getComponentForBrowser(browser);
            
            for (int i = 0; i < searches.size(); i++) {
                SearchItem item = searches.get(i);
                String url = config.buildSearchUrl(item.getSearchText());
                
                Log.d(TAG, String.format("🔍 [%d/%d] %s: %s", i + 1, searches.size(), browserName, item.getSearchText()));
                updateNotification(String.format("🔍 %s [%d/%d]: %s", browserName, i + 1, searches.size(), item.getSearchText()));
                
                // Acordar dispositivo antes de cada pesquisa
                rootManager.wakeDevice();
                
                // Abrir URL usando ROOT
                String command = String.format(
                    "am start -a android.intent.action.VIEW -d '%s' %s",
                    url,
                    component != null ? "-n " + component : "-p " + packageName
                );
                
                String result = rootManager.executeRootCommand(command);
                
                if (result != null && (result.contains("Starting") || result.contains("Success"))) {
                    item.setStatus(SearchItem.SearchStatus.COMPLETED);
                    Log.d(TAG, "✅ Pesquisa aberta com sucesso via ROOT");
                } else {
                    item.setStatus(SearchItem.SearchStatus.FAILED);
                    Log.w(TAG, "⚠️ Falha ao abrir com ROOT: " + result);
                }
                
                // Aguardar intervalo antes da próxima
                if (i < searches.size() - 1) {
                    int interval = config.getActualSearchInterval() * 1000;
                    Thread.sleep(interval);
                }
            }
            
            Log.d(TAG, "✅ Todas as pesquisas " + browserName + " concluídas via ROOT");
            
        } catch (Exception e) {
            Log.e(TAG, "❌ Erro ao executar com ROOT: " + e.getMessage(), e);
        }
    }
    
    /**
     * Retorna o componente específico para cada navegador
     */
    private String getComponentForBrowser(AppConfig.BrowserApp browser) {
        switch (browser) {
            case CHROME:
                return "com.android.chrome/com.google.android.apps.chrome.Main";
            case BING:
                return "com.microsoft.bing/com.microsoft.sapphire.app.main.MainActivity";
            case EDGE:
                return "com.microsoft.emmx/com.microsoft.ruby.Main";
            case FIREFOX:
                return "org.mozilla.firefox/.App";
            default:
                return null;
        }
    }
    
    private void rescheduleNextAlarm() {
        Log.d(TAG, "🔄 Re-agendando próximo alarme");
        
        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(this, SchedulerBroadcastReceiver.class);
        intent.setAction("com.deivid22srk.microsoftrewards.SCHEDULED_SEARCH");
        
        PendingIntent pendingIntent = PendingIntent.getBroadcast(
            this,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );
        
        // Calcular próximo horário (amanhã)
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DAY_OF_YEAR, 1);
        calendar.set(Calendar.HOUR_OF_DAY, config.getSchedulerHour());
        calendar.set(Calendar.MINUTE, config.getSchedulerMinute());
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        
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
        
        Log.d(TAG, "✅ Próximo alarme agendado para: " + calendar.getTime());
    }
    
    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                CHANNEL_ID,
                "Pesquisas Agendadas",
                NotificationManager.IMPORTANCE_LOW
            );
            channel.setDescription("Notificações de pesquisas automáticas agendadas");
            
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }
    
    private Notification createNotification(String message) {
        return new NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("⏰ Pesquisas Agendadas")
            .setContentText(message)
            .setSmallIcon(R.drawable.ic_search)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setOngoing(true)
            .build();
    }
    
    private void updateNotification(String message) {
        Notification notification = createNotification(message);
        NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        notificationManager.notify(NOTIFICATION_ID, notification);
    }
    
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
    
    @Override
    public void onDestroy() {
        super.onDestroy();
        
        if (wakeLock != null && wakeLock.isHeld()) {
            wakeLock.release();
        }
        
        // Re-habilitar Doze mode se usou ROOT
        if (useRoot && rootManager != null) {
            rootManager.enableDozeMode();
            Log.d(TAG, "🔓 Doze mode re-habilitado");
        }
        
        Log.d(TAG, "🛑 Serviço finalizado");
    }
}
