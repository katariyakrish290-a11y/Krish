package com.example.data.repository

import android.content.Context
import android.content.SharedPreferences
import com.example.data.model.*
import org.json.JSONArray
import org.json.JSONObject

data class ParsedCommand(
    val actionType: ActionType?,
    val targetData: String?,
    val extraMessage: String? = null,
    val directSpokenFeedback: String? = null
)

class PriyaRepository(private val context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences("priya_ai_prefs", Context.MODE_PRIVATE)

    // Initial default contacts
    private val defaultContacts = listOf(
        ContactItem(name = "Mummy (मम्मी)", phoneNumber = "+919876543210", relation = "Mother", isFavorite = true, isMummy = true),
        ContactItem(name = "Papa (पापा)", phoneNumber = "+919876543211", relation = "Father", isFavorite = true),
        ContactItem(name = "Bhai (भाई)", phoneNumber = "+919876543212", relation = "Brother"),
        ContactItem(name = "Doctor Verma", phoneNumber = "+919876543213", relation = "Doctor"),
        ContactItem(name = "Rahul (Dost)", phoneNumber = "+919876543214", relation = "Friend")
    )

    // Initial default daily routine
    private val defaultRoutines = listOf(
        RoutineItem(title = "सुबह पूजा एवं भजन (Morning Puja & Aarti)", time = "06:30 AM", period = DayPeriod.MORNING, forMummy = true),
        RoutineItem(title = "प्रातः भ्रमण एवं वॉक (Morning Walk)", time = "07:15 AM", period = DayPeriod.MORNING, forMummy = true),
        RoutineItem(title = "चाय व नाश्ता (Morning Tea & Breakfast)", time = "08:30 AM", period = DayPeriod.MORNING, forMummy = true),
        RoutineItem(title = "दोपहर का विश्राम (Afternoon Rest)", time = "02:00 PM", period = DayPeriod.AFTERNOON, forMummy = true),
        RoutineItem(title = "शाम की चाय और वॉक (Evening Tea)", time = "05:30 PM", period = DayPeriod.EVENING, forMummy = true),
        RoutineItem(title = "रात का हल्का भोजन (Light Dinner)", time = "08:30 PM", period = DayPeriod.NIGHT, forMummy = true)
    )

    // Initial default medicine reminders for Mummy
    private val defaultMedicines = listOf(
        MedicineReminder(medicineName = "BP Tablet (Amlodipine)", dosage = "1 Tablet", time = "08:00 AM", instructions = "नाश्ते के बाद (After Breakfast)"),
        MedicineReminder(medicineName = "Multivitamin & Calcium", dosage = "1 Capsule", time = "01:30 PM", instructions = "दोपहर खाने के बाद (After Lunch)"),
        MedicineReminder(medicineName = "Sugar Tablet (Metformin)", dosage = "1 Tablet", time = "08:30 PM", instructions = "रात के खाने से पहले (Before Dinner)")
    )

    // Initial kitchen groceries
    private val defaultGroceries = listOf(
        GroceryItem(name = "ताजा दूध (Fresh Milk)", quantity = "2 Litres", isBought = true),
        GroceryItem(name = "आटा (Wheat Flour)", quantity = "5 kg", isBought = false),
        GroceryItem(name = "सरसों तेल (Mustard Oil)", quantity = "1 Litre", isBought = false),
        GroceryItem(name = "हरी सब्जियां (Fresh Veggies)", quantity = "2 kg", isBought = false),
        GroceryItem(name = "चाय पत्ती व अदरक (Tea & Ginger)", quantity = "500 gm", isBought = false)
    )

    fun getContacts(): MutableList<ContactItem> {
        val jsonStr = prefs.getString("contacts_json", null) ?: return defaultContacts.toMutableList()
        return try {
            val array = JSONArray(jsonStr)
            val list = mutableListOf<ContactItem>()
            for (i in 0 until array.length()) {
                val obj = array.getJSONObject(i)
                list.add(
                    ContactItem(
                        id = obj.getString("id"),
                        name = obj.getString("name"),
                        phoneNumber = obj.getString("phoneNumber"),
                        relation = obj.optString("relation", "Family"),
                        isFavorite = obj.optBoolean("isFavorite", false),
                        isMummy = obj.optBoolean("isMummy", false)
                    )
                )
            }
            if (list.isEmpty()) defaultContacts.toMutableList() else list
        } catch (e: Exception) {
            defaultContacts.toMutableList()
        }
    }

    fun saveContacts(contacts: List<ContactItem>) {
        val array = JSONArray()
        contacts.forEach {
            val obj = JSONObject().apply {
                put("id", it.id)
                put("name", it.name)
                put("phoneNumber", it.phoneNumber)
                put("relation", it.relation)
                put("isFavorite", it.isFavorite)
                put("isMummy", it.isMummy)
            }
            array.put(obj)
        }
        prefs.edit().putString("contacts_json", array.toString()).apply()
    }

    fun getRoutines(): MutableList<RoutineItem> {
        val jsonStr = prefs.getString("routines_json", null) ?: return defaultRoutines.toMutableList()
        return try {
            val array = JSONArray(jsonStr)
            val list = mutableListOf<RoutineItem>()
            for (i in 0 until array.length()) {
                val obj = array.getJSONObject(i)
                val periodName = obj.optString("period", "MORNING")
                val period = try { DayPeriod.valueOf(periodName) } catch (e: Exception) { DayPeriod.MORNING }
                list.add(
                    RoutineItem(
                        id = obj.getString("id"),
                        title = obj.getString("title"),
                        time = obj.getString("time"),
                        period = period,
                        isCompleted = obj.optBoolean("isCompleted", false),
                        forMummy = obj.optBoolean("forMummy", false)
                    )
                )
            }
            if (list.isEmpty()) defaultRoutines.toMutableList() else list
        } catch (e: Exception) {
            defaultRoutines.toMutableList()
        }
    }

    fun saveRoutines(routines: List<RoutineItem>) {
        val array = JSONArray()
        routines.forEach {
            val obj = JSONObject().apply {
                put("id", it.id)
                put("title", it.title)
                put("time", it.time)
                put("period", it.period.name)
                put("isCompleted", it.isCompleted)
                put("forMummy", it.forMummy)
            }
            array.put(obj)
        }
        prefs.edit().putString("routines_json", array.toString()).apply()
    }

    fun getMedicines(): MutableList<MedicineReminder> {
        val jsonStr = prefs.getString("medicines_json", null) ?: return defaultMedicines.toMutableList()
        return try {
            val array = JSONArray(jsonStr)
            val list = mutableListOf<MedicineReminder>()
            for (i in 0 until array.length()) {
                val obj = array.getJSONObject(i)
                list.add(
                    MedicineReminder(
                        id = obj.getString("id"),
                        medicineName = obj.getString("medicineName"),
                        dosage = obj.getString("dosage"),
                        time = obj.getString("time"),
                        taken = obj.optBoolean("taken", false),
                        instructions = obj.optString("instructions", "खाने के बाद")
                    )
                )
            }
            if (list.isEmpty()) defaultMedicines.toMutableList() else list
        } catch (e: Exception) {
            defaultMedicines.toMutableList()
        }
    }

    fun saveMedicines(medicines: List<MedicineReminder>) {
        val array = JSONArray()
        medicines.forEach {
            val obj = JSONObject().apply {
                put("id", it.id)
                put("medicineName", it.medicineName)
                put("dosage", it.dosage)
                put("time", it.time)
                put("taken", it.taken)
                put("instructions", it.instructions)
            }
            array.put(obj)
        }
        prefs.edit().putString("medicines_json", array.toString()).apply()
    }

    fun getGroceries(): MutableList<GroceryItem> {
        val jsonStr = prefs.getString("groceries_json", null) ?: return defaultGroceries.toMutableList()
        return try {
            val array = JSONArray(jsonStr)
            val list = mutableListOf<GroceryItem>()
            for (i in 0 until array.length()) {
                val obj = array.getJSONObject(i)
                list.add(
                    GroceryItem(
                        id = obj.getString("id"),
                        name = obj.getString("name"),
                        quantity = obj.getString("quantity"),
                        isBought = obj.optBoolean("isBought", false)
                    )
                )
            }
            if (list.isEmpty()) defaultGroceries.toMutableList() else list
        } catch (e: Exception) {
            defaultGroceries.toMutableList()
        }
    }

    fun saveGroceries(groceries: List<GroceryItem>) {
        val array = JSONArray()
        groceries.forEach {
            val obj = JSONObject().apply {
                put("id", it.id)
                put("name", it.name)
                put("quantity", it.quantity)
                put("isBought", it.isBought)
            }
            array.put(obj)
        }
        prefs.edit().putString("groceries_json", array.toString()).apply()
    }

    // Natural Language Processing parser for commands
    fun parseCommand(input: String, contacts: List<ContactItem>): ParsedCommand {
        val lower = input.lowercase().trim()

        // 1. App Opening: "open [app]", "[app] kholo", "[app] chalao", "start [app]"
        val appMatches = listOf(
            "whatsapp" to "com.whatsapp",
            "youtube" to "com.google.android.youtube",
            "camera" to "camera",
            "chrome" to "com.android.chrome",
            "google chrome" to "com.android.chrome",
            "maps" to "com.google.android.apps.maps",
            "google maps" to "com.google.android.apps.maps",
            "settings" to "settings",
            "instagram" to "com.instagram.android",
            "dialer" to "dialer",
            "phone" to "dialer",
            "gallery" to "gallery",
            "calculator" to "calculator"
        )

        for ((appName, target) in appMatches) {
            if ((lower.contains("open $appName") || lower.contains("$appName kholo") ||
                 lower.contains("$appName open") || lower.contains("$appName chalao") ||
                 lower.contains("chalu karo $appName"))
            ) {
                return ParsedCommand(
                    actionType = ActionType.OPEN_APP,
                    targetData = target,
                    directSpokenFeedback = "Ji Krish, main $appName open kar rahi hoon!"
                )
            }
        }

        // 2. Call Command: "call [person]", "[person] ko call karo/lagao", "phone lagao [person]"
        if (lower.startsWith("call ") || lower.contains(" call karo") || lower.contains(" call lagao") ||
            lower.contains(" phone lagao") || lower.contains(" phone karo")
        ) {
            // Check for Mummy specifically
            if (lower.contains("mummy") || lower.contains("mum") || lower.contains("maa") || lower.contains("mata")) {
                val mummyContact = contacts.firstOrNull { it.isMummy } ?: contacts.firstOrNull()
                val phone = mummyContact?.phoneNumber ?: "+919876543210"
                return ParsedCommand(
                    actionType = ActionType.CALL_CONTACT,
                    targetData = phone,
                    directSpokenFeedback = "Theek hai Krish, main Mummy ko call laga rahi hoon!"
                )
            }

            // Find matching contact by name
            for (contact in contacts) {
                val cName = contact.name.lowercase()
                if (lower.contains(cName) || cName.split(" ").any { lower.contains(it) && it.length > 2 }) {
                    return ParsedCommand(
                        actionType = ActionType.CALL_CONTACT,
                        targetData = contact.phoneNumber,
                        directSpokenFeedback = "Main ${contact.name} ko call connect kar rahi hoon!"
                    )
                }
            }

            // If phone number is directly provided in digits
            val digits = lower.filter { it.isDigit() }
            if (digits.length >= 7) {
                return ParsedCommand(
                    actionType = ActionType.CALL_CONTACT,
                    targetData = digits,
                    directSpokenFeedback = "Main $digits par call laga rahi hoon!"
                )
            }
        }

        // 3. Message / SMS command: "message [person] [text]", "sms [person] [text]"
        if (lower.contains("message") || lower.contains("sms") || lower.contains("massage") || lower.contains("sandesh")) {
            val targetContact = when {
                lower.contains("mummy") || lower.contains("maa") -> contacts.firstOrNull { it.isMummy }
                lower.contains("papa") -> contacts.firstOrNull { it.relation.equals("father", true) }
                else -> contacts.firstOrNull { lower.contains(it.name.lowercase()) }
            } ?: contacts.firstOrNull { it.isMummy } ?: contacts.firstOrNull()

            val phone = targetContact?.phoneNumber ?: "+919876543210"
            // Extract message body if possible
            val body = if (input.contains(":")) {
                input.substringAfter(":").trim()
            } else {
                "Hello, Priya AI ke madhyam se bheja gaya sandesh."
            }

            return ParsedCommand(
                actionType = ActionType.SEND_SMS,
                targetData = phone,
                extraMessage = body,
                directSpokenFeedback = "Ji Krish, main ${targetContact?.name ?: "contact"} ko message send kar rahi hoon."
            )
        }

        // 4. Simulate Girlfriend Incoming Call ("call upade", "call uthao", "call me", "tum call karo")
        if (lower.contains("call me") || lower.contains("tum call karo") || lower.contains("call upade") ||
            lower.contains("call uthao") || lower.contains("mujhe call karo") || lower.contains("call priya")
        ) {
            return ParsedCommand(
                actionType = ActionType.SIMULATE_CALL,
                targetData = "Priya",
                directSpokenFeedback = "Haan Krish! Main abhi tumhe call kar rahi hoon, jaldi se call uthao!"
            )
        }

        // 5. Google Search: "google par search karo [query]", "google ma search karo", "search [query]"
        if (lower.startsWith("google ") || lower.contains("search google") || lower.contains("google search") ||
            lower.contains("google par") || lower.contains("google ma") || lower.startsWith("search ")
        ) {
            val searchQuery = input
                .replace("(?i)google par search karo".toRegex(), "")
                .replace("(?i)search google for".toRegex(), "")
                .replace("(?i)google search".toRegex(), "")
                .replace("(?i)google par".toRegex(), "")
                .replace("(?i)google ma".toRegex(), "")
                .replace("(?i)search".toRegex(), "")
                .replace("(?i)kholo".toRegex(), "")
                .replace("(?i)dhundo".toRegex(), "")
                .trim()

            val query = if (searchQuery.isNotBlank()) searchQuery else "Google"
            return ParsedCommand(
                actionType = ActionType.SEARCH_GOOGLE,
                targetData = query,
                directSpokenFeedback = "Main Google par '$query' search kar rahi hoon!"
            )
        }

        // 6. Mummy Help / Medicine: "dawai", "mummy dawai", "medicine"
        if (lower.contains("dawai") || lower.contains("medicine") || lower.contains("dawa")) {
            return ParsedCommand(
                actionType = ActionType.MUMMY_REMINDER,
                targetData = "medicine",
                directSpokenFeedback = "Maine Mummy ke dawai aur dincharya ka schedule khol diya hai!"
            )
        }

        return ParsedCommand(null, null)
    }
}
