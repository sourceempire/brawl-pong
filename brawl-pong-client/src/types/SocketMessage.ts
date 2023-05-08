import { GameState } from "./GameState";

export type PlayerInfoMessageData = {
  player1: {
    id: string,
    isSessionPlayer: boolean;
  },
  player2: {
    id: string,
    isSessionPlayer: boolean;
  },
}

export type CountDownMessageData = {
  countdown: number; // Seconds to start
}

export type GameStateMessageData = {
    tickRate: number;
    gameState: GameState;
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
    };
