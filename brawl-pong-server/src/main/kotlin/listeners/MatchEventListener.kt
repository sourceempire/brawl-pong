package io.sourceempire.brawlpong.listeners

import io.sourceempire.brawlpong.models.Match
import io.sourceempire.brawlpong.models.Player
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