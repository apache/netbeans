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

package org.netbeans.modules.project.ui.api;

import java.net.URL;
import javax.swing.Icon;

import org.netbeans.modules.project.ui.ProjectInfoAccessor;

/**
 * Lite version of information about project.
 * @author Milan Kubec
 * @since 1.9.0
 */
public final class UnloadedProjectInformation {

    private String displayName;
    private Icon icon;
    private URL url;

    static {
        ProjectInfoAccessor.DEFAULT = new ProjectInfoAccessorImpl();
    }

    /**
     * Creates a new instance of UnloadedProjectInformation
     */
    UnloadedProjectInformation(String displayName, Icon icon, URL url) {
        this.displayName = displayName;
        this.icon = icon;
        this.url = url;
    }
    
    /**
     * Gets a human-readable display name for the project.
     * May contain spaces, international characters, etc.
     * @return a display name for the project
     */
    public String getDisplayName() {
        return displayName;
    }
    
    /**
     * Gets icon for given project.
     * Usually determined by the project type.
     * @return icon of the project.
     */
    public Icon getIcon() {
        return icon;
    }
    
    /**
     * Gets URL of the project folder location
     * Use {@link ProjectManager#findProject} to get the project
     * @return url of the project folder
     */
    public URL getURL() {
        return url;
    }
    
    public @Override String toString() {
        return url.toString();
    }

}
