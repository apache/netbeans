/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.netbeans.modules.markdown.ui.preview;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import org.netbeans.modules.markdown.ui.preview.views.MarkdownViewFactory;
import javax.swing.text.ViewFactory;
import javax.swing.text.html.HTMLEditorKit;
import javax.swing.text.html.StyleSheet;
import org.netbeans.modules.editor.settings.storage.api.EditorSettings;
import org.netbeans.modules.markdown.MarkdownDataObject;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.WeakListeners;

/**
 *
 * @author moacirrf
 */
public class MarkdownEditorKit extends HTMLEditorKit {

    private final transient ViewFactory viewFactory;
    private transient StyleSheet markdownStyles;
    private final PropertyChangeListener pcl = this::colorProfileChange;
    
    public MarkdownEditorKit() {
        super();
        this.viewFactory = new MarkdownViewFactory();
        EditorSettings.getDefault().addPropertyChangeListener(WeakListeners.propertyChange(pcl, this));
    }

    @Override
    public StyleSheet getStyleSheet() {
        if (markdownStyles == null) {
            StyleSheet defaultStyles = super.getStyleSheet();
            StyleSheet ss = new StyleSheet();
            ss.addStyleSheet(defaultStyles);
            
            String profile = EditorSettings.getDefault().getCurrentFontColorProfile();
            String configPath = "Editors/" + MarkdownDataObject.MIME_TYPE +"/FontsColors/" + profile + "/Defaults/viewer.css";
            FileObject config = FileUtil.getSystemConfigFile(configPath);
      
            if (config != null) {
                try (Reader rd = new InputStreamReader(config.getInputStream(), StandardCharsets.ISO_8859_1)){
                    ss.loadRules(rd, null);
                } catch (IOException ex) {}
            } else {
                
            }
            markdownStyles = ss;
        }
        return markdownStyles;
    }

    @Override
    public ViewFactory getViewFactory() {
        return viewFactory;
    }
    
    public void colorProfileChange(PropertyChangeEvent evt) {
        if (EditorSettings.PROP_CURRENT_FONT_COLOR_PROFILE.equals(evt.getPropertyName())) {
            markdownStyles = null;
        }
    }
}
