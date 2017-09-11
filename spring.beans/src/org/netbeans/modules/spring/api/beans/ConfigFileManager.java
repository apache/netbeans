/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
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
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
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
 */

package org.netbeans.modules.spring.api.beans;

import java.io.File;
import java.io.IOException;
import java.util.List;
import javax.swing.event.ChangeListener;
import org.netbeans.modules.spring.beans.ConfigFileManagerAccessor;
import org.netbeans.modules.spring.beans.ConfigFileManagerImplementation;
import org.openide.util.Mutex;
import org.openide.util.Parameters;

/**
 * Manages all config file groups in a {@link SpringScope Spring scope}.
 *
 * @author Andrei Badea
 */
public final class ConfigFileManager {

    private final ConfigFileManagerImplementation impl;

    static {
        ConfigFileManagerAccessor.setDefault(new ConfigFileManagerAccessor() {
            @Override
            public ConfigFileManager createConfigFileManager(ConfigFileManagerImplementation impl) {
                return new ConfigFileManager(impl);
            }
        });
    }

    private ConfigFileManager(ConfigFileManagerImplementation impl) {
        this.impl = impl;
    }

    /**
     * Returns the mutex which protectes the access to this ConfigFileManager.
     *
     * @return the mutex; never null.
     */
    public Mutex mutex() {
        return impl.mutex();
    }

    /**
     * Returns the list of config files in this manager. The list is
     * modifiable and not live, therefore changes to the list do not
     * modify the contents of the manager.
     *
     * @return the list; never null.
     */
    public List<File> getConfigFiles() {
        return impl.getConfigFiles();
    }

    /**
     * Returns the list of config file groups in this manger. The list is
     * modifiable and not live, therefore changes to the list do not
     * modify the contents of the manager.
     *
     * @return the list; never null.
     */
    public List<ConfigFileGroup> getConfigFileGroups() {
        return impl.getConfigFileGroups();
    }

    /**
     * Modifies the list of config file groups. This method needs to be called
     * under {@code mutex()} write access.
     *
     * @param  files the files to add; never null.
     * @param  groups the groups to add; never null.
     * @throws IllegalStateException if the called does not hold {@code mutex()}
     *         write access.
     */
    public void putConfigFilesAndGroups(List<File> files, List<ConfigFileGroup> groups) {
        Parameters.notNull("files", files);
        Parameters.notNull("groups", groups);
        if (!mutex().isWriteAccess()) {
            throw new IllegalStateException("The putConfigFilesAndGroups() method should be called under mutex() write access");
        }
        impl.putConfigFilesAndGroups(files, groups);
    }

    /**
     * Saves the list of config file groups, for example to a persistent storage.
     * This method needs to be called under {@code mutex()} write access.
     *
     * @throws IOException if an error occured.
     */
    public void save() throws IOException {
        if (!mutex().isWriteAccess()) {
            throw new IllegalStateException("The save() method should be called under mutex() write access");
        }
        impl.save();
    }

    /**
     * Adds a change listener which will be notified of changes to the
     * list of config file groups.
     *
     * @param  listener a listener.
     */
    void addChangeListener(ChangeListener listener) {
        impl.addChangeListener(listener);
    }
}
