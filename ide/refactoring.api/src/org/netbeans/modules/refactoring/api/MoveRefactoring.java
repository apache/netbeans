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
 * This class is just holder for parameters of Move Refactoring.
 * Refactoring itself is implemented in plugins.
 * 
 * @see org.netbeans.modules.refactoring.spi.RefactoringPlugin
 * @see org.netbeans.modules.refactoring.spi.RefactoringPluginFactory
 * @see AbstractRefactoring
 * @see RefactoringSession
 * @author Jan Becicka
  */
public final class MoveRefactoring extends AbstractRefactoring {

    private Lookup target;

    /**
     * Public constructor takes Lookup containing objects to refactor as parameter.
     * Move Refactoring implementations currently understand following types:
     * <table border="1">
     *   <tr><th>Module</th><th>Types the Module Understands</th><th>Implementation</th></tr>
     *   <tr><td>Refactoring API (Default impl.)</td><td>{@link org.openide.filesystems.FileObject}(s)</td><td>Does file(s) move</td></tr>
     *   <tr><td>Java Refactoring</td><td>{@link org.openide.filesystems.FileObject}(s) with content type text/x-java</td><td>Does refactoring inside .java files</td></tr>
     *   <tr><td></td><td>{@link org.netbeans.api.java.source.TreePathHandle}(s)</td><td>Does refactoring of members in .java files</td></tr>
     *   <tr><td></td><td>{@link org.netbeans.api.java.source.TreePathHandle} of class</td><td>Does refactoring of class in .java files</td></tr>
     * </table>
     * @param objectsToMove store your objects into Lookup
     */
    public MoveRefactoring (@NonNull Lookup objectsToMove) {
        super(objectsToMove);
    }

    /**
     * Target for moving.
     * Move Refactoring implementations currently understand following types:
     * <table border="1">
     *   <tr><th>Module</th><th>Types the Module Understands</th><th>Implementation</th></tr>
     *   <tr><td>Refactoring API (Default impl.)</td><td>{@link java.net.URL}</td>
     *        <td>Creates directory corresponding to specified {@link java.net.URL} if does not 
     *            exist and moves all FileObjects into this folder.</td></tr>
     *   <tr><td>Java Refactoring</td><td>{@link java.net.URL}</td><td>Does move refactoring inside .java files</td></tr>
     *   <tr><td></td><td>{@link org.netbeans.api.java.source.TreePathHandle}</td><td>Does refactoring of members in .java files</td></tr>
     * </table>
     * @param target
     */
    public void setTarget(@NonNull Lookup target) {
        Parameters.notNull("target", target); // NOI18N
        this.target = target;
    }

    /**
     * Target for moving
     * @see #setTarget
     * @return target
     */
    @CheckForNull
    public Lookup getTarget() {
        return this.target;
    }
}
