package com.deivid22srk.microsoftrewards.utils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

public class SearchGenerator {
    private Random random;
    
    // Categorias de pesquisas com termos relacionados
    private final String[][] categories = {
        // Tecnologia
        {"inteligência artificial", "machine learning", "blockchain", "realidade virtual", 
         "internet das coisas", "computação quântica", "robótica", "criptomoeda", 
         "desenvolvimento web", "aplicativos móveis", "cibersegurança", "nuvem"},
        
        // Ciência
        {"física quântica", "astronomia", "biologia molecular", "genética", 
         "neurociência", "medicina", "química orgânica", "paleontologia",
         "ecologia", "microbiologia", "psicologia", "antropologia"},
        
        // História
        {"história antiga", "império romano", "revolução francesa", "segunda guerra mundial",
         "civilização maia", "renascimento", "idade média", "revolução industrial",
         "descobrimentos", "história do brasil", "império otomano", "vikings"},
        
        // Geografia
        {"montanhas mais altas", "oceanos profundos", "países europeus", "capitais mundiais",
         "clima tropical", "desertos", "vulcões ativos", "rios importantes",
         "ilhas paradisíacas", "cidades históricas", "patrimônio mundial", "biodiversidade"},
        
        // Culinária
        {"receitas italiana", "culinária japonesa", "pratos vegetarianos", "sobremesas caseiras",
         "comida mexicana", "receitas saudáveis", "culinária brasileira", "pratos típicos",
         "gastronomia francesa", "comida árabe", "receitas veganas", "doces tradicionais"},
        
        // Esportes
        {"futebol brasileiro", "olimpíadas", "basquete NBA", "tênis profissional",
         "fórmula 1", "mundial fifa", "natação", "atletismo",
         "esportes extremos", "ginástica artística", "vôlei", "artes marciais"},
        
        // Arte e Cultura
        {"museus famosos", "pinturas clássicas", "arte moderna", "esculturas antigas",
         "música clássica", "cinema internacional", "teatro", "literatura mundial",
         "dança contemporânea", "fotografia artística", "arquitetura histórica", "design"},
        
        // Saúde e Bem-estar
        {"exercícios físicos", "yoga", "meditação", "alimentação saudável",
         "vitaminas", "plantas medicinais", "primeiros socorros", "saúde mental",
         "exercícios aeróbicos", "nutrição esportiva", "sono reparador", "hidratação"},
        
        // Educação
        {"aprender idiomas", "matemática básica", "história da educação", "metodologias ensino",
         "educação infantil", "ensino superior", "cursos online", "técnicas estudo",
         "educação especial", "tecnologia educacional", "pedagogia", "psicopedagogia"},
        
        // Natureza e Meio Ambiente
        {"animais selvagens", "plantas tropicais", "conservação ambiental", "mudanças climáticas",
         "energia renovável", "reciclagem", "biodiversidade", "florestas",
         "vida marinha", "sustentabilidade", "ecossistemas", "poluição ambiental"}
    };
    
    private final String[] questionWords = {
        "como", "onde", "quando", "por que", "o que é", "quem", "qual", "quanto"
    };
    
    private final String[] actionWords = {
        "fazer", "criar", "aprender", "descobrir", "encontrar", "entender", "usar", "comprar"
    };
    
    private final String[] adjectives = {
        "melhor", "mais fácil", "mais rápido", "mais eficiente", "mais barato", 
        "mais seguro", "mais popular", "mais interessante", "mais útil", "mais moderno"
    };

    public SearchGenerator() {
        this.random = new Random();
    }

    public List<String> generateSearches(int count) {
        List<String> searches = new ArrayList<>();
        
        for (int i = 0; i < count; i++) {
            String search = generateSingleSearch();
            
            // Evitar duplicatas
            while (searches.contains(search)) {
                search = generateSingleSearch();
            }
            
            searches.add(search);
        }
        
        return searches;
    }
    
    private String generateSingleSearch() {
        int searchType = random.nextInt(5);
        
        switch (searchType) {
            case 0:
                return generateQuestionSearch();
            case 1:
                return generateActionSearch();
            case 2:
                return generateComparativeSearch();
            case 3:
                return generateDirectSearch();
            default:
                return generateComplexSearch();
        }
    }
    
    private String generateQuestionSearch() {
        String questionWord = questionWords[random.nextInt(questionWords.length)];
        String topic = getRandomTopic();
        
        return questionWord + " " + topic;
    }
    
    private String generateActionSearch() {
        String actionWord = actionWords[random.nextInt(actionWords.length)];
        String topic = getRandomTopic();
        
        return actionWord + " " + topic;
    }
    
    private String generateComparativeSearch() {
        String adjective = adjectives[random.nextInt(adjectives.length)];
        String topic = getRandomTopic();
        
        return adjective + " " + topic;
    }
    
    private String generateDirectSearch() {
        return getRandomTopic();
    }
    
    private String generateComplexSearch() {
        String topic1 = getRandomTopic();
        String topic2 = getRandomTopic();
        
        String[] connectors = {"para", "com", "sobre", "em", "de", "e"};
        String connector = connectors[random.nextInt(connectors.length)];
        
        return topic1 + " " + connector + " " + topic2;
    }
    
    private String getRandomTopic() {
        int categoryIndex = random.nextInt(categories.length);
        String[] category = categories[categoryIndex];
        return category[random.nextInt(category.length)];
    }
    
    // Método para gerar pesquisas com base em tendências
    public List<String> generateTrendingSearches(int count) {
        List<String> trendingTopics = Arrays.asList(
            "inteligência artificial 2024", "mudanças climáticas", "energia solar",
            "carros elétricos", "realidade aumentada", "trabalho remoto",
            "criptomoedas", "sustentabilidade", "saúde mental", "receitas veganas",
            "exercícios em casa", "tecnologia 5G", "streaming", "jogos online",
            "educação digital", "marketplace", "e-commerce", "fintech"
        );
        
        List<String> searches = new ArrayList<>();
        
        for (int i = 0; i < count && i < trendingTopics.size(); i++) {
            String baseTopic = trendingTopics.get(i);
            String search = enhanceSearch(baseTopic);
            searches.add(search);
        }
        
        // Completar com pesquisas normais se necessário
        while (searches.size() < count) {
            searches.addAll(generateSearches(count - searches.size()));
        }
        
        return searches.subList(0, count);
    }
    
    private String enhanceSearch(String baseTopic) {
        String[] prefixes = {"como usar", "benefícios de", "tutorial", "guia completo", 
                           "melhores práticas", "futuro da", "impacto da"};
        
        if (random.nextBoolean()) {
            String prefix = prefixes[random.nextInt(prefixes.length)];
            return prefix + " " + baseTopic;
        }
        
        return baseTopic;
    }
}