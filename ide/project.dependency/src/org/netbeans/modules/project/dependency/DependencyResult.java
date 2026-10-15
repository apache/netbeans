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

import java.io.IOException;
import java.util.Collection;
import javax.swing.event.ChangeListener;
import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.project.Project;
import org.openide.filesystems.FileObject;
import org.openide.util.Lookup;

/**
 * Results of a dependency inspection. Contains dependency graph from the {@link #getRoot}.
 * The result may become invalid, as a result of project changes or dependency changes.
 * The state change will be reported by {@link ChangeListener}. If the client is interested
 * in an updated result, it must perform another dependency scan. Once invalid instance
 * will never turn back to valid. Source mapping can be also monitored, the result will inform
 * a listener added by {@link #addSourceChangeListener} that the {@link SourceLocation}s returned
 * from {@link #getDeclarationRange} may have changed, e.g. as a result of a editor operation.
 * <p>
 * The {@link #getLookup() lookup} can be used to search for project-specific services that
 * can provide further info on the artifacts or dependencies.
 * 
 * PENDING: move to SPI, make API delegating wrapper.
 * @author sdedic
 */
public interface DependencyResult extends Lookup.Provider {
    /**
     * @return the inspected project
     */
    public Project getProject();
    
    /**
     * Returns files that may declare dependencies contained in this report.
     * @return project files that define dependencies.
     */
    public Collection<FileObject>   getDependencyFiles();
    
    /**
     * The root of the dependency tree. Its artifact should represent the project itself.
     * @return project dependency root.
     */
    public Dependency getRoot();
    
    /**
     * Checks if the data is still valid
     * @return true, if the data is valid
     */
    public boolean isValid();
    
    /**
     * Returns artifacts that may be unavailable or erroneous.
     * @return problem artifacts
     */
    public Collection<ArtifactSpec> getProblemArtifacts();
    
    /**
     * Registers a Listener to be notified when validity changes, e.g. as a result
     * of project reload.
     * @param l listener
     */
    public void addChangeListener(ChangeListener l);

    /**
     * Unregisters a previously registered Listener.
     * @param l listener
     */
    public void removeChangeListener(ChangeListener l);
    
    /**
     * Registers a listener that gets notified if the source locations could change, as
     * a result of e.g. text edit.
     * @param l the listener
     */
    public void addSourceChangeListener(ChangeListener l);
    
    /**
     * Removes a previously registered listener
     * @param l the listener
     */
    public void removeSourceChangeListener(ChangeListener l);
    
    /**
     * Name part of the dependency declaration.
     */
    public static final String PART_NAME = "name"; // NOI18N

    /**
     * Group or publisher part of the dependency declaration.
     */
    public static final String PART_GROUP = "group"; // NOI18N
    
    /**
     * The version part of the dependency declaration.
     */
    public static final String PART_VERSON = "version"; // NOI18N
    
    /**
     * The scope part of the dependency declaration.
     */
    public static final String PART_SCOPE = "scope"; // NOI18N
    
    /**
     * A special part that locates a location appropriate for the surrounding
     * container. For example {@code dependencies} element in Maven or {@code dependencies}
     * block in a gradle script. Use project root or {@code null} as the dependency
     */
    public static final String PART_CONTAINER = "container"; // NOI18N

    /**
     * Attempts to find location where this dependency is declared. 
     * @param d the dependency to query
     * @param part a specific part that should be located in the text. 
     * @return the location for the dependency or its part; {@code null} if the
     * source location can not be determined.
     */
    public @CheckForNull SourceLocation getDeclarationRange(@NonNull Dependency d, String part) throws IOException;
    
    /**
     * Returns description of project scopes.
     * @return project scopes.
     */
    public ProjectScopes getScopes();
}
