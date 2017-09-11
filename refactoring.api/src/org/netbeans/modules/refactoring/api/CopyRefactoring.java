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
 * This class is just holder for parameters of Copy Refactoring.
 * Refactoring itself is implemented in plugins.
 * 
 * @see org.netbeans.modules.refactoring.spi.RefactoringPlugin
 * @see org.netbeans.modules.refactoring.spi.RefactoringPluginFactory
 * @see AbstractRefactoring
 * @see RefactoringSession
 * @author Ralph Ruijs
 * @since 1.21
 */
public final class CopyRefactoring extends AbstractRefactoring {

    private Lookup target;

    /**
     * Creates a new instance of CopyRefactoring.
     * Copy Refactoring implementations currently understand the following types:
     * <table border="1">
     *   <tr><th>Module</th><th>Types the Module Understands</th><th>Implementation</th></tr>
     *   <tr><td>Refactoring API (Default impl.)</td><td>{@link org.openide.filesystems.FileObject}(s)</td><td>Does file copy</td></tr>
     *   <tr><td>Java Refactoring</td><td><ul>
     *                                    <li>{@link org.openide.filesystems.FileObject}(s) with content type text/x-java (class copy)
     *                                    </ul>
     *                              <td>Updates name, package declaration and import statements</td></tr>
     * </table>
     * @param objectsToCopy Object to be copied stored into Lookup
     */
    public CopyRefactoring (@NonNull Lookup objectsToCopy) {
        super(objectsToCopy);
    }

    /**
     * Target for copying.
     * Copy Refactoring implementations currently understand the following types:
     * <table border="1">
     *   <tr><th>Module</th><th>Types the Module Understands</th><th>Implementation</th></tr>
     *   <tr><td>Refactoring API (Default impl.)</td><td>{@link java.net.URL}</td>
     *        <td>Creates directory corresponding to specified URL (if it does not exist) and copies all FileObjects into this folder.</td></tr>
     *   <tr><td>Java Refactoring</td><td>{@link java.net.URL}</td><td>Updates name, package declaration and import statements</td></tr>
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
