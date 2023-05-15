import { useCallback, useRef } from "react";
import { GameState } from "../types/GameState";

export function useDrawGameState() {
  const canvasRef = useRef<HTMLCanvasElement>(null);

  const drawGameState = useCallback((gameState: GameState) => {
    if (!canvasRef.current) return;

    const ctx = canvasRef.current.getContext("2d");
    if (!ctx) return;

    const { leftPaddle, rightPaddle, ball, field } = gameState;

    canvasRef.current.width = field.width;
    canvasRef.current.height = field.height;

    ctx.clearRect(0, 0, field.width, field.height);

    // Draw player 1
    ctx.fillStyle = "white";
    ctx.fillRect(leftPaddle.x, leftPaddle.y, leftPaddle.width, leftPaddle.height);

    // Draw player 2
    ctx.fillStyle = "white";

    ctx.fillRect(rightPaddle.x, rightPaddle.y, rightPaddle.width, rightPaddle.height);
    ctx.globalAlpha = 1;

    // Draw ball
    ctx.fillStyle = "white";
    ctx.beginPath();
    ctx.arc(ball.x, ball.y, ball.radius, 0, 2 * Math.PI);
    ctx.fill();
  }, []);

  return { canvasRef, drawGameState };
}
