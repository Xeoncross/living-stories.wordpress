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

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.core.client.JsArray;
import com.google.gwt.core.client.JsArrayInteger;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.i18n.client.Dictionary;
import com.google.gwt.user.client.Window;
import com.google.livingstories.client.AssetContentItem;
import com.google.livingstories.client.AssetType;
import com.google.livingstories.client.BackgroundContentItem;
import com.google.livingstories.client.BaseContentItem;
import com.google.livingstories.client.ContentItemType;
import com.google.livingstories.client.DataContentItem;
import com.google.livingstories.client.EventContentItem;
import com.google.livingstories.client.Importance;
import com.google.livingstories.client.NarrativeContentItem;
import com.google.livingstories.client.NarrativeType;
import com.google.livingstories.client.PlayerContentItem;
import com.google.livingstories.client.PlayerType;
import com.google.livingstories.client.QuoteContentItem;
import com.google.livingstories.client.ReactionContentItem;
import com.google.livingstories.client.util.Constants;
import com.google.livingstories.client.util.GlobalUtil;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Defines an api usable by external javascript to access GWT objects.
 */
public class Externs {
  private static final DateTimeFormat dateFormat = DateTimeFormat.getFormat("yyyy-MM-dd hh:mm a");

  // Initialize a map from the write panel names used in magic fields to the corresponding content
  // item types
  private static final Map<String, ContentItemType> panelNamesToContentTypes;
  static {
    Map<String, ContentItemType> tempMap = new HashMap<String, ContentItemType>();
    tempMap.put("events", ContentItemType.EVENT);
    tempMap.put("narratives", ContentItemType.NARRATIVE);
    tempMap.put("players", ContentItemType.PLAYER);
    tempMap.put("background", ContentItemType.BACKGROUND);
    tempMap.put("images", ContentItemType.ASSET);
    tempMap.put("videos", ContentItemType.ASSET);
    tempMap.put("audio", ContentItemType.ASSET);
    tempMap.put("graphics", ContentItemType.ASSET);
    tempMap.put("resources", ContentItemType.ASSET);
    tempMap.put("documents", ContentItemType.ASSET);
    tempMap.put("quotes", ContentItemType.QUOTE);
    tempMap.put("facts", ContentItemType.DATA);
    tempMap.put("reactions", ContentItemType.REACTION);
    panelNamesToContentTypes = Collections.unmodifiableMap(tempMap);
  }
  private static final Map<String, AssetType> panelNamesToAssetTypes;
  static {
    Map<String, AssetType> tempMap = new HashMap<String, AssetType>();
    tempMap.put("images", AssetType.IMAGE);
    tempMap.put("videos", AssetType.VIDEO);
    tempMap.put("audio", AssetType.AUDIO);
    tempMap.put("graphics", AssetType.INTERACTIVE);
    tempMap.put("resources", AssetType.LINK);
    tempMap.put("documents", AssetType.DOCUMENT);
    panelNamesToAssetTypes = Collections.unmodifiableMap(tempMap);
  }

  private static Map<String, List<String>> currentParameters;
  private static Set<ContentItemType> coreItemTypes = new HashSet<ContentItemType>();
  private static Set<AssetType> coreItemAssetTypes = new HashSet<AssetType>();
  private static List<Integer> coreItemIds = new ArrayList<Integer>();
  private static Map<Integer, BaseContentItem> contentItemsById =
      new HashMap<Integer, BaseContentItem>();
  private static Map<Integer, String> themesById = new HashMap<Integer, String>();
  // Maps from content type to the magic field write panel id for that content type.
  private static Map<ContentItemType, Integer> contentTypesToPanelIds =
      new HashMap<ContentItemType, Integer>();
  // Maps from asset type to the magic field write panel id for that asset type.
  private static Map<AssetType, Integer> assetTypesToPanelIds =
      new HashMap<AssetType, Integer>();
  
