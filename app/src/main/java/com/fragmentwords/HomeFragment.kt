package com.fragmentwords

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.DecelerateInterpolator
import android.widget.FrameLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.fragmentwords.data.WordRepository
import com.fragmentwords.service.WordService
import com.fragmentwords.utils.AlarmScheduler
import com.fragmentwords.utils.AppPreferences
import com.fragmentwords.utils.LibrarySelection
import com.fragmentwords.utils.NotificationPermissionHelper
import com.fragmentwords.utils.WorkManagerScheduler
import kotlinx.coroutines.launch

class HomeFragment : Fragment() {

    companion object {
        private const val PREFS_NAME = "word_prefs"
        private const val KEY_NOTIFICATION_ENABLED = "notification_enabled"
        private const val KEY_LAST_REFRESH_TIME = "last_refresh_time"
        private const val KEY_JUST_CLICKED = "just_clicked_button"
        private const val KEY_CLICK_TIME = "button_click_time"
    }

    private lateinit var repository: WordRepository
    private lateinit var tvStatus: TextView
    private lateinit var tvLibraryInfo: TextView
    private lateinit var tvNotebookInfo: TextView
    private lateinit var toggleContainer: FrameLayout
    private lateinit var toggleThumb: View

    private var isPushEnabled = false
    private var pendingEnableAfterPermission = false
    private var runtimeSyncedThisView = false

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            if (pendingEnableAfterPermission) {
                if (NotificationPermissionHelper.canPostNotifications(requireContext())) {
                    enablePush(showToast = true)
                } else {
                    pendingEnableAfterPermission = false
                    showPermissionGuideDialog()
                }
            } else if (readPushEnabledFromPrefs() && NotificationPermissionHelper.canPostNotifications(requireContext())) {
                syncPushRuntime(enabled = true)
                runtimeSyncedThisView = true
            }
        } else {
            pendingEnableAfterPermission = false
            persistPushEnabled(false)
            isPushEnabled = false
            updateToggleUI()
            updateUI()
            showPermissionGuideDialog()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_home, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        runtimeSyncedThisView = false
        repository = WordRepository(requireContext())
        initViews(view)
        setupClickListeners()
        refreshPushStateFromPrefs()
        updateUI()

        lifecycleScope.launch {
            repository.initializeIfNeeded()
            updateUI()
        }

        ensurePermissionStateOnEntry()
    }

    override fun onResume() {
        super.onResume()
        refreshPushStateFromPrefs()
        updateUI()
    }

    private fun initViews(view: View) {
        tvStatus = view.findViewById(R.id.tv_status)
        tvLibraryInfo = view.findViewById(R.id.tv_library_info)
        tvNotebookInfo = view.findViewById(R.id.tv_notebook_info)
        toggleContainer = view.findViewById(R.id.toggle_container)
        toggleThumb = view.findViewById(R.id.toggle_thumb)
    }

    private fun setupClickListeners() {
        toggleContainer.setOnClickListener {
            if (isPushEnabled) {
                disablePush(showToast = true)
            } else {
                requestEnablePush()
            }
        }
    }

    private fun requestEnablePush() {
        if (NotificationPermissionHelper.canPostNotifications(requireContext())) {
            enablePush(showToast = true)
            return
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            !NotificationPermissionHelper.hasRuntimePermission(requireContext())
        ) {
            showPermissionConsentDialog()
        } else {
            showPermissionGuideDialog()
        }
    }

    private fun enablePush(showToast: Boolean) {
        pendingEnableAfterPermission = false
        isPushEnabled = true
        persistPushEnabled(true)
        updateToggleUI()
        syncPushRuntime(enabled = true)
        runtimeSyncedThisView = true
        updateUI()

        if (showToast) {
            Toast.makeText(requireContext(), getString(R.string.push_enabled_toast), Toast.LENGTH_SHORT).show()
        }
    }

    private fun disablePush(showToast: Boolean) {
        pendingEnableAfterPermission = false
        isPushEnabled = false
        persistPushEnabled(false)
        updateToggleUI()
        syncPushRuntime(enabled = false)
        runtimeSyncedThisView = false
        updateUI()

        if (showToast) {
            Toast.makeText(requireContext(), getString(R.string.push_disabled_toast), Toast.LENGTH_SHORT).show()
        }
    }

    private fun syncPushRuntime(enabled: Boolean) {
        if (enabled) {
            if (!NotificationPermissionHelper.canPostNotifications(requireContext())) {
                return
            }
            WordService.startService(requireContext())
            AlarmScheduler.schedulePeriodicAlarm(requireContext())
            WorkManagerScheduler.cancelRefresh(requireContext())
        } else {
            WordService.stopService(requireContext())
            AlarmScheduler.cancelAlarms(requireContext())
            WorkManagerScheduler.cancelRefresh(requireContext())
            repository.clearCurrentWord()
            AppPreferences.clearNotificationRuntimeState(requireContext())
        }
    }

    private fun ensurePermissionStateOnEntry() {
        if (!isPushEnabled || runtimeSyncedThisView) {
            return
        }

        if (NotificationPermissionHelper.canPostNotifications(requireContext())) {
            syncPushRuntime(enabled = true)
            runtimeSyncedThisView = true
        }
    }

    private fun showPermissionConsentDialog() {
        AlertDialog.Builder(requireContext())
            .setTitle(R.string.title_notification_permission)
            .setMessage(R.string.permission_consent_message)
            .setPositiveButton(R.string.confirm) { _, _ ->
                pendingEnableAfterPermission = true
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
            .setNegativeButton(android.R.string.cancel) { _, _ ->
                pendingEnableAfterPermission = false
                persistPushEnabled(false)
                refreshPushStateFromPrefs()
                updateUI()
            }
            .show()
    }

    private fun refreshPushStateFromPrefs() {
        isPushEnabled = readPushEnabledFromPrefs()
        updateToggleUI()
    }

    private fun readPushEnabledFromPrefs(): Boolean {
        return prefs().getBoolean(KEY_NOTIFICATION_ENABLED, false)
    }

    private fun persistPushEnabled(enabled: Boolean) {
        prefs().edit().putBoolean(KEY_NOTIFICATION_ENABLED, enabled).apply()
    }

    private fun prefs() = requireContext().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    private fun updateToggleUI() {
        toggleThumb.animate().cancel()

        if (isPushEnabled) {
            toggleContainer.setBackgroundResource(R.drawable.toggle_track_on)
            toggleThumb.setBackgroundResource(R.drawable.toggle_thumb_on)
            toggleContainer.post {
                val maxDistance = (toggleContainer.width - toggleThumb.width).toFloat().coerceAtLeast(0f)
                toggleThumb.animate()
                    .translationX(maxDistance)
                    .setDuration(250)
                    .setInterpolator(DecelerateInterpolator())
                    .start()
            }
        } else {
            toggleContainer.setBackgroundResource(R.drawable.toggle_track_off)
            toggleThumb.setBackgroundResource(R.drawable.toggle_thumb)
            toggleThumb.animate()
                .translationX(0f)
                .setDuration(250)
                .setInterpolator(DecelerateInterpolator())
                .start()
        }
    }

    private fun updateUI() {
        lifecycleScope.launch {
            val notebookCount = repository.getNotebookCountRemoteFirst()
            val selectedLibraries = repository.getSelectedLibraries()
            val libraryName = getLibraryDisplayName(selectedLibraries)

            tvStatus.text = if (isPushEnabled) {
                getString(R.string.push_status_enabled)
            } else {
                getString(R.string.push_status_disabled)
            }
            tvLibraryInfo.text = getString(R.string.current_library_format, libraryName)
            tvNotebookInfo.text = getString(R.string.notebook_count_format, notebookCount)
        }
    }

    private fun getLibraryDisplayName(selectedLibraries: List<String>): String {
        return LibrarySelection.getDisplayName(selectedLibraries)
    }

    private fun showPermissionGuideDialog() {
        AlertDialog.Builder(requireContext())
            .setTitle(R.string.title_notification_permission)
            .setMessage(R.string.permission_message)
            .setPositiveButton(R.string.go_to_settings) { _, _ -> openNotificationSettings() }
            .setNegativeButton(android.R.string.cancel, null)
            .show()
    }

    private fun openNotificationSettings() {
        try {
            val intent = Intent().apply {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    action = Settings.ACTION_APP_NOTIFICATION_SETTINGS
                    putExtra(Settings.EXTRA_APP_PACKAGE, requireContext().packageName)
                } else {
                    action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
                    data = Uri.parse("package:${requireContext().packageName}")
                }
            }
            startActivity(intent)
        } catch (_: Exception) {
            Toast.makeText(requireContext(), getString(R.string.open_settings_failed), Toast.LENGTH_SHORT).show()
        }
    }
}

