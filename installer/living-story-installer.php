<?php
/*
Plugin name: Living Stories Installer
Plugin URI: 
Description: Makes it super easy to install Living Stories on WordPress
Version: 0.1
Author: Mohammad Jangda
Author URI: http://digitalize.ca
Copyright: (C) 2010 Mohammad Jangda

Use the code as you please. GPL it, perhaps?

*/

require_once( ABSPATH . 'wp-admin/includes/class-wp-upgrader.php' );

define( LIVINGSTORY_INSTALL_SUCCESS, 'livingstory_installed' );
define( LIVINGSTORY_INSTALL_PAGE, 'livingstoryinstall' );
define( LIVINGSTORIES_INSTALL_NONCE, 'livingstories_install' );
define( LIVINGSTORIES_WIKI_PAGE, 'http://code.google.com/p/living-stories/wiki/WordpressInstallation' );

class LivingStories_Installer {

	var $installed_plugins = null;
	var $installed_themes = null;
	var $dependencies = array(
		/*
		// slug should be full name for themes
		'slug' => array(
			'type' => 'plugin|theme'
			'label' => 'The Name' // name of the plugin or theme
			'url' => '' // The full url to the zip file for the plugin/theme. If not specified, will try to grab the plugin/theme from the WP Directory.
		)
		*/
		'co-authors-plus' => array(
			'type' => 'plugin',
			'label' => 'Co-Authors Plus',
		),
		'tinymce-excerpt' => array(
			'type' => 'plugin',
			'label' => 'TinyMCE Excerpt',
			'url' => 'http://wordpress.living-stories.googlecode.com/hg/tinymce-excerpt.zip'
		),
		'magic-fields' => array(
			'type' => 'plugin',
			'label' => 'Magic Fields',
			'url' => 'http://wordpress.living-stories.googlecode.com/hg/magic-fields.zip',
		),
		'living-story-plugin' => array(
			'type' => 'plugin',
			'label' => 'Living Story Plugin',
			'url' => 'http://wordpress.living-stories.googlecode.com/hg/living-story-plugin_1_02.zip',
		),
		'Living Stories' => array(
			'type' => 'theme',
			'label' => 'Living Story Theme',
			'url' => 'http://wordpress.living-stories.googlecode.com/hg/living-story-theme_1_0.zip',
		),
	);
		
	function LivingStories_Installer( ) {
		return $this->__construct( );
	}
	function __construct( ) {
		//if( get_option( LIVINGSTORY_INSTALL_SUCCESS ) != 0 )
			//add_action( 'admin_notices', array( &$this, 'show_install_nag' ) );
		
		if(  current_user_can( 'install_plugins' ) && current_user_can( 'install_themes' ) )
			add_action( 'admin_menu', array( &$this, 'add_menu_page' ) );
	}
	
	function install( ) {
		$this->output( __( 'Starting install' ) );

		$failed = false;
		
		$this->refresh_themes();
		$this->refresh_plugins();
		
		// Check that all dependencies are installed
		
		$dependencies = $this->get_dependencies( );
		
		foreach( $dependencies as $dependency_key => $dependency_vals ) {
			$dependency_type = $dependency_vals['type'];
			$dependency_label = $dependency_vals['label'];
			
			$this->output( sprintf( __( '<h4>%s</h4>' ),  $dependency_label ) );
			
			// if not installed
			if( !$this->is_dependency_installed( $dependency_key, $dependency_type ) ) {
				$this->output( __( 'Attempting to install.' ) );
				
				// download and install it
				$install = $this->install_dependency( $dependency_key, $dependency_type, $dependency_vals );
				
				// check if install failed
				if( !$install || is_wp_error( $install ) ) {
					// die (tell user to manually install stuff)
					$this->install_failed( sprintf( __( 'Failed to install: %s', 'ls-installer' ), $dependency_label ), $install );
					$failed = true;
					break;
				}
				$this->refresh_dependencies( $dependency_type );
			} else {
				$this->output( sprintf( __('%s already installed.', 'ls-installer' ), ucwords($dependency_type) ) );
			}
			
			// Check to see if plugin active
			if( !$this->is_dependency_active( $dependency_key, $dependency_type ) ) {
				$this->output( __( 'Attempting to activate.' ) );
				
				// Attempt to activate it
				$activate = $this->activate_dependency( $dependency_key, $dependency_type );
				
				// check if install failed
				if( !$activate || is_wp_error( $activate ) ) {
					// die (tell user to manually install stuff)
					$this->install_failed( __( 'Failed to activate', 'ls-installer' ), $install );
					$failed = true;
					break;
				}
				
				$this->output( __( 'Successfully activated.' ) );
				
			} else {
				$this->output( sprintf( __('%s already activated.', 'ls-installer' ), ucwords($dependency_type) ) );
				continue;
			}
		}
		
		if( !$failed ) {
			update_option( LIVINGSTORY_INSTALL_SUCCESS, 1 );
			echo '<div class="updated">';
			$this->output( __( 'Living Stories successfully installed.' ) );
			$this->output( sprintf( __( 'Get started: <a href="%s" target="_blank" title="Opens new window">How to Create Living Stories</a>', 'ls-installer' ), LIVINGSTORIES_WIKI_PAGE ) );
			echo '</div>';
		}
		
	}
	
