package com.deivid22srk.microsoftrewards.utils;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * 🛠️ Sistema de Configurações Avançadas
 * Gerencia todas as configurações personalizáveis do app
 */
public class AppConfig {
    
    private static final String PREFS_NAME = "MicrosoftRewards_AdvancedConfig";
    
    // ⏰ Configurações de Tempo
    private static final String KEY_SEARCH_INTERVAL = "search_interval_seconds";
    private static final String KEY_COUNTDOWN_INTERVAL = "countdown_interval_seconds";
    private static final String KEY_RESULT_DISPLAY_TIME = "result_display_time_seconds";
    
    // 🌐 Configurações de URL
    private static final String KEY_SEARCH_ENGINE = "search_engine";
    private static final String KEY_CUSTOM_URL_TEMPLATE = "custom_url_template";
    private static final String KEY_URL_PC_PARAMETER = "url_pc_parameter";
    private static final String KEY_URL_FORM_PARAMETER = "url_form_parameter";
    
    // 📱 Configurações de App
    private static final String KEY_BROWSER_APP = "browser_app_package";
    private static final String KEY_ENABLE_CHROME_FALLBACK = "enable_chrome_fallback";
    private static final String KEY_USE_INCOGNITO_MODE = "use_incognito_mode";
    
    // 🎯 Configurações de Automação
    private static final String KEY_AUTO_CLOSE_TABS = "auto_close_tabs";
    private static final String KEY_RANDOM_DELAY_ENABLED = "random_delay_enabled";
    private static final String KEY_MIN_RANDOM_DELAY = "min_random_delay";
    private static final String KEY_MAX_RANDOM_DELAY = "max_random_delay";
    
    // 🧠 Configurações de IA
    private static final String KEY_AI_MODE = "ai_mode";
    private static final String KEY_ENABLE_CONTEXTUAL_LEARNING = "enable_contextual_learning";
    private static final String KEY_ENABLE_TEMPORAL_AWARENESS = "enable_temporal_awareness";
    
    // 🔐 Configurações de Segurança
    private static final String KEY_STEALTH_MODE = "stealth_mode";
    private static final String KEY_ROTATE_USER_AGENT = "rotate_user_agent";
    
    // Valores padrão
    public static final int DEFAULT_SEARCH_INTERVAL = 5; // segundos
    public static final int DEFAULT_COUNTDOWN_INTERVAL = 1; // segundos
    public static final int DEFAULT_RESULT_DISPLAY_TIME = 2; // segundos
    public static final String DEFAULT_SEARCH_ENGINE = "bing";
    public static final String DEFAULT_BROWSER_APP = "com.android.chrome";
    public static final String DEFAULT_AI_MODE = "advanced_chatgpt";
    
    // Enum para engines de busca
    public enum SearchEngine {
        BING("bing", "https://www.bing.com/search?q=%s&PC=U316&FORM=CHROMN"),
        GOOGLE("google", "https://www.google.com/search?q=%s"),
        DUCKDUCKGO("duckduckgo", "https://duckduckgo.com/?q=%s"),
        CUSTOM("custom", "");
        
        private final String id;
        private final String urlTemplate;
        
        SearchEngine(String id, String urlTemplate) {
            this.id = id;
            this.urlTemplate = urlTemplate;
        }
        
        public String getId() { return id; }
        public String getUrlTemplate() { return urlTemplate; }
    }
    
    // Enum para apps de navegador
    public enum BrowserApp {
        CHROME("com.android.chrome", "Chrome"),
        CHROME_BETA("com.chrome.beta", "Chrome Beta"),
        BING("com.microsoft.bing", "Microsoft Bing"),
        EDGE("com.microsoft.emmx", "Microsoft Edge"),
        FIREFOX("org.mozilla.firefox", "Firefox"),
        OPERA("com.opera.browser", "Opera"),
        CUSTOM("", "Custom Package");
        
        private final String packageName;
        private final String displayName;
        
        BrowserApp(String packageName, String displayName) {
            this.packageName = packageName;
            this.displayName = displayName;
        }
        
        public String getPackageName() { return packageName; }
        public String getDisplayName() { return displayName; }
    }
    
    // Enum para modos de IA
    public enum AIMode {
        BASIC("basic", "IA Básica"),
        ADVANCED("advanced", "IA Avançada"),
        CHATGPT("advanced_chatgpt", "IA Tipo ChatGPT"),
        CUSTOM("custom", "Personalizada");
        
        private final String id;
        private final String displayName;
        
        AIMode(String id, String displayName) {
            this.id = id;
            this.displayName = displayName;
        }
        
        public String getId() { return id; }
        public String getDisplayName() { return displayName; }
    }
    
    private Context context;
    private SharedPreferences prefs;
    
