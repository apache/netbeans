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

import java.io.File;
import org.apache.tools.ant.Task;

/**
 * This class is an ant task which absolutizes the path contained in a given 
 * property with regard to the ant project's basedir.
 * 
 * @author Kirill Sorokin
 */
public class Absolutize extends Task {
    /////////////////////////////////////////////////////////////////////////////////
    // Instance
    /**
     * Name of the property whose value should be corrected.
     */
    private String property  = null;
    
    // setters //////////////////////////////////////////////////////////////////////
    /**
     * Setter for the 'property' property.
     * 
     * @param property The new value for the 'property' property.
     */
    public void setProperty(final String property) {
        this.property = property;
    }
    
    // execution ////////////////////////////////////////////////////////////////////
    /**
     * Executes the task. A <code>File</code> object is constructed from the 
     * property value and is then compared to its absolute variant. If they differ 
     * the absolute path is put back to the property.
     */
    public void execute() {
        final String value = getProject().getProperty(property);
        final File file = new File(value);
        
        if (!file.equals(file.getAbsoluteFile())) {
            getProject().setProperty(
                    property, 
                    new File(getProject().getBaseDir(), value).getAbsolutePath());
        }
    }
}
