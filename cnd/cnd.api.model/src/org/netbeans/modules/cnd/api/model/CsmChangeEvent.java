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

package org.netbeans.modules.cnd.api.model;

import java.util.Collection;
import java.util.EventObject;
import java.util.Map;

/**
 * Event for model change notifications
 */
public abstract class CsmChangeEvent extends EventObject {

    protected CsmChangeEvent(Object source) {
        super(source);
    }

    public abstract Collection<CsmFile> getNewFiles();

    public abstract Collection<CsmFile> getRemovedFiles();

    public abstract Collection<CsmFile> getChangedFiles();

    public abstract Collection<CsmOffsetableDeclaration> getNewDeclarations();
    
    public abstract Collection<CsmOffsetableDeclaration> getRemovedDeclarations();
    
    public abstract Map<CsmOffsetableDeclaration,CsmOffsetableDeclaration> getChangedDeclarations();
    
    public abstract Collection<CsmProject> getChangedProjects();
    
    public abstract Collection<CsmProject> getProjectsWithChangedLibs();
    
    public abstract Collection<CsmNamespace> getNewNamespaces();
    
    public abstract Collection<CsmNamespace> getRemovedNamespaces();
}

