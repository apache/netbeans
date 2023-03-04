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
