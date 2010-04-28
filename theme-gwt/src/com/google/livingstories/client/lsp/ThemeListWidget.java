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

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.http.client.UrlBuilder;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.livingstories.client.lsp.event.EventBus;
import com.google.livingstories.client.lsp.event.ThemeSelectedEvent;
import com.google.livingstories.client.ui.ButtonListWidget;
import com.google.livingstories.client.util.Constants;

import java.util.HashMap;
import java.util.Map;

/**
 * Displays a vertical list of theme names.  Positioned correctly, this can be made
 * to look like tabs to a pane full of updates, filtered by the appropriate theme.
 */
public class ThemeListWidget extends Composite {
  private ButtonListWidget contentPanel;
  private Map<Integer, Integer> themeIdToThemeRowMap = new HashMap<Integer, Integer>();
  private Integer selectedThemeId = null;
  private String ALL_THEMES = LspMessageHolder.consts.allThemes();
  
  public ThemeListWidget() {
    contentPanel = new ButtonListWidget();
    DOM.setStyleAttribute(contentPanel.getElement(), "marginBottom", "10px");
    initWidget(contentPanel);
    setVisible(false);
  }
  
  public void load(Map<Integer, String> themesById, Integer selectedThemeId) {
    if (themesById.isEmpty()) {
      setVisible(false);
    } else {
      setVisible(true);
      contentPanel.addItem(createThemeBlock(null, ALL_THEMES), new ThemeClickHandler(null),
          selectedThemeId == null);
      
      int rows = 1;
      for (Map.Entry<Integer, String> theme : themesById.entrySet()) {
        Integer themeId = theme.getKey();
        String displayName = theme.getValue();
        Widget themeRow = createThemeBlock(themeId, displayName);
        contentPanel.addItem(themeRow, new ThemeClickHandler(themeId),
            selectedThemeId != null && selectedThemeId.equals(themeId));
        themeIdToThemeRowMap.put(themeId, rows);
        rows++;
      }
    }
  }

  public Integer getSelectedThemeId() {
    return selectedThemeId;
  }
  
  public void setSelectedThemeId(Integer themeId) {
    selectedThemeId = themeId;
    if (isVisible()) {
      if (themeId == null) {
        contentPanel.selectItem(0);
      } else {
        contentPanel.selectItem(themeIdToThemeRowMap.get(themeId));
      }
    }
    EventBus.INSTANCE.fireEvent(new ThemeSelectedEvent(themeId));
  }
  
  private Widget createThemeBlock(Integer themeId, String name) {
    SimplePanel panel = new SimplePanel();
    Anchor theme = new Anchor(name);

    UrlBuilder urlBuilder = Window.Location.createUrlBuilder();
    if (themeId == null) {
      urlBuilder.removeParameter(Constants.THEME_ID_PARAM);
    } else {
      urlBuilder.setParameter(Constants.THEME_ID_PARAM, themeId.toString());
    }
    theme.setHref(urlBuilder.buildString());

    panel.add(theme);
    return panel;
  }
  
  private class ThemeClickHandler implements ClickHandler {
    private Integer themeId;
    
    public ThemeClickHandler(Integer themeId) {
      this.themeId = themeId;
    }
    
    public void onClick(ClickEvent e) {
      setSelectedThemeId(themeId);
    }
  }
}
