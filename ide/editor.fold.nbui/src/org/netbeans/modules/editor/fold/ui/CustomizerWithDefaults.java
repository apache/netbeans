/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.netbeans.modules.editor.fold.ui;

import java.util.prefs.Preferences;

/**
 * Mixin interface, that allows to configure a Customizer to be configured with parent preferences.
 * The parent preferences may be either defaults stored or predefined, or preferences for the parent Mime type.
 * The customizer <b>must not</b> write to the default preferences
 * 
 * @author sdedic
 */
public interface CustomizerWithDefaults {
    /**
     * Provides default preferences to the customizer. All language preferences,
     * or text/xml preferences are passed this way to the language customizer,
     * to serve as a basis. The default preferences 
     * 
     * @param pref default preferences
     */
    public void setDefaultPreferences(Preferences pref);
}
