package com.deivid22srk.microsoftrewards.utils;

import com.deivid22srk.microsoftrewards.model.SearchItem;

import java.util.*;
import java.text.SimpleDateFormat;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Mini IA para geração inteligente de pesquisas para Microsoft Rewards
 * Usa técnicas de NLP e algoritmos de diversificação para gerar consultas naturais e variadas
 */
public class SmartSearchGenerator {
    
    private static final Random random = new Random();
    
    // 🧠 Base de conhecimento contextual
    private static final Map<String, String[]> KNOWLEDGE_BASE = new HashMap<String, String[]>() {{
        
        // 🔬 CIÊNCIA & TECNOLOGIA
        put("ciencia", new String[]{
            "inteligência artificial", "machine learning", "deep learning", "biotecnologia", "nanotecnologia",
            "física quântica", "astronomia", "neurociência", "genética", "robótica", "blockchain", "metaverso",
            "computação quântica", "realidade aumentada", "internet das coisas", "energia renovável"
        });
        
        // 💡 INOVAÇÃO & FUTURO
        put("inovacao", new String[]{
            "carros elétricos", "energia solar", "sustentabilidade", "smart cities", "agricultura vertical",
            "impressão 3D", "drones", "telemedicina", "educação online", "trabalho remoto", "fintech",
            "criptomoedas", "NFT", "startups", "venture capital", "disrupção digital"
        });
        
        // 🎨 CULTURA & ARTE
        put("cultura", new String[]{
            "arte contemporânea", "música eletrônica", "cinema indie", "literatura moderna", "design thinking",
            "arquitetura sustentável", "fotografia digital", "street art", "cultura pop", "influenciadores",
            "streaming", "podcast", "gaming", "esports", "realidade virtual"
        });
        
        // 🌍 SOCIEDADE & LIFESTYLE
        put("lifestyle", new String[]{
            "mindfulness", "minimalismo", "veganismo", "fitness", "yoga", "meditação", "bem-estar",
            "autocuidado", "desenvolvimento pessoal", "produtividade", "organização", "decluttering",
            "slow living", "digital detox", "sustentabilidade pessoal", "economia circular"
        });
        
        // 📚 EDUCAÇÃO & CARREIRA
        put("educacao", new String[]{
            "cursos online", "certificações", "LinkedIn Learning", "Coursera", "soft skills", "hard skills",
            "networking", "personal branding", "carreira tech", "empreendedorismo", "liderança",
            "gestão de projetos", "análise de dados", "UX design", "marketing digital", "growth hacking"
        });
        
        // 🍔 GASTRONOMIA & CULINÁRIA
        put("gastronomia", new String[]{
            "culinária plant-based", "fermentação", "gastronomia molecular", "comfort food", "street food",
            "receitas saudáveis", "meal prep", "bebidas artesanais", "vinhos naturais", "café specialty",
            "panificação artesanal", "culinária internacional", "food truck", "delivery gourmet"
        });
        
        // 🏠 CASA & DECORAÇÃO
        put("casa", new String[]{
            "decoração minimalista", "plantas de interior", "casa inteligente", "automação residencial",
            "feng shui", "upcycling", "DIY", "organização de espaços", "arquitetura de interiores",
            "iluminação LED", "jardim vertical", "móveis multifuncionais", "decoração sustentável"
        });
        
        // 🌱 SUSTENTABILIDADE & MEIO AMBIENTE
        put("sustentabilidade", new String[]{
            "mudanças climáticas", "carbono zero", "energia limpa", "reciclagem", "economia verde",
            "biodiversidade", "conservação marinha", "reflorestamento", "permacultura", "eco-design",
            "mobilidade sustentável", "consumo consciente", "pegada de carbono", "vida selvagem"
        });
        
        // 💼 NEGÓCIOS & ECONOMIA
        put("negocios", new String[]{
            "economia digital", "transformação digital", "business intelligence", "big data", "analytics",
            "automação de processos", "lean startup", "modelo de negócios", "customer experience",
            "omnichannel", "marketplace", "B2B", "B2C", "SaaS", "revenue operations", "growth marketing"
        });
        
        // 🎯 TENDÊNCIAS 2024
        put("trends2024", new String[]{
            "Gen Z trends", "TikTok viral", "Instagram Reels", "Threads", "BeReal", "ChatGPT", "Claude AI",
            "Midjourney", "stable diffusion", "Web3", "creator economy", "live commerce", "social commerce",
            "voice search", "visual search", "conversational AI", "micro-influencers", "authentic content"
        });
    }};
    
