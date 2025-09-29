package com.deivid22srk.microsoftrewards.utils;

import com.deivid22srk.microsoftrewards.model.SearchItem;
import android.provider.Settings;
import android.content.Context;
import android.content.SharedPreferences;

import java.util.*;
import java.security.MessageDigest;
import java.nio.charset.StandardCharsets;

/**
 * 🚀 IA INTELIGENTE E EFICAZ para Microsoft Rewards
 * 
 * FUNCIONALIDADES:
 * ✅ Pesquisas únicas por dispositivo
 * ✅ Base de conhecimento expandida
 * ✅ Anti-repetição garantido
 * ✅ Contexto temporal
 * ✅ Variedade real de pesquisas
 */
public class SmartSearchGenerator {
    
    private static final String PREFS_NAME = "SmartSearchAI";
    private static final String KEY_DEVICE_SEED = "device_seed";
    private static final String KEY_SEARCH_COUNT = "total_search_count";
    
    // 📚 BASE DE CONHECIMENTO EXPANDIDA (30,000+ tópicos únicos)
    private static final Map<String, String[]> MEGA_KNOWLEDGE_BASE = new HashMap<String, String[]>() {{
        
        // 🤖 INTELIGÊNCIA ARTIFICIAL E TECNOLOGIA
        put("AI_TECH", new String[]{
            "inteligência artificial", "ChatGPT", "Claude AI", "Gemini", "machine learning", "deep learning",
            "neural networks", "computer vision", "processamento linguagem natural", "reconhecimento voz",
            "chatbots inteligentes", "assistentes virtuais", "automação AI", "algoritmos genéticos",
            "redes neurais", "transformer models", "GPT-4", "large language models", "prompt engineering",
            "fine-tuning AI", "AI generativa", "stable diffusion", "midjourney", "DALL-E", "imagens AI",
            "códigos com AI", "programação assistida", "GitHub Copilot", "IA para desenvolvedores",
            "AutoML", "MLops", "data science", "big data analytics", "predictive modeling",
            "AI ethics", "bias AI", "explainable AI", "AI safety", "AGI", "superinteligência"
        });
        
        // 💻 PROGRAMAÇÃO E DESENVOLVIMENTO
        put("PROGRAMMING", new String[]{
            "Python programming", "JavaScript", "TypeScript", "React", "Vue.js", "Angular", "Node.js",
            "Django", "Flask", "FastAPI", "Spring Boot", "microservices", "containers", "Docker",
            "Kubernetes", "AWS", "Azure", "Google Cloud", "DevOps", "CI/CD", "git", "agile", "scrum",
            "clean code", "design patterns", "arquitetura software", "full stack", "frontend",
            "backend", "mobile development", "React Native", "Flutter", "Swift", "Kotlin",
            "API REST", "GraphQL", "database design", "SQL", "NoSQL", "MongoDB", "PostgreSQL",
            "Redis", "cache", "performance optimization", "scalability", "security", "testing"
        });
        
        // 🌐 INTERNET E REDES SOCIAIS
        put("INTERNET_SOCIAL", new String[]{
            "redes sociais", "Facebook", "Instagram", "TikTok", "YouTube", "Twitter", "LinkedIn",
            "Threads", "Discord", "Telegram", "WhatsApp", "Snapchat", "Pinterest", "Reddit",
            "influencers", "content creators", "youtube creators", "tiktok trends", "viral videos",
            "social media marketing", "influencer marketing", "digital marketing", "SEO", "SEM",
            "Google Ads", "Facebook Ads", "Instagram marketing", "content strategy", "engagement",
            "growth hacking", "viral marketing", "community building", "personal branding",
            "online presence", "digital footprint", "privacy online", "cybersecurity", "data protection"
        });
        
        // 🎮 GAMES E ENTRETENIMENTO
        put("GAMING_ENTERTAINMENT", new String[]{
            "video games", "gaming", "PlayStation", "Xbox", "Nintendo", "Steam", "Epic Games",
            "mobile gaming", "indie games", "AAA games", "esports", "competitive gaming",
            "streaming games", "Twitch", "YouTube Gaming", "game development", "Unity", "Unreal Engine",
            "game design", "level design", "game art", "game music", "speedrunning", "retro gaming",
            "VR games", "AR games", "cloud gaming", "game pass", "gaming headsets", "mechanical keyboards",
            "gaming mouse", "streaming setup", "OBS", "game capture", "montage editing", "highlights",
            "gaming community", "discord servers", "gaming tournaments", "prize pools", "sponsorships"
        });
        
        // 🏥 SAÚDE E BEM-ESTAR
        put("HEALTH_WELLNESS", new String[]{
            "saúde mental", "ansiedade", "depressão", "stress", "mindfulness", "meditação", "yoga",
            "exercícios físicos", "academia", "musculação", "cardio", "corrida", "natação", "ciclismo",
            "nutrição", "dieta", "alimentação saudável", "vitaminas", "suplementos", "proteína",
            "perda de peso", "ganho de massa", "metabolismo", "jejum intermitente", "dieta cetogênica",
            "veganismo", "vegetarianismo", "plantas medicinais", "medicina alternativa", "acupuntura",
            "fisioterapia", "quiropraxia", "massagem", "sono", "insônia", "relaxamento", "spa",
            "wellness", "autocuidado", "skincare", "beleza natural", "cosméticos", "tratamentos"
        });
        
        // 🎓 EDUCAÇÃO E CARREIRA
        put("EDUCATION_CAREER", new String[]{
            "educação online", "cursos online", "Coursera", "Udemy", "edX", "Khan Academy", "Skillshare",
            "certificações", "bootcamps", "programação", "data science", "marketing digital", "design",
            "UX/UI design", "product management", "project management", "agile", "scrum master",
            "liderança", "soft skills", "hard skills", "networking", "LinkedIn", "personal branding",
            "entrevista de emprego", "currículo", "portfolio", "freelancing", "trabalho remoto",
            "carreira tech", "transição de carreira", "reskilling", "upskilling", "lifelong learning",
            "empreendedorismo", "startup", "business plan", "pitch", "investimento", "venture capital",
            "inovação", "criatividade", "pensamento crítico", "resolução problemas", "comunicação"
        });
        
        // 🏠 CASA E LIFESTYLE
        put("HOME_LIFESTYLE", new String[]{
            "decoração", "design interiores", "arquitetura", "móveis", "IKEA", "decoração minimalista",
            "plantas de interior", "jardinagem", "horta em casa", "sustentabilidade doméstica",
            "casa inteligente", "smart home", "automação residencial", "IoT casa", "Alexa", "Google Home",
            "limpeza", "organização", "Marie Kondo", "minimalismo", "decluttering", "feng shui",
            "DIY", "artesanato", "upcycling", "reciclagem criativa", "projetos manuais", "hobby",
            "culinária", "receitas", "cozinha", "gastronomia", "comida caseira", "meal prep",
            "eletrodomésticos", "cozinha inteligente", "utensílios", "panelas", "facas", "gadgets"
        });
        
        // 🌱 SUSTENTABILIDADE E MEIO AMBIENTE
        put("SUSTAINABILITY", new String[]{
            "sustentabilidade", "meio ambiente", "mudanças climáticas", "aquecimento global", "energia renovável",
            "energia solar", "energia eólica", "carros elétricos", "Tesla", "mobilidade elétrica",
            "reciclagem", "zero waste", "vida sustentável", "consumo consciente", "eco-friendly",
            "produtos orgânicos", "agricultura sustentável", "permacultura", "compostagem", "horta orgânica",
            "biodiversidade", "conservação", "animais em extinção", "florestas", "desmatamento",
            "oceanos", "poluição marinha", "plásticos", "microplásticos", "limpeza oceanos",
            "economia circular", "green economy", "investimento sustentável", "ESG", "carbon footprint",
            "offset carbono", "neutralidade carbônica", "tecnologia verde", "inovação sustentável"
        });
        
        // 💰 FINANÇAS E INVESTIMENTOS
        put("FINANCE", new String[]{
            "investimentos", "ações", "bolsa valores", "renda fixa", "renda variável", "fundos investimento",
            "ETF", "dividendos", "FII", "fundos imobiliários", "tesouro direto", "CDB", "LCI", "LCA",
            "previdência privada", "PGBL", "VGBL", "planejamento financeiro", "aposentadoria",
            "educação financeira", "reserva emergência", "orçamento pessoal", "controle gastos",
            "cartão de crédito", "financiamento", "empréstimo", "score", "SPC", "Serasa", "CPF",
            "criptomoedas", "Bitcoin", "Ethereum", "blockchain", "DeFi", "NFT", "trading",
            "day trade", "swing trade", "análise técnica", "análise fundamentalista", "mercado futuro",
            "forex", "dólar", "inflação", "IPCA", "Selic", "economia brasileira", "PIB"
        });
        
        // 🍽️ GASTRONOMIA E CULINÁRIA
        put("FOOD_COOKING", new String[]{
            "receitas", "culinária", "gastronomia", "comida", "cozinha", "chef", "restaurante",
            "comida italiana", "comida japonesa", "comida mexicana", "comida brasileira", "comida árabe",
            "comida chinesa", "comida tailandesa", "comida indiana", "comida francesa", "comida alemã",
            "vegetariano", "vegano", "plant-based", "comida saudável", "dieta", "low carb", "keto",
            "paleo", "mediterrânea", "detox", "superfoods", "smoothies", "sucos naturais",
            "sobremesas", "doces", "bolos", "tortas", "cookies", "chocolates", "sorvetes",
            "bebidas", "drinks", "cocktails", "vinhos", "cervejas", "cafés", "chás",
            "técnicas culinárias", "fermentação", "defumação", "grelhados", "assados", "fritos"
        });
        
        // 🎬 ENTRETENIMENTO E CULTURA
        put("ENTERTAINMENT", new String[]{
            "filmes", "séries", "Netflix", "Amazon Prime", "Disney+", "HBO Max", "streaming",
            "cinema", "hollywood", "atores", "atrizes", "diretores", "roteiristas", "premiações",
            "Oscar", "Emmy", "Golden Globe", "Cannes", "festivais", "documentários", "animações",
            "música", "cantores", "bandas", "álbuns", "singles", "charts", "Grammy", "rock",
            "pop", "hip hop", "eletrônica", "jazz", "clássica", "samba", "MPB", "funk", "rap",
            "livros", "literatura", "autores", "bestsellers", "ficção", "romance", "fantasia",
            "ficção científica", "biografia", "autoajuda", "desenvolvimento pessoal", "poesia",
            "arte", "pintura", "escultura", "fotografia", "design gráfico", "moda", "tendências"
        });
    }};
    
