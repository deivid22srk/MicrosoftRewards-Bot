package com.deivid22srk.microsoftrewards.service;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.util.Log;

import androidx.core.app.NotificationCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.deivid22srk.microsoftrewards.MainActivity;
import com.deivid22srk.microsoftrewards.R;
import com.deivid22srk.microsoftrewards.model.SearchItem;
import com.deivid22srk.microsoftrewards.utils.AppConfig;
import com.deivid22srk.microsoftrewards.utils.RootManager;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * 🚀 Serviço de Automação de Pesquisas AVANÇADO
 * Agora com configurações personalizáveis e múltiplos navegadores
 */
public class SearchAutomationService extends Service {
    
    private static final String TAG = "SearchAutomationService";
    private static final String CHANNEL_ID = "SearchAutomationChannel";
    private static final int NOTIFICATION_ID = 2;
    
    private Handler handler;
    private Handler countdownHandler;
    private List<SearchItem> searchItems;
    private int currentSearchIndex = 0;
    private boolean isRunning = false;
    private boolean isPaused = false;
    private int countdownSeconds = 5;
    private boolean scheduledMode = false;
    private String browserName = "";
    
    // 🛠️ Configurações avançadas
    private AppConfig config;
    private RootManager rootManager;
    private Random randomGenerator;
    
