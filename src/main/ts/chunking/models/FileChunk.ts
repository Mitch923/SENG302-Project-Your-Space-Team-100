export interface FileChunk {
    index: number;
    tempUploadToken: string;
    start: number;
    end: number;
    size: number;
    blob: Blob;
}