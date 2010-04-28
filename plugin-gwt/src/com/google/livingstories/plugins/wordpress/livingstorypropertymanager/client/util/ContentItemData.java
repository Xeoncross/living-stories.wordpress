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

package com.google.livingstories.plugins.wordpress.livingstorypropertymanager.client.util;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.livingstories.plugins.wordpress.livingstorypropertymanager.client.Importance;
import com.google.livingstories.plugins.wordpress.livingstorypropertymanager.client.Location;

/**
 * Accesses the global CONTENT_METADATA object.
 */
public class ContentItemData {
  // Keys for getting metadata out of the global metadata object.
  public static final String IMPORTANCE_KEY = "importance";
  public static final String LATITUDE_KEY = "latitude";
  public static final String LONGITUDE_KEY = "longitude";
  public static final String LOCATION_DESC_KEY = "location_description";
  
  public static native MetadataObject getContentMetadata() /*-{
    if (!$wnd.CONTENT_METADATA) {
      $wnd.CONTENT_METADATA = {};
    }
    return $wnd.CONTENT_METADATA;
  }-*/;
  
  public static Importance getImportance() {
    String importance = getStringValue(IMPORTANCE_KEY);
    return importance == null ? null : Importance.valueOf(importance);
  }
  
  public static Location getLocation() {
    // Get these as string values and then convert to double instead of directly getting as doubles
    // so that we can detect whether the values exist. A JSNI method cannot return a null if the 
    // return type is double.
    String latitudeString = getStringValue(LATITUDE_KEY);
    String longitudeString = getStringValue(LONGITUDE_KEY);
    if (latitudeString == null || longitudeString == null) {
      return null;
    } else {
      String description = getStringValue(LOCATION_DESC_KEY);
      return new Location(Double.valueOf(latitudeString), Double.valueOf(longitudeString),
          description);
    }
  }
  
  private static String getStringValue(String key) {
    String value = getContentMetadata().getStringValue(key);
    return (value == null || value.isEmpty()) ? null : value;
  }
  
  private static final class MetadataObject extends JavaScriptObject {
    @SuppressWarnings("unused")
    protected MetadataObject() {}
    
    public native String getStringValue(String key) /*-{
      return this[key];
    }-*/;
    
    public native boolean getBooleanValue(String key) /*-{
      return this[key];
    }-*/;

    public native int getIntValue(String key) /*-{
      return this[key] ? this[key] : -1;
    }-*/;
    
    public native JavaScriptObject getObjectValue(String key) /*-{
      return this[key];
    }-*/;
  }
}
