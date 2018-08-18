import * as events from './events.js';
import smoothscroll from 'smoothscroll-polyfill';

smoothscroll.polyfill();

function isInViewport(elem) {
  var bounding = elem.getBoundingClientRect();
  return (
    bounding.top >= 0 &&
    bounding.left >= 0 &&
    bounding.bottom <= (window.innerHeight || document.documentElement.clientHeight) &&
    bounding.right <= (window.innerWidth || document.documentElement.clientWidth)
  );
}

export function toSelected() {
  var element = events.getSelectedElement();
  if (element) {
    if (element.scrollIntoViewIfNeeded) {
      element.scrollIntoViewIfNeeded({block: 'center', inline: 'center'});
    } else if (!isInViewport(element)) {
      element.scrollIntoView({block: 'center', inline: 'center'});
    }
  }
}
