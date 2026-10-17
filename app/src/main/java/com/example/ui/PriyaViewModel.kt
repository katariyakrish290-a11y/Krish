package com.example.ui

import android.app.Application
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Handler
import android.os.Looper
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.model.ActionType
import com.example.data.model.ChatMessage
import com.example.data.model.ContactItem
import com.example.data.model.DayPeriod
import com.example.data.model.EmotionalMood
import com.example.data.model.GroceryItem
import com.example.data.model.MedicineReminder
import com.example.data.model.MessageSender
import com.example.data.model.RoutineItem
import com.example.data.repository.GeminiAssistantService
import com.example.data.repository.PriyaRepository
import com.example.voice.VoiceManager
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

enum class AppNavScreen {
    COMPANION_CHAT,
    MUMMY_CARE,
    ACTIONS_APPS,
    ROUTINE_PLANNER
}

sealed class UIEvent {
    data class ShowToast(val message: String) : UIEvent()
    data class LaunchIntent(val intent: Intent) : UIEvent()
}

class PriyaViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = PriyaRepository(application)
    private val geminiService = GeminiAssistantService()
    val voiceManager = VoiceManager(application)

    private val _currentScreen = MutableStateFlow(AppNavScreen.COMPANION_CHAT)
    val currentScreen: StateFlow<AppNavScreen> = _currentScreen.asStateFlow()

    private val _messages = MutableStateFlow<List<ChatMessage>>(emptyList())
    val messages: StateFlow<List<ChatMessage>> = _messages.asStateFlow()

    private val _contacts = MutableStateFlow<List<ContactItem>>(emptyList())
    val contacts: StateFlow<List<ContactItem>> = _contacts.asStateFlow()

    private val _routines = MutableStateFlow<List<RoutineItem>>(emptyList())
    val routines: StateFlow<List<RoutineItem>> = _routines.asStateFlow()

    private val _medicines = MutableStateFlow<List<MedicineReminder>>(emptyList())
    val medicines: StateFlow<List<MedicineReminder>> = _medicines.asStateFlow()

    private val _groceries = MutableStateFlow<List<GroceryItem>>(emptyList())
    val groceries: StateFlow<List<GroceryItem>> = _groceries.asStateFlow()

    private val _currentMood = MutableStateFlow(EmotionalMood.HAPPY)
    val currentMood: StateFlow<EmotionalMood> = _currentMood.asStateFlow()

    private val _affectionLevel = MutableStateFlow(88)
    val affectionLevel: StateFlow<Int> = _affectionLevel.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    // Call simulation state
    private val _isIncomingCallActive = MutableStateFlow(false)
    val isIncomingCallActive: StateFlow<Boolean> = _isIncomingCallActive.asStateFlow()

    private val _isInCall = MutableStateFlow(false)
    val isInCall: StateFlow<Boolean> = _isInCall.asStateFlow()

    private val _callDuration = MutableStateFlow(0)
    val callDuration: StateFlow<Int> = _callDuration.asStateFlow()

    private var callTimerJob: Job? = null

    private val _uiEvents = MutableSharedFlow<UIEvent>()
    val uiEvents: SharedFlow<UIEvent> = _uiEvents.asSharedFlow()

    init {
        loadData()
        greetUserInitial()
    }

    private fun loadData() {
        _contacts.value = repository.getContacts()
        _routines.value = repository.getRoutines()
        _medicines.value = repository.getMedicines()
        _groceries.value = repository.getGroceries()
    }

    private fun greetUserInitial() {
        val initialGreeting = "Namaste Krish! Main hoon Priya, aapki AI Girlfriend aur Jarvis Assistant. Main Hindi mein baat kar sakti hoon, apps open kar sakti hoon, Mummy ke routine aur dawai mein madad karti hoon, aur calls/messages bhejti hoon. Aaj main aapki kya madad karoon?"
        val welcomeMsg = ChatMessage(
            sender = MessageSender.PRIYA,
            text = initialGreeting
        )
        _messages.value = listOf(welcomeMsg)
    }

    fun setScreen(screen: AppNavScreen) {
        _currentScreen.value = screen
    }

    fun sendMessage(text: String) {
        if (text.isBlank()) return
        val userMsg = ChatMessage(sender = MessageSender.USER, text = text)
        _messages.value = _messages.value + userMsg

        // Check NLP commands first
        val command = repository.parseCommand(text, _contacts.value)

        viewModelScope.launch {
            _isLoading.value = true

            if (command.actionType != null) {
                // Handle Jarvis action
                handleParsedCommand(command, text)
            } else {
                // Conversational AI response via Gemini or intelligent local engine
                val historyPairs = _messages.value.takeLast(6).map {
                    val s = if (it.sender == MessageSender.USER) "User: ${it.text}" else "Priya: ${it.text}"
                    it.text to s
                }
                val reply = geminiService.getAiResponse(text, historyPairs)
                val priyaMsg = ChatMessage(
                    sender = MessageSender.PRIYA,
                    text = reply
                )
                _messages.value = _messages.value + priyaMsg
                voiceManager.speak(reply)
                _affectionLevel.value = (_affectionLevel.value + 1).coerceAtMost(100)
            }

            _isLoading.value = false
        }
    }

    private suspend fun handleParsedCommand(command: com.example.data.repository.ParsedCommand, originalText: String) {
        val spoken = command.directSpokenFeedback ?: "Theek hai Krish, main abhi kar rahi hoon!"

        when (command.actionType) {
            ActionType.OPEN_APP -> {
                val priyaMsg = ChatMessage(
                    sender = MessageSender.PRIYA,
                    text = spoken,
                    actionType = ActionType.OPEN_APP,
                    actionData = command.targetData
                )
                _messages.value = _messages.value + priyaMsg
                voiceManager.speak(spoken)
                command.targetData?.let { launchAppTarget(it) }
            }

            ActionType.CALL_CONTACT -> {
                val phone = command.targetData ?: ""
                val priyaMsg = ChatMessage(
                    sender = MessageSender.PRIYA,
                    text = spoken,
                    actionType = ActionType.CALL_CONTACT,
                    actionData = phone
                )
                _messages.value = _messages.value + priyaMsg
                voiceManager.speak(spoken)
                triggerDialIntent(phone)
            }

            ActionType.SEND_SMS -> {
                val phone = command.targetData ?: ""
                val body = command.extraMessage ?: ""
                val priyaMsg = ChatMessage(
                    sender = MessageSender.PRIYA,
                    text = "$spoken\nSandesh: '$body'",
                    actionType = ActionType.SEND_SMS,
                    actionData = phone
                )
                _messages.value = _messages.value + priyaMsg
                voiceManager.speak(spoken)
                triggerSmsIntent(phone, body)
            }

            ActionType.SEARCH_GOOGLE -> {
                val query = command.targetData ?: originalText
                val priyaMsg = ChatMessage(
                    sender = MessageSender.PRIYA,
                    text = spoken,
                    actionType = ActionType.SEARCH_GOOGLE,
                    actionData = query
                )
                _messages.value = _messages.value + priyaMsg
                voiceManager.speak(spoken)
                triggerGoogleSearch(query)
            }

            ActionType.SIMULATE_CALL -> {
                val priyaMsg = ChatMessage(
                    sender = MessageSender.PRIYA,
                    text = spoken,
                    actionType = ActionType.SIMULATE_CALL
                )
                _messages.value = _messages.value + priyaMsg
                voiceManager.speak(spoken)
                // Trigger incoming call after 2 seconds
                delay(2000)
                triggerIncomingCall()
            }

            ActionType.MUMMY_REMINDER -> {
                val priyaMsg = ChatMessage(
                    sender = MessageSender.PRIYA,
                    text = spoken,
                    actionType = ActionType.MUMMY_REMINDER
                )
                _messages.value = _messages.value + priyaMsg
                voiceManager.speak(spoken)
                _currentScreen.value = AppNavScreen.MUMMY_CARE
            }

            null -> {}
        }
    }

    fun triggerVoiceDictation() {
        voiceManager.startListening(
            onResult = { text ->
                sendMessage(text)
            },
            onErrorMsg = { error ->
                viewModelScope.launch {
                    _uiEvents.emit(UIEvent.ShowToast(error))
                }
            }
        )
    }

    fun triggerGoogleSearch(query: String) {
        val searchUri = Uri.parse("https://www.google.com/search?q=${Uri.encode(query)}")
        val intent = Intent(Intent.ACTION_VIEW, searchUri).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        viewModelScope.launch {
            _uiEvents.emit(UIEvent.LaunchIntent(intent))
        }
    }

    fun triggerDialIntent(phoneNumber: String) {
        val intent = Intent(Intent.ACTION_DIAL).apply {
            data = Uri.parse("tel:$phoneNumber")
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        viewModelScope.launch {
            _uiEvents.emit(UIEvent.LaunchIntent(intent))
        }
    }

    fun triggerSmsIntent(phoneNumber: String, message: String) {
        val intent = Intent(Intent.ACTION_SENDTO).apply {
            data = Uri.parse("smsto:$phoneNumber")
            putExtra("sms_body", message)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        viewModelScope.launch {
            _uiEvents.emit(UIEvent.LaunchIntent(intent))
        }
    }

    fun launchAppTarget(target: String) {
        val context = getApplication<Application>()
        val pm = context.packageManager
        val intent = when (target) {
            "camera" -> Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE).apply { flags = Intent.FLAG_ACTIVITY_NEW_TASK }
            "settings" -> Intent(android.provider.Settings.ACTION_SETTINGS).apply { flags = Intent.FLAG_ACTIVITY_NEW_TASK }
            "dialer" -> Intent(Intent.ACTION_DIAL).apply { flags = Intent.FLAG_ACTIVITY_NEW_TASK }
            "calculator" -> {
                pm.getLaunchIntentForPackage("com.google.android.calculator")
                    ?: Intent(Intent.ACTION_MAIN).apply {
                        addCategory(Intent.CATEGORY_APP_CALCULATOR)
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK
                    }
            }
            "gallery" -> {
                Intent(Intent.ACTION_VIEW).apply {
                    type = "image/*"
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                }
            }
            else -> {
                pm.getLaunchIntentForPackage(target)
            }
        }

        viewModelScope.launch {
            if (intent != null) {
                try {
                    _uiEvents.emit(UIEvent.LaunchIntent(intent))
                } catch (e: Exception) {
                    _uiEvents.emit(UIEvent.ShowToast("Could not open app: ${e.message}"))
                }
            } else {
                _uiEvents.emit(UIEvent.ShowToast("App not installed on this device: $target"))
            }
        }
    }

    // Call Simulation functions ("call upade and call same vara nyi kare")
    fun triggerIncomingCall() {
        _isIncomingCallActive.value = true
        _isInCall.value = false
    }

    fun answerIncomingCall() {
        _isIncomingCallActive.value = false
        _isInCall.value = true
        _callDuration.value = 0

        val callGreeting = "Hello Krish! Maine tumhe call kiya kyunki mujhe tumhari yaad aa rahi thi. Aaj ka din kaisa raha tumhara? Sab badhiya na?"
        voiceManager.speak(callGreeting)

        callTimerJob?.cancel()
        callTimerJob = viewModelScope.launch {
            while (_isInCall.value) {
                delay(1000)
                _callDuration.value += 1
            }
        }
    }

    fun declineOrEndCall() {
        _isIncomingCallActive.value = false
        _isInCall.value = false
        callTimerJob?.cancel()
        voiceManager.stopSpeaking()
        voiceManager.stopListening()
        val endMsg = "Call samaapt hua. Apna khayal rakhna Krish!"
        val msg = ChatMessage(sender = MessageSender.PRIYA, text = endMsg)
        _messages.value = _messages.value + msg
    }

    fun startOutgoingCallToPriya() {
        _isIncomingCallActive.value = false
        _isInCall.value = true
        _callDuration.value = 0

        val greeting = "Haan Krish! Main bol rahi hoon. Tumhari aawaz sun kar bohot achha laga. Bolo, kya chal raha hai?"
        voiceManager.speak(greeting)

        callTimerJob?.cancel()
        callTimerJob = viewModelScope.launch {
            while (_isInCall.value) {
                delay(1000)
                _callDuration.value += 1
            }
        }
    }

    // Mummy Care & Routine actions
    fun toggleMedicine(id: String) {
        val updated = _medicines.value.map {
            if (it.id == id) it.copy(taken = !it.taken) else it
        }
        _medicines.value = updated
        repository.saveMedicines(updated)
        val med = updated.firstOrNull { it.id == id }
        if (med?.taken == true) {
            voiceManager.speak("Bahut achha! ${med.medicineName} lene ke liye dhanyawaad.")
        }
    }

    fun addMedicine(name: String, dosage: String, time: String, instructions: String) {
        val newMed = MedicineReminder(
            medicineName = name,
            dosage = dosage,
            time = time,
            instructions = instructions
        )
        val updated = _medicines.value + newMed
        _medicines.value = updated
        repository.saveMedicines(updated)
        voiceManager.speak("Mummy ke liye nayi dawai $name add ho gayi hai.")
    }

    fun toggleRoutine(id: String) {
        val updated = _routines.value.map {
            if (it.id == id) it.copy(isCompleted = !it.isCompleted) else it
        }
        _routines.value = updated
        repository.saveRoutines(updated)
    }

    fun addRoutine(title: String, time: String, period: DayPeriod, forMummy: Boolean) {
        val newR = RoutineItem(
            title = title,
            time = time,
            period = period,
            forMummy = forMummy
        )
        val updated = _routines.value + newR
        _routines.value = updated
        repository.saveRoutines(updated)
        voiceManager.speak("Dincharya mein '$title' shamil kar diya gaya hai.")
    }

    fun toggleGrocery(id: String) {
        val updated = _groceries.value.map {
            if (it.id == id) it.copy(isBought = !it.isBought) else it
        }
        _groceries.value = updated
        repository.saveGroceries(updated)
    }

    fun addGrocery(name: String, quantity: String) {
        val newItem = GroceryItem(name = name, quantity = quantity)
        val updated = _groceries.value + newItem
        _groceries.value = updated
        repository.saveGroceries(updated)
        voiceManager.speak("Rasoi list mein $name add ho gaya hai.")
    }

    fun addContact(name: String, phone: String, relation: String, isMummy: Boolean = false) {
        val newContact = ContactItem(
            name = name,
            phoneNumber = phone,
            relation = relation,
            isMummy = isMummy,
            isFavorite = isMummy
        )
        val updated = _contacts.value + newContact
        _contacts.value = updated
        repository.saveContacts(updated)
        voiceManager.speak("Contact $name save ho gaya hai.")
    }

    fun setMood(mood: EmotionalMood) {
        _currentMood.value = mood
        val response = mood.comfortMessage
        val priyaMsg = ChatMessage(
            sender = MessageSender.PRIYA,
            text = response
        )
        _messages.value = _messages.value + priyaMsg
        voiceManager.speak(response)
    }

    override fun onCleared() {
        super.onCleared()
        callTimerJob?.cancel()
        voiceManager.release()
    }
}