    public AppConfig(Context context) {
        this.context = context;
        this.prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }
    
    // ⏰ Getters e Setters para Configurações de Tempo
    public int getSearchInterval() {
        return prefs.getInt(KEY_SEARCH_INTERVAL, DEFAULT_SEARCH_INTERVAL);
    }
    
    public void setSearchInterval(int seconds) {
        prefs.edit().putInt(KEY_SEARCH_INTERVAL, seconds).apply();
    }
    
    public int getCountdownInterval() {
        return prefs.getInt(KEY_COUNTDOWN_INTERVAL, DEFAULT_COUNTDOWN_INTERVAL);
    }
    
    public void setCountdownInterval(int seconds) {
        prefs.edit().putInt(KEY_COUNTDOWN_INTERVAL, seconds).apply();
    }
    
    public int getResultDisplayTime() {
        return prefs.getInt(KEY_RESULT_DISPLAY_TIME, DEFAULT_RESULT_DISPLAY_TIME);
    }
    
    public void setResultDisplayTime(int seconds) {
        prefs.edit().putInt(KEY_RESULT_DISPLAY_TIME, seconds).apply();
    }
    
    // 🌐 Getters e Setters para Configurações de URL
    public SearchEngine getSearchEngine() {
        String engineId = prefs.getString(KEY_SEARCH_ENGINE, DEFAULT_SEARCH_ENGINE);
        for (SearchEngine engine : SearchEngine.values()) {
            if (engine.getId().equals(engineId)) {
                return engine;
            }
        }
        return SearchEngine.BING;
    }
    
    public void setSearchEngine(SearchEngine engine) {
        prefs.edit().putString(KEY_SEARCH_ENGINE, engine.getId()).apply();
    }
    
    public String getCustomUrlTemplate() {
        return prefs.getString(KEY_CUSTOM_URL_TEMPLATE, SearchEngine.BING.getUrlTemplate());
    }
    
    public void setCustomUrlTemplate(String template) {
        prefs.edit().putString(KEY_CUSTOM_URL_TEMPLATE, template).apply();
    }
    
    public String getUrlPcParameter() {
        return prefs.getString(KEY_URL_PC_PARAMETER, "U316");
    }
    
    public void setUrlPcParameter(String parameter) {
        prefs.edit().putString(KEY_URL_PC_PARAMETER, parameter).apply();
    }
    
    public String getUrlFormParameter() {
        return prefs.getString(KEY_URL_FORM_PARAMETER, "CHROMN");
    }
    
    public void setUrlFormParameter(String parameter) {
        prefs.edit().putString(KEY_URL_FORM_PARAMETER, parameter).apply();
    }
    
    // 📱 Getters e Setters para Configurações de App
    public BrowserApp getBrowserApp() {
        String packageName = prefs.getString(KEY_BROWSER_APP, DEFAULT_BROWSER_APP);
        for (BrowserApp app : BrowserApp.values()) {
            if (app.getPackageName().equals(packageName)) {
                return app;
            }
        }
        return BrowserApp.CHROME;
    }
    
    public void setBrowserApp(BrowserApp app) {
        prefs.edit().putString(KEY_BROWSER_APP, app.getPackageName()).apply();
    }
    
    public boolean isChromeFallbackEnabled() {
        return prefs.getBoolean(KEY_ENABLE_CHROME_FALLBACK, true);
    }
    
    public void setChromeFallbackEnabled(boolean enabled) {
        prefs.edit().putBoolean(KEY_ENABLE_CHROME_FALLBACK, enabled).apply();
    }
    
    public boolean isIncognitoModeEnabled() {
        return prefs.getBoolean(KEY_USE_INCOGNITO_MODE, false);
    }
    
    public void setIncognitoModeEnabled(boolean enabled) {
        prefs.edit().putBoolean(KEY_USE_INCOGNITO_MODE, enabled).apply();
    }
    
    // 🎯 Getters e Setters para Configurações de Automação
    public boolean isAutoCloseTabsEnabled() {
        return prefs.getBoolean(KEY_AUTO_CLOSE_TABS, false);
    }
    
    public void setAutoCloseTabsEnabled(boolean enabled) {
        prefs.edit().putBoolean(KEY_AUTO_CLOSE_TABS, enabled).apply();
    }
    
    public boolean isRandomDelayEnabled() {
        return prefs.getBoolean(KEY_RANDOM_DELAY_ENABLED, true);
    }
    
    public void setRandomDelayEnabled(boolean enabled) {
        prefs.edit().putBoolean(KEY_RANDOM_DELAY_ENABLED, enabled).apply();
    }
    
    public int getMinRandomDelay() {
        return prefs.getInt(KEY_MIN_RANDOM_DELAY, 3);
    }
    
