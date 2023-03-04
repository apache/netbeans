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

package org.netbeans.modules.refactoring.java.api;

import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.annotations.common.NullAllowed;
import org.netbeans.api.java.source.TreePathHandle;
import org.netbeans.modules.refactoring.api.AbstractRefactoring;
import org.openide.util.lookup.Lookups;

/**
 * Invert Boolean Refactoring.
 * Invert logic of boolean field or method.
 * @author lahvac
 * @author Jan Becicka
 * @since 1.36
 */
public final class InvertBooleanRefactoring extends AbstractRefactoring {
    
    private String newName;

    /**
     * Constructor for refactoring accepts TreePathHandles representing field or
     * method.
     * @param handle 
     */
    public InvertBooleanRefactoring(@NonNull TreePathHandle handle) {
        super(Lookups.singleton(handle));
    }

    /**
     * Getter for new name of inverted method of field.
     * @return name of field or method. Can return null, if rename is not 
     * requested.
     */
    @NonNull
    public String getNewName() {
        return newName;
    }

    /**
     * Setter for new name of method or field. Null if rename is not requested.
     * @param newName name of method or field
     */
    public void setNewName(@NullAllowed String newName) {
        this.newName = newName;
    }
}
