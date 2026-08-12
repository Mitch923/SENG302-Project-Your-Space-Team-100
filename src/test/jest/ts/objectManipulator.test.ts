import {snapObjectToFloor} from "../../../main/ts/objectcontrols/objectManipulator";
import {Box3, Object3D, Vector3} from "three";

const testObjectData = [
    [new Vector3(1, 8, 0), new Vector3(1, 7, 9), new Vector3(1, 1, 2), new Vector3(1,1.5,0)],
    [new Vector3(2, 4, 5), new Vector3(3, 6, 1), new Vector3(0, 2, 8), new Vector3(2,-1,5)],
    [new Vector3(9, 3, 3), new Vector3(4, 4, 4), new Vector3(7, 2, 6), new Vector3(9,0,3)],
    [new Vector3(5, 5, 5), new Vector3(8, 0, 1), new Vector3(6, 7, 3), new Vector3(5,8.5,5)],
    [new Vector3(3, 9, 0), new Vector3(2, 2, 2), new Vector3(1, 0, 9), new Vector3(3,7,0)],
];

const objectManipulator = require("../../../main/ts/objectcontrols/objectManipulator")

const boxCenter = new Vector3();
const boxSize = new Vector3();

const mockSetFrom = jest.spyOn(Box3.prototype, 'setFromObject').mockImplementation(() => {
    return new Box3().setFromCenterAndSize(boxCenter, boxSize);
})

test('snapObjectToFloor(), valid input, modifies object correctly', () => {

    testObjectData.forEach((data: Vector3[]) => {
        const object: Object3D = new Object3D();
        object.position.x = data[0].x;
        object.position.y = data[0].y;
        object.position.z = data[0].z;

        boxCenter.copy(data[1]);
        boxSize.copy(data[2]);

        snapObjectToFloor(object);

        expect(object.position).toStrictEqual(data[3]);
    })
    expect(mockSetFrom).toHaveBeenCalledTimes(testObjectData.length);
})