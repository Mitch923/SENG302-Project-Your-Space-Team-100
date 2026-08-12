export class Semaphore {
    private permits: number;
    private readonly waiting: Array<() => void> = [];

    constructor(permits: number) {
        this.permits = permits;
    }

    async acquire(): Promise<void> {
        if (this.permits > 0) {
            this.permits--;
            return;
        }

        return new Promise<void>(resolve => {
            this.waiting.push(resolve);
        });
    }

    async release(): Promise<void> {
        this.permits++;
        const next = this.waiting.shift();
        if (next) {
            this.permits--;
            next();
        }
    }
}