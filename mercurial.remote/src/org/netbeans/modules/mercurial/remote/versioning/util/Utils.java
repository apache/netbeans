/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2015 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2015 Sun Microsystems, Inc.
 */
package org.netbeans.modules.mercurial.remote.versioning.util;

import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.api.project.Sources;
import org.netbeans.modules.versioning.core.api.VCSFileProxy;
import org.netbeans.modules.versioning.core.spi.VCSContext;
import org.openide.filesystems.FileObject;

/**
 * clone missed methods from org.netbeans.modules.versioning.util.Utils
 *
 * 
 */
public class Utils {
    private static final Logger LOG = Logger.getLogger(Utils.class.getName());
    
    /**
     * Returns the {@link Project} {@link File} for the given context
     *
     * @param VCSContext
     * @return File of Project Directory
     */
    public static VCSFileProxy getProjectFile(VCSContext context){
        return getProjectFile(getProject(context));
    }
    
    /**
     * Returns the {@link Project} {@link File} for the given {@link Project}
     * 
     * @param project
     * @return 
     */
    public static VCSFileProxy getProjectFile(Project project){
        if (project == null) {
            return null;
        }

        return VCSFileProxy.createFileProxy(project.getProjectDirectory());
    }
    
    /**
     * Returns {@link Project} for the given context
     * 
     * @param context
     * @return 
     */
    public static Project getProject(VCSContext context){
        if (context == null) {
            return null;
        }
        return getProject(context.getRootFiles().toArray(new VCSFileProxy[context.getRootFiles().size()]));
    }
    
    public static Project getProject (VCSFileProxy[] files) {
        for (VCSFileProxy file : files) {
            /* We may be committing a LocallyDeleted file */
            if (!file.exists()) {
                file = file.getParentFile();
            }
            FileObject fo =file.toFileObject();
            if(fo == null) {
                LOG.log(Level.FINE, "Utils.getProjectFile(): No FileObject for {0}", file); // NOI18N
            } else {
                Project p = FileOwnerQuery.getOwner(fo);
                if (p != null) {
                    return p;
                } else {
                    LOG.log(Level.FINE, "Utils.getProjectFile(): No project for {0}", file); // NOI18N
                }
            }
        }
        return null;
    }
    
    /**
     * Returns all root files for the given {@link Project}
     * 
     * @param project
     * @return 
     */
    public static VCSFileProxy[] getProjectRootFiles(Project project){
        if (project == null) {
            return null;
        }
        Set<VCSFileProxy> set = new HashSet<>();

        Sources sources = ProjectUtils.getSources(project);
        SourceGroup [] sourceGroups = sources.getSourceGroups(Sources.TYPE_GENERIC);
        for (int j = 0; j < sourceGroups.length; j++) {
            SourceGroup sourceGroup = sourceGroups[j];
            FileObject srcRootFo = sourceGroup.getRootFolder();
            VCSFileProxy rootFile = VCSFileProxy.createFileProxy(srcRootFo);
            set.add(rootFile);
        }
        return set.toArray(new VCSFileProxy[set.size()]);
    }    
   
}
