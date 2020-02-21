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

package org.netbeans.modules.cnd.classview;

import java.util.Collection;
import java.util.HashMap;
import org.netbeans.modules.cnd.api.model.CsmClass;
import org.netbeans.modules.cnd.api.model.CsmClassifier;
import org.netbeans.modules.cnd.api.model.CsmCompoundClassifier;
import org.netbeans.modules.cnd.api.model.CsmDeclaration;
import org.netbeans.modules.cnd.api.model.CsmEnum;
import org.netbeans.modules.cnd.api.model.CsmFile;
import org.netbeans.modules.cnd.api.model.CsmFunction;
import org.netbeans.modules.cnd.api.model.CsmFunctionDefinition;
import org.netbeans.modules.cnd.api.model.CsmListeners;
import org.netbeans.modules.cnd.api.model.CsmMember;
import org.netbeans.modules.cnd.api.model.CsmNamespace;
import org.netbeans.modules.cnd.api.model.CsmObject;
import org.netbeans.modules.cnd.api.model.CsmOffsetableDeclaration;
import org.netbeans.modules.cnd.api.model.CsmProgressListener;
import org.netbeans.modules.cnd.api.model.CsmProject;
import org.netbeans.modules.cnd.api.model.CsmTypedef;
import org.netbeans.modules.cnd.api.model.CsmVariable;
import org.netbeans.modules.cnd.api.model.services.CsmCacheManager;
import org.netbeans.modules.cnd.api.model.util.CsmKindUtilities;
import org.netbeans.modules.cnd.classview.model.CVUtil;
import org.netbeans.modules.cnd.classview.model.ClassNode;
import org.netbeans.modules.cnd.classview.model.EnumNode;
import org.netbeans.modules.cnd.classview.model.GlobalFuncNode;
import org.netbeans.modules.cnd.classview.model.GlobalVarNode;
import org.netbeans.modules.cnd.classview.model.ProjectLibsNode;
import org.netbeans.modules.cnd.classview.model.NamespaceNode;
import org.netbeans.modules.cnd.classview.model.ObjectNode;
import org.netbeans.modules.cnd.classview.model.TypedefNode;
import org.openide.nodes.Node;

/**
 *
 */
public class NamespaceKeyArray extends HostKeyArray implements UpdatebleHost, CsmProgressListener {
    private boolean isRootNamespase;
    
    public NamespaceKeyArray(ChildrenUpdater childrenUpdater, CsmProject project){
        super(childrenUpdater, project, PersistentKey.createGlobalNamespaceKey(project));
        if (!project.isArtificial()) {
            CsmListeners.getDefault().addProgressListener(this);
            isRootNamespase = true;
        }
    }

    public NamespaceKeyArray(ChildrenUpdater childrenUpdater, CsmNamespace namespace){
        super(childrenUpdater, namespace.getProject(),PersistentKey.createKey(namespace));
        CsmProject project = namespace.getProject();
        if (namespace.equals(project.getGlobalNamespace())){
            if (!project.isArtificial()) {
                CsmListeners.getDefault().addProgressListener(this);
                isRootNamespase = true;
            }
        }
    }
    
    @Override
    protected boolean isGlobalNamespace() {
        return isRootNamespase;
    }
    
    @Override
    protected boolean isNamespace() {
        return true;
    }
    
    @Override
    protected void addNotify() {
        super.addNotify();
    }
    
