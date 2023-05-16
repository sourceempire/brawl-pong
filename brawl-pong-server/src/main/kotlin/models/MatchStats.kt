package io.sourceempire.brawlpong.models

import java.util.*

data class PlayerStats(val playerId: UUID, val score: Int)
data class MatchStats(val matchId: UUID, val players: Map<UUID, PlayerStats>, val winner: UUID?)