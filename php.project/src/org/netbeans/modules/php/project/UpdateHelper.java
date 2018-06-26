/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */

package org.netbeans.modules.php.project;

import java.io.IOException;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.netbeans.spi.project.support.ant.EditableProperties;
import org.openide.util.Exceptions;
import org.openide.util.Mutex;
import org.openide.util.MutexException;
import org.w3c.dom.Element;

/**
 * Helper class to support migration from
 * older version of ant based python project
 * to the newer version of ant base python project.
 * Currently there is no older version of ant based
 * python project, so the class has no upgrade logic.
 * Based on the UpdateHelper (java.common.api)
 * @author Tomas Zezula, Tomas Mysik
 */
public final class UpdateHelper {


    private final UpdateImplementation updateProject;
    private final AntProjectHelper helper;

    public UpdateHelper(final UpdateImplementation update, final AntProjectHelper helper) {
        assert update != null;
        assert helper != null;
        this.updateProject = update;
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
            @Override
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
            @Override
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
     * @param shared if <code>true</code>, refers to <tt>project.xml</tt>, else refers to
     *               <tt>private.xml</tt>.
     * @return the configuration data that is available.
     */
    public Element getPrimaryConfigurationData(final boolean shared) {
        return ProjectManager.mutex().readAccess(new Mutex.Action<Element>() {
            @Override
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
            @Override
            public void run() {
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
        try {
            return ProjectManager.mutex().writeAccess(new Mutex.ExceptionAction<Boolean>() {
                @Override
                public Boolean run() throws IOException {
                    if (isCurrent()) {
                        return true;
                    }
                    if (!updateProject.canUpdate()) {
                        return false;
                    }
                    updateProject.saveUpdate(null);
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
        return updateProject.isCurrent();
    }

    /**
     * Get the {@link AntProjectHelper} that is proxied.
     * @return the {@link AntProjectHelper} that is proxied.
     */
    public AntProjectHelper getAntProjectHelper() {
        return helper;
    }

}
