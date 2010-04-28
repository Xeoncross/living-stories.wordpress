<?php
/**
 * @package WordPress
 * @subpackage Default_Theme
 */
?>

<?php
  define("POSTS_PER_PAGE", 2);

  function create_query($params, $page = null) {
    $args = array();

    global $wpdb;
    $query = "SELECT P.* FROM $wpdb->posts P ";
    if ($params['cat_id']) {
      $query .= "INNER JOIN $wpdb->term_taxonomy T ON T.term_id = %d "
          . "INNER JOIN $wpdb->term_relationships C ON "
          . "C.term_taxonomy_id = T.term_taxonomy_id AND C.object_id = P.ID ";
      $args[] = $params['cat_id'];
    }
    if ($params['panel_id']) {
      $panel_ids = $params['panel_id'];
      $placeholders = join(",", array_fill(0, count($panel_ids), "%s"));
      $query .= "INNER JOIN $wpdb->postmeta PM ON "
          . "PM.post_id = P.ID AND PM.meta_key = '" . RC_CWP_POST_WRITE_PANEL_ID_META_KEY . "' AND PM.meta_value IN ($placeholders) ";
      $args = array_merge($args, $panel_ids);
    }
    if ($params['linked_post_id']) {
      $query .= "INNER JOIN $wpdb->postmeta PM2 ON "
          . "PM2.post_id = P.ID AND PM2.meta_key LIKE 'Linked_%%' AND PM2.meta_value = %s ";
      $args[] = $params['linked_post_id'];
    }
    if ($params['importance']) {
      $query .= "INNER JOIN $wpdb->postmeta PM3 ON "
          . "PM3.post_id = P.ID AND PM3.meta_key = 'Importance' AND PM3.meta_value = 'HIGH' ";
    }

    $query .= "WHERE P.post_status = 'publish' ";

    if ($params['chronological']) {
      $query .= 'ORDER BY P.post_date ASC ';
    } else {
      $query .= 'ORDER BY P.post_date DESC ';
    }

    if ($page != null) {
      // Limit the number of results.  Return 11 even though we're only showing 10
      // so that we know whether or not to show the 'read more' link.
      $offset = POSTS_PER_PAGE * $page;
      $query .= "LIMIT " . (POSTS_PER_PAGE + 1) . " OFFSET $offset";
    }

    $query = $wpdb->prepare($query, $args);

    return $query;
  }

  function create_content_item(&$all_linked_items = null) {
    $panel_name = get_panel_name();
    $content_item = array();
    $content_item['id'] = get_the_ID();
    $content_item['panelName'] = $panel_name;
    $content_item['timestamp'] = get_the_time('Y-m-d H:i a');
    $coauthors = get_coauthors();
    $authors_count = count($coauthors);
    // Code equivalent to coauthors_posts_links(), but put into a variable
    // instead of rolling our own.
    $authors_string = '';
    for ($i = 0; $i < $authors_count; $i++) {
      $coauthor = $coauthors[$i];
      if (isset($coauthor->first_name) && isset($coauthor->last_name)) {
        $fullname = $coauthor->first_name . ' ' . $coauthor->last_name;
      } else {
        $fullname = $coauthor->nickname;
      }
      $authors_string .= sprintf('<a href="%s" title="Posts by %s">%s</a>',
                                 get_author_posts_url($coauthor->ID),
                                 $fullname, $fullname);

      if ($i < $authors_count - 2) {
        $authors_string .= ', ';
      } else if ($i == $authors_count - 2) {
        $authors_string .= ' and ';
      }
    }
    $content_item['authorsString'] = $authors_string;
    $content_item['authorsCount'] = $authors_count;
    $content_item['importance'] = get_post_meta(get_the_ID(), 'Importance', true);
    $content_item['latitude'] = get_post_meta(get_the_ID(), 'Latitude', true);
    $content_item['longitude'] = get_post_meta(get_the_ID(), 'Longitude', true);
    $content_item['locationDescription'] = get_post_meta(get_the_ID(), 'Location_description', true);

    if ($panel_name == 'events') {
      $content_item['eventDate'] = get_post_meta(get_the_ID(), 'Event_date', true);
      $content_item['eventTime'] = get_post_meta(get_the_ID(), 'Event_time', true);
      $content_item['update'] = get_the_title();
      $content_item['summary'] = has_excerpt() ? get_the_excerpt() : '';
      $content_item['content'] = get_the_content();
    } else if ($panel_name == 'narratives') {
      $content_item['narrativeType'] = get_post_meta(get_the_ID(), 'Narrative_type', true);
      $content_item['narrativeDate'] = get_post_meta(get_the_ID(), 'Narrative_date', true);
      $content_item['narrativeTime'] = get_post_meta(get_the_ID(), 'Narrative_time', true);
      $content_item['update'] = get_the_title();
      $content_item['summary'] = get_the_excerpt();
      $content_item['content'] = get_the_content();
    } else if ($panel_name == 'players') {
      $content_item['playerName'] = get_the_title();
      $content_item['playerAliases'] = get_post_meta(get_the_ID(), 'Player_Aliases', true);
      $content_item['playerType'] = get_post_meta(get_the_ID(), 'Player_type', true);
      $content_item['playerPhoto'] = get_post_meta(get_the_ID(), 'Player_Photo', true);
      $content_item['content'] = get_the_content();
    } else if ($panel_name == 'images') {
      $content_item['assetType'] = 'IMAGE';
      $content_item['imageUrl'] = full_image_path(get_post_meta(get_the_ID(), 'Full_Image', true));
      $content_item['caption'] = get_post_meta(get_the_ID(), 'Image_Caption', true);
      $content_item['previewUrl'] = full_image_path(get_post_meta(get_the_ID(), 'Image_Thumbnail', true));
    } else if ($panel_name == 'videos') {
      $content_item['assetType'] = 'VIDEO';
      $content_item['content'] = get_post_meta(get_the_ID(), 'Video_content', true);
      $content_item['caption'] = get_post_meta(get_the_ID(), 'Video_Caption', true);
      $content_item['previewUrl'] = full_image_path(get_post_meta(get_the_ID(), 'Video_Thumbnail', true));
    } else if ($panel_name == 'audio') {
      $content_item['assetType'] = 'AUDIO';
      $content_item['content'] = get_post_meta(get_the_ID(), 'Audio_content', true);
      $content_item['caption'] = get_post_meta(get_the_ID(), 'Audio_caption', true);
    } else if ($panel_name == 'graphics') {
      $content_item['assetType'] = 'INTERACTIVE';
      $content_item['content'] = get_post_meta(get_the_ID(), 'Graphic_content', true);
      $content_item['caption'] = get_post_meta(get_the_ID(), 'Graphic_caption', true);
      $content_item['previewUrl'] = full_image_path(get_post_meta(get_the_ID(), 'Graphic_thumbnail', true));
    } else if ($panel_name == 'resources') {
      $content_item['assetType'] = 'LINK';
      $content_item['content'] = get_post_meta(get_the_ID(), 'Resource_content', true);
    } else if ($panel_name == 'documents') {
      $content_item['assetType'] = 'DOCUMENT';
      $content_item['caption'] = get_post_meta(get_the_ID(), 'Document_caption', true);
      $content_item['content'] = get_post_meta(get_the_ID(), 'Document_content', true);
    } else if ($panel_name == 'background') {
      $content_item['conceptName'] = get_post_meta(get_the_ID(), 'Concept_name', true);
      $content_item['content'] = get_the_content();
    } else if ($panel_name == 'quotes' || $panel_name == 'facts' || $panel_name == 'reactions') {
      $content_item['content'] = get_the_content();
    } 
      
    // Add the linked item ids - the same custom field names are maintained for all write panels, 
    // so that this action can remain the same for all of them.
    if (!is_null($all_linked_items)) {
      $linked_items = array();
      $linked_narratives = get_post_meta(get_the_ID(), 'Linked_narratives');
      // the get_post_meta method returns an array with an empty string if the key is not found
      if (!empty($linked_narratives[0])) {
        $linked_items = array_merge($linked_items, $linked_narratives);
      }
      $linked_background = get_post_meta(get_the_ID(), 'Linked_background');
      if (!empty($linked_background[0])) {
        $linked_items = array_merge($linked_items, $linked_background);
      }
      $linked_players = get_post_meta(get_the_ID(), 'Linked_players');
      if (!empty($linked_players[0])) {
        $linked_items = array_merge($linked_items, $linked_players);
      }
      $linked_images = get_post_meta(get_the_ID(), 'Linked_images');
      if (!empty($linked_images[0])) {
        $linked_items = array_merge($linked_items, $linked_images);
      }
      $linked_videos = get_post_meta(get_the_ID(), 'Linked_videos');
      if (!empty($linked_videos[0])) {
        $linked_items = array_merge($linked_items, $linked_videos);
      }
      $linked_audio = get_post_meta(get_the_ID(), 'Linked_audio');
      if (!empty($linked_audio[0])) {
        $linked_items = array_merge($linked_items, $linked_audio);
      }
      $linked_graphics = get_post_meta(get_the_ID(), 'Linked_graphics');
      if (!empty($linked_graphics[0])) {
        $linked_items = array_merge($linked_items, $linked_graphics);
      }
      $linked_resources = get_post_meta(get_the_ID(), 'Linked_resources');
      if (!empty($linked_resources[0])) {
        $linked_items = array_merge($linked_items, $linked_resources);
      }
      $linked_documents = get_post_meta(get_the_ID(), 'Linked_documents');
      if (!empty($linked_documents[0])) {
        $linked_items = array_merge($linked_items, $linked_documents);
      }
      $linked_quotes = get_post_meta(get_the_ID(), 'Linked_quotes');
      if (!empty($linked_quotes[0])) {
        $linked_items = array_merge($linked_items, $linked_quotes);
      }
      $linked_facts = get_post_meta(get_the_ID(), 'Linked_facts');
      if (!empty($linked_facts[0])) {
        $linked_items = array_merge($linked_items, $linked_facts);
      }
      $linked_reactions = get_post_meta(get_the_ID(), 'Linked_reactions');
      if (!empty($linked_reactions[0])) {
        $linked_items = array_merge($linked_items, $linked_reactions);
      }

      $content_item['linkedContentIds'] = join(',', $linked_items);
      $all_linked_items = array_merge($all_linked_items, $linked_items);
    } else {
      $content_item['linkedContentIds'] = '';
    }
    return $content_item;
  }
  
  function full_image_path($image_file_name) {
    return get_bloginfo('url') . "/wp-content/files_mf/" . $image_file_name;
  }
?>
