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

import org.netbeans.api.annotations.common.NonNull;
import org.openide.util.Lookup;

/**
 * Refactoring to Safely Delete an element after checking its usages.
 * @see org.netbeans.modules.refactoring.spi.RefactoringPlugin
 * @see org.netbeans.modules.refactoring.spi.RefactoringPluginFactory
 * @see AbstractRefactoring
 * @see RefactoringSession
 * @author Bharath Ravikumar, Jan Becicka
 */
public final class SafeDeleteRefactoring extends AbstractRefactoring {
    private boolean checkInComments;

    /**
     * Creates a new instance of SafeDeleteRefactoring, passing Lookup containing the candidate
     * elements as parameter.
     * 
     * Safe Delete Refactoring implementations currently understand following types:
     * <table>
     *   <caption>SafeDeleteRefactoring types supported</caption>
     *   <tr><th>Module</th><th>Types the Module Understands</th><th>Implementation</th></tr>
     *   <tr><td>Refactoring API (Default impl.)</td><td>FileObject</td><td>Does file delete</td></tr>
     *   <tr><td>Java Refactoring</td><td><ul><li>{@link org.openide.filesystems.FileObject}(s) with content type text/x-java (safly delete class)
     *                                <li><a href="@org-netbeans-modules-java-source-base@/org/netbeans/api/java/source/TreePathHandle.html">org.netbeans.api.java.source.TreePathHandle</a> (class, field, method)</ul></td>
     *                              <td>Check for usages and does refactoring inside .java files.</td></tr>
     * </table>
     * @param namedElements The elements to be safely deleted
     */
    public SafeDeleteRefactoring(@NonNull Lookup namedElements) {
        super(namedElements);
    }
    
    /**
     * Indicates whether the usage of the elements will also be checked
     * in comments before deleting the elements
     * @return Returns the value of the field checkInComments
     */
    public boolean isCheckInComments() {
        return checkInComments;
    }
    
    /**
     * Sets whether or not the usage of the elements will be checked
     * in comments before deleting the elements
     * @param checkInComments Sets the checInComments field of this class
     */
    public void setCheckInComments(boolean checkInComments) {
        this.checkInComments = checkInComments;
    }
}