	function output( $message, $obj = null ) {
		echo '<p> '. $message .'</p>';
	}
	
	function get_dependencies( ) {
		// abstracted into a function in case we ever want to grab dependencies from elswehere
		return $this->dependencies();
	}
	
	function is_dependency_installed( $name, $type ) {
		switch( $type ) {
			case 'plugin':
				return $this->is_plugin_installed( $name );
			case 'theme':
				return $this->is_theme_installed( $name );
			default:
				return false;
		}
	}
	
	function is_plugin_installed( $name ) {
		foreach( array_keys( $this->installed_plugins ) as $plugin_path ) {
			if( strstr( $plugin_path, $name ) )
				return true;
		}
		return false;
	}
	function is_theme_installed( $name ) {
	
		$theme = get_theme($name);
		if( $theme ) 
			return true;
		
		return false;
	}
	
	function is_dependency_active( $name, $type ) {
		switch ( $type ) {
			case 'plugin':
				return $this->is_plugin_active( $name );
			case 'theme':
				return $this->is_theme_active( $name );
			default:
				return false;
		}
	}
	
	function install_dependency( $name, $type, $values ) {
		switch ( $type ) {
			case 'plugin':
				return $this->install_plugin( $name, $values );
			case 'theme':
				return $this->install_theme( $name, $values );
			default:
				return false;
		}
	}
	
	function install_plugin( $name, $values ) {
		
		include_once( ABSPATH . 'wp-admin/includes/plugin-install.php' ); //for plugins_api..
		
		$plugin = $name;
		$title = sprintf( __('Installing Plugin: %s', 'ls-installer'), $values['label'] );
		
		if( $values['url'] ) {
			$download_link = $values['url'];
		} else {
			$api = plugins_api('plugin_information', array('slug' => $plugin, 'fields' => array('sections' => false) ) );
			$download_link = $api->download_link;
		}
		
		$installer = new Plugin_Upgrader( new LSInstaller_Plugin_Installer_Skin( compact('title', 'plugin') ) );
		$installer->install( $download_link );
		
		return $installer->result;
	}
	
	function install_theme( $name, $values ) {
		
		include_once( ABSPATH . 'wp-admin/includes/theme-install.php' ); //for themes_api..
		
		if( $values['url'] ) {
			$download_link = $values['url'];
		} else {
			$api = themes_api('theme_information', array('slug' => $theme, 'fields' => array('sections' => false) ) ); //Save on a bit of bandwidth.
			if ( is_wp_error($api) )
				return WP_Error( __('Failed to contact theme API', 'ls-installer' ), $api );
				
			$download_link = $api->download_link;
		}
		
		$title = sprintf( __('Installing Theme: %s', 'ls-installer' ), $values['label'] );
		$theme = $name;
		
		$type = 'web'; //Install theme type, From Web or an Upload.
		
		$upgrader = new Theme_Upgrader( new LSInstaller_Theme_Installer_Skin( compact('title', 'theme', 'api') ) );
		$upgrader->install( $download_link );
		
		return $upgrader->result;
	}
	
	function activate_dependency( $name, $type ) {
		switch( $type ) {
			case 'plugin':
				return $this->activate_plugin( $name );
			case 'theme':
				return $this->activate_theme( $name );
			default:
				return false;
		}
	}
	
	function is_plugin_active( $name ) {
		$plugin_path = $this->get_plugin_path( $name );
		return is_plugin_active( $plugin_path );
	}
	
	function is_theme_active( $name ) {
		$theme = get_current_theme();
		if( $theme == $name )
			return true;
		return false;
	}
	
	function activate_plugin( $name ) {
		$plugin_path = $this->get_plugin_path( $name );
		
		if( $plugin_path ) {
			activate_plugin( $plugin_path );
			return $this->is_plugin_active( $name );
		}
		return new WP_Error( __('Could not find plugin', 'ls-installer') );
	}
	
	function get_plugin_path( $name ) {
		foreach( array_keys( $this->installed_plugins ) as $plugin_path ) {
			if( strstr( $plugin_path, $name ) )
				return $plugin_path;
		}
		return false;
	}
	
