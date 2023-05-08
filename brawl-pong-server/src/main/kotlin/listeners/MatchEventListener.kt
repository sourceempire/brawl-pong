package listeners

import models.Match
import models.Player
import java.util.*

interface MatchEventListener {
    fun onMatchCreated(match: Match)
    fun onPlayerConnected(match: Match, player: Player)
    fun onPlayerDisconnected(match: Match, player: Player)
    fun onPlayerReady(match: Match, player: Player)
    fun onMatchStarted(match: Match)
    fun onPlayerScored(match: Match, player: UUID)
    fun onMatchEnded(match: Match)
}