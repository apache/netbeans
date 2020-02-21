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
