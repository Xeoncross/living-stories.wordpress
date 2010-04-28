<?php
/**
 * @package WordPress
 * @subpackage Default_Theme
 */
?>

<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml" <?php language_attributes(); ?>>

<head profile="http://gmpg.org/xfn/11">
<meta http-equiv="Content-Type" content="<?php bloginfo('html_type'); ?>; charset=<?php bloginfo('charset'); ?>" />

<title><?php wp_title('&laquo;', true, 'right'); ?> <?php bloginfo('name'); ?></title>

<link rel="stylesheet" href="<?php bloginfo('template_directory') ?>/gwt/standard/standard.css" type="text/css"/>
<link rel="stylesheet" href="<?php bloginfo('stylesheet_url'); ?>" type="text/css" media="screen" />
<link rel="pingback" href="<?php bloginfo('pingback_url'); ?>" />

<style type="text/css" media="screen">

<?php
// Checks to see whether it needs a sidebar or not
if ( empty($withcomments) && !is_single() ) {
?>
  #page { background: url("<?php bloginfo('stylesheet_directory'); ?>/images/kubrickbg-<?php bloginfo('text_direction'); ?>.jpg") repeat-y top; border: none; }
<?php } else { // No sidebar ?>
  #page { background: url("<?php bloginfo('stylesheet_directory'); ?>/images/kubrickbgwide.jpg") repeat-y top; border: none; }
<?php } ?>

</style>

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
  
  // $category is assigned in category.php
  global $category;
  
  // $filtered_by_post_id is assigned in category.php
  global $filtered_by_post_id;

  // Get the selected theme, or use the living story category if none selected.
  $category_id = $_GET['themeId'];
  if (!$category_id) {
    $category_id = $category->cat_ID;
  }

  // Get the selected panel ID, or use events and narratives if none is specified and
  // we're not filtering on a specific post.  In that situation, we want all content types.
  if ($_GET['panelId']) {
    $panel_ids[] = $_GET['panelId'];
  } else if (!$filtered_by_post_id) {
    $panel_ids[] = array_search('Events', $write_panels);
    $panel_ids[] = array_search('Narratives', $write_panels);
  }

  $jump_to_id = $_GET['jumpTo'];

  $has_more = false;
  $query_args = array(
    'cat_id' => $category_id,
    'panel_id' => $panel_ids,
    'linked_post_id' => $_GET['linkedPostId'],
    'importance' => $_GET['importance'],
    'chronological' => $_GET['order']
  );
  // If there's an ID we want to jump to, we return all query results and loop through
  // them.  Otherwise, we just return one page worth of results.
  $pageposts = $wpdb->get_results(create_query($query_args, $jump_to_id ? null : 0));
  if ($pageposts) {
    $foundJumpToPost = !isset($jump_to_id);
    $result_count = POSTS_PER_PAGE;
    foreach ($pageposts as $post) {
      if ($result_count == 0) {
        if ($foundJumpToPost) {
          // We've shown this page of results but we still have more in $pageposts.
          $has_more = true;
          break;
        } else {
          // We've finished showing a page but haven't found our jump-to post yet.
          // Display another page.
          $result_count = POSTS_PER_PAGE;
        }
      }
      setup_postdata($post);
      $content_items[] = create_content_item($all_linked_items);
      if ($post->ID == $jump_to_id) {
        $foundJumpToPost = true;
      }
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
  
  // Get the available themes
  $themes = array();
  $subcategories = get_categories('parent='.$category->cat_ID);
  foreach ($subcategories as $s) {
    $themes[$s->cat_ID] = $s->name;
  }
  
  // Get the available filters
  $filters = array();
  // TODO make this filter on living story as well
  $used_write_panels = $wpdb->get_col("SELECT DISTINCT meta_value FROM $wpdb->postmeta WHERE meta_key = '" . RC_CWP_POST_WRITE_PANEL_ID_META_KEY . "'");
  foreach ($used_write_panels as $p) {
    $filters[$p] = $write_panels[$p];
  }
?>

<script type="text/javascript">
  var LIVING_STORY_ID = <?php echo $category->cat_ID; ?>;
  var LIVING_STORY_SUMMARY = <?php echo json_encode($category->description); ?>;
  var CONTENT_ITEMS = <?php echo json_encode($content_items); ?>;
  var THEMES = <?php echo json_encode($themes); ?>;
  var TEMPLATE_DIRECTORY = "<?php bloginfo('template_directory') ?>";
  var ITEM_TYPES = <?php echo json_encode($filters); ?>;
  var CORE_ITEM_TYPES = <?php echo json_encode($panel_ids); ?>;
  var HAS_MORE_ITEMS = <?php echo json_encode($has_more); ?>;
  var POST_LOADER_URL = <?php echo json_encode(get_permalink(get_page_by_title('Post Loader'))) ?>
</script>

<script type="text/javascript" src="<?php bloginfo('template_directory'); ?>/LivingStoryPage.nocache.js"></script>

<?php if ( is_singular() ) wp_enqueue_script( 'comment-reply' ); ?>

<?php wp_head(); ?>
</head>

