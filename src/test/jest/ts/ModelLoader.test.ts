jest.mock('three', () => {
    const actualThree = jest.requireActual('three');
    return {
        ...actualThree, // Mock only the scene
        Scene: jest.fn(() => ({
            add: jest.fn()
        }))
    };
});

jest.mock('three/examples/jsm/loaders/GLTFLoader', () => ({
    GLTFLoader: jest.fn(() => ({
        load: jest.fn(),
        parse: jest.fn()
    }))
}));

jest.mock('three/examples/jsm/loaders/OBJLoader', () => ({
    OBJLoader: jest.fn(() => ({
        load: jest.fn(),
        parse: jest.fn()
    }))
}));

jest.mock('three/examples/jsm/loaders/FBXLoader', () => ({
    FBXLoader: jest.fn(() => ({
        load: jest.fn(),
        parse: jest.fn()
    }))
}));

import {ModelLoader} from "../../../main/ts/ModelLoader";
import {BoxGeometry, Group, Mesh, MeshBasicMaterial, Object3D, Scene} from 'three';


describe('ModelLoader', () => {
    let modelLoader: ModelLoader;
    let mockScene: Scene;

    beforeEach(() => {
        mockScene = new Scene();
        modelLoader = new ModelLoader(mockScene);

        jest.clearAllMocks();
    });

    // Claude used to assist in writing the initial mocks and tests for the loadModel method
    describe('loadModel', () => {
        let loadGLTFSpy: jest.SpyInstance;
        let loadOBJSpy: jest.SpyInstance;

        beforeEach(() => {
            loadGLTFSpy = jest.spyOn(modelLoader as any, 'loadGLTF').mockResolvedValue(undefined);
            loadOBJSpy = jest.spyOn(modelLoader as any, 'loadOBJ').mockResolvedValue(undefined);
        });

        afterEach(() => {
            loadGLTFSpy.mockRestore();
            loadOBJSpy.mockRestore();
        });

        describe('GLTF file formats', () => {
            it('should call loadGLTF for .gltf files', async () => {
                const filePath = 'models/test-model.gltf';
                const modelName = 'Test Model';

                await modelLoader.loadModel(filePath, modelName);

                expect(loadGLTFSpy).toHaveBeenCalledWith(filePath, modelName);
                expect(loadGLTFSpy).toHaveBeenCalledTimes(1);
            });

            it('should call loadGLTF for .glb files', async () => {
                const filePath = 'models/test-model.glb';
                const modelName = 'Test GLB Model';

                await modelLoader.loadModel(filePath, modelName);

                expect(loadGLTFSpy).toHaveBeenCalledWith(filePath, modelName);
                expect(loadGLTFSpy).toHaveBeenCalledTimes(1);
            });

            it('should handle mixed case .GLB extension', async () => {
                const filePath = 'models/test-model.GlB';
                const modelName = 'Mixed Case GLB';

                await modelLoader.loadModel(filePath, modelName);

                expect(loadGLTFSpy).toHaveBeenCalledWith(filePath, modelName);
                expect(loadGLTFSpy).toHaveBeenCalledTimes(1);
            });
        });

        describe('OBJ file formats', () => {
            it('should call loadOBJ for .obj files', async () => {
                const filePath = 'models/test-model.obj';
                const modelName = 'Test OBJ Model';

                await modelLoader.loadModel(filePath, modelName);

                expect(loadOBJSpy).toHaveBeenCalledWith(filePath, modelName);
                expect(loadOBJSpy).toHaveBeenCalledTimes(1);
            });

            it('should handle uppercase .OBJ extension', async () => {
                const filePath = 'models/test-model.OBJ';
                const modelName = 'Uppercase OBJ';

                await modelLoader.loadModel(filePath, modelName);

                expect(loadOBJSpy).toHaveBeenCalledWith(filePath, modelName);
                expect(loadOBJSpy).toHaveBeenCalledTimes(1);
            });
        });

        describe('unsupported file formats', () => {
            it('should reject with error for unsupported file extensions', async () => {
                const filePath = 'models/test-model.fbx';
                const modelName = 'Unsupported Model';

                await expect(modelLoader.loadModel(filePath, modelName))
                .rejects
                .toThrow('Unsupported file format: models/test-model.fbx');

                expect(loadGLTFSpy).not.toHaveBeenCalled();
                expect(loadOBJSpy).not.toHaveBeenCalled();
            });

            it('should reject for files with no extension', async () => {
                const filePath = 'models/test-model';
                const modelName = 'No Extension Model';

                await expect(modelLoader.loadModel(filePath, modelName))
                .rejects
                .toThrow('Unsupported file format: models/test-model');
            });

            it('should handle file paths ending with dot but no extension', async () => {
                const filePath = 'models/test-model.';
                const modelName = 'Dot No Extension';

                await expect(modelLoader.loadModel(filePath, modelName))
                .rejects
                .toThrow('Unsupported file format: models/test-model.');
            });

            it('should handle empty file paths', async () => {
                const filePath = '';
                const modelName = 'Empty Path';

                await expect(modelLoader.loadModel(filePath, modelName))
                .rejects
                .toThrow('Unsupported file format: ');
            });
        });

        describe('error handling', () => {
            it('should propagate OBJ loading errors', async () => {
                const error = new Error('OBJ loading failed');
                loadOBJSpy.mockRejectedValue(error);

                const filePath = 'models/broken.obj';
                const modelName = 'Broken OBJ';

                await expect(modelLoader.loadModel(filePath, modelName))
                .rejects
                .toThrow('OBJ loading failed');
            });

            it('should propagate GLTF loading errors', async () => {
                const error = new Error('GLTF loading failed');
                loadGLTFSpy.mockRejectedValue(error);

                const filePath = 'models/broken.gltf';
                const modelName = 'Broken GLTF';

                await expect(modelLoader.loadModel(filePath, modelName))
                .rejects
                .toThrow('GLTF loading failed');
            });
        });

        describe('loaders return promises that resolve', () => {
            it('should resolve when GLTF loading succeeds', async () => {
                const filePath = 'models/success.gltf';
                const modelName = 'Success GLTF';

                const result = await modelLoader.loadModel(filePath, modelName);

                expect(result).toBeUndefined();
                expect(loadGLTFSpy).toHaveBeenCalledWith(filePath, modelName);
            });

            it('should resolve when OBJ loading succeeds', async () => {
                const filePath = 'models/success.obj';
                const modelName = 'Success OBJ';

                const result = await modelLoader.loadModel(filePath, modelName);

                expect(result).toBeUndefined();
                expect(loadOBJSpy).toHaveBeenCalledWith(filePath, modelName);
            });
        });
    });

    describe("LoadModelFromFile", () => {
        let loadOBJFromBufferSpy: jest.SpyInstance;
        let loadGLBFromBufferSpy: jest.SpyInstance;
        let loadFBXFromBufferSpy: jest.SpyInstance;

        beforeEach(() => {
            loadGLBFromBufferSpy = jest.spyOn(modelLoader as any, 'loadGLBFromBuffer').mockResolvedValue(undefined);
            loadOBJFromBufferSpy = jest.spyOn(modelLoader as any, 'loadOBJFromBuffer').mockResolvedValue(undefined);
            loadFBXFromBufferSpy = jest.spyOn(modelLoader as any, 'loadFBXFromBuffer').mockResolvedValue(undefined);
        });

        afterEach(() => {
            loadGLBFromBufferSpy.mockRestore()
            loadOBJFromBufferSpy.mockRestore()
            loadFBXFromBufferSpy.mockRestore()
        });

        describe('GLB file formats', () => {
            it("should call loadGLBFromBuffer for files with filename with .glb extension", async () => {
                const content = 'Some super interesting file content here';
                const file = new File([content], 'test.glb');

                await modelLoader.loadModelFromFile(file, file.name);

                expect(loadGLBFromBufferSpy).toHaveBeenCalledWith(expect.any(ArrayBuffer), file.name);
                expect(loadGLBFromBufferSpy).toHaveBeenCalledTimes(1);
            });

            it("should call loadGLBFromBuffer for files with filename with mixed case .GlB extension", async () => {
                const content = 'Some super interesting file content here';
                const file = new File([content], 'test.GlB');

                await modelLoader.loadModelFromFile(file, file.name);
                const expectedName = "test.glb"

                expect(loadGLBFromBufferSpy).toHaveBeenCalledWith(expect.any(ArrayBuffer), expectedName);
                expect(loadGLBFromBufferSpy).toHaveBeenCalledTimes(1);
            });

        })

        describe('OBJ file formats', () => {
            it("should call loadOBJFromBufferSpy for files with filename with .obj extension", async () => {
                const content = 'My obj file here';
                const file = new File([content], 'test.obj');

                await modelLoader.loadModelFromFile(file, file.name);

                expect(loadOBJFromBufferSpy).toHaveBeenCalledWith(expect.any(ArrayBuffer), file.name);
                expect(loadOBJFromBufferSpy).toHaveBeenCalledTimes(1);
            });

            it("should call loadOBJFromBufferSpy for files with filename with mixed case .oBJ extension", async () => {
                const content = 'Some super interesting file content here';
                const file = new File([content], 'test.oBJ');

                await modelLoader.loadModelFromFile(file, file.name);
                const expectedName = "test.obj"

                expect(loadOBJFromBufferSpy).toHaveBeenCalledWith(expect.any(ArrayBuffer), expectedName);
                expect(loadOBJFromBufferSpy).toHaveBeenCalledTimes(1);
            });
        })

        describe('FBX file formats', () => {
            it("should call loadFBXFromBufferSpy for files with filename with .fbx extension", async () => {
                const content = 'My crazy fbx file here';
                const file = new File([content], 'test.fbx');

                await modelLoader.loadModelFromFile(file, file.name);

                expect(loadFBXFromBufferSpy).toHaveBeenCalledWith(expect.any(ArrayBuffer), file.name);
                expect(loadFBXFromBufferSpy).toHaveBeenCalledTimes(1);
            });

            it("should call loadFBXFromBufferSpy for files with filename with mixed case .FBx extension", async () => {
                const content = 'Some super interesting file content here';
                const file = new File([content], 'test.Fbx');

                await modelLoader.loadModelFromFile(file, file.name);
                const expectedName = "test.fbx"

                expect(loadFBXFromBufferSpy).toHaveBeenCalledWith(expect.any(ArrayBuffer), expectedName);
                expect(loadFBXFromBufferSpy).toHaveBeenCalledTimes(1);
            });
        });

        describe('unsupported file formats and invalid extensions', () => {
            it('should reject with error for unsupported file extensions', async () => {
                const content = 'Some super interesting file content here';
                const file = new File([content], 'test.jpg');

                await expect(modelLoader.loadModelFromFile(file, file.name))
                .rejects
                .toThrow(`Unsupported file format: ${file.name}`);

                expect(loadGLBFromBufferSpy).not.toHaveBeenCalled();
                expect(loadOBJFromBufferSpy).not.toHaveBeenCalled();
                expect(loadFBXFromBufferSpy).not.toHaveBeenCalled();
            });

            it('should reject for files with no extension', async () => {
                const content = 'Some super interesting file content here';
                const file = new File([content], 'test');

                await expect(modelLoader.loadModelFromFile(file, file.name))
                .rejects
                .toThrow(`Unsupported file format: ${file.name}`);
            });

            it('should handle file paths ending with dot but no extension', async () => {
                const content = 'Some super interesting file content here';
                const file = new File([content], 'test.');

                await expect(modelLoader.loadModelFromFile(file, file.name))
                .rejects
                .toThrow(`Unsupported file format: ${file.name}`);
            });

            it('should handle empty file name', async () => {
                const content = 'Some super interesting file content here';
                const file = new File([content], '');

                await expect(modelLoader.loadModelFromFile(file, file.name))
                .rejects
                .toThrow(`Unsupported file format: ${file.name}`);
            });
        });

    });

    describe('processLoadedModel', () => {
        let loadedObject: Object3D;

        beforeEach(() => {
            loadedObject = new Mesh(
                    new BoxGeometry(1, 2, 1),
                    new MeshBasicMaterial()
            );
            loadedObject.position.set(0, 0, 0);
            loadedObject.scale.set(1, 1, 1);
        });

        it('should scale the model to a target height of 2.5', () => {
            modelLoader['processLoadedModel'](loadedObject, 'Mikey Abromwitz');

            const expectedScale = 2.5 / 2;
            expect(loadedObject.scale.x).toBeCloseTo(expectedScale);
            expect(loadedObject.scale.y).toBeCloseTo(expectedScale);
            expect(loadedObject.scale.z).toBeCloseTo(expectedScale);
        });

        it('should offset the model so the bottom of the object is at y = 0', () => {
            modelLoader['processLoadedModel'](loadedObject, 'Reggie Belafonte');
            expect(loadedObject.position.y).toBeCloseTo(1.25);
        });

        it('should add the movable displayName and scale properties to the user data and add to the scene', () => {
            modelLoader['processLoadedModel'](loadedObject, 'Zeke');

            expect(mockScene.add).toHaveBeenCalledTimes(1);
            const addedGroup = (mockScene.add as jest.Mock).mock.calls[0][0] as Group;

            expect(addedGroup).toBeInstanceOf(Group);
            expect(addedGroup.userData.movable).toBe(true);
            expect(addedGroup.userData.displayName).toBe('Zeke');
            expect(addedGroup.userData.scale).toBe(1);
        });

        it('should traverse all children and add the groupUUID as the parentGroup parameter of the user data', () => {
            const child = new Mesh(new BoxGeometry(1, 1, 1));
            loadedObject.add(child);

            modelLoader['processLoadedModel'](loadedObject, 'Cody Maverick');

            const group = (mockScene.add as jest.Mock).mock.calls[0][0] as Group;
            const groupUUID = group.name;

            expect(child.userData.parentGroup).toBe(groupUUID);
            expect(loadedObject.userData.parentGroup).toBe(groupUUID);
        });
    });

});