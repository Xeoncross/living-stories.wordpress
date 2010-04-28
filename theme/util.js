/**
 * Toggles the display state of the given element. Also, toggles the state of the 
 * 'Read more' link between 'Read more' and 'Show less'.
 */
function showOrHide(elementId, readMoreId) {
  var element = document.getElementById(elementId);
  var readMore = document.getElementById(readMoreId);
  if (element != null) {
    if (element.style.display == 'block') {
      element.style.display = 'none';
      readMore.innerHTML = 'Read more';
    } else { 
      element.style.display = 'block';
      readMore.innerHTML = 'Show less';
    }
  }
}
