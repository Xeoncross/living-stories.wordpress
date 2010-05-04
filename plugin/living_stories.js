/**
 * Scripts used by the living stories plugin.
 */

var LivingStoryPlugin = {};

LivingStoryPlugin.load = function() {
  // TODO: this breaks the editor, so it's commented out for now.
  // The problem is: we aren't always able to do this before the
  // tinyMCE excerpt editor is created.  As a result, when we
  // move the node, it loses all the event handlers and the
  // editor becomes dead.  If we call tme_convertExcerpt() a
  // second time, it screws up the editor and makes it so that it doesn't
  // save text properly :(
  /*
  var postexcerpt = document.getElementById('postexcerpt');
  var postdiv = document.getElementById('postdivrich');
  if (postexcerpt && postdiv) {
    postexcerpt.parentNode.removeChild(postexcerpt);
    postdiv.parentNode.insertBefore(postexcerpt, postdiv);
    tme_convertExcerpt();
  }
  */
}

// jQuery(document).ready(LivingStoryPlugin.load);
