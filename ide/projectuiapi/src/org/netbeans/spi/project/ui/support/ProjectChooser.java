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

package org.netbeans.spi.project.ui.support;

import java.io.File;
import javax.swing.JFileChooser;
import org.netbeans.modules.project.uiapi.Utilities;

/**
 * Support for creating project chooser.
 * @author Petr Hrebejk
 */
public class ProjectChooser {

    private ProjectChooser() {}


    /**
     * Returns the folder last used for creating a new project.
     * @return File the folder, never returns null. In the case
     * when the projects folder was not set the home folder is returned.
     */
    public static File getProjectsFolder () {
        return Utilities.getProjectChooserFactory().getProjectsFolder();
    }

    /**
     * Sets the folder last used for creating a new project.
     * @param folder The folder to be set as last used. Must not be null
     * @throws IllegalArgumentException if folder parameter is null or not a directory.
     */
    public static void setProjectsFolder (File folder) {
        if (folder == null || !folder.isDirectory()) {
            throw new IllegalArgumentException("Parameter must be a valid folder."); //NOI18N
        }
        Utilities.getProjectChooserFactory().setProjectsFolder(folder);
    }

    /**
     * Creates a project chooser.
     * @return New instance of JFileChooser which is able to select
     *         project directories.
     */
    public static JFileChooser projectChooser() {
        return Utilities.getProjectChooserFactory().createProjectChooser();
    }
        
}
