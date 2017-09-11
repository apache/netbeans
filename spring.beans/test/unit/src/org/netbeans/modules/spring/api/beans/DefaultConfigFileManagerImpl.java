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
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import javax.swing.event.ChangeListener;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.modules.spring.beans.ConfigFileManagerImplementation;
import org.openide.util.ChangeSupport;
import org.openide.util.Mutex;

/**
 *
 * @author Andrei Badea
 */
public class DefaultConfigFileManagerImpl implements ConfigFileManagerImplementation {

    private final ChangeSupport changeSupport = new ChangeSupport(this);
    private List<File> files = new ArrayList<File>();
    private List<ConfigFileGroup> groups = new ArrayList<ConfigFileGroup>();

    public DefaultConfigFileManagerImpl(ConfigFileGroup... groups) {
        Set<File> allFiles = new TreeSet<File>();
        for (ConfigFileGroup group : groups) {
            this.groups.add(group);
            for (File file : group.getFiles()) {
                allFiles.add(file);
            }
        }
        for (File file : allFiles) {
            files.add(file);
        }
    }

    public DefaultConfigFileManagerImpl(File[] files, ConfigFileGroup[] groups) {
        for (File file : files) {
            this.files.add(file);
        }
        for (ConfigFileGroup group : groups) {
            this.groups.add(group);
        }
    }

    public Mutex mutex() {
        return ProjectManager.mutex();
    }

    public List<File> getConfigFiles() {
        return files;
    }

    public List<ConfigFileGroup> getConfigFileGroups() {
        return groups;
    }

    public void putConfigFilesAndGroups(List<File> files, List<ConfigFileGroup> groups) {
        this.groups = groups;
        this.files = files;
        changeSupport.fireChange();
    }

    public void save() throws IOException {
    }

    public void addChangeListener(ChangeListener listener) {
        changeSupport.addChangeListener(listener);
    }
}
