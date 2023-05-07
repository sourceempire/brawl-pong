export type Ball = {
    x: number,
    y: number,
    dx: number,
    dy: number,
    radius: number,
    speed: number, // calculated from dx, dy in backend.
}