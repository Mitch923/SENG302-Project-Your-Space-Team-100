import {PerspectiveCamera, Vector2, Vector3, WebGLRenderer} from 'three';
import {EditorScene} from "@/editor-scene";
import CSRFProvider from "@/util/csrfProvider";

export type ScreenShotParams = {
    renderer: WebGLRenderer;
    scene: EditorScene;
    imageType: 'jpeg' | 'png';
    quality?: number;
    width?: number;
    height?: number;
    cameraPosition?: Vector3;
    cameraTarget?: Vector3;
    resetScene?: boolean;
}

/**
 * Take a screenshot of the provided scene from the camera
 * Returns a Blob object of the requested image and quality.
 *
 * @param params ScreenShotParams object
 */
export async function takeScreenshot(params: ScreenShotParams): Promise<Blob | null> {
    let {
        renderer,
        scene,
        imageType,
        quality,
        width,
        height,
        cameraPosition,
        cameraTarget,
    } = params;

    width ??= 800;
    height ??= 600;
    cameraPosition ??= new Vector3(15, 15, 15);
    cameraTarget ??= new Vector3(0, 0, 0);

    const screenShotCamera = new PerspectiveCamera(50, width / height, 0.1, 1000);
    screenShotCamera.position.copy(cameraPosition);
    screenShotCamera.lookAt(cameraTarget);

    // Original State
    const controlsWereVisible = scene.transformControls.getHelper().visible;
    const originalSize = renderer.getSize(new Vector2());
    const originalPixelRatio = renderer.getPixelRatio();

    try {
        // Render scene at set aspect ratio
        renderer.setSize(width, height, false);
        renderer.setPixelRatio(1);

        // Hide unwanted stuff
        scene.disableGrid();
        scene.transformControls.getHelper().visible = false;

        // Render scene to ensure frame buffer is not empty
        renderer.render(scene, screenShotCamera);

        return new Promise((resolve) => {
            renderer.domElement.toBlob(
                    (blob: Blob | null) => resolve(blob),
                    `image/${imageType}`,
                    quality
            );
        });
    } finally {
        if (params.resetScene ?? true) {
            scene.enableGrid();
            scene.transformControls.getHelper().visible = controlsWereVisible;

            renderer.setSize(originalSize.x, originalSize.y, false);
            renderer.setPixelRatio(originalPixelRatio);

            screenShotCamera.clear?.();
        }
    }
}

/**
 * Sends a screenshot to be saved to the server.
 *
 * @param blob The screenshotted image
 * @param url url to post screenshot, eg: upload/image/model/${modelId} or `upload/image/design/${designId}
 */
export async function saveScreenshot(blob: Blob, url: string): Promise<void> {
    const file = new File([blob], 'thumbnail.jpg', {type: 'image/jpeg'});

    // Use FormData to create a multipart/form-data payload
    const formData = new FormData();
    formData.append('image', file); // "image" must match @RequestParam("image")

    const csrfHeader = CSRFProvider.getCsrfHeaderName();
    const csrfToken = CSRFProvider.getCsrfToken();

    const response = await fetch(url, {
        method: 'POST',
        headers: {
            [csrfHeader]: csrfToken
        },
        body: formData
    });

    if (!response.ok) {
        const error = await response.text();
        console.error('Upload failed:', error);
    }
}