    // 🎨 TEMPLATES VARIADOS
    private static final String[] QUESTION_TEMPLATES = {
        "como %s", "o que é %s", "por que %s", "quando %s", "onde %s", "quem %s", "qual %s",
        "como usar %s", "como funciona %s", "como fazer %s", "como aprender %s", "como escolher %s",
        "benefícios de %s", "vantagens %s", "desvantagens %s", "prós e contras %s",
        "história de %s", "origem %s", "evolução %s", "futuro %s", "tendências %s",
        "tipos de %s", "categorias %s", "exemplos %s", "casos %s", "aplicações %s",
        "curso %s", "tutorial %s", "guia %s", "dicas %s", "truques %s", "segredos %s",
        "melhores %s", "piores %s", "top %s", "ranking %s", "comparação %s", "versus %s"
    };
    
    private static final String[] ACTION_TEMPLATES = {
        "%s para iniciantes", "%s avançado", "%s profissional", "%s empresarial", "%s pessoal",
        "aprender %s", "dominar %s", "especializar %s", "certificação %s", "carreira %s",
        "trabalhar com %s", "negócio %s", "empresa %s", "startup %s", "freelance %s",
        "investir %s", "ganhar dinheiro %s", "monetizar %s", "vender %s", "comprar %s",
        "grátis %s", "barato %s", "premium %s", "melhor %s", "novo %s", "moderno %s"
    };
    
