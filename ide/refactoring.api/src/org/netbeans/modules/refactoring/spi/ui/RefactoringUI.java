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

import javax.swing.event.ChangeListener;
import org.netbeans.modules.refactoring.api.AbstractRefactoring;
import org.netbeans.modules.refactoring.api.Problem;
import org.openide.util.HelpCtx;

/** Interface representing UI for a refactoring.
 *
 * @author Martin Matula
 */
public interface RefactoringUI {
    /** Returns name of the refactoring.
     * @return Refactoring name.
     */
    String getName();
    
    /** Returns description of the refactoring.
     * @return Refactoring description.
     */
    String getDescription();
    
    /** Indicates whether this class represents a real refactoring that changes
     * code or whether it is just a query (e.g. all usages for a class).
     * @return <code>true</code> if the class represents only a query,
     * <code>false</code> if the class represents a real refactoring.
     */
    boolean isQuery();
    
    /** Returns refactoring-specific panel containing input fields for 
     * refactoring parameters. 
     * Name of the panel returned from this method will be used as the dialog
     * name. 
     * this method might return null if hasParameters return false.
     * @param parent dialog in which that the returned panel will be displayed in.
     * @see #hasParameters
     * @return Refactoring-specific parameters panel.
     */
    CustomRefactoringPanel getPanel(ChangeListener parent);

    /** Implementation of this method should set the refactoring parameters entered
     * by user into the refactoring-specific parameters panel (returned from getPanel
     * method) into the underlying refactoring object.
     * @return Chain of problems returned from the underlying refactoring object
     * when trying to set its parameters.
     */
    Problem setParameters();
    
    /**
     * check parameters of refactoring
     * @return Chain of problems returned from the underlying refactoring object
     * when trying to check its parameters.
     */
    Problem checkParameters();
    
    /**
     * true, if refactoring has parameters
     * false otherwise. In this case {@link #getPanel} method can return null
     * @return false if this UI does not require any parameters. True otherwise.
     */
    boolean hasParameters();
    
    /** Returns underlying refactoring object.
     * @return Underlying refactoring object.
     */
    AbstractRefactoring getRefactoring();
    
    /**
     * @return helpcontext
     */
    public HelpCtx getHelpCtx();
}
