import {FileChunk} from "@/chunking/models/FileChunk";

/**
 * Describes a class for uploading a chunk
 */
export interface IChunkUploader {
    upload(chunk: FileChunk): Promise<void>;
}