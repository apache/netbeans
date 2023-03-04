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
package org.netbeans.modules.refactoring.api;

import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.api.annotations.common.NonNull;
import org.openide.util.Lookup;
import org.openide.util.Parameters;

/**
 * Refactoring used for renaming objects.
 * @see org.netbeans.modules.refactoring.spi.RefactoringPlugin
 * @see org.netbeans.modules.refactoring.spi.RefactoringPluginFactory
 * @see AbstractRefactoring
 * @see RefactoringSession
 * @author Jan Becicka, Martin Matula, Pavel Flaska, Daniel Prusa
 */
public final class RenameRefactoring extends AbstractRefactoring {
    private String newName = null;
    private boolean searchInComments;

    /**
     * Creates a new instance of RenameRefactoring.
     * Rename Refactoring implementations currently understand following types:
     * <table border="1">
     *   <tr><th>Module</th><th>Types the Module Understands</th><th>Implementation</th></tr>
     *   <tr><td>Refactoring API (Default impl.)</td><td>FileObject</td><td>Does file rename</td></tr>
     *   <tr><td>Java Refactoring</td><td><ul>
     *                                    <li>{@link org.openide.filesystems.FileObject}(s) with content type text/x-java (class rename)
     *                                    <li>{@link org.openide.filesystems.FileObject} (folder) folder rename 
     *                                    <li>{@link org.netbeans.api.java.source.TreePathHandle} (class, field, method rename)
     *                                    <li>{@link org.netbeans.api.fileinfo.NonRecursiveFolder} package rename</td>
     *                                    </ul>
     *                              <td>Does refactoring inside .java files. 
     *                               In case of FolderRename it also does corresponding file moves</td></tr>
     * </table>
     * @param item put object to rename into Lookup instance.
     */
    public RenameRefactoring(@NonNull Lookup item) {
        super(item);
    }
    
    /**
     * Getter for property newName
     * @return Value of property newName
     */
    @CheckForNull
    public String getNewName() {
        return newName;
    }
    
    /**
     * Setter for property newName
     * @param newName New value of property newName
     */
    public void setNewName(@NonNull String newName) {
        Parameters.notNull("newName", newName); // NOI18N
        this.newName = newName;
    }
    
    /**
     * Getter for boolean property searchInComments
     * @return true if user selected search in comments
     */
    public boolean isSearchInComments() {
        return searchInComments;
    }

    /**
     * Setter for property searchInComments.
     * @param searchInComments New value of property searchInComments
     */
    public void setSearchInComments(boolean searchInComments) {
        this.searchInComments = searchInComments;
    }
}
