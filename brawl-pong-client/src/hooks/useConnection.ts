import { useState, useEffect } from "react";
import SockJS from "sockjs-client";
import { SocketMessage } from "../types/SocketMessage";

type Options = {
  onMessage: (message: SocketMessage) => void;
};

export function useConnection({ onMessage }: Options) {
  const [socket, setSocket] = useState<WebSocket | null>(null);

  useEffect(() => {
    const token = getToken();
    const url = buildSocketUrl(token);
    const socket = new SockJS(url);
    setSocket(socket);

    socket.onopen = () => {
      console.log("Connected to server");
    };

    socket.onclose = () => {
      console.log("Disconnected from server");
    };

    socket.onmessage = (event) => {
      const message = JSON.parse(event.data) as SocketMessage;
      onMessage(message);
    };

    return () => socket.close();
  }, [onMessage]);

  return { socket };
}

function getToken(): string | null {
  const tokenFromUrl = new URLSearchParams(window.location.search).get("token");
  const tokenFromStorage = sessionStorage.getItem("token");

  if (tokenFromUrl) {
    sessionStorage.setItem("token", tokenFromUrl);
    removeTokenFromUrl();
    return tokenFromUrl;
  }

  return tokenFromStorage;
}

function buildSocketUrl(token: string | null): string {
  const baseUrl = "http://localhost:8181/game";
  return token ? `${baseUrl}?token=${encodeURIComponent(token)}` : baseUrl;
}

function removeTokenFromUrl() {
  const url = new URL(window.location.href);
  url.searchParams.delete("token");
  window.history.replaceState({}, document.title, url.toString());
}
