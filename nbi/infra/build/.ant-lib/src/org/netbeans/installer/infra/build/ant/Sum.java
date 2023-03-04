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
