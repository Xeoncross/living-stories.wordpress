<?php
/**
 * @package WordPress
 * @subpackage Default_Theme
 */
  global $query_string;
  global $category;
  parse_str($query_string, $params);
  if (isset($params['cat'])) {
    $category = get_category($params['cat']);
    $category_desc = category_description($category->cat_ID);
  } else if (isset($params['category_name'])) {
    $category = get_category_by_slug($params['category_name']);
    $category_desc = $category->description;
  } else {
    $category_desc = 'Unable to retrieve living story description.';
  }

  // Check if this page is being filtered by a linked post id.
  // This will change the behavior of some parts (e.g. don't get linked content)
  $filtered_by_post_id = isset($_GET['linkedPostId']);

  get_header('lsp');
?>

<body <?php body_class(); ?>>
<div id="page">

<div id="header" role="banner">
	<div id="headerimg">
		<h1><a href="<?php echo get_option('home'); ?>/"><?php bloginfo('name'); ?></a></h1>
		<div class="description"><?php bloginfo('description'); ?></div>
	</div>
</div>

<hr/>

<?php if ($filtered_by_post_id) : ?>
  <?php $post = get_post($_GET['linkedPostId']); ?>
  <!-- TODO: render the posts in more detail -->
  <div class="categorySummary">
    <a href="<?php echo get_category_link($category->cat_ID); ?>" style="float:right">&laquo; Back to story</a>
    <h2><?php echo $post->post_title; ?></h2>
    <div><?php echo $post->post_content; ?></div>
  </div>
<?php else : ?>
  <div class="categorySummary">
    <h2><?php echo $category->name; ?></h2>
    <div id="summary"></div>
  </div>
<?php endif; ?>

<div id="content" class="narrowcolumn" role="main">
  <div id="contentList"></div>
</div>
<div id="sidebar" class="complementary">
  <div id="themes" style="width=100%"></div>
  <div id="filters"></div>
</div>

<?php get_footer(); ?>
