package com.antago30.a7dtd_lukomorie.fragments

import android.content.res.ColorStateList
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import com.antago30.a7dtd_lukomorie.R
import com.antago30.a7dtd_lukomorie.logic.BloodMoonCalculator
import com.antago30.a7dtd_lukomorie.logic.BloodMoonDisplayManager
import com.antago30.a7dtd_lukomorie.logic.timer.BloodMoonProgressTimer
import com.antago30.a7dtd_lukomorie.model.ServerInfo
import com.antago30.a7dtd_lukomorie.permissions.NotificationPermissionManager
import com.antago30.a7dtd_lukomorie.receivers.BloodMoonNotificationManager
import com.antago30.a7dtd_lukomorie.utils.Constants
import com.google.android.material.switchmaterial.SwitchMaterial
import java.time.LocalDateTime
import android.widget.Spinner
import androidx.core.graphics.toColorInt

class InfoFragment : BaseFragment() {

    private lateinit var statusText: TextView
    private lateinit var timeText: TextView
    private lateinit var dayText: TextView
    private lateinit var playersOnlineText: TextView
    private lateinit var nextBloodMoonText: TextView
    private lateinit var spinnerReminder: Spinner
    private lateinit var switchReminder: SwitchMaterial

    private var bloodMoonTimer: BloodMoonProgressTimer? = null
    private var displayManager: BloodMoonDisplayManager? = null
    private lateinit var reminderManager: BloodMoonNotificationManager
    private lateinit var permissionManager: NotificationPermissionManager
    private var pendingReminderData: Pair<LocalDateTime, Int>? = null

