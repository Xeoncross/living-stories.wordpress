<?php
/*
Plugin Name: Living Stories Custom Metadata
*/

register_activation_hook(__FILE__, 'install');

function install() {
  require_once(dirname(__FILE__) . '/../magic-fields/RCCWP_CustomWritePanel.php');

  // Create default write pages
  $existing_write_panels = array();
  foreach (RCCWP_CustomWritePanel::GetCustomWritePanels() as $panel) {
    array_push($existing_write_panels, $panel->name);
  }
  $panels_dir_name = dirname(__FILE__) . '/panels/';
  $panels_dir = opendir($panels_dir_name);
  $panels = array();
  while (false !== ($panel_file = readdir($panels_dir))) {
    $panel_name = basename($panel_file, ".pnl");
    if (!is_dir($panel_file) && $panel_name && $panel_name != $panel_file && !in_array($panel_name, $existing_write_panels)) {
      // If this file isn't a directory, ends with .pnl, and isn't already an existing panel, create and store it for import.
      // $panel_id = RCCWP_CustomWritePanel::Create($panel_name, '', array(), array(), 1, "post", false);
      RCCWP_CustomWritePanel::Import($panels_dir_name . $panel_file);
      $panels[$panel_name] = $panel_file;
    }
  }

  foreach ($panels as $panel_name => $panel_file) {
    RCCWP_CustomWritePanel::Import($panels_dir_name . $panel_file, $panel_name, true);
  }
}

/* Use the admin_init action to inject our GWT code */
add_action('admin_init', 'lsp_add_scripts');

/* Use the admin_menu action to define the custom boxes */
add_action('admin_menu', 'lsp_add_custom_meta');

/* Use the save_post action to do something with the data entered */
add_action('save_post', 'lsp_save_meta');

/* Adds a script tag referencing the GWT code used by this plugin */
function lsp_add_scripts() {
  wp_enqueue_script('contentmanager', WP_PLUGIN_URL . '/living-story-plugin/LivingStoryPropertyManagerPlugin.nocache.js');
  wp_enqueue_script('living_story_plugin', WP_PLUGIN_URL . '/living-story-plugin/living_stories.js', array('jquery'));
}

/* Adds a custom section to the "advanced" Post and Page edit screens */
function lsp_add_custom_meta() {
  add_meta_box('lsp_custom_meta', __('Living Story Content Attributes', 'lsp_textdomain'),
      'lsp_create_html', 'post', 'advanced');
}
   
// Constants for metadata keys.
define('IMPORTANCE', 'Importance');
define('LATITUDE', 'Latitude');
define('LONGITUDE', 'Longitude');
define('LOCATION_DESCRIPTION', 'Location_description');

/* Prints the inner fields for the custom post/page section */
function lsp_create_html() {
  // Use nonce for verification
  echo '<input type="hidden" name="lsp_noncename" id="lsp_noncename" value="' . 
      wp_create_nonce(plugin_basename(__FILE__)) . '" />';

  // Get the post for this page
  global $post;
  $postId = $post->ID;
  $importance = get_post_meta($postId, IMPORTANCE, True);
  $latitude = get_post_meta($postId, LATITUDE, True);
  $longitude = get_post_meta($postId, LONGITUDE, True);
  $location_description = get_post_meta($postId, LOCATION_DESCRIPTION, True);
  
  // Create a content metadata javascript object that passes the current metadata values
  // to our gwt widgets.
  // If you change the keys in this object, remember to change the corresponding key defined
  // in ContentItemData.java, as well as the javascript object created on the test page
  // (LivingStoryPropertyManagerPlugin.html).
  echo <<< END
<script type="text/javascript">
  var CONTENT_METADATA = {
    "importance" : "$importance",
    "latitude" : "$latitude",
    "longitude" : "$longitude",
    "location_description" : "$location_description"
  };
</script>
END;

  // The actual fields for data entry
  echo '<div id="contentManagerBox"></div>';
}

/* When the post is saved, saves our custom data */
function lsp_save_meta($post_id) {
  // verify this came from our plugin and with proper authorization,
  // because save_post can be triggered at other times

  if (!wp_verify_nonce( $_POST['lsp_noncename'], plugin_basename(__FILE__))) {
    return $post_id;
  }

  // verify if this is an auto save routine. If it is our form has not been submitted, so we dont want
  // to do anything
  if (defined('DOING_AUTOSAVE') && DOING_AUTOSAVE) 
    return $post_id;

  
  // Check permissions
  if ('page' == $_POST['post_type']) {
    if (!current_user_can( 'edit_page', $post_id ))
      return $post_id;
  } else {
    if (!current_user_can( 'edit_post', $post_id ))
      return $post_id;
  }

  // OK, we're authenticated: save the metadata
  update_post_meta($post_id, IMPORTANCE, $_POST['lsp_importance']);
  update_post_meta($post_id, LATITUDE, $_POST['lsp_latitude']);
  update_post_meta($post_id, LONGITUDE, $_POST['lsp_longitude']);
  update_post_meta($post_id, LOCATION_DESCRIPTION, $_POST['lsp_location_description']);
}
?>
