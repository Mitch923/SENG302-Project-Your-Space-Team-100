import {ChunkingConfig, IChunkingStrategy} from "@/chunking/interfaces/IChunkingStrategy";
import {FileChunk} from "@/chunking/models/FileChunk";

export interface IFileChunker {
    strategy: IChunkingStrategy;

    chunkFile(file: File | Blob, config: ChunkingConfig): Promise<FileChunk[]>;

    chunkFileSync(file: File | Blob): FileChunk[];

    calculateChunks(chunkFile: File | Blob, config?: ChunkingConfig): number;
}