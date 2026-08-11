package com.oblivi0n_arch.flowwithnoir.data

import java.time.LocalTime

enum class BreathPhase(val label: String) {
    INHALE("WDECH"),
    INHALE_TOP("DOBIERZ"),
    HOLD_IN("ZATRZYMAJ"),
    EXHALE("WYDECH"),
    HOLD_OUT("ZATRZYMAJ")
}

data class BreathPattern(
    val inhaleSeconds: Int,
    val holdInSeconds: Int,
    val exhaleSeconds: Int,
    val holdOutSeconds: Int,
    val inhaleTopSeconds: Int = 0
) {
    val hasTopUp: Boolean
        get() = inhaleTopSeconds > 0

    val cycleSeconds: Int
        get() = inhaleSeconds + inhaleTopSeconds + holdInSeconds + exhaleSeconds + holdOutSeconds

    val phases: List<Pair<BreathPhase, Int>>
        get() = listOf(
            BreathPhase.INHALE to inhaleSeconds,
            BreathPhase.INHALE_TOP to inhaleTopSeconds,
            BreathPhase.HOLD_IN to holdInSeconds,
            BreathPhase.EXHALE to exhaleSeconds,
            BreathPhase.HOLD_OUT to holdOutSeconds
        ).filter { it.second > 0 }

    fun phaseAt(elapsedSeconds: Int): Pair<BreathPhase, Float> {
        val activePhases = phases
        if (activePhases.isEmpty() || cycleSeconds == 0) {
            return BreathPhase.INHALE to 0f
        }
        var offset = elapsedSeconds % cycleSeconds
        for ((phase, length) in activePhases) {
            if (offset < length) {
                return phase to (offset.toFloat() / length.toFloat())
            }
            offset -= length
        }
        val last = activePhases.last()
        return last.first to 1f
    }

    fun phaseAtMillis(elapsedMillis: Long): Pair<BreathPhase, Float> {
        val activePhases = phases
        if (activePhases.isEmpty() || cycleSeconds == 0) {
            return BreathPhase.INHALE to 0f
        }
        val cycleMillis = cycleSeconds * 1000L
        var offset = elapsedMillis % cycleMillis
        for ((phase, lengthSeconds) in activePhases) {
            val lengthMillis = lengthSeconds * 1000L
            if (offset < lengthMillis) {
                return phase to (offset.toFloat() / lengthMillis.toFloat())
            }
            offset -= lengthMillis
        }
        val last = activePhases.last()
        return last.first to 1f
    }

    val shortLabel: String
        get() {
            val head = if (hasTopUp) "$inhaleSeconds+$inhaleTopSeconds" else "$inhaleSeconds"
            val tail = listOf(holdInSeconds, exhaleSeconds, holdOutSeconds)
                .filter { it > 0 }
                .map { it.toString() }
            return (listOf(head) + tail).joinToString("-")
        }
}

enum class SessionCategory(val label: String) {
    MORNING("Poranek"),
    FOCUS("Skupienie"),
    CALM("Cisza"),
    SLEEP("Sen")
}

data class SessionTemplate(
    val id: String,
    val name: String,
    val technique: String,
    val description: String,
    val pattern: BreathPattern,
    val defaultDurationSeconds: Int,
    val category: SessionCategory
) {
    fun meta(durationSeconds: Int = defaultDurationSeconds): String =
        "${durationSeconds / 60} min · $technique"
}

object SessionCatalog {

