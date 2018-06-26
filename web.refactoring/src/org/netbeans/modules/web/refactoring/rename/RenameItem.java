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
 * Portions Copyrighted 2007 Sun Microsystems, Inc.
 */
package org.netbeans.modules.web.refactoring.rename;

import org.openide.util.Parameters;
import org.netbeans.modules.refactoring.api.Problem;
import org.netbeans.api.annotations.common.NonNull;

/**
 * Encapsulates the new and old name.
 * TODO: needs a better name
 *
 * @author Erno Mononen
 * @author ads 
 */
class RenameItem {

    private final String oldFqn;
    private final String newFqn;
    
    private final Problem myProblem;

    /**
     * Creates a new RenameItem object with a specific problem 
     * 
     * Its recommended to use this constructor in situations when some problems
     * during RenamingItem occurs. (should be used only when we don't care about
     * old and new renaming item values)
     * 
     * @param problem that occurs during item renaming
     */
    public RenameItem(@NonNull Problem problem) {
        Parameters.notNull("problem", problem);
        
        myProblem = problem;
        oldFqn = "";
        newFqn = "";
    }
    
    /**
     * Creates a new RenameItem.
     * 
     * @param newFqn the new fully qualified name for the object being renamed;
     * must not be empty or null.
     * @param oldFqn the old fully qualified name of the object being renamed;
     * must not be empty or null.
     */
    public RenameItem(@NonNull String newFqn, @NonNull String oldFqn) {
        this(newFqn, oldFqn, null);
    }
    
    public RenameItem(@NonNull String newFqn, @NonNull String oldFqn, Problem problem) {
        Parameters.notEmpty("newFqn", newFqn); //NO18N
        Parameters.notEmpty("oldFqn", oldFqn); //NO18N
        
        this.newFqn = newFqn;
        this.oldFqn = oldFqn;
        myProblem = problem;
    }
    
    public String getNewFqn() {
        return newFqn;
    }
    
    public String getOldFqn() {
        return oldFqn;
    }
    
    public Problem getProblem(){
        return myProblem;
    }
    
}
