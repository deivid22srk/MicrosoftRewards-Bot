package com.deivid22srk.microsoftrewards.service;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.PixelFormat;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.view.ContextThemeWrapper;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.core.app.NotificationCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.deivid22srk.microsoftrewards.MainActivity;
import com.deivid22srk.microsoftrewards.R;
import com.deivid22srk.microsoftrewards.model.SearchItem;
import com.google.android.material.progressindicator.CircularProgressIndicator;

import java.util.ArrayList;
import java.util.List;

public class FloatingButtonService extends Service {
    
    private static final String CHANNEL_ID = "FloatingButtonChannel";
    private static final int NOTIFICATION_ID = 1;
    
    public static final String ACTION_UPDATE_PROGRESS = "com.deivid22srk.microsoftrewards.UPDATE_PROGRESS";
    public static final String EXTRA_CURRENT_INDEX = "current_index";
    public static final String EXTRA_TOTAL_COUNT = "total_count";
    public static final String EXTRA_CURRENT_SEARCH = "current_search";
    public static final String EXTRA_STATUS = "status";
    
    private WindowManager windowManager;
    private View floatingView;
    private TextView progressText;
    private ImageView floatingIcon;
    private CircularProgressIndicator progressBar;
    
    private int currentIndex = 0;
    private int totalCount = 0;
    private String currentSearch = "";
    private boolean isRunning = false;
    
    private List<SearchItem> searchItems;
    
    private BroadcastReceiver progressReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (ACTION_UPDATE_PROGRESS.equals(intent.getAction())) {
                currentIndex = intent.getIntExtra(EXTRA_CURRENT_INDEX, 0);
                totalCount = intent.getIntExtra(EXTRA_TOTAL_COUNT, 0);
                currentSearch = intent.getStringExtra(EXTRA_CURRENT_SEARCH);
                String status = intent.getStringExtra(EXTRA_STATUS);
                
                updateFloatingButton(status);
            }
        }
    };

    @Override
    public void onCreate() {
        super.onCreate();
        createNotificationChannel();
        initializeFloatingButton();
        
        // Registrar receiver para updates de progresso
        LocalBroadcastManager.getInstance(this).registerReceiver(
            progressReceiver, new IntentFilter(ACTION_UPDATE_PROGRESS)
        );
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null && intent.hasExtra("searchItems")) {
            ArrayList<SearchItem> items = (ArrayList<SearchItem>) intent.getSerializableExtra("searchItems");
            if (items != null) {
                searchItems = items;
                totalCount = searchItems.size();
                isRunning = true;
                updateFloatingButton("STARTED");
            }
        }
        
        startForeground(NOTIFICATION_ID, createNotification());
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
                "Floating Button Service",
                NotificationManager.IMPORTANCE_LOW
            );
            channel.setDescription("Mantém o botão flutuante ativo");
            
            NotificationManager manager = getSystemService(NotificationManager.class);
            manager.createNotificationChannel(channel);
        }
    }

    private Notification createNotification() {
        Intent notificationIntent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(
            this, 0, notificationIntent, 
            PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        return new NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle(getString(R.string.floating_button_notification_title))
            .setContentText(getString(R.string.floating_button_notification_text))
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .build();
    }

    private void initializeFloatingButton() {
        windowManager = (WindowManager) getSystemService(WINDOW_SERVICE);
        
        // Criar contexto com tema AppCompat para Material Components
        Context themedContext = new ContextThemeWrapper(this, R.style.FloatingButtonTheme);
        LayoutInflater inflater = LayoutInflater.from(themedContext);
        floatingView = inflater.inflate(R.layout.floating_button_layout, null);
        
        // Inicializar views
        progressText = floatingView.findViewById(R.id.floatingProgressText);
        floatingIcon = floatingView.findViewById(R.id.floatingIcon);
        progressBar = floatingView.findViewById(R.id.floatingProgressBar);
        
        // Configurar parâmetros da janela
        WindowManager.LayoutParams params = new WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.O ?
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY :
                WindowManager.LayoutParams.TYPE_PHONE,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
            PixelFormat.TRANSLUCENT
        );
        
        params.gravity = Gravity.TOP | Gravity.START;
        params.x = 0;
        params.y = 100;
        
        // Adicionar touch listener para arrastar
        setupTouchListener(params);
        
        // Adicionar à janela
        windowManager.addView(floatingView, params);
        
        // Click listener para abrir app principal
        floatingView.setOnClickListener(v -> {
            Intent intent = new Intent(this, MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
        });
        
        // Estado inicial
        updateFloatingButton("IDLE");
    }

    private void setupTouchListener(WindowManager.LayoutParams params) {
        floatingView.setOnTouchListener(new View.OnTouchListener() {
            private int initialX;
            private int initialY;
            private float initialTouchX;
            private float initialTouchY;

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        initialX = params.x;
                        initialY = params.y;
                        initialTouchX = event.getRawX();
                        initialTouchY = event.getRawY();
                        return true;
                        
                    case MotionEvent.ACTION_UP:
                        int xDiff = (int) (event.getRawX() - initialTouchX);
                        int yDiff = (int) (event.getRawY() - initialTouchY);
                        
                        // Se foi um clique (pouco movimento), não processar como drag
                        if (Math.abs(xDiff) < 10 && Math.abs(yDiff) < 10) {
                            return false; // Permite que o OnClickListener seja chamado
                        }
                        return true;
                        
                    case MotionEvent.ACTION_MOVE:
                        params.x = initialX + (int) (event.getRawX() - initialTouchX);
                        params.y = initialY + (int) (event.getRawY() - initialTouchY);
                        
                        windowManager.updateViewLayout(floatingView, params);
                        return true;
                }
                return false;
            }
        });
    }

    private void updateFloatingButton(String status) {
        if (floatingView == null) return;
        
        new Handler(Looper.getMainLooper()).post(() -> {
            // Atualizar texto de progresso
            progressText.setText(currentIndex + "/" + totalCount);
            
            // Atualizar barra de progresso
            if (totalCount > 0) {
                int progress = (int) ((currentIndex / (float) totalCount) * 100);
                progressBar.setProgress(progress);
            }
            
            // Atualizar aparência baseada no status
            switch (status) {
                case "STARTED":
                case "IN_PROGRESS":
                    progressBar.setVisibility(View.VISIBLE);
                    floatingIcon.setAlpha(0.8f);
                    break;
                    
                case "COMPLETED":
                    progressBar.setVisibility(View.GONE);
                    floatingIcon.setAlpha(1.0f);
                    // Pode adicionar animação de sucesso aqui
                    break;
                    
                case "IDLE":
                default:
                    progressBar.setVisibility(View.GONE);
                    floatingIcon.setAlpha(1.0f);
                    break;
            }
        });
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        
        if (floatingView != null && windowManager != null) {
            windowManager.removeView(floatingView);
        }
        
        LocalBroadcastManager.getInstance(this).unregisterReceiver(progressReceiver);
    }
}