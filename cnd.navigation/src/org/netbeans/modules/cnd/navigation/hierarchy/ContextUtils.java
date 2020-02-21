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

package org.netbeans.modules.cnd.navigation.hierarchy;

import java.util.Iterator;
import javax.swing.JEditorPane;
import org.netbeans.modules.cnd.api.model.CsmClass;
import org.netbeans.modules.cnd.api.model.CsmClassifier;
import org.netbeans.modules.cnd.api.model.CsmDeclaration;
import org.netbeans.modules.cnd.api.model.CsmFile;
import org.netbeans.modules.cnd.api.model.CsmInclude;
import org.netbeans.modules.cnd.api.model.CsmNamespaceDefinition;
import org.netbeans.modules.cnd.api.model.CsmObject;
import org.netbeans.modules.cnd.api.model.CsmOffsetable;
import org.netbeans.modules.cnd.api.model.CsmOffsetableDeclaration;
import org.netbeans.modules.cnd.api.model.CsmScope;
import org.netbeans.modules.cnd.api.model.CsmScopeElement;
import org.netbeans.modules.cnd.api.model.CsmType;
import org.netbeans.modules.cnd.api.model.CsmVariable;
import org.netbeans.modules.cnd.api.model.services.CsmSelect;
import org.netbeans.modules.cnd.api.model.services.CsmSelect.CsmFilter;
import org.netbeans.modules.cnd.api.model.util.CsmKindUtilities;
import org.netbeans.modules.cnd.api.model.xref.CsmReference;
import org.netbeans.modules.cnd.api.model.xref.CsmReferenceResolver;
import org.netbeans.modules.cnd.modelutil.CsmUtilities;
import org.netbeans.modules.cnd.utils.CndUtils;
import org.openide.cookies.EditorCookie;
import org.openide.nodes.Node;

public class ContextUtils {
    public static final boolean USE_REFERENCE_RESOLVER = CndUtils.getBoolean("hierarchy.use.reference", true); // NOI18N
    
    private ContextUtils() {
    }

    public static CsmFile findFile(Node[] activatedNodes) {
        if (activatedNodes != null && activatedNodes.length > 0) {
            if (ContextUtils.USE_REFERENCE_RESOLVER) {
                CsmReference ref = ContextUtils.findReference(activatedNodes[0]);
                if (ref != null){
                    if (ref.getClosestTopLevelObject() != null) {
                        if (CsmKindUtilities.isInclude(ref.getClosestTopLevelObject())) {
                            CsmInclude incl = (CsmInclude) ref.getClosestTopLevelObject();
                            CsmFile file = incl.getIncludeFile();
                            if (file != null) {
                                return file;
                            }
                        }
                    }
                }
            }
            return ContextUtils.findFile(activatedNodes[0]);
        }
        return null;
    }

    public static CsmFile findFile(Node activatedNode) {
        EditorCookie c = activatedNode.getLookup().lookup(EditorCookie.class);
        if (c != null && CsmUtilities.findRecentEditorPaneInEQ(c) != null) {
            return CsmUtilities.getCsmFile(activatedNode,false);
        } else {
            return null;
        }
    }

    public static CsmClass getContextClass(Node[] activatedNodes){
        CsmObject decl;
        if (ContextUtils.USE_REFERENCE_RESOLVER) {
            CsmReference ref = ContextUtils.findReference(activatedNodes[0]);            
            if (isSupportedReference(ref)) {
                decl = ref.getReferencedObject();
                if (CsmKindUtilities.isClass(decl)){
                    return (CsmClass)decl;
                } else if (CsmKindUtilities.isVariable(decl)){
                    CsmVariable v = (CsmVariable)decl;
                    CsmType type = v.getType();
                    // could be null type for parameter with vararg "..." type
                    CsmClassifier cls = type == null ? null : type.getClassifier();
                    if (CsmKindUtilities.isClass(cls)){
                        return (CsmClass)cls;
                    }
                }
            }
        }
        decl = ContextUtils.findDeclaration(activatedNodes[0]);
        if (CsmKindUtilities.isClass(decl)){
            return (CsmClass)decl;
        }
        return null;
    }

    public static boolean isSupportedReference(CsmReference ref) {
        if (ref != null) {
            if (ref.getClosestTopLevelObject() != null) {
                return !CsmKindUtilities.isMacro(ref.getClosestTopLevelObject()) &&
                       !CsmKindUtilities.isInclude(ref.getClosestTopLevelObject());
            }
        }
        return false;
    }
    
    public static CsmReference findReference(Node activatedNode) {
        return CsmReferenceResolver.getDefault().findReference(activatedNode);
    }
    
    public static CsmDeclaration findDeclaration(Node activatedNode) {
        EditorCookie c = activatedNode.getLookup().lookup(EditorCookie.class);
        if (c != null) {
            JEditorPane pane = CsmUtilities.findRecentEditorPaneInEQ(c);
            if (pane != null ) {
                int offset = pane.getCaret().getDot();
                CsmFile file = CsmUtilities.getCsmFile(activatedNode,false);
                if (file != null){
                    return findInnerFileDeclaration(file, offset);
                }
            }
        }
        return null;
    }
    
