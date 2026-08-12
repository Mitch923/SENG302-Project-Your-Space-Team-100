import {FileChunk} from "@/chunking/models/FileChunk";

export type ChunkingConfig = {
    token: string;
}

/**
 * Describes a strategy for chunking a blob or file
 */
export interface IChunkingStrategy {

    chunkCount(file: File | Blob): number;

    createChunks(file: File | Blob, config: ChunkingConfig): FileChunk[];
}