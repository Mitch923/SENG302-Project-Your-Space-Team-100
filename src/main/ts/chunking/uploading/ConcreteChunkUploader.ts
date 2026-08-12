import {IChunkUploader} from "@/chunking/interfaces/IChunkUploader";
import {FileChunk} from "@/chunking/models/FileChunk";
import CSRFProvider from "@/util/csrfProvider";

/**
 * Defines the actual upload behaviour for each chunk.
 *
 */
export class ConcreteChunkUploader implements IChunkUploader {

    private readonly chunk_url = "chunks/upload";

    async upload(chunk: FileChunk): Promise<void> {
        let formData = new FormData();
        formData.append("chunkData", chunk.blob);
        formData.append("request",
                new Blob([JSON.stringify({
                    tempUploadToken: chunk.tempUploadToken,
                    chunkIndex: chunk.index,
                    startByte: chunk.start,
                    endByte: chunk.end,
                } as ChunkUploadRequest)], {type: "application/json"}))

        return fetch(this.chunk_url, {
            method: "POST",
            headers: {
                [CSRFProvider.getCsrfHeaderName()]: CSRFProvider.getCsrfToken()
            },
            body: formData
        })
        .then(res => {
            const response = {...res.body} as ChunkUploadResponse;

        })
        .catch(err => console.error("Error sending a chunk", err));
    }
}