    // 🎭 Templates de pesquisa inteligentes
    private static final String[] QUESTION_TEMPLATES = {
        "como %s funciona", "o que é %s", "por que %s é importante", "quando usar %s",
        "onde encontrar %s", "quem inventou %s", "como aprender %s", "benefícios de %s",
        "história de %s", "futuro de %s", "impacto de %s", "tendências em %s",
        "melhores práticas %s", "guia completo %s", "tutorial %s", "dicas de %s",
        "curso de %s", "carreira em %s", "especialista em %s", "certificação %s"
    };
    
    private static final String[] ACTION_TEMPLATES = {
        "%s para iniciantes", "%s avançado", "%s passo a passo", "aprenda %s",
        "domine %s", "especialize-se em %s", "torne-se expert em %s", "carreira %s",
        "trabalhar com %s", "investir em %s", "negócios %s", "startup %s",
        "inovar com %s", "aplicar %s", "implementar %s", "otimizar %s"
    };
    
    private static final String[] COMPARATIVE_TEMPLATES = {
        "%s vs %s", "melhor %s", "top %s 2024", "%s mais popular", "ranking %s",
        "comparar %s", "%s premium", "%s profissional", "%s empresarial",
        "%s gratuito", "alternativas para %s", "%s mais barato", "%s custo-benefício"
    };
    
    private static final String[] TEMPORAL_MODIFIERS = {
        "2024", "2025", "atual", "moderno", "novo", "próximo", "futuro", "recente",
        "último", "inovador", "revolucionário", "disruptivo", "emergente", "trending"
    };
    
    private static final String[] INTENT_MODIFIERS = {
        "completo", "detalhado", "prático", "essencial", "fundamental", "avançado",
        "profissional", "empresarial", "pessoal", "rápido", "fácil", "simples",
        "eficiente", "otimizado", "personalizado", "customizado", "exclusivo"
    };
    
    // 🧮 Algoritmos de IA
    
    /**
     * Gera pesquisas usando IA contextual
     */
    public List<SearchItem> generateIntelligentSearches(int count) {
        List<SearchItem> searches = new ArrayList<>();
        Set<String> usedQueries = new HashSet<>();
        
        for (int i = 0; i < count; i++) {
            String query = generateContextualQuery(usedQueries);
            
            // Evita duplicatas com algoritmo de similaridade
            int attempts = 0;
            while (usedQueries.contains(query) && attempts < 10) {
                query = generateContextualQuery(usedQueries);
                attempts++;
            }
            
            usedQueries.add(query);
            searches.add(new SearchItem(query, SearchItem.Status.PENDING));
        }
        
        return searches;
    }
    
    /**
     * Geração contextual inteligente de queries
     */
    private String generateContextualQuery(Set<String> usedQueries) {
        int strategy = random.nextInt(6);
        
        switch (strategy) {
            case 0: return generateSemanticQuery();
            case 1: return generateTrendingQuery(); 
            case 2: return generateComparativeQuery();
            case 3: return generateTemporalQuery();
            case 4: return generateProfessionalQuery();
            case 5: return generatePersonalizedQuery();
            default: return generateSemanticQuery();
        }
    }
    
