import {Texture, TextureLoader} from "three";

/**
 * Function written by Claude (https://claude.ai).
 * Converts a File into a ThreeJS Texture object asynchronously.
 *
 * @param file the file to convert into a three texture
 * @return a Promise that will resolve to the file converted into a Texture object
 */
export function fileToTexture(file: File): Promise<Texture> {
    return new Promise((resolve, reject) => {
        const textureLoader = new TextureLoader();
        const imageUrl = URL.createObjectURL(file);

        textureLoader.load(
                imageUrl,
                (texture) => {
                    URL.revokeObjectURL(imageUrl); // Clean up
                    resolve(texture);
                },
                undefined,
                (error) => {
                    URL.revokeObjectURL(imageUrl); // Clean up on error too
                    reject(error);
                }
        );
    });
}