package com.rewardsbot.viewmodel

import android.accessibilityservice.AccessibilityServiceInfo
import android.content.Context
import android.net.Uri
import android.provider.Settings
import android.view.accessibility.AccessibilityManager
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class MainViewModel : ViewModel() {
    
    private val _searchCount = MutableStateFlow(30)
    val searchCount = _searchCount.asStateFlow()
    
    private val _isRunning = MutableStateFlow(false)
    val isRunning = _isRunning.asStateFlow()
    
    private val _hasOverlayPermission = MutableStateFlow(false)
    val hasOverlayPermission = _hasOverlayPermission.asStateFlow()
    
    private val _hasAccessibilityPermission = MutableStateFlow(false)
    val hasAccessibilityPermission = _hasAccessibilityPermission.asStateFlow()
    
    fun updateSearchCount(count: Int) {
        _searchCount.value = count
    }
    
    fun setRunning(running: Boolean) {
        _isRunning.value = running
    }
    
    fun checkPermissions(context: Context) {
        viewModelScope.launch {
            _hasOverlayPermission.value = Settings.canDrawOverlays(context)
            _hasAccessibilityPermission.value = isAccessibilityServiceEnabled(context)
        }
    }
    
    private fun isAccessibilityServiceEnabled(context: Context): Boolean {
        val accessibilityManager = context.getSystemService(Context.ACCESSIBILITY_SERVICE) as AccessibilityManager
        val enabledServices = accessibilityManager.getEnabledAccessibilityServiceList(AccessibilityServiceInfo.FEEDBACK_ALL_MASK)
        
        for (service in enabledServices) {
            if (service.resolveInfo.serviceInfo.packageName == context.packageName) {
                return true
            }
        }
        return false
    }
}