	function activate_theme( $name ) {
		$theme = $this->installed_themes[$name]['Template'];
		$stylesheet = $this->installed_themes[$name]['Stylesheet'];
		
		switch_theme( $theme, $stylesheet );
		
		if( $this->is_theme_active( $name ) )
			return true;
		
		return false;
	}
	
	function refresh_dependencies( $type ) {
		switch( $type ) {
			case 'plugin':
				$this->refresh_plugins();
			case 'theme':
				$this->refresh_themes();
			default:
				break;
		}
		return;
	}
	function refresh_themes() {
		global $wp_themes;
		$wp_themes = null;
		wp_cache_delete('themes', 'themes');
		$this->installed_themes = get_themes();
	}
	function refresh_plugins() {
		wp_cache_delete('plugins', 'plugins');
		$this->installed_plugins = get_plugins();
	}

	function add_menu_page( ) {
		add_submenu_page('plugins.php', __('Install Living Stories'), __('Install Living Stories'), 'install_plugins', LIVINGSTORY_INSTALL_PAGE, array( &$this, 'show_main_page' ) );
	}
	
	function show_main_page( ) {
		$install_url = $this->get_install_action_url();
		?>
		<div class="wrap">
			<div id="icon-tools" class="icon32"><br></div>
			<h2><?php _e( 'Install Living Stories' ); ?></h2>
			<?php if( $_REQUEST['go'] == 1 && check_admin_referer( LIVINGSTORIES_INSTALL_NONCE ) ) : ?>
				<?php $this->install(); ?>
			<?php else : ?>
				<p>
					<?php echo sprintf( __( 'Use the button below to automatically install Living Stories for WordPress along with any other requirements. For manual installation instructions, follow the instructions on our <a href="http://code.google.com/p/living-stories/wiki/WordpressInstallation" target="_blank">Google Code Page</a>.' ), LIVINGSTORIES_WIKI_PAGE ); ?>
				</p>
				<p>
					<a href="<?php echo $install_url; ?>" class="button">
					<?php _e( 'Install' ); ?>
					</a>
				</p>
			<?php endif; ?>
		</div>
		<?php
	}
	
	function show_install_nag( ) {
		$install_page = $this->get_install_page_url();
		?>
		<?php if( !get_option( LIVINGSTORY_INSTALL_SUCCESS ) ) : ?>
			<div id="message" class="updated">
				<p>
					<?php _e( 'Looks like you have not installed Living Stories for WordPress.' ); ?>
					<a href="<?php echo $install_page; ?>"><?php _e( 'Install Now!' ); ?></a>
				</p>
			</div>
		<?php endif; ?>
		<?php
	}
	
	function install_failed( $message, $key = null, $val = null ) {
		$install_url = $this->get_install_page_url();
		?>
		<?php $this->output( _e('Sorry, installation of Living Stories for WordPress failed.', 'living-stories'), array( $install, $key, $val ) ); ?>
		
		<a href="<?php echo $install_url ?>"><?php _e('Try again?', 'living-stories'); ?></a> | <a href="<?php echo LIVINGSTORIES_WIKI_PAGE ?>" target="_blank"><?php _e('Install Manually?', 'living-stories'); ?></a>
		<?php
	}
	
	function get_install_page_url( ) {
		return add_query_arg( 
			array(
				'page' => LIVINGSTORY_INSTALL_PAGE
			),
			'plugins.php' 
		);
	}
	function get_install_action_url( ) {
		return wp_nonce_url(
			add_query_arg( 
				array(
					'page' => LIVINGSTORY_INSTALL_PAGE,
					'go' => 1
				),
				'plugins.php' 
			),
			LIVINGSTORIES_INSTALL_NONCE
		);
	}
}

class LSInstaller_Plugin_Installer_Skin extends Plugin_Installer_Skin {

	function LSInstaller_Plugin_Installer_Skin($args = array()) {
		return $this->__construct($args);
	}

	function __construct($args = array()) {
		parent::__construct($args);
	}

	function header() {
		if ( $this->done_header )
			return;
		$this->done_header = true;
		//echo '<h4>' . $this->options['title'] . '</h4>';
	}
	function footer() {}
	function before() {}
	function after() {}
}
class LSInstaller_Theme_Installer_Skin extends Theme_Installer_Skin {

	function LSInstaller_Theme_Installer_Skin($args = array()) {
		return $this->__construct($args);
	}

	function __construct($args = array()) {
		parent::__construct($args);
	}
	function header() {
		if ( $this->done_header )
			return;
		$this->done_header = true;
		//echo '<h4>' . $this->options['title'] . '</h4>';
	}
	function footer() {}
	function before() {}
	function after() {}
}

add_action('init', 'living_stories_installer_init');
function living_stories_installer_init( ) {
	global $livingstories_installer;
	$livingstories_installer = new LivingStories_Installer();
}

?>