  public static void bind() {
    currentParameters = Window.Location.getParameterMap();

    Set<Integer> corePanelIds = new HashSet<Integer>();
    JsArrayInteger corePanelIdsJs = getCorePanelIds();
    if (corePanelIdsJs != null) {
      for (int i = 0; i < corePanelIdsJs.length(); i++) {
        corePanelIds.add(corePanelIdsJs.get(i));
      }
    }
    
    Dictionary itemTypes = Dictionary.getDictionary("ITEM_TYPES");
    for (String key : itemTypes.keySet()) {
      String itemType = itemTypes.get(key);
      int panelId = Integer.valueOf(key);
      boolean isCurrentFilteredType = corePanelIds.contains(panelId);
      ContentItemType contentType = getContentTypeForFilters(itemType);
      if (contentType == null) {
        throw new IllegalArgumentException("No matching content type: " + itemType);
      } else if (contentType != ContentItemType.ASSET) {
        contentTypesToPanelIds.put(contentType, panelId);
        if (isCurrentFilteredType) {
          coreItemTypes.add(contentType);
        }
      } else {
        AssetType assetType = getAssetTypeForFilters(itemType);
        assetTypesToPanelIds.put(assetType, panelId);
        if (isCurrentFilteredType) {
          coreItemTypes.add(ContentItemType.ASSET);
          coreItemAssetTypes.add(assetType);
        }
      }
    }

    Dictionary themes = Dictionary.getDictionary("THEMES");
    for (String key : themes.keySet()) {
      themesById.put(Integer.valueOf(key), themes.get(key));
    }
    
    parseContentItems(getContentItemsJs());
  }
  
  public static ContentItemBundle parseContentItems(JsArray<JsContentItem> contentItemsJs) {
    ContentItemBundle result = new ContentItemBundle();
    for (int i = 0; i < contentItemsJs.length(); i++) {
      JsContentItem jsItem = contentItemsJs.get(i);
      BaseContentItem item = createContentItem(jsItem);
      result.addContentItem(item, isCoreItem(item));
    }
    coreItemIds.addAll(result.getCoreContentIds());
    contentItemsById.putAll(result.getContentItemsById());
    return result;
  }
  
  public static List<Integer> getCoreItemIds() {
    return coreItemIds;
  }
  
  public static Map<Integer, BaseContentItem> getContentItemsById() {
    return contentItemsById;
  }
  
  public static native boolean hasMoreItems() /*-{
    return $wnd.HAS_MORE_ITEMS;
  }-*/;
  
  public static native int getLivingStoryId() /*-{
    return $wnd.LIVING_STORY_ID;
  }-*/;
  
  public static native String getLivingStorySummary() /*-{
    return $wnd.LIVING_STORY_SUMMARY;
  }-*/;
  
  public static Map<Integer, String> getThemesById() {
    return themesById;
  }
  
  public static Map<ContentItemType, Integer> getContentTypesToPanelIds() {
    return contentTypesToPanelIds;
  }

  public static Map<AssetType, Integer> getAssetTypesToPanelIds() {
    return assetTypesToPanelIds;
  }

  public static native String getTemplateDirectory() /*-{
    return $wnd.TEMPLATE_DIRECTORY;
  }-*/;

  public static native String getPostLoaderUrl() /*-{
    return $wnd.POST_LOADER_URL
  }-*/;
  
  public static native JsArrayInteger getCorePanelIds() /*-{
    return $wnd.CORE_ITEM_TYPES;
  }-*/;
  
  public static int getJumpToId() {
    return currentParameters.containsKey(Constants.JUMP_TO_ID_PARAM)
        ? Integer.valueOf(currentParameters.get(Constants.JUMP_TO_ID_PARAM).get(0))
        : 0;
  }
  
  public static Integer getThemeId() {
    return currentParameters.containsKey(Constants.THEME_ID_PARAM)
        ? Integer.valueOf(currentParameters.get(Constants.THEME_ID_PARAM).get(0))
        : null;
  }
  
  public static int getFilterPanelId() {
    return currentParameters.containsKey(Constants.FILTER_PANEL_ID_PARAM)
        ? Integer.valueOf(currentParameters.get(Constants.FILTER_PANEL_ID_PARAM).get(0))
        : 0;
  }

  public static int getFilterLinkedPostId() {
    return currentParameters.containsKey(Constants.FILTER_LINKED_POST_ID_PARAM)
        ? Integer.valueOf(currentParameters.get(Constants.FILTER_LINKED_POST_ID_PARAM).get(0))
        : 0;
  }
  
  public static boolean getFilterImportantOnly() {
    return currentParameters.containsKey(Constants.FILTER_IMPORTANCE_PARAM)
        && Constants.FILTER_HIGH_IMPORTANCE_VALUE.equals(
            currentParameters.get(Constants.FILTER_IMPORTANCE_PARAM).get(0));
  }
  
  public static boolean getFilterChronological() {
    return currentParameters.containsKey(Constants.FILTER_ORDER_PARAM)
        && Constants.FILTER_CHRONOLOGICAL_VALUE.equals(
            currentParameters.get(Constants.FILTER_ORDER_PARAM).get(0));
  }

