package io.sourceempire.brawlpong.listeners

import java.util.*

interface MatchEventListener {
    fun onStateChanged(matchId: UUID)
    fun onMatchEnd(matchId: UUID, winner: UUID)
}