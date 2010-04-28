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

package com.google.livingstories.plugins.wordpress.livingstorypropertymanager.client;

import com.google.gwt.ajaxloader.client.AjaxLoader;
import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.livingstories.plugins.wordpress.livingstorypropertymanager.client.i18n.ClientConstants;
import com.google.livingstories.plugins.wordpress.livingstorypropertymanager.client.util.Resources;

/**
 * Widget entry point
 */
public class Plugin implements EntryPoint {
  public static ClientConstants properties = GWT.create(ClientConstants.class);
  
  public void onModuleLoad() {
    Resources.INSTANCE.css().ensureInjected();
    AjaxLoader.init();
    
    RootPanel.get("contentManagerBox").add(new LivingStoryPropertyManager());
  }
}
