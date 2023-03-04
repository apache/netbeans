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

package org.netbeans.modules.refactoring.spi.ui;

import java.io.IOException;

/**
 * This was historicaly intended to enhance RefactoringUI.
 * RefactoringUI must support "bypass" of refactoring for common 
 * operations Copy/Move/Rename for users to be able to do 
 * regular operation (Copy/Move/Rename) instead of refactoring operation
 * (Refactor | Copy, Refactor | Move, Refactor | Rename)
 * 
 * For instance UI for Java Rename Refactoring has checkbox 
 * [ ] Rename Without Refactoring
 *  
 * isRefactoringBypassRequired() should return true if and only if
 * this checkbox is checked.
 * doRefactoringBypass() implementation does only regular file rename
 *
 * @author Jan Becicka
 * 
 */
public interface RefactoringUIBypass {
    
    /**
     * @return true if user want to bypass refactoring
     */
    boolean isRefactoringBypassRequired();
    
    /**
     * do regular operation, bypass refactoring
     */
    void doRefactoringBypass() throws IOException;
}
