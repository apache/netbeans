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

package org.netbeans.modules.cnd.modelimpl.csm.core;


import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import org.netbeans.modules.cnd.api.model.CsmChangeEvent;
import org.netbeans.modules.cnd.api.model.CsmFile;
import org.netbeans.modules.cnd.api.model.CsmNamespace;
import org.netbeans.modules.cnd.api.model.CsmOffsetableDeclaration;
import org.netbeans.modules.cnd.api.model.CsmProject;

/**
 * CsmChangeEvent implementation
 */
public class ChangeEventImpl extends CsmChangeEvent {

    protected Set<CsmFile> newFiles;
    protected Set<CsmFile> removedFiles;
    protected Set<CsmFile> changedFiles;
    
    protected Set<CsmOffsetableDeclaration> newDeclarations;
    protected Set<CsmOffsetableDeclaration> removedDeclarations;
    protected Map<CsmOffsetableDeclaration,CsmOffsetableDeclaration> changedDeclarations;
    
    protected Set<CsmProject>   changedProjects;
    protected Set<CsmProject>   projectsWithChangedLibs;
    
    protected Map<CharSequence, CsmNamespace>   newNamespaces;
    protected Map<CharSequence, CsmNamespace>   removedNamespaces;
    
    public ChangeEventImpl(Object source) {
	super(source);
    }
    
    @Override
    public Collection<CsmFile> getNewFiles() {
	if( newFiles == null ) {
	    newFiles = new HashSet<>();
	}
	return newFiles;
    }
    
    @Override
    public Collection<CsmFile> getRemovedFiles() {
	if( removedFiles == null ) {
	    removedFiles = new HashSet<>();
	}
	return removedFiles;
    }
    
    @Override
    public Collection<CsmFile> getChangedFiles() {
	if( changedFiles == null ) {
	    changedFiles = new HashSet<>();
	}
	return changedFiles;
    }
    
    @Override
    public Collection<CsmOffsetableDeclaration> getNewDeclarations() {
	if( newDeclarations == null ) {
	    newDeclarations = new HashSet<>();
	}
	return newDeclarations;
    }
    
    @Override
    public Collection<CsmOffsetableDeclaration> getRemovedDeclarations() { 
	if( removedDeclarations == null ) { 
	    removedDeclarations = new HashSet<>(); 
	} 
	return removedDeclarations; 
    }
    
    @Override
    public Map<CsmOffsetableDeclaration,CsmOffsetableDeclaration> getChangedDeclarations() { 
	if( changedDeclarations == null ) { 
	    changedDeclarations = new HashMap<>(); 
	} 
	return changedDeclarations; 
    }
    
    @Override
    public Collection<CsmProject> getChangedProjects() {
        if( changedProjects == null ) {
            changedProjects = new HashSet<>();
        }
        return changedProjects;
    }
    
    @Override
    public Collection<CsmProject> getProjectsWithChangedLibs() {
        return projectsWithChangedLibs == null ? 
                Collections.<CsmProject>emptyList() : 
                Collections.unmodifiableSet(projectsWithChangedLibs);
    }

    @Override
    public Collection<CsmNamespace> getNewNamespaces() {
        if( newNamespaces != null ) {
            return newNamespaces.values();
        }
        return Collections.<CsmNamespace>emptyList();
    }
    
    @Override
    public Collection<CsmNamespace> getRemovedNamespaces() {
        if( removedNamespaces != null ) {
            return removedNamespaces.values();
        }
        return Collections.<CsmNamespace>emptyList();
    }
    
    public boolean isEmpty() {
        return 
            (changedProjects == null || changedProjects.isEmpty()) &&
            (newFiles == null || newFiles.isEmpty()) && 
            (changedFiles == null || changedFiles.isEmpty()) &&
            (removedFiles == null || removedFiles.isEmpty()) &&
            (newDeclarations == null || newDeclarations.isEmpty()) && 
            (removedDeclarations== null || removedDeclarations.isEmpty()) && 
            (changedDeclarations == null || changedDeclarations.isEmpty()) &&
            (newNamespaces == null || newNamespaces.isEmpty()) &&
            (removedNamespaces == null || removedNamespaces.isEmpty());
    }    
    
    public void addChangedFile(CsmFile file) {
        getChangedFiles().add(file);
        getChangedProjects().add(file.getProject());
    }

    public void addNewFile(CsmFile file) {
        getNewFiles().add(file);
        getChangedProjects().add(file.getProject());
    }
    
    public void addRemovedFile(CsmFile file) {
        getRemovedFiles().add(file);
        getChangedProjects().add(file.getProject());
    }

    public void  addChangedDeclaration(CsmOffsetableDeclaration oldDecl, CsmOffsetableDeclaration newDecl) {
        getChangedDeclarations().put(oldDecl,newDecl);
        addChangedFile(oldDecl.getContainingFile());
    }

    public void addNewDeclaration(CsmOffsetableDeclaration declaration) {
        getNewDeclarations().add(declaration);
        CsmFile file = declaration.getContainingFile();
        if( ! getNewFiles().contains(file) ) {
            addChangedFile(file);
        }
    }
    
    public void addRemovedDeclaration(CsmOffsetableDeclaration declaration) {
        getRemovedDeclarations().add(declaration);
        CsmFile file = declaration.getContainingFile();
        if( ! getRemovedFiles().contains(file) ) {
            addChangedFile(file);
        }
    }
    
    public void addNewNamespace(CsmNamespace ns) {
        // remove from removed
        if (_getRemovedNamespaces().remove(ns.getQualifiedName()) == null) {
            _getNewNamespaces().put(ns.getQualifiedName(), ns);
            getChangedProjects().add(ns.getProject());
        }
    }
    
    public void addRemovedNamespace(CsmNamespace ns) {
        // put in removed only if not added as "new"
        if (_getNewNamespaces().get(ns.getQualifiedName()) == null) {
            _getRemovedNamespaces().put(ns.getQualifiedName(), ns);
            getChangedProjects().add(ns.getProject());
        }
    }   
    
    public void addProjectThatChangedLibs(CsmProject project) {
        if (projectsWithChangedLibs == null) {
            projectsWithChangedLibs = new HashSet<>();
        }        
        projectsWithChangedLibs.add(project);
    }

    private Map<CharSequence, CsmNamespace> _getRemovedNamespaces() {
        if (removedNamespaces == null) {
            removedNamespaces = new HashMap<>();            
        }
        return removedNamespaces;
    }
    
    private Map<CharSequence,CsmNamespace> _getNewNamespaces() {
        if (newNamespaces == null) {
            newNamespaces = new HashMap<>();            
        }
        return newNamespaces;
    }    
}
