/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
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
