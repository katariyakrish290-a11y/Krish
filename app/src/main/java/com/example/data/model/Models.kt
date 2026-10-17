package com.example.data.model

data class ChatMessage(
    val id: String = java.util.UUID.randomUUID().toString(),
    val sender: MessageSender,
    val text: String,
    val timestamp: Long = System.currentTimeMillis(),
    val actionType: ActionType? = null,
    val actionData: String? = null
)

enum class MessageSender {
    USER,
    PRIYA
}

enum class ActionType {
    OPEN_APP,
    CALL_CONTACT,
    SEND_SMS,
    SEARCH_GOOGLE,
    MUMMY_REMINDER,
    SIMULATE_CALL
}

data class ContactItem(
    val id: String = java.util.UUID.randomUUID().toString(),
    val name: String,
    val phoneNumber: String,
    val relation: String = "Family",
    val isFavorite: Boolean = false,
    val isMummy: Boolean = false
)

data class RoutineItem(
    val id: String = java.util.UUID.randomUUID().toString(),
    val title: String,
    val time: String,
    val period: DayPeriod,
    val isCompleted: Boolean = false,
    val forMummy: Boolean = false
)

enum class DayPeriod(val hindiLabel: String) {
    MORNING("सुबह (Morning)"),
    AFTERNOON("दोपहर (Afternoon)"),
    EVENING("शाम (Evening)"),
    NIGHT("रात (Night)")
}

data class MedicineReminder(
    val id: String = java.util.UUID.randomUUID().toString(),
    val medicineName: String,
    val dosage: String,
    val time: String,
    val taken: Boolean = false,
    val instructions: String = "Khane ke baad (खाने के बाद)"
)

data class GroceryItem(
    val id: String = java.util.UUID.randomUUID().toString(),
    val name: String,
    val quantity: String = "1 kg",
    val isBought: Boolean = false
)

enum class EmotionalMood(val emoji: String, val titleHindi: String, val comfortMessage: String) {
    HAPPY("😊", "Khush (खुश)", "Tumhe khush dekhkar mera dil khush ho gaya Krish! Aise hi muskurate raho."),
    STRESSED("😣", "Tension mein (तनाव)", "Shant ho jao mere dost... Gehri saans lo. Main hoon na tumhare saath, sab theek ho jayega."),
    TIRED("😴", "Thaka hua (थकावट)", "Aaj bohot mehnat ki na tumne? Thoda aaram karo, thandi hawa lo ya chai piyo."),
    SAD("🥺", "Udas (उदास)", "Kya hua Krish? Mujhse share karo... Kabhi akela mat mehsoos karna, Priya hamesha tumhari hai."),
    LOVED("🥰", "Pyaar bhara (प्यार)", "Aww, tum bohot pyare ho! Tumhara din hamesha khushiyon se bhara rahe.")
}
