import {IFileChunker} from "@/chunking/interfaces/IFileChunker";
import {ChunkUploadPool} from "@/chunking/uploading/ChunkUploadPool";
import {ChunkingConfig} from "@/chunking/interfaces/IChunkingStrategy";
import CSRFProvider from "@/util/csrfProvider";

export type UploadConfig = {
    designId: number;
    isCompetition: boolean;
}

export type UploadProgress = {
    chunksCompleted: number;
    totalChunks: number;
    bytesUploaded: number;
    totalBytes: number;
    percentComplete: number;
}

export class UploadManager {

    private readonly uploadPool: ChunkUploadPool = new ChunkUploadPool();

    constructor(fileChunker: IFileChunker) {
        this._fileChunker = fileChunker;
    }

    private _fileChunker: IFileChunker;

    get fileChunker(): IFileChunker {
        return this._fileChunker;
    }

    set fileChunker(value: IFileChunker) {
        this._fileChunker = value;
    }

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

    async setupUpload(file: File | Blob, config: UploadConfig): Promise<ChunkingConfig> {
        const url = 'chunks/initiate';
        const request: InitiateUploadRequest = {
            designId: config.designId,
            isCompetition: config.isCompetition,
            expectedChunks: this._fileChunker.calculateChunks(file),
            totalSize: file.size
        };

        const res = await fetch(url, {
            method: 'POST',
            body: JSON.stringify(request),
            headers: {
                'Content-Type': 'application/json',
                [CSRFProvider.getCsrfHeaderName()]: CSRFProvider.getCsrfToken()
            }
        });

        if (!res.ok) {
            throw new Error(res.statusText);
        }

        const response = await res.json() as InitiateUploadResponse;

        return {
            token: response.tempUploadToken
        };
    }

    /**
     * Uploads a scene to the server by breaking it into chunks to circumvent the 10mb limit per request
     *
     * @param file
     * @param config
     */
    async uploadFile(file: File | Blob, config: UploadConfig): Promise<void> {
        const chunkingConfig: ChunkingConfig = await this.setupUpload(file, config);
        const chunks = await this._fileChunker.chunkFile(file, chunkingConfig);

        // Progress tracking
        let chunksCompleted = 0;
        let bytesUploaded = 0;
        const totalChunks = chunks.length;
        const totalBytes = file.size;

        const updateProgress = () => {
            const percentComplete = totalBytes > 0 ? (bytesUploaded / totalBytes) * 100 : 0;

            this._onProgress({
                chunksCompleted,
                totalChunks,
                bytesUploaded,
                totalBytes,
                percentComplete
            });
        };

        updateProgress();

        const uploadPromises = chunks.map(chunk => {
                    this.uploadPool.uploadChunk(chunk).then(() => {
                        chunksCompleted++;
                        bytesUploaded += chunk.size;
                        updateProgress();
                    })
                }
        );

        await Promise.all(uploadPromises);
        this._onComplete();
    }
}