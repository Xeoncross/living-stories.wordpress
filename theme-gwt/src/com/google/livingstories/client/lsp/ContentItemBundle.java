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

import com.google.livingstories.client.BaseContentItem;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Bundle of content items plus a list of the ones considered 'core' content,
 * which we will display in the content list.
 */
public class ContentItemBundle {
  private List<Integer> coreContentIds = new ArrayList<Integer>();
  private Map<Integer, BaseContentItem> contentItemsById = new HashMap<Integer, BaseContentItem>();

  public void addContentItem(BaseContentItem item, boolean isCoreItem) {
    contentItemsById.put(item.getId(), item);
    if (isCoreItem) {
      coreContentIds.add(item.getId());
    }
  }
  
  public List<Integer> getCoreContentIds() {
    return new ArrayList<Integer>(coreContentIds);
  }
  
  public Map<Integer, BaseContentItem> getContentItemsById() {
    return new HashMap<Integer, BaseContentItem>(contentItemsById);
  }
}
