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
 * This class is an ant task which adds the for-loop functionality. It is an
 * arbitrary tasks container, which is capable of executing its childrne ina loop.
 *
 * <p>
 * Two types of loops are supported. First, iterating of over a list of values; in
 * this case the <code>list</code> and <code>separator</code> (optional) attributes
 * should be set and the property speficied with <code>property</code> attribute
 * will be set to each of the value speficied in the the list.
 *
 * <p>
 * Second, iterating of a range of integers. In this case the <code>from</code>,
 * <code>to</code> and <code>increment</code> (optional) attributes should be set,
 * meaning the starting point, the finish point and the step. Note that if one
 * specifies a negative step, the iteration could go backwards.
 *
 * <p>
 * If a mixture of attributes is supplied, iterating over a list is preferred.
 *
 * @author Kirill Sorokin
 */
public class ForEach extends Task implements TaskContainer {
    /////////////////////////////////////////////////////////////////////////////////
    // Instance
    /**
     * The list of values over which the iteration process should go.
     */
    private String list;
    
    /**
     * Separator token which should be used to split the list string into individual
     * values.
     */
    private String separator;
    
    /**
     * Starting point for iteration over an integer range.
     */
    private int from;
    
    /**
     * Ending point for iteration over an integer range.
     */
    private int to;
    
    /**
     * Step amount for iteration over an integer range.
     */
    private int increment;
    
    /**
     * Name of the property which has to be set for each iteration.
     */
    private String property;
    
    /**
     * List of child tasks which should be executed at each iteration.
     */
    private List<Task> children;
    
    /**
     * Flag which marks incorrect attribute values.
     */
    private boolean wrongArgs;
    
    // constructor //////////////////////////////////////////////////////////////////
    /**
     * Constructs a new instance of the {@link ForEach} task. It simply sets the
     * default values for the attributes.
     */
    public ForEach() {
        separator = DEFAULT_SEPARATOR;
        
        from = DEFAULT_FROM;
        to = DEFAULT_TO;
        increment = DEFAULT_INCREMENT;
        
        children = new LinkedList<Task>();
    }
    
    // setters //////////////////////////////////////////////////////////////////////
    /**
     * Setter for the 'list' property.
     * 
     * @param list The new value for the 'list' property.
     */
    public void setList(final String list) {
        this.list = list;
    }
    
    /**
     * Setter for the 'separator' property.
     * 
     * @param separator The new value for the 'separator' property.
     */
    public void setSeparator(final String separator) {
        this.separator = separator;
    }
    
    /**
     * Setter for the 'property' property.
     * 
     * @param property The new value for the 'property' property.
     */
    public void setProperty(final String property) {
        this.property = property;
    }
    
    /**
     * Setter for the 'from' property.
     * 
     * @param from The new value for the 'from' property.
     */
    public void setFrom(final String from) {
        try {
            this.from = Integer.parseInt(from);
        } catch (NumberFormatException e) {
            log("Wrong value for parameter 'from' : " + from); // NOI18N
            wrongArgs = true;
        }
    }
    
    /**
     * Setter for the 'to' property.
     * 
     * @param to The new value for the 'to' property.
     */
    public void setTo(final String to) {
        try {
            this.to = Integer.parseInt(to);
        } catch (NumberFormatException e) {
            log("Wrong value for parameter 'to' : " + to); // NOI18N
            wrongArgs = true;
        }
    }
    
    /**
     * Setter for the 'increment' property.
     * 
     * @param increment The new value for the 'increment' property.
     */
    public void setIncrement(final String increment) {
        try {
            this.increment = Integer.parseInt(increment);
        } catch (NumberFormatException e) {
            log("Wrong value for parameter 'increment' : " + increment); // NOI18N
            wrongArgs = true;
        }
    }
    
    /**
     * Registers a child task. The supplied <code>Task</code> object will be added 
     * to the list of child tasks and then executed at each iteration.
     * 
     * @param task The <code>Task</code> object to register.
     */
    public void addTask(final Task task) {
        children.add(task);
    }
    
    // execution ////////////////////////////////////////////////////////////////////
    /**
     * Executes the task. Basing on which attributes were set, the choice is made of
     * whether the iteration should happen over a list or an integer range. Then the
     * iteration takes place the specified property is set and the child tasks are 
     * executed.
     * 
     * @throws org.apache.tools.ant.BuildException if a child task fails to execute.
     */
    public void execute() throws BuildException {
        if (wrongArgs) {
            throw new BuildException(
                    "Correct parameters were not supplied."); // NOI18N
        }
        
        if (list != null) {
            final String[] items = list.split(separator);
            
            for (String value: items) {
                executeChildren(value);
            }
            return;
        } else {
            for (int i = from; i <= to; i += increment) {
                executeChildren(Integer.toString(i));
            }
        }
    }
    
    // private //////////////////////////////////////////////////////////////////////
    /**
     * Sets the specified property to the given value and executes the children.
     */
    private void executeChildren(String value) throws BuildException {
        getProject().setProperty(this.property, value);
        
        for (Task task: this.children) {
            task.perform();
        }
    }
    
    /////////////////////////////////////////////////////////////////////////////////
    // Constants
    /**
     * Default value for the 'separator' property.
     */
    private static final String DEFAULT_SEPARATOR =
            " "; // NOI18N
    
    /**
     * Default value for the 'from' property.
     */
    private static final int DEFAULT_FROM =
            0;                                                              // NOMAGI
    
    /**
     * Default value for the 'to' property.
     */
    private static final int DEFAULT_TO =
            0;                                                              // NOMAGI
    
    /**
     * Default value for the 'increment' property.
     */
    private static final int DEFAULT_INCREMENT =
            1;                                                              // NOMAGI
}