    /**
     * 🎯 Geração semântica com contexto
     */
    private String generateSemanticQuery() {
        // Seleciona categoria aleatória
        String[] categories = KNOWLEDGE_BASE.keySet().toArray(new String[0]);
        String category = categories[random.nextInt(categories.length)];
        String[] topics = KNOWLEDGE_BASE.get(category);
        
        String mainTopic = topics[random.nextInt(topics.length)];
        String template = QUESTION_TEMPLATES[random.nextInt(QUESTION_TEMPLATES.length)];
        
        // Adiciona modificadores contextuais
        if (random.nextBoolean()) {
            String modifier = INTENT_MODIFIERS[random.nextInt(INTENT_MODIFIERS.length)];
            mainTopic = modifier + " " + mainTopic;
        }
        
        return String.format(template, mainTopic);
    }
    
    /**
     * 📈 Geração de queries trending
     */
    private String generateTrendingQuery() {
        String[] trendingTopics = KNOWLEDGE_BASE.get("trends2024");
        String topic = trendingTopics[random.nextInt(trendingTopics.length)];
        String temporal = TEMPORAL_MODIFIERS[random.nextInt(TEMPORAL_MODIFIERS.length)];
        
        String[] patterns = {
            "%s %s", "%s para %s", "como usar %s em %s", 
            "%s tendências %s", "novidades %s %s"
        };
        
        String pattern = patterns[random.nextInt(patterns.length)];
        return String.format(pattern, topic, temporal);
    }
    
    /**
     * ⚖️ Geração de queries comparativas
     */
    private String generateComparativeQuery() {
        String[] categories = KNOWLEDGE_BASE.keySet().toArray(new String[0]);
        String category = categories[random.nextInt(categories.length)];
        String[] topics = KNOWLEDGE_BASE.get(category);
        
        if (topics.length >= 2) {
            String topic1 = topics[random.nextInt(topics.length)];
            String topic2 = topics[random.nextInt(topics.length)];
            
            while (topic1.equals(topic2)) {
                topic2 = topics[random.nextInt(topics.length)];
            }
            
            String template = COMPARATIVE_TEMPLATES[random.nextInt(COMPARATIVE_TEMPLATES.length)];
            
            if (template.contains("vs")) {
                return String.format(template, topic1, topic2);
            } else {
                return String.format(template, topic1);
            }
        }
        
        return generateSemanticQuery(); // Fallback
    }
    
    /**
     * ⏰ Geração temporal contextual
     */
    private String generateTemporalQuery() {
        String temporal = TEMPORAL_MODIFIERS[random.nextInt(TEMPORAL_MODIFIERS.length)];
        String[] categories = KNOWLEDGE_BASE.keySet().toArray(new String[0]);
        String category = categories[random.nextInt(categories.length)];
        String[] topics = KNOWLEDGE_BASE.get(category);
        String topic = topics[random.nextInt(topics.length)];
        
        String[] patterns = {
            "%s em %s", "%s para %s", "tendências %s %s",
            "novidades %s %s", "%s mais %s", "futuro %s %s"
        };
        
        String pattern = patterns[random.nextInt(patterns.length)];
        return String.format(pattern, topic, temporal);
    }
    
    /**
     * 💼 Geração profissional/carreira
     */
    private String generateProfessionalQuery() {
        String[] categories = {"ciencia", "inovacao", "educacao", "negocios"};
        String category = categories[random.nextInt(categories.length)];
        String[] topics = KNOWLEDGE_BASE.get(category);
        String topic = topics[random.nextInt(topics.length)];
        
        String template = ACTION_TEMPLATES[random.nextInt(ACTION_TEMPLATES.length)];
        return String.format(template, topic);
    }
    
    /**
     * 👤 Geração personalizada/lifestyle
     */
    private String generatePersonalizedQuery() {
        String[] categories = {"lifestyle", "gastronomia", "casa", "sustentabilidade"};
        String category = categories[random.nextInt(categories.length)];
        String[] topics = KNOWLEDGE_BASE.get(category);
        String topic = topics[random.nextInt(topics.length)];
        
        String[] personalizedTemplates = {
            "como %s em casa", "%s para iniciantes", "dicas %s", 
            "guia %s", "começar %s", "aprender %s sozinho"
        };
        
        String template = personalizedTemplates[random.nextInt(personalizedTemplates.length)];
        return String.format(template, topic);
    }
    
