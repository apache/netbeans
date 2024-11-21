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

package org.netbeans.modules.maven.api.execute;

import java.io.File;
import java.util.List;
import java.util.Map;
import org.apache.maven.project.MavenProject;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.annotations.common.NullAllowed;
import org.netbeans.api.project.Project;
import org.openide.filesystems.FileObject;
import org.openide.util.Lookup;

/**
 * Context provider for maven executors and checkers. Never to be implemented by
 * client code.
 * @author Milos Kleint
 */
public interface RunConfig {
    
    /**
     * directory where the maven build execution happens.
     * @return
     */
    File getExecutionDirectory();

    void setExecutionDirectory(File directory);

    RunConfig getPreExecution();

    void setPreExecution(RunConfig config);

    RunConfig.ReactorStyle getReactorStyle();

//    void setPreExecution(RunConfig config);

    /**
     * project that is being used for execution, can be null.
     * @return 
     */
    Project getProject();

    /**
     * the maven project instance loaded with the context of execution,
     * with execution's profiles enabled and execution properties injected.
     * Can differd from the MavenProject returned from within the Project instance.
     * All Maven model checks shall be done against this instance.
     * @return
     */
    MavenProject getMavenProject();

    /**
     * goals to be executed.
     * @return a list of goals to run
     */
    List<String> getGoals();

    String getExecutionName();
    
    String getTaskDisplayName();

    String getActionName();
    
    /**
     * Options/switches passed to maven.
     * @return a read-only copy of the current maven options
     * @since 2.167
     */
    @NonNull Map<? extends String,? extends String> getOptions();
    
    /**
     * Sets option that will be passed to maven.
     * @param key a key that represents option/switch name
     * @param value a value of the option/switch
     * @since 2.167
     */
    void setOption(@NonNull String key, @NullAllowed String value);

    /** 
     * Adds options/switches that will be passed to maven.
     * @param options options/switches that will be added
     * @since 2.167
     */
    void addOptions(@NonNull Map<String, String> options);
    
    /**
     * Properties to be used in execution.
     * @return a read-only copy of the current properties (possibly inherited from the parent)
     */
    @NonNull Map<? extends String,? extends String> getProperties();

    void setProperty(@NonNull String key, @NullAllowed String value);
    
    void addProperties(@NonNull Map<String, String> properties);  
    
    void setInternalProperty(@NonNull String key, @NullAllowed Object value);
    
    @NonNull Map<? extends String, ? extends Object> getInternalProperties();
    
    boolean isShowDebug();
    
    boolean isShowError();
    
    Boolean isOffline();
    
    void setOffline(Boolean bool);
    
    boolean isRecursive();
    
    boolean isUpdateSnapshots();

    List<String> getActivatedProfiles();
    
    void setActivatedProfiles(List<String> profiles);
    
    boolean isInteractive();

    FileObject getSelectedFileObject();

    /**
     * Provides access to possible additional parameters from the action invoker. Must
     * not return {@code null}, use {@link Lookup#EMPTY} for empty instance.
     * @return action context Lookup
     * @since 2.144
     */
    default Lookup getActionContext() { 
        return Lookup.EMPTY;
    }

    public enum ReactorStyle {
        NONE,
        /**
         * am, --also-make
         * If project list is specified, also build projects required by the list
         */
        ALSO_MAKE,
        /**
         * -amd,--also-make-dependents
         * If project list is specified, also build projects that depend on projects on the list
         */
        ALSO_MAKE_DEPENDENTS
    }
    
}
