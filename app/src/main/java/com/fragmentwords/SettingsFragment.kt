package com.fragmentwords

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Switch
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

class SettingsFragment : Fragment() {

    private lateinit var repository: WordRepository
    private lateinit var switchPush: Switch
    private lateinit var tvCurrentLibrary: TextView
    private var suppressSwitchCallback = false
    private var pendingEnableAfterPermission = false

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            if (pendingEnableAfterPermission) {
                if (NotificationPermissionHelper.canPostNotifications(requireContext())) {
                    applyPushEnabled(true)
                } else {
                    pendingEnableAfterPermission = false
                    applySwitchState(false)
                    AppPreferences.setNotificationEnabled(requireContext(), false)
                    showPermissionGuideDialog()
                }
            }
        } else {
            pendingEnableAfterPermission = false
            applySwitchState(false)
            AppPreferences.setNotificationEnabled(requireContext(), false)
            showPermissionGuideDialog()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_settings, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        repository = WordRepository(requireContext())
        initViews(view)
        setupClickListeners(view)
        loadSettings()
    }

    override fun onResume() {
        super.onResume()
        loadSettings()
    }

    private fun initViews(view: View) {
        switchPush = view.findViewById(R.id.switch_push)
        tvCurrentLibrary = view.findViewById(R.id.tv_current_library)
    }

    private fun setupClickListeners(view: View) {
        switchPush.setOnCheckedChangeListener { _, isChecked ->
            if (suppressSwitchCallback) {
                return@setOnCheckedChangeListener
            }
            onPushToggleChanged(isChecked)
        }

        view.findViewById<View>(R.id.btn_manage_libraries).setOnClickListener {
            startActivity(Intent(requireContext(), LibrarySelectActivity::class.java))
        }

        view.findViewById<View>(R.id.btn_about).setOnClickListener {
            showAboutDialog()
        }
    }

    private fun loadSettings() {
        applySwitchState(AppPreferences.isNotificationEnabled(requireContext()))
        lifecycleScope.launch {
            repository.initializeIfNeeded()
            val selectedLibraries = repository.getSelectedLibraries()
            tvCurrentLibrary.text = LibrarySelection.getDisplayName(selectedLibraries)
        }
    }

    private fun onPushToggleChanged(enabled: Boolean) {
        if (enabled) {
            requestEnablePush()
        } else {
            applyPushEnabled(false)
        }
    }

    private fun requestEnablePush() {
        if (NotificationPermissionHelper.canPostNotifications(requireContext())) {
            applyPushEnabled(true)
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

    private fun applyPushEnabled(enabled: Boolean) {
        pendingEnableAfterPermission = false
        AppPreferences.setNotificationEnabled(requireContext(), enabled)

        if (enabled) {
            if (!NotificationPermissionHelper.canPostNotifications(requireContext())) {
                applySwitchState(false)
                return
            }
            WordService.startService(requireContext())
            AlarmScheduler.schedulePeriodicAlarm(requireContext())
            WorkManagerScheduler.cancelRefresh(requireContext())
            Toast.makeText(requireContext(), getString(R.string.push_enabled_toast), Toast.LENGTH_SHORT).show()
        } else {
            WordService.stopService(requireContext())
            AlarmScheduler.cancelAlarms(requireContext())
            WorkManagerScheduler.cancelRefresh(requireContext())
            repository.clearCurrentWord()
            AppPreferences.clearNotificationRuntimeState(requireContext())
            Toast.makeText(requireContext(), getString(R.string.push_disabled_toast), Toast.LENGTH_SHORT).show()
        }

        applySwitchState(enabled)
    }

    private fun applySwitchState(enabled: Boolean) {
        suppressSwitchCallback = true
        switchPush.isChecked = enabled
        suppressSwitchCallback = false
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
                applySwitchState(false)
                AppPreferences.setNotificationEnabled(requireContext(), false)
            }
            .show()
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

    private fun showAboutDialog() {
        AlertDialog.Builder(requireContext())
            .setTitle(R.string.title_about_app)
            .setMessage(R.string.about_app_message)
            .setPositiveButton(R.string.confirm, null)
            .show()
    }
}