    public static CsmDeclaration findInnerFileDeclaration(CsmFile file, int offset) {
        CsmFilter offsetFilter = CsmSelect.getFilterBuilder().createOffsetFilter(offset);
        Iterator<? extends CsmObject> fileElements = getInnerObjectsIterator(offsetFilter, file);
        CsmDeclaration innermostDecl = (CsmDeclaration)(fileElements.hasNext() ? fileElements.next() : null);
        if (innermostDecl != null && CsmKindUtilities.isScope(innermostDecl)) {
            CsmDeclaration inner = findInnerDeclaration(offsetFilter, (CsmScope)innermostDecl);
            innermostDecl = inner != null ? inner : innermostDecl;
        }
        return innermostDecl;
    }

    private static Iterator<? extends CsmObject> getInnerObjectsIterator(CsmFilter offsetFilter, CsmScope scope) {
        Iterator<? extends CsmObject> out;
        if (CsmKindUtilities.isFile(scope)) {
            out = CsmSelect.getDeclarations((CsmFile)scope, offsetFilter);
        } else if (CsmKindUtilities.isNamespaceDefinition(scope)) {
            out = CsmSelect.getDeclarations(((CsmNamespaceDefinition)scope), offsetFilter);
        } else if (CsmKindUtilities.isClass(scope)) {
            out = CsmSelect.getClassMembers(((CsmClass)scope), offsetFilter);
        } else {
            out = scope.getScopeElements().iterator();
        }
        return out;
    }

    private static CsmDeclaration findInnerDeclaration(CsmFilter offsetFilter, CsmScope scope) {
        Iterator<? extends CsmObject> it = getInnerObjectsIterator(offsetFilter, scope);
        if (it != null && it.hasNext()) {
            CsmObject decl = it.next();
            if (CsmKindUtilities.isScope(decl)) {
                CsmObject innerDecl = findInnerDeclaration(offsetFilter, (CsmScope)decl);
                if (CsmKindUtilities.isClass(innerDecl)){
                    return (CsmClass)innerDecl;
                } else if (CsmKindUtilities.isClass(decl)){
                    return (CsmClass)decl;
                }
            }
        }
        return null;
    }    

    private static boolean isInObject(CsmObject obj, int offset) {
        if (!CsmKindUtilities.isOffsetable(obj)) {
            return false;
        }
        CsmOffsetable offs = (CsmOffsetable)obj;
        if ((offs.getStartOffset() <= offset) &&
                (offset <= offs.getEndOffset())) {
            return true;
        }
        return false;
    }

    public static CsmScope findScope(Node activatedNode) {
        EditorCookie c = activatedNode.getLookup().lookup(EditorCookie.class);
        if (c != null) {
            JEditorPane pane = CsmUtilities.findRecentEditorPaneInEQ(c);
            if (pane != null ) {
                int offset = pane.getCaret().getDot();
                CsmFile file = CsmUtilities.getCsmFile(activatedNode,false);
                if (file != null){
                    return findInnerFileScope(file, offset);
                }
            }
        }
        return null;
    }

    public static CsmScope findInnerFileScope(CsmFile file, int offset) {
        CsmScope innerScope = null;
        for (Iterator<CsmOffsetableDeclaration> it = file.getDeclarations().iterator(); it.hasNext();) {
            CsmOffsetableDeclaration decl = it.next();
            if (CsmKindUtilities.isScope(decl) && isInObject(decl, offset)) {
                innerScope = findInnerScope((CsmScope)decl, offset);
                innerScope = innerScope != null ? innerScope : (CsmScope)decl;
                break;
            }
        }
        return innerScope;
    }

    private static CsmScope findInnerScope(CsmScope outScope, int offset) {
        for (CsmScopeElement item : outScope.getScopeElements()) {
            if(CsmKindUtilities.isScope(item) && isInObject(item, offset)) {
                CsmScope inScope = findInnerScope((CsmScope) item, offset);
                if(inScope != null) {
                    return inScope;
                } else {
                    if(CsmKindUtilities.isClass(item) ||
                            CsmKindUtilities.isNamespace(item) ||
                            CsmKindUtilities.isFunction(item) ||
                            CsmKindUtilities.isEnum(item) ||
                            CsmKindUtilities.isUnion(item) ||
                            CsmKindUtilities.isFile(item)) {
                        return (CsmScope) item;
                    }
                }
            }
        }
        if (CsmKindUtilities.isNamespaceDefinition(outScope)) {
            for (CsmDeclaration item : ((CsmNamespaceDefinition) outScope).getDeclarations()) {
                if (CsmKindUtilities.isScope(item) && isInObject(item, offset)) {
                    CsmScope inScope = findInnerScope((CsmScope) item, offset);
                    if (inScope != null) {
                        return inScope;
                    } else {
                        if (CsmKindUtilities.isClass(item) ||
                                CsmKindUtilities.isNamespace(item) ||
                                CsmKindUtilities.isFunction(item) ||
                                CsmKindUtilities.isEnum(item) ||
                                CsmKindUtilities.isUnion(item) ||
                                CsmKindUtilities.isFile(item)) {
                            return (CsmScope) item;
                        }
                    }
                }
            }
        }

        return null;
    }
}
