package com.deivid22srk.microsoftrewards.utils;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.deivid22srk.microsoftrewards.model.SearchItem;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * 🤖 Integração com Gemini AI para geração de pesquisas inteligentes
 * Gera pesquisas diversificadas e contextualmente relevantes usando IA generativa
 * 
 * MODELOS SUPORTADOS (2025):
 * - gemini-2.5-flash: Melhor custo-benefício (padrão)
 * - gemini-2.5-pro: Mais avançado para tarefas complexas
 * - gemini-2.5-flash-lite: Mais rápido e econômico
 */
public class GeminiSearchGenerator {
    
    private static final String TAG = "GeminiSearchGenerator";
    private static final String BASE_API_URL = "https://generativelanguage.googleapis.com/v1/models";
    private static final MediaType JSON = MediaType.get("application/json; charset=utf-8");
    
    // Modelos disponíveis em 2025
    public enum GeminiModel {
        FLASH_2_5("gemini-2.5-flash", "Gemini 2.5 Flash (Recomendado)"),
        PRO_2_5("gemini-2.5-pro", "Gemini 2.5 Pro (Mais Avançado)"),
        FLASH_LITE_2_5("gemini-2.5-flash-lite", "Gemini 2.5 Flash-Lite (Mais Rápido)");
        
        private final String modelId;
        private final String displayName;
        
        GeminiModel(String modelId, String displayName) {
            this.modelId = modelId;
            this.displayName = displayName;
        }
        
        public String getModelId() { return modelId; }
        public String getDisplayName() { return displayName; }
        public String getApiUrl() { return BASE_API_URL + "/" + modelId + ":generateContent"; }
    }
    
    // Modelo padrão
    private static final GeminiModel DEFAULT_MODEL = GeminiModel.FLASH_2_5;
    
    // Cliente HTTP configurado para requisições à API
    private static final OkHttpClient client = new OkHttpClient.Builder()
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(25, TimeUnit.SECONDS)
            .writeTimeout(15, TimeUnit.SECONDS)
            .build();
    
    /**
     * Interface para callback de resultado da geração
     */
    public interface OnSearchGeneratedListener {
        void onSuccess(List<SearchItem> searches);
        void onError(String errorMessage);
    }
    
    /**
     * Gera pesquisas usando Gemini AI de forma assíncrona
     * @param count Número de pesquisas a gerar
     * @param context Contexto da aplicação
     * @param apiKey API Key do Gemini
     * @param listener Callback para resultado
     */
    public static void generateSearchesWithGemini(int count, Context context, String apiKey, OnSearchGeneratedListener listener) {
        generateSearchesWithGemini(count, context, apiKey, DEFAULT_MODEL, listener);
    }
    
    /**
     * Gera pesquisas usando Gemini AI com modelo específico
     */
    public static void generateSearchesWithGemini(int count, Context context, String apiKey, GeminiModel model, OnSearchGeneratedListener listener) {
        new GenerateSearchTask(count, context, apiKey, model, listener).execute();
    }
    
    /**
     * Task assíncrona para geração de pesquisas
     */
    private static class GenerateSearchTask extends AsyncTask<Void, Void, List<SearchItem>> {
        
        private final int count;
        private final Context context;
        private final String apiKey;
        private final GeminiModel model;
        private final OnSearchGeneratedListener listener;
        private String errorMessage;
        
        public GenerateSearchTask(int count, Context context, String apiKey, GeminiModel model, OnSearchGeneratedListener listener) {
            this.count = count;
            this.context = context;
            this.apiKey = apiKey;
            this.model = model;
            this.listener = listener;
        }
        
        @Override
        protected List<SearchItem> doInBackground(Void... voids) {
            try {
                return generateSingleBatch(count, apiKey, model);
            } catch (Exception e) {
                Log.e(TAG, "Erro ao gerar pesquisas com Gemini", e);
                errorMessage = "Erro na comunicação com Gemini AI: " + e.getMessage();
                return null;
            }
        }
        
        @Override
        protected void onPostExecute(List<SearchItem> result) {
            if (result != null && !result.isEmpty()) {
                listener.onSuccess(result);
            } else {
                String error = errorMessage != null ? errorMessage : "Falha na geração de pesquisas";
                listener.onError(error);
            }
        }
    }
    
