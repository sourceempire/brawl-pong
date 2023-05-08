export enum PlayerDirection {
    Stop = "Stop",
    Up = "Up",
    Down = "Down"
}

export type Player = {
    id: string,
    ready: boolean,
    score: number,
    connected: boolean,
    renderData: {
        x: number,
        y: number,
        direction: PlayerDirection,
        height: number,
        width: number,
        speed: number,
    }
}

export type PlayerInfo = {
    id: string
    isSessionPlayer: boolean,
}