  // Determine if the given item is a 'core' item, which we will show at the top
  // level in the content list.
  private static boolean isCoreItem(BaseContentItem item) {
    if (coreItemTypes.isEmpty()) {
      // All items are considered 'core' if no types are explicitly specified.
      return true;
    } else if (coreItemTypes.contains(item.getContentItemType())) {
      // Otherwise, make sure the coreItemTypes/coreItemAssetTypes contains
      // this item's type.
      return item.getContentItemType() != ContentItemType.ASSET
          || coreItemAssetTypes.contains(((AssetContentItem) item).getAssetType());
    } else {
      // Didn't match a core type, so this is not a core item.
      return false;
    }
  }
  
  // Get the content type equivalent for the given write panel name, for use with
  // the filters.
  private static ContentItemType getContentTypeForFilters(String panelName) {
    return panelNamesToContentTypes.get(panelName.toLowerCase());
  }
  
  // Get the asset type equivalent for the given write panel name, for use with
  // the filters.
  private static AssetType getAssetTypeForFilters(String panelName) {
    AssetType assetType = panelNamesToAssetTypes.get(panelName.toLowerCase());
    if (assetType == AssetType.DOCUMENT) {
      // Note that for filtering purposes, we treat put documents under the same
      // filter as links.
      assetType = AssetType.LINK;
    }
    return assetType;
  }

  // Method to parse the JSON response from a 'read more' request.
  // Automatically changes the 'has more items' field based on the response data.
  public static native JsArray<JsContentItem> parseReadMoreJSON(String json) /*-{
    // Parens are necessary to prevent 'invalid label' errors when evaling json.
    // This is due to an ambiguity in javascript syntax that makes eval think the
    // JSON is actually a code label.
    var response = eval("(" + json + ")");
    $wnd.HAS_MORE_ITEMS = response["HAS_MORE_ITEMS"];
    return response["CONTENT_ITEMS"];
  }-*/;
  
  private static native JsArray<JsContentItem> getContentItemsJs() /*-{
    return $wnd.CONTENT_ITEMS;
  }-*/;

  private static class JsContentItem extends JavaScriptObject {
    protected JsContentItem() {}
    
    /*** Common methods ***/
    
    public final int getId() {
      return getPositiveIntValue("id");
    }
    
    public final ContentItemType getContentItemType() {
      String panelName = getStringValue("panelName");
      return GlobalUtil.isContentEmpty(panelName) ? null : panelNamesToContentTypes.get(panelName);
    }
    
    public final Date getTimestamp() {
      String timestampString = getStringValue("timestamp");
      return GlobalUtil.isContentEmpty(timestampString) ? null : dateFormat.parse(timestampString);
    }
    
    public final Importance getImportance() {
      String importanceString = getStringValue("importance");
      return GlobalUtil.isContentEmpty(importanceString) ? 
          Importance.MEDIUM : Importance.valueOf(importanceString);
    }
    
    public final Set<Integer> getLinkedContentIds() {
      String idSetString = getStringValue("linkedContentIds");
      return GlobalUtil.isContentEmpty(idSetString) ? new HashSet<Integer>() : getIdSet(idSetString);
    }
    
    public final String getContent() {
      return getStringValue("content");
    }

    public final String getAuthorsString() {
      return getStringValue("authorsString");
    }
  
    public final int getAuthorsCount() {
      return getPositiveIntValue("authorsCount");
    }

    /*** Common event and narrative methods ***/
    
    public final String getUpdate() {
      return getStringValue("update");
    }
  
    public final String getSummary() {
      return getStringValue("summary");
    }
  
    /*** Event methods ***/

    public final Date getEventDate() {
      return getDate(getStringValue("eventDate"), getStringValue("eventTime"));
    }
    
    /*** Narrative methods ***/

    public final Date getNarrativeDate() {
      return getDate(getStringValue("narrativeDate"), getStringValue("narrativeTime"));
    }

    public final NarrativeType getNarrativeType() {
      String narrativeTypeString = getStringValue("narrativeType");
      return GlobalUtil.isContentEmpty(narrativeTypeString) ? NarrativeType.FEATURE : 
          NarrativeType.valueOf(narrativeTypeString.toUpperCase().replace("-", "_").replace(" ", "_"));
    }
    
    /*** Player methods ***/

    public final String getPlayerName() {
      return getStringValue("playerName");
    }

    public final List<String> getPlayerAliases() {
      List<String> aliasList = new ArrayList<String>();
      String aliases = getStringValue("playerAliases");
      if (!GlobalUtil.isContentEmpty(aliases)) {
        for (String alias : aliases.split(",")) {
          String trimmed = alias.trim();
          if (!trimmed.isEmpty()) {
            aliasList.add(trimmed);
          }
        }
      }
      return aliasList;
    }
    
