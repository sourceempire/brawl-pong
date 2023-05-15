export enum PlayerDirection {
    Stop = "Stop",
    Up = "Up",
    Down = "Down"
}

export type Paddle = {
    x: number,
    y: number,
    direction: PlayerDirection,
    height: number,
    width: number,
    speed: number,
}

export type PlayerInfo = {
    id: string
    ready: boolean,
    connected: boolean,
    isSessionPlayer: boolean,
}

