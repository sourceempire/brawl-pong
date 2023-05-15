import { Ball } from "./Ball";
import { Field } from "./Field";
import { Paddle } from "./Paddle";

export type GameState = {
    leftPaddle: Paddle,
    rightPaddle: Paddle,
    ball: Ball,
    field: Field,
    paused: boolean,
    winner: string | null // UUID -> playerId
}