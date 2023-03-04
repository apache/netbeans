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
import java.net.URI;
import java.net.URISyntaxException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;
import org.netbeans.installer.infra.build.ant.utils.Utils;

/**
 * Thsi class is an ant task which converts an URI to a relative path. This is
 * useful for caching the downloads.
 *
 * @author Kirill Sorokin
 */
public class UriToPath extends Task {
    /////////////////////////////////////////////////////////////////////////////////
    // Instance
    /**
     * URI which should be converted.
     */
    private String uriString;
    
    /**
     * Name of the property whose value should contain the size.
     */
    private String property;
    
    // setters //////////////////////////////////////////////////////////////////////
    /**
     * Setter for the 'uri' property.
     *
     * @param uri New value for the 'uri' property.
     */
    public void setUri(final String uri) {
        this.uriString = uri;
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
    public void execute() throws BuildException {
        try {
            final URI uri = new URI(uriString);
            
            String path = uri.getSchemeSpecificPart();
            while (path.startsWith("/")) { // NOI18N
                path = path.substring(1);
            }
            
            if (uri.getScheme().equals("file")) {
                path = "local/" + path;
            }
            path = path.replace(":", "_").replace("*", "_");
            getProject().setProperty(property, path);
        } catch (URISyntaxException e) {
            throw new BuildException("Cannot parse URI.",e); // NOI18N
        }
    }
}
