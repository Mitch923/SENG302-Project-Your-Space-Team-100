type ChunkUploadRequest = {
    tempUploadToken: String;
    chunkIndex: number;
    startByte?: number;
    endByte?: number;
    totalSize?: number;
    totalChunks?: number;
    originalFileName?: string;
    mimeType?: string;
}

type ChunkUploadResponse = {
    success: boolean;
    message: string;
    receivedChunks: number;
    expectedChunks: number;
    uploadComplete: boolean;
}

type InitiateUploadRequest = {
    designId: number;
    fileName?: string;
    mimeType?: string;
    totalSize: number;
    expectedChunks: number;
    isCompetition: boolean;
}

type InitiateUploadResponse = {
    tempUploadToken: string;
    message: string;
    timeoutMinutes: number;
}

type InitiateDownloadResponse = {
    totalChunks: number;
}

type GetChunkRequest = {
    chunkIndex: number;
    designId: number;
    isCompetition: boolean;
}