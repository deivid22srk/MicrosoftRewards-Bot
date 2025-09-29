# Microsoft Rewards Bot

Um aplicativo Android em Kotlin que automatiza pesquisas do Microsoft Rewards usando IA para gerar pesquisas aleatórias e inteligentes.

## 🤖 Funcionalidades

- **Interface Material You**: Design moderno que se adapta ao tema do sistema
- **Gerador de IA**: Cria pesquisas aleatórias inteligentes sem necessidade de API Key
- **Automação Inteligente**: Executa pesquisas automaticamente no Chrome/navegador padrão
- **Botão Flutuante**: Mostra o progresso em tempo real sobre qualquer app
- **Intervalo Customizável**: Pesquisas a cada 5 segundos (configurável)
- **Múltiplos Navegadores**: Suporta Chrome, Edge, Firefox e outros

## 📱 Capturas de Tela

- Interface principal com Material You design
- Botão flutuante mostrando progresso
- Configurações de número de pesquisas (1-100)

## 🚀 Como Usar

1. **Instale o APK** no seu dispositivo Android
2. **Habilite Permissões**:
   - Sobreposição de tela (para botão flutuante)
   - Serviço de acessibilidade (para automação)
3. **Configure** o número de pesquisas desejado
4. **Pressione "Iniciar Pesquisas"** e deixe o bot trabalhar!

## 🛠️ Tecnologias Utilizadas

- **Kotlin** - Linguagem principal
- **Jetpack Compose** - Interface moderna
- **Material Design 3** - Material You theming
- **Accessibility Service** - Automação do navegador
- **Overlay Service** - Botão flutuante
- **Coroutines** - Programação assíncrona
- **OkHttp** - Cliente HTTP para APIs
- **Moshi** - Parsing JSON

## 🏗️ Arquitetura

```
├── MainActivity - Interface principal
├── Services
│   ├── SearchService - Coordena as pesquisas
│   ├── FloatingButtonService - Botão flutuante
│   └── SearchAccessibilityService - Automação do navegador
├── Utils
│   └── SearchGenerator - Gera pesquisas com IA
└── ViewModels
    └── MainViewModel - Gerencia estado da UI
```

## 🔧 Compilação

### Pré-requisitos
- Android Studio Arctic Fox ou superior
- JDK 17
- Android SDK 34

### Build Local
```bash
git clone https://github.com/deivid22srk/MicrosoftRewards-Bot.git
cd MicrosoftRewards-Bot
./gradlew assembleDebug
```

### Build via GitHub Actions
O projeto inclui CI/CD automático que gera APKs a cada push:

1. Push para o branch `main`
2. GitHub Actions compila automaticamente
3. Download do APK nos Artifacts da Action

## 📋 Permissões Necessárias

- **INTERNET** - Para gerar pesquisas online
- **SYSTEM_ALERT_WINDOW** - Botão flutuante
- **BIND_ACCESSIBILITY_SERVICE** - Automação do navegador
- **FOREGROUND_SERVICE** - Execução em background
- **WAKE_LOCK** - Manter dispositivo ativo

## ⚙️ Como Funciona

1. **Geração de Pesquisas**: Usa IA local e APIs gratuitas para criar pesquisas variadas
2. **Execução**: Abre o navegador com URLs do Bing automaticamente
3. **Monitoramento**: Botão flutuante mostra progresso em tempo real
4. **Intervalo**: Aguarda 5 segundos entre cada pesquisa

## 🔒 Privacidade

- ✅ Todas as pesquisas são geradas localmente
- ✅ Nenhum dado pessoal é coletado
- ✅ Código fonte aberto para auditoria
- ✅ APIs gratuitas sem necessidade de login

## 🤝 Contribuindo

1. Fork o projeto
2. Crie uma branch para sua feature (`git checkout -b feature/nova-feature`)
3. Commit suas mudanças (`git commit -am 'Adiciona nova feature'`)
4. Push para a branch (`git push origin feature/nova-feature`)
5. Abra um Pull Request

## ⚠️ Disclaimer

Este aplicativo é apenas para fins educacionais. Use-o responsavelmente e de acordo com os termos de serviço do Microsoft Rewards. O desenvolvedor não se responsabiliza por qualquer violação dos termos de uso.

## 📄 Licença

Este projeto está sob a licença MIT. Veja o arquivo [LICENSE](LICENSE) para mais detalhes.

## 🆘 Suporte

Encontrou um bug ou tem uma sugestão? Abra uma [issue](https://github.com/deivid22srk/MicrosoftRewards-Bot/issues)!

---

**Desenvolvido com ❤️ usando Kotlin e Material You**