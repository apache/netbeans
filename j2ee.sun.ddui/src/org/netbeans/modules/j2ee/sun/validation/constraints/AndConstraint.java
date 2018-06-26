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

package org.netbeans.modules.j2ee.sun.validation.constraints;

import java.util.ArrayList;
import java.util.Collection;

/**
 * AndConstraint  is a logical <code>And</code> {@link Constraint}. It validates
 * the given value by performing logical <code>And</code> of validations of the
 * value against its constituent <code>leftConstraint</code> and
 * <code>rightConstraint</code>.
 * <p>
 * It implements <code>Constraint</code> interface and extends
 * {@link ConstraintUtils} class.
 * <code>match</code> method of this object returns empty collection if the
 * value being validated satisfies both the <code>leftConstraint</code> and the
 * <code>rightConstraint</code>; else it returns a <code>Collection</code>
 * with {@link ConstraintFailure} objects in it for the failed
 * <code>Constraints</code>.
 * <code>ConstraintUtils</code> class provides utility methods for formating 
 * failure messages and a <code>print<method> method to print this object.
 *  
 * @author  Rajeshwar Patil
 * @version %I%, %G%
 */
public class AndConstraint extends ConstraintUtils implements Constraint {

    /**
     * The left <code>Constraint</code> of this <code>AndCosntraint</code>
     * This <code>AndConstraint</code> is logical <code>AND</code> of its left 
     * <code>Constraint</code> and right <code>Constraint</code>.
     */
    private Constraint leftConstraint = null;


    /**
     * The right <code>Constraint</code> of this <code>AndCosntraint</code>
     * This <code>AndConstraint</code> is logical <code>AND</code> of its left 
     * <code>Constraint</code> and right <code>Constraint</code>.
     */
    private Constraint rightConstraint = null;


    /** Creates a new instance of <code>AndConstraint</code> */
    public AndConstraint() {
        Constraint leftConstraint = null;
        Constraint rightConstraint = null;
    }


    /** Creates a new instance of <code>AndConstraint</code> */
    public AndConstraint(Constraint leftConstraint,
            Constraint rightConstraint){
        this.leftConstraint = leftConstraint;
        this.rightConstraint = rightConstraint;
    }


    /**
     * Validates the given value against this <code>Constraint</code>.
     * 
     * @param value the value to be validated
     * @param name the element name, value of which is being validated.
     * It is used only in case of <code>Constraint</code> failure, to construct
     * the failure message.
     *
     * @return <code>Collection</code> the Collection of
     * <code>ConstraintFailure</code> Objects. Collection is empty if 
     * there are no failures.
     */
    public Collection match(String value, String name) {
        Collection failed_constrained_list = new ArrayList();
        failed_constrained_list.addAll(leftConstraint.match(value, name));
        failed_constrained_list.addAll(rightConstraint.match(value, name));
        return failed_constrained_list;
    }


    /**
     * Sets the given <code>Constraint</code> as the
     * <code>leftConstraint</code> of this object.
     * 
     * @param constraint the <code>Constraint</code> to be
     * set as <code>leftConstraint</code> of this object
     */
    public void setLeftConstraint(Constraint constraint){
        leftConstraint = constraint;
    }


    /**
     * Sets the given <code>Constraint</code> as
     * the <code>rightConstraint</code> of this object.
     * 
     * @param constraint the <code>Constraint</code> to be
     * set as <code>rightConstraint</code> of this object
     */
    public void setRightConstraint(Constraint constraint){
        rightConstraint = constraint;
    }


    /**
     * Prints this <code>Constraint</code>.
     */
    public void print() {
        super.print();
        ((ConstraintUtils)leftConstraint).print();
        ((ConstraintUtils)rightConstraint).print();
    }
}
