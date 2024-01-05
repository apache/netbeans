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
package org.netbeans.modules.project.dependency;

import java.util.Collections;
import java.util.Set;
import org.netbeans.api.project.Project;
import org.openide.filesystems.FileObject;

/**
 * The exception is thrown when an error happens during project operation because
 * of limited or refused access to the project metadata / structure.
 * <p/>
 * It may wrap underlying build system error, such as maven not able to parse the
 * POM.
 * @author sdedic
 */
public final class ProjectOperationException extends IllegalStateException {
    public enum State {
        /**
         * Unexpected project system error, see the exception cause for details.
         */
        ERROR,
        
        /**
         * The project has not been yet fully initialized. The query can not
         * produce sane results.
         */
        UNINITIALIZED,
        
        /**
         * The project definition is broken so that the query cannot be evaluated.
         */
        BROKEN,
        
        /**
         * Online resources are required to evaluate the query, but the query was not
         * allowed to inspect them.
         */
        OFFLINE,
        
        /**
         * The project is OK. The project operation threw an exception.
         */
        OK,
        
        /**
         * Project files are modified, and the model may be out of sync. The client
         * should save project changes and/or reload the model before retry.
         * @since 1.7
         */
        OUT_OF_SYNC,
        
        /**
         * Project configuration is unsupported, or is defined in a way that 
         * makes an operation impossible.
         * @since 1.7
         */
        UNSUPPORTED,
    }
    
    private final Project project;
    private final State state;
    private Set<FileObject> files;
    
    public ProjectOperationException(Project project, State state, String s) {
        this(project, state, s, Collections.emptySet());
    }

    public ProjectOperationException(Project project, State state, String s, Set<FileObject> files) {
        super(s);
        this.files = files;
        this.project = project;
        this.state = state;
    }
    
    public ProjectOperationException(Project project, State state, String message, Throwable cause) {
        super(message, cause);
        this.state = state;
        this.project = project;
        this.files = Collections.emptySet();
    }

    public Project getProject() {
        return project;
    }

    public State getState() {
        return state;
    }

    public Set<FileObject> getFiles() {
        return files;
    }
}
