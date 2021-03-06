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

import java.util.List;
import java.util.Map;

/**
 * RPC service for saving and retrieving living stories from the datastore.
 */
public interface LivingStoryRpcService {
  // Theme management functions. Themes are sufficiently tied to living stories that
  // their implementation belongs on the same service.
  List<Theme> getThemesForLivingStory(long livingStoryId);
  
  Map<Long, ContentItemTypesBundle> getThemeInfoForLivingStory(long livingStoryId);
  
  Theme getThemeById(long id);
}
