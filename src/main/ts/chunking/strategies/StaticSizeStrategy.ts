import {ChunkingConfig, IChunkingStrategy} from "@/chunking/interfaces/IChunkingStrategy";
import {FileChunk} from "@/chunking/models/FileChunk";

const toBytes = (value: number) => {
    return value * 1048576;
}

/**
 * Ensures each chunk made is at or below a specific size (in MB).
 */
export class StaticSizeStrategy implements IChunkingStrategy {

    private readonly maxChunkSizeMb: number;

    /**
     *
     * @param maxChunkSizeMb The max chunk size in MB
     */
    constructor(maxChunkSizeMb: number) {
        this.maxChunkSizeMb = maxChunkSizeMb;
    }

    createChunks(file: File | Blob, config: ChunkingConfig): FileChunk[] {
        const chunks: FileChunk[] = [];
        const chunkSizeBytes = toBytes(this.maxChunkSizeMb);
        const totalSize = file.size;

        let offset = 0;
        let chunkIndex = 0;

        while (offset < totalSize) {
            const remainingBytes = totalSize - offset;
            const currentChunkSize = Math.min(chunkSizeBytes, remainingBytes);

            const chunk: FileChunk = {
                index: chunkIndex,
                tempUploadToken: config.token,
                start: offset,
                end: offset + currentChunkSize - 1,
                size: currentChunkSize,
                blob: file.slice(offset, offset + currentChunkSize)
            };

            chunks.push(chunk);

            offset += currentChunkSize;
            chunkIndex++;
        }

        return chunks;
    }

    chunkCount(file: File | Blob): number {
        const chunkSizeBytes = toBytes(this.maxChunkSizeMb);
        const totalSize = file.size;

        if (totalSize == 0) {
            return 0;
        }


        return Math.ceil(totalSize / chunkSizeBytes);
    }
}