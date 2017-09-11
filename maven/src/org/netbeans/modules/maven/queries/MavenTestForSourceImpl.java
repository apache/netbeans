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

package org.netbeans.modules.maven.queries;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import org.netbeans.api.project.Project;
import org.netbeans.modules.maven.api.FileUtilities;
import org.netbeans.modules.maven.api.NbMavenProject;
import org.netbeans.spi.java.queries.MultipleRootsUnitTestForSourceQueryImplementation;
import org.netbeans.spi.project.ProjectServiceProvider;
import org.openide.ErrorManager;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Utilities;

/**
 * JUnit tests queries.
 * @author  Milos Kleint
 */
@ProjectServiceProvider(service=MultipleRootsUnitTestForSourceQueryImplementation.class, projectType="org-netbeans-modules-maven")
public class MavenTestForSourceImpl implements MultipleRootsUnitTestForSourceQueryImplementation {
    
                                                          
    private final Project project;

    public MavenTestForSourceImpl(Project proj) {
        project = proj;
    }


    @Override
    public URL[] findUnitTests(FileObject fileObject) {
        try {
            String str = project.getLookup().lookup(NbMavenProject.class).getMavenProject().getBuild().getTestSourceDirectory();
            if (str != null) {
                File fl = FileUtilities.convertStringToFile(str);
                File param = FileUtil.toFile(fileObject);
                if (fl.equals(param)) {
                    return null;
                }
                URI uri = Utilities.toURI(fl);
                URL entry = uri.toURL();
                if  (!entry.toExternalForm().endsWith("/")) { //NOI18N
                    entry = new URL(entry.toExternalForm() + "/"); //NOI18N
                }
                return new URL[] { entry };
            }
        } catch (MalformedURLException exc) {
            ErrorManager.getDefault().notify(exc);
        }
        return null;
    }

    @Override
    public URL[] findSources(FileObject fileObject) {
        try {
            String str = project.getLookup().lookup(NbMavenProject.class).getMavenProject().getBuild().getSourceDirectory();
            if (str != null) {
                File fl = FileUtilities.convertStringToFile(str);
                File param = FileUtil.toFile(fileObject);
                if (fl.equals(param)) {
                    return null;
                }
                URI uri = Utilities.toURI(fl);
                URL entry = uri.toURL();
                if  (!entry.toExternalForm().endsWith("/")) { //NOI18N
                    entry = new URL(entry.toExternalForm() + "/"); //NOI18N
                }
                
                return new URL[] { entry };
            }
        } catch (MalformedURLException exc) {
            ErrorManager.getDefault().notify(exc);
        }
        return null;
    }
    
}
