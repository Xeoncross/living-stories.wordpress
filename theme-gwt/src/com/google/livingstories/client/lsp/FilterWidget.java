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

import com.google.gwt.http.client.UrlBuilder;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.livingstories.client.AssetType;
import com.google.livingstories.client.ContentItemType;
import com.google.livingstories.client.util.Constants;

import java.util.Map;

/**
 * Widget that contains different types of filters for the events and content items displayed on
 * the LSP.
 */
public class FilterWidget extends Composite {
  private static final ContentItemType[] CONTENT_ITEM_TYPE_PRESENTATION_ORDER = {
    ContentItemType.EVENT, ContentItemType.NARRATIVE, ContentItemType.PLAYER,
    ContentItemType.QUOTE, ContentItemType.DATA 
  };
  private static final AssetType[] ASSET_TYPE_PRESENTATION_ORDER = {
    AssetType.LINK, AssetType.DOCUMENT, AssetType.IMAGE, AssetType.VIDEO,
    AssetType.AUDIO, AssetType.INTERACTIVE
  };
  
  private VerticalPanel filterPanel;

  public FilterWidget() {
    super();
    
    filterPanel = new VerticalPanel();
    DOM.setStyleAttribute(filterPanel.getElement(), "paddingTop", "5px");
    filterPanel.setWidth("125px");

    initWidget(filterPanel);
  }

  public void load(Map<ContentItemType, Integer> contentTypesToIds,
      Map<AssetType, Integer> assetTypesToIds) {
    createContentItemTypeFilters(contentTypesToIds, assetTypesToIds);
    addSeparator();
    createImportanceFilters();
    addSeparator();
    createTimeSortControls();
  }
  
  private void createContentItemTypeFilters(Map<ContentItemType, Integer> contentTypesToIds,
      Map<AssetType, Integer> assetTypesToIds) {
    filterPanel.add(createFilterRow(LspMessageHolder.consts.allTypes(), 0,
        Externs.getFilterImportantOnly(), Externs.getFilterChronological(),
        Externs.getFilterPanelId() == 0));
    
    for (ContentItemType contentType : CONTENT_ITEM_TYPE_PRESENTATION_ORDER) {
      if (contentTypesToIds.containsKey(contentType)) {
        createContentTypeFilter(contentType.getFilterString(), contentTypesToIds.get(contentType));
      }
    }

    for (AssetType assetType : ASSET_TYPE_PRESENTATION_ORDER) {
      if (assetTypesToIds.containsKey(assetType)) {
        createContentTypeFilter(assetType.getPluralPresentationString(),
            assetTypesToIds.get(assetType));
      }
    }

    // Invisible spacer element that will have the separator attached to it (via styling).
    // Can't attach directly to filters since they can appear/disappear for different themes.
    SimplePanel spacer = new SimplePanel();
    spacer.setPixelSize(10, 0);
    spacer.getElement().getStyle().setProperty("lineHeight", "0");
    filterPanel.add(spacer);
  }
  
  private void createContentTypeFilter(String filterName, Integer panelId) {
    filterPanel.add(createFilterRow(filterName, panelId, Externs.getFilterImportantOnly(),
        Externs.getFilterChronological(), panelId.equals(Externs.getFilterPanelId())));
  }
  
  /**
   * Create options in the filter panel to switch between 'all' and the 'most important' items
   */
  private void createImportanceFilters() {
    filterPanel.add(createFilterRow(LspMessageHolder.consts.allImportance(), 
        Externs.getFilterPanelId(), false, Externs.getFilterChronological(),
        !Externs.getFilterImportantOnly()));
    filterPanel.add(createFilterRow(LspMessageHolder.consts.highImportance(), 
        Externs.getFilterPanelId(), true, Externs.getFilterChronological(),
        Externs.getFilterImportantOnly()));
  }
  
  /**
   * Create options in the filter panel to switch the sorting of the items by time.
   */
  private void createTimeSortControls() {
    filterPanel.add(createFilterRow(LspMessageHolder.consts.newestFirst(),
        Externs.getFilterPanelId(), Externs.getFilterImportantOnly(), false,
        !Externs.getFilterChronological()));
    filterPanel.add(createFilterRow(LspMessageHolder.consts.oldestFirst(),
        Externs.getFilterPanelId(), Externs.getFilterImportantOnly(), true,
        Externs.getFilterChronological()));
  }
    
  private void addSeparator() {
    filterPanel.getWidget(filterPanel.getWidgetCount() - 1).addStyleName("toolbeltSeparator");
  }
  
  private Widget createFilterRow(String name, int panelId, boolean importantOnly,
      boolean chronological, boolean selected) {
    HTML arrow = new HTML(selected ? "&#8250;&nbsp;" : "&nbsp;&nbsp;");
    
    Anchor text = new Anchor(name);
    text.setStylePrimaryName(selected ? "selectedToolbeltFilter" : "unselectedToolbeltFilter");

    UrlBuilder urlBuilder = Window.Location.createUrlBuilder();
    if (panelId > 0) {
      urlBuilder.setParameter(Constants.FILTER_PANEL_ID_PARAM, Integer.toString(panelId));
    } else {
      urlBuilder.removeParameter(Constants.FILTER_PANEL_ID_PARAM);
    }
    if (importantOnly) {
      urlBuilder.setParameter(Constants.FILTER_IMPORTANCE_PARAM,
          Constants.FILTER_HIGH_IMPORTANCE_VALUE);
    } else {
      urlBuilder.removeParameter(Constants.FILTER_IMPORTANCE_PARAM);
    }
    if (chronological) {
      urlBuilder.setParameter(Constants.FILTER_ORDER_PARAM,
          Constants.FILTER_CHRONOLOGICAL_VALUE);
    } else {
      urlBuilder.removeParameter(Constants.FILTER_ORDER_PARAM);
    }
    text.setHref(urlBuilder.buildString());
    
    HorizontalPanel row = new HorizontalPanel();
    row.add(arrow);
    row.add(text);
    return row;
  }    
}
