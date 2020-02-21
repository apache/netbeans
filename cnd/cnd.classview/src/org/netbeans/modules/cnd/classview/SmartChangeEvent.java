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

import java.util.*;
import org.netbeans.modules.cnd.api.model.CsmChangeEvent;
import org.netbeans.modules.cnd.api.model.CsmFile;
import org.netbeans.modules.cnd.api.model.CsmNamespace;
import org.netbeans.modules.cnd.api.model.CsmOffsetableDeclaration;
import org.netbeans.modules.cnd.api.model.CsmProject;

/**
 *
 */
public class SmartChangeEvent {
    private Map<CsmProject,Storage> changedProjects = new HashMap<CsmProject,Storage>();
    
    // to trace only
    private int count = 1;
    
    public SmartChangeEvent(CsmChangeEvent e){
        //super(e.getSource());
        doAdd(e);
    }
    
    public boolean addChangeEvent(CsmChangeEvent e){
        if (e.getRemovedDeclarations().isEmpty()){
            doAdd(e);
            count++;
            return true;
        }
        return false;
    }
    
    public boolean addChangeEvent(SmartChangeEvent e){
        for(Storage storage : getChangedProjects().values()){
            if (storage.getRemovedDeclarations().size() > 0 ||
                storage.getRemovedNamespaces().size() > 0){
                return false;
            }
        }
        doAdd(e);
        count++;
        return true;
    }
    
    int getCount(){
        return count;
    }
    
    private void doAdd(SmartChangeEvent e){
        for(Map.Entry<CsmProject,Storage> entry : e.getChangedProjects().entrySet()){
            CsmProject project = entry.getKey();
            Storage storage = changedProjects.get(project);
            if (storage == null) {
                changedProjects.put(project, entry.getValue());
            } else {
                storage.getNewNamespaces().addAll(entry.getValue().getNewNamespaces());
                storage.getRemovedNamespaces().addAll(entry.getValue().getRemovedNamespaces());
                storage.getNewDeclarations().addAll(entry.getValue().getNewDeclarations());
                storage.getRemovedDeclarations().addAll(entry.getValue().getRemovedDeclarations());
                storage.getChangedDeclarations().putAll(entry.getValue().getChangedDeclarations());
                if (entry.getValue().hasChangedLibs()) {
                    storage.setChangedLibs(true);
                }
            }
        }
    }

    private void doAdd(CsmChangeEvent e){
        for (CsmNamespace ns : e.getNewNamespaces()){
            Storage storage = getStorage(ns);
            if (storage != null){
                storage.addNewNamespaces(ns);
            }
        }
        for (CsmNamespace ns : e.getRemovedNamespaces()){
            Storage storage = getStorage(ns);
            if (storage != null){
                storage.addRemovedNamespaces(ns);
            }
        }
        for (CsmOffsetableDeclaration decl : e.getNewDeclarations()){
            Storage storage = getStorage(decl);
            if (storage != null){
                storage.addNewDeclaration(decl);
            }
        }
        for (CsmOffsetableDeclaration decl : e.getRemovedDeclarations()){
            Storage storage = getStorage(decl);
            if (storage != null){
                storage.addRemovedDeclarations(decl);
            }
        }
        for (Map.Entry<CsmOffsetableDeclaration,CsmOffsetableDeclaration> decl : e.getChangedDeclarations().entrySet()){
            Storage storage = getStorage(decl.getValue());
            if (storage != null){
                storage.addChangedDeclarations(decl.getKey(),decl.getValue());
            }
        }
        for (CsmProject proj : e.getProjectsWithChangedLibs()) {
            Storage storage = getStorage(proj);
            if (storage != null){
                storage.setChangedLibs(true);
            }
        }
    }
    
    private Storage getStorage(CsmNamespace ns){
        CsmProject project = ns.getProject();
        return getStorage(project);
    }
    
    private Storage getStorage(CsmOffsetableDeclaration decl){
        CsmProject project = findProject(decl);
        return getStorage(project);
    }
    
    private Storage getStorage(CsmProject project){
        if (project != null && project.isValid()){
            Storage storage = changedProjects.get(project);
            if (storage == null) {
                storage = new Storage(project);
                changedProjects.put(project, storage);
            }
            return storage;        
        }
        return null;
    }

    private static CsmProject findProject(CsmOffsetableDeclaration decl){
        CsmFile file = decl.getContainingFile();
        if (file != null){
            //if (file.isValid()) {
                return file.getProject();
            //}
            //return null;
        }
        System.err.println("Cannot fing project for declaration "+decl.getUniqueName());
        return null;
    }
    
    public Map<CsmProject,Storage> getChangedProjects(){
        return changedProjects;
    }
    
    public static class Storage {
        private final CsmProject changedProject;
        private final Set<CsmNamespace>  newNamespaces = new HashSet<CsmNamespace>();
        private final Set<CsmNamespace>  removedNamespaces = new HashSet<CsmNamespace>();
        private final Set<CsmOffsetableDeclaration> newDeclarations = new HashSet<CsmOffsetableDeclaration>();
        private final Set<CsmOffsetableDeclaration> removedDeclarations = new HashSet<CsmOffsetableDeclaration>();
        private final Map<CsmOffsetableDeclaration,CsmOffsetableDeclaration> changedDeclarations = new LinkedHashMap<CsmOffsetableDeclaration,CsmOffsetableDeclaration>();
        private boolean changedLibs = false;
        
        public Storage(CsmProject project){
            changedProject = project;
        }
        
        public Collection<CsmOffsetableDeclaration> getNewDeclarations() {
            return newDeclarations;
        }
        
        private void addNewDeclaration(CsmOffsetableDeclaration declaration) {
            newDeclarations.add(declaration);
        }
        
        public Collection<CsmOffsetableDeclaration> getRemovedDeclarations() {
            return removedDeclarations;
        }
        
        private void addRemovedDeclarations(CsmOffsetableDeclaration declaration) {
            removedDeclarations.add(declaration);
        }
        
        public Map<CsmOffsetableDeclaration,CsmOffsetableDeclaration> getChangedDeclarations() {
            return changedDeclarations;
        }
        
        private void addChangedDeclarations(CsmOffsetableDeclaration oldDecl, CsmOffsetableDeclaration newDecl) {
            changedDeclarations.put(oldDecl, newDecl);
        }
        
        public CsmProject getProject() {
            return changedProject;
        }
        
        public Collection<CsmNamespace> getNewNamespaces() {
            return newNamespaces;
        }
        
        public boolean hasChangedLibs() {
            return changedLibs;
        }

        public void setChangedLibs(boolean changedLibs) {
            this.changedLibs = changedLibs;
        }
                
        private void addNewNamespaces(CsmNamespace ns) {
            newNamespaces.add(ns);
        }
        
        public Collection<CsmNamespace> getRemovedNamespaces() {
            return removedNamespaces;
        }
        
        private void addRemovedNamespaces(CsmNamespace ns) {
            removedNamespaces.add(ns);
        }
    }
}
