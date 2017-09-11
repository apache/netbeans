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
package org.netbeans.modules.refactoring.java.api;

import org.netbeans.api.java.source.TreePathHandle;
import org.netbeans.modules.refactoring.api.AbstractRefactoring;
import org.openide.util.lookup.Lookups;

/** 
 * Convert Inner to Top-Level refactoring implementation class. This refactoring
 * is capable of converting an inner class into a top-level class.
 * 
 * @see org.netbeans.modules.refactoring.spi.RefactoringPlugin
 * @see org.netbeans.modules.refactoring.spi.RefactoringPluginFactory
 * @see org.netbeans.modules.refactoring.api.AbstractRefactoring
 * @see org.netbeans.modules.refactoring.api.RefactoringSession
 *
 * @author Martin Matula
 * @author Jan Becicka
 */
public final class InnerToOuterRefactoring extends AbstractRefactoring {

    // parameters of the refactoring
    private String className;
    private String referenceName;
    
    /**
     * Creates a new instance of InnerToOuterRefactoring.
     * 
     * @param sourceType An inner class that should be converted to a top-level class.
     */
    public InnerToOuterRefactoring(TreePathHandle sourceType) {
        super(Lookups.singleton(sourceType));
    }
    
    /** Returns the type the members of which should be pulled up
     * by this refactoring.
     * @return Source of the members to be pulled up.
     */
    public TreePathHandle getSourceType() {
        return getRefactoringSource().lookup(TreePathHandle.class);
    }

    // --- PARAMETERS ----------------------------------------------------------
    
    /** Returns the name for the top-level class to be created.
     * @return Class name.
     */
    public String getClassName() {
        return className;
    }

    /** Sets name for the top-level class to be created.
     * @param className Class name.
     */
    public void setClassName(String className) {
        this.className = className;
    }

    /** Returns name of the field that should be generated as a reference to the original
     * outer class. If null, no field will be generated.
     * @return Name of the field to be generated or null if no field will be generated.
     */
    public String getReferenceName() {
        return referenceName;
    }
    
    /** Sets name of the field that should be generated as a reference to the original
     * outer class. Can be set to null which indicates that no field should be generated.
     * @param referenceName Name of the field or null if no field should be generated.
     */ 
    public void setReferenceName(String referenceName) {
        this.referenceName = referenceName;
    }
}
