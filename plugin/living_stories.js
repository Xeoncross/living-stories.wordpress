/**
 * Scripts used by the living stories plugin.
 */

var LivingStoryPlugin = {};

LivingStoryPlugin.load = function() {

  var postexcerpt = document.getElementById('postexcerpt');
  var postdiv = document.getElementById('postdivrich');
  if (postexcerpt && postdiv) {
    postexcerpt.parentNode.removeChild(postexcerpt);
    postdiv.parentNode.insertBefore(postexcerpt, postdiv);
    tme_convertExcerpt();
  }
}

jQuery(document).ready(LivingStoryPlugin.load);
