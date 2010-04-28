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

package com.google.livingstories.plugins.wordpress.livingstorypropertymanager.client.i18n;

import com.google.livingstories.plugins.wordpress.livingstorypropertymanager.client.Importance;

/**
 * Handles client-side translation of enum values and enum-related fields
 */
public class EnumTranslator {
  private static ClientConstants consts = ClientMessageHolder.consts;
  
  public static String translate(Importance importance) {
    switch (importance) {
      case HIGH:
        return consts.importanceHigh();
      case MEDIUM:
        return consts.importanceMedium();
      case LOW:
        return consts.importanceLow();
      default:
        assert false;
        return null;
    }
  }

  public static String defaultOrOverride(String defaultString, String override) {
    return override.equals("") ? defaultString : override;
  }
}
