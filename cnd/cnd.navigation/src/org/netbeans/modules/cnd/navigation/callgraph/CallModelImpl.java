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

package org.netbeans.modules.cnd.navigation.callgraph;

import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.swing.text.Document;
import org.netbeans.modules.cnd.api.model.CsmClass;
import org.netbeans.modules.cnd.api.model.CsmClassifier;
import org.netbeans.modules.cnd.api.model.CsmCompoundClassifier;
import org.netbeans.modules.cnd.api.model.CsmDeclaration;
import org.netbeans.modules.cnd.api.model.CsmEnum;
import org.netbeans.modules.cnd.api.model.CsmEnumerator;
import org.netbeans.modules.cnd.api.model.CsmFile;
import org.netbeans.modules.cnd.api.model.CsmFriend;
import org.netbeans.modules.cnd.api.model.CsmFunction;
import org.netbeans.modules.cnd.api.model.CsmFunctionDefinition;
import org.netbeans.modules.cnd.api.model.CsmMacro;
import org.netbeans.modules.cnd.api.model.CsmMember;
import org.netbeans.modules.cnd.api.model.CsmMethod;
import org.netbeans.modules.cnd.api.model.CsmNamespaceDefinition;
import org.netbeans.modules.cnd.api.model.CsmObject;
import org.netbeans.modules.cnd.api.model.CsmOffsetable;
import org.netbeans.modules.cnd.api.model.CsmOffsetableDeclaration;
import org.netbeans.modules.cnd.api.model.CsmProject;
import org.netbeans.modules.cnd.api.model.CsmScope;
import org.netbeans.modules.cnd.api.model.CsmTypedef;
import org.netbeans.modules.cnd.api.model.CsmUID;
import org.netbeans.modules.cnd.api.model.CsmVariable;
import org.netbeans.modules.cnd.api.model.services.CsmCacheManager;
import org.netbeans.modules.cnd.api.model.services.CsmFileInfoQuery;
import org.netbeans.modules.cnd.api.model.services.CsmFileReferences;
import org.netbeans.modules.cnd.api.model.services.CsmReferenceContext;
import org.netbeans.modules.cnd.api.model.services.CsmVirtualInfoQuery;
import org.netbeans.modules.cnd.api.model.util.CsmKindUtilities;
import org.netbeans.modules.cnd.api.model.util.UIDs;
import org.netbeans.modules.cnd.api.model.xref.CsmReference;
import org.netbeans.modules.cnd.api.model.xref.CsmReferenceKind;
import org.netbeans.modules.cnd.api.model.xref.CsmReferenceRepository;
import org.netbeans.modules.cnd.api.model.xref.CsmReferenceResolver;
import org.netbeans.modules.cnd.callgraph.api.Call;
import org.netbeans.modules.cnd.callgraph.api.CallModel;
import org.netbeans.modules.cnd.callgraph.api.Function;
import org.netbeans.modules.cnd.callgraph.api.ui.CallGraphPreferences;
import org.netbeans.modules.cnd.support.Interrupter;

/**
 *
 */
public class CallModelImpl implements CallModel {
    private final CsmReferenceRepository repository;
    private final CsmFileReferences fileReferences;
    private final CsmProject project;
    private String name;
    private DeclarationUIN uin;
    
    public CallModelImpl(CsmProject project, CsmOffsetableDeclaration root) {
        repository = CsmReferenceRepository.getDefault();
        fileReferences = CsmFileReferences.getDefault();
        this.project = project;
        uin = new CallModelImpl.DeclarationUIN(project, root);
        name = root.getName().toString();
    }

    @Override
    public Function getRoot() {
        CsmOffsetableDeclaration root = uin.getDeclaration();
        if (root != null) {
            CsmCacheManager.enter();
            try {
                return implementationResolver(root);
            } finally {
                CsmCacheManager.leave();
            }
        }
        return null;
    }

