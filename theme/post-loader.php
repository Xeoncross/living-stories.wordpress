<?php
/*
Template Name: Post Loader
 */
?>

<?php
  require 'content-util.php';

  // Get some useful values from the database

  $write_panels = array();
  $mf_write_panels = $wpdb->get_results('SELECT id, name FROM ' . MF_TABLE_PANELS);
  foreach ($mf_write_panels as $panel) {
    $write_panels[$panel->id] = $panel->name;
  }


  // Get the content for the page

  $content_items = array();
  $all_linked_items = array();

  // First get all the core content items

  // Check if this page is being filtered by a linked post id.
  // This will change the behavior of some parts (e.g. don't get linked content)
  $filtered_by_post_id = isset($_GET['linkedPostId']);

  // Get the selected panel ID, or use events and narratives if none is specified and
  // we're not filtering on a specific post.  In that situation, we want all content types.
  if ($_GET['panelId']) {
    $panel_ids[] = $_GET['panelId'];
  } else if (!$filtered_by_post_id) {
    $panel_ids[] = array_search('Events', $write_panels);
    $panel_ids[] = array_search('Narratives', $write_panels);
  }
  
  $has_more = false;
  $query_args = array(
    'cat_id' => $_GET['cat'],
    'panel_id' => $panel_ids,
    'linked_post_id' => $_GET['linkedPostId'],
    'importance' => $_GET['importance'],
    'chronological' => $_GET['order']
  );
  $pageposts = $wpdb->get_results(create_query($query_args, $_GET['page']));
  if ($pageposts) {
    // If we don't specify a page, we want to return everything.
    $result_count = $_GET['page'] ? POSTS_PER_PAGE : -1;
    foreach ($pageposts as $post) {
      if ($result_count == 0) {
        $has_more = true;
        break;
      }
      setup_postdata($post);
      $content_items[] = create_content_item($all_linked_items);
      $result_count--;
    }
  }

  // Then get all the linked elements. We need to explicitly check
  // if $all_linked_items is empty; query_posts doesn't do the right
  // thing with an empty post__in parameter. It's probably just as well,
  // but an annoying behavior and, IMO, a wordpress WTF. Anyway,
  if (!$filtered_by_post_id && !empty($all_linked_items)) {
    query_posts(array('post__in' => $all_linked_items));
    while (have_posts()) {
      the_post();
      $content_items[] = create_content_item();
    }
  }
  
  // Finally, output the result as JSON
  $result = array(
    "CONTENT_ITEMS" => $content_items,
    "HAS_MORE_ITEMS" => $has_more
  );
  echo json_encode($result); 
?>

