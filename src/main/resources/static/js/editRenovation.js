import {setRoomErrorMessage, validateRoom} from "./roomControls.js";

const segmenter = new Intl.Segmenter('en', {granularity: 'grapheme'});

/**
 * Creates a new room element for the given room summary
 *
 * @param roomSummary a summary of a room in the format { name: string, id: number, isDeletable: boolean }
 * @returns {HTMLDivElement} the room element created
 */
function createRoomElement(roomSummary) {
  const roomName = roomSummary.name;
  const roomDiv = document.createElement('div');
  roomDiv.className = 'room-item';
  roomDiv.classList.add('room-div')
  roomDiv.dataset.roomId = roomSummary.id;

  const nameSpan = document.createElement('label');
  nameSpan.textContent = roomName;
  nameSpan.className = 'room-name-span';

  if (roomSummary.isDeletable) {
    const deleteButton = document.createElement('button');
    deleteButton.className = 'remove-btn';
    deleteButton.textContent = '×';
    deleteButton.onclick = function () {
      const hiddenInputs = document.querySelectorAll('.room-input');
      const hiddenIdInputs = document.querySelectorAll('.room-id');
      let roomRemoved = false;
      hiddenInputs.forEach((input, index) => {
        if (input.value.replace(/!/g, ',') === roomName.replace(/!/g, ',')
            && !roomRemoved
            && input.dataset.roomId == roomSummary.id) { // use type coercion
          input.remove();
          hiddenIdInputs[index].remove();
          roomRemoved = true;
        }
      });
      roomDiv.remove();
    };
    roomDiv.appendChild(deleteButton);
  }

  roomDiv.appendChild(nameSpan);

  return roomDiv;
}

function countGraphemeClusters(str) {
  return [...segmenter.segment(str)].length;
}

function createHiddenFormInput(roomName, roomList, roomId) {
  const roomNameNoCommas = roomName.replace(/,/g, '!');
  const hiddenInput = document.createElement('input');
  hiddenInput.type = 'hidden';
  hiddenInput.name = 'roomNames';
  hiddenInput.value = roomNameNoCommas;
  hiddenInput.className = 'room-input';
  hiddenInput.dataset.roomId = roomId;
  roomList.appendChild(hiddenInput);

  const hiddenIdInput = document.createElement('input');
  hiddenIdInput.type = 'hidden';
  hiddenIdInput.name = 'roomIds';
  hiddenIdInput.value = roomId;
  hiddenIdInput.className = 'room-id'; // fake class name to allow easy selecting
  roomList.appendChild(hiddenIdInput);
}

document.addEventListener('DOMContentLoaded', function () {
  const addRoomButton = document.getElementById('addRoomButton');
  const roomNameInput = document.getElementById('roomName');
  const roomError = document.getElementById('roomError');
  const roomList = document.getElementById('roomList');
  const nameField = document.getElementById('name');
  const descriptionText = document.getElementById('description');

  rooms.forEach(room => {
    const roomElement = createRoomElement(room);
    roomList.appendChild(roomElement);
    createHiddenFormInput(room.name, roomList, room.id);
  });

  setRoomErrorMessage(roomNameInput, roomError);

  document.getElementById('charCount').textContent = countGraphemeClusters(
      descriptionText.value) + " / 512";
  descriptionText.addEventListener("keyup", function () {
    let textLength = countGraphemeClusters(descriptionText.value);
    document.getElementById('charCount').textContent = textLength + " / 512";
    if (textLength <= 512) {
      document.getElementById('description').classList.remove('is-invalid');
    }
  });

  function handleRoomSubmission(roomName) {
    if (roomName) {
      const error = validateRoom(roomName);
      if (error === undefined) {
        roomError.hidden = true;
        roomError.innerText = "";
        roomNameInput.classList.remove('is-invalid');
        const roomElement = createRoomElement({
          name: roomName,
          id: "null",
          isDeletable: true
        });
        roomList.appendChild(roomElement);
        createHiddenFormInput(roomName, roomList, null);
        roomNameInput.value = '';
      } else {
        roomNameInput.classList.add('is-invalid');
        roomError.innerText = error;
        roomError.hidden = false;
      }
    }
  }

  addRoomButton.addEventListener('click', function (e) {
    e.preventDefault();
    handleRoomSubmission(roomNameInput.value.trim());
  });

  roomNameInput.addEventListener('keydown', function (e) {
    if (e.key === 'Enter') {
      e.preventDefault();
      handleRoomSubmission(roomNameInput.value.trim());
    }
  });

  nameField.addEventListener('keydown', function (e) {
    nameField.classList.remove('is-invalid');
    if (e.key === 'Enter') {
      e.preventDefault()
      descriptionText.focus();
    }
  });
});