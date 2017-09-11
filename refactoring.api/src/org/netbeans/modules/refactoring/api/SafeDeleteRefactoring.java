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
     * <table border="1">
     *   <tr><th>Module</th><th>Types the Module Understands</th><th>Implementation</th></tr>
     *   <tr><td>Refactoring API (Default impl.)</td><td>FileObject</td><td>Does file delete</td></tr>
     *   <tr><td>Java Refactoring</td><td><ul><li>{@link org.openide.filesystems.FileObject}(s) with content type text/x-java (safly delete class)
     *                                <li>{@link org.netbeans.api.java.source.TreePathHandle} (class, field, method)</td>
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
