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

package org.netbeans.modules.options.editor.spi;

import java.util.prefs.Preferences;
import javax.swing.JComponent;
import org.openide.util.HelpCtx;

/**
 * A customizer for editor preferences. This interface can be used for implementing
 * a UI panel, which allows users to controll editor preferences.
 *
 * <p class="nonnormative">This interface is currently used for editor formatting
 * settings and their customizers supplied by particular language support modules.
 * The customizers are shown in both the Tools-Options dialog and the project properties
 * dialog for an individual project.
 *
 * <p>Modules can register <code>PreferencesCustomizer.Factory</code> in
 * <code>OptionsDialog/Editor/Formatting/&lt;mime-type&gt;</code> folder on the
 * system filesystem. The editor options infrastructure will use registered factories
 * for creating <code>PreferencesCustomizer</code>s and their UI components, which
 * it will then show in the appropriate dialog.
 *
 * @author vita
 */
public interface PreferencesCustomizer {

    /**
     * The id of a customizer that controls settings such as tab size, indent
     * size, right margin, etc.
     */
    static final String TABS_AND_INDENTS_ID = "tabs-and-indents"; //NOI18N

    /**
     * Gets an id that will uniquely identify this customizer.
     *
     * @return The customizer's identification.
     */
    String getId();

    /**
     * Gets the customizer's display name. The display name will be shown to
     * a user whenever it is needed to refer to this customizer.
     *
     * @return The customizer's display name.
     */
    String getDisplayName();

    /**
     * Gets the help context for showing help information relevant for this customizer.
     *
     * @return The customizer's help context.
     */
    HelpCtx getHelpCtx();

    /**
     * Gets the component with UI controls for customizing preferences controlled
     * by this customizer. The UI controls shoudl treat the <code>Preferences</code>
     * instance supplied to this customizer as their model. That is it should reflect
     * any changes done to the <code>preferences</code>' values they are
     * presenting in the UI and also write any changes done by user back to the
     * <code>Preferences</code> instance.
     *
     * @return The UI component for customizing the preferences.
     */
    JComponent getComponent();

    /**
     * A factory for <code>PreferencesCustomizer</code>. The factory is given
     * an instance of <code>Preferences</code>, which are the subject of customization.
     * The <code>PreferencesCustomizer</code> created by the factory is expected to
     * read and write values from the <code>Preferences</code> instance passed to
     * the {@link Factory#create(java.util.prefs.Preferences)} method.
     *
     * <p>If you need to show the default 'Tabs And Indents' customizer for your language
     * you can use the following XML layer
     * registration.
     *
     * <pre style="background-color: rgb(255, 255, 153);">
     * &lt;folder name="OptionsDialog"&gt;
     *   &lt;folder name="Editor"&gt;
     *     &lt;folder name="Formatting"&gt;
     *       &lt;folder name="your"&gt;
     *         &lt;folder name="mimetype"&gt;
     *           &lt;file name="TabsAndIndents.instance"&gt;
     *             &lt;attr name="instanceOf" methodvalue="org.netbeans.modules.options.editor.spi.PreferencesCustomizer$Factory"/&gt;
     *             &lt;attr name="instanceCreate" methodvalue="org.netbeans.modules.options.editor.spi.CustomizerFactories.createDefaultTabsAndIndentsCustomizerFactory"/&gt;
     *             &lt;attr name="previewTextFile" stringvalue="file/with/preview/text"/&gt;
     *
     *             &lt;attr name="position" intvalue="100"/&gt;
     *           &lt;/file&gt;
     *         &lt;/folder&gt;
     *       &lt;/folder&gt;
     *     &lt;/folder&gt;
     *   &lt;/folder&gt;
     * &lt;/folder&gt;</pre>
     *
     * <p>The <code>previewTextFile</code> attribute is optional. If it is not specified the
     * preview text is loaded from <code>OptionsDialog/PreviewExamples/your/mimetype</code> file.
     * If the attribute is specified it should contain the full path to the text preview file
     * on the system filesystem.
     */
    public static interface Factory {

        /**
         * Creates a new <code>PreferencesCustomizer</code>.
         *
         * @param preferences The <code>Preferences</code> instance containing values
         *   that should be customized.
         * @return A new <code>PreferencesCustomizer</code> which will provide
         *   UI for customizing the <code>preferences</code>
         */
        PreferencesCustomizer create(Preferences preferences);
    }
    
    /**
     * A way to query a custom <code>PreferencesCustomizer</code>, created by a <code>{@link Factory}</code>, 
     * for the saved value of any option by invoking the {@link CustomCustomizer#getSavedValue(PreferencesCustomizer, String)} method.
     * Based on the returned value the infrastructure will decide whether to enable or disable the Apply button in options window.
     * The custom customizer need to be registered in the XML layer in order to be discoverable.
     *
     * <p>If you need to show some custom customizer for your language you can use the following XML layer registration.
     *
     * <pre style="background-color: rgb(255, 255, 153);">
     * &lt;folder name="OptionsDialog"&gt;
     *   &lt;folder name="Editor"&gt;
     *     &lt;folder name="OnSave"&gt;
     *       &lt;folder name="your"&gt;
     *         &lt;folder name="mimetype"&gt;
     *           &lt;file name="JavaOnSaveCustomCustomizer.instance"&gt;
     *             &lt;attr name="instanceCreate" methodvalue="fully.qualified.name.of.the.PreferencesCustomizer$CustomCustomizerImpl"/&gt;
     *             &lt;attr name="position" intvalue="100"/&gt;
     *           &lt;/file&gt;
     *         &lt;/folder&gt;
     *       &lt;/folder&gt;
     *     &lt;/folder&gt;
     *   &lt;/folder&gt;
     * &lt;/folder&gt;</pre>
     */
    public class CustomCustomizer {
        
        /**
         * Returns the <code>PreferencesCustomizer</code>'s saved value for the specific key.
         *
         * @param customCustomizer the custom <code>PreferencesCustomizer</code>
         * @param key the key for which the saved value is needed
         * @return the saved value for the specific <code>PreferencesCustomizer</code> and key or null
         */
        public String getSavedValue(PreferencesCustomizer customCustomizer, String key) {
            return null;
        }
    }
}
