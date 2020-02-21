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
package org.netbeans.modules.cnd.api.project;

import java.util.List;
import org.netbeans.modules.cnd.utils.FSPath;
import org.netbeans.modules.cnd.utils.NamedRunnable;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileSystem;
import org.openide.util.Lookup;

public interface NativeProject {
    /**
     * Returns project object, 
     * i.e. Netbeans project object
     * org.netbeans.api.project.Project
     */
    public Lookup.Provider getProject();

    /**
     * File system project sources reside in
     */
    public FileSystem getFileSystem();
    
     /**
     * Returns file path to project root
     * @return file path to project root
     */
    public String getProjectRoot();

    
     /**
     * Returns file paths to source roots
     * @return file paths to source roots
     */
    public List<String> getSourceRoots();

    /**
     * Returns the display name of the project
     * @return display name of the project
     */
    public String getProjectDisplayName();

     /**
      * Returns a list of all files in the project.
      * @return a list of all files in the project.
      */
     public List<NativeFileItem> getAllFiles();

     /**
      * Returns a list of standard headers indexers in the project.
      * @return a list of standard headers indexers in the project.
      */
     public List<NativeFileItem> getStandardHeadersIndexers();

     /**
      * Adds a listener to changes when items are added to or removed from the project.
      * @param listener a listener to add
      */
     public void addProjectItemsListener(NativeProjectItemsListener listener);

     /**
      * Removes a listener.
      * @param listener a listener to remove
      */
     public void removeProjectItemsListener(NativeProjectItemsListener listener);
     
     /**
      * Finds a file item in the project.
      * @param fileObject  a file object to find item for
      * @return the file item if found. Otherwise it returns null.
      */
     public NativeFileItem findFileItem(FileObject fileObject);
     
    /**
     * Returns a list <IncludePath> of compiler defined include paths used when parsing 'orpan' source files.
     * @return a list <IncludePath> of compiler defined include paths.
     * A path is always an absolute path.
     */
    public List<IncludePath> getSystemIncludePaths();
    
    /**
     * Returns a list <IncludePath> of user defined include paths used when parsing 'orpan' source files.
     * @return a list <IncludePath> of user defined include paths.
     * A path is always an absolute path.
     * Include paths are not prefixed with the compiler include path option (usually -I).
     */
    public List<IncludePath> getUserIncludePaths();
    
    /**
     * Returns a list of system pre-included headers.
     * @return list of included files
     * A path is always an absolute path.
     */
    public List<FSPath> getSystemIncludeHeaders();

    /**
     * Returns a list of '-include file' options 
     * as if #include "file" appeared as the first line of the primary source file.
     * However, the first directory searched for file is the preprocessor's working directory 
     * instead of the directory containing the main source file. 
     * If not found there, it is searched for in the remainder of the #include "..." search chain as normal. 
     * @return list of included files
     */
    public List<FSPath> getIncludeFiles();
    
    /**
     * Returns a list <String> of compiler defined macro definitions used when parsing 'orpan' source files.
     * @return a list <String> of compiler defined macro definitions.
     * Macro definitions are not prefixed with the compiler option (usually -D).
     */
    public List<String> getSystemMacroDefinitions();
    
    /**
     * Returns a list <String> of user defined macro definitions used when parsing 'orpan' source files.
     * @return a list <String> of user defined macro definitions.
     * Macro definitions are not prefixed with the compiler option (usually -D).
     */
    public List<String> getUserMacroDefinitions();

    /**
     * Returns a list <NativeProject> of libraries.
     * @return a list <NativeProject> of libraries.
     */
    public List<NativeProject> getDependences();
    
    /**
     * Add task which will be run then <NativeProject> is ready to provide Code Model data
     *
     * @param task - task to run. Why is it NamedRunnable?
     * The issue is that when we pass Runnable to be run through a chain of calls,
     * sometimes a user interaction might be needed.
     * For example, we'd like to say user "To perform XXX IDE needs to connect host yyy@zzz" :)
     * In this case NamedRunnable.getName() is used and its value is inserted instead of XXX
     */
    public abstract void runOnProjectReadiness(NamedRunnable task);
    
    /**
     * All native file items were changed. For example because tool collection system include paths were changed.
     */
    public void fireFilesPropertiesChanged();
}
