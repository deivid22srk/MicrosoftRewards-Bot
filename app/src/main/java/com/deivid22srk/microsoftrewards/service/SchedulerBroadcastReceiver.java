package com.deivid22srk.microsoftrewards.service;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;

import com.deivid22srk.microsoftrewards.utils.AppConfig;

public class SchedulerBroadcastReceiver extends BroadcastReceiver {
    
    private static final String TAG = "SchedulerBroadcastReceiver";
    private static final String ACTION_SCHEDULED_SEARCH = "com.deivid22srk.microsoftrewards.SCHEDULED_SEARCH";
    private static final String ACTION_TEST_SEARCH = "com.deivid22srk.microsoftrewards.TEST_SCHEDULED_SEARCH";
    
    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        Log.d(TAG, "📬 Broadcast recebido: " + action);
        
        if (ACTION_SCHEDULED_SEARCH.equals(action) || ACTION_TEST_SEARCH.equals(action)) {
            AppConfig config = AppConfig.getInstance(context);
            
            // Verificar se o agendamento está ativo (exceto para testes)
            if (!ACTION_TEST_SEARCH.equals(action) && !config.isSchedulerEnabled()) {
                Log.w(TAG, "⚠️ Agendamento desativado, ignorando alarme");
                return;
            }
            
            boolean isTest = ACTION_TEST_SEARCH.equals(action);
            Log.d(TAG, "🚀 Iniciando serviço de pesquisas agendadas (Teste: " + isTest + ")");
            
            // Iniciar o serviço de execução
            Intent serviceIntent = new Intent(context, ScheduledSearchService.class);
            serviceIntent.putExtra("isTest", isTest);
            
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(serviceIntent);
            } else {
                context.startService(serviceIntent);
            }
            
            // Reagendar para Android 12+ (que cancela alarmes de longa duração)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && !isTest) {
                rescheduleAlarm(context, config);
            }
        }
    }
    
    private void rescheduleAlarm(Context context, AppConfig config) {
        // Re-agendar o próximo alarme (necessário para Android 12+)
        Log.d(TAG, "🔄 Re-agendando próximo alarme");
        // A lógica de re-agendamento será feita no serviço
    }
}
