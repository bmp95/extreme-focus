package com.example.domain

object ToughLoveQuoteEngine {

    private val quotes = listOf(
        ToughQuote(
            title = "¿EN SERIO OTRA VEZ?",
            body = "¿De verdad abres esta porquería de nuevo? Nadie te está buscando. Ningún reel ni mensaje cambiará tu realidad. Cierra esto y ponte a construir la vida que juras querer.",
            category = "REALIDAD"
        ),
        ToughQuote(
            title = "ESPECTADOR PASIVO",
            body = "Llevas horas consumiendo las vidas ficticias de extraños mientras la tuya se desmorona en segundo plano. ¿Te parece entretenido o simplemente te rendiste?",
            category = "EGO"
        ),
        ToughQuote(
            title = "ESCLAVO DEL ALGORITMO",
            body = "Tu capacidad de atención es tan frágil que una luz roja domina tu voluntad completa. Eres un peón generando centavos publicitarios a cambio de tu juventud.",
            category = "DISCIPLINA"
        ),
        ToughQuote(
            title = "LA EXCUSA DE 'SOLO UN MINUTO'",
            body = "Sabes perfectamente que 'solo un minuto' son 45 minutos de scroll zombi con la boca abierta. Levántate, toma agua y haz lo que estás evitando hacer.",
            category = "PROCRASTINACIÓN"
        ),
        ToughQuote(
            title = "MIEDO AL SILENCIO",
            body = "No abres esta app por curiosidad; la abres porque no toleras quedarte a solas con tus propios pensamientos por 30 segundos sin dopamina barata.",
            category = "VERDAD"
        ),
        ToughQuote(
            title = "TU FUTURO TE OBSERVA",
            body = "La versión de ti dentro de 5 años está arrepentida de esta media hora que estás a punto de tirar a la basura. Bloqueado por tu propio bien.",
            category = "FUTURO"
        )
    )

    fun getRandomQuote(): ToughQuote {
        return quotes.random()
    }

    fun getQuoteForApp(appName: String, minutesSpent: Int): ToughQuote {
        val base = getRandomQuote()
        return if (minutesSpent > 0) {
            base.copy(
                body = "Llevas ya $minutesSpent minutos hoy atrapado en $appName. ${base.body}"
            )
        } else {
            base
        }
    }
}

data class ToughQuote(
    val title: String,
    val body: String,
    val category: String
)
