package com.rewardsbot.service

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.GestureDescription
import android.content.Intent
import android.graphics.Path
import android.graphics.Rect
import android.net.Uri
import android.os.Handler
import android.os.Looper
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import android.util.Log

class SearchAccessibilityService : AccessibilityService() {
    
    companion object {
        private const val TAG = "SearchAccessibilityService"
        const val ACTION_PERFORM_SEARCH = "com.rewardsbot.PERFORM_SEARCH"
        const val EXTRA_SEARCH_QUERY = "search_query"
        
        // Package names para diferentes navegadores
        private val BROWSER_PACKAGES = listOf(
            "com.android.chrome",
            "com.microsoft.emmx", // Edge
            "org.mozilla.firefox",
            "com.opera.browser",
            "com.brave.browser"
        )
    }
    
    private val handler = Handler(Looper.getMainLooper())
    private var pendingSearchQuery: String? = null
    private var isSearchInProgress = false
    
    override fun onServiceConnected() {
        super.onServiceConnected()
        Log.d(TAG, "Accessibility Service Connected")
    }
    
    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        event?.let { handleAccessibilityEvent(it) }
    }
    
    override fun onInterrupt() {
        Log.d(TAG, "Accessibility Service Interrupted")
    }
    
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        intent?.let { handleIntent(it) }
        return START_STICKY
    }
    
    private fun handleIntent(intent: Intent) {
        when (intent.action) {
            ACTION_PERFORM_SEARCH -> {
                val query = intent.getStringExtra(EXTRA_SEARCH_QUERY)
                if (query != null && !isSearchInProgress) {
                    performSearch(query)
                }
            }
        }
    }
    
    private fun handleAccessibilityEvent(event: AccessibilityEvent) {
        if (!isBrowserPackage(event.packageName)) return
        
        when (event.eventType) {
            AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED,
            AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED -> {
                if (pendingSearchQuery != null && !isSearchInProgress) {
                    handler.postDelayed({
                        executeSearchInBrowser(pendingSearchQuery!!)
                        pendingSearchQuery = null
                    }, 2000) // Aguarda 2 segundos para o navegador carregar
                }
            }
        }
    }
    
    private fun performSearch(query: String) {
        isSearchInProgress = true
        pendingSearchQuery = query
        
        Log.d(TAG, "Performing search: $query")
        
        // Primeiro tenta abrir o navegador com a pesquisa
        openBrowserWithSearch(query)
        
        // Define um timeout para resetar o estado
        handler.postDelayed({
            isSearchInProgress = false
            pendingSearchQuery = null
        }, 15000) // 15 segundos timeout
    }
    
    private fun openBrowserWithSearch(query: String) {
        try {
            val searchUrl = "https://www.bing.com/search?q=${Uri.encode(query)}"
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(searchUrl)).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                
                // Tenta cada navegador na ordem de preferência
                for (browserPackage in BROWSER_PACKAGES) {
                    setPackage(browserPackage)
                    try {
                        startActivity(this)
                        Log.d(TAG, "Opened search in $browserPackage")
                        return
                    } catch (e: Exception) {
                        // Tenta o próximo navegador
                        continue
                    }
                }
                
                // Se nenhum navegador específico funcionou, usa o padrão
                setPackage(null)
                startActivity(this)
                Log.d(TAG, "Opened search in default browser")
                
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to open browser", e)
            isSearchInProgress = false
        }
    }
    
    private fun executeSearchInBrowser(query: String) {
        try {
            val rootNode = rootInActiveWindow ?: return
            
            // Tenta encontrar e interagir com elementos do navegador
            when {
                isChromePackage(rootNode.packageName) -> handleChromeSearch(rootNode, query)
                isEdgePackage(rootNode.packageName) -> handleEdgeSearch(rootNode, query)
                else -> handleGenericBrowserSearch(rootNode, query)
            }
            
        } catch (e: Exception) {
            Log.e(TAG, "Error executing search in browser", e)
        } finally {
            // Finaliza a pesquisa após 3 segundos
            handler.postDelayed({
                isSearchInProgress = false
            }, 3000)
        }
    }
    
    private fun handleChromeSearch(rootNode: AccessibilityNodeInfo, query: String) {
        // Procura pela barra de endereços do Chrome
        val addressBar = findNodeByResourceId(rootNode, "com.android.chrome:id/url_bar") ?:
                          findNodeByText(rootNode, "Search or type URL") ?:
                          findNodeByClassName(rootNode, "android.widget.EditText")
        
        if (addressBar != null) {
            performSearchInAddressBar(addressBar, query)
        } else {
            // Se não encontrar a barra, tenta scroll para garantir que a página carregou
            scrollDown(rootNode)
        }
    }
    
    private fun handleEdgeSearch(rootNode: AccessibilityNodeInfo, query: String) {
        // Similar ao Chrome, mas com IDs específicos do Edge
        val addressBar = findNodeByResourceId(rootNode, "com.microsoft.emmx:id/url_bar") ?:
                          findNodeByClassName(rootNode, "android.widget.EditText")
        
        if (addressBar != null) {
            performSearchInAddressBar(addressBar, query)
        } else {
            scrollDown(rootNode)
        }
    }
    
    private fun handleGenericBrowserSearch(rootNode: AccessibilityNodeInfo, query: String) {
        // Para navegadores genéricos, tenta encontrar qualquer campo de texto
        val searchField = findNodeByClassName(rootNode, "android.widget.EditText")
        
        if (searchField != null) {
            performSearchInAddressBar(searchField, query)
        } else {
            // Se não encontrar campo de texto, apenas faz scroll para simular atividade
            scrollDown(rootNode)
            handler.postDelayed({ scrollUp(rootNode) }, 1000)
        }
    }
    
    private fun performSearchInAddressBar(addressBar: AccessibilityNodeInfo, query: String) {
        // Clica na barra de endereços
        addressBar.performAction(AccessibilityNodeInfo.ACTION_CLICK)
        
        handler.postDelayed({
            // Limpa o texto existente
            addressBar.performAction(AccessibilityNodeInfo.ACTION_SELECT_ALL)
            
            handler.postDelayed({
                // Digita a nova pesquisa
                val arguments = android.os.Bundle().apply {
                    putString(AccessibilityNodeInfo.ACTION_ARGUMENT_SET_TEXT_CHARSEQUENCE, 
                             "https://www.bing.com/search?q=$query")
                }
                addressBar.performAction(AccessibilityNodeInfo.ACTION_SET_TEXT, arguments)
                
                handler.postDelayed({
                    // Pressiona Enter
                    performGlobalAction(GLOBAL_ACTION_BACK) // Simula Enter
                }, 500)
            }, 500)
        }, 500)
    }
    
    private fun scrollDown(rootNode: AccessibilityNodeInfo) {
        val scrollable = findScrollableNode(rootNode)
        scrollable?.performAction(AccessibilityNodeInfo.ACTION_SCROLL_FORWARD)
            ?: performSwipeGesture(false)
    }
    
    private fun scrollUp(rootNode: AccessibilityNodeInfo) {
        val scrollable = findScrollableNode(rootNode)
        scrollable?.performAction(AccessibilityNodeInfo.ACTION_SCROLL_BACKWARD)
            ?: performSwipeGesture(true)
    }
    
    private fun performSwipeGesture(swipeUp: Boolean) {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
            val displayMetrics = resources.displayMetrics
            val screenHeight = displayMetrics.heightPixels
            val screenWidth = displayMetrics.widthPixels
            
            val startY = if (swipeUp) screenHeight * 0.8f else screenHeight * 0.3f
            val endY = if (swipeUp) screenHeight * 0.3f else screenHeight * 0.8f
            
            val path = Path().apply {
                moveTo(screenWidth * 0.5f, startY)
                lineTo(screenWidth * 0.5f, endY)
            }
            
            val gesture = GestureDescription.Builder()
                .addStroke(GestureDescription.StrokeDescription(path, 0, 500))
                .build()
            
            dispatchGesture(gesture, null, null)
        }
    }
    
    // Funções auxiliares para busca de nós
    private fun findNodeByResourceId(root: AccessibilityNodeInfo, resourceId: String): AccessibilityNodeInfo? {
        if (root.viewIdResourceName == resourceId) return root
        
        for (i in 0 until root.childCount) {
            val child = root.getChild(i) ?: continue
            val result = findNodeByResourceId(child, resourceId)
            if (result != null) return result
        }
        return null
    }
    
    private fun findNodeByText(root: AccessibilityNodeInfo, text: String): AccessibilityNodeInfo? {
        if (root.text?.toString()?.contains(text, ignoreCase = true) == true) return root
        if (root.contentDescription?.toString()?.contains(text, ignoreCase = true) == true) return root
        
        for (i in 0 until root.childCount) {
            val child = root.getChild(i) ?: continue
            val result = findNodeByText(child, text)
            if (result != null) return result
        }
        return null
    }
    
    private fun findNodeByClassName(root: AccessibilityNodeInfo, className: String): AccessibilityNodeInfo? {
        if (root.className?.toString() == className && root.isEditable) return root
        
        for (i in 0 until root.childCount) {
            val child = root.getChild(i) ?: continue
            val result = findNodeByClassName(child, className)
            if (result != null) return result
        }
        return null
    }
    
    private fun findScrollableNode(root: AccessibilityNodeInfo): AccessibilityNodeInfo? {
        if (root.isScrollable) return root
        
        for (i in 0 until root.childCount) {
            val child = root.getChild(i) ?: continue
            val result = findScrollableNode(child)
            if (result != null) return result
        }
        return null
    }
    
    // Funções auxiliares para identificação de navegadores
    private fun isBrowserPackage(packageName: CharSequence?): Boolean {
        return packageName?.toString() in BROWSER_PACKAGES
    }
    
    private fun isChromePackage(packageName: CharSequence?): Boolean {
        return packageName?.toString() == "com.android.chrome"
    }
    
    private fun isEdgePackage(packageName: CharSequence?): Boolean {
        return packageName?.toString() == "com.microsoft.emmx"
    }
}