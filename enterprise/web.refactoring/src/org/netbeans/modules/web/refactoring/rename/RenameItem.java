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
