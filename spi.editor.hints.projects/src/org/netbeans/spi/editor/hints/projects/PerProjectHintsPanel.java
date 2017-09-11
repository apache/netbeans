/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2013 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2013 Sun Microsystems, Inc.
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
