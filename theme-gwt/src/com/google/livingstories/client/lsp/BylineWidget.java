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

import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.InlineHTML;
import com.google.gwt.user.client.ui.Widget;
import com.google.livingstories.client.BaseContentItem;

import java.util.List;

/**
 * Widget to create a byline from the contributors to a content item. Each of the names are linked.
 * Clicking on them takes you to the player page that lists all the contributions of that author.
 * This class also now includes a facility for prepending a widget that should be presented
 * alongside the BylineWidget, in a visually-consistent manner.
 */
public class BylineWidget extends Composite {
  private static final int MANUAL_BREAK_THRESHOLD = 8;
  
  private FlowPanel container;
  private InlineHTML manualBreak;
  private int navLinkCount = 0;
  private int prependedWidgetCount = 0;
  private int contributorCount;
  
  public BylineWidget(BaseContentItem contentItem) {
    this(contentItem, true);
  }
  
  public BylineWidget(BaseContentItem contentItem, boolean secondaryLinkStyle) {
    super();
    
    manualBreak = new InlineHTML("<br>");
    
    container = new FlowPanel();
    container.addStyleName("contributorList");
    container.add(manualBreak);
    
    initWidget(container);
  
    contributorCount = contentItem.getAuthorsCount();
    if (contributorCount > 0) {
      InlineHTML authorsLabel =
        new InlineHTML(contentItem.getBylineLeadin() + "&nbsp;" + contentItem.getAuthorsString());
      container.add(authorsLabel);
      // TODO: resolve how the handling of contributorCount works, since it isn't updated
      // properly in this code path
      contributorCount = contentItem.getAuthorsCount();
    }
    setManualBreakVisibility();
  }
  
  public void setNavLinks(List<Widget> navLinks) {
    // remove the old prepended links first, if any.
    for (int i = prependedWidgetCount - 1; i >= 0; i++) {
      container.remove(i);
    }

    // now add all the new links & separators:
    prependedWidgetCount = 0;
    for (Widget widget : navLinks) {
      container.insert(widget, prependedWidgetCount++);
      container.insert(new InlineHTML("&nbsp;| "), prependedWidgetCount++);   // separator
    }
    navLinkCount = navLinks.size();
    setManualBreakVisibility();
  }
  
  private void setManualBreakVisibility() {
    manualBreak.setVisible(navLinkCount > 0 && contributorCount > 0
        && navLinkCount + contributorCount > MANUAL_BREAK_THRESHOLD);
  }
    
  /**
   * Conditionally creates a new byline widget to a panel for the authors of a content item.
   * Only does this if there are contributors, and the contributors are _not the same_ as the
   * containing context's contributor set. (Partial overlap is okay.)
   * @param contentItem The content item that the byline widget is based on
   * @param containingAuthorsString The authors string of the content eventBlock, if any.
   *   If this argument is null, the method is a no-op; if this argument is an empty
   *   string, this will serve to always add a byline widget (provided there are _some_
   *   authors).
   * @return the newly-created byline widget, or null if one was not created.
   */
  public static BylineWidget makeContextSensitive(
      BaseContentItem contentItem, String containingAuthorsString) {
    String contentItemAuthorsString =  contentItem.getAuthorsString();
    if (containingAuthorsString!= null && !contentItemAuthorsString.isEmpty()
        && !contentItemAuthorsString.equals(containingAuthorsString)) {
      return new BylineWidget(contentItem);
    } else {
      return null;
    }
  }
}