    val templates: List<SessionTemplate> = listOf(
        SessionTemplate(
            id = "morning_breath",
            name = "Poranny oddech",
            technique = "box breathing",
            description = "Równy, kwadratowy rytm. Rozbudza bez pobudzania — dobry start dnia, " +
                "kiedy głowa jeszcze się nie rozkręciła.",
            pattern = BreathPattern(4, 4, 4, 4),
            defaultDurationSeconds = 300,
            category = SessionCategory.MORNING
        ),
        SessionTemplate(
            id = "grounding",
            name = "Uziemienie",
            technique = "body scan",
            description = "Powolny, długi wydech i uwaga wędrująca po ciele. Wraca cię do tu i teraz, " +
                "gdy myśli uciekają w przyszłość.",
            pattern = BreathPattern(4, 0, 6, 0),
            defaultDurationSeconds = 600,
            category = SessionCategory.CALM
        ),
        SessionTemplate(
            id = "before_sleep",
            name = "Przed snem",
            technique = "4-7-8",
            description = "Klasyka na zasypianie. Wydech dwa razy dłuższy od wdechu wycisza układ nerwowy.",
            pattern = BreathPattern(4, 7, 8, 0),
            defaultDurationSeconds = 480,
            category = SessionCategory.SLEEP
        ),
        SessionTemplate(
            id = "metta",
            name = "Życzliwość",
            technique = "metta",
            description = "Spokojny oddech w tle, uwaga na życzliwych zdaniach kierowanych do siebie i innych.",
            pattern = BreathPattern(5, 0, 5, 0),
            defaultDurationSeconds = 420,
            category = SessionCategory.CALM
        ),
        SessionTemplate(
            id = "reset",
            name = "Szybki reset",
            technique = "physiological sigh",
            description = "Wdech, krótkie dobranie powietrza na szczycie, długi wydech. Najszybszy " +
                "sposób na zbicie napięcia między jednym a drugim zadaniem.",
            pattern = BreathPattern(
                inhaleSeconds = 3,
                holdInSeconds = 0,
                exhaleSeconds = 6,
                holdOutSeconds = 0,
                inhaleTopSeconds = 1
            ),
            defaultDurationSeconds = 180,
            category = SessionCategory.FOCUS
        ),
        SessionTemplate(
            id = "focus",
            name = "Ostrość",
            technique = "coherent breathing",
            description = "Pięć oddechów na minutę. Wyrównuje tętno i pomaga wejść w dłuższą pracę.",
            pattern = BreathPattern(6, 0, 6, 0),
            defaultDurationSeconds = 600,
            category = SessionCategory.FOCUS
        ),
        SessionTemplate(
            id = "tension_release",
            name = "Rozładowanie",
            technique = "extended exhale",
            description = "Krótki wdech, bardzo długi wydech. Na moment, gdy napięcie siedzi w barkach i szczęce.",
            pattern = BreathPattern(4, 0, 8, 0),
            defaultDurationSeconds = 420,
            category = SessionCategory.CALM
        ),
        SessionTemplate(
            id = "night_wake",
            name = "Nocne przebudzenie",
            technique = "4-7-8",
            description = "Krótsza wersja na powrót do snu po przebudzeniu w środku nocy. Bez pośpiechu.",
            pattern = BreathPattern(4, 7, 8, 0),
            defaultDurationSeconds = 240,
            category = SessionCategory.SLEEP
        ),
        SessionTemplate(
            id = "morning_energy",
            name = "Rozruch",
            technique = "energizing breath",
            description = "Nieco żwawszy rytm z krótkim zatrzymaniem u góry. Kiedy trzeba się obudzić, nie uspokoić.",
            pattern = BreathPattern(3, 2, 4, 0),
            defaultDurationSeconds = 300,
            category = SessionCategory.MORNING
        )
    )

    val durationOptionsMinutes: List<Int> = listOf(1, 3, 5, 10, 15, 20, 30)

    fun byId(id: String): SessionTemplate? = templates.firstOrNull { it.id == id }

    fun byCategory(category: SessionCategory): List<SessionTemplate> =
        templates.filter { it.category == category }

    fun suggestedFor(time: LocalTime = LocalTime.now()): SessionTemplate {
        val category = when (time.hour) {
            in 5..10 -> SessionCategory.MORNING
            in 11..16 -> SessionCategory.FOCUS
            in 17..21 -> SessionCategory.CALM
            else -> SessionCategory.SLEEP
        }
        return byCategory(category).firstOrNull() ?: templates.first()
    }

    fun featured(time: LocalTime = LocalTime.now(), count: Int = 4): List<SessionTemplate> {
        val suggested = suggestedFor(time)
        return (listOf(suggested) + templates.filter { it.id != suggested.id }).take(count)
    }
}