    @Override
    public boolean isRootVisible() {
        return true;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public void setRoot(Function newRoot) {
        if (newRoot instanceof FunctionImpl) {
            FunctionImpl impl = (FunctionImpl)newRoot;
            CsmFunction f = impl.getDefinition();
            if (f != null) {
                uin = new DeclarationUIN(project, f);
                name = f.getName().toString();
            }
        } else if (newRoot instanceof VariableImpl) {
            VariableImpl impl = (VariableImpl)newRoot;
            CsmOffsetableDeclaration v = impl.getVariable();
            if (v != null) {
                uin = new DeclarationUIN(project, v);
                name = v.getName().toString();
            }
        }
    }

    @Override
    public void update() {
        //do nothing 
    }

    /**
     * declaration()<-f()
     * @param declaration
     * @return
     */
    @Override
    public List<Call> getCallers(Function declaration) {
        CsmCacheManager.enter();
        try {
            Collection<CsmOffsetableDeclaration> declarations = new ArrayList<CsmOffsetableDeclaration>();
            List<Call> res = new ArrayList<Call>();
            
            if (declaration instanceof FunctionImpl) {
                FunctionImpl functionImpl = (FunctionImpl) declaration;
                CsmFunction owner = functionImpl.getDeclaration();
                declarations.add(owner);
                if (CallGraphPreferences.isShowOverriding()) {
                    if (CsmKindUtilities.isMethodDeclaration(owner)) {
                        Collection<CsmMethod> overrides = CsmVirtualInfoQuery.getDefault().getAllBaseDeclarations((CsmMethod) owner);
                        declarations.addAll(overrides);
                    }
                }
            } else if (declaration instanceof VariableImpl) {
                declarations.add(((VariableImpl) declaration).getVariable());
            } else {
                return res;
            }
            
            EnumSet<CsmReferenceKind> kinds;
            if (declaration instanceof FunctionImpl) {
                kinds = EnumSet.of(CsmReferenceKind.DIRECT_USAGE, CsmReferenceKind.UNKNOWN);
            } else {
                kinds = EnumSet.of(CsmReferenceKind.DIRECT_USAGE, CsmReferenceKind.UNKNOWN, CsmReferenceKind.DECLARATION);
            }
            
            HashMap<CsmFunction,ArrayList<CsmReference>> set = new HashMap<CsmFunction,ArrayList<CsmReference>>();
            HashMap<CsmMacro,CsmReference> macros = new HashMap<CsmMacro,CsmReference>();
            for(CsmOffsetableDeclaration decl : declarations) {
                if (decl.getContainingFile().isValid()) {
                    for(CsmReference r : repository.getReferences(decl, project, CsmReferenceKind.ANY_REFERENCE_IN_ACTIVE_CODE, Interrupter.DUMMY)){
                        if (r == null) {
                            continue;
                        }
                        if (CsmReferenceResolver.getDefault().isKindOf(r,kinds)) {
                            CsmFunction o = getFunctionDeclaration(getEnclosingFunction(r));
                            if (o != null) {
                                if (!set.containsKey(o)) {
                                    set.put(o, new ArrayList<CsmReference>());
                                }
                                set.get(o).add(r);
                            } else {
                                CsmMacro enclosingMacro = getEnclosingMacro(r);
                                if (enclosingMacro != null && !macros.containsKey(enclosingMacro)) {
                                    macros.put(enclosingMacro, r);
                                }
                            }
                        }
                    }
                }
            }
            for(Map.Entry<CsmMacro,CsmReference> entry : macros.entrySet()) {
                for(CsmReference r : repository.getReferences(entry.getKey(), project, CsmReferenceKind.ANY_REFERENCE_IN_ACTIVE_CODE, Interrupter.DUMMY)){
                    if (r == null) {
                        continue;
                    }
                    if (CsmReferenceResolver.getDefault().isKindOf(r,kinds)) {
                        CsmFunction o = getFunctionDeclaration(getEnclosingFunction(r));
                        if (o != null) {
                            if (!set.containsKey(o)) {
                                set.put(o, new ArrayList<CsmReference>());
                            }
                            set.get(o).add(r);
                        }
                    }
                }
            }
            
            CsmOffsetableDeclaration owner;
            if (declaration instanceof FunctionImpl) {
                FunctionImpl functionImpl = (FunctionImpl) declaration;
                owner = functionImpl.getDeclaration();
            } else if (declaration instanceof VariableImpl) {
                owner = ((VariableImpl) declaration).getVariable();
            } else {
                owner = null;
            }
            
            final CsmOffsetableDeclaration ownerDeclaration = owner;
            if (ownerDeclaration != null) {
                for(Map.Entry<CsmFunction,ArrayList<CsmReference>> entry : set.entrySet()){
                    res.add(new CallImpl(entry.getKey(), entry.getValue(), ownerDeclaration, true));
                }
            }
            return res;
        } finally {
            CsmCacheManager.leave();
        }
    }
    
    /**
     * declaration()->f()
     * @param declaration
     * @return
     */
    @Override
    public List<Call> getCallees(Function definition) {
        CsmCacheManager.enter();
        try {
            if (definition instanceof VariableImpl) {
                return new ArrayList<Call>();
            } else if (definition instanceof FunctionImpl) {
                FunctionImpl definitionImpl = (FunctionImpl) definition;
                CsmFunction owner = definitionImpl.getDefinition();
                Collection<CsmFunction> functions = new ArrayList<CsmFunction>();
                functions.add(owner);
                if (CallGraphPreferences.isShowOverriding()) {
                    if (CsmKindUtilities.isMethodDeclaration(owner)) {
                        Collection<CsmMethod> overrides = CsmVirtualInfoQuery.getDefault().getOverriddenMethods((CsmMethod) owner, false);
                        functions.addAll(overrides);
                    }
                }
                List<Call> res = new ArrayList<Call>();
                final HashMap<CsmOffsetableDeclaration,ArrayList<CsmReference>> set = new HashMap<CsmOffsetableDeclaration,ArrayList<CsmReference>>();
                for(CsmFunction function : functions) {
                    if (CsmKindUtilities.isFunctionDefinition(function) && function.getContainingFile().isValid()) {
                        final List<CsmOffsetable> list = CsmFileInfoQuery.getDefault().getUnusedCodeBlocks((function).getContainingFile(), Interrupter.DUMMY);
                        fileReferences.accept((CsmScope)function, null, new CsmFileReferences.Visitor() {
                            @Override
                            public void visit(CsmReferenceContext context) {
                                CsmReference r = context.getReference();
                                if (r == null) {
                                    return;
                                }
                                for(CsmOffsetable offset:list){
                                    if (offset.getStartOffset()<=r.getStartOffset() &&
                                        offset.getEndOffset()  >=r.getEndOffset()){
                                        return;
                                    }
                                }
                                try {
                                    CsmObject o = r.getReferencedObject();
                                    if (CsmKindUtilities.isFunction(o) && 
                                            !CsmReferenceResolver.getDefault().isKindOf(r, CsmReferenceKind.FUNCTION_DECLARATION_KINDS)) {
                                        o = getFunctionDeclaration((CsmFunction)o);
                                        if (!set.containsKey((CsmOffsetableDeclaration)o)) {
                                            set.put((CsmOffsetableDeclaration)o, new ArrayList<CsmReference>());
                                        }
                                        set.get((CsmOffsetableDeclaration)o).add(r);
                                    } else if (CsmKindUtilities.isVariable(o)) {
                                        if (CsmKindUtilities.isFunctionPointerType(((CsmVariable) o).getType())) {
                                            if (!set.containsKey((CsmOffsetableDeclaration)o)) {
                                                set.put((CsmOffsetableDeclaration)o, new ArrayList<CsmReference>());
                                            }
                                            set.get((CsmOffsetableDeclaration)o).add(r);
                                        }
                                    }
                                } catch (AssertionError e){
                                    e.printStackTrace(System.err);
                                } catch (Exception e) {
                                    e.printStackTrace(System.err);
                                }
                            }

                            @Override
                            public boolean cancelled() {
                                return false;
                            }
                        }, CsmReferenceKind.ANY_REFERENCE_IN_ACTIVE_CODE);
                    }
                }
                final CsmFunction functionDeclaration = getFunctionDeclaration(owner);
                if (functionDeclaration != null) {
                    for(Map.Entry<CsmOffsetableDeclaration,ArrayList<CsmReference>> entry : set.entrySet()){
                        res.add(new CallImpl(functionDeclaration, entry.getValue(), entry.getKey(), false));
                    }
                }
                return res;
            } else {
                return new ArrayList<Call>();
            }
        } finally {
            CsmCacheManager.leave();
        }
    }
    
    private CsmFunction getFunctionDeclaration(CsmFunction definition){
        if (definition != null) {
            if (CsmKindUtilities.isFunctionDefinition(definition)) {
                return ((CsmFunctionDefinition)definition).getDeclaration();
            }
        }
        return definition;
    }

    private CsmMacro getEnclosingMacro(CsmReference ref){
        CsmObject o = ref.getClosestTopLevelObject();
        if (CsmKindUtilities.isMacro(o)) {
            return (CsmMacro) o;
        }
        return null;
    }

    private CsmFunction getEnclosingFunction(CsmReference ref){
        CsmObject o = ref.getClosestTopLevelObject();
        if (CsmKindUtilities.isFunction(o)) {
            return (CsmFunction) o;
        }
        return null;
    }
    
    private static Function implementationResolver(CsmOffsetableDeclaration entity) {
        if (CsmKindUtilities.isFunction(entity)) {
            return new FunctionImpl((CsmFunction) entity);
        } else if (CsmKindUtilities.isVariable(entity)) {
            return new VariableImpl((CsmVariable) entity);
        }  else if (CsmKindUtilities.isEnumerator(entity)) {
            return new VariableImpl((CsmEnumerator) entity);
        } else {
            return null;
        }
    }

    private static class DeclarationUIN {
        private final CsmProject project;
        private final CharSequence declarationUin;
        private final CsmUID<CsmFile> fileUid;
        
        private DeclarationUIN(CsmProject project, CsmOffsetableDeclaration declaration) {
            this.project = project;
            fileUid = UIDs.get(declaration.getContainingFile());
            declarationUin = declaration.getUniqueName();
        }
        
        private CsmOffsetableDeclaration getDeclaration() {
            if (!project.isValid()) {
                return null;
            }
            for(CsmOffsetableDeclaration decl : project.findDeclarations(declarationUin)) {
                CsmFile containingFile = decl.getContainingFile();
                if (containingFile != null) {
                    if (fileUid.equals(UIDs.get(decl.getContainingFile()))) {
                        return decl;
                    }
                }
            }
            CsmFile file = fileUid.getObject();
            if (!file.isValid()) {
                return null;
            }
            for (CsmDeclaration d : file.getDeclarations()) {
                CsmOffsetableDeclaration root = findDeclaration(d);
                if (root != null){
                    return root;
                }
            }
            return null;
        }
        
        private CsmOffsetableDeclaration findDeclaration(CsmDeclaration element) {
            if (CsmKindUtilities.isTypedef(element) || CsmKindUtilities.isTypeAlias(element)) {
                CsmTypedef def = (CsmTypedef) element;
                if (def.isTypeUnnamed()) {
                    CsmClassifier cls = def.getType().getClassifier();
                    if (cls != null && cls.getName().length() == 0 &&
                            (cls instanceof CsmCompoundClassifier)) {
                        return findDeclaration((CsmCompoundClassifier) cls);
                    }
                } 
                return null;
            } else if (CsmKindUtilities.isClassifier(element)) {
                String name = ((CsmClassifier) element).getName().toString();
                if (name.length() == 0 && (element instanceof CsmCompoundClassifier)) {
                    Collection<CsmTypedef> list = ((CsmCompoundClassifier) element).getEnclosingTypedefs();
                    if (list.size() > 0) {
                        return null;
                    }
                }
                if (CsmKindUtilities.isClass(element)) {
                    CsmClass cls = (CsmClass) element;
                    for (CsmMember member : cls.getMembers()) {
                        CsmOffsetableDeclaration res = findDeclaration(member);
                        if (res != null) {
                            return res;
                        }
                    }
                    for (CsmFriend friend : cls.getFriends()) {
                        CsmOffsetableDeclaration res = findDeclaration(friend);
                        if (res != null) {
                            return res;
                        }
                    }
                } else if (CsmKindUtilities.isEnum(element)) {
                    CsmEnum cls = (CsmEnum) element;
                    for (CsmEnumerator member : cls.getEnumerators()) {
                        CsmOffsetableDeclaration res = findDeclaration(member);
                        if (res != null) {
                            return res;
                        }
                    }
                }
                return null;
            } else if (CsmKindUtilities.isNamespaceDefinition(element)) {
                for (CsmDeclaration d : ((CsmNamespaceDefinition) element).getDeclarations()) {
                    CsmOffsetableDeclaration res = findDeclaration(d);
                    if (res != null) {
                        return res;
                    }
                }
            } else if (CsmKindUtilities.isFunction(element)) {
                if (element.getUniqueName().equals(declarationUin)) {
                    return (CsmOffsetableDeclaration)element;
                }
            } else if (CsmKindUtilities.isVariableDeclaration(element)) {
                CsmVariable var = (CsmVariable) element;
                if (var.getUniqueName().equals(declarationUin)) {
                    return (CsmOffsetableDeclaration) var;
                }
            } else if (CsmKindUtilities.isEnumerator(element)) {
                CsmEnumerator var = (CsmEnumerator) element;
                if (var.getUniqueName().equals(declarationUin)) {
                    return (CsmOffsetableDeclaration) var;
                }
            }
            return null;
        }
    }
}
