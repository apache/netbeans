/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
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
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
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
