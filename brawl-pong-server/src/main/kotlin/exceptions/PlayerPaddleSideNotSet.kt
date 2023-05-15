package io.sourceempire.brawlpong.exceptions

import io.sourceempire.brawlpong.models.PaddleSide

class PlayerPaddleSideNotSet(paddleSide: PaddleSide) : Exception("$paddleSide paddle side had no player connection")