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
