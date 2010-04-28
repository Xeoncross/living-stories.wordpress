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

import com.google.gwt.http.client.UrlBuilder;

/**
 * Parses a string url and returns a UrlBuilder for that url.
 * We need this until GWT adds a way to parse string URLs to their
 * UrlBuilder class.
 */
public class UriParser {
  public static UrlBuilder parse(String url) {
    UrlBuilder urlBuilder = new UrlBuilder();

    // This utility only needs to support http urls.
    urlBuilder.setProtocol("http");
    urlBuilder.setHost(matchHost(url));
    urlBuilder.setPath(matchPath(url));
    
    String queryString = matchQuery(url);
    if (!queryString.isEmpty()) {
      for (String params : queryString.split("&")) {
        // Doesn't work for urls with multiple values on the same key.
        // But we don't currently need that.
        String[] pair = params.split("=");
        urlBuilder.setParameter(pair[0], pair.length > 1 ? pair[1] : "");
      }
    }
    
    return urlBuilder;
  }
  
  private static native String matchHost(String url) /*-{
    var HOST_REGEX = /^https?:\/\/([^\/]+)(\/.*)?$/
    return HOST_REGEX.exec(url)[1];
  }-*/;
  
  private static native String matchPath(String url) /*-{
    var PATH_REGEX = /^https?:\/\/[^\/]+\/([^\?]+)/;
    var result = PATH_REGEX.exec(url);
    return result && result[1] || "";
  }-*/;

  private static native String matchQuery(String url) /*-{
    var QUERY_REGEX = /^[^\?]+\?(.*)/;
    var result = QUERY_REGEX.exec(url);
    return result && result[1] || "";
  }-*/;
}
