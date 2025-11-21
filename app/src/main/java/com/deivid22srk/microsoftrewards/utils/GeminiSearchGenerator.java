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
import java.util.Random;
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
        // Otimizar geração baseada no número solicitado
        int batchSize = Math.min(count, 25); // Aumentar limite para 25 por lote
        
        if (count > 25) {
            // Para números grandes, usar geração em múltiplos lotes
            Log.d(TAG, "Gerando " + count + " pesquisas em múltiplos lotes (máximo 25 por lote)");
            return generateMultipleBatches(count, apiKey, model);
        }
        
        Log.d(TAG, "Gerando " + batchSize + " pesquisas em lote único");
        
        // Criar prompt otimizado
        String prompt = createOptimizedPrompt(batchSize);
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
     * Gera múltiplos lotes para números grandes de pesquisas
     */
    private static List<SearchItem> generateMultipleBatches(int totalCount, String apiKey, GeminiModel model) throws IOException, JSONException {
        List<SearchItem> allSearchItems = new ArrayList<>();
        int batchSize = 25;
        int remainingCount = totalCount;
        int currentIndex = 1;
        
        Log.d(TAG, "Iniciando geração em lotes: " + totalCount + " pesquisas totais, " + batchSize + " por lote");
        
        while (remainingCount > 0 && allSearchItems.size() < totalCount) {
            int currentBatchSize = Math.min(remainingCount, batchSize);
            
            Log.d(TAG, "Gerando lote: " + allSearchItems.size() + "/" + totalCount + " (" + currentBatchSize + " neste lote)");
            
            try {
                // Criar prompt específico para este lote
                String prompt = createOptimizedPromptForBatch(currentBatchSize, allSearchItems.size(), totalCount);
                JSONObject requestBody = buildOptimizedGeminiRequest(prompt);
                
                // Fazer requisição
                String fullUrl = model.getApiUrl() + "?key=" + apiKey;
                Request request = new Request.Builder()
                        .url(fullUrl)
                        .post(RequestBody.create(requestBody.toString(), JSON))
                        .addHeader("Content-Type", "application/json")
                        .addHeader("User-Agent", "Microsoft-Rewards-Bot/2.0")
                        .build();
                
                try (Response response = client.newCall(request).execute()) {
                    if (!response.isSuccessful()) {
                        String errorBody = response.body() != null ? response.body().string() : "Sem corpo na resposta";
                        Log.e(TAG, "Erro na API (lote): " + response.code() + " - " + errorBody);
                        throw new IOException("Erro na API Gemini: " + response.code() + " - " + errorBody);
                    }
                    
                    String responseBody = response.body().string();
                    List<SearchItem> batchResults = parseOptimizedGeminiResponse(responseBody);
                    
                    // Reindexar os itens para continuar a sequência
                    for (SearchItem item : batchResults) {
                        item = new SearchItem(item.getSearchText(), currentIndex++);
                        allSearchItems.add(item);
                    }
                    
                    Log.d(TAG, "Lote concluído: " + batchResults.size() + " itens adicionados. Total: " + allSearchItems.size());
                    
                    remainingCount -= batchResults.size();
                    
                    // Delay entre lotes para evitar rate limiting
                    if (remainingCount > 0 && allSearchItems.size() < totalCount) {
                        try {
                            Thread.sleep(1000); // 1 segundo entre lotes
                        } catch (InterruptedException e) {
                            Thread.currentThread().interrupt();
                        }
                    }
                    
                } catch (IOException e) {
                    Log.e(TAG, "Erro no lote " + (allSearchItems.size() / batchSize + 1) + ": " + e.getMessage());
                    if (allSearchItems.size() < 5) {
                        // Se temos muito poucos resultados, falhar
                        throw e;
                    } else {
                        // Se já temos alguns resultados, continuar com o que temos
                        Log.w(TAG, "Continuando com " + allSearchItems.size() + " pesquisas parciais");
                        break;
                    }
                }
                
            } catch (JSONException e) {
                Log.e(TAG, "Erro JSON no lote: " + e.getMessage());
                break;
            }
        }
        
        Log.d(TAG, "Geração em lotes concluída: " + allSearchItems.size() + " pesquisas de " + totalCount + " solicitadas");
        
        // Se não conseguimos gerar pelo menos 70% do solicitado, usar fallback
        if (allSearchItems.size() < (totalCount * 0.7)) {
            Log.w(TAG, "Muitas falhas na geração em lotes, usando fallback");
            return SmartSearchGenerator.generateSmartSearches(totalCount);
        }
        
        return allSearchItems;
    }
    
    /**
     * Cria um prompt otimizado específico para lotes
     */
    private static String createOptimizedPromptForBatch(int batchSize, int currentTotal, int finalTotal) {
        // Gerar contexto aleatório para variar as respostas
        String[] contexts = {
            "para pesquisa acadêmica",
            "sobre temas atuais",
            "para estudo diversificado", 
            "sobre tecnologia e inovação",
            "sobre cultura e entretenimento",
            "sobre ciência e descobertas",
            "sobre história e sociedade",
            "sobre economia e negócios",
            "sobre saúde e bem-estar",
            "sobre meio ambiente",
            "sobre arte e literatura",
            "sobre esportes e lazer"
        };
        
        String[] styles = {
            "termos de busca únicos",
            "palavras-chave interessantes", 
            "tópicos relevantes",
            "assuntos diversos",
            "temas variados",
            "conceitos importantes"
        };
        
        // Seleção aleatória considerando o lote atual
        Random random = new Random(System.currentTimeMillis() + currentTotal);
        String selectedContext = contexts[random.nextInt(contexts.length)];
        String selectedStyle = styles[random.nextInt(styles.length)];
        
        String batchInfo = "";
        if (finalTotal > batchSize) {
            batchInfo = String.format(" (Lote %d de %d)", (currentTotal / batchSize) + 1, (finalTotal + batchSize - 1) / batchSize);
        }
        
        return String.format(
            "Gere %d %s diferentes em português %s%s. " +
            "Seja muito criativo e original. Evite repetições. Cada linha deve conter apenas um termo único. " +
            "IMPORTANTE: Responda APENAS com os termos, um por linha, sem numeração, símbolos ou explicações.\n\n" +
            "Contexto: %s\n" +
            "Batch ID: %d",
            batchSize,
            selectedStyle,
            selectedContext,
            batchInfo,
            selectedContext,
            System.currentTimeMillis() + currentTotal
        );
    }
    
    /**
     * Cria um prompt ultra-otimizado para evitar MAX_TOKENS com alta diversidade
     */
    private static String createOptimizedPrompt(int count) {
        // Gerar contexto aleatório para variar as respostas
        String[] contexts = {
            "para estudo universitário",
            "para pesquisa pessoal", 
            "sobre atualidades",
            "sobre tecnologia e inovação",
            "sobre entretenimento",
            "sobre ciência e descobertas",
            "sobre cultura e arte",
            "sobre esportes e saúde",
            "sobre viagem e turismo",
            "sobre culinária e gastronomia",
            "sobre história e geografia",
            "sobre economia e negócios"
        };
        
        String[] styles = {
            "perguntas curiosas",
            "tópicos interessantes", 
            "termos de pesquisa",
            "assuntos relevantes",
            "palavras-chave úteis",
            "questões importantes"
        };
        
        // Seleção aleatória para variar as gerações
        Random random = new Random(System.currentTimeMillis());
        String selectedContext = contexts[random.nextInt(contexts.length)];
        String selectedStyle = styles[random.nextInt(styles.length)];
        
        return String.format(
            "Gere %d %s diferentes em português %s. " +
            "Seja muito criativo e varie os temas amplamente. Cada linha deve ter apenas um termo único. " +
            "Evite repetições e seja completamente original. " +
            "IMPORTANTE: Responda APENAS com os termos, um por linha, sem numeração, símbolos ou explicações.\n\n" +
            "Contexto atual: %s\n" +
            "Timestamp: %d",
            count,
            selectedStyle,
            selectedContext,
            selectedContext,
            System.currentTimeMillis()
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
        
        // Configuração otimizada para geração de termos de pesquisa
        JSONObject generationConfig = new JSONObject();
        generationConfig.put("temperature", 1.1); // Criatividade alta mas controlada
        generationConfig.put("maxOutputTokens", 4096); // Limite aumentado para até 25 termos
        generationConfig.put("candidateCount", 1);
        generationConfig.put("topP", 0.9); // Permitir mais diversidade
        generationConfig.put("topK", 50); // Aumentar diversidade de tokens
        
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
            String errorMsg = error.has("message") ? error.getString("message") : "Erro desconhecido";
            Log.e(TAG, "Erro da API Gemini: " + errorMsg);
            throw new RuntimeException("Erro da API Gemini: " + errorMsg);
        }
        
        if (!response.has("candidates")) {
            Log.w(TAG, "Resposta sem candidates - usando fallback");
            return SmartSearchGenerator.generateSmartSearches(10);
        }
        
        JSONArray candidates = response.getJSONArray("candidates");
        if (candidates.length() == 0) {
            Log.w(TAG, "Array de candidates vazio - usando fallback");
            return SmartSearchGenerator.generateSmartSearches(10);
        }
        
        JSONObject candidate = candidates.getJSONObject(0);
        
        // Verificar se a resposta foi bloqueada por segurança
        if (candidate.has("finishReason")) {
            String finishReason = candidate.getString("finishReason");
            Log.d(TAG, "Finish reason: " + finishReason);
            
            if ("SAFETY".equals(finishReason) || "BLOCKED".equals(finishReason)) {
                Log.w(TAG, "Resposta bloqueada por segurança - usando fallback");
                return SmartSearchGenerator.generateSmartSearches(10);
            }
            
            if ("MAX_TOKENS".equals(finishReason)) {
                Log.w(TAG, "Resposta truncada - usando fallback");
                return SmartSearchGenerator.generateSmartSearches(10);
            }
        }
        
        // Extrair conteúdo
        if (!candidate.has("content")) {
            Log.w(TAG, "Candidate sem content - usando fallback");
            return SmartSearchGenerator.generateSmartSearches(10);
        }
        
        JSONObject content = candidate.getJSONObject("content");
        
        if (!content.has("parts")) {
            Log.w(TAG, "Content sem parts - usando fallback");
            return SmartSearchGenerator.generateSmartSearches(10);
        }
        
        JSONArray parts = content.getJSONArray("parts");
        if (parts.length() == 0) {
            Log.w(TAG, "Parts array vazio - usando fallback");
            return SmartSearchGenerator.generateSmartSearches(10);
        }
        
        JSONObject part = parts.getJSONObject(0);
        
        if (!part.has("text")) {
            Log.w(TAG, "Part sem text - usando fallback");
            return SmartSearchGenerator.generateSmartSearches(10);
        }
        
        String generatedText = part.getString("text").trim();
        Log.d(TAG, "Texto gerado bruto: " + generatedText);
        
        if (generatedText.isEmpty()) {
            Log.w(TAG, "Texto gerado vazio - usando fallback");
            return SmartSearchGenerator.generateSmartSearches(10);
        }
        
        // Processar o texto linha por linha
        String[] lines = generatedText.split("\n");
        int index = 1;
        
        for (String line : lines) {
            String cleanLine = line.trim();
            
            // Filtrar linhas válidas
            if (isValidSearchTerm(cleanLine)) {
                searchItems.add(new SearchItem(cleanLine, index++));
                Log.d(TAG, "Termo adicionado: " + cleanLine);
            } else {
                Log.d(TAG, "Termo rejeitado: " + cleanLine);
            }
        }
        
        // Verificar se conseguimos extrair termos suficientes
        if (searchItems.size() < 3) {
            Log.w(TAG, "Poucos termos extraídos (" + searchItems.size() + ") - usando fallback");
            return SmartSearchGenerator.generateSmartSearches(10);
        }
        
        Log.d(TAG, "Sucesso: " + searchItems.size() + " termos de pesquisa extraídos");
        return searchItems;
    }
    
    /**
     * Valida se uma linha é um termo de pesquisa válido
     */
    private static boolean isValidSearchTerm(String term) {
        if (term == null || term.isEmpty()) {
            return false;
        }
        
        // Remover caracteres especiais comuns
        term = term.replaceAll("^[-*•\\d+\\.\\s]+", "").trim();
        
        // Verificações de validade
        return term.length() >= 2 && 
               term.length() <= 100 &&
               !term.contains(":") &&
               !term.toLowerCase().contains("gere") &&
               !term.toLowerCase().contains("liste") &&
               !term.toLowerCase().contains("termos") &&
               !term.toLowerCase().contains("pesquisa") &&
               !term.toLowerCase().matches(".*\\b(um|uma|cada|linha|separada|inclua|responda|apenas)\\b.*") &&
               !term.matches("^\\d+$") && // Evitar apenas números
               term.matches(".*[a-zA-ZÀ-ÿ]+.*"); // Deve ter pelo menos uma letra
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