    /**
     * Gera um lote único de pesquisas (método principal)
     */
    private static List<SearchItem> generateSingleBatch(int count, String apiKey, GeminiModel model) throws IOException, JSONException {
        // Limitar para evitar MAX_TOKENS
        if (count > 15) {
            count = 15;
            Log.w(TAG, "Limitando para 15 pesquisas para evitar MAX_TOKENS");
        }
        
        // Criar prompt otimizado
        String prompt = createOptimizedPrompt(count);
        Log.d(TAG, "Usando modelo: " + model.getDisplayName() + " (" + model.getModelId() + ")");
        Log.d(TAG, "Prompt criado: " + prompt);
        
        // Construir request JSON
        JSONObject requestBody = buildOptimizedGeminiRequest(prompt);
        
        // Fazer requisição HTTP
        String fullUrl = model.getApiUrl() + "?key=" + apiKey;
        Log.d(TAG, "Fazendo requisição para: " + model.getApiUrl());
        
        Request request = new Request.Builder()
                .url(fullUrl)
                .post(RequestBody.create(requestBody.toString(), JSON))
                .addHeader("Content-Type", "application/json")
                .addHeader("User-Agent", "Microsoft-Rewards-Bot/2.0")
                .build();
        
        try (Response response = client.newCall(request).execute()) {
            Log.d(TAG, "Resposta recebida: " + response.code() + " " + response.message());
            
            if (!response.isSuccessful()) {
                String errorBody = response.body() != null ? response.body().string() : "Sem corpo na resposta";
                Log.e(TAG, "Erro na API: " + response.code() + " - " + errorBody);
                throw new IOException("Erro na API Gemini: " + response.code() + " - " + errorBody);
            }
            
            String responseBody = response.body().string();
            return parseOptimizedGeminiResponse(responseBody);
        }
    }
    
    /**
     * Cria um prompt ultra-otimizado para evitar MAX_TOKENS
     */
    private static String createOptimizedPrompt(int count) {
        return String.format(
            "Liste %d termos de pesquisa em português, um por linha:\n" +
            "tecnologia\n" +
            "filmes\n" +
            "jogos\n\n" +
            "Agora liste %d termos únicos:",
            Math.min(3, count), count
        );
    }
    
    /**
     * Constrói request JSON otimizado
     */
    private static JSONObject buildOptimizedGeminiRequest(String prompt) throws JSONException {
        JSONObject request = new JSONObject();
        
        JSONArray contents = new JSONArray();
        JSONObject content = new JSONObject();
        JSONArray parts = new JSONArray();
        JSONObject part = new JSONObject();
        
        part.put("text", prompt);
        parts.put(part);
        content.put("parts", parts);
        contents.put(content);
        
        request.put("contents", contents);
        
        // Configuração mínima e otimizada
        JSONObject generationConfig = new JSONObject();
        generationConfig.put("temperature", 0.8);
        generationConfig.put("maxOutputTokens", 4096); // Maior limite
        generationConfig.put("candidateCount", 1);
        
        request.put("generationConfig", generationConfig);
        
        return request;
    }
    
