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

package com.google.livingstories.client;

import java.util.Date;

/**
 * Client-side version of a data content entity
 */
public class DataContentItem extends BaseContentItem {
  public DataContentItem() {}
  
  public DataContentItem(int id, Date timestamp, String authorsString, int authorsCount,
      String content, Importance importance) {
    super(id, timestamp, ContentItemType.DATA, authorsString, authorsCount, content, importance);
  }
  
  // Superclass implementation of renderContent should be fine

}
