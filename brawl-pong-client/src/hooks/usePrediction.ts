import { useCallback, useRef } from "react";
import { GameState } from "../types/GameState";
import { Ball } from "../types/Ball";
import { Player, PlayerDirection } from "../types/Player";

export const usePrediction = (
  drawGameState: (gameState: GameState) => void
) => {
  const animationFrameRef = useRef<number>();
  const lastTimeRef = useRef<number>(0);
  const tickRateRef = useRef<number>();
  const predictedGameStateRef = useRef<GameState>();

  const predictState = useCallback(
    (time: number) => {
      if (!tickRateRef.current) return;
      if (!predictedGameStateRef.current) return;

      const deltaTime = time - lastTimeRef.current;
      const delta = (tickRateRef.current * deltaTime) / 1000;
      const { ball, player1, player2, field } = predictedGameStateRef.current;

      predictedGameStateRef.current = {
        ...predictedGameStateRef.current,
        ball: updatePredictedBallState(ball, delta),
        player1: updatePredictedPlayerState(player1, delta, field.height),
        player2: updatePredictedPlayerState(player2, delta, field.height),
      };

      drawGameState(predictedGameStateRef.current);

      animationFrameRef.current = requestAnimationFrame(predictState);

      lastTimeRef.current = time;
    },
    [drawGameState]
  );

  const startPrediction = useCallback(
    (gameState: GameState, tickRate: number) => {
      tickRateRef.current = tickRate;
      predictedGameStateRef.current = { ...gameState };
      drawGameState(predictedGameStateRef.current);

      requestAnimationFrame(predictState);
    },
    [predictState, drawGameState]
  );

  const stopPrediction = useCallback(() => {
    if (animationFrameRef.current !== undefined) {
      cancelAnimationFrame(animationFrameRef.current);
    }
  }, []);

  return {
    startPrediction,
    stopPrediction,
  };
};

// To compencate for client laging behind server (experminental)
const speedAdjustmentFactor = 1.038;

function updatePredictedBallState(ball: Ball, delta: number) {
  const { x, y, dx, dy } = ball;
  const movementX = dx * delta * speedAdjustmentFactor;
  const movementY = dy * delta * speedAdjustmentFactor;

  return { ...ball, x: x + movementX, y: y + movementY };
}

function updatePredictedPlayerState(
  player: Player,
  delta: number,
  fieldHeight: number
): Player {
  const { y, direction, speed, height } = player.renderData;

  const movement = speed * delta * speedAdjustmentFactor;
  const minPlayerY = 0;
  const maxPlayerY = fieldHeight - height;

  let newY = y;

  if (direction === PlayerDirection.Up) {
    newY = Math.max(y - movement, minPlayerY);
  } else if (direction === PlayerDirection.Down) {
    newY = Math.min(y + movement, maxPlayerY);
  }

  return { ...player, renderData: { ...player.renderData, y: newY } };
}
