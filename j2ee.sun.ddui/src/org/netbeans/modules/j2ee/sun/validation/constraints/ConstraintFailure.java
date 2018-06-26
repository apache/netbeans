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

import org.netbeans.modules.j2ee.sun.validation.Failure;

/**
 * ConstraintFailure is a Validation failure Object.
 * It provides the following failure information; Constraint failed,
 * the value it failed for; the name of the value it failed for,
 * failure message and the generic failure message.
 *
 * @author  Rajeshwar Patil
 * @version %I%, %G%
 */
public class ConstraintFailure implements Failure{

    /**
     * The name of the failed <code>Constraint</code>.
     */
    private String constraint = null;

    /**
     * The name of the value, the <code>Constraint</code> failed for.
     */
    private String name = null;

    /**
     * The value,  the <code>Constraint</code> failed for.
     */
    private Object value = null;

    /**
     * The failure message.
     */
    private String failureMessage = null;


    /**
     * The generic failure message.
     */
    private String genericFailureMessage = null;


    /** Creates a new instance of ConstraintFailure */
    public ConstraintFailure(String constraint,
        Object value, String name, String failureMessage, 
                String genericFailureMessage) {
            this.constraint =  constraint;
            this.value = value;
            this.failureMessage = failureMessage;
            this.name = name;
            this.genericFailureMessage = genericFailureMessage;
    }


    /**
     * Returns the failed <code>Constraint</code> this Object represents.
     */
    public String getConstraint(){
        return constraint;
    }


    /**
     * Returns the value failed for the <code>Constraint</code>
     * represented by this Object.
     */
    public Object getFailedValue(){
        return value;
    }


    /**
     * Returns an failure message for this failure.
     */
    public String failureMessage(){
        return failureMessage;
    }


    /**
     * Returns the name of the value failed for the 
     * <code>Constraint</code> represented by this Object.
     */
    public String getName(){
        return name;
    }


    /**
     * Returns generic message for this failure.
     */
    public String getGenericfailureMessage(){
        return genericFailureMessage;
    }
}
