import * as THREE from 'three';
import {EditorScene} from "./editor-scene";
import {closeLoadingBar} from "@/edit-design-page-controls";

// Materials for the walls and floor
const floorMaterial = new THREE.MeshStandardMaterial({color: 0x7e7e7e});
const backWallMaterial = new THREE.MeshStandardMaterial({color: 0xffffff});
const leftWallMaterial = new THREE.MeshStandardMaterial({color: 0xffffff});

const roomHeight = 8;
const wallThickness = 0.1;
const floorThickness = 0.1;

// === Room Dimensions ===
let roomWidth: number;
let roomDepth: number;

/**
 * Create the floor object.
 * @param scene EditorScene
 */
function addFloor(scene: EditorScene) {
    const floor = new THREE.Mesh(new THREE.BoxGeometry(roomWidth, floorThickness, roomDepth), floorMaterial);
    floor.position.y = 0 - (floorThickness / 2);
    floor.userData.displayName = "Floor";
    floor.userData.colour = "#7e7e7e";
    floor.receiveShadow = true;
    scene.add(floor);
}

/**
 * Create and position the back wall object.
 * @param scene EditorScene
 */
function addBackWall(scene: EditorScene) {
    const backWall = new THREE.Mesh(new THREE.BoxGeometry(roomWidth, roomHeight, wallThickness), backWallMaterial);
    backWall.position.set(0, roomHeight / 2 - (floorThickness / 2), -roomDepth / 2);
    backWall.userData.displayName = "Wall";
    backWall.userData.colour = "#ffffff";
    backWall.receiveShadow = true;
    scene.add(backWall);
}

/**
 * Create and position the left wall object.
 * @param scene EditorScene
 */
function addLeftWall(scene: EditorScene) {
    const leftWall = new THREE.Mesh(new THREE.BoxGeometry(wallThickness, roomHeight, roomDepth), leftWallMaterial);
    leftWall.position.set(-roomWidth / 2, roomHeight / 2 - (floorThickness / 2), 0);
    leftWall.userData.displayName = "Wall";
    leftWall.userData.colour = "#ffffff";
    leftWall.receiveShadow = true;
    scene.add(leftWall);
}

/**
 * Adds walls and floor to the scene in the given dimensions.
 * @param scene EditorScene
 * @param width Room width
 * @param depth Room depth
 */
function createDefaultRoom(scene: EditorScene, width: number, depth: number) {
    roomWidth = width;
    roomDepth = depth;
    addFloor(scene);
    addBackWall(scene);
    addLeftWall(scene);
}

/**
 * Create all the default objects and add them to the scene
 *
 * @param scene the scene to add the objects to
 */
async function createDefaultScene(scene: EditorScene) {
    try {
        createDefaultRoom(scene, 20, 15);

        // Add a default light
        console.log("Default scene loaded.");
        closeLoadingBar();
    } catch (err) {
        console.error(err);
    }
}

export {createDefaultScene};