    // Broadcast receiver para comandos de controle
    private BroadcastReceiver controlReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (FloatingButtonService.ACTION_PAUSE_RESUME.equals(action)) {
                togglePauseResume();
            } else if (FloatingButtonService.ACTION_STOP_AUTOMATION.equals(action)) {
                stopAutomation();
            }
        }
    };

    @Override
    public void onCreate() {
        super.onCreate();
        config = AppConfig.getInstance(this);
        rootManager = RootManager.getInstance();
        randomGenerator = new Random();
        
        createNotificationChannel();
        handler = new Handler(Looper.getMainLooper());
        countdownHandler = new Handler(Looper.getMainLooper());
        
        // Registrar receiver para comandos de controle
        IntentFilter filter = new IntentFilter();
        filter.addAction(FloatingButtonService.ACTION_PAUSE_RESUME);
        filter.addAction(FloatingButtonService.ACTION_STOP_AUTOMATION);
        LocalBroadcastManager.getInstance(this).registerReceiver(controlReceiver, filter);
        
        Log.d(TAG, "SearchAutomationService created with advanced config");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null && intent.hasExtra("searchItems")) {
            ArrayList<SearchItem> items = (ArrayList<SearchItem>) intent.getSerializableExtra("searchItems");
            scheduledMode = intent.getBooleanExtra("scheduledMode", false);
            browserName = intent.getStringExtra("browserName");
            if (browserName == null) browserName = "";
            
            if (items != null) {
                searchItems = items;
                currentSearchIndex = 0;
                isRunning = true;
                
                startForeground(NOTIFICATION_ID, createNotification("🚀 Iniciando automação avançada..."));
                startSearchAutomation();
                
                Log.d(TAG, "Advanced search automation started with " + searchItems.size() + " items");
                Log.d(TAG, "Config: " + config.exportConfig());
            }
        }
        
        return START_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                CHANNEL_ID,
                "Advanced Search Automation Service",
                NotificationManager.IMPORTANCE_LOW
            );
            channel.setDescription("Executa pesquisas automáticas com configurações avançadas");
            
            NotificationManager manager = getSystemService(NotificationManager.class);
            manager.createNotificationChannel(channel);
        }
    }

    private Notification createNotification(String contentText) {
        Intent notificationIntent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(
            this, 0, notificationIntent, 
            PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        return new NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("🤖 Microsoft Rewards Bot Advanced")
            .setContentText(contentText)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .build();
    }

    private void startSearchAutomation() {
        if (!isRunning || searchItems == null || currentSearchIndex >= searchItems.size()) {
            completeAutomation();
            return;
        }
        
        // Verificar se está pausado
        if (isPaused) {
            updateNotification("⏸️ Automação pausada");
            updateFloatingButton("PAUSED");
            return;
        }

        // Usar intervalo configurável com delay aleatório
        int delayTime = config.getActualSearchInterval();
        
        // Iniciar countdown antes da próxima pesquisa
        if (currentSearchIndex > 0) {
            startCountdown(delayTime);
        } else {
            // Primeira pesquisa, executar imediatamente
            executeCurrentSearch();
        }
    }

    private void startCountdown(int delaySeconds) {
        countdownSeconds = delaySeconds;
        runCountdown();
    }

    private void runCountdown() {
        if (!isRunning) return;
        
        // Verificar se foi pausado durante o countdown
        if (isPaused) {
            updateNotification("⏸️ Automação pausada durante countdown");
            updateFloatingButton("PAUSED");
            return;
        }
        
        if (countdownSeconds > 0) {
            updateNotification(String.format("⏰ Próxima pesquisa em %ds (Config: %ds)", 
                               countdownSeconds, config.getSearchInterval()));
            updateFloatingButton("COUNTDOWN");
            
            countdownSeconds--;
            countdownHandler.postDelayed(this::runCountdown, config.getCountdownInterval() * 1000);
        } else {
            executeCurrentSearch();
        }
    }

    private void executeCurrentSearch() {
        if (!isRunning || isPaused || searchItems == null || currentSearchIndex >= searchItems.size()) {
            if (isPaused) {
                updateNotification("⏸️ Automação pausada");
                updateFloatingButton("PAUSED");
                return;
            }
            completeAutomation();
            return;
        }

        SearchItem currentItem = searchItems.get(currentSearchIndex);
        currentItem.setStatus(SearchItem.SearchStatus.IN_PROGRESS);
        
        Log.d(TAG, String.format("🔍 Executing search %d/%d: %s", 
                                currentSearchIndex + 1, searchItems.size(), currentItem.getSearchText()));
        
        String notificationText = scheduledMode && !browserName.isEmpty() 
            ? String.format("🔍 %s: %s", browserName, currentItem.getSearchText())
            : "🔍 Pesquisando: " + currentItem.getSearchText();
        updateNotification(notificationText);
        updateFloatingButton("IN_PROGRESS");

        // Abrir navegador com configurações avançadas
        boolean success = openAdvancedBrowserSearch(currentItem.getSearchText());
        
        if (success) {
            currentItem.setStatus(SearchItem.SearchStatus.COMPLETED);
            Log.d(TAG, "✅ Search completed successfully: " + currentItem.getSearchText());
        } else {
            currentItem.setStatus(SearchItem.SearchStatus.FAILED);
            Log.e(TAG, "❌ Search failed: " + currentItem.getSearchText());
        }

        // Atualizar progresso
        currentSearchIndex++;
        updateFloatingButton("COMPLETED");

        // Agendar próxima pesquisa com tempo configurável
        int resultDisplayTime = config.getResultDisplayTime() * 1000;
        handler.postDelayed(this::startSearchAutomation, resultDisplayTime);
    }

    /**
     * 🚀 Método avançado de abertura de navegador com múltiplas opções
     */
    private boolean openAdvancedBrowserSearch(String searchQuery) {
        try {
            // 1. Construir URL usando configurações personalizadas
            String searchUrl = config.buildSearchUrl(searchQuery);
            Log.d(TAG, "🌐 Built search URL: " + searchUrl);
            
            // 2. SE TIVER ROOT, tentar usar comandos shell primeiro (mais confiável)
            if (rootManager != null && rootManager.isRootGranted()) {
                Log.d(TAG, "🔐 Tentando abrir com ROOT...");
                boolean rootSuccess = tryOpenWithRoot(searchUrl);
                if (rootSuccess) {
                    Log.d(TAG, "✅ Aberto com sucesso usando ROOT");
                    return true;
                } else {
                    Log.w(TAG, "⚠️ Falha com ROOT, tentando método normal...");
                }
            }
            
            // 3. Configurar Intent com flags personalizáveis
            Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(searchUrl));
            configureBrowserIntent(browserIntent);
            
            // 4. Tentar app de navegador configurado
            AppConfig.BrowserApp preferredBrowser = config.getBrowserApp();
            boolean success = tryOpenInBrowser(browserIntent, preferredBrowser, searchUrl);
            
            // 5. Fallback para Chrome se habilitado
            if (!success && config.isChromeFallbackEnabled() && preferredBrowser != AppConfig.BrowserApp.CHROME) {
                Log.d(TAG, "🔄 Trying Chrome fallback...");
                success = tryOpenInBrowser(browserIntent, AppConfig.BrowserApp.CHROME, searchUrl);
            }
            
            // 6. Fallback para navegador padrão
            if (!success) {
                Log.d(TAG, "🔄 Trying default browser...");
                success = tryOpenInDefaultBrowser(browserIntent, searchUrl);
            }
            
            return success;
            
        } catch (Exception e) {
            Log.e(TAG, "❌ Error in advanced browser search: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * 🔐 Abre navegador usando ROOT (mais confiável com tela desligada)
     */
    private boolean tryOpenWithRoot(String url) {
        try {
            AppConfig.BrowserApp browser = config.getBrowserApp();
            String packageName = browser.getPackageName();
            
            // Acordar dispositivo primeiro
            rootManager.wakeDevice();
            
            // Construir comando am start
            String component = getComponentForBrowser(browser);
            String command = String.format(
                "am start -a android.intent.action.VIEW -d '%s' %s",
                url,
                component != null ? "-n " + component : "-p " + packageName
            );
            
            Log.d(TAG, "📱 Executando: " + command);
            String result = rootManager.executeRootCommand(command);
            
            return result != null && (result.contains("Starting") || result.contains("Success"));
            
        } catch (Exception e) {
            Log.e(TAG, "❌ Erro ao abrir com ROOT: " + e.getMessage());
            return false;
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
    
    private void configureBrowserIntent(Intent intent) {
        // Flags para reutilização inteligente de guias
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        
        // Modo incógnito se habilitado (apenas para Chrome)
        if (config.isIncognitoModeEnabled()) {
            intent.putExtra("com.google.android.apps.chrome.EXTRA_OPEN_NEW_INCOGNITO_TAB", true);
        }
        
        // Configurações stealth se habilitadas
        if (config.isStealthModeEnabled()) {
            intent.putExtra("create_new_tab", true);
        }
    }
    
    private boolean tryOpenInBrowser(Intent intent, AppConfig.BrowserApp browser, String searchUrl) {
        try {
            PackageManager pm = getPackageManager();
            String packageName = browser.getPackageName();
            
            if (packageName.isEmpty()) return false; // Custom package não implementado ainda
            
            // Verificar se o app está instalado
            try {
                pm.getPackageInfo(packageName, PackageManager.GET_ACTIVITIES);
            } catch (PackageManager.NameNotFoundException e) {
                Log.w(TAG, "⚠️ Browser not installed: " + browser.getDisplayName());
                return false;
            }
            
            // Configurar package específico
            intent.setPackage(packageName);
            
            // Configurações específicas por browser
            configureBrowserSpecifics(intent, browser);
            
            startActivity(intent);
            Log.d(TAG, String.format("✅ Opened in %s: %s", browser.getDisplayName(), searchUrl));
            return true;
            
        } catch (Exception e) {
            Log.e(TAG, String.format("❌ Failed to open %s: %s", browser.getDisplayName(), e.getMessage()));
            return false;
        }
    }
    
    private void configureBrowserSpecifics(Intent intent, AppConfig.BrowserApp browser) {
        switch (browser) {
            case BING:
                // Configurações específicas do Bing
                intent.putExtra("msrewards", true);
                break;
            case EDGE:
                // Configurações específicas do Edge
                intent.putExtra("edge_rewards", true);
                break;
            case CHROME:
            case CHROME_BETA:
                // Configurações específicas do Chrome
                if (config.isIncognitoModeEnabled()) {
                    intent.putExtra("com.google.android.apps.chrome.EXTRA_OPEN_NEW_INCOGNITO_TAB", true);
                }
                break;
        }
    }
    
    private boolean tryOpenInDefaultBrowser(Intent intent, String searchUrl) {
        try {
            PackageManager pm = getPackageManager();
            intent.setPackage(null); // Remove package restriction
            
            if (intent.resolveActivity(pm) != null) {
                startActivity(intent);
                Log.d(TAG, "✅ Opened in default browser: " + searchUrl);
                return true;
            } else {
                Log.e(TAG, "❌ No browser available to open URL");
                return false;
            }
            
        } catch (Exception e) {
            Log.e(TAG, "❌ Error opening default browser: " + e.getMessage());
            return false;
        }
    }

    private void completeAutomation() {
        isRunning = false;
        updateNotification("🎉 Automação avançada concluída!");
        updateFloatingButton("COMPLETED");
        
        Log.d(TAG, "🏁 Advanced search automation completed");
        
        // Parar serviço após alguns segundos
        handler.postDelayed(() -> {
            stopSelf();
        }, 5000);
    }

    private void updateNotification(String message) {
        NotificationManager manager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        manager.notify(NOTIFICATION_ID, createNotification(message));
    }

    private void updateFloatingButton(String status) {
        // Não enviar broadcast se estiver em modo agendado
        if (scheduledMode) {
            return;
        }
        
        Intent intent = new Intent(FloatingButtonService.ACTION_UPDATE_PROGRESS);
        intent.putExtra(FloatingButtonService.EXTRA_CURRENT_INDEX, currentSearchIndex);
        intent.putExtra(FloatingButtonService.EXTRA_TOTAL_COUNT, searchItems != null ? searchItems.size() : 0);
        intent.putExtra(FloatingButtonService.EXTRA_CURRENT_SEARCH, 
            searchItems != null && currentSearchIndex < searchItems.size() ? 
            searchItems.get(currentSearchIndex).getSearchText() : "");
        intent.putExtra(FloatingButtonService.EXTRA_STATUS, status);
        
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }

    private void togglePauseResume() {
        isPaused = !isPaused;
        
        if (isPaused) {
            Log.d(TAG, "⏸️ Automation paused");
            updateNotification("⏸️ Automação pausada - Clique em play para continuar");
            updateFloatingButton("PAUSED");
            
            // Parar handlers ativos
            if (countdownHandler != null) {
                countdownHandler.removeCallbacksAndMessages(null);
            }
        } else {
            Log.d(TAG, "▶️ Automation resumed");
            updateNotification("▶️ Automação retomada");
            updateFloatingButton("RESUMED");
            
            // Retomar automação
            handler.postDelayed(this::startSearchAutomation, 1000);
        }
    }
    
    private void stopAutomation() {
        Log.d(TAG, "🛑 Stopping automation via floating button");
        isRunning = false;
        isPaused = false;
        
        // Parar todos os handlers
        if (handler != null) {
            handler.removeCallbacksAndMessages(null);
        }
        if (countdownHandler != null) {
            countdownHandler.removeCallbacksAndMessages(null);
        }
        
        updateNotification("🛑 Automação interrompida pelo usuário");
        updateFloatingButton("COMPLETED");
        
        // Parar o serviço
        stopSelf();
    }
    
    @Override
    public void onDestroy() {
        super.onDestroy();
        isRunning = false;
        isPaused = false;
        
        if (handler != null) {
            handler.removeCallbacksAndMessages(null);
        }
        
        if (countdownHandler != null) {
            countdownHandler.removeCallbacksAndMessages(null);
        }
        
        // Desregistrar receiver
        LocalBroadcastManager.getInstance(this).unregisterReceiver(controlReceiver);
        
        Log.d(TAG, "🛑 Advanced SearchAutomationService destroyed");
    }
}