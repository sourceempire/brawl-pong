import { GameState } from "./GameState";

export type GameStateMessageData = {
    tickRate: number;
    clientPlayerNumber: 1 | 2;
    gameState: GameState;
}

export type CountDownMessageData = {
    countdown: number; // Seconds to start
}

export type SocketMessage =
  | {
      type: "game-state";
      data: GameStateMessageData;
    }
  | {
      type: "match-countdown";
      data: CountDownMessageData
    };
