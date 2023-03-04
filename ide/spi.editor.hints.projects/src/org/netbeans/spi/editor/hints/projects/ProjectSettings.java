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
package org.netbeans.spi.editor.hints.projects;

import java.util.prefs.Preferences;
import org.netbeans.modules.editor.tools.storage.api.ToolPreferences;
import org.netbeans.spi.project.LookupProvider;

/**Return project-specific hint settings. To be placed in the project's {@link Lookup}.
 *
 * @author lahvac
 * @see LookupProvider
 */
public interface ProjectSettings {
    
    /**The tool ID for hints that can be used in {@link ToolPreferences}.
     * 
     */
    public static final String HINTS_TOOL_ID = "hints";
    
    /**Whether the per-project settings should or should not be used. If <code>true</code>
     * is used, the {@link #getProjectSettings(java.lang.String) } should be used
     * to obtain the settings.
     * 
     * @return true if and only if the per-project hints settings should be used
     *              for this project.
     */
    public boolean getUseProjectSettings();
    
    /**Return the per-project hints settings.
     * 
     * @param mimeType the mime type for which the settings should be returned
     * @return the per-project settings
     */
    public Preferences getProjectSettings(String mimeType);
    
}