    public final PlayerType getPlayerType() {
      String playerTypeString = getStringValue("playerType");
      return GlobalUtil.isContentEmpty(playerTypeString) ? 
          PlayerType.PERSON : PlayerType.valueOf(playerTypeString.toUpperCase());
    }
    
    public final int getPlayerPhotoId() {
      return getPositiveIntValue("playerPhoto");
    }
    
    /*** Common asset methods ***/
    
    public final AssetType getAssetType() {
      String assetTypeString = getStringValue("assetType");
      return GlobalUtil.isContentEmpty(assetTypeString) ? null : AssetType.valueOf(assetTypeString);
    }
    
    public final String getPreviewUrl() {
      return getStringValue("previewUrl");
    }

    public final String getCaption() {
      return getStringValue("caption");
    }
    
    /*** Image methods ***/
    
    public final String getImageUrl() {
      return getStringValue("imageUrl");
    }
    
    /*** Background methods ***/
    
    public final String getConceptName() {
      return getStringValue("conceptName");
    }
    
    /*** Internal methods ***/
    
    private final native String getStringValue(String key) /*-{
      return this[key];
    }-*/;
    
    public final native int getPositiveIntValue(String key) /*-{
      if (this[key]) {
        return this[key];
      } else {
        return 0;
      }
    }-*/;
    
    private final Date getDate(String dateString, String timeString) {
      if (GlobalUtil.isContentEmpty(timeString)) {
        timeString = "12:00 am";
      }
      return GlobalUtil.isContentEmpty(dateString) ? null : 
          dateFormat.parse(dateString + " " + (timeString.trim()));
    }
    
    private Set<Integer> getIdSet(String idString) {
      Set<Integer> idSet = new HashSet<Integer>();
      if (!idString.isEmpty()) {
        String[] ids = idString.split(",");
        for (String id : ids) {
          idSet.add(Integer.valueOf(id));
        }
      }
      return idSet;
    }
  }
  
  private static BaseContentItem createContentItem(JsContentItem item) {
    BaseContentItem content;
    switch (item.getContentItemType()) {
      case EVENT:
        content = new EventContentItem(
            item.getId(),
            item.getTimestamp(),
            item.getAuthorsString(),
            item.getAuthorsCount(),
            item.getImportance(),
            item.getEventDate(),
            item.getUpdate(),
            item.getSummary(),
            item.getContent());
        break;
      case NARRATIVE:
        content = new NarrativeContentItem(
            item.getId(),
            item.getTimestamp(),
            item.getAuthorsString(),
            item.getAuthorsCount(),
            item.getContent(),
            item.getImportance(),
            item.getUpdate(),
            item.getNarrativeType(),
            true, // TODO: put the real value of isStandalone here
            item.getNarrativeDate(),
            item.getSummary());
        break;
      case PLAYER:
        content = new PlayerContentItem(
            item.getId(),
            item.getTimestamp(),
            item.getAuthorsString(),
            item.getAuthorsCount(),
            item.getContent(),
            item.getImportance(),
            item.getPlayerName(),
            item.getPlayerAliases(),
            item.getPlayerType(),
            null); // TODO: replace with actual AssetContentItem if the photoId != -1
        break;
      case ASSET:
        AssetType assetType = item.getAssetType();
        content = new AssetContentItem(
            item.getId(),
            item.getTimestamp(),
            item.getAuthorsString(),
            item.getAuthorsCount(),
            assetType == AssetType.IMAGE ? item.getImageUrl() : item.getContent(),
            item.getImportance(),
            assetType,
            item.getCaption(),
            item.getPreviewUrl());
        break;
      case BACKGROUND:
        content = new BackgroundContentItem(
            item.getId(),
            item.getTimestamp(),
            item.getAuthorsString(),
            item.getAuthorsCount(),
            item.getContent(),
            item.getImportance(),
            item.getConceptName());
        break;
      case QUOTE:
        content = new QuoteContentItem(
            item.getId(),
            item.getTimestamp(),
            item.getAuthorsString(),
            item.getAuthorsCount(),
            item.getContent(),
            item.getImportance());
        break;
      case DATA:
        content = new DataContentItem(
            item.getId(),
            item.getTimestamp(),
            item.getAuthorsString(),
            item.getAuthorsCount(),
            item.getContent(),
            item.getImportance());
        break;
      case REACTION:
        content = new ReactionContentItem(
            item.getId(),
            item.getTimestamp(),
            item.getAuthorsString(),
            item.getAuthorsCount(),
            item.getContent(),
            item.getImportance());
        break;
      default:
        return null;
    }
    content.setLinkedContentItemIds(item.getLinkedContentIds());
    return content;
  }
}
