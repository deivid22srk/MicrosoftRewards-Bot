package com.deivid22srk.microsoftrewards.utils;

import android.util.Log;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;

/**
 * 🔐 Gerenciador de Permissões ROOT
 * Solicita, verifica e executa comandos como superusuário
 */
public class RootManager {
    
    private static final String TAG = "RootManager";
    private static RootManager instance;
    private boolean isRootAvailable = false;
    private boolean isRootGranted = false;
    
    private RootManager() {
        checkRootAccess();
    }
    
    public static synchronized RootManager getInstance() {
        if (instance == null) {
            instance = new RootManager();
        }
        return instance;
    }
    
    /**
     * Verifica se o dispositivo tem ROOT disponível
     */
    public boolean isRootAvailable() {
        return isRootAvailable;
    }
    
    /**
     * Verifica se o ROOT foi concedido ao app
     */
    public boolean isRootGranted() {
        return isRootGranted;
    }
    
    /**
     * Verifica acesso ROOT
     */
    private void checkRootAccess() {
        try {
            // Tentar executar comando su
            Process process = Runtime.getRuntime().exec("su");
            DataOutputStream os = new DataOutputStream(process.getOutputStream());
            
            // Comando simples para testar
            os.writeBytes("id\n");
            os.writeBytes("exit\n");
            os.flush();
            
            // Aguardar resposta
            int exitCode = process.waitFor();
            
            if (exitCode == 0) {
                isRootAvailable = true;
                
                // Verificar se realmente tem acesso root lendo o output
                BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
                String line = reader.readLine();
                
                if (line != null && line.contains("uid=0")) {
                    isRootGranted = true;
                    Log.d(TAG, "✅ ROOT disponível e concedido!");
                } else {
                    Log.w(TAG, "⚠️ ROOT disponível mas não concedido");
                }
            } else {
                Log.w(TAG, "❌ ROOT não disponível neste dispositivo");
            }
            
        } catch (Exception e) {
            Log.w(TAG, "❌ Erro ao verificar ROOT: " + e.getMessage());
            isRootAvailable = false;
            isRootGranted = false;
        }
    }
    
    /**
     * Solicita permissões ROOT (mostra popup do SuperSU/Magisk)
     */
    public boolean requestRootAccess() {
        Log.d(TAG, "📱 Solicitando permissões ROOT...");
        
        try {
            // Executar comando que requer root
            Process process = Runtime.getRuntime().exec("su");
            DataOutputStream os = new DataOutputStream(process.getOutputStream());
            
            // Comando que solicita permissão
            os.writeBytes("echo 'ROOT_ACCESS_TEST'\n");
            os.writeBytes("exit\n");
            os.flush();
            
            // Aguardar resposta (com timeout)
            process.waitFor();
            
            // Verificar novamente
            checkRootAccess();
            
            if (isRootGranted) {
                Log.d(TAG, "✅ Permissões ROOT concedidas!");
                return true;
            } else {
                Log.w(TAG, "❌ Permissões ROOT negadas pelo usuário");
                return false;
            }
            
        } catch (Exception e) {
            Log.e(TAG, "❌ Erro ao solicitar ROOT: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Executa comando como ROOT
     */
    public String executeRootCommand(String command) {
        if (!isRootGranted) {
            Log.w(TAG, "⚠️ Tentando executar comando sem ROOT: " + command);
            return null;
        }
        
        try {
            Log.d(TAG, "🔧 Executando comando ROOT: " + command);
            
            Process process = Runtime.getRuntime().exec("su");
            DataOutputStream os = new DataOutputStream(process.getOutputStream());
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            
            os.writeBytes(command + "\n");
            os.writeBytes("exit\n");
            os.flush();
            
            StringBuilder output = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                output.append(line).append("\n");
            }
            
            process.waitFor();
            
            String result = output.toString().trim();
            Log.d(TAG, "✅ Resultado: " + result);
            return result;
            
        } catch (Exception e) {
            Log.e(TAG, "❌ Erro ao executar comando ROOT: " + e.getMessage());
            return null;
        }
    }
    
    /**
     * Executa múltiplos comandos como ROOT em sequência
     */
    public boolean executeRootCommands(String... commands) {
        if (!isRootGranted) {
            Log.w(TAG, "⚠️ ROOT não concedido, não é possível executar comandos");
            return false;
        }
        
        try {
            Process process = Runtime.getRuntime().exec("su");
            DataOutputStream os = new DataOutputStream(process.getOutputStream());
            
            for (String command : commands) {
                Log.d(TAG, "🔧 Executando: " + command);
                os.writeBytes(command + "\n");
            }
            
            os.writeBytes("exit\n");
            os.flush();
            
            int exitCode = process.waitFor();
            
            if (exitCode == 0) {
                Log.d(TAG, "✅ Comandos executados com sucesso");
                return true;
            } else {
                Log.w(TAG, "⚠️ Comandos executados com código: " + exitCode);
                return false;
            }
            
        } catch (Exception e) {
            Log.e(TAG, "❌ Erro ao executar comandos ROOT: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Desabilita otimização de bateria usando ROOT
     */
    public boolean disableBatteryOptimization(String packageName) {
        String command = "dumpsys deviceidle whitelist +" + packageName;
        String result = executeRootCommand(command);
        return result != null && !result.contains("error");
    }
    
    /**
     * Acorda o dispositivo usando ROOT
     */
    public boolean wakeDevice() {
        return executeRootCommands(
            "input keyevent KEYCODE_WAKEUP",
            "input keyevent KEYCODE_MENU"
        );
    }
    
    /**
     * Abre URL no navegador usando ROOT
     */
    public boolean openUrlInBrowser(String url, String browserPackage) {
        String command = String.format(
            "am start -a android.intent.action.VIEW -d \"%s\" -n %s",
            url, 
            browserPackage
        );
        
        String result = executeRootCommand(command);
        return result != null && result.contains("Starting");
    }
    
    /**
     * Mantém tela ligada usando ROOT
     */
    public boolean keepScreenAwake() {
        return executeRootCommands(
            "settings put system screen_off_timeout 2147483647"
        );
    }
    
    /**
     * Restaura timeout normal da tela
     */
    public boolean restoreScreenTimeout() {
        return executeRootCommands(
            "settings put system screen_off_timeout 30000"
        );
    }
    
    /**
     * Verifica se o dispositivo está em Doze mode
     */
    public boolean isInDozeMode() {
        String result = executeRootCommand("dumpsys deviceidle get deep");
        return result != null && result.contains("IDLE");
    }
    
    /**
     * Desabilita Doze mode temporariamente
     */
    public boolean disableDozeMode() {
        return executeRootCommands(
            "dumpsys deviceidle disable"
        );
    }
    
    /**
     * Habilita Doze mode novamente
     */
    public boolean enableDozeMode() {
        return executeRootCommands(
            "dumpsys deviceidle enable"
        );
    }
    
    /**
     * Força o CPU a ficar ligado
     */
    public boolean forceCpuAwake() {
        return executeRootCommands(
            "echo 'performance' > /sys/devices/system/cpu/cpu0/cpufreq/scaling_governor"
        );
    }
}
