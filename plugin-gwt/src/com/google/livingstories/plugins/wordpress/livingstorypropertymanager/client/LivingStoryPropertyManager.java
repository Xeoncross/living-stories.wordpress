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

package com.google.livingstories.plugins.wordpress.livingstorypropertymanager.client;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.livingstories.plugins.wordpress.livingstorypropertymanager.client.i18n.ClientConstants;
import com.google.livingstories.plugins.wordpress.livingstorypropertymanager.client.ui.LocationInput;
import com.google.livingstories.plugins.wordpress.livingstorypropertymanager.client.util.ContentItemData;

/**
 * Widget that displays the content management panel.
 * 
 * This widget contains a number of controls that represent the content state.
 * The canonical state information is stored in local variables.  When the widget
 * initializes, it first copies the stored metadata from the page (using ContentItemData),
 * and then sets those values on each of the user-facing controls.  If the metadata was
 * initially empty, this widget will fill it in with reasonable defaults.
 * 
 * Whenever a control is modified by the user, the value will be copied into the local
 * variable, and then events will be propogated to the other controls if necessary.
 * 
 * This design prevents us from having to reach into the view layer constantly from
 * the business logic that determines how the controls interact.
 */
public class LivingStoryPropertyManager extends Composite {
  public static ClientConstants properties = GWT.create(ClientConstants.class);

  private static LivingStoryPropertyManagerUiBinder uiBinder
      = GWT.create(LivingStoryPropertyManagerUiBinder.class);
  interface LivingStoryPropertyManagerUiBinder
      extends UiBinder<Widget, LivingStoryPropertyManager> {
  }

  /* Common fields */
  @UiField SimplePanel importanceDropdownPanel;
  
  @UiField SimplePanel locationInput;
  
  /* Non-UiBinder Form controls */
  private EnumDropdown<Importance> importanceDropdown
      = new EnumDropdown<Importance>(Importance.class);
  
  /* Content metadata */
  private Importance importance;
  private Location location;
  
  public LivingStoryPropertyManager() {
    initMetadataValues();
    String mapsKey = properties.mapsKey();
    if (mapsKey != null && !mapsKey.isEmpty()) {
      locationInput.add(new LocationInput(location, mapsKey));
    }
    
    initWidget(uiBinder.createAndBindUi(this));
    createImportanceDropdown();
    initializeControls();
  }

  private void initMetadataValues() {
    importance = ContentItemData.getImportance();
    location = ContentItemData.getLocation();
  }
  
  private void createImportanceDropdown() {
    importanceDropdown.setName("lsp_importance");
    importanceDropdown.addChangeHandler(new ChangeHandler() {
      @Override
      public void onChange(ChangeEvent event) {
        importance = importanceDropdown.getSelectedConstant();
      }
    });
    importanceDropdownPanel.add(importanceDropdown);
  }

  
  private void initializeControls() {
    if (importance == null) {
      // Use the default values.
      importance = Importance.MEDIUM;
    }
    
    // Set the values to those provided by the metadata object (or the defaults).
    importanceDropdown.selectConstant(importance);
  }
}
