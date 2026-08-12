/* This js programmed with the help of ChatGPT */
document.addEventListener("DOMContentLoaded", () => {
  const textElements = document.querySelectorAll('[id^="truncatedText-"]');

  textElements.forEach(textEl => {
    /* Initialises ids and buttons */
    const uid = textEl.id.replace("truncatedText-", "");
    const buttonEl = document.getElementById("showMoreBtn-" + uid);

    /* Show 'Show more...' button if the text height is greater than the <p> height */
    if (textEl.scrollHeight > textEl.clientHeight) {
      buttonEl.style.display = "inline";
    }

    /* On click show the rest of the text element */
    buttonEl?.addEventListener("click", () => {
      textEl.style.lineClamp = null;
      textEl.style.display = "block";
      buttonEl.style.display = "none";
    });
  });
});