    @Override
    protected java.util.Map<PersistentKey,SortedName> getMembers() {
        java.util.Map<PersistentKey,SortedName> res = new HashMap<PersistentKey,SortedName>();
        try {
            CsmCacheManager.enter();
            CsmNamespace namespace = getNamespace();
            if (namespace != null) {
                for (CsmNamespace ns : namespace.getNestedNamespaces()) {
                    PersistentKey key = PersistentKey.createKey(ns);
                    if (key != null) {
                        res.put(key, getSortedName(ns));
                    }
                }
                Collection<CsmOffsetableDeclaration> decl = namespace.getDeclarations();
                if (decl != null) {
                    for (CsmDeclaration d : decl) {
                        if (d != null && (d.getKind() == CsmDeclaration.Kind.FUNCTION_DEFINITION ||
                                d.getKind() == CsmDeclaration.Kind.FUNCTION_FRIEND_DEFINITION)) {
                            CsmFunctionDefinition def = (CsmFunctionDefinition) d;
                            CsmFunction func = def.getDeclaration();
                            if (func != null) {
                                d = func;
                            }
                        }
                        if (CsmKindUtilities.isOffsetable(d)) {
                            if (canCreateNode((CsmOffsetableDeclaration) d)) {
                                PersistentKey key = PersistentKey.createKey(d);
                                if (key != null) {
                                    res.put(key, getSortedName((CsmOffsetableDeclaration) d));
                                }
                            }
                        }
                    }
                }
            }
            if (isRootNamespase && !getProject().isStable(null)) {
                PersistentKey key = PersistentKey.createKey(getProject());
                if (key != null) {
                    res.put(key, new SortedName(0, "", 0)); // NOI18N
                }
            }
            if (isRootNamespase) {
                PersistentKey key = PersistentKey.createLibsKey(getProject());
                res.put(key, new SortedName(0, "", 0)); // NOI18N
            }
        } catch (AssertionError ex){
            ex.printStackTrace();
        } catch (Exception ex){
            ex.printStackTrace();
        } finally {
            CsmCacheManager.leave();
        }
        return res;
    }
    
    @Override
    protected boolean canCreateNode(CsmOffsetableDeclaration d) {
        if (d.getName().length() > 0) {
            if( CsmKindUtilities.isClass(d) ) {
                CsmClass cls = (CsmClass) d;
                if( !CsmKindUtilities.isClassMember(cls) ) {
                    return true;
                }
            } else if( d.getKind() == CsmDeclaration.Kind.VARIABLE ) {
                return true;
            } else if( d.getKind() == CsmDeclaration.Kind.FUNCTION ||
                    d.getKind() == CsmDeclaration.Kind.FUNCTION_FRIEND) {
                return true;
            } else if( d.getKind() == CsmDeclaration.Kind.FUNCTION_DEFINITION ||
                    d.getKind() == CsmDeclaration.Kind.FUNCTION_FRIEND_DEFINITION) {
                CsmFunctionDefinition definition = (CsmFunctionDefinition) d;
                CsmFunction func = definition.getDeclaration();
                if( func == null || func == definition ) {
                    return true;
                }
            } else if( d.getKind() == CsmDeclaration.Kind.ENUM ) {
                CsmEnum en = (CsmEnum) d;
                if( ! CsmKindUtilities.isClassMember(en) || ((CsmMember) en).getContainingClass() == null ) {
                    return true;
                }
            } else if( d.getKind() == CsmDeclaration.Kind.TYPEDEF ) {
                return true;
            }
        }
        return false;
    }
    
