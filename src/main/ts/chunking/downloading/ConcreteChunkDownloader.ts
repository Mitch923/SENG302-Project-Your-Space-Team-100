import {DownloadParams, IChunkDownloader} from "@/chunking/interfaces/IChunkDownloader";
import {FileChunk} from "@/chunking/models/FileChunk";
import {Chunky} from "@/chunking/downloading/DownloadManager";

export class ConcreteChunkDownloader implements IChunkDownloader {

    download(params: DownloadParams): Promise<Chunky> {

        const searchParams = new URLSearchParams({
            chunkIndex: params.chunkIndex.toString(),
            designId: params.designId.toString(),
            isCompetition: params.isCompetition.toString()
        });

        const url = "chunks?" + searchParams.toString();

        return fetch(url, {
            method: "GET"
        })
        .then(async res => {
            return {index: Number(params.chunkIndex), blob: await res.blob()} as Chunky;
        });
    };
}