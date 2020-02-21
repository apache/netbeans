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
