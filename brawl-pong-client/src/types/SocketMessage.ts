import { GameState } from "./GameState";
import { PlayerInfo } from "./Paddle";

export type PlayerInfoMessageData = {
  leftPaddle: PlayerInfo,
  rightPaddle: PlayerInfo | null,
}

export type CountDownMessageData = {
  countdown: number; // Seconds to start
}

export type GameStateMessageData = {
  tickRate: number;
  gameState: GameState;
}

export type StatsMessageData = {
  winner?: string;
  score: {
    leftPaddle: number;
    rightPaddle: number;
  }
}


export type SocketMessage =
  | {
    type: 'player-info';
    data: PlayerInfoMessageData;
  }
  | {
    type: "game-state";
    data: GameStateMessageData;
  }
  | {
    type: "match-countdown";
    data: CountDownMessageData
  }
  | {
    type: "stats";
    data: StatsMessageData;
  };