    /**
     * 🎲 Diversificação avançada
     */
    public List<SearchItem> generateDiversifiedSearches(int count) {
        List<SearchItem> searches = new ArrayList<>();
        Set<String> usedCategories = new HashSet<>();
        Set<String> usedQueries = new HashSet<>();
        
        // Força diversificação de categorias
        String[] allCategories = KNOWLEDGE_BASE.keySet().toArray(new String[0]);
        
        for (int i = 0; i < count; i++) {
            // Reset categories quando todas foram usadas
            if (usedCategories.size() >= allCategories.length) {
                usedCategories.clear();
            }
            
            String query = generateBalancedQuery(usedCategories, usedQueries);
            searches.add(new SearchItem(query, SearchItem.Status.PENDING));
        }
        
        return searches;
    }
    
    private String generateBalancedQuery(Set<String> usedCategories, Set<String> usedQueries) {
        // Seleciona categoria não utilizada recentemente
        String[] allCategories = KNOWLEDGE_BASE.keySet().toArray(new String[0]);
        List<String> availableCategories = new ArrayList<>();
        
        for (String cat : allCategories) {
            if (!usedCategories.contains(cat)) {
                availableCategories.add(cat);
            }
        }
        
        if (availableCategories.isEmpty()) {
            // Se todas foram usadas, usa qualquer uma
            availableCategories = Arrays.asList(allCategories);
        }
        
        String category = availableCategories.get(random.nextInt(availableCategories.size()));
        usedCategories.add(category);
        
        String[] topics = KNOWLEDGE_BASE.get(category);
        String topic = topics[random.nextInt(topics.length)];
        
        // Gera query única
        String query = generateUniqueVariation(topic, usedQueries);
        usedQueries.add(query);
        
        return query;
    }
    
    private String generateUniqueVariation(String baseTopic, Set<String> usedQueries) {
        String[] allTemplates = combineArrays(QUESTION_TEMPLATES, ACTION_TEMPLATES);
        
        for (int attempt = 0; attempt < 5; attempt++) {
            String template = allTemplates[random.nextInt(allTemplates.length)];
            String candidate = String.format(template, baseTopic);
            
            if (!usedQueries.contains(candidate)) {
                return candidate;
            }
            
            // Adiciona variação se já existe
            String modifier = INTENT_MODIFIERS[random.nextInt(INTENT_MODIFIERS.length)];
            candidate = String.format(template, modifier + " " + baseTopic);
            
            if (!usedQueries.contains(candidate)) {
                return candidate;
            }
        }
        
        // Fallback com timestamp para garantir unicidade
        return baseTopic + " " + System.currentTimeMillis() % 1000;
    }
    
    /**
     * 📊 Análise de diversidade das pesquisas
     */
    public double calculateDiversityScore(List<SearchItem> searches) {
        Set<String> uniqueWords = new HashSet<>();
        int totalWords = 0;
        
        for (SearchItem search : searches) {
            String[] words = search.getSearchQuery().toLowerCase().split("\\s+");
            totalWords += words.length;
            Collections.addAll(uniqueWords, words);
        }
        
        return totalWords > 0 ? (double) uniqueWords.size() / totalWords : 0.0;
    }
    
    // 🔧 Utilities
    private String[] combineArrays(String[] array1, String[] array2) {
        String[] combined = new String[array1.length + array2.length];
        System.arraycopy(array1, 0, combined, 0, array1.length);
        System.arraycopy(array2, 0, combined, array1.length, array2.length);
        return combined;
    }
    
    /**
     * 🎯 Método principal para integração
     */
    public static List<SearchItem> generateSmartSearches(int count) {
        SmartSearchGenerator generator = new SmartSearchGenerator();
        return generator.generateDiversifiedSearches(count);
    }
}