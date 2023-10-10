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

package org.netbeans.modules.web.core.jsploader;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.IOException;
import org.openide.nodes.Node;
import org.openide.util.Lookup;
import org.openide.util.Task;
import org.openide.text.Line;
import org.openide.util.NbBundle;
import org.netbeans.api.java.loaders.JavaDataSupport;
import org.openide.cookies.EditorCookie;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectExistsException;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.loaders.MultiDataObject;
import org.openide.loaders.MultiFileLoader;

/** Dataobject representing a servlet generated from a JSP page
*
* @author Petr Jiricka
*/

public final class JspServletDataObject extends MultiDataObject {

    public static final String EA_ORIGIN_JSP_PAGE = "NetBeansAttrOriginJspPage"; // NOI18N

    public JspServletDataObject(FileObject pf, MultiFileLoader loader) throws DataObjectExistsException {
        super(pf, loader);
    }
    
    @Override
    public Node createNodeDelegate() {
        return JavaDataSupport.createJavaNode(getPrimaryFile());
    }

    @Override
    public Lookup getLookup() {
        return getCookieSet().getLookup();
    }

    /** Get the name of the data object.
    * Uses the name of the source JSP
    * @return the name
    */
    @Override
    public String getName () {
        DataObject jsp = getSourceJspPage();
        if (jsp == null)
            return super.getName();
        int markIndex = getPrimaryFile().getName().lastIndexOf(JspServletDataLoader.JSP_MARK);
        String fileIndex = (markIndex == -1) ? "" : getPrimaryFile().getName().substring(
                               markIndex + JspServletDataLoader.JSP_MARK.length());
        if (fileIndex.startsWith("_"))  // NOI18N
            fileIndex = fileIndex.substring(1);
        if ("".equals(fileIndex)) {
            return NbBundle.getMessage(JspServletDataObject.class, "LBL_ServletDisplayNameNoNumber", jsp.getPrimaryFile().getName());
        }
        else {
            return NbBundle.getMessage(JspServletDataObject.class, "LBL_ServletDisplayName", new Object[] {fileIndex, jsp.getPrimaryFile().getName()});
        }
    }

    /** Sets the source JSP page for this servlet */
    public void setSourceJspPage(DataObject jspPage) throws IOException {
        setSourceJspPage(getPrimaryFile(), jspPage);
        firePropertyChange(PROP_COOKIE, null, null);
    }
    
    public static void setSourceJspPage(FileObject generatedServlet, DataObject jspPage) throws IOException {
        generatedServlet.setAttribute(EA_ORIGIN_JSP_PAGE, jspPage.getPrimaryFile());
    }

    /** Returns the source JSP page for this servlet */
    public DataObject getSourceJspPage() {
    	Object obj = getPrimaryFile().getAttribute(EA_ORIGIN_JSP_PAGE);
    	if (obj instanceof DataObject) return (DataObject)obj;
    	if (obj instanceof FileObject) {
    		if (((FileObject)obj).isValid()) {
                    try {
                        return DataObject.find((FileObject)obj);
                    } catch (DataObjectNotFoundException e) {
                        //nothing to do
    		    }
    		}
        }
        return null;
    }
    
    private static class ServletEditorCookie implements EditorCookie.Observable, PropertyChangeListener {
        
        private EditorCookie original;
        private JspServletDataObject servlet;
        private EditorCookie currentEditor;
        private PropertyChangeSupport pcs;
        
        public ServletEditorCookie(EditorCookie original, JspServletDataObject servlet) {
            this.original = original;
            this.servlet = servlet;
            pcs = new PropertyChangeSupport(this);
        }

        private EditorCookie currentEditorCookie() {
            EditorCookie newCurrent = computeCurrentEditorCookie();
            if (currentEditor != newCurrent) {
                // re-register a property change listener to the new editor
                if (currentEditor instanceof EditorCookie.Observable) {
                    ((EditorCookie.Observable)currentEditor).removePropertyChangeListener(this);
                }
                if (newCurrent instanceof EditorCookie.Observable) {
                    ((EditorCookie.Observable)newCurrent).addPropertyChangeListener(this);
                }
                // remember the new editor
                currentEditor = newCurrent;
            }
            return currentEditor;
        }
        
        private EditorCookie computeCurrentEditorCookie() {
            DataObject jsp = servlet.getSourceJspPage();
            if (jsp instanceof JspDataObject) {
                if (((JspDataObject)jsp).getServletDataObject() == servlet) {
                    EditorCookie newCookie = ((JspDataObject) jsp).getServletEditor();
                    if (newCookie != null)
                        return newCookie;
                }
            }
            return original;
        }

        // implementation of EditorCookie
        public Line.Set getLineSet() {
            return currentEditorCookie().getLineSet();
        }

        public void open() {
            currentEditorCookie().open();
        }

        public boolean close() {
            return currentEditorCookie().close();
        }

        public Task prepareDocument() {
            return currentEditorCookie().prepareDocument();
        }

        public javax.swing.text.StyledDocument openDocument() throws java.io.IOException {
            return currentEditorCookie().openDocument();
        }

        public javax.swing.text.StyledDocument getDocument() {
            return currentEditorCookie().getDocument();
        }

        public void saveDocument() throws java.io.IOException {
            currentEditorCookie().saveDocument();
        }

        public boolean isModified() {
            return currentEditorCookie().isModified();
        }

        public javax.swing.JEditorPane[] getOpenedPanes() {
            return currentEditorCookie().getOpenedPanes();
        }

        // implementation of EditorSupport.Observable
        
        public void addPropertyChangeListener(PropertyChangeListener l) {
            pcs.addPropertyChangeListener(l);
        }
        
        public void removePropertyChangeListener(PropertyChangeListener l) {
            pcs.removePropertyChangeListener(l);
        }
        
        // implementation of PropertyChangeListener
        
        public void propertyChange(PropertyChangeEvent evt) {
            pcs.firePropertyChange(evt);
        }
        
    }

}

