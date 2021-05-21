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
package org.netbeans.modules.css.visual;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import javax.swing.JComponent;
import org.netbeans.modules.css.visual.spi.CssStylesPanelProvider;
import org.openide.filesystems.FileObject;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author marekfukala
 */

@NbBundle.Messages({
    "DocumentView.displayName=Document"
})
@ServiceProvider(service=CssStylesPanelProvider.class)
public class DocumentViewPanelProvider implements CssStylesPanelProvider {

    private static String DOCUMENT_PANEL_ID = "static_document";
    private static Collection<String> MIME_TYPES = new HashSet<>(Arrays.asList(new String[]{"text/css", "text/html", "text/xhtml"}));
    private DocumentViewPanel panel;
    
    @Override
    public String getPanelDisplayName() {
        return Bundle.DocumentView_displayName();
    }

    @Override
    public JComponent getContent(Lookup lookup) {
        if(panel == null) {
            panel = new DocumentViewPanel(lookup);
        }
        return panel;
    }
    
    @Override
    public String getPanelID() {
        return DOCUMENT_PANEL_ID;
    }

    @Override
    public Lookup getLookup() {
        return panel.getLookup();
    }

    @Override
    public void activated() {
        panel.activated();
    }

    @Override
    public void deactivated() {
        panel.deactivated();
    }

    @Override
    public boolean providesContentFor(FileObject file) {
        return (file != null) && MIME_TYPES.contains(file.getMIMEType());
    }
    
}