    private var cachedNextBloodMoonDateTime: LocalDateTime? = null

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        permissionManager.handlePermissionResult(
            isGranted = isGranted,
            onGranted = {
                Toast.makeText(context, "Уведомления разрешены", Toast.LENGTH_SHORT).show()
                setupReminderAfterPermission()
            },
            onDenied = {
                Toast.makeText(context, "Уведомления запрещены", Toast.LENGTH_LONG).show()
                switchReminder.isChecked = false
            }
        )
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_info, container, false)
        initViews(view)
        initReminderSystem()
        setupSpinner()
        setupSwitch()
        return view
    }

    private fun initViews(view: View) {
        statusText = view.findViewById(R.id.status_value)
        timeText = view.findViewById(R.id.time_value)
        dayText = view.findViewById(R.id.day_value)
        playersOnlineText = view.findViewById(R.id.players_value)
        nextBloodMoonText = view.findViewById(R.id.blood_moon_value)
        spinnerReminder = view.findViewById(R.id.spinner_reminder)
        switchReminder = view.findViewById(R.id.switch_reminder)
    }

    private fun initReminderSystem() {
        reminderManager = BloodMoonNotificationManager(requireContext())
        permissionManager = NotificationPermissionManager(requireContext(), requestPermissionLauncher)
    }

    private fun setupSpinner() {
        val adapter = ArrayAdapter.createFromResource(
            requireContext(),
            R.array.reminder_times,
            android.R.layout.simple_spinner_item
        )
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerReminder.adapter = adapter

        restoreSpinnerPosition()
        spinnerReminder.onItemSelectedListener = createSpinnerListener()
    }

    private fun setupSwitch() {
        switchReminder.trackTintList = ColorStateList.valueOf("#666666".toColorInt())
        switchReminder.setOnCheckedChangeListener { _, isChecked ->
            handleSwitchToggle(isChecked)
        }
    }

    private fun handleSwitchToggle(isChecked: Boolean) {
        switchReminder.thumbTintList = ColorStateList.valueOf(
            if (isChecked) "#00FF00".toColorInt() else "#FF0000".toColorInt()
        )

        if (isChecked) {
            cachedNextBloodMoonDateTime?.let { nextBloodMoonTime ->
                val minutes = getSelectedMinutes()

                permissionManager.requestPermissionIfNeeded {
                    reminderManager.scheduleReminder(nextBloodMoonTime, minutes) {
                        Toast.makeText(context, "Напоминание установлено!", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        } else {
            reminderManager.cancelReminder()
            Toast.makeText(context, "Напоминание отключено", Toast.LENGTH_SHORT).show()
        }
    }

    private fun setupReminderAfterPermission() {
        pendingReminderData?.let { (nextBloodMoonTime, minutes) ->
            reminderManager.scheduleReminder(nextBloodMoonTime, minutes) {
                Toast.makeText(context, "Напоминание установлено!", Toast.LENGTH_SHORT).show()
            }
            pendingReminderData = null
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        restoreReminderState()
    }

    override fun loadData(): Any {
        return try {
            webParser.parseInfo(Constants.INFO_URL)
        } catch (e: Exception) {
            Log.e("InfoFragment", "Ошибка загрузки данных", e)
            ServerInfo("Ошибка загрузки", "00:00", 0, 0, "22:00")
        }
    }

    override fun updateUI(data: Any) {
        if (data !is ServerInfo) {
            statusText.text = "Ошибка отображения"
            return
        }

        updateBasicInfo(data)
        calculateAndDisplayBloodMoonTime(data)
        startBloodMoonTimer(data)
    }

    private fun updateBasicInfo(data: ServerInfo) {
        statusText.text = if (data.status == "в сети.") "Online" else "Offline"
        timeText.text = data.time
        dayText.text = "${data.day}"
        dayText.setTextColor(
            if (data.day % 7 == 0)
                ContextCompat.getColor(requireContext(), android.R.color.holo_red_light)
            else
                ContextCompat.getColor(requireContext(), android.R.color.holo_orange_light)
        )
        playersOnlineText.text = "${data.playersOnline}"
    }

    private fun calculateAndDisplayBloodMoonTime(data: ServerInfo) {
        try {
            val calculator = BloodMoonCalculator()
            val nextBloodMoonDateTime = calculator.calculateNextBloodMoon(
                currentGameDay = data.day,
                currentGameTime = data.time
            )
            nextBloodMoonText.text = calculator.formatDateTime(nextBloodMoonDateTime)
            nextBloodMoonText.visibility = View.VISIBLE
            cachedNextBloodMoonDateTime = nextBloodMoonDateTime

            if (switchReminder.isChecked) {
                val minutes = getSelectedMinutes()
                reminderManager.cancelReminder()
                reminderManager.scheduleReminder(nextBloodMoonDateTime, minutes) {}
            }
        } catch (e: Exception) {
            Log.e("InfoFragment", "Ошибка вычисления луны", e)
            nextBloodMoonText.text = "Ошибка: Не удалось вычислить луну"
            nextBloodMoonText.visibility = View.VISIBLE
            cachedNextBloodMoonDateTime = null
        }
    }

    private fun startBloodMoonTimer(data: ServerInfo) {
        val nextBloodMoonDateTime = cachedNextBloodMoonDateTime ?: return
        val gameDayLengthSeconds = 8111L
        val previousBloodMoonDateTime = nextBloodMoonDateTime.minusSeconds(7 * gameDayLengthSeconds)

        displayManager = BloodMoonDisplayManager(
            circularTimer = requireView().findViewById(R.id.circular_timer),
            timerText = requireView().findViewById(R.id.timer_text),
            previousBloodMoon = previousBloodMoonDateTime,
            nextBloodMoon = nextBloodMoonDateTime
        )

        if (data.playersOnline == 0) {
            bloodMoonTimer?.stop()
            bloodMoonTimer = null
            displayManager?.updateStaticState()
        } else {
            bloodMoonTimer = displayManager?.startDynamicTimer(
                onTimerStop = { bloodMoonTimer?.stop() }
            )
        }
    }

    private fun restoreSpinnerPosition() {
        val savedMinutes = reminderManager.getSavedReminderMinutes()
        val position = getPositionForMinutes(savedMinutes)
        spinnerReminder.setSelection(position)
    }

    private fun restoreReminderState() {
        if (reminderManager.shouldAutoDisable()) {
            switchReminder.isChecked = false
            reminderManager.cancelReminder()
        } else if (reminderManager.isReminderActive()) {
            switchReminder.isChecked = true
            val savedMinutes = reminderManager.getSavedReminderMinutes()
            val position = getPositionForMinutes(savedMinutes)
            spinnerReminder.setSelection(position)
        }
    }

    private fun createSpinnerListener() = object : AdapterView.OnItemSelectedListener {
        override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
            if (switchReminder.isChecked) {
                cachedNextBloodMoonDateTime?.let { nextBloodMoonTime ->
                    val minutes = parseMinutesFromSpinner(position)
                    reminderManager.cancelReminder()
                    reminderManager.scheduleReminder(nextBloodMoonTime, minutes) {
                        Toast.makeText(context, "Напоминание обновлено!", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }

        override fun onNothingSelected(parent: AdapterView<*>) {}
    }

    private fun getSelectedMinutes(): Int {
        return parseMinutesFromSpinner(spinnerReminder.selectedItemPosition)
    }

    private fun parseMinutesFromSpinner(position: Int): Int {
        return when (position) {
            0 -> 15
            1 -> 30
            2 -> 60
            3 -> 120
            else -> 15
        }
    }

    private fun getPositionForMinutes(minutes: Int): Int {
        return when (minutes) {
            15 -> 0
            30 -> 1
            60 -> 2
            120 -> 3
            else -> 0
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        bloodMoonTimer?.stop()
    }
}