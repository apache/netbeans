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

import java.util.ArrayList;
import java.util.List;
import org.netbeans.modules.cnd.api.model.CsmChangeEvent;
import org.netbeans.modules.cnd.api.model.CsmClass;
import org.netbeans.modules.cnd.api.model.CsmFile;
import org.netbeans.modules.cnd.api.model.CsmFunction;
import org.netbeans.modules.cnd.api.model.CsmFunctionDefinition;
import org.netbeans.modules.cnd.api.model.CsmNamespace;
import org.netbeans.modules.cnd.api.model.CsmNamespaceDefinition;
import org.netbeans.modules.cnd.api.model.CsmObject;
import org.netbeans.modules.cnd.api.model.CsmOffsetableDeclaration;
import org.netbeans.modules.cnd.api.model.CsmProject;
import org.netbeans.modules.cnd.api.model.util.CsmKindUtilities;
import org.netbeans.modules.cnd.classview.model.ProjectNode;
import org.openide.nodes.Children;
import org.openide.nodes.Node;

/**
 *
 */
/*package-local*/ class ClassViewModel {
    
    private ClassViewUpdater updater;
    private ChildrenUpdater childrenUpdater;
    
    public ClassViewModel() {
        updater = new ClassViewUpdater(this);
        childrenUpdater = new ChildrenUpdater();
        updater.start();
    }
    
    public RootNode getRoot() {
        if( root == null ) {
            root = createRoot();
        }
        return root;
    }
    
    private RootNode createRoot() {
        return new RootNode(childrenUpdater);
    }
    
    /*package local*/ void openProject(CsmProject project){
        if( root == null ) { // paranoya
            root = createRoot();
            //return;
        }
        ProjectsKeyArray children = (ProjectsKeyArray)root.getChildren();
        children.openProject(project);
        childrenUpdater.openProject(project);
//        if (project.isArtificial()) {
//            Node[] childNodes = children.getNodes();
//            for (Node childNode : childNodes) {
//                Children grandChildren = childNode.getChildren();
//                if (grandChildren instanceof ProjectsKeyArray) {
//                    ((ProjectsKeyArray) grandChildren).openProject(project);
//                }
//                if (grandChildren instanceof NamespaceKeyArray) {
//                    ((NamespaceKeyArray) grandChildren).openProject(project);
//                }
//            }
//        } else {
//            children.openProject(project);
//        }
    }
    
    /*package local*/ void closeProject(CsmProject project){
        if( root == null ) { // paranoya
            return;
        }
        childrenUpdater.closeProject(project);
        childrenUpdater.unregister(project);
        ProjectsKeyArray children = (ProjectsKeyArray)root.getChildren();
        children.closeProject(project);
    }

    /*package local*/ void scheduleUpdate(CsmChangeEvent e) {
        updater.scheduleUpdate(e);
    }
    
    private volatile boolean userActivity = false;
    /*package local*/ void setUserActivity(boolean active){
        userActivity = active;
    }
    /*package local*/ boolean isUserActivity(){
        return userActivity;
    }
    
    /*package local*/ void dispose() {
        if( Diagnostic.DEBUG ) Diagnostic.trace("ClassesM: Dispose model"); // NOI18N
        updater.setStop();
        childrenUpdater.unregister();
        if (root !=null){
            root.destroy();
            root = null;
        }
        updater = null;
        childrenUpdater = null;
    }
    
    /*package local*/ void update(final SmartChangeEvent e) {
        if (childrenUpdater != null) {
            childrenUpdater.update(e);
        }
    }

    /*package local*/ Node findDeclaration(CsmOffsetableDeclaration decl) {
        if (root == null) {
            return null;
        }
        ProjectsKeyArray children = (ProjectsKeyArray)root.getChildren();
        CsmFile file = decl.getContainingFile();
        CsmProject project = file.getProject();
        children.ensureAddNotify();
        ProjectNode projectNode = (ProjectNode) children.findChild(project.getName().toString());
        if (projectNode == null) {
            return null;
        }
        List<CsmObject> path = new ArrayList<CsmObject>();
        CsmObject scope;
        if (CsmKindUtilities.isFunctionDefinition(decl)){
            CsmFunction func = ((CsmFunctionDefinition)decl).getDeclaration();
            if (func != null){
                decl = func;
            }
            path.add(decl);
            scope = decl.getScope();
        } else if (CsmKindUtilities.isNamespaceDefinition(decl)){
            CsmNamespace ns = ((CsmNamespaceDefinition)decl).getNamespace();
            path.add(ns);
            scope = ns.getParent();
        } else {
            path.add(decl);
            scope = decl.getScope();
        }
        while(scope != null) {
            if (CsmKindUtilities.isFile(scope)) {
                path.add(project.getGlobalNamespace());
                break;
            }
            path.add(scope);
            if (CsmKindUtilities.isNamespace(scope)) {
                CsmNamespace ns = (CsmNamespace)scope;
                if (ns.isGlobal()){
                    break;
                }
                scope = ns.getParent();
            } else if (CsmKindUtilities.isClass(scope)) {
                CsmClass cls = (CsmClass)scope;
                scope = cls.getScope();
            } else {
                break;
            }
        }
        Node res = null;
        HostKeyArray child = (HostKeyArray) projectNode.getChildren();
        for (int i = path.size() - 2; i >= 0; i--){
            child.ensureInited();
            scope = path.get(i);
            res = child.findChild(scope);
            if (res != null && (res.getChildren() instanceof HostKeyArray)) {
                child = (HostKeyArray) res.getChildren();
            }
        }
        return res;
    }

    private RootNode root;
    
}
