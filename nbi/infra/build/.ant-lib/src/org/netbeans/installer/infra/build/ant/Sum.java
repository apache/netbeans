/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU General
 * Public License Version 2 only ("GPL") or the Common Development and Distribution
 * License("CDDL") (collectively, the "License"). You may not use this file except in
 * compliance with the License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html or nbbuild/licenses/CDDL-GPL-2-CP. See the
 * License for the specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header Notice in
 * each file and include the License file at nbbuild/licenses/CDDL-GPL-2-CP.  Oracle
 * designates this particular file as subject to the "Classpath" exception as
 * provided by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the License Header,
 * with the fields enclosed by brackets [] replaced by your own identifying
 * information: "Portions Copyrighted [year] [name of copyright owner]"
 * 
 * Contributor(s):
 * 
 * The Original Software is NetBeans. The Initial Developer of the Original Software
 * is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun Microsystems, Inc. All
 * Rights Reserved.
 * 
 * If you wish your version of this file to be governed by only the CDDL or only the
 * GPL Version 2, indicate your decision by adding "[Contributor] elects to include
 * this software in this distribution under the [CDDL or GPL Version 2] license." If
 * you do not indicate a single choice of license, a recipient has the option to
 * distribute your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above. However, if
 * you add GPL Version 2 code and therefore, elected the GPL Version 2 license, then
 * the option applies only if the new code is made subject to such option by the
 * copyright holder.
 */

package org.netbeans.installer.infra.build.ant;

import org.apache.tools.ant.Task;

/**
 * This class is an ant task which is capable of summing two integer values and 
 * storing the result as the value of a property.
 * 
 * @author Kirill Sorokin
 */
public class Sum extends Task {
    /////////////////////////////////////////////////////////////////////////////////
    // Instance
    /**
     * Name of the first item.
     */
    private String arg1;
    
    /**
     * Name of the second item.
     */
    private String arg2;
    
    /**
     * Name of the property shich should hold the result.
     */
    private String property;
    
    // setters //////////////////////////////////////////////////////////////////////
    /**
     * Setter for the 'arg1' property.
     * 
     * @param arg1 New value for the 'arg1' property.
     */
    public void setArg1(final String arg1) {
        this.arg1 = arg1;
    }
    
    /**
     * Setter for the 'arg2' property.
     * 
     * @param arg2 New value for the 'arg2' property.
     */
    public void setArg2(final String arg2) {
        this.arg2 = arg2;
    }
    
    /**
     * Setter for the 'property' property.
     * 
     * @param property New value for the 'property' property.
     */
    public void setProperty(final String property) {
        this.property = property;
    }
    
    // execution ////////////////////////////////////////////////////////////////////
    /**
     * Executes the task.
     */
    public void execute() {
        getProject().setProperty(
                property, 
                Long.toString(Long.parseLong(arg1) + Long.parseLong(arg2)));
    }
}
