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

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.prefs.Preferences;
import javax.swing.JComponent;
import javax.swing.JPanel;
import org.netbeans.api.editor.mimelookup.MimePath;
import org.netbeans.modules.options.indentation.CustomizerSelector;
import org.netbeans.modules.options.indentation.IndentationPanel;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.HelpCtx;

/**
 *
 * @author vita
 */
final class CustomizerFactories {

    private static final Logger LOG = Logger.getLogger(CustomizerFactories.class.getName());

    public static PreferencesCustomizer.Factory createDefaultTabsAndIndentsCustomizerFactory(final FileObject file) {
        return new PreferencesCustomizer.Factory() {
            public PreferencesCustomizer create(Preferences preferences) {
                String customizerMimeType = null;
                PreviewProvider preview = null;

                String folderPath = file.getParent().getPath();
                if (folderPath.startsWith(CustomizerSelector.FORMATTING_CUSTOMIZERS_FOLDER)) {
                    String mimeType = folderPath.substring(CustomizerSelector.FORMATTING_CUSTOMIZERS_FOLDER.length());
                    if (MimePath.validate(mimeType)) {
                        customizerMimeType = mimeType;
                    }
                }

                String path = (String) file.getAttribute("previewTextFile"); //NOI18N
                if (path != null) {
                    FileObject previewTextFile = FileUtil.getConfigFile(path);
                    if (previewTextFile != null) {
                        try {
                            preview = new IndentationPanel.TextPreview(preferences, customizerMimeType, previewTextFile);
                        } catch (IOException ioe) {
                            LOG.log(Level.WARNING, null, ioe);
                        }
                    }
                }

                if (preview == null && customizerMimeType != null) {
                    FileObject previewTextFile = FileUtil.getConfigFile("OptionsDialog/PreviewExamples/" + customizerMimeType); //NOI18N
                    if (previewTextFile != null && previewTextFile.isData()) {
                        try {
                            preview = new IndentationPanel.TextPreview(preferences, customizerMimeType, previewTextFile);
                        } catch (IOException ioe) {
                            LOG.log(Level.WARNING, null, ioe);
                        }
                    }
                }

                if (preview == null) {
                    preview = new IndentationPanel.NoPreview();
                }

                return new SimpleTabsAndIndentsCustomizer(preview);
            }
        };
    }

    private static final class SimpleTabsAndIndentsCustomizer implements PreferencesCustomizer, PreviewProvider {

        public SimpleTabsAndIndentsCustomizer(PreviewProvider preview) {
            this.preview = preview;
        }

        // -------------------------------------------------------------------
        // PreferencesCustomizer implementation
        // -------------------------------------------------------------------

        public String getId() {
            return TABS_AND_INDENTS_ID;
        }

        public String getDisplayName() {
            // this should never be used
            return TABS_AND_INDENTS_ID;
        }

        public HelpCtx getHelpCtx() {
            return null;
        }

        public JComponent getComponent() {
            return new JPanel(); // just an empty panel
        }

        // -------------------------------------------------------------------
        // PreviewProvider implementation
        // -------------------------------------------------------------------

        public JComponent getPreviewComponent() {
            return preview.getPreviewComponent();
        }

        public void refreshPreview() {
            preview.refreshPreview();
        }

        // -------------------------------------------------------------------
        // private implementation
        // -------------------------------------------------------------------

        private final PreviewProvider preview;
        
    } // End of SimpleTabsAndIndentsCustomizer class
}
