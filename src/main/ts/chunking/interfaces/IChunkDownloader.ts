import {Chunky} from "@/chunking/downloading/DownloadManager";

export type DownloadParams = {
    designId: number,
    chunkIndex: number,
    isCompetition: boolean
}

/**
 * Describes a class for uploading a chunk
 */
export interface IChunkDownloader {
    download(params: DownloadParams): Promise<Chunky>;
}