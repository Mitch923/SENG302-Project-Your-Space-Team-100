import {ChunkDownloadPool} from "@/chunking/downloading/ChunkDownloadPool";
import {UploadProgress} from "@/chunking/uploading/UploadManager";
import {DownloadParams} from "@/chunking/interfaces/IChunkDownloader";

export type DownloadConfig = {
    designId: number;
    isCompetition: boolean;
    url: string
}

export type Chunky = {
    index: number,
    blob: Blob
}

export class DownloadManager {

    private readonly downloadPool: ChunkDownloadPool = new ChunkDownloadPool();

    private _onComplete: () => void = () => {
    };

    set onComplete(callback: () => void) {
        this._onComplete = callback;
    }

    private _onProgress: (arg0: UploadProgress) => void = () => {
    };

    set onProgress(callback: (arg0: UploadProgress) => void) {
        this._onProgress = callback;
    }

    async initialiseDownload(config: DownloadConfig) {
        return fetch(config.url, {method: "GET"}).then(res => res.json()).then(res => {
            console.log(res);
            return res;
        });
    }

    async getFile(config: DownloadConfig) {
        this._onProgress({percentComplete: 0} as UploadProgress)
        const initialResponse = await this.initialiseDownload(config) as InitiateDownloadResponse;

        let range = Array.from({length: initialResponse.totalChunks}, (_, i) => i);
        let chunksReceived = 0;

        const chunkPromises: Promise<Chunky>[] = range.map(async chunkIndex => {
            const params: DownloadParams = {
                chunkIndex: chunkIndex,
                designId: config.designId,
                isCompetition: config.isCompetition
            };
            const result = this.downloadPool.downloadChunk(params);
            result.then(() => {
                console.log("Updating progress in download manager")
                chunksReceived++;
                this._onProgress({percentComplete: (chunksReceived / initialResponse.totalChunks) * 100} as UploadProgress)
            });

            return result;
        });

        const chunks = await Promise.all(chunkPromises);
        const orderedChunksData = chunks.sort((a, b) => a.index - b.index).map(chunkObj => chunkObj.blob);
        return new Blob(orderedChunksData);
    }


}