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
import com.google.gwt.event.dom.client.HasClickHandlers;
import com.google.gwt.http.client.Request;
import com.google.gwt.http.client.RequestBuilder;
import com.google.gwt.http.client.RequestCallback;
import com.google.gwt.http.client.RequestException;
import com.google.gwt.http.client.Response;
import com.google.gwt.http.client.UrlBuilder;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.livingstories.client.BaseContentItem;
import com.google.livingstories.client.contentitemlist.ContentItemList;
import com.google.livingstories.client.util.Constants;
import com.google.livingstories.client.util.UriParser;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Widget for the main area of the LSP that contains the list of content. By default, a list of
 * events and narrative content items is shown. It can be filtered in various different ways, such
 * as seeing only content of a particular type, to see only important content, etc.; the callers
 * generally handle this filtering by making asynchronous calls and then replacing the
 * contents of this widget with new contents. A simple chronological reversal of the
 * elements to show, however, can be accomplished by calling doSimpleReversal().
 */
public class LspContentItemListWidget extends Composite {
  private VerticalPanel panel;
  private ContentItemList contentItemList;

  private int nextPage = 0; 
  
  private Label moreLink;
  private Image loadingImage;
  private Label problemLabel;
  
  private static String VIEW_MORE = LspMessageHolder.consts.viewMore();
  private static String PROBLEM_TEXT = LspMessageHolder.consts.viewMoreProblem();
  
  public LspContentItemListWidget() {
    super();
    
    panel = new VerticalPanel();
    panel.addStyleName("contentItemList");
    
    contentItemList = ContentItemList.create();
    contentItemList.adjustTimeOrdering(Externs.getFilterChronological());

    moreLink = new Label(VIEW_MORE);
    moreLink.setStylePrimaryName("primaryLink");
    moreLink.addStyleName("biggerFont");
    moreLink.setVisible(false);
    DOM.setStyleAttribute(moreLink.getElement(), "padding", "5px");
    addMoreLinkHandler(moreLink);

    loadingImage = new Image("/images/loading.gif");
    loadingImage.setVisible(false);
    
    problemLabel = new Label(PROBLEM_TEXT);
    problemLabel.addStyleName("error");
    problemLabel.setVisible(false);
    
    panel.add(contentItemList);
    panel.add(moreLink);
    panel.add(loadingImage);
    panel.add(problemLabel);

    clear();
    
    initWidget(panel);
  }
  
  protected void addMoreLinkHandler(HasClickHandlers moreLink) {
    moreLink.addClickHandler(new ClickHandler() {
      @Override
      public void onClick(ClickEvent event) {
        UrlBuilder urlBuilder = UriParser.parse(Externs.getPostLoaderUrl());
        Map<String, List<String>> parameters = Window.Location.getParameterMap();
        for (String key : parameters.keySet()) {
          List<String> values = parameters.get(key);
          urlBuilder.setParameter(key, values.toArray(new String[values.size()]));
        }
        urlBuilder.setParameter("cat", Integer.toString(Externs.getLivingStoryId()));
        urlBuilder.setParameter(Constants.FILTER_PAGE_PARAM, Integer.toString(nextPage));
        
        try {
          new RequestBuilder(RequestBuilder.GET, urlBuilder.buildString()).sendRequest(null,
              new RequestCallback() {
                public void onError(Request request, Throwable exception) {
                  showError();
                }
                public void onResponseReceived(Request request, Response response) {
                  if (200 == response.getStatusCode()) {
                    ContentItemBundle contentItems =
                        Externs.parseContentItems(Externs.parseReadMoreJSON(response.getText()));
                    load(contentItems.getCoreContentIds(), contentItems.getContentItemsById());
                  } else {
                    showError();
                  }
                }
              });
        } catch (RequestException e) {
          showError();
        }
      }
    });
  }
  
  public void clear() {
    nextPage = 0;
    moreLink.setVisible(false);
    loadingImage.setVisible(false);
    problemLabel.setVisible(false);
    contentItemList.clear();
  }
    
  public void showError() {
    loadingImage.setVisible(false);
    problemLabel.setVisible(true);
  }
  
  public void load(List<Integer> coreContentIds, Map<Integer, BaseContentItem> contentItemsById) {
    List<BaseContentItem> coreContentItems = new ArrayList<BaseContentItem>();
    for (Integer eventId : coreContentIds) {
      coreContentItems.add(contentItemsById.get(eventId));
    }

    contentItemList.appendContentItems(coreContentItems, contentItemsById);
    moreLink.setVisible(Externs.hasMoreItems());
    nextPage++;
  }
  
  public void doSimpleReversal(boolean oldestFirst) {
    contentItemList.adjustTimeOrdering(oldestFirst);
  }
  
  /**
   * "Jumps to" the item indicated by contentItemId, scrolling it into view and opening it.
   * @return true if the event was found, false otherwise.
   */
  public boolean goToContentItem(int contentItemId) {
    Set<Integer> contentItemIds = new HashSet<Integer>();
    contentItemIds.add(contentItemId);
    return contentItemList.openElements(contentItemIds);
  }
}
