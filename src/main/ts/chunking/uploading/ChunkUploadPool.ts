import {Semaphore} from "@/util/Semaphore";
import {IChunkUploader} from "@/chunking/interfaces/IChunkUploader";
import {ConcreteChunkUploader} from "@/chunking/uploading/ConcreteChunkUploader";
import {FileChunk} from "@/chunking/models/FileChunk";

/**
 * Manages a pool of current uploads using a semaphore.
 *
 * The default maximum is 3 concurrent uploads, this can be adjusted in the constructor
 */
export class ChunkUploadPool {
    private readonly semaphore: Semaphore;
    private readonly chunkUploader: IChunkUploader;

    constructor(chunkUploader: IChunkUploader = new ConcreteChunkUploader(), maxConcurrent: number = 3) {
        this.semaphore = new Semaphore(maxConcurrent);
        this.chunkUploader = chunkUploader;
    }

    async uploadChunk(chunk: FileChunk): Promise<void> {
        await this.semaphore.acquire();

        try {
            return await this.chunkUploader.upload(chunk)
        } finally {
            await this.semaphore.release();
        }
    }
}