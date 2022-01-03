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

package org.netbeans.modules.cnd.refactoring.api;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;
import org.netbeans.api.editor.EditorRegistry;
import org.netbeans.modules.cnd.api.model.CsmClass;
import org.netbeans.modules.cnd.api.model.CsmFile;
import org.netbeans.modules.cnd.api.model.CsmFriend;
import org.netbeans.modules.cnd.api.model.CsmFunction;
import org.netbeans.modules.cnd.api.model.CsmMember;
import org.netbeans.modules.cnd.api.model.CsmNamespaceDefinition;
import org.netbeans.modules.cnd.api.model.CsmObject;
import org.netbeans.modules.cnd.api.model.CsmOffsetable;
import org.netbeans.modules.cnd.api.model.CsmScope;
import org.netbeans.modules.cnd.api.model.services.CsmCacheManager;
import org.netbeans.modules.cnd.api.model.services.CsmSelect;
import org.netbeans.modules.cnd.api.model.services.CsmSelect.CsmFilter;
import org.netbeans.modules.cnd.api.model.util.CsmKindUtilities;
import org.netbeans.modules.cnd.api.model.xref.CsmReference;
import org.netbeans.modules.cnd.api.model.xref.CsmReferenceResolver;
import org.netbeans.modules.cnd.modelutil.CsmUtilities;
import org.openide.cookies.EditorCookie;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import org.openide.util.Lookup;

/**
 *
 */
public final class CsmContext {
    private final JTextComponent component;
    private final CsmFile file;
    private final Document doc;
    private final FileObject fo;

    private final int startOffset;
    private final int endOffset;
    private final int caretOffset;
    private final CsmReference csmReference;
    private List<CsmObject> path = null;
    private CsmClass enclosingClass = null;
    private CsmNamespaceDefinition enclosingNS = null;
    private CsmFunction enclosingFun = null;
    private CsmOffsetable objectUnderOffset = null;

    private CsmContext(JTextComponent component, CsmFile file, CsmReference ref, FileObject fo, Document doc, int startOffset, int endOffset, int caretOffset) {
        this.component = component;
        this.file = file;
        this.fo = fo;
        this.doc = doc;
        this.startOffset = startOffset;
        this.endOffset = endOffset;
        this.caretOffset = caretOffset;
        this.csmReference = ref;
    }

    public static CsmContext create(final Document doc, int start, int end, int offset) {
        CsmFile csmFile = CsmUtilities.getCsmFile(doc, false, false);
        if (csmFile != null) {
            final CsmReference ref = CsmReferenceResolver.getDefault().findReference(doc, offset);
            return new CsmContext(EditorRegistry.lastFocusedComponent(), csmFile, ref, CsmUtilities.getFileObject(doc), doc, start, end, offset);
        }
        return null;
    }

    public static CsmContext create(final CsmFile csmFile, int offset) {
        final DataObject dob = CsmUtilities.getDataObject(csmFile);
        final Document doc = getDocument(dob);
        if (doc != null) {
            final CsmReference ref = CsmReferenceResolver.getDefault().findReference(doc, offset);
            return new CsmContext(null, csmFile, ref, CsmUtilities.getFileObject(doc), doc, offset, offset, offset);
        }
        return null;
    }

    private static Document getDocument(DataObject dataObject) {
        return CsmUtilities.openDocument(dataObject);
    }

    public static CsmContext create(final Lookup context) {
        JTextComponent component = context.lookup(JTextComponent.class);
        if (component == null) {
            EditorCookie ec = context.lookup(EditorCookie.class);
            component = (ec == null) ? null : CsmUtilities.findRecentEditorPaneInEQ(ec);
        }
        if (component != null) {
            CsmFile csmFile = CsmUtilities.getCsmFile(component, false, false);
            if (csmFile != null) {
                final int start = component.getSelectionStart();
                final int end = component.getSelectionEnd();
                final int caret = component.getCaretPosition();
                final Document compDoc = component.getDocument();
                final FileObject compFO = CsmUtilities.getFileObject(compDoc);
                final CsmReference ref = CsmReferenceResolver.getDefault().findReference(compDoc, caret);
                return new CsmContext(component, csmFile, ref, compFO, compDoc, start, end, caret);
            }
        }
        return null;
    }

    public int getCaretOffset() {
        return caretOffset;
    }
    
    public FileObject getFileObject() {
        return fo;
    }

    public JTextComponent getComponent() {
        return component;
    }

    /**
     *
     * @return reference if any, could be null if no caret offset or caret is not on reference object
     */
    public CsmReference getCsmReferenceUnderOffset() {
        return csmReference;
    }

    public Document getDocument() {
        return doc;
    }

    public CsmFile getFile() {
        return file;
    }

