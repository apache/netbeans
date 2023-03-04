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
package org.netbeans.modules.refactoring.spi.impl;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.WeakHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.StyledDocument;
import org.netbeans.api.diff.DiffController;
import org.netbeans.api.diff.Difference;
import org.netbeans.api.diff.StreamSource;
import org.netbeans.modules.refactoring.api.impl.SPIAccessor;
import org.netbeans.modules.refactoring.spi.SimpleRefactoringElementImplementation;
import org.netbeans.modules.refactoring.spi.ui.UI;
import org.netbeans.spi.editor.guards.GuardedEditorSupport;
import org.netbeans.spi.editor.guards.GuardedSectionsFactory;
import org.netbeans.spi.editor.guards.GuardedSectionsProvider;
import org.openide.cookies.EditorCookie;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.text.CloneableEditorSupport;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.lookup.Lookups;

/**
 *
 * @author Jan Becicka
 */
public class PreviewManager {
    
    private class Pair {
        DiffController dc;
        NewDiffSource source;
        Pair(DiffController dc, NewDiffSource source) {
            this.dc = dc;
            this.source = source;
        }
    }

    private static PreviewManager manager;
    private WeakHashMap<RefactoringPanel, HashMap<FileObject, Pair>> map = new WeakHashMap<>();
    private PreviewManager() {
    }
    
    public static PreviewManager getDefault() {
        if (manager==null)
            manager = new PreviewManager();
        return manager;
    }
    
    public void clean(RefactoringPanel panel) {
        map.remove(panel);
    }
    
    private Pair getPair(SimpleRefactoringElementImplementation element) {
        RefactoringPanel current = RefactoringPanelContainer.getRefactoringComponent().getCurrentPanel();
        HashMap<FileObject, Pair> m = map.get(current);
        if (m!=null) {
            Pair pair = m.get(element.getParentFile());
            if (pair!=null)
                return pair;
        }
        NewDiffSource nds;
        try {
            nds = new NewDiffSource(element);
            DiffController diffView = DiffController.create(
                    new OldDiffSource(element),
                    nds);
            if (m==null) {
                m = new HashMap<FileObject, Pair>();
                map.put(current, m);
            }
            Pair p = new Pair(diffView, nds);
            m.put(element.getParentFile(), p);
            return p;
        } catch (IOException ioe) {
            throw new RuntimeException(ioe);
        }
    }
    
    public void refresh(SimpleRefactoringElementImplementation element) {
        try {
            String newText = SPIAccessor.DEFAULT.getNewFileContent(element);
            if (newText==null) {
                UI.setComponentForRefactoringPreview(null);
                return;
            }
            Pair p = getPair(element);
            p.source.setNewText(newText);
            UI.setComponentForRefactoringPreview(p.dc.getJComponent());
            if(element.getPosition() != null) 
                p.dc.setLocation(DiffController.DiffPane.Base, DiffController.LocationType.LineNumber, element.getPosition().getBegin().getLine());
        } catch (IOException ioe) {
            throw new RuntimeException(ioe);
        }
    }
    
    private class OldDiffSource extends StreamSource {
        private FileObject file;
        
        OldDiffSource(SimpleRefactoringElementImplementation r) {
            this.file = r.getParentFile();
        }
        @Override
        public String getName() {
            if (file.isFolder()) {
                return NbBundle.getMessage(PreviewManager.class,"LBL_FileDoesNotExist");
            }
            return file.getName();
        }

        @Override
        public String getTitle() {
            if (file.isFolder()) {
                return NbBundle.getMessage(PreviewManager.class,"LBL_FileDoesNotExist");
            }
            return file.getNameExt();
        }

        @Override
        public String getMIMEType() {
            return file.getMIMEType();
        }

        @Override
        public Reader createReader() throws IOException {
            return null;
        }

        @Override
        public Writer createWriter(Difference[] conflicts) throws IOException {
            return null;
        }
        
        @Override
        public Lookup getLookup() {
            return Lookups.singleton(file);
        }
        
    }
    
    private class NewDiffSource extends StreamSource {
        private SimpleRefactoringElementImplementation element;
        
        NewDiffSource(SimpleRefactoringElementImplementation refactElemImpl) {
            this.element = refactElemImpl;
        }

        @Override
        public String getName() {
            return NbBundle.getMessage(PreviewManager.class,"LBL_ProposedRefactoring");
        }
        
        @Override
        public String getTitle() {
            if (element.getParentFile().isFolder()) {
                return NbBundle.getMessage(PreviewManager.class,"LBL_NewFile");
            }
            return  NbBundle.getMessage(PreviewManager.class,"LBL_Refactored",element.getParentFile().getNameExt());
        }
        
        @Override
        public String getMIMEType() {
            if (element.getParentFile().isFolder()) {
                //this is hack, all folders are text/x-java
                return "text/x-java"; //NOI18N
            }
            return element.getParentFile().getMIMEType();
        }
        
        @Override
        public Reader createReader() throws IOException {
            return null;
        }
        
        @Override
        public Writer createWriter(Difference[] conflicts) throws IOException {
            return null;
        }
        
        // @GuardedBy(this)
        private Document internal;
        
        // accessed asynchronously from DiffController through Lookup, must protected
        // `internal' reference.
        private synchronized Document getDocument() {
            if (internal==null) {
                internal = CloneableEditorSupport.getEditorKit(getMIMEType()).createDefaultDocument();
            }
            return internal;
        }
        
        @Override
        public Lookup getLookup() {
            return Lookups.singleton(getDocument());
        }
        
        private boolean isOriginalDocumentLoaded() {
            try {
                FileObject fo = element.getParentFile();
                DataObject dObj = DataObject.find(fo);
                EditorCookie ec = dObj != null ? dObj.getCookie(org.openide.cookies.EditorCookie.class) : null;
                if (ec != null) {
                    StyledDocument doc = ec.getDocument();
                    return doc!=null;
                }
            } catch (DataObjectNotFoundException ex) {
                //ignore;
            }
            return false;
            
       }
        
        public void setNewText(String newText) {
            try {
                
                if (!isOriginalDocumentLoaded() && GuardedSectionsFactory.find(getMIMEType())!=null) {
                    GuardedSectionsFactory guardedSectionsFactory = GuardedSectionsFactory.find(getMIMEType());
                    GuardedSectionsProvider guardedProvider = guardedSectionsFactory.create(new GuardedEditorSupport() {
                        @Override
                        public StyledDocument getDocument() {
                            return (StyledDocument) NewDiffSource.this.getDocument();
                        }
                    });
                    Reader reader = guardedProvider.createGuardedReader(new ByteArrayInputStream(newText.getBytes()), Charset.defaultCharset());
                    char buf[] = new char[newText.length()];
                    try {
                        reader.read(buf);
                    } catch (IOException ex) {
                        Exceptions.printStackTrace(ex);
                    }
                    Document doc = getDocument();
                    doc.remove(0, doc.getLength());
                    doc.insertString(0, new String(buf), null);
                } else {
                    Document doc = getDocument();
                    doc.remove(0, doc.getLength());
                    doc.insertString(0, newText, null);
                }
            } catch (BadLocationException ex) {
                Logger.getLogger(getClass().getName()).log(Level.SEVERE, ex.getMessage(), ex);
            }
        }
    }        
}
