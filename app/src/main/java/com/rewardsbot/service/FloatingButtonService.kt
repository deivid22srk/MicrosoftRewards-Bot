package com.rewardsbot.service

import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.PixelFormat
import android.os.Build
import android.os.IBinder
import android.view.Gravity
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import android.widget.FrameLayout
import android.widget.ProgressBar
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import com.rewardsbot.MainActivity
import com.rewardsbot.R

class FloatingButtonService : Service() {
    
    private lateinit var windowManager: WindowManager
    private var floatingView: View? = null
    private var progressBar: ProgressBar? = null
    private var progressText: TextView? = null
    private var statusText: TextView? = null
    
    private var isDragging = false
    private var initialTouchX = 0f
    private var initialTouchY = 0f
    private var initialX = 0
    private var initialY = 0
    
    private val progressReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent?.action == SearchService.ACTION_UPDATE_PROGRESS) {
                val current = intent.getIntExtra(SearchService.EXTRA_CURRENT_SEARCH, 0)
                val total = intent.getIntExtra(SearchService.EXTRA_TOTAL_SEARCHES, 0)
                val currentSearch = intent.getStringExtra(SearchService.EXTRA_PROGRESS) ?: ""
                val nextSearchTime = intent.getIntExtra(SearchService.EXTRA_NEXT_SEARCH_TIME, 0)
                
                updateProgress(current, total, currentSearch, nextSearchTime)
            }
        }
    }
    
    override fun onCreate() {
        super.onCreate()
        windowManager = getSystemService(WINDOW_SERVICE) as WindowManager
        createFloatingView()
        registerProgressReceiver()
    }
    
    override fun onBind(intent: Intent?): IBinder? = null
    
    override fun onDestroy() {
        super.onDestroy()
        removeFloatingView()
        unregisterProgressReceiver()
    }
    
    private fun createFloatingView() {
        floatingView = createFloatingLayout()
        
        val layoutParams = WindowManager.LayoutParams().apply {
            width = WindowManager.LayoutParams.WRAP_CONTENT
            height = WindowManager.LayoutParams.WRAP_CONTENT
            type = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
            } else {
                @Suppress("DEPRECATION")
                WindowManager.LayoutParams.TYPE_PHONE
            }
            flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                    WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS
            format = PixelFormat.TRANSLUCENT
            gravity = Gravity.TOP or Gravity.START
            x = 50
            y = 100
        }
        
        windowManager.addView(floatingView, layoutParams)
        setupTouchListener(layoutParams)
    }
    
    private fun createFloatingLayout(): View {
        val view = FrameLayout(this).apply {
            val cardView = CardView(this@FloatingButtonService).apply {
                radius = 24f
                cardElevation = 8f
                setCardBackgroundColor(ContextCompat.getColor(this@FloatingButtonService, android.R.color.white))
                
                val padding = 16
                setPadding(padding, padding, padding, padding)
                
                val container = FrameLayout(this@FloatingButtonService)
                
                // Progress bar circular
                progressBar = ProgressBar(this@FloatingButtonService, null, android.R.attr.progressBarStyleHorizontal).apply {
                    max = 100
                    progress = 0
                    layoutParams = FrameLayout.LayoutParams(120, 120).apply {
                        gravity = Gravity.CENTER
                    }
                    
                    // Estilo circular
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                        progressDrawable = ContextCompat.getDrawable(this@FloatingButtonService, android.R.drawable.progress_horizontal)
                    }
                }
                
                // Texto do progresso no centro
                progressText = TextView(this@FloatingButtonService).apply {
                    text = "0/0"
                    textSize = 14f
                    setTextColor(ContextCompat.getColor(this@FloatingButtonService, android.R.color.black))
                    gravity = Gravity.CENTER
                    layoutParams = FrameLayout.LayoutParams(
                        FrameLayout.LayoutParams.WRAP_CONTENT,
                        FrameLayout.LayoutParams.WRAP_CONTENT
                    ).apply {
                        gravity = Gravity.CENTER
                    }
                }
                
                // Status text
                statusText = TextView(this@FloatingButtonService).apply {
                    text = "Iniciando..."
                    textSize = 10f
                    maxLines = 1
                    setTextColor(ContextCompat.getColor(this@FloatingButtonService, android.R.color.darker_gray))
                    gravity = Gravity.CENTER
                    layoutParams = FrameLayout.LayoutParams(
                        FrameLayout.LayoutParams.WRAP_CONTENT,
                        FrameLayout.LayoutParams.WRAP_CONTENT
                    ).apply {
                        gravity = Gravity.CENTER
                        topMargin = 40
                    }
                }
                
                container.addView(progressBar)
                container.addView(progressText)
                container.addView(statusText)
                addView(container)
                
                // Click listener para abrir o app
                setOnClickListener {
                    if (!isDragging) {
                        openMainActivity()
                    }
                }
            }
            
            addView(cardView)
        }
        
        return view
    }
    
    private fun setupTouchListener(layoutParams: WindowManager.LayoutParams) {
        floatingView?.setOnTouchListener { _, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    isDragging = false
                    initialTouchX = event.rawX
                    initialTouchY = event.rawY
                    initialX = layoutParams.x
                    initialY = layoutParams.y
                    true
                }
                
                MotionEvent.ACTION_MOVE -> {
                    val deltaX = event.rawX - initialTouchX
                    val deltaY = event.rawY - initialTouchY
                    
                    if (Math.abs(deltaX) > 10 || Math.abs(deltaY) > 10) {
                        isDragging = true
                        layoutParams.x = initialX + deltaX.toInt()
                        layoutParams.y = initialY + deltaY.toInt()
                        windowManager.updateViewLayout(floatingView, layoutParams)
                    }
                    true
                }
                
                MotionEvent.ACTION_UP -> {
                    // Permite click após um pequeno delay
                    if (isDragging) {
                        android.os.Handler().postDelayed({ isDragging = false }, 100)
                    }
                    true
                }
                
                else -> false
            }
        }
    }
    
    private fun updateProgress(current: Int, total: Int, currentSearch: String, nextSearchTime: Int) {
        progressText?.text = "$current/$total"
        
        val progressPercentage = if (total > 0) (current * 100) / total else 0
        progressBar?.progress = progressPercentage
        
        val status = when {
            nextSearchTime > 0 -> "Próxima em ${nextSearchTime}s"
            current >= total -> "Concluído!"
            currentSearch.isNotEmpty() -> currentSearch.take(20) + "..."
            else -> "Processando..."
        }
        
        statusText?.text = status
    }
    
    private fun openMainActivity() {
        val intent = Intent(this, MainActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP)
        }
        startActivity(intent)
    }
    
    private fun removeFloatingView() {
        floatingView?.let { view ->
            try {
                windowManager.removeView(view)
            } catch (e: Exception) {
                // View may already be removed
            }
            floatingView = null
        }
    }
    
    private fun registerProgressReceiver() {
        val filter = IntentFilter(SearchService.ACTION_UPDATE_PROGRESS)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            registerReceiver(progressReceiver, filter, RECEIVER_NOT_EXPORTED)
        } else {
            registerReceiver(progressReceiver, filter)
        }
    }
    
    private fun unregisterProgressReceiver() {
        try {
            unregisterReceiver(progressReceiver)
        } catch (e: Exception) {
            // Receiver may not be registered
        }
    }
}