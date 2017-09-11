/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2014 Oracle and/or its affiliates. All rights reserved.
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
package org.netbeans.modules.profiler.nbimpl.providers;

import java.io.IOException;
import java.net.URL;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.modules.profiler.spi.ProfilerStorageProvider;
import org.netbeans.modules.profiler.utils.IDEUtils;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.URLMapper;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author Jiri Sedlacek
 */
@ServiceProvider(service=ProfilerStorageProvider.class)
public final class ProfilerStorageProviderImpl extends ProfilerStorageProvider.Abstract {
    
    private static final String PROFILER_FOLDER = "NBProfiler/Config";  // NOI18N
    private static final String SETTINGS_FOLDER = "Settings";   // NOI18N
    private static final String SETTINGS_FOR_ATTR = "settingsFor"; // NOI18N

    @Override
    public FileObject getGlobalFolder(boolean create) throws IOException {
        FileObject folder = FileUtil.getConfigFile(PROFILER_FOLDER);
        FileObject settingsFolder = folder.getFileObject(SETTINGS_FOLDER, null);

        if ((settingsFolder == null) && create)
            settingsFolder = folder.createFolder(SETTINGS_FOLDER);

        return settingsFolder;
    }

    @Override
    public FileObject getProjectFolder(Lookup.Provider project, boolean create) throws IOException {
        Project p = (Project)project;
        FileObject nbproject = p.getProjectDirectory().getFileObject("nbproject"); // NOI18N
        FileObject d;
        if (nbproject != null) {
            // For compatibility, continue to use nbproject/private/profiler for Ant-based projects.
            d = create ? FileUtil.createFolder(nbproject, "private/profiler") : nbproject.getFileObject("private/profiler"); // NOI18N
        } else {
            // Maven projects, autoprojects, etc.
            d = ProjectUtils.getCacheDirectory(p, IDEUtils.class);
        }
        if (d != null) d.setAttribute(SETTINGS_FOR_ATTR, p.getProjectDirectory().getURL());
        return d;
    }

    @Override
    public Lookup.Provider getProjectFromFolder(FileObject settingsFolder) {
        Object o = settingsFolder.getAttribute(SETTINGS_FOR_ATTR);
        if (o instanceof URL) {
            FileObject d = URLMapper.findFileObject((URL) o);
            if (d != null && d.isFolder()) {
                try {
                    return ProjectManager.getDefault().findProject(d);
                } catch (IOException ex) {
                    Exceptions.printStackTrace(ex);
                }
            }
        }
        Project p = FileOwnerQuery.getOwner(settingsFolder);
        try {
            if (p != null && getProjectFolder(p, false) == settingsFolder) return p;
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }
        return null;
    }
    
}
