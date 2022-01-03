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

package org.netbeans.modules.cnd.completion.impl.xref;

import java.util.Collection;
import java.util.Collections;
import java.util.Set;
import javax.swing.JEditorPane;
import javax.swing.text.Document;
import javax.swing.text.StyledDocument;
import org.netbeans.editor.BaseDocument;
import org.netbeans.modules.cnd.api.model.CsmFile;
import org.netbeans.modules.cnd.api.model.CsmOffsetable;
import org.netbeans.modules.cnd.api.model.services.CsmFileInfoQuery;
import org.netbeans.modules.cnd.api.model.xref.CsmReference;
import org.netbeans.modules.cnd.api.model.xref.CsmReferenceKind;
import org.netbeans.modules.cnd.api.model.xref.CsmReferenceRepository;
import org.netbeans.modules.cnd.api.model.xref.CsmReferenceResolver;
import org.netbeans.modules.cnd.modelutil.CsmUtilities;
import org.netbeans.modules.cnd.support.Interrupter;
import org.openide.cookies.EditorCookie;
import org.openide.nodes.Node;

/**
 * implementation of references resolver
 *
 */
@org.openide.util.lookup.ServiceProvider(service=org.netbeans.modules.cnd.api.model.xref.CsmReferenceResolver.class, position = 1000)
public class ReferenceResolverImpl extends CsmReferenceResolver {
    
    public ReferenceResolverImpl() {
    }    

    @Override
    public boolean isKindOf(CsmReference ref, Set<CsmReferenceKind> kinds) {
        if (kinds.equals(CsmReferenceKind.ALL) || kinds.contains(ref.getKind())) {
            return true;
        }
        CsmFile file = ref.getContainingFile();
        int offset = ref.getStartOffset();
        if (kinds.contains(CsmReferenceKind.IN_DEAD_BLOCK)) {
            if (isIn(CsmFileInfoQuery.getDefault().getUnusedCodeBlocks(file, Interrupter.DUMMY), offset)) {
                return true;
            }
        }
        if (kinds.contains(CsmReferenceKind.IN_PREPROCESSOR_DIRECTIVE)) {
            if (isIn(file.getIncludes(), offset)) {
                return true;
            }
            if (isIn(file.getMacros(), offset)) {
                return true;
            }
        }
        return false;
    }
    
    private static boolean isIn(Collection<? extends CsmOffsetable> collection, int offset) {
        for (CsmOffsetable element : collection) {
            if (element.getStartOffset() <= offset &&
                element.getEndOffset() >= offset){
                return true;
            }
        }
        return false;
    }
    
    @Override
    public CsmReference findReference(CsmFile file, Document doc, int offset) {
        assert file != null;
        if (doc == null) {
            doc = CsmReferenceRepository.getDocument(file);
        }
        if (!(doc instanceof BaseDocument)) {
            return null;
        }
        CsmReference ref = ReferencesSupport.createReferenceImpl(file, (BaseDocument)doc, offset);
        return ref;
    }
    
    @Override
    public CsmReference findReference(Node activatedNode) {
        assert activatedNode != null : "activatedNode must be not null";
        EditorCookie cookie = activatedNode.getCookie(EditorCookie.class);
        if (cookie != null) {
            JEditorPane pane = CsmUtilities.findRecentEditorPaneInEQ(cookie);
            if (pane != null) {
                //System.err.printf("caret: %d, %d, %d\n",panes[0].getCaretPosition(), panes[0].getSelectionStart(), panes[0].getSelectionEnd());
                int offset = pane.getSelectionEnd();
                StyledDocument doc = CsmUtilities.openDocument(cookie);
                return findReferenceInDoc(doc, offset);
            }
        }
        return null;
    }
    
    @Override
    public CsmReference findReference(Document doc, int offset) {
        return findReferenceInDoc(doc, offset);
    }

    private CsmReference findReferenceInDoc(Document doc, int offset) {
        if (doc instanceof BaseDocument) {
            CsmFile file = CsmUtilities.getCsmFile(doc, false, false);
            if (file != null) {
                return ReferencesSupport.createReferenceImpl(file, (BaseDocument) doc, offset);
            }
        }
        return null;
    }

    @Override
    public Scope fastCheckScope(CsmReference ref) {
        return ReferencesSupport.fastCheckScope(ref);
    }

    @Override
    public Collection<CsmReference> getReferences(CsmFile file) {
        return Collections.<CsmReference>emptyList();
    }
}