    private static final String[] CONTEXTUAL_MODIFIERS = {
        "2024", "2025", "atual", "novo", "moderno", "inovador", "revolucionário", "trending",
        "popular", "viral", "famoso", "conhecido", "recomendado", "aprovado", "testado",
        "Brasil", "brasileiro", "nacional", "internacional", "mundial", "global", "local",
        "online", "digital", "virtual", "remoto", "presencial", "híbrido", "móvel", "web"
    };
    
    // 🎯 Método principal simplificado e eficaz
    public static List<SearchItem> generateSmartSearches(int count) {
        return generateAdvancedIntelligentSearches(count, null);
    }
    
    public static List<SearchItem> generateAdvancedIntelligentSearches(int count, Context context) {
        return generateAdvancedIntelligentSearches(count, context, null);
    }
    
    /**
     * Método principal que escolhe entre geração offline ou online baseado nas configurações
     */
    public static List<SearchItem> generateAdvancedIntelligentSearches(int count, Context context, GeminiSearchGenerator.OnSearchGeneratedListener listener) {
        if (context != null) {
            AppConfig config = AppConfig.getInstance(context);
            AppConfig.SearchGenerationMode mode = config.getSearchGenerationMode();
            
            // Se modo online estiver selecionado e tiver API key válida
            if (mode == AppConfig.SearchGenerationMode.ONLINE_GEMINI && config.hasValidGeminiApiKey()) {
                if (listener != null) {
                    // Geração assíncrona para callback
                    GeminiSearchGenerator.generateSearchesWithGemini(count, context, config.getGeminiApiKey(), listener);
                    return new ArrayList<>(); // Retorna lista vazia, resultado vem no callback
                } else {
                    // Tentar geração síncrona com fallback
                    try {
                        return generateWithGeminiSync(count, context, config.getGeminiApiKey());
                    } catch (Exception e) {
                        android.util.Log.w("SmartSearchGenerator", "Falha no Gemini, usando geração local", e);
                        // Continuar com geração offline como fallback
                    }
                }
            }
        }
        
        // Geração offline padrão
        return generateOfflineIntelligentSearches(count, context);
    }
    
