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

package org.netbeans.modules.maven;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.atomic.AtomicBoolean;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.spi.project.ProjectFactory;
import org.netbeans.spi.project.ProjectFactory2;
import org.netbeans.spi.project.ProjectState;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.ImageUtilities;
import org.openide.util.lookup.ServiceProvider;

/**
 * factory of maven projects
 * @author  Milos Kleint
 */
@ServiceProvider(service=ProjectFactory.class, position=666)
public class NbMavenProjectFactory implements ProjectFactory2 {
    
    private static final AtomicBoolean atLeastOneMavenProjectAround = new AtomicBoolean(false);
    
    /**
     * a simple way to tell if at least one maven project was loaded, to be used for
     * performance optimizations in global services.
     * @return 
     * @since
     */
    public static boolean isAtLeastOneMavenProjectAround() {
        return atLeastOneMavenProjectAround.get();
    }
    
    
    public @Override boolean isProject(FileObject fileObject) {
        File projectDir = FileUtil.toFile(fileObject); //guard that we only recognize projects on local filesystem. Maven won't be able to work with anything else.
        if (projectDir == null) {
            return false;
        }
        File project = new File(projectDir, "pom.xml"); // NOI18N
        if (!project.isFile()) {
            return false;
        }
        if (project.getAbsolutePath().contains("resources" + File.separator + "archetype-resources")) { //NOI18N
            //this is an archetype resource, happily ignore..
            return false;
        }
        String projectDirName = projectDir.getName();
        if (projectDirName.equals("nbproject")) {
            return false; // XXX why?
        }
        if (projectDirName.equals("target") && new File(projectDir.getParentFile(), "pom.xml").isFile()) {
            return false;
        }
        return true;
    }

    public @Override ProjectManager.Result isProject2(FileObject projectDirectory) {
        if (isProject(projectDirectory)) {
            return new ProjectManager.Result(ImageUtilities.loadImageIcon("org/netbeans/modules/maven/resources/Maven2Icon.gif", true)); //NOI18N
        }
        return null;
    }

    public @Override Project loadProject(FileObject fileObject, ProjectState projectState) throws IOException { 
        if (!isProject(fileObject)) {
            return null;
        }
        FileObject projectFile = fileObject.getFileObject("pom.xml"); //NOI18N
        if (projectFile == null || !projectFile.isData()) {
            return null;

        }
        atLeastOneMavenProjectAround.set(true);
        return new NbMavenProjectImpl(fileObject, projectFile, projectState);
    }
    
    public @Override void saveProject(Project project) throws IOException {
        // what to do here??
    }    
}
