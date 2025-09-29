package com.rewardsbot.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import androidx.core.app.NotificationCompat
import com.rewardsbot.MainActivity
import com.rewardsbot.R
import com.rewardsbot.utils.SearchGenerator
import kotlinx.coroutines.*

class SearchService : Service() {
    
    companion object {
        const val NOTIFICATION_ID = 1
        const val CHANNEL_ID = "search_service_channel"
        const val ACTION_UPDATE_PROGRESS = "com.rewardsbot.UPDATE_PROGRESS"
        const val EXTRA_CURRENT_SEARCH = "current_search"
        const val EXTRA_TOTAL_SEARCHES = "total_searches"
        const val EXTRA_PROGRESS = "progress"
        const val EXTRA_NEXT_SEARCH_TIME = "next_search_time"
        private const val SEARCH_INTERVAL_MS = 5000L // 5 segundos
    }
    
    private lateinit var searchGenerator: SearchGenerator
    private lateinit var notificationManager: NotificationManager
    private var serviceJob = SupervisorJob()
    private val serviceScope = CoroutineScope(Dispatchers.Main + serviceJob)
    
    private var searchList: List<String> = emptyList()
    private var currentSearchIndex = 0
    private var totalSearches = 0
    
    private val handler = Handler(Looper.getMainLooper())
    private var searchRunnable: Runnable? = null
    
    override fun onCreate() {
        super.onCreate()
        searchGenerator = SearchGenerator()
        notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        createNotificationChannel()
    }
    
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val searchCount = intent?.getIntExtra("search_count", 30) ?: 30
        totalSearches = searchCount
        
        startForegroundService()
        generateAndStartSearches(searchCount)
        
        return START_STICKY
    }
    
    override fun onBind(intent: Intent?): IBinder? = null
    
    override fun onDestroy() {
        super.onDestroy()
        searchRunnable?.let { handler.removeCallbacks(it) }
        serviceJob.cancel()
    }
    
    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Microsoft Rewards Bot",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Notificações do bot de pesquisas"
            }
            notificationManager.createNotificationChannel(channel)
        }
    }
    
    private fun startForegroundService() {
        val notification = createNotification(
            "Preparando pesquisas...",
            0,
            totalSearches,
            0
        )
        startForeground(NOTIFICATION_ID, notification)
    }
    
    private fun createNotification(
        contentText: String,
        progress: Int,
        maxProgress: Int,
        nextSearchIn: Int
    ) = NotificationCompat.Builder(this, CHANNEL_ID)
        .setContentTitle("Microsoft Rewards Bot")
        .setContentText(contentText)
        .setSmallIcon(android.R.drawable.ic_search_category_default)
        .setProgress(maxProgress, progress, false)
        .setSubText(if (nextSearchIn > 0) "Próxima pesquisa em ${nextSearchIn}s" else "")
        .setContentIntent(createPendingIntent())
        .setOngoing(true)
        .build()
    
    private fun createPendingIntent(): PendingIntent {
        val intent = Intent(this, MainActivity::class.java)
        return PendingIntent.getActivity(
            this,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }
    
    private fun generateAndStartSearches(searchCount: Int) {
        serviceScope.launch {
            try {
                updateNotification("Gerando pesquisas...", 0, 0)
                
                // Gera as pesquisas usando o gerador inteligente
                searchList = searchGenerator.generateSearchesFromOnlineSource(searchCount)
                currentSearchIndex = 0
                
                updateNotification("Iniciando pesquisas...", 0, 5)
                
                // Inicia as pesquisas
                scheduleNextSearch()
                
            } catch (e: Exception) {
                updateNotification("Erro ao gerar pesquisas", 0, 0)
            }
        }
    }
    
    private fun scheduleNextSearch() {
        if (currentSearchIndex >= searchList.size) {
            // Todas as pesquisas foram concluídas
            updateNotification("Todas as pesquisas concluídas!", searchList.size, 0)
            broadcastProgress(searchList.size, searchList.size, "", 0)
            stopSelf()
            return
        }
        
        val currentSearch = searchList[currentSearchIndex]
        
        // Atualiza notificação com countdown
        startCountdown(currentSearch)
        
        searchRunnable = Runnable {
            performSearch(currentSearch)
            currentSearchIndex++
            scheduleNextSearch()
        }
        
        handler.postDelayed(searchRunnable!!, SEARCH_INTERVAL_MS)
    }
    
    private fun startCountdown(searchQuery: String) {
        val countdownRunnable = object : Runnable {
            var secondsLeft = 5
            
            override fun run() {
                if (secondsLeft > 0) {
                    updateNotification(
                        "Próxima pesquisa: $searchQuery",
                        currentSearchIndex,
                        secondsLeft
                    )
                    broadcastProgress(
                        currentSearchIndex,
                        totalSearches,
                        searchQuery,
                        secondsLeft
                    )
                    secondsLeft--
                    handler.postDelayed(this, 1000)
                }
            }
        }
        handler.post(countdownRunnable)
    }
    
    private fun performSearch(query: String) {
        try {
            // Abre o Chrome com a pesquisa
            val searchUrl = "https://www.bing.com/search?q=${Uri.encode(query)}"
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(searchUrl)).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                setPackage("com.android.chrome") // Força uso do Chrome
            }
            
            // Se o Chrome não estiver instalado, usa o navegador padrão
            try {
                startActivity(intent)
            } catch (e: Exception) {
                intent.setPackage(null) // Remove restrição do Chrome
                startActivity(intent)
            }
            
            updateNotification(
                "Pesquisando: $query",
                currentSearchIndex + 1,
                0
            )
            
            broadcastProgress(
                currentSearchIndex + 1,
                totalSearches,
                query,
                0
            )
            
        } catch (e: Exception) {
            updateNotification(
                "Erro na pesquisa: $query",
                currentSearchIndex + 1,
                0
            )
        }
    }
    
    private fun updateNotification(text: String, progress: Int, nextSearchIn: Int) {
        val notification = createNotification(text, progress, totalSearches, nextSearchIn)
        notificationManager.notify(NOTIFICATION_ID, notification)
    }
    
    private fun broadcastProgress(
        current: Int,
        total: Int,
        currentSearch: String,
        nextSearchTime: Int
    ) {
        val intent = Intent(ACTION_UPDATE_PROGRESS).apply {
            putExtra(EXTRA_CURRENT_SEARCH, current)
            putExtra(EXTRA_TOTAL_SEARCHES, total)
            putExtra(EXTRA_PROGRESS, currentSearch)
            putExtra(EXTRA_NEXT_SEARCH_TIME, nextSearchTime)
        }
        sendBroadcast(intent)
    }
}