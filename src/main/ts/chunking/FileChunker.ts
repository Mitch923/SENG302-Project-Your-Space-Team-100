import {ChunkingConfig, IChunkingStrategy} from "@/chunking/interfaces/IChunkingStrategy";
import {IFileChunker} from "@/chunking/interfaces/IFileChunker";
import {FileChunk} from "@/chunking/models/FileChunk";

export class FileChunker implements IFileChunker {
    constructor(strategy: IChunkingStrategy) {
        this._strategy = strategy;
    }

    private _strategy: IChunkingStrategy;

    get strategy() {
        return this._strategy;
    }

    set strategy(strategy: IChunkingStrategy) {
        this._strategy = strategy;
    }

    public async chunkFile(file: File | Blob, config: ChunkingConfig): Promise<FileChunk[]> {
        return this.strategy.createChunks(file, config);
    }

    calculateChunks(file: File | Blob, config?: ChunkingConfig): number {
        return this._strategy.chunkCount(file);
    }

    chunkFileSync(file: File | Blob): FileChunk[] {
        console.warn("Not yet implemented");
        return [];
    }
}