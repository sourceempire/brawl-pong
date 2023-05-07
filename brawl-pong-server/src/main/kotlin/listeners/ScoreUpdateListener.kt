package listeners

import models.Match
import java.util.*

interface ScoreUpdateListener {
    fun onPlayerScoreUpdate(match: Match, player: UUID)
}