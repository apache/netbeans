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
