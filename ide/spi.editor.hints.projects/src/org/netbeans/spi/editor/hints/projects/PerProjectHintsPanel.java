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
import javax.swing.JComponent;
import org.netbeans.modules.editor.tools.storage.api.ToolPreferences;
import org.openide.filesystems.FileObject;

/**Provides a general panel that shows all registered hints customizers.
 *
 * @author lahvac
 */
public class PerProjectHintsPanel {

    /**Create a new instance of <code>PerProjectHintsPanel</code>.
     * 
     * @param customizersFolder folder from which the customizers should be read.
     * @return a new instance of <code>PerProjectHintsPanel</code>
     */
    public static PerProjectHintsPanel create(FileObject customizersFolder) {
        return new PerProjectHintsPanel(customizersFolder);
    }
    
    private final PerProjectHintsPanelUI panel;
    
    private PerProjectHintsPanel(FileObject customizersFolder) {
        panel = new PerProjectHintsPanelUI(customizersFolder);
    }
    
    /**Get the actual component that will show the customizers.
     * 
     * @return the customizers component
     */
    public JComponent getPanel() {
        return panel;
    }
    
    /**Set that per-project settings should be shown by the customizers. Read settings
     * from the given {@link ToolPreferences}.
     * 
     * @param preferencesProvider settings to use
     */
    public void setPerProjectSettings(final ToolPreferences preferencesProvider) {
        setPerProjectSettings(new MimeType2Preferences() {
            @Override public Preferences getPreferences(String mimeType) {
                return preferencesProvider.getPreferences(ProjectSettings.HINTS_TOOL_ID, mimeType);
            }
        });
    }
    
    /**Set that per-project settings should be shown by the customizers. Read settings
     * from the given {@link MimeType2Preferences}.
     * 
     * @param preferences settings to use
     */
    public void setPerProjectSettings(final MimeType2Preferences preferences) {
        panel.setPerProjectSettings(preferences);
    }
    
    /**Set that global setting should be shown by the customizers.
     */
    public void setGlobalSettings() {
        panel.setGlobalSettings();
    }

    /**Writes all changes.
     */
    public void applyChanges() {
        panel.applyChanges();
    }

    /**Retrieve hint settings for the given mime type.
     * 
     */
    public interface MimeType2Preferences {
        /**Settings for the given mime type.
         * 
         * @param mimeType for which the settings should be retrieved
         * @return the settings
         */
        public Preferences getPreferences(String mimeType);
    }
}
