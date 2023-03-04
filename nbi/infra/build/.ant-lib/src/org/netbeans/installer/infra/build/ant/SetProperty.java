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

import org.apache.tools.ant.Project;
import org.apache.tools.ant.Task;
import org.netbeans.installer.infra.build.ant.utils.Utils;

/**
 * This class is an ant task which is capable of setting a property value basing on
 * either a supplied value or a value of another property.
 *
 * @author Kirill Sorokin
 */
public class SetProperty extends Task {     
    /////////////////////////////////////////////////////////////////////////////////
    // Instance
    /**
     * Name of the target property whose value should be set.
     */
    private String property;
    
    /**
     * Name of the source property whose value should be evaluated and set as the
     * value of the target property.
     */
    private String source;
    
    /**
     * String which should be set as the value for the target property.
     */
    private String value;
    
    // setters //////////////////////////////////////////////////////////////////////
    /**
     * Setter for the 'property' property.
     *
     * @param property New value for the 'property' property.
     */
    public void setProperty(final String property) {
        this.property = property;
    }
    
    /**
     * Setter for the 'source' property.
     *
     * @param source New value for the 'source' property.
     */
    public void setSource(final String source) {
        this.source = source;
    }
    
    /**
     * Setter for the 'value' property.
     *
     * @param value New value for the 'value' property.
     */
    public void setValue(final String value) {
        this.value = value;
    }
    
    // execution ////////////////////////////////////////////////////////////////////
    /**
     * Executes the task. If the source property was specified, its value is
     * evaluated and set as the value of the target property. Otherwise the literal
     * string value is used.
     */
    public void execute() {        
        final Project project = getProject();
        final String string = (source != null) ? 
            project.getProperty(Utils.resolveProperty(source, project)) : 
            value;
        final String resolved = Utils.resolveProperty(string, project);
        log("Setting " + property + " to " + resolved);
        project.setProperty(property, resolved);
    }
    
    
}
