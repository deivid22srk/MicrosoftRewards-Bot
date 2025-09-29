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
 */
public class GeminiSearchGenerator {
    
    private static final String TAG = "GeminiSearchGenerator";
    private static final String GEMINI_API_URL = "https://generativelanguage.googleapis.com/v1/models/gemini-pro:generateContent";
    private static final MediaType JSON = MediaType.get("application/json; charset=utf-8");
    
    // Cliente HTTP configurado para requisições à API
    private static final OkHttpClient client = new OkHttpClient.Builder()
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(20, TimeUnit.SECONDS)
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
        new GenerateSearchTask(count, context, apiKey, listener).execute();
    }
    
    /**
     * Task assíncrona para geração de pesquisas
     */
    private static class GenerateSearchTask extends AsyncTask<Void, Void, List<SearchItem>> {
        
        private final int count;
        private final Context context;
        private final String apiKey;
        private final OnSearchGeneratedListener listener;
        private String errorMessage;
        
        public GenerateSearchTask(int count, Context context, String apiKey, OnSearchGeneratedListener listener) {
            this.count = count;
            this.context = context;
            this.apiKey = apiKey;
            this.listener = listener;
        }
        
        @Override
        protected List<SearchItem> doInBackground(Void... voids) {
            try {
                return generateSearchesSynchronously(count, apiKey);
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
     * Gera pesquisas de forma síncrona (para uso em AsyncTask)
     */
    private static List<SearchItem> generateSearchesSynchronously(int count, String apiKey) throws IOException, JSONException {
        
        // Criar prompt inteligente para o Gemini
        String prompt = createSmartPrompt(count);
        Log.d(TAG, "Prompt criado para Gemini: " + prompt.substring(0, Math.min(100, prompt.length())) + "...");
        
        // Construir request JSON
        JSONObject requestBody = buildGeminiRequest(prompt);
        Log.d(TAG, "Request JSON criado: " + requestBody.toString().substring(0, Math.min(200, requestBody.toString().length())) + "...");
        
        // Fazer requisição HTTP
        String fullUrl = GEMINI_API_URL + "?key=" + apiKey;
        Log.d(TAG, "Fazendo requisição para: " + GEMINI_API_URL + "?key=" + apiKey.substring(0, Math.min(10, apiKey.length())) + "...");
        
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
                throw new IOException("Erro na API Gemini: " + response.code() + " " + response.message() + "\nDetalhes: " + errorBody);
            }
            
            String responseBody = response.body().string();
            Log.d(TAG, "Corpo da resposta: " + responseBody.substring(0, Math.min(300, responseBody.length())) + "...");
            return parseGeminiResponse(responseBody);
        }
    }
    
    /**
     * Cria um prompt inteligente e detalhado para o Gemini
     */
    private static String createSmartPrompt(int count) {
        return String.format(
            "Gere exatamente %d termos de pesquisa únicos em português brasileiro para Microsoft Rewards. " +
            "Cada termo deve ter 2-5 palavras e cobrir tópicos diversos.\n\n" +
            
            "REQUISITOS:\n" +
            "- Todos os termos devem ser diferentes e únicos\n" +
            "- Use tópicos atuais de 2024-2025\n" +
            "- Inclua variedade: tecnologia, entretenimento, saúde, educação, cultura, esportes\n" +
            "- Linguagem natural que pessoas realmente pesquisam\n" +
            "- Sem repetição ou termos muito similares\n\n" +
            
            "TÓPICOS PARA INCLUIR:\n" +
            "- Inteligência artificial e tecnologia\n" +
            "- Filmes, séries e entretenimento\n" +
            "- Jogos e esports\n" +
            "- Saúde e bem-estar\n" +
            "- Educação e carreira\n" +
            "- Finanças e investimentos\n" +
            "- Culinária e receitas\n" +
            "- Viagens e turismo\n" +
            "- Esportes e times\n" +
            "- Notícias e eventos atuais\n\n" +
            
            "FORMATO DE RESPOSTA:\n" +
            "Retorne APENAS os termos de pesquisa, um por linha, sem numeração, sem explicações adicionais.\n\n" +
            
            "Exemplo:\n" +
            "inteligência artificial 2024\n" +
            "receitas saudáveis\n" +
            "melhores filmes netflix\n\n" +
            
            "Gere %d termos únicos agora:",
            count, count
        );
    }
    
    /**
     * Constrói o JSON de requisição para a API do Gemini
     */
    private static JSONObject buildGeminiRequest(String prompt) throws JSONException {
        JSONObject request = new JSONObject();
        
        // Configurar conteúdo no formato correto
        JSONArray contents = new JSONArray();
        JSONObject content = new JSONObject();
        JSONArray parts = new JSONArray();
        JSONObject part = new JSONObject();
        
        part.put("text", prompt);
        parts.put(part);
        content.put("parts", parts);
        contents.put(content);
        
        request.put("contents", contents);
        
        // Configurar parâmetros de geração (simplificados para compatibilidade)
        JSONObject generationConfig = new JSONObject();
        generationConfig.put("temperature", 0.7);
        generationConfig.put("topK", 40);
        generationConfig.put("topP", 0.95);
        generationConfig.put("maxOutputTokens", 1024);
        
        request.put("generationConfig", generationConfig);
        
        // Configurar filtros de segurança (básicos)
        JSONArray safetySettings = new JSONArray();
        String[] categories = {
            "HARM_CATEGORY_HARASSMENT",
            "HARM_CATEGORY_HATE_SPEECH", 
            "HARM_CATEGORY_SEXUALLY_EXPLICIT",
            "HARM_CATEGORY_DANGEROUS_CONTENT"
        };
        
        for (String category : categories) {
            JSONObject safety = new JSONObject();
            safety.put("category", category);
            safety.put("threshold", "BLOCK_ONLY_HIGH");
            safetySettings.put(safety);
        }
        
        request.put("safetySettings", safetySettings);
        
        return request;
    }
    
    /**
     * Analisa a resposta do Gemini e extrai os termos de pesquisa
     */
    private static List<SearchItem> parseGeminiResponse(String responseBody) throws JSONException {
        List<SearchItem> searchItems = new ArrayList<>();
        
        JSONObject response = new JSONObject(responseBody);
        
        // Verificar se houve erro na resposta
        if (response.has("error")) {
            JSONObject error = response.getJSONObject("error");
            throw new RuntimeException("Erro da API Gemini: " + error.getString("message"));
        }
        
        // Extrair conteúdo gerado
        JSONArray candidates = response.getJSONArray("candidates");
        if (candidates.length() > 0) {
            JSONObject candidate = candidates.getJSONObject(0);
            JSONObject content = candidate.getJSONObject("content");
            JSONArray parts = content.getJSONArray("parts");
            
            if (parts.length() > 0) {
                String generatedText = parts.getJSONObject(0).getString("text");
                
                // Processar linhas do texto gerado
                String[] lines = generatedText.split("\n");
                int index = 1;
                
                for (String line : lines) {
                    String cleanLine = line.trim();
                    
                    // Pular linhas vazias ou com caracteres especiais
                    if (!cleanLine.isEmpty() && 
                        !cleanLine.startsWith("#") && 
                        !cleanLine.startsWith("-") &&
                        !cleanLine.startsWith("•") &&
                        !cleanLine.matches("\\d+\\..*")) {
                        
                        // Remover numeração se presente
                        cleanLine = cleanLine.replaceAll("^\\d+\\s*[\\.\\-]\\s*", "");
                        
                        if (!cleanLine.isEmpty() && cleanLine.length() > 2) {
                            searchItems.add(new SearchItem(cleanLine, index++));
                        }
                    }
                }
            }
        }
        
        // Se não conseguiu extrair pesquisas suficientes, gerar algumas de fallback
        if (searchItems.size() < 5) {
            Log.w(TAG, "Poucas pesquisas extraídas do Gemini, usando fallback");
            return SmartSearchGenerator.generateSmartSearches(Math.max(10, searchItems.size()));
        }
        
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
        
        // Aceitar diferentes formatos de API Key do Google
        return (trimmedKey.startsWith("AIza") || 
                trimmedKey.startsWith("AIzaSy") ||
                trimmedKey.startsWith("AI") ||
                trimmedKey.matches("^[A-Za-z0-9_-]+$")) && 
               trimmedKey.length() >= 20 && 
               trimmedKey.length() <= 100;
    }
    
    /**
     * Testa a conectividade com a API do Gemini
     */
    public static void testGeminiConnection(String apiKey, OnSearchGeneratedListener listener) {
        // Primeiro tenta listar modelos disponíveis
        testApiConnection(apiKey, new ApiTestListener() {
            @Override
            public void onApiWorking() {
                // Se API está funcionando, tenta gerar pesquisas de teste
                generateSearchesWithGemini(3, null, apiKey, new OnSearchGeneratedListener() {
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
     * Testa se a API está funcionando fazendo uma requisição simples
     */
    private static void testApiConnection(String apiKey, ApiTestListener listener) {
        new Thread(() -> {
            try {
                // Fazer uma requisição simples para testar conectividade
                String testUrl = "https://generativelanguage.googleapis.com/v1/models?key=" + apiKey;
                
                Request request = new Request.Builder()
                        .url(testUrl)
                        .get()
                        .addHeader("User-Agent", "Microsoft-Rewards-Bot/2.0")
                        .build();
                
                try (Response response = client.newCall(request).execute()) {
                    if (response.isSuccessful()) {
                        Log.d(TAG, "Teste de API bem-sucedido");
                        listener.onApiWorking();
                    } else {
                        String errorBody = response.body() != null ? response.body().string() : "Sem detalhes";
                        Log.e(TAG, "Teste de API falhou: " + response.code() + " - " + errorBody);
                        listener.onApiError("API retornou erro " + response.code() + ": " + errorBody);
                    }
                }
                
            } catch (Exception e) {
                Log.e(TAG, "Erro no teste de conectividade", e);
                listener.onApiError("Erro de conectividade: " + e.getMessage());
            }
        }).start();
    }
}