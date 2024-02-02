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

package org.netbeans.modules.refactoring.api;

import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.api.annotations.common.NonNull;
import org.openide.util.Lookup;
import org.openide.util.Parameters;

/**
 * This class is just holder for parameters of Single Copy Refactoring. 
 * Refactoring itself is implemented in plugins
 * @see org.netbeans.modules.refactoring.spi.RefactoringPlugin
 * @see org.netbeans.modules.refactoring.spi.RefactoringPluginFactory
 * @see AbstractRefactoring
 * @see RefactoringSession
 * @author Jan Becicka
 */
public final class SingleCopyRefactoring extends AbstractRefactoring {

    private Lookup target;
    private String newName;

    /**
     * Creates a new instance of SingleCopyRefactoring.
     * Single Copy Refactoring implementations currently understand following types:
     * <table>
     *   <caption>SingleCopyRefactoring types supported</caption>
     *   <tr><th>Module</th><th>Types the Module Understands</th><th>Implementation</th></tr>
     *   <tr><td>Refactoring API (Default impl.)</td><td>{@link org.openide.filesystems.FileObject}</td><td>Does file copy</td></tr>
     *   <tr><td>Java Refactoring</td><td><ul>
     *                                    <li>{@link org.openide.filesystems.FileObject}(s) with content type text/x-java (class copy)
     *                                    </ul>
     *                              <td>Updates name, package declaration and import statements</td></tr>
     * </table>
     * @param objectToCopy Object to be copied stored into Lookup
     */
    public SingleCopyRefactoring (@NonNull Lookup objectToCopy) {
        super(objectToCopy);
    }

    /**
     * Target for copying.
     * Single Copy Refactoring implementations currently understand following types:
     * <table>
     *   <caption>SingleCopyRefactoring types supported</caption>
     *   <tr><th>Module</th><th>Types the Module Understands</th><th>Implementation</th></tr>
     *   <tr><td>Refactoring API (Default impl.)</td><td>{@link java.net.URL}</td>
     *        <td>Creates directory corresponding to specified URL (if does not 
     *            exist) and copies all FileObjects into this folder.</td></tr>
     *   <tr><td>Java Refactoring</td><td>{@link java.net.URL}</td><td>Updates name, package declaration and import statements</td></tr>
     * </table>
     * @param target
     */
    public void setTarget(@NonNull Lookup target) {
        Parameters.notNull("target", target); // NOI18N
        this.target = target;
    }
    
    /**
     * Target for copying
     * @see #setTarget
     * @return target
     */
    @CheckForNull
    public Lookup getTarget() {
        return target;
    }
    
    /**
     * getter for new name of copied file
     * @return value String value
     */
    @CheckForNull
    public String getNewName() {
        return newName;
    }
    
    /**
     * setter for new name of copied file
     * @param newName new value
     */
    public void setNewName(@NonNull String newName) {
        Parameters.notNull("newName", newName); // NOI18N
        this.newName = newName;
    }
}

