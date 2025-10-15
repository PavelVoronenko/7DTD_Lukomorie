package com.antago30.a7dtd_lukomorie.fragments

import android.content.Context
import android.content.SharedPreferences
import android.content.res.ColorStateList
import android.os.Bundle
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
import com.antago30.a7dtd_lukomorie.permissions.ExactAlarmPermissionManager
import com.google.android.material.progressindicator.CircularProgressIndicator

class InfoFragment : BaseFragment() {

    private lateinit var statusText: TextView
    private lateinit var timeText: TextView
    private lateinit var dayText: TextView
    private lateinit var playersOnlineText: TextView
    private lateinit var nextBloodMoonText: TextView
    private lateinit var spinnerReminder: Spinner
    private lateinit var switchReminder: SwitchMaterial
    private lateinit var bloodMoonPrefs: SharedPreferences
    private lateinit var circularTimer: CircularProgressIndicator
    private lateinit var timerText: TextView

    private var bloodMoonTimer: BloodMoonProgressTimer? = null
    private var displayManager: BloodMoonDisplayManager? = null
    private lateinit var reminderManager: BloodMoonNotificationManager
    private lateinit var permissionManager: NotificationPermissionManager
    private lateinit var exactAlarmManager: ExactAlarmPermissionManager
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

        bloodMoonPrefs = requireContext().getSharedPreferences(
            BloodMoonNotificationManager.PREFS_NAME,
            Context.MODE_PRIVATE
        )

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
        circularTimer = view.findViewById(R.id.circular_timer)
        timerText = view.findViewById(R.id.timer_text)
    }

    private fun initReminderSystem() {
        reminderManager = BloodMoonNotificationManager(requireContext())
        permissionManager =
            NotificationPermissionManager(requireContext(), requestPermissionLauncher)
        exactAlarmManager = ExactAlarmPermissionManager(requireContext())
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
            handleSwitchToggle(isChecked, false)
        }
    }

    fun handleSwitchToggle(isChecked: Boolean, message: Boolean) {
        if (isChecked) {
            cachedNextBloodMoonDateTime?.let { nextBloodMoonTime ->
                val minutes = getSelectedMinutes()

                if (!exactAlarmManager.canScheduleExactAlarms()) {
                    if (message) Toast.makeText(
                        context,
                        "Разрешите точные будильники в настройках",
                        Toast.LENGTH_LONG
                    ).show()
                    exactAlarmManager.openExactAlarmSettings()
                    switchReminder.isChecked = false
                    return
                }

                permissionManager.requestPermissionIfNeeded {
                    reminderManager.scheduleReminder(
                        nextBloodMoonTime,
                        minutes,
                        onScheduled = {
                            switchReminder.thumbTintList =
                                ColorStateList.valueOf("#00FF00".toColorInt())
                            if (message) Toast.makeText(
                                context,
                                "Напоминание установлено",
                                Toast.LENGTH_SHORT
                            ).show()
                        },
                        onFailed = {
                            switchReminder.isChecked = false
                        }
                    )
                }
            } ?: run {
                if (message) Toast.makeText(
                    context,
                    "Нет данных о времени Кровавой Луны",
                    Toast.LENGTH_SHORT
                ).show()
                switchReminder.isChecked = false
            }
        } else {
            reminderManager.cancelReminder()
            switchReminder.thumbTintList = ColorStateList.valueOf("#FF0000".toColorInt())
            Toast.makeText(context, "Напоминание отключено", Toast.LENGTH_SHORT).show()
        }
    }

    private val prefsListener = SharedPreferences.OnSharedPreferenceChangeListener { _, key ->
        if (key == BloodMoonNotificationManager.KEY_IS_ACTIVE ||
            key == BloodMoonNotificationManager.KEY_SHOULD_DISABLE
        ) {
            restoreReminderState()
        }
    }

    private fun setupReminderAfterPermission() {
        pendingReminderData?.let { (nextBloodMoonTime, minutes) ->
            reminderManager.scheduleReminder(
                nextBloodMoonTime,
                minutes,
                onScheduled = {
                    Toast.makeText(context, "Напоминание установлено", Toast.LENGTH_SHORT).show()
                },
                onFailed = {
                    switchReminder.isChecked = false
                }
            )
            pendingReminderData = null
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        restoreReminderState()
    }

    override fun loadData(): Any {
        return try {
            webParser.parseInfo(Constants.FULL_DATA_URL)
        } catch (e: Exception) {
            //ServerInfo("Ошибка загрузки", "00:00", 7, 0, "22:00")
        }
    }

    override fun updateUI(data: Any) {
        if (data !is ServerInfo) {
            statusText.text = " Нет сети!"
            playersOnlineText.text = "--"
            timeText.text = "--:--:--"
            dayText.text = "---"
            nextBloodMoonText.text = "--.--.---- --:--"
            return
        }

        updateBasicInfo(data)
        calculateAndDisplayBloodMoonTime(data)
        startBloodMoonTimer(data)
        restoreReminderState()
    }

    private fun updateBasicInfo(data: ServerInfo) {
        if(data.status == "в сети.") {
            statusText.setTextColor(ContextCompat.getColor(requireContext(), R.color.green))
            statusText.text = " Online"
        } else {
            statusText.setTextColor(ContextCompat.getColor(requireContext(), R.color.red))
            statusText.text = " Offline"
        }

        timeText.text = data.time
        dayText.text = "${data.day}"
        dayText.setTextColor(
            if (data.day % 7 == 0)
                ContextCompat.getColor(requireContext(), R.color.red)
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

            // Если надо обновлять будильник с изменением времени кровавой луны на сервере
            /*if (switchReminder.isChecked) {
                val minutes = getSelectedMinutes()
                reminderManager.cancelReminder()
                reminderManager.scheduleReminder(nextBloodMoonDateTime, minutes) {}
            }*/
        } catch (e: Exception) {
            nextBloodMoonText.text = "Ошибка: Не удалось вычислить луну"
            nextBloodMoonText.visibility = View.VISIBLE
            cachedNextBloodMoonDateTime = null
        }
    }

    private fun startBloodMoonTimer(data: ServerInfo) {
        val nextBloodMoonDateTime = cachedNextBloodMoonDateTime ?: return
        val previousBloodMoonDateTime =
            nextBloodMoonDateTime.minusSeconds(7 * Constants.LENGTH_OF_DAY)
        var currentBloodMoonDateTime: LocalDateTime

        if (LocalDateTime.now() > previousBloodMoonDateTime.plusSeconds(BloodMoonDisplayManager.BLOOD_MOON_DURATION_HOURS * Constants.LENGTH_OF_DAY / 24)) {
            currentBloodMoonDateTime = nextBloodMoonDateTime
        } else {
            currentBloodMoonDateTime = previousBloodMoonDateTime
        }

        bloodMoonTimer?.stop()
        bloodMoonTimer = null

        if (data.playersOnline == 0) {
            displayManager = BloodMoonDisplayManager(
                circularTimer = requireView().findViewById(R.id.circular_timer),
                timerText = requireView().findViewById(R.id.timer_text),
                previousBloodMoon = previousBloodMoonDateTime,
                currentBloodMoon = currentBloodMoonDateTime,
                nextBloodMoon = nextBloodMoonDateTime
            )
            displayManager?.updateStaticState()
            return
        }

        displayManager = BloodMoonDisplayManager(
            circularTimer = circularTimer,
            timerText = timerText,
            previousBloodMoon = previousBloodMoonDateTime,
            currentBloodMoon = currentBloodMoonDateTime,
            nextBloodMoon = nextBloodMoonDateTime
        )

        bloodMoonTimer = displayManager?.startDynamicTimer(
            onTimerStop = { }
        )
    }

    private fun restoreSpinnerPosition() {
        val savedMinutes = reminderManager.getSavedReminderMinutes()
        val position = getPositionForMinutes(savedMinutes)
        spinnerReminder.setSelection(position)
    }

    private fun restoreReminderState() {
        if (!isAdded || view == null) return

        if (reminderManager.shouldAutoDisable()) {
            switchReminder.setOnCheckedChangeListener(null)
            switchReminder.isChecked = false
            switchReminder.thumbTintList =
                ColorStateList.valueOf(ContextCompat.getColor(requireContext(), R.color.red))
            switchReminder.setOnCheckedChangeListener { _, isChecked ->
                handleSwitchToggle(isChecked, true)
            }
            reminderManager.cancelReminder()
            reminderManager.clearAutoDisableFlag()
        } else if (reminderManager.isReminderActive()) {
            switchReminder.isChecked = true
            val savedMinutes = reminderManager.getSavedReminderMinutes()
            val position = getPositionForMinutes(savedMinutes)
            spinnerReminder.setSelection(position)
        } else {
            switchReminder.setOnCheckedChangeListener(null)
            switchReminder.isChecked = false
            switchReminder.thumbTintList =
                ColorStateList.valueOf(ContextCompat.getColor(requireContext(), R.color.red))
            switchReminder.setOnCheckedChangeListener { _, isChecked ->
                handleSwitchToggle(isChecked, true)
            }
        }
    }

    private fun createSpinnerListener() = object : AdapterView.OnItemSelectedListener {
        override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
            if (switchReminder.isChecked) {
                cachedNextBloodMoonDateTime?.let { nextBloodMoonTime ->
                    val minutes = parseMinutesFromSpinner(position)

                    reminderManager.scheduleReminder(
                        nextBloodMoonTime,
                        minutes,
                        onScheduled = {
                            switchReminder.thumbTintList =
                                ColorStateList.valueOf("#00FF00".toColorInt())
                            Toast.makeText(context, "Напоминание обновлено", Toast.LENGTH_SHORT)
                                .show()
                        },
                        onFailed = {
                            switchReminder.isChecked = false
                        }
                    )
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

    override fun onResume() {
        super.onResume()
        bloodMoonTimer?.start()
        bloodMoonPrefs.registerOnSharedPreferenceChangeListener(prefsListener)
    }

    override fun onPause() {
        super.onPause()
        bloodMoonPrefs.unregisterOnSharedPreferenceChangeListener(prefsListener)
        bloodMoonTimer?.stop()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        bloodMoonTimer?.stop()
    }
}