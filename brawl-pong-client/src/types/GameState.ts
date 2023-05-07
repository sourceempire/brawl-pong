import { Ball } from "./Ball";
import { Field } from "./Field";
import { Player } from "./Player";

export type GameState = {
    player1: Player,
    player2: Player,
    ball: Ball,
    field: Field,
}