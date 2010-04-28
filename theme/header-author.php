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

  $content_items = array();
  
  // if on an author page, the default loop will pick up all items this
  // contributor has authored appropriately, and we deliberately skip linked
  // items.
  while (have_posts()) {
    the_post();
    $content_items[] = create_content_item();
  }

  // Get the item types
  $write_panels = array();
  $mf_write_panels = $wpdb->get_results('SELECT id, name FROM ' . MF_TABLE_PANELS);
  foreach ($mf_write_panels as $panel) {
    $write_panels[$panel->id] = $panel->name;
  }
?>

<script type="text/javascript">
  var CONTENT_ITEMS = <?php echo json_encode($content_items); ?>;
  var ITEM_TYPES = <?php echo json_encode($write_panels); ?>;
  var THEMES = {};
  var HAS_MORE_ITEMS = false;
  var TEMPLATE_DIRECTORY = "<?php bloginfo('template_directory') ?>";
</script>

<script type="text/javascript" src="<?php bloginfo('template_directory'); ?>/LivingStoryPage.nocache.js"></script>

<?php if ( is_singular() ) wp_enqueue_script( 'comment-reply' ); ?>

<?php wp_head(); ?>
</head>
<body <?php body_class(); ?>>
<div id="page">

<div id="header" role="banner">
	<div id="headerimg">
		<h1><a href="<?php echo get_option('home'); ?>/"><?php bloginfo('name'); ?></a></h1>
		<div class="description"><?php bloginfo('description'); ?></div>
	</div>
</div>

<hr />
