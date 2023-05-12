package io.sourceempire.brawlpong.utils

import io.vertx.core.Vertx
import io.vertx.core.impl.VertxInternal
import io.vertx.spi.cluster.zookeeper.ZookeeperClusterManager

/**
 * Get the cluster manager used by vertx. Will only return if vertx is in
 * clustered mode and the cluster manager is zookeeper
 */
fun Vertx.getClusterManager(): ZookeeperClusterManager? {
    if (this.isClustered) {
        val vertxInternal = this as VertxInternal
        val clusterManager = vertxInternal.clusterManager
        if (clusterManager is ZookeeperClusterManager) {
            return clusterManager
        }
    }
    return null
}
