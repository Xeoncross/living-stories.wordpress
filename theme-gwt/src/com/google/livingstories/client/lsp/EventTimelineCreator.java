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
import com.google.gwt.http.client.Request;
import com.google.gwt.http.client.RequestBuilder;
import com.google.gwt.http.client.RequestCallback;
import com.google.gwt.http.client.RequestException;
import com.google.gwt.http.client.Response;
import com.google.gwt.http.client.UrlBuilder;
import com.google.gwt.user.client.Window;
import com.google.livingstories.client.BaseContentItem;
import com.google.livingstories.client.ContentItemType;
import com.google.livingstories.client.EventContentItem;
import com.google.livingstories.client.ui.TimelineData;
import com.google.livingstories.client.ui.TimelineWidget;
import com.google.livingstories.client.util.Constants;
import com.google.livingstories.client.util.DateUtil;
import com.google.livingstories.client.util.UriParser;

import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Provides methods for attaching a timeline to a specific DOM element and for loading
 * it with eventContentItem data when available.
 */
public class EventTimelineCreator {
  
  // Prevent instantiation
  private EventTimelineCreator() {}

  /**
   * Create a new timeline widget.
   * TODO: implement different policies on which events get mentioned in the timeline.
   *   Setup for those policies should be here; implementation in @link{loadTimeline}.
   */
  public static TimelineWidget<Integer> createTimeline(Integer width, Integer height) {
    final TimelineWidget<Integer> timeline = new TimelineWidget<Integer>(
        width, height, new TimelineWidget.OnClickBehavior<Integer>() {
          @Override
          public void onClick(ClickEvent event, Integer data) {
            UrlBuilder urlBuilder = Window.Location.createUrlBuilder();
            urlBuilder.removeParameter(Constants.FILTER_PANEL_ID_PARAM);
            urlBuilder.removeParameter(Constants.FILTER_LINKED_POST_ID_PARAM);
            urlBuilder.setParameter(Constants.JUMP_TO_ID_PARAM, Integer.toString(data));
            Window.Location.assign(urlBuilder.buildString());
          }
        });
    timeline.setVisible(false);

    // Load the timeline data asynchronously.
    UrlBuilder urlBuilder = UriParser.parse(Externs.getPostLoaderUrl());
    urlBuilder.setParameter("cat", Integer.toString(Externs.getLivingStoryId()));
    urlBuilder.setParameter(
        Constants.FILTER_IMPORTANCE_PARAM, Constants.FILTER_HIGH_IMPORTANCE_VALUE);
    
    try {
      new RequestBuilder(RequestBuilder.GET, urlBuilder.buildString()).sendRequest(null,
          new RequestCallback() {
            public void onError(Request request, Throwable exception) {}
            public void onResponseReceived(Request request, Response response) {
              if (200 == response.getStatusCode()) {
                ContentItemBundle contentItems =
                    Externs.parseContentItems(Externs.parseReadMoreJSON(response.getText()));
                List<EventContentItem> events = new ArrayList<EventContentItem>();
                for (BaseContentItem content : contentItems.getContentItemsById().values()) {
                  if (content.getContentItemType() == ContentItemType.EVENT) {
                    events.add((EventContentItem) content);
                  }
                }
                loadTimeline(timeline, events);
              }
            }
        });
    } catch (RequestException e) {
      // Ignore this for now
    }

    return timeline;
  }

  /**
   * Actually loads timeline data into the timeline widget.
   */
  public static void loadTimeline(TimelineWidget<Integer> timeline,
      List<EventContentItem> importantEvents) {
    // TODO: implement policies other than "important events only".
    Map<Date, TimelineData<Integer>> pointEvents = new LinkedHashMap<Date, TimelineData<Integer>>();
    Map<TimelineWidget.Interval, TimelineData<Integer>> rangeEvents =
      new LinkedHashMap<TimelineWidget.Interval, TimelineData<Integer>>();
    Date earliest = null, latest = null;
    
    for (EventContentItem event : importantEvents) {
      // TODO: very similar to code in DateTimeRangeWidget.makeForEventContentitem. Refactor.
      Date eventDate = event.getEventDate();

      if (eventDate == null) {
        // If there is no special date on the event, use the creation time
        // as a backup.
        eventDate = event.getTimestamp();
      }

      Date effectiveEndDate;
      TimelineData<Integer> data = makeData(event.getEventUpdate(), event.getId());

      pointEvents.put(eventDate, data);
      effectiveEndDate = eventDate;

      if (earliest == null || earliest.after(eventDate)) {
        earliest = eventDate;
      }
      if (latest == null || latest.before(effectiveEndDate)) {
        latest = effectiveEndDate;
      }
    }
    
    // earliest and latest are nominally the endpoints for our timeline. Pad them out by
    // another ~10%, or 2 days, whichever is greater.
    long timePadding = Math.max((latest.getTime() - earliest.getTime()) / 10,
        2 * DateUtil.MILLISECONDS_PER_DAY);
    
    earliest = new Date(earliest.getTime() - timePadding);
    latest = new Date(latest.getTime() + timePadding);
    
    if (!(rangeEvents.isEmpty() && pointEvents.isEmpty())) {
      timeline.load(new TimelineWidget.Interval(earliest, latest), pointEvents, rangeEvents);
      timeline.setVisible(true);
    }
  }
  
  private static TimelineData<Integer> makeData(String label, int data) {
    return new TimelineData<Integer>(label, data);
  }
}
