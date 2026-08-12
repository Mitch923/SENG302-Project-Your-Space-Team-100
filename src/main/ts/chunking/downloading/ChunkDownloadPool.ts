import {Semaphore} from "@/util/Semaphore";
import {DownloadParams, IChunkDownloader} from "@/chunking/interfaces/IChunkDownloader";
import {ConcreteChunkDownloader} from "@/chunking/downloading/ConcreteChunkDownloader";
import {Chunky} from "@/chunking/downloading/DownloadManager";

/**
 * Manages a pool of current uploads using a semaphore.
 *
 * The default maximum is 3 concurrent uploads, this can be adjusted in the constructor
 */
export class ChunkDownloadPool {
    private readonly semaphore: Semaphore;
    private readonly chunkDownloader: IChunkDownloader;

    constructor(chunkDownloader: IChunkDownloader = new ConcreteChunkDownloader(), maxConcurrent: number = 3) {
        this.semaphore = new Semaphore(maxConcurrent);
        this.chunkDownloader = chunkDownloader;
    }

    async downloadChunk(params: DownloadParams): Promise<Chunky> {
        await this.semaphore.acquire();

        try {
            return await this.chunkDownloader.download(params);
        } finally {
            await this.semaphore.release();
        }
    }
}