    private ObjectNode createNode(CsmOffsetableDeclaration d) {
        ChildrenUpdater updater = getUpdater();
        if (updater != null) {
            // TODO: shouldn't be empty, if everything was resolved!
            if (d.getName().length() > 0) {
                if( CsmKindUtilities.isClass(d) ) {
                    CsmClass cls = (CsmClass) d;
                    // inner classes are return in namespace declarations list
                    // (since they act just like top-level classes),
                    // but shouldn't be included in class view at the top level
		    if( ! CsmKindUtilities.isClassMember(cls) ) {
                    //if( cls.getContainingClass() == null ) {
                        return new ClassNode( (CsmClass) d,
                                new ClassifierKeyArray(updater, (CsmClass) d));
                    }
                } else if( d.getKind() == CsmDeclaration.Kind.VARIABLE ) {
                    return new GlobalVarNode((CsmVariable) d);
                }
//            else if( d.getKind() == CsmDeclaration.Kind.VARIABLE_DEFINITION ) {
//                CsmVariableDefinition definition = (CsmVariableDefinition) d;
//                CsmVariable var = definition.getDeclaration();
//                if( var == null || var == definition ) {
//                    return new GlobalVarNode(definition);
//                }
//            }
                else if( d.getKind() == CsmDeclaration.Kind.FUNCTION ||
                        d.getKind() == CsmDeclaration.Kind.FUNCTION_FRIEND) {
                    return new GlobalFuncNode((CsmFunction) d);
                } else if( d.getKind() == CsmDeclaration.Kind.FUNCTION_DEFINITION ||
                        d.getKind() == CsmDeclaration.Kind.FUNCTION_FRIEND_DEFINITION) {
                    CsmFunctionDefinition definition = (CsmFunctionDefinition) d;
                    CsmFunction func = definition.getDeclaration();
                    if( func == null || func == definition ) {
                        return new GlobalFuncNode(definition);
                    }
                } else if( d.getKind() == CsmDeclaration.Kind.ENUM ) {
                    CsmEnum en = (CsmEnum) d;
                    if( ! CsmKindUtilities.isClassMember(en) || ((CsmMember) en).getContainingClass() == null ) {
                        return new EnumNode(en,
                                new ClassifierKeyArray(updater, en));
                    }
                } else if( d.getKind() == CsmDeclaration.Kind.TYPEDEF ) {
                    CsmTypedef def = (CsmTypedef) d;
                    if (def.isTypeUnnamed()) {
                        CsmClassifier cls = def.getType().getClassifier();
                        if (cls != null && cls.getName().length()==0 &&
                                (cls instanceof CsmCompoundClassifier)) {
                            return new TypedefNode(def,new ClassifierKeyArray(updater, def, (CsmCompoundClassifier) cls));
                        }
                    }
                    return new TypedefNode(def);
                }
            }
        }
        return null;
    }
    
    private CsmNamespace getNamespace(){
        return (CsmNamespace)getHostId().getObject();
    }
    
    @Override
    protected CsmOffsetableDeclaration findDeclaration(PersistentKey declId){
        return (CsmOffsetableDeclaration) declId.getObject();
    }
    
//    private CsmNamespace findNamespace(PersistentKey nsId){
//        return (CsmNamespace) nsId.getObject();
//    }
    
    @Override
    protected Node createNode(PersistentKey key) {
        Node node = null;
        ChildrenUpdater updater = getUpdater();
        if (updater != null) {
            try {
                Object o = key.getObject();
                if (CsmKindUtilities.isNamespace((CsmObject) o)) {
                    CsmNamespace ns = (CsmNamespace) o;
                    node = new NamespaceNode(ns, new NamespaceKeyArray(updater, ns));
                } else if (o instanceof CsmProject) {
                    if (key.isProjectLibs()) {
                        node = new ProjectLibsNode((CsmProject) o, updater);
                    } else {
                        node = CVUtil.createLoadingNode();
                    }
                } else {
                    CsmOffsetableDeclaration decl = (CsmOffsetableDeclaration) o;
                    if (decl != null && canCreateNode(decl)) {
                        node = createNode(decl);
                    }
                }
            } catch (AssertionError ex){
                ex.printStackTrace();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
        return node;
    }
    
    @Override
    public void projectParsingStarted(CsmProject project) {
    }
    
    @Override
    public void projectFilesCounted(CsmProject project, int filesCount) {
    }
    
    @Override
    public void projectParsingFinished(CsmProject project) {
        onPprojectParsingFinished(project);
    }
    
    @Override
    public void projectLoaded(CsmProject project) {
	onPprojectParsingFinished(project);
    }

    @Override
    public void projectParsingCancelled(CsmProject project) {
    }

    @Override
    public void fileInvalidated(CsmFile file) {
    }

    @Override
    public void fileAddedToParse(CsmFile file) {
    }
    
    @Override
    public void fileParsingStarted(CsmFile file) {
    }
    
    @Override
    public void fileParsingFinished(CsmFile file) {
    }
    
    @Override
    public void parserIdle() {
    }

    @Override
    public void fileRemoved(CsmFile file) {
    }
}
