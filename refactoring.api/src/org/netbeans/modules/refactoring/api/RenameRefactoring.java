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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
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
