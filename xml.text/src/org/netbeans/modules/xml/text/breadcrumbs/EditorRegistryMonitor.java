/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2015 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2015 Sun Microsystems, Inc.
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
final public class EditorRegistryMonitor implements PropertyChangeListener, Runnable {
    
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
