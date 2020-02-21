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