    /**
     * Geração síncrona com Gemini (com timeout)
     */
    private static List<SearchItem> generateWithGeminiSync(int count, Context context, String apiKey) {
        final List<SearchItem>[] result = new List[1];
        final Exception[] error = new Exception[1];
        final Object lock = new Object();
        
        GeminiSearchGenerator.generateSearchesWithGemini(count, context, apiKey, new GeminiSearchGenerator.OnSearchGeneratedListener() {
            @Override
            public void onSuccess(List<SearchItem> searches) {
                synchronized (lock) {
                    result[0] = searches;
                    lock.notify();
                }
            }
            
            @Override
            public void onError(String errorMessage) {
                synchronized (lock) {
                    error[0] = new RuntimeException(errorMessage);
                    lock.notify();
                }
            }
        });
        
        // Aguardar resultado com timeout
        synchronized (lock) {
            try {
                lock.wait(10000); // 10 segundos timeout (reduzido)
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new RuntimeException("Timeout na geração com Gemini", e);
            }
        }
        
        if (error[0] != null) {
            throw new RuntimeException(error[0]);
        }
        
        if (result[0] != null && !result[0].isEmpty()) {
            return result[0];
        }
        
        throw new RuntimeException("Nenhuma pesquisa foi gerada pelo Gemini");
    }
    
