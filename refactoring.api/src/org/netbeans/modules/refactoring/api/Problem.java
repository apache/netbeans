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
import org.openide.util.Parameters;

/** Class used to represent problems encountered when performing
 * various refactoring calls. Problems can be chained (using setNext method)
 * - every problem can point to the following problem.
 *
 * @author Martin Matula
 */
public final class Problem {
    private final boolean fatal;
    private final String message;
    private Problem next = null;
    private ProblemDetails details;

    /** Creates new instance of Problem class.
     * @param fatal Indicates whether the problem is fatal.
     * @param message Textual description of the problem.
     */
    public Problem(boolean fatal, @NonNull String message) {
        this(fatal, message, null);
    }
    
    /** Creates new instance of Problem class.
     * @param fatal Indicates whether the problem is fatal.
     * @param message Textual description of the problem.
     * @param details Problem details
     * @see ProblemDetails
     */
    public Problem(boolean fatal, @NonNull String message, ProblemDetails details) {
        Parameters.notNull("message", message); // NOI18N
        this.fatal = fatal;
        this.message = message;
        this.details = details;
    }
    
    /** Indicates whether the problem is fatal.
     * @return <code>true</code> if the problem is fatal, otherwise returns <code>false</code>.
     */
    public boolean isFatal() {
        return fatal;
    }
    
    /** Returns textual description of the problem.
     * @return Textual description of the problem.
     */
    @NonNull
    public String getMessage() {
        return message;
    }
    
    /** Returns the following problem (or <code>null</code> if there none).
     * @return The following problem.
     */
    @CheckForNull
    public Problem getNext() {
        return next;
    }
    
    /**
     * Sets the following problem. The problem can be set only once - subsequent
     * attempts to call this method will result in IllegalStateException.
     * @param next The following problem.
     * @throws java.lang.IllegalStateException subsequent attempts to call this method will result in IllegalStateException.
     */
    public void setNext(@NonNull Problem next) throws IllegalStateException {
        Parameters.notNull("next", next); // NOI18N
        if (this.next != null) {
            throw new IllegalStateException("Cannot change \"next\" property of Problem."); //NOI18N
        }
        this.next = next;
    }

    /**
     * Getter for ProblemDetails
     * @return instance of ProblemDetails or null
     */
    @CheckForNull
    public ProblemDetails getDetails() {
        return details;
    }
}
