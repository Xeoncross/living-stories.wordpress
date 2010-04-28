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

package com.google.livingstories.client.util;

import com.google.livingstories.client.Importance;
import com.google.livingstories.client.lsp.Externs;

/**
 * Class that stores shared constants.
 */
public final class Constants {
  public static int CONTENT_SNIPPET_LENGTH = 40;
  public static int ANIMATION_DURATION = 350;
  public static final int MAX_IMAGE_PREVIEW_WIDTH = 200;

  public static String ZOOM_ICON = Externs.getTemplateDirectory() + "/images/zoom_icon.png";
  public static int ZOOM_WIDTH = 16;
  public static int ZOOM_HEIGHT = 16;
  
  public static final String BREAK_TAG = "<break></break>";

  public static String CLOSE_IMAGE_URL =
      Externs.getTemplateDirectory() + "/images/lightbox-close.gif";

  public static final String JUMP_TO_ID_PARAM = "jumpTo";
  public static final String THEME_ID_PARAM = "themeId";
  
  public static final String FILTER_PANEL_ID_PARAM = "panelId";
  public static final String FILTER_LINKED_POST_ID_PARAM = "linkedPostId";
  public static final String FILTER_IMPORTANCE_PARAM = "importance";
  public static final String FILTER_ORDER_PARAM = "order";
  public static final String FILTER_PAGE_PARAM = "page";

  public static final String FILTER_HIGH_IMPORTANCE_VALUE = Importance.HIGH.name();
  public static final String FILTER_CHRONOLOGICAL_VALUE = "ASC";
  
  public static String getCookieName(String url) {
    return url + "visit";
  }
  
  // prevent instantiation
  private Constants() {}
}
