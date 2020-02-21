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
