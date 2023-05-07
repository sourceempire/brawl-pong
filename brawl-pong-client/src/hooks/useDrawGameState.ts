import { useCallback, useRef } from "react";
import { GameState } from "../types/GameState";

export function useDrawGameState() {
  const canvasRef = useRef<HTMLCanvasElement>(null);

  const drawGameState = useCallback((gameState: GameState) => {
    if (!canvasRef.current) return;

    const ctx = canvasRef.current.getContext("2d");
    if (!ctx) return;

    const { player1, player2, ball, field } = gameState;

    canvasRef.current.width = field.width;
    canvasRef.current.height = field.height;

    ctx.clearRect(0, 0, field.width, field.height);

    // Draw player 1
    ctx.fillStyle = "white";
    ctx.fillRect(player1.renderData.x, player1.renderData.y, player1.renderData.width, player1.renderData.height);

    // Draw player 2
    ctx.fillStyle = "white";

    if (!player2.connected) {
        ctx.globalAlpha = 0.4;
    }

    ctx.fillRect(player2.renderData.x, player2.renderData.y, player2.renderData.width, player2.renderData.height);
    ctx.globalAlpha = 1;

    // Draw ball
    ctx.fillStyle = "white";
    ctx.beginPath();
    ctx.arc(ball.x, ball.y, ball.radius, 0, 2 * Math.PI);
    ctx.fill();
  }, []);

  return { canvasRef, drawGameState };
}
