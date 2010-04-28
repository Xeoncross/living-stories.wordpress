/**
 * Copyright 2010 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS-IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.google.livingstories.client.lsp;

import com.google.gwt.ajaxloader.client.AjaxLoader;
import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.livingstories.client.AssetContentItem;
import com.google.livingstories.client.BaseContentItem;
import com.google.livingstories.client.lsp.views.Resources;
import com.google.livingstories.client.lsp.views.contentitems.PopupViewFactory;
import com.google.livingstories.client.ui.Lightbox;
import com.google.livingstories.client.ui.Slideshow;

import java.util.ArrayList;
import java.util.List;

/**
 * Chrome around a living story.
 */
public class LivingStory implements EntryPoint {
  private Lightbox lightbox = new Lightbox();

  @Override
  public void onModuleLoad() {
    exportMethods();
    
    // Inject the contents of the CSS file
    Resources.INSTANCE.css().ensureInjected();

    // Initialize the Google Ajax APIs
    AjaxLoader.init();

    // Bind to the externally defined variables to get content
    Externs.bind();

    RootPanel summaryPanel = RootPanel.get("summary");
    if (summaryPanel != null) {
      ContentRenderer summary = new ContentRenderer(Externs.getLivingStorySummary(), true);
      summaryPanel.add(summary);
    }
    
    RootPanel contentPanel = RootPanel.get("contentList");
    if (contentPanel != null) {
      // Create the content item list.
      LspContentItemListWidget list = new LspContentItemListWidget();
      list.load(Externs.getCoreItemIds(), Externs.getContentItemsById());
      contentPanel.add(list);
      int jumpToId = Externs.getJumpToId();
      if (jumpToId > 0) {
        list.goToContentItem(jumpToId);
      }
    }

    RootPanel themesPanel = RootPanel.get("themes");
    if (themesPanel != null) {
      // Create the theme tabs
      ThemeListWidget themes = new ThemeListWidget();
      themes.setWidth("100%");
      themes.load(Externs.getThemesById(), Externs.getThemeId());
      themesPanel.add(themes);
    }
    
    RootPanel filtersPanel = RootPanel.get("filters");
    if (filtersPanel != null) {
      // Create the filters
      FilterWidget filters = new FilterWidget();
      filters.load(Externs.getContentTypesToPanelIds(), Externs.getAssetTypesToPanelIds());
      filtersPanel.add(filters);
    }
  }
  
  public void showLightbox(String title, BaseContentItem contentItem) {
    lightbox.showItem(title, PopupViewFactory.createView(contentItem));
  }
  
  public void showLightboxForContentItem(final String title, int contentItemId) {
    showLightbox(title, Externs.getContentItemsById().get(contentItemId));
  }
  
  public void showSlideshow(int[] contentItemIds) {
    List<AssetContentItem> images = new ArrayList<AssetContentItem>(contentItemIds.length);
    for (int contentItemId : contentItemIds) {
      images.add((AssetContentItem) Externs.getContentItemsById().get(contentItemId));
    }
    new Slideshow(images).show(0);
  }

  private native void exportMethods() /*-{
    var instance = this;
    $wnd.showLightbox = function(title, contentItem) {
      instance.
          @com.google.livingstories.client.lsp.LivingStory::showLightbox(Ljava/lang/String;Lcom/google/livingstories/client/BaseContentItem;)
          .call(instance, title, contentItem);
    };
    $wnd.showLightboxForContentItem = function(title, contentItemId) {
      instance.
          @com.google.livingstories.client.lsp.LivingStory::showLightboxForContentItem(Ljava/lang/String;I)
          .call(instance, title, contentItemId);
    };
    $wnd.showSlideshow = function(contentItemIds) {
      instance.
          @com.google.livingstories.client.lsp.LivingStory::showSlideshow([I)
          .call(instance, contentItemIds);
    };
  }-*/;
}
