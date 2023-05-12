package io.sourceempire.brawlpong.listeners

import io.sourceempire.brawlpong.models.Match
import io.sourceempire.brawlpong.models.Player
import java.util.*

interface MatchEventListener {
    fun onStateChanged(matchId: UUID)
}