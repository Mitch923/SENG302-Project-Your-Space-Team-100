import {
    createHiddenElement,
    createRoomElement,
    setRoomErrorMessage,
    validateRoom
} from "./roomControls.js";

/**
 * Method pass to the createRoomElement function to determine the behaviour when
 * the delete button on an individual room is clicked. Removes the element from
 * the list of hidden inputs and removes the visual HTML element from the onscreen
 * list.
 * @param roomName name of the room that was clicked
 * @param roomDiv HTML div element that contains the delete button clicked
 */
function onRoomDelete(roomName, roomDiv) {
    const hiddenInputs = document.querySelectorAll('.room-input');
    let roomDeleted = false;
    hiddenInputs.forEach(input => {
        if (input.value.replace(/!/g, ',') === roomName.replace(/!/g, ',')
                && !roomDeleted) {
            input.remove();
            roomDeleted = true;
        }
    });
    roomDiv.remove();
}

window.onRoomDelete = onRoomDelete;

document.addEventListener('DOMContentLoaded', function () {
    const addRoomButton = document.getElementById('addRoomButton');
    const roomNameInput = document.getElementById('roomName');
    const roomError = document.getElementById('roomError');
    const roomList = document.getElementById('roomList');
    const descriptionText = document.getElementById('description');
    const nameField = document.getElementById('name');

    setRoomErrorMessage(roomNameInput, roomError);

    descriptionText.addEventListener("keyup", function () {
        let textLength = descriptionText.value.length;
        document.getElementById('charCount').textContent = textLength
                + " / 512";
    });

    addRoomButton.addEventListener('click', function (e) {
        e.preventDefault();
        const roomName = roomNameInput.value.trim();

        if (roomName) {
            const error = validateRoom(roomName);
            if (error === undefined) {
                roomError.innerText = "";
                roomError.hidden = true;
                roomNameInput.classList.remove('is-invalid');

                const roomElement = createRoomElement(roomName, onRoomDelete);
                roomList.appendChild(roomElement);

                const hiddenInput = createHiddenElement(roomName);
                roomList.appendChild(hiddenInput);

                roomNameInput.value = '';
            } else {
                roomNameInput.classList.add('is-invalid');
                roomError.innerText = error;
                roomError.hidden = false
            }
        }
    });

    roomNameInput.addEventListener('keydown', function (e) {
        if (e.key === 'Enter') {
            e.preventDefault();
            const roomName = roomNameInput.value.trim();
            if (roomName) {
                const error = validateRoom(roomName);
                if (error === undefined) {
                    roomError.hidden = true;
                    roomError.innerText = "";
                    roomNameInput.classList.remove('is-invalid');

                    const roomElement = createRoomElement(roomName,
                            onRoomDelete);
                    roomList.appendChild(roomElement);

                    const hiddenInput = createHiddenElement(roomName);
                    roomList.appendChild(hiddenInput);
                    roomNameInput.value = '';
                } else {
                    roomNameInput.classList.add('is-invalid');
                    roomError.innerText = error;
                    roomError.hidden = false
                }
            }
        }
    })

    nameField.addEventListener('keydown', function (e) {
        if (e.key === 'Enter') {
            e.preventDefault()
            descriptionText.focus();
        }
    });
});