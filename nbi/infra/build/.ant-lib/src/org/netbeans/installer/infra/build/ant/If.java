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

import java.util.LinkedList;
import java.util.List;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;
import org.apache.tools.ant.TaskContainer;

/**
 * This class is an ant task, which adds conditional execution capabilities. It 
 * examines the value of the given property and executed the nested tasks only if 
 * the the property's value equals to the given string.
 * 
 * @author Kirill Sorokin
 */
public class If extends Task implements TaskContainer {
    /////////////////////////////////////////////////////////////////////////////////
    // Instance
    /**
     * Name of the property whose value should be checked.
     */
    private String property;
    
    /**
     * String which should be equal to the property's value in order for the nested 
     * tasks to execute.
     */
    private String value;
    
    /**
     * List of child tasks which should be executed if the condition is satisfied.
     */
    private List<Task> children;
    
    // constructor //////////////////////////////////////////////////////////////////
    /**
     * Constructs a new instance of the {@link If} task. It simply sets the
     * default values for the attributes.
     */
    public If() {
        children = new LinkedList<Task>();
    }
    
    // setters //////////////////////////////////////////////////////////////////////
    /**
     * Setter for the 'property' property.
     * 
     * @param property The new value for the 'property' property.
     */
    public void setProperty(final String property) {
        this.property = property;
    }
    
    /**
     * Setter for the 'value' property.
     * 
     * @param value The new value for the 'value' property.
     */
    public void setValue(final String value) {
        this.value = value;
    }
    
    /**
     * Registers a child task. The supplied <code>Task</code> object will be added 
     * to the list of child tasks and executed if the condition is satisfied.
     * 
     * @param task The <code>Task</code> object to register.
     */
    public void addTask(final Task task) {
        children.add(task);
    }
    
    // execution ////////////////////////////////////////////////////////////////////
    /**
     * Executes the task. If the required value is set, then the property's value is 
     * compared to it and the child tasks are executes if they are equal. If the 
     * required value is not set, then the child tasks are executed if the property
     * was set, without regard to its value.
     * 
     * @throws org.apache.tools.ant.BuildException if a child task fails to execute.
     */
    public void execute() throws BuildException {
        if (getProject().getProperty(property) != null) {
            if (value == null || getProject().getProperty(property).equals(value)) {
                    executeChildren();
            }            
        }
    }
    
    // private //////////////////////////////////////////////////////////////////////
    /**
     * Executes the child tasks.
     */
    private void executeChildren() throws BuildException {
        for (Task task: this.children) {
            task.perform();
        }
    }
}
