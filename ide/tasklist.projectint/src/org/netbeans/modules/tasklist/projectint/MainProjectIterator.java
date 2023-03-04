/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.netbeans.modules.tasklist.projectint;

import java.util.ArrayList;
import java.util.Iterator;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.api.project.Sources;
import org.openide.filesystems.FileObject;

/**
 * Iterate all resources (files and folders) that are under the current main project
 * and under opened projects that depend on the main one.
 * The iretator is empty when no project has been set as the main one.
 * 
 * @author S. Aubrecht
 */
class MainProjectIterator implements Iterator<FileObject> {
    
    private Iterator<FileObject> iterator;
    
    /** Creates a new instance of MainProjectIterator */
    public MainProjectIterator() {
    }
    
    public boolean hasNext() {
        initialize();
        return iterator.hasNext();
    }

    public FileObject next() {
        initialize();
        return iterator.next();
    }

    public void remove() {
        throw new UnsupportedOperationException();
    }

    private void initialize() {
        if( null == iterator ) {
            iterator = createIterator();
        }
    }
    
    protected Iterator<FileObject> createIterator() {
        Project currentProject = MainProjectScanningScope.findCurrentProject();
            if( null == currentProject )
                return new EmptyIterator();
        
        ArrayList<FileObject> roots = new ArrayList<FileObject>(10);
        
        addProject( currentProject, roots );
        
        return new FileObjectIterator( roots );
    }
    
    private void addProject( Project p, ArrayList<FileObject> roots ) {
        Sources sources = ProjectUtils.getSources( p );
        SourceGroup[] groups = sources.getSourceGroups( Sources.TYPE_GENERIC );
        for( SourceGroup group : groups ) {
            FileObject rootFolder = group.getRootFolder();
            roots.add( rootFolder );
        }
    }
}
