/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.netbeans.modules.java.api.common.ant;

import java.io.IOException;
import org.openide.util.Exceptions;
import org.w3c.dom.Element;
import org.openide.util.Mutex;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.netbeans.spi.project.support.ant.EditableProperties;
import org.openide.util.MutexException;
import org.openide.util.Parameters;


/**
 * Proxy for the {@link AntProjectHelper} which defers the update of the project metadata
 * to explicit user action. Caller has to provide implementation of {@link UpdateImplementation}
 * which takes care of updating project itself.
 * @author Tomas Zezula, Tomas Mysik
 * @see UpdateImplementation
 */
public final class UpdateHelper {

    private final UpdateImplementation updateProject;
    private final AntProjectHelper helper;

    /**
     * Create new {@link UpdateHelper}.
     * @param updateProject {@link UpdateImplementation} which takes care of updating project itself.
     * @param helper {@link AntProjectHelper} to be proxied.
     */
    public UpdateHelper(UpdateImplementation updateProject, AntProjectHelper helper) {
        Parameters.notNull("updateProject", updateProject); // NOI18N
        Parameters.notNull("helper", helper); // NOI18N

        this.updateProject = updateProject;
        this.helper = helper;
    }

    /**
     * In the case that the project is of current version or the properties
     * are not {@link AntProjectHelper#PROJECT_PROPERTIES_PATH} it calls
     * {@link AntProjectHelper#getProperties(String)} otherwise it asks for updated project properties.
     * @param path a relative URI in the project directory.
     * @return a set of properties.
     */
    public EditableProperties getProperties(final String path) {
        return ProjectManager.mutex().readAccess(new Mutex.Action<EditableProperties>() {
            public EditableProperties run() {
                if (!isCurrent() && AntProjectHelper.PROJECT_PROPERTIES_PATH.equals(path)) {
                    // only project properties were changed
                    return updateProject.getUpdatedProjectProperties();
                }
                return helper.getProperties(path);
            }
        });
    }

    /**
     * In the case that the project is of current version or the properties
     * are not {@link AntProjectHelper#PROJECT_PROPERTIES_PATH} it calls
     * {@link AntProjectHelper#putProperties(String, EditableProperties)} otherwise it asks to update project.
     * If the project can be updated, it does the update and calls
     * {@link AntProjectHelper#putProperties(String, EditableProperties)}.
     * @param path a relative URI in the project directory.
     * @param props a set of properties.
     */
    public void putProperties(final String path, final EditableProperties props) {
        ProjectManager.mutex().writeAccess(
            new Runnable() {
                public void run() {
                    if (isCurrent() || !AntProjectHelper.PROJECT_PROPERTIES_PATH.equals(path)) {
                        // only project props should cause update
                        helper.putProperties(path, props);
                    } else if (updateProject.canUpdate()) {
                        try {
                            updateProject.saveUpdate(props);
                            helper.putProperties(path, props);
                        } catch (IOException ioe) {
                            Exceptions.printStackTrace(ioe);
                        }
                    }
                }
            });
    }

    /**
     * In the case that the project is of current version or shared is <code>false</code> it delegates to
     * {@link AntProjectHelper#getPrimaryConfigurationData(boolean)}.
     * Otherwise it creates an in memory update of shared configuration data and returns it.
     * @param shared if <code>true</code>, refers to <code>project.xml</code>, else refers to
     *               <code>private.xml</code>.
     * @return the configuration data that is available.
     */
    public Element getPrimaryConfigurationData(final boolean shared) {
        return ProjectManager.mutex().readAccess(new Mutex.Action<Element>() {
            public Element run() {
                if (!shared || isCurrent()) { // only shared props should cause update
                    return helper.getPrimaryConfigurationData(shared);
                }
                return updateProject.getUpdatedSharedConfigurationData();
            }
        });
    }

    /**
     * In the case that the project is of current version or shared is <code>false</code> it calls
     * {@link AntProjectHelper#putPrimaryConfigurationData(Element, boolean)}.
     * Otherwise the project can be updated, it does the update and calls
     * {@link AntProjectHelper#putPrimaryConfigurationData(Element, boolean)}.
     * @param element the configuration data
     * @param shared if true, refers to <code>project.xml</code>, else refers to
     * <code>private.xml</code>
     */
    public void putPrimaryConfigurationData(final Element element, final boolean shared) {
        ProjectManager.mutex().writeAccess(new Runnable() {
            public void run () {
                if (!shared || isCurrent()) {
                    helper.putPrimaryConfigurationData(element, shared);
                } else if (updateProject.canUpdate()) {
                    try {
                        updateProject.saveUpdate(null);
                        helper.putPrimaryConfigurationData(element, shared);
                    } catch (IOException ioe) {
                        Exceptions.printStackTrace(ioe);
                    }
                }
            }
        });
    }

    /**
     * Request saving of update. If the project is not of current version and the project can be updated, then
     * the update is done.
     * @return <code>true</code> if the metadata are of current version or updated.
     * @throws IOException if error occurs during saving.
     */
    public boolean requestUpdate() throws IOException {
        if (isCurrent()) {
            return true;
        }
        if (!updateProject.canUpdate()) {
            return false;
        }
        try {
            return ProjectManager.mutex().writeAccess(new Mutex.ExceptionAction<Boolean>() {
                public Boolean run() throws IOException {                                        
                    if (!isCurrent()) {
                        updateProject.saveUpdate(null);
                    }
                    return true;
                }
            });

        } catch (MutexException ex) {
            Exception inner = ex.getException();
            if (inner instanceof IOException) {
                throw (IOException) inner;
            }
            throw (RuntimeException) inner;
        }
    }

    /**
     * Return <code>true</code> if the project is of current version.
     * @return <code>true</code> if the project is of current version.
     */
    public boolean isCurrent() {
        return ProjectManager.mutex().readAccess(new Mutex.Action<Boolean>() {
            public Boolean run() {
                return updateProject.isCurrent();
            }
        });
    }

    /**
     * Get the {@link AntProjectHelper} that is proxied.
     * @return the {@link AntProjectHelper} that is proxied.
     */
    public AntProjectHelper getAntProjectHelper() {
        return helper;
    }
}