    /**
     * Parse otimizado da resposta do Gemini
     */
    private static List<SearchItem> parseOptimizedGeminiResponse(String responseBody) throws JSONException {
        List<SearchItem> searchItems = new ArrayList<>();
        
        Log.d(TAG, "Parsing resposta: " + responseBody.substring(0, Math.min(500, responseBody.length())));
        
        JSONObject response = new JSONObject(responseBody);
        
        if (response.has("error")) {
            JSONObject error = response.getJSONObject("error");
            throw new RuntimeException("Erro da API Gemini: " + error.getString("message"));
        }
        
        JSONArray candidates = response.getJSONArray("candidates");
        if (candidates.length() > 0) {
            JSONObject candidate = candidates.getJSONObject(0);
            
            // Log para debugging
            Log.d(TAG, "Candidate: " + candidate.toString());
            
            if (candidate.has("finishReason")) {
                String finishReason = candidate.getString("finishReason");
                Log.d(TAG, "Finish reason: " + finishReason);
                
                if ("MAX_TOKENS".equals(finishReason)) {
                    Log.w(TAG, "Resposta truncada - usando fallback");
                    return SmartSearchGenerator.generateSmartSearches(10);
                }
            }
            
            if (candidate.has("content")) {
                JSONObject content = candidate.getJSONObject("content");
                Log.d(TAG, "Content: " + content.toString());
                
                if (content.has("parts")) {
                    JSONArray parts = content.getJSONArray("parts");
                    
                    if (parts.length() > 0) {
                        JSONObject part = parts.getJSONObject(0);
                        
                        if (part.has("text")) {
                            String generatedText = part.getString("text");
                            Log.d(TAG, "Texto gerado: " + generatedText);
                            
                            // Processar o texto
                            String[] lines = generatedText.split("\n");
                            int index = 1;
                            
                            for (String line : lines) {
                                String cleanLine = line.trim();
                                
                                if (!cleanLine.isEmpty() && 
                                    cleanLine.length() > 2 && 
                                    cleanLine.length() < 50 &&
                                    !cleanLine.contains(":") &&
                                    !cleanLine.matches("\\d+\\..*") &&
                                    !cleanLine.startsWith("-") &&
                                    !cleanLine.startsWith("*")) {
                                    
                                    searchItems.add(new SearchItem(cleanLine, index++));
                                    Log.d(TAG, "Adicionado: " + cleanLine);
                                }
                            }
                        } else {
                            Log.w(TAG, "Part não tem texto");
                        }
                    } else {
                        Log.w(TAG, "Parts array vazio");
                    }
                } else {
                    Log.w(TAG, "Content não tem parts - usando fallback");
                    return SmartSearchGenerator.generateSmartSearches(10);
                }
            } else {
                Log.w(TAG, "Candidate não tem content");
            }
        }
        
        if (searchItems.size() < 3) {
            Log.w(TAG, "Poucas pesquisas extraídas (" + searchItems.size() + "), usando fallback");
            return SmartSearchGenerator.generateSmartSearches(10);
        }
        
        Log.d(TAG, "Total extraído: " + searchItems.size() + " pesquisas");
        return searchItems;
    }
    
    /**
     * Valida se uma API Key do Gemini parece válida
     */
    public static boolean isValidGeminiApiKey(String apiKey) {
        if (apiKey == null || apiKey.trim().isEmpty()) {
            return false;
        }
        
        String trimmedKey = apiKey.trim();
        
        return (trimmedKey.startsWith("AIza") || 
                trimmedKey.startsWith("AIzaSy") ||
                trimmedKey.startsWith("AI")) && 
               trimmedKey.length() >= 20 && 
               trimmedKey.length() <= 100;
    }
    
    /**
     * Testa a conectividade com a API do Gemini
     */
    public static void testGeminiConnection(String apiKey, OnSearchGeneratedListener listener) {
        testApiConnection(apiKey, new ApiTestListener() {
            @Override
            public void onApiWorking() {
                generateSearchesWithGemini(3, null, apiKey, DEFAULT_MODEL, new OnSearchGeneratedListener() {
                    @Override
                    public void onSuccess(List<SearchItem> searches) {
                        listener.onSuccess(searches);
                    }
                    
                    @Override
                    public void onError(String errorMessage) {
                        listener.onError("Teste de geração falhou: " + errorMessage);
                    }
                });
            }
            
            @Override
            public void onApiError(String error) {
                listener.onError("Teste de conexão falhou: " + error);
            }
        });
    }
    
    /**
     * Interface para callback de teste da API
     */
    private interface ApiTestListener {
        void onApiWorking();
        void onApiError(String error);
    }
    
    /**
     * Testa se a API está funcionando
     */
    private static void testApiConnection(String apiKey, ApiTestListener listener) {
        new Thread(() -> {
            try {
                String testUrl = BASE_API_URL + "?key=" + apiKey;
                
                Request request = new Request.Builder()
                        .url(testUrl)
                        .get()
                        .addHeader("User-Agent", "Microsoft-Rewards-Bot/2.0")
                        .build();
                
                try (Response response = client.newCall(request).execute()) {
                    if (response.isSuccessful()) {
                        String responseBody = response.body().string();
                        Log.d(TAG, "Teste de API bem-sucedido");
                        
                        if (responseBody.contains("gemini-2.5-flash") || responseBody.contains("gemini-2")) {
                            listener.onApiWorking();
                        } else {
                            listener.onApiError("Modelos Gemini 2.5 não encontrados");
                        }
                    } else {
                        String errorBody = response.body() != null ? response.body().string() : "Sem detalhes";
                        listener.onApiError("API erro " + response.code() + ": " + errorBody);
                    }
                }
                
            } catch (Exception e) {
                listener.onApiError("Erro de conectividade: " + e.getMessage());
            }
        }).start();
    }
}