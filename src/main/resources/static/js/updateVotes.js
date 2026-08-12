const designId = document.getElementById("designId").value;
document.getElementById(`vote-button-checkbox`).addEventListener(
        'click', () => {
            vote();

        })

/**
 * Converts a boolean string in to a boolean value
 * @param strBool ("true" or "false")
 * @returns {boolean} true or false
 */
function toBool(strBool) {
    return strBool === "true";
}

function toggleVoteVisuals() {
    const unfilledHeart = document.getElementById("vote-button-heart")
    const filledHeart = document.getElementById("vote-button-full-heart")
    const voteButtonCheckbox = document.getElementById("vote-button-checkbox")
    const voteButton = document.getElementById("vote-button")
    const voteCount = document.getElementById("vote-count")

    let voted = !toBool(voteButton.dataset.vote);
    voteButton.dataset.vote = voted;
    const currentValue = Number(voteCount.textContent);
    if (voted) {
        filledHeart.removeAttribute('hidden')
        unfilledHeart.setAttribute('hidden', 'hidden');
        voteCount.textContent = String(currentValue + 1);
    } else {
        filledHeart.setAttribute('hidden', 'hidden');
        unfilledHeart.removeAttribute('hidden');
        voteCount.textContent = String(currentValue - 1);
    }
}

async function vote() {
    const csrfToken = document.querySelector('meta[name="_csrf"]').getAttribute(
            "content");
    const csrfHeader = document.querySelector(
            'meta[name="_csrf_header"]').getAttribute("content");

    const fullUrl = `competitionEntry/${designId}/vote`;
    const response = await fetch(fullUrl, {
        method: "POST",
        headers: {
            [csrfHeader]: csrfToken
        }
    }).then((res) => {
        if (res.ok) {
            toggleVoteVisuals();
        }
    });
}