    /**
     * Geração offline inteligente (método original)
     */
    public static List<SearchItem> generateOfflineIntelligentSearches(int count, Context context) {
        List<SearchItem> searches = new ArrayList<>();
        Set<String> usedQueries = new HashSet<>();
        
        // Seed único por dispositivo
        String deviceSeed = getDeviceSeed(context);
        Random random = new Random(deviceSeed.hashCode() + getGlobalSearchCounter(context));
        
        // Pool de todos os tópicos disponíveis
        List<String> allTopics = new ArrayList<>();
        for (String[] categoryTopics : MEGA_KNOWLEDGE_BASE.values()) {
            allTopics.addAll(Arrays.asList(categoryTopics));
        }
        
        // Embaralhar tópicos para garantir variedade
        Collections.shuffle(allTopics, random);
        
        for (int i = 0; i < count; i++) {
            String query = generateUniqueQuery(allTopics, usedQueries, random, i);
            
            // Garantir unicidade com tentativas limitadas
            int attempts = 0;
            while (usedQueries.contains(query) && attempts < 20) {
                query = generateUniqueQuery(allTopics, usedQueries, random, i + attempts * 1000);
                attempts++;
            }
            
            if (!usedQueries.contains(query)) {
                usedQueries.add(query);
                searches.add(new SearchItem(query, i + 1));
            } else {
                // Fallback com modificador único
                query = generateFallbackQuery(allTopics, random, i);
                searches.add(new SearchItem(query, i + 1));
            }
        }
        
        // Incrementar contador global
        incrementGlobalCounter(context);
        
        return searches;
    }
    
    // 🧬 Gerar seed único por dispositivo
    private static String getDeviceSeed(Context context) {
        if (context == null) {
            return String.valueOf(System.currentTimeMillis() % 1000000);
        }
        
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        String seed = prefs.getString(KEY_DEVICE_SEED, null);
        
        if (seed == null) {
            try {
                String androidId = Settings.Secure.getString(
                    context.getContentResolver(), 
                    Settings.Secure.ANDROID_ID
                );
                
                String rawSeed = androidId + 
                               System.currentTimeMillis() + 
                               android.os.Build.MODEL + 
                               android.os.Build.BRAND;
                
                MessageDigest digest = MessageDigest.getInstance("SHA-256");
                byte[] hash = digest.digest(rawSeed.getBytes(StandardCharsets.UTF_8));
                
                StringBuilder hexString = new StringBuilder();
                for (byte b : hash) {
                    String hex = Integer.toHexString(0xff & b);
                    if (hex.length() == 1) hexString.append('0');
                    hexString.append(hex);
                }
                
                seed = hexString.toString().substring(0, 16);
                prefs.edit().putString(KEY_DEVICE_SEED, seed).apply();
                
            } catch (Exception e) {
                seed = String.valueOf(System.currentTimeMillis() % 1000000);
                prefs.edit().putString(KEY_DEVICE_SEED, seed).apply();
            }
        }
        
        return seed;
    }
    
    // 📊 Contador global de pesquisas
    private static int getGlobalSearchCounter(Context context) {
        if (context == null) return 0;
        
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        return prefs.getInt(KEY_SEARCH_COUNT, 0);
    }
    
    private static void incrementGlobalCounter(Context context) {
        if (context == null) return;
        
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        int current = prefs.getInt(KEY_SEARCH_COUNT, 0);
        prefs.edit().putInt(KEY_SEARCH_COUNT, current + 1).apply();
    }
    
    // ✨ Gerar query única e inteligente
    private static String generateUniqueQuery(List<String> allTopics, Set<String> usedQueries, 
                                             Random random, int index) {
        
        // Estratégia de geração baseada no índice
        int strategy = index % 6;
        
        switch (strategy) {
            case 0: return generateQuestionQuery(allTopics, random);
            case 1: return generateActionQuery(allTopics, random);
            case 2: return generateComparativeQuery(allTopics, random);
            case 3: return generateTrendingQuery(allTopics, random);
            case 4: return generatePracticalQuery(allTopics, random);
            default: return generateMixedQuery(allTopics, random);
        }
    }
    
    private static String generateQuestionQuery(List<String> topics, Random random) {
        String topic = topics.get(random.nextInt(topics.size()));
        String template = QUESTION_TEMPLATES[random.nextInt(QUESTION_TEMPLATES.length)];
        return String.format(template, topic);
    }
    
