import { useCallback, useState } from "react";
import { useDrawGameState } from "./hooks/useDrawGameState";
import { usePrediction } from "./hooks/usePrediction";
import { useControls } from "./hooks/useControls";
import {
  CountDownMessageData,
  GameStateMessageData,
  PlayerInfoMessageData,
  SocketMessage,
  StatsMessageData,
} from "./types/SocketMessage";
import { useConnection } from "./hooks/useConnection";
import { PlayerInfo } from "./types/Paddle";

function App() {
  const [secondsToStart, setSecondsToStart] = useState<number | null>(null);

  const [player1Score, setPlayer1Score] = useState<number>(0);
  const [player2Score, setPlayer2Score] = useState<number>(0);

  const [player1Info, setPlayer1Info] = useState<PlayerInfo | null>(null);
  const [player2Info, setPlayer2Info] = useState<PlayerInfo | null>(null);

  const [winner, setWinner] = useState<string | null>(null);

  const { canvasRef, drawGameState } = useDrawGameState();
  const { stopPrediction, startPrediction } = usePrediction(drawGameState);

  const handleGameStateChange = useCallback(
    (message: GameStateMessageData) => {
      console.log(message);
      stopPrediction();

      if (message.gameState.paused) {
        drawGameState(message.gameState);
        return;
      }

      startPrediction(message.gameState, message.tickRate);
    },
    [drawGameState, startPrediction, stopPrediction]
  );

  const handlePlayerInfo = useCallback((message: PlayerInfoMessageData) => {
    setPlayer1Info(message.leftPaddle);
    setPlayer2Info(message.rightPaddle);
  }, []);

  const handleCountDown = useCallback((message: CountDownMessageData) => {
    setSecondsToStart(message.countdown);
  }, []);

  const handleStatsMessage = useCallback((message: StatsMessageData) => {
    if (message.winner) {
      setWinner(message.winner);
    }

    setPlayer1Score(message.score.leftPaddle);
    setPlayer2Score(message.score.rightPaddle);
  }, [])

  // It is very important that onMessage is never recreated
  const onMessage = useCallback(
    (message: SocketMessage) => {
      switch (message.type) {
        case "game-state":
          handleGameStateChange(message.data);
          break;
        case "match-countdown":
          handleCountDown(message.data);
          break;
        case "player-info":
          handlePlayerInfo(message.data);
          break;
        case "stats":
          handleStatsMessage(message.data)
          break;
      }
    },
    [handleCountDown, handleGameStateChange, handlePlayerInfo, handleStatsMessage]
  );

  const { socket } = useConnection({ onMessage });

  useControls(socket);

  return (
    <div className="App">
      <div className="game-field-wrapper">
        {player1Info?.isSessionPlayer && (
          <div className="player-info left">This is you</div>
        )}

        {!player1Info?.connected && (
          <div className="player-info left">(not connected)</div>
        )}

        <canvas className="game-field" ref={canvasRef}></canvas>
        {player2Info?.isSessionPlayer && (
          <div className="player-info right">
            This is you {!player2Info.connected && "(not connected)"}
          </div>
        )}
        {!player2Info?.connected && (
          <div className="player-info right">(not connected)</div>
        )}
      </div>
      {secondsToStart !== null && <div>Seconds to start: {secondsToStart}</div>}
      <div>player 1 score: {player1Score}</div>
      <div>player 2 score: {player2Score}</div>
      <button onClick={() => socket?.send(JSON.stringify({ type: "ready" }))}>
        Im ready
      </button>

      {winner === player1Info?.id && <div>Player 1 won</div>}
      {winner === player2Info?.id && <div>Player 2 won</div>}
    </div>
  );
}

export default App;