    public List<CsmObject> getPath() {
        initPath();
        return path;
    }

    public int getStartOffset() {
        return startOffset;
    }

    public int getEndOffset() {
        return endOffset;
    }

    @Override
    public String toString() {
        return "context: [" + file + ":" + startOffset + ", " + endOffset + "]"; // NOI18N
    }

    public CsmClass getEnclosingClass() {
        initPath();
        return enclosingClass;
    }

    public CsmFunction getEnclosingFunction() {
        initPath();
        return enclosingFun;
    }

    public CsmNamespaceDefinition getEnclosingNamespace() {
        initPath();
        return enclosingNS;
    }

    public CsmOffsetable getObjectUnderOffset() {
        initPath();
        return objectUnderOffset;
    }

    private Iterator<? extends CsmObject> getInnerObjectsIterator(CsmFilter offsetFilter, CsmScope scope) {
        Iterator<? extends CsmObject> out;
        if (CsmKindUtilities.isFile(scope)) {
            out = CsmSelect.getDeclarations((CsmFile)scope, offsetFilter);
        } else if (CsmKindUtilities.isNamespaceDefinition(scope)) {
            out = CsmSelect.getDeclarations(((CsmNamespaceDefinition)scope), offsetFilter);
        } else if (CsmKindUtilities.isClass(scope)) {
            Iterator<CsmMember> out1 = CsmSelect.getClassMembers(((CsmClass)scope), offsetFilter);
            Iterator<CsmFriend> out2 = CsmSelect.getClassFrirends(((CsmClass)scope), offsetFilter);
            out = new CompoundIterator(out1, out2);
        } else if (CsmKindUtilities.isCompoundStatement(scope)) {
            // we stop on compound statement
            out = Collections.<CsmObject>emptyList().iterator();
        } else {
            out = scope.getScopeElements().iterator();
        }
        return out;
    }
    
    private synchronized void initPath() {
        if (path != null) {
            return;
        }
        CsmCacheManager.enter();
        try {
            initPathImpl();
        } finally {
        CsmCacheManager.leave();
        }
    }
    private void initPathImpl() {
        path = new ArrayList<>(5);
        path.add(file);
        CsmFilter offsetFilter = CsmSelect.getFilterBuilder().createOffsetFilter(startOffset);
        Iterator<? extends CsmObject> fileElements = getInnerObjectsIterator(offsetFilter, file);
        CsmObject innerDecl = fileElements.hasNext() ? fileElements.next() : null;
        if (innerDecl != null) {
            path.add(innerDecl);
            rememberObject(innerDecl);
            if (CsmKindUtilities.isScope(innerDecl)) {
                CsmScope curScope = (CsmScope)innerDecl;
                boolean cont;
                do {
                    cont = false;
                    final Iterator<? extends CsmObject> innerObjects = getInnerObjectsIterator(offsetFilter, curScope);
                    while (innerObjects.hasNext()) {
                        CsmObject csmScopeElement = innerObjects.next();
                        if (CsmKindUtilities.isOffsetable(csmScopeElement)) {
                            CsmOffsetable elem = (CsmOffsetable) csmScopeElement;
                            // stop if element starts after offset
                            if (this.startOffset < elem.getStartOffset()) {
                                break;
                            } else if (this.startOffset < elem.getEndOffset()) {
                                // offset is in element
                                cont = true;
                                path.add(elem);
                                rememberObject(elem);
                                if (CsmKindUtilities.isScope(elem)) {
                                    // deep diving
                                    curScope = (CsmScope)elem;
                                    break;
                                } else {
                                    objectUnderOffset = elem;
                                    cont = false;
                                }
                            }
                        }
                    }
                } while (cont);
            }
        }
    }

    private void rememberObject(CsmObject obj) {
        if (CsmKindUtilities.isNamespaceDefinition(obj)) {
            enclosingNS = (CsmNamespaceDefinition) obj;
        } else if (CsmKindUtilities.isClass(obj)) {
            enclosingClass = (CsmClass)obj;
        } else if (CsmKindUtilities.isFunction(obj)) {
            enclosingFun = (CsmFunction) obj;
        }
    }
    
    private static final class CompoundIterator<T> implements  Iterator<T> {
        private final Iterator<T> it1;
        private final Iterator<T> it2;
        CompoundIterator(Iterator<T> it1,  Iterator<T> it2) {
            this.it1 = it1;
            this.it2 = it2;
        }

        @Override
        public boolean hasNext() {
            return it1.hasNext() || it2.hasNext();
        }

        @Override
        public T next() {
            if (it1.hasNext()) {
                return it1.next();
            } else if (it2.hasNext()) {
                return it2.next();
            }
            throw new IllegalStateException();
        }

        @Override
        public void remove() {
            throw new UnsupportedOperationException();
        }
    }
}
