package com.deivid22srk.microsoftrewards.service;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.ComponentName;
import android.content.Intent;
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

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

public class SearchAutomationService extends Service {
    
    private static final String TAG = "SearchAutomationService";
    private static final String CHANNEL_ID = "SearchAutomationChannel";
    private static final int NOTIFICATION_ID = 2;
    private static final int SEARCH_INTERVAL_MS = 5000; // 5 segundos
    private static final int COUNTDOWN_INTERVAL_MS = 1000; // 1 segundo para countdown
    
    private Handler handler;
    private Handler countdownHandler;
    private List<SearchItem> searchItems;
    private int currentSearchIndex = 0;
    private boolean isRunning = false;
    private int countdownSeconds = 5;

    @Override
    public void onCreate() {
        super.onCreate();
        createNotificationChannel();
        handler = new Handler(Looper.getMainLooper());
        countdownHandler = new Handler(Looper.getMainLooper());
        Log.d(TAG, "SearchAutomationService created");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null && intent.hasExtra("searchItems")) {
            ArrayList<SearchItem> items = (ArrayList<SearchItem>) intent.getSerializableExtra("searchItems");
            if (items != null) {
                searchItems = items;
                currentSearchIndex = 0;
                isRunning = true;
                
                startForeground(NOTIFICATION_ID, createNotification("Iniciando automação..."));
                startSearchAutomation();
                
                Log.d(TAG, "Search automation started with " + searchItems.size() + " items");
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
                "Search Automation Service",
                NotificationManager.IMPORTANCE_LOW
            );
            channel.setDescription("Executa pesquisas automáticas");
            
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
            .setContentTitle("Microsoft Rewards Bot")
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

        // Iniciar countdown antes da próxima pesquisa
        if (currentSearchIndex > 0) {
            startCountdown();
        } else {
            // Primeira pesquisa, executar imediatamente
            executeCurrentSearch();
        }
    }

    private void startCountdown() {
        countdownSeconds = 5;
        runCountdown();
    }

    private void runCountdown() {
        if (!isRunning) return;
        
        if (countdownSeconds > 0) {
            updateNotification("Próxima pesquisa em " + countdownSeconds + " segundos");
            updateFloatingButton("COUNTDOWN");
            
            countdownSeconds--;
            countdownHandler.postDelayed(this::runCountdown, COUNTDOWN_INTERVAL_MS);
        } else {
            executeCurrentSearch();
        }
    }

    private void executeCurrentSearch() {
        if (!isRunning || searchItems == null || currentSearchIndex >= searchItems.size()) {
            completeAutomation();
            return;
        }

        SearchItem currentItem = searchItems.get(currentSearchIndex);
        currentItem.setStatus(SearchItem.SearchStatus.IN_PROGRESS);
        
        Log.d(TAG, "Executing search: " + currentItem.getSearchText());
        
        updateNotification("Pesquisando: " + currentItem.getSearchText());
        updateFloatingButton("IN_PROGRESS");

        // Abrir Chrome com a pesquisa
        boolean success = openChromeSearch(currentItem.getSearchText());
        
        if (success) {
            currentItem.setStatus(SearchItem.SearchStatus.COMPLETED);
            Log.d(TAG, "Search completed successfully: " + currentItem.getSearchText());
        } else {
            currentItem.setStatus(SearchItem.SearchStatus.FAILED);
            Log.e(TAG, "Search failed: " + currentItem.getSearchText());
        }

        // Atualizar progresso
        currentSearchIndex++;
        updateFloatingButton("COMPLETED");

        // Agendar próxima pesquisa
        handler.postDelayed(this::startSearchAutomation, 2000); // 2 segundos para mostrar resultado
    }

    private boolean openChromeSearch(String searchQuery) {
        try {
            // Codificar a query para URL
            String encodedQuery = URLEncoder.encode(searchQuery, "UTF-8");
            String searchUrl = "https://www.bing.com/search?q=" + encodedQuery;
            
            // Tentar abrir no Chrome especificamente
            Intent chromeIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(searchUrl));
            chromeIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            chromeIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            
            // Verificar se Chrome está disponível
            PackageManager pm = getPackageManager();
            
            // Tentar diferentes pacotes do Chrome
            String[] chromePackages = {
                "com.android.chrome",           // Chrome
                "com.chrome.beta",              // Chrome Beta
                "com.chrome.dev",               // Chrome Dev
                "com.chrome.canary",            // Chrome Canary
                "org.chromium.chrome"           // Chromium
            };
            
            boolean chromeAvailable = false;
            for (String packageName : chromePackages) {
                try {
                    pm.getPackageInfo(packageName, PackageManager.GET_ACTIVITIES);
                    chromeIntent.setPackage(packageName);
                    chromeAvailable = true;
                    Log.d(TAG, "Found Chrome package: " + packageName);
                    break;
                } catch (PackageManager.NameNotFoundException e) {
                    // Continue para próximo pacote
                }
            }
            
            if (chromeAvailable) {
                startActivity(chromeIntent);
                Log.d(TAG, "Opened in Chrome: " + searchUrl);
                return true;
            } else {
                // Se Chrome não estiver disponível, usar navegador padrão
                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(searchUrl));
                browserIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                
                if (browserIntent.resolveActivity(pm) != null) {
                    startActivity(browserIntent);
                    Log.d(TAG, "Opened in default browser: " + searchUrl);
                    return true;
                } else {
                    Log.e(TAG, "No browser available to open URL");
                    return false;
                }
            }
            
        } catch (UnsupportedEncodingException e) {
            Log.e(TAG, "Error encoding search query: " + e.getMessage());
            return false;
        } catch (Exception e) {
            Log.e(TAG, "Error opening browser: " + e.getMessage());
            return false;
        }
    }

    private void completeAutomation() {
        isRunning = false;
        updateNotification("Automação concluída!");
        updateFloatingButton("COMPLETED");
        
        Log.d(TAG, "Search automation completed");
        
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
        Intent intent = new Intent(FloatingButtonService.ACTION_UPDATE_PROGRESS);
        intent.putExtra(FloatingButtonService.EXTRA_CURRENT_INDEX, currentSearchIndex);
        intent.putExtra(FloatingButtonService.EXTRA_TOTAL_COUNT, searchItems != null ? searchItems.size() : 0);
        intent.putExtra(FloatingButtonService.EXTRA_CURRENT_SEARCH, 
            searchItems != null && currentSearchIndex < searchItems.size() ? 
            searchItems.get(currentSearchIndex).getSearchText() : "");
        intent.putExtra(FloatingButtonService.EXTRA_STATUS, status);
        
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        isRunning = false;
        
        if (handler != null) {
            handler.removeCallbacksAndMessages(null);
        }
        
        if (countdownHandler != null) {
            countdownHandler.removeCallbacksAndMessages(null);
        }
        
        Log.d(TAG, "SearchAutomationService destroyed");
    }
}