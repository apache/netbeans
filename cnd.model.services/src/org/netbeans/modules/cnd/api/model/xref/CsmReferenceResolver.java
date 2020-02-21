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

package org.netbeans.modules.cnd.api.model.xref;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import javax.swing.JEditorPane;
import javax.swing.text.Document;
import org.netbeans.modules.cnd.api.model.CsmFile;
import org.netbeans.modules.cnd.modelutil.CsmUtilities;
import org.openide.cookies.EditorCookie;
import org.openide.nodes.Node;
import org.openide.util.Lookup;

/**
 * entry point to search references in files
 */
public abstract class CsmReferenceResolver {
    /** A default resolver that combines all results.
     */
    private static final CsmReferenceResolver DEFAULT = new Default();
    
    protected CsmReferenceResolver() {
    }
    
    /** Static method to obtain the resolver.
     * @return the resolver
     */
    public static CsmReferenceResolver getDefault() {
        return DEFAULT;
    }
    
    /**
     * look for reference on specified position in file
     * @param file file where to search
     * @param offset position in file to find reference
     * @return reference for element on position "offset", null if not found
     */
    public abstract CsmReference findReference(CsmFile file, Document doc, int offset);

    /**
     * look for reference on specified position in file
     * @param file file where to search
     * @param line line position in file to find reference
     * @param column column position in file to find reference
     * @return reference for element on position "offset", null if not found
     */
//    public abstract CsmReference findReference(CsmFile file, int line, int column);

    /**
     * default implementation of method based on Node
     */
    public CsmReference findReference(Node activatedNode) {
        assert activatedNode != null : "activatedNode must be not null";
        EditorCookie c = activatedNode.getCookie(EditorCookie.class);
        if (c != null) {
            JEditorPane pane = CsmUtilities.findRecentEditorPaneInEQ(c);
            if (pane != null) {
                //System.err.printf("caret: %d, %d, %d\n",panes[0].getCaretPosition(), panes[0].getSelectionStart(), panes[0].getSelectionEnd());                
                int offset = pane.getSelectionEnd();
                CsmFile file = CsmUtilities.getCsmFile(activatedNode,false);
                if (file != null){
                    return findReference(file, pane.getDocument(), offset);
                }
            }
        }
        return null;
    }   

    public CsmReference findReference(Document doc, int offset) {
        CsmFile file = CsmUtilities.getCsmFile(doc, false, false);
        if (file != null) {
            return findReference(file, doc, offset);
        }
        return null;
    }
    /**
     * fast checks reference scope if possible
     * @param ref
     * @return scope kind if detected or UNKNOWN
     */
    public abstract Scope fastCheckScope(CsmReference ref);

    public abstract boolean isKindOf(CsmReference ref, Set<CsmReferenceKind> kinds);

    public abstract Collection<CsmReference> getReferences(CsmFile file);

    public static enum Scope {
        LOCAL,
        FILE_LOCAL,
        GLOBAL,
        UNKNOWN
    }
    //
    // Implementation of the default resolver
    //
    private static final class Default extends CsmReferenceResolver {
        private final Lookup.Result<CsmReferenceResolver> res;
        Default() {
            res = Lookup.getDefault().lookupResult(CsmReferenceResolver.class);
        }

        @Override
        public CsmReference findReference(CsmFile file, Document doc, int offset) {
            for (CsmReferenceResolver resolver : res.allInstances()) {
                CsmReference out = resolver.findReference(file, doc, offset);
                if (out != null) {
                    return out;
                }
            }
            return null;
        }

        @Override
        public CsmReference findReference(Node activatedNode) {
            for (CsmReferenceResolver resolver : res.allInstances()) {
                CsmReference out = resolver.findReference(activatedNode);
                if (out != null) {
                    return out;
                }
            }
            return null;
        }

        @Override
        public CsmReference findReference(Document doc, int offset) {
            for (CsmReferenceResolver resolver : res.allInstances()) {
                CsmReference out = resolver.findReference(doc, offset);
                if (out != null) {
                    return out;
                }
            }
            return null;
        }

        @Override
        public Scope fastCheckScope(CsmReference ref) {
            for (CsmReferenceResolver resolver : res.allInstances()) {
                Scope scope = resolver.fastCheckScope(ref);
                if (scope != Scope.UNKNOWN) {
                    return scope;
                }
            }
            return Scope.UNKNOWN;
        }

        @Override
        public boolean isKindOf(CsmReference ref, Set<CsmReferenceKind> kinds) {
            for (CsmReferenceResolver resolver : res.allInstances()) {
                if (resolver.isKindOf(ref, kinds)) {
                    return true;
                }
            }
            return false;
        }

        @Override
        public Collection<CsmReference> getReferences(CsmFile file) {
            List<CsmReference> list = new ArrayList<CsmReference>();
            for (CsmReferenceResolver resolver : res.allInstances()) {
                list.addAll(resolver.getReferences(file));
            }
            return list;
        }
    }    
}
