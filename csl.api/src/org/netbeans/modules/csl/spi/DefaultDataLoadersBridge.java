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
 * Portions Copyrighted 2010 Sun Microsystems, Inc.
 */

package org.netbeans.modules.csl.spi;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JEditorPane;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;
import javax.swing.text.StyledDocument;
import org.netbeans.modules.csl.api.DataLoadersBridge;
import org.openide.ErrorManager;
import org.openide.cookies.EditorCookie;
import org.openide.cookies.InstanceCookie;
import org.openide.cookies.LineCookie;
import org.openide.filesystems.FileChangeListener;
import org.openide.filesystems.FileEvent;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.nodes.Node;
import org.openide.text.Line;
import org.openide.util.Exceptions;
import org.openide.util.WeakListeners;

/**
 *
 * @author Emilian Bold
 */
public class DefaultDataLoadersBridge extends DataLoadersBridge {

    private static final Logger LOG = Logger.getLogger(DataLoadersBridge.class.getName());

    private DataObject getDataObject(Document doc) {
        Object o = doc.getProperty(Document.StreamDescriptionProperty);
        if (o instanceof DataObject) {
            return (DataObject) o;
        } else if (o != null) {
            LOG.warning("Unable to return DataObject for Document " + doc + ". StreamDescriptionProperty points to non-DataLoader instace: " + o); //NOI18N
        }
        return null;
    }

    @Override
    public FileObject getFileObject(Document doc) {
        Object o = doc.getProperty(Document.StreamDescriptionProperty);
        if (o instanceof DataObject) {
            return ((DataObject) o).getPrimaryFile();
        } else if (o instanceof FileObject) {
            return (FileObject) o;
        } else if (o != null) {
            LOG.warning("Unable to return FileObject for Document " + doc + ". StreamDescriptionProperty points to non-DataLoader, non-FileObject instace: " + o); //NOI18N
        }
        return null;
    }

    @Override
    public StyledDocument getDocument(FileObject file) {
        try {
            DataObject d = DataObject.find(file);
            EditorCookie ec = (EditorCookie) d.getCookie(EditorCookie.class);

            if (ec == null) {
                return null;
            }
            return ec.getDocument();
        } catch (IOException e) {
            LOG.log(Level.INFO, "SemanticHighlighter: Cannot find DataObject for file: " + FileUtil.getFileDisplayName(file), e); //NOI18N
            return null;
        }
    }

    @Override
    public JEditorPane[] getOpenedPanes(FileObject fo) {
        DataObject dobj;
        try {
            dobj = DataObject.find(fo);
        } catch (DataObjectNotFoundException ex) {
            return new JEditorPane[0];
        }

        EditorCookie editorCookie = dobj.getCookie(EditorCookie.class);

        if (editorCookie == null) {
            return new JEditorPane[0];
        }

        return editorCookie.getOpenedPanes();
    }

    @Override
    public Object createInstance(FileObject file) {
        assert file.getExt().equals("instance"); // NOI18N
        // Construct the service lazily using the instance cookie on the provided data object
        try {
            DataObject dobj = DataObject.find(file);
            InstanceCookie ic = dobj.getCookie(InstanceCookie.class);
            return ic.instanceCreate();
        } catch (ClassNotFoundException e) {
            ErrorManager.getDefault().notify(e);
        } catch (DataObjectNotFoundException e) {
            ErrorManager.getDefault().notify(e);
        } catch (IOException e) {
            ErrorManager.getDefault().notify(e);
        }
        return null;
    }

    @Override
    public FileObject getPrimaryFile(FileObject fileObject) {
        try {
            DataObject dobj = DataObject.find(fileObject);

            if (dobj != null) {
                return dobj.getPrimaryFile();
            }
            return null;
        } catch (DataObjectNotFoundException ex) {
            Exceptions.printStackTrace(ex);
            return null;
        }
    }

    @Override
    public String getLine(Document doc, int lineNumber) {
        DataObject dObj = (DataObject) doc.getProperty(doc.StreamDescriptionProperty);
        LineCookie lc = dObj.getCookie(LineCookie.class);
        Line line = lc.getLineSet().getCurrent(lineNumber);

        return line.getText();
    }

    @Override
    public Object getCookie(FileObject fo, Class aClass) throws IOException {
        DataObject od = DataObject.find(fo);
        return od.getCookie(aClass);
    }

    @Override
    public Object getSafeCookie(FileObject fo, Class aClass) {
        try {
            return getCookie(fo, aClass);
        } catch (IOException ioe) {
            return null;
        }
    }

    @Override
    public EditorCookie isModified(FileObject file) {
        DataObject.Registry regs = DataObject.getRegistry();
        Set<DataObject> modified = (Set<DataObject>) regs.getModifiedSet();
        for (DataObject dobj : modified) {
            if (file.equals(dobj.getPrimaryFile())) {
                EditorCookie ec = (EditorCookie) dobj.getCookie(EditorCookie.class);
                return ec;
            }
        }
        return null;
    }

    @Override
    public PropertyChangeListener getDataObjectListener(FileObject fo, FileChangeListener fcl) throws IOException {
        return new DataObjectListener(fo, fcl);
    }

    /**
     * Adds listener on a given FileObject and notify if the dataobject is being deleted or
     * disposed.
     */
    private final static class DataObjectListener implements PropertyChangeListener {

        private DataObject dobj;
        private final FileObject fobj;
        private PropertyChangeListener wlistener;
        private final FileChangeListener flisten;

        /**
         * @param fo The file object to listen upon at DataObject level
         * @param fcl The FileChangeListener to be called when the DataObject has been invalidated
         * 
         * @throws org.openide.loaders.DataObjectNotFoundException
         */
        public DataObjectListener(FileObject fo, FileChangeListener fcl) throws DataObjectNotFoundException {
            this.fobj = fo;
            this.flisten = fcl;
            this.dobj = DataObject.find(fo);
            wlistener = WeakListeners.propertyChange(this, dobj);
            this.dobj.addPropertyChangeListener(wlistener);
        }

        public void propertyChange(PropertyChangeEvent pce) {
            DataObject invalidDO = (DataObject) pce.getSource();
            if (invalidDO != dobj) {
                return;
            }
            if (DataObject.PROP_VALID.equals(pce.getPropertyName())) {
                handleInvalidDataObject(invalidDO);
            } else if (pce.getPropertyName() == null && !dobj.isValid()) {
                handleInvalidDataObject(invalidDO);
            }
        }

        private void handleInvalidDataObject(DataObject invalidDO) {
            invalidDO.removePropertyChangeListener(wlistener);
            if (fobj.isValid()) {
                // file object still exists try to find new data object
                try {
                    dobj = DataObject.find(fobj);
                    dobj.addPropertyChangeListener(wlistener);
                    flisten.fileChanged(new FileEvent(fobj));
                } catch (IOException ex) {
                    // should not occur
                    LOG.log(Level.SEVERE, ex.getMessage(), ex);
                }
            }
        }
    }

    @Override
    public Node getNodeDelegate(JTextComponent target) {
        DataObject dobj = getDataObject(target.getDocument());
        return dobj!=null ? dobj.getNodeDelegate() : null;
    }
}