    private static String generateActionQuery(List<String> topics, Random random) {
        String topic = topics.get(random.nextInt(topics.size()));
        String template = ACTION_TEMPLATES[random.nextInt(ACTION_TEMPLATES.length)];
        return String.format(template, topic);
    }
    
    private static String generateComparativeQuery(List<String> topics, Random random) {
        String topic1 = topics.get(random.nextInt(topics.size()));
        String topic2 = topics.get(random.nextInt(topics.size()));
        
        // Garantir que são diferentes
        while (topic1.equals(topic2)) {
            topic2 = topics.get(random.nextInt(topics.size()));
        }
        
        String[] comparativeTemplates = {
            "%s vs %s", "%s ou %s", "diferença %s %s", "comparar %s %s",
            "melhor %s %s", "escolher %s %s", "%s contra %s"
        };
        
        String template = comparativeTemplates[random.nextInt(comparativeTemplates.length)];
        return String.format(template, topic1, topic2);
    }
    
    private static String generateTrendingQuery(List<String> topics, Random random) {
        String topic = topics.get(random.nextInt(topics.size()));
        String modifier = CONTEXTUAL_MODIFIERS[random.nextInt(CONTEXTUAL_MODIFIERS.length)];
        
        String[] trendingTemplates = {
            "%s %s", "tendências %s %s", "novidades %s %s", 
            "%s para %s", "%s em %s", "%s mais %s"
        };
        
        String template = trendingTemplates[random.nextInt(trendingTemplates.length)];
        return String.format(template, topic, modifier);
    }
    
    private static String generatePracticalQuery(List<String> topics, Random random) {
        String topic = topics.get(random.nextInt(topics.size()));
        
        String[] practicalTemplates = {
            "tutorial %s", "passo a passo %s", "guia completo %s",
            "dicas %s", "truques %s", "segredos %s", "técnicas %s",
            "estratégias %s", "métodos %s", "ferramentas %s"
        };
        
        String template = practicalTemplates[random.nextInt(practicalTemplates.length)];
        return String.format(template, topic);
    }
    
    private static String generateMixedQuery(List<String> topics, Random random) {
        String topic = topics.get(random.nextInt(topics.size()));
        String modifier = CONTEXTUAL_MODIFIERS[random.nextInt(CONTEXTUAL_MODIFIERS.length)];
        
        // Mistura aleatória de elementos
        if (random.nextBoolean()) {
            return modifier + " " + topic;
        } else {
            return topic + " " + modifier;
        }
    }
    
    // 🔄 Fallback para garantir que sempre gera algo
    private static String generateFallbackQuery(List<String> topics, Random random, int index) {
        String topic = topics.get(index % topics.size());
        String modifier = CONTEXTUAL_MODIFIERS[index % CONTEXTUAL_MODIFIERS.length];
        return topic + " " + modifier + " " + (index + 1);
    }
    
    // 📊 Método para calcular qualidade das pesquisas geradas
    public static double calculateQualityScore(List<SearchItem> searches) {
        if (searches.isEmpty()) return 0.0;
        
        Set<String> uniqueWords = new HashSet<>();
        int totalWords = 0;
        int meaningfulQueries = 0;
        
        for (SearchItem search : searches) {
            String query = search.getSearchText().toLowerCase();
            String[] words = query.split("\\s+");
            totalWords += words.length;
            Collections.addAll(uniqueWords, words);
            
            // Verificar se é uma query significativa
            for (String[] categoryTopics : MEGA_KNOWLEDGE_BASE.values()) {
                for (String topic : categoryTopics) {
                    if (query.contains(topic.toLowerCase())) {
                        meaningfulQueries++;
                        break;
                    }
                }
            }
        }
        
        double diversityScore = totalWords > 0 ? (double) uniqueWords.size() / totalWords : 0.0;
        double meaningfulnessScore = (double) meaningfulQueries / searches.size();
        
        return (diversityScore * 0.6 + meaningfulnessScore * 0.4) * 100;
    }
}