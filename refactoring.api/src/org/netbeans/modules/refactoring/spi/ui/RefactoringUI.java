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
