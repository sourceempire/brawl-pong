export enum PlayerDirection {
    Stop = "Stop",
    Up = "Up",
    Down = "Down"
}

export type Player = {
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

