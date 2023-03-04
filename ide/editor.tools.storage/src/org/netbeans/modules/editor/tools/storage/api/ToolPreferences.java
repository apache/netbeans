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
package org.netbeans.modules.editor.tools.storage.api;

import java.io.IOException;
import java.net.URI;
import java.util.prefs.Preferences;
import org.netbeans.modules.editor.tools.storage.api.XMLHintPreferences.HintPreferencesProviderImpl;

/**Persistent storage of {@link Preferences}, sorted by tools and mime-types.
 *
 * @author lahvac
 */
public class ToolPreferences {
    private final HintPreferencesProviderImpl prefs;

    private ToolPreferences(HintPreferencesProviderImpl prefs) {
        this.prefs = prefs;
    }

    /**Read the specified file and construct the {@link ToolPreferences}.
     * 
     * @param source from which the settings should be read
     * @return a new instance of {@link ToolPreferences}
     */
    public static ToolPreferences from(URI source) {
        return new ToolPreferences(XMLHintPreferences.from(source));
    }
    
    /**Get {@link Preferences} for the given tool and mime-type. The settings are
     * read-write, and are saved automatically, but it is recommended to call
     * {@link Preferences#flush() } or {@link #save() } to ensure they are saved
     * immediately.
     * 
     * @param toolId id of the tool
     * @param mimeType mime-type for which the settings should be gathered
     * @return the settings
     */
    public Preferences getPreferences(String toolId, String mimeType) {
        return prefs.getPreferences(toolId, mimeType);
    }
    
    /**Saves any changes done to the {@link Preferences}.
     * 
     * @throws IOException if the settings cannot be written
     */
    public void save() throws IOException {
        prefs.save();
    }
}