    public void setMinRandomDelay(int seconds) {
        prefs.edit().putInt(KEY_MIN_RANDOM_DELAY, seconds).apply();
    }
    
    public int getMaxRandomDelay() {
        return prefs.getInt(KEY_MAX_RANDOM_DELAY, 8);
    }
    
    public void setMaxRandomDelay(int seconds) {
        prefs.edit().putInt(KEY_MAX_RANDOM_DELAY, seconds).apply();
    }
    
    // 🧠 Getters e Setters para Configurações de IA
    public AIMode getAIMode() {
        String modeId = prefs.getString(KEY_AI_MODE, DEFAULT_AI_MODE);
        for (AIMode mode : AIMode.values()) {
            if (mode.getId().equals(modeId)) {
                return mode;
            }
        }
        return AIMode.CHATGPT;
    }
    
    public void setAIMode(AIMode mode) {
        prefs.edit().putString(KEY_AI_MODE, mode.getId()).apply();
    }
    
    public boolean isContextualLearningEnabled() {
        return prefs.getBoolean(KEY_ENABLE_CONTEXTUAL_LEARNING, true);
    }
    
    public void setContextualLearningEnabled(boolean enabled) {
        prefs.edit().putBoolean(KEY_ENABLE_CONTEXTUAL_LEARNING, enabled).apply();
    }
    
    public boolean isTemporalAwarenessEnabled() {
        return prefs.getBoolean(KEY_ENABLE_TEMPORAL_AWARENESS, true);
    }
    
    public void setTemporalAwarenessEnabled(boolean enabled) {
        prefs.edit().putBoolean(KEY_ENABLE_TEMPORAL_AWARENESS, enabled).apply();
    }
    
    // 🔐 Getters e Setters para Configurações de Segurança
    public boolean isStealthModeEnabled() {
        return prefs.getBoolean(KEY_STEALTH_MODE, false);
    }
    
    public void setStealthModeEnabled(boolean enabled) {
        prefs.edit().putBoolean(KEY_STEALTH_MODE, enabled).apply();
    }
    
    public boolean isUserAgentRotationEnabled() {
        return prefs.getBoolean(KEY_ROTATE_USER_AGENT, false);
    }
    
    public void setUserAgentRotationEnabled(boolean enabled) {
        prefs.edit().putBoolean(KEY_ROTATE_USER_AGENT, enabled).apply();
    }
    
    // 🔧 Métodos utilitários
    public String buildSearchUrl(String query) {
        SearchEngine engine = getSearchEngine();
        String template;
        
        if (engine == SearchEngine.CUSTOM) {
            template = getCustomUrlTemplate();
        } else if (engine == SearchEngine.BING) {
            // Usar parâmetros personalizáveis para Bing
            template = String.format("https://www.bing.com/search?q=%%s&PC=%s&FORM=%s", 
                                   getUrlPcParameter(), getUrlFormParameter());
        } else {
            template = engine.getUrlTemplate();
        }
        
        try {
            return String.format(template, java.net.URLEncoder.encode(query, "UTF-8"));
        } catch (Exception e) {
            return String.format(template, query.replaceAll(" ", "+"));
        }
    }
    
    public int getActualSearchInterval() {
        int baseInterval = getSearchInterval();
        
        if (isRandomDelayEnabled()) {
            java.util.Random random = new java.util.Random();
            int min = getMinRandomDelay();
            int max = getMaxRandomDelay();
            int randomDelay = random.nextInt(max - min + 1) + min;
            return baseInterval + randomDelay;
        }
        
        return baseInterval;
    }
    
    // 🏭 Factory method para criar instância
    public static AppConfig getInstance(Context context) {
        return new AppConfig(context);
    }
    
    // 📊 Método para exportar configurações (para backup/debug)
    public String exportConfig() {
        StringBuilder config = new StringBuilder();
        config.append("=== Microsoft Rewards Bot - Configurações ===\\n");
        config.append("Intervalo entre pesquisas: ").append(getSearchInterval()).append("s\\n");
        config.append("Engine de busca: ").append(getSearchEngine().getId()).append("\\n");
        config.append("App do navegador: ").append(getBrowserApp().getDisplayName()).append("\\n");
        config.append("Modo IA: ").append(getAIMode().getDisplayName()).append("\\n");
        config.append("Delay aleatório: ").append(isRandomDelayEnabled() ? "Ativado" : "Desativado").append("\\n");
        config.append("Modo stealth: ").append(isStealthModeEnabled() ? "Ativado" : "Desativado").append("\\n");
        return config.toString();
    }
    
    // 🔄 Método para resetar para configurações padrão
    public void resetToDefaults() {
        SharedPreferences.Editor editor = prefs.edit();
        editor.clear();
        editor.apply();
    }
}