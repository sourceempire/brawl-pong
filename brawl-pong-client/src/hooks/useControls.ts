import { useEffect, useRef } from "react";

export const useControls = (socket: WebSocket | null) => {
  const pressedKeysRef = useRef(new Set());

  useEffect(() => {
    if (!socket) {
      return;
    }

    const handleKeyDown = (event: KeyboardEvent) => {
      if (
        (event.key === "ArrowUp" || event.key === "ArrowDown") &&
        !pressedKeysRef.current.has(event.key)
      ) {
        pressedKeysRef.current.add(event.key);
        socket.send(JSON.stringify({ type: "key-down", key: event.key }));
      }
    };

    const handleKeyUp = (event: KeyboardEvent) => {
      if (event.key === "ArrowUp" || event.key === "ArrowDown") {
        pressedKeysRef.current.delete(event.key);
        socket.send(JSON.stringify({ type: "key-up", key: event.key }));
      }
    };

    document.addEventListener("keydown", handleKeyDown);
    document.addEventListener("keyup", handleKeyUp);

    return () => {
      document.removeEventListener("keydown", handleKeyDown);
      document.removeEventListener("keyup", handleKeyUp);
    };
  }, [socket]);
};