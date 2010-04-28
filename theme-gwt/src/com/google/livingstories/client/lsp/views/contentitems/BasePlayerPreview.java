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

package com.google.livingstories.client.lsp.views.contentitems;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.http.client.UrlBuilder;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.livingstories.client.PlayerContentItem;
import com.google.livingstories.client.lsp.ContentRenderer;
import com.google.livingstories.client.util.Constants;

/**
 * Basic preview implementation for a player contentItem.
 * 
 * Note the use of the bind() method to bind the ui.xml file to this class.
 * This allows other classes to extend this class rather than composing it,
 * providing a different ui.xml file to bind to while keeping the same
 * underlying functionality and preventing the subclass from having to
 * deal with injecting constructor args for this class.
 * See the PlayerPopupView UiBinder classes for an example of this.
 */
public class BasePlayerPreview extends Composite {

  private static BasePlayerPreviewUiBinder uiBinder = GWT.create(BasePlayerPreviewUiBinder.class);

  interface BasePlayerPreviewUiBinder extends UiBinder<Widget, BasePlayerPreview> {
  }

  @UiField Label header;
  @UiField SimplePanel image;
  @UiField Label name;
  @UiField SimplePanel description;
  
  private PlayerContentItem contentItem;

  public BasePlayerPreview(PlayerContentItem contentItem) {
    this.contentItem = contentItem;
    
    bind();
    
    if (contentItem.getPhotoContentItem() != null) {
      Image photoWidget = new Image();
      photoWidget.setUrl(contentItem.getPhotoContentItem().getPreviewUrl());
      photoWidget.addStyleName("playerPhoto");
      image.add(photoWidget);
    }

    name.setText(contentItem.getName());
    description.add(
        new ContentRenderer(contentItem.getContent().split(Constants.BREAK_TAG)[0], false));
  }

  protected void bind() {
    initWidget(uiBinder.createAndBindUi(this));
  }

  public BasePlayerPreview hideHeader() {
    header.setVisible(false);
    return this;
  }
  
  @UiHandler("name") void goToPlayerPage(ClickEvent e) {
    UrlBuilder urlBuilder = Window.Location.createUrlBuilder();
    urlBuilder.setParameter(Constants.FILTER_LINKED_POST_ID_PARAM,
        Integer.toString(contentItem.getId()));
    Window.Location.assign(urlBuilder.buildString());
  }
}
