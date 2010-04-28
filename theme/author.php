<?php
/**
 * @package WordPress
 * @subpackage Default_Theme
 */

get_header('author');
?>

<?php
  $currauth = isset($_GET['author_name']) ? get_userdatabylogin($author_name)
      : get_userdata(intval($author));
  $first_last_name = $currauth->first_name . ' ' . $currauth->last_name;
?>
  <div class="authorSummary">
    <p>
      <b><?php echo $first_last_name; ?></b>
    </p>
    <div><?php echo wpautop($currauth->description); ?></div>
  </div>
<?php if (have_posts()) : ?>

  <h3 class="narrowcolumn"><em>Posts by this author:</em></h3>

  <div id="contentList" class="narrowcolumn" role="main"></div>

<?php else :
  printf("<h2 class='center'>Sorry, but there aren't any posts by %s yet.</h2>", $first_last_name);
  get_search_form();
endif;
?>

<?php get_footer(); ?>
