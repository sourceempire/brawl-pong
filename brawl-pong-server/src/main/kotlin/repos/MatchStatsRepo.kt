package io.sourceempire.brawlpong.repos

import io.sourceempire.brawlpong.exceptions.MatchStatsNotFoundException
import io.sourceempire.brawlpong.models.MatchStats
import io.vertx.core.Future
import java.util.*

interface MatchStatsRepo {
    fun updateMatchStats(matchStats: MatchStats): Future<Unit>
    fun getMatchStats(matchId: UUID): Future<MatchStats>
}

class MatchStatsRepoLocal: MatchStatsRepo {
    private val matchStatsMap = mutableMapOf<UUID, MatchStats>()

    override fun updateMatchStats(matchStats: MatchStats): Future<Unit> {
        matchStatsMap[matchStats.matchId] = matchStats
        return Future.succeededFuture()
    }

    override fun getMatchStats(matchId: UUID): Future<MatchStats> {
        val matchStats = matchStatsMap[matchId]?: return Future.failedFuture(MatchStatsNotFoundException())
        return Future.succeededFuture(matchStats)
    }
}