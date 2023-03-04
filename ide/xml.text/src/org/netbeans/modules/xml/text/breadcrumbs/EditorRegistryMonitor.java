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
package org.netbeans.modules.xml.text.breadcrumbs;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import javax.swing.text.JTextComponent;
import org.netbeans.api.editor.EditorRegistry;
import org.netbeans.api.editor.mimelookup.MimePath;
import org.netbeans.lib.editor.util.swing.DocumentUtilities;
import org.netbeans.modules.editor.structure.api.DocumentModel;
import org.netbeans.modules.editor.structure.api.DocumentModelException;
import org.openide.modules.OnStart;
import org.openide.util.Exceptions;

/**
 * Monitors Editor.Registry for active components. Maintains one {@link BreadcrumbProvider}
 * for each XML-like activated editor. Releases the Provider on editor deactivation.
 * Should work for all text/?+xml MIMEtypes as well.
 * 
 * @author sdedic
 */
@OnStart
public final class EditorRegistryMonitor implements PropertyChangeListener, Runnable {
    
    public void run() {
        EditorRegistry.addPropertyChangeListener(this);
    }
    
    /**
     * Current breadcrumb reporter, for the active editor.
     * The Provider attaches (hard) caretListener to the editor, so it will
     * be GCed eventually with the editor.
     */
    private Reference<BreadcrumbProvider>  currentProvider;
    
    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        switch (evt.getPropertyName()) {
            case EditorRegistry.FOCUS_GAINED_PROPERTY: 
                componentActivated((JTextComponent)evt.getNewValue());
                break;
            case EditorRegistry.FOCUSED_DOCUMENT_PROPERTY:
                updateBreadcrumbs();
        }
    }
    
    private void updateBreadcrumbs() {
        BreadcrumbProvider p = currentProvider();
        if (p == null) {
            return;
        }
        p.update();
    }
    
    private DocumentModel findTextModel(JTextComponent comp) {
        String mime = DocumentUtilities.getMimeType(comp);
        MimePath mp = MimePath.parse(mime);
        if (!"text/xml".equals(mp.getInheritedType())) { // NOI18N
            return null;
        }
        
        try {
            DocumentModel mod = DocumentModel.getDocumentModel(comp.getDocument());
            return mod;
        } catch (DocumentModelException ex) {
            Exceptions.printStackTrace(ex);
            return null;
        }
    }
    
    private BreadcrumbProvider currentProvider() {
        Reference<BreadcrumbProvider> c = currentProvider;
        if (c == null) {
            return null;
        } else {
            return c.get();
        }
    }
    
    private void componentActivated(JTextComponent comp) {
        // check if the current component has some XML model on it
        BreadcrumbProvider c = currentProvider();
        
        if (c != null) {
            c.release();
        }
        currentProvider = null;
        if (findTextModel(comp) == null) {
            return;
        }
        BreadcrumbProvider p = new BreadcrumbProvider(comp);
        currentProvider = new WeakReference(p);
        p.update();
    }
    
}
