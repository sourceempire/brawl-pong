import { useCallback, useRef } from "react";
import { GameState } from "../types/GameState";

export function useDrawGameState() {
  const canvasRef = useRef<HTMLCanvasElement>(null);

  const drawGameState = useCallback((gameState: GameState) => {
    if (!canvasRef.current) return;

    const ctx = canvasRef.current.getContext("2d");
    if (!ctx) return;

    const { paddle1, paddle2, ball, field } = gameState;

    canvasRef.current.width = field.width;
    canvasRef.current.height = field.height;

    ctx.clearRect(0, 0, field.width, field.height);

    // Draw player 1
    ctx.fillStyle = "white";
    ctx.fillRect(paddle1.renderData.x, paddle1.renderData.y, paddle1.renderData.width, paddle1.renderData.height);

    // Draw player 2
    ctx.fillStyle = "white";

    if (!paddle2.connected) {
        ctx.globalAlpha = 0.4;
    }

    ctx.fillRect(paddle2.renderData.x, paddle2.renderData.y, paddle2.renderData.width, paddle2.renderData.height);
    ctx.globalAlpha = 1;

    // Draw ball
    ctx.fillStyle = "white";
    ctx.beginPath();
    ctx.arc(ball.x, ball.y, ball.radius, 0, 2 * Math.PI);
    ctx.fill();
  }, []);

  return { canvasRef, drawGameState };
}
