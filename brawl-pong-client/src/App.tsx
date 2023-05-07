import { useCallback, useState } from "react";
import { useDrawGameState } from "./hooks/useDrawGameState";
import { usePrediction } from "./hooks/usePrediction";
import { useControls } from "./hooks/useControls";
import {
  CountDownMessageData,
  GameStateMessageData,
  SocketMessage,
} from "./types/SocketMessage";
import { useConnection } from "./hooks/useConnection";

function App() {
  const [playerNumber, setPlayerNumber] = useState<number | null>(null);
  const [secondsToStart, setSecondsToStart] = useState<number | null>(null);

  const [player1Score, setPlayer1Score] = useState<number>(0);
  const [player2Score, setPlayer2Score] = useState<number>(0);

  const { canvasRef, drawGameState } = useDrawGameState();
  const { stopPrediction, startPrediction } = usePrediction(drawGameState);

  const handleGameStateChange = useCallback(
    (message: GameStateMessageData) => {
      stopPrediction();
      startPrediction(message.gameState, message.tickRate);

      setPlayerNumber(message.clientPlayerNumber);
      setPlayer1Score(message.gameState.player1.score);
      setPlayer2Score(message.gameState.player2.score);
    },
    [startPrediction, stopPrediction]
  );

  const handleCountDown = useCallback((message: CountDownMessageData) => {
    setSecondsToStart(message.countdown);
  }, []);

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
      }
    },
    [handleCountDown, handleGameStateChange]
  );

  const { socket } = useConnection({ onMessage });

  useControls(socket);

  return (
    <div className="App">
      <div className="game-field-wrapper">
        {playerNumber === 1 && (
          <div className="player-info left">This is you</div>
        )}
        <canvas className="game-field" ref={canvasRef}></canvas>
        {playerNumber === 2 && (
          <div className="player-info right">This is you</div>
        )}
      </div>
      {secondsToStart !== null && <div>Seconds to start: {secondsToStart}</div>}
      <div>player 1 score: {player1Score}</div>
      <div>player 2 score: {player2Score}</div>
      <button onClick={() => socket?.send(JSON.stringify({ type: "ready" }))}>
        Im ready
      </button>
    </div>
  );
}

export default App;
