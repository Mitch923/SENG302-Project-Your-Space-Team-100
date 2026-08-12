import {PerspectiveCamera} from 'three';

export class EditorCamera extends PerspectiveCamera {
    constructor(fov: number, aspect: number, near: number, far: number) {
        super(fov, aspect, near, far);
        // Enable rendering of layer 0 (default) and layer 1 (for GridHelper)
        this.layers.enable(0); // Already enabled by default, but explicit for clarity
        this.layers.enable(2); // Enable layer 2 for debug
        this.reset(); // Set initial position
    }

    public reset(): void {
        this.position.x = 0;
        this.position.y = 5; // Adjusted for better GridHelper visibility
        this.position.z = 5;
        this.lookAt(0, 0, 0); // Ensure camera looks at the origin
        this.zoom = 1;
        this.updateProjectionMatrix();
    }
}