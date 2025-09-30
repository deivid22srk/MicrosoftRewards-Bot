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
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
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
    public static final String ACTION_PAUSE_RESUME = "com.deivid22srk.microsoftrewards.PAUSE_RESUME";
    public static final String ACTION_STOP_AUTOMATION = "com.deivid22srk.microsoftrewards.STOP_AUTOMATION";
    public static final String EXTRA_CURRENT_INDEX = "current_index";
    public static final String EXTRA_TOTAL_COUNT = "total_count";
    public static final String EXTRA_CURRENT_SEARCH = "current_search";
    public static final String EXTRA_STATUS = "status";
    public static final String EXTRA_IS_PAUSED = "is_paused";
    
    private WindowManager windowManager;
    private View floatingView;
    private TextView progressText;
    private ImageView floatingIcon;
    private CircularProgressIndicator progressBar;
    private LinearLayout controlButtonsContainer;
    private ImageView pausePlayButton;
    private ImageView stopButton;
    private RelativeLayout dragArea;
    private ImageView dragHandle;
    private TextView statusText;
    
    private int currentIndex = 0;
    private int totalCount = 0;
    private String currentSearch = "";
    private boolean isRunning = false;
    private boolean isPaused = false;
    
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
        
        // Inicializar views com novo layout
        progressText = floatingView.findViewById(R.id.floatingProgressText);
        floatingIcon = floatingView.findViewById(R.id.floatingIcon);
        progressBar = floatingView.findViewById(R.id.floatingProgressBar);
        controlButtonsContainer = floatingView.findViewById(R.id.controlButtonsContainer);
        pausePlayButton = floatingView.findViewById(R.id.pausePlayButton);
        stopButton = floatingView.findViewById(R.id.stopButton);
        dragArea = floatingView.findViewById(R.id.dragArea);
        dragHandle = floatingView.findViewById(R.id.dragHandle);
        statusText = floatingView.findViewById(R.id.statusText);
        
        // Configurar parâmetros da janela otimizados PRIMEIRO
        WindowManager.LayoutParams params = new WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.O ?
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY :
                WindowManager.LayoutParams.TYPE_PHONE,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
            PixelFormat.TRANSLUCENT
        );
        
        params.gravity = Gravity.TOP | Gravity.END; // Posição inicial melhor
        params.x = 50;
        params.y = 200;
        
        // Configurar click listeners dos botões de controle
        setupControlButtons();
        
        // Configurar sistema de drag melhorado
        setupImprovedDragSystem(params);
        
        // Configurar progress bar inicial
        progressBar.setMax(100);
        progressBar.setProgress(0);
        
        // Estado inicial
        updateFloatingButton("IDLE");
        
        // Adicionar à janela
        windowManager.addView(floatingView, params);
    }
    
    /**
     * Sistema de drag melhorado com áreas específicas
     */
    private void setupImprovedDragSystem(WindowManager.LayoutParams params) {
        // Área de drag principal - apenas a dragArea pode ser usada para arrastar
        dragArea.setOnTouchListener(new View.OnTouchListener() {
            private int initialX;
            private int initialY;
            private float initialTouchX;
            private float initialTouchY;
            private boolean isDragging = false;

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        initialX = params.x;
                        initialY = params.y;
                        initialTouchX = event.getRawX();
                        initialTouchY = event.getRawY();
                        isDragging = false;
                        
                        // Feedback visual ao tocar
                        dragHandle.setAlpha(1.0f);
                        dragArea.setAlpha(0.9f);
                        return true;
                        
                    case MotionEvent.ACTION_MOVE:
                        int deltaX = (int) (event.getRawX() - initialTouchX);
                        int deltaY = (int) (event.getRawY() - initialTouchY);
                        
                        // Detectar se é um movimento de drag (não apenas um tap)
                        if (!isDragging && (Math.abs(deltaX) > 15 || Math.abs(deltaY) > 15)) {
                            isDragging = true;
                            // Feedback visual durante drag
                            dragArea.setAlpha(0.7f);
                        }
                        
                        if (isDragging) {
                            params.x = initialX - deltaX; // Inverter X para Gravity.END
                            params.y = initialY + deltaY;
                            
                            // Manter dentro dos limites da tela
                            params.x = Math.max(0, Math.min(params.x, getScreenWidth() - floatingView.getWidth()));
                            params.y = Math.max(0, Math.min(params.y, getScreenHeight() - floatingView.getHeight()));
                            
                            windowManager.updateViewLayout(floatingView, params);
                        }
                        return true;
                        
                    case MotionEvent.ACTION_UP:
                    case MotionEvent.ACTION_CANCEL:
                        // Restaurar feedback visual
                        dragHandle.setAlpha(0.7f);
                        dragArea.setAlpha(1.0f);
                        
                        if (!isDragging) {
                            // Se não foi drag, tratar como click para abrir app principal
                            Intent intent = new Intent(FloatingButtonService.this, MainActivity.class);
                            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            startActivity(intent);
                        }
                        return true;
                }
                return false;
            }
        });
        
        // Remover o touch listener antigo do floatingView
        // floatingView.setOnTouchListener(null);
    }
    
    private int getScreenWidth() {
        return getResources().getDisplayMetrics().widthPixels;
    }
    
    private int getScreenHeight() {
        return getResources().getDisplayMetrics().heightPixels;
    }



    private void setupControlButtons() {
        // Botão pause/play
        pausePlayButton.setOnClickListener(v -> {
            sendPauseResumeCommand();
        });
        
        // Botão stop
        stopButton.setOnClickListener(v -> {
            sendStopCommand();
        });
    }
    
    private void sendPauseResumeCommand() {
        Intent intent = new Intent(ACTION_PAUSE_RESUME);
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
        
        // Inverter estado de pause localmente para atualizar UI imediatamente
        isPaused = !isPaused;
        updatePausePlayButton();
    }
    
    private void sendStopCommand() {
        Intent intent = new Intent(ACTION_STOP_AUTOMATION);
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }
    
    private void updatePausePlayButton() {
        if (isPaused) {
            pausePlayButton.setImageResource(R.drawable.ic_play_arrow);
        } else {
            pausePlayButton.setImageResource(R.drawable.ic_pause);
        }
    }
    
    private void updateFloatingButton(String status) {
        if (floatingView == null) return;
        
        new Handler(Looper.getMainLooper()).post(() -> {
            // Atualizar texto de progresso
            progressText.setText(currentIndex + "/" + totalCount);
            
            // Atualizar barra de progresso com correção de cores
            if (totalCount > 0) {
                int progress = (int) ((currentIndex / (float) totalCount) * 100);
                progressBar.setProgress(progress);
                
                // Garantir que a progress bar tenha as cores corretas
                int[] colors = { getResources().getColor(R.color.microsoft_green, null) };
                progressBar.setIndicatorColor(colors);
                
                // Forçar redraw para garantir que as cores sejam aplicadas
                progressBar.invalidate();
            }
            
            // Atualizar texto de status
            String statusText = "";
            
            // Atualizar aparência baseada no status
            switch (status) {
                case "STARTED":
                case "IN_PROGRESS":
                    progressBar.setVisibility(View.VISIBLE);
                    controlButtonsContainer.setVisibility(View.VISIBLE);
                    floatingIcon.setAlpha(0.9f);
                    statusText = "🚀 Ativo";
                    isRunning = true;
                    break;
                    
                case "PAUSED":
                    progressBar.setVisibility(View.VISIBLE);
                    controlButtonsContainer.setVisibility(View.VISIBLE);
                    floatingIcon.setAlpha(0.6f);
                    statusText = "⏸️ Pausado";
                    isPaused = true;
                    updatePausePlayButton();
                    break;
                    
                case "RESUMED":
                    progressBar.setVisibility(View.VISIBLE);
                    controlButtonsContainer.setVisibility(View.VISIBLE);
                    floatingIcon.setAlpha(0.9f);
                    statusText = "▶️ Executando";
                    isPaused = false;
                    updatePausePlayButton();
                    break;
                    
                case "COMPLETED":
                    progressBar.setVisibility(View.GONE);
                    controlButtonsContainer.setVisibility(View.GONE);
                    floatingIcon.setAlpha(1.0f);
                    statusText = "✅ Concluído";
                    isRunning = false;
                    isPaused = false;
                    // Animação de sucesso
                    animateSuccess();
                    break;
                    
                case "COUNTDOWN":
                    progressBar.setVisibility(View.VISIBLE);
                    controlButtonsContainer.setVisibility(View.VISIBLE);
                    floatingIcon.setAlpha(0.7f);
                    statusText = "⏰ Aguardando";
                    break;
                    
                case "IDLE":
                default:
                    progressBar.setVisibility(View.GONE);
                    controlButtonsContainer.setVisibility(View.GONE);
                    floatingIcon.setAlpha(1.0f);
                    statusText = "😴 Inativo";
                    isRunning = false;
                    isPaused = false;
                    break;
            }
            
            // Atualizar texto de status
            this.statusText.setText(statusText);
            this.statusText.setVisibility(isRunning || isPaused ? View.VISIBLE : View.GONE);
            
            // Atualizar botão de pause/play se necessário
            if (isRunning) {
                updatePausePlayButton();
            }
        });
    }
    
    private void animateSuccess() {
        // Animação simples de sucesso
        floatingIcon.animate()
                .scaleX(1.2f)
                .scaleY(1.2f)
                .setDuration(200)
                .withEndAction(() -> {
                    floatingIcon.animate()
                            .scaleX(1.0f)
                            .scaleY(1.0f)
                            .setDuration(200);
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