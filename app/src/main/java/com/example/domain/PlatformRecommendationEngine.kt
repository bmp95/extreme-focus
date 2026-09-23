package com.example.domain

/**
 * Standard preset restriction profiles recommended by digital wellbeing research
 * for high-distraction platforms (TikTok, Instagram, YouTube, X/Twitter, Reddit, Facebook).
 *
 * 1. STRICT ("Monje Digital" / Máxima restricción):
 *    - TikTok: 5m, Instagram: 10m, YouTube: 15m, X/Twitter: 10m, Reddit: 15m, Genérico: 10m
 * 2. BALANCED ("Equilibrado" / Recomendación estándar de salud digital):
 *    - TikTok: 15m, Instagram: 20m, YouTube: 30m, X/Twitter: 20m, Reddit: 25m, Genérico: 20m
 * 3. LENIENT ("Permisivo" / Mínima restricción antes de caer en adicción pasiva):
 *    - TikTok: 30m, Instagram: 40m, YouTube: 60m, X/Twitter: 35m, Reddit: 45m, Genérico: 35m
 */
enum class RestrictionTier(
    val title: String,
    val subtitle: String,
    val badge: String
) {
    STRICT(
        title = "Estricto",
        subtitle = "Corte radical para evitar scroll infinito",
        badge = "5 - 15 min"
    ),
    BALANCED(
        title = "Equilibrado",
        subtitle = "Consumo consciente recomendado para salud mental",
        badge = "15 - 30 min"
    ),
    LENIENT(
        title = "Flexible",
        subtitle = "Margen amplio antes de caer en pérdida de foco",
        badge = "30 - 60 min"
    )
}

object PlatformRecommendationEngine {

    fun getRecommendedMinutes(packageName: String, tier: RestrictionTier): Int {
        val lower = packageName.lowercase()
        return when (tier) {
            RestrictionTier.STRICT -> when {
                lower.contains("tiktok") || lower.contains("musically") -> 5
                lower.contains("instagram") -> 10
                lower.contains("twitter") || lower.contains("x.com") -> 10
                lower.contains("facebook") -> 10
                lower.contains("reddit") -> 15
                lower.contains("youtube") -> 15
                lower.contains("twitch") -> 15
                lower.contains("netflix") || lower.contains("disney") || lower.contains("primevideo") -> 20
                // Mobile Games & Gacha
                lower.contains("game") || lower.contains("clash") || lower.contains("brawl") ||
                        lower.contains("candy") || lower.contains("roblox") || lower.contains("pubg") ||
                        lower.contains("genshin") || lower.contains("freefire") || lower.contains("subway") -> 10
                else -> 10
            }
            RestrictionTier.BALANCED -> when {
                lower.contains("tiktok") || lower.contains("musically") -> 15
                lower.contains("instagram") -> 20
                lower.contains("twitter") || lower.contains("x.com") -> 20
                lower.contains("facebook") -> 15
                lower.contains("reddit") -> 25
                lower.contains("youtube") -> 30
                lower.contains("twitch") -> 30
                lower.contains("netflix") || lower.contains("disney") || lower.contains("primevideo") -> 45
                // Mobile Games & Gacha
                lower.contains("game") || lower.contains("clash") || lower.contains("brawl") ||
                        lower.contains("candy") || lower.contains("roblox") || lower.contains("pubg") ||
                        lower.contains("genshin") || lower.contains("freefire") || lower.contains("subway") -> 20
                else -> 20
            }
            RestrictionTier.LENIENT -> when {
                lower.contains("tiktok") || lower.contains("musically") -> 30
                lower.contains("instagram") -> 40
                lower.contains("twitter") || lower.contains("x.com") -> 35
                lower.contains("facebook") -> 30
                lower.contains("reddit") -> 45
                lower.contains("youtube") -> 60
                lower.contains("twitch") -> 60
                lower.contains("netflix") || lower.contains("disney") || lower.contains("primevideo") -> 90
                // Mobile Games & Gacha
                lower.contains("game") || lower.contains("clash") || lower.contains("brawl") ||
                        lower.contains("candy") || lower.contains("roblox") || lower.contains("pubg") ||
                        lower.contains("genshin") || lower.contains("freefire") || lower.contains("subway") -> 40
                else -> 35
            }
        }
    }

    /**
     * Threshold where minutes configured are considered excessive / self-sabotaging.
     */
    fun isExcessive(packageName: String, minutes: Int): Boolean {
        val lower = packageName.lowercase()
        val limitThreshold = when {
            lower.contains("tiktok") || lower.contains("musically") -> 35
            lower.contains("instagram") -> 45
            lower.contains("youtube") -> 65
            lower.contains("twitch") -> 60
            lower.contains("twitter") || lower.contains("x.com") -> 40
            lower.contains("facebook") -> 35
            lower.contains("reddit") -> 50
            lower.contains("game") || lower.contains("clash") || lower.contains("brawl") ||
                    lower.contains("roblox") || lower.contains("pubg") || lower.contains("genshin") -> 35
            else -> 45
        }
        return minutes > limitThreshold
    }

    /**
     * Humiliating / Tough-love message generator when the user selects excessive time.
     */
    fun getHumiliatingMessage(appName: String, minutes: Int): String {
        return when {
            minutes >= 120 ->
                "¿${minutes} minutos al día en $appName? Prácticamente estás trabajando a tiempo parcial para sus accionistas. ¿Para qué te instalaste una app de enfoque si vas a regalarles media jornada?"
            minutes >= 90 ->
                "¿$minutes minutos? Casi dos horas diarias viendo vídeos de 15 segundos. En un año son un mes entero de tu vida mirando una pantalla con la boca abierta."
            minutes >= 60 ->
                "¿Una hora entera en $appName? No necesitas una app de enfoque, necesitas admitir que eres voluntario del algoritmo. ¿Tus metas pueden esperar 60 minutos diarios?"
            else ->
                "¿${minutes} min en $appName? Es bastante generoso para una app diseñada para secuestrar tu dopamina. No te engañes llamando a esto 'autocontrol'."
        }
    }
}
