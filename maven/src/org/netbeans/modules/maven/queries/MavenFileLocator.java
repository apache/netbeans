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
 * Portions Copyrighted 2009 Sun Microsystems, Inc.
 */
package org.netbeans.modules.maven.queries;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.api.extexecution.print.LineConvertors;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.platform.JavaPlatform;
import org.netbeans.api.java.project.JavaProjectConstants;
import org.netbeans.api.java.queries.SourceForBinaryQuery;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.api.project.Sources;
import org.netbeans.modules.maven.api.NbMavenProject;
import org.netbeans.modules.maven.api.execute.ActiveJ2SEPlatformProvider;
import org.netbeans.spi.java.classpath.ClassPathProvider;
import org.netbeans.spi.java.classpath.support.ClassPathSupport;
import org.netbeans.spi.project.ProjectServiceProvider;
import org.openide.filesystems.FileObject;

/**
 *
 * @author mkleint
 */
@ProjectServiceProvider(service=LineConvertors.FileLocator.class, projectType="org-netbeans-modules-maven")
public class MavenFileLocator implements LineConvertors.FileLocator {

    private ClassPath classpath;
    private final Project project;
    private static final Object LOCK = new Object();
    private static final Logger LOG = Logger.getLogger(MavenFileLocator.class.getName());

    public MavenFileLocator(Project project) {
        this.project = project;
        NbMavenProject.addPropertyChangeListener(project, new PropertyChangeListener() {
            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                 //explicitly listing both RESOURCE and PROJECT properties, it's unclear if both are required but since some other places call addWatchedPath but don't listen it's likely required
                if (NbMavenProject.PROP_RESOURCE.equals(evt.getPropertyName()) || NbMavenProject.PROP_PROJECT.equals(evt.getPropertyName())) {
                    synchronized (LOCK) {
                        classpath = null;
                    }
                }
            }
        });
    }

    @Override
    public FileObject find(String filename) {
        if (filename == null) {
            return null;
        }
        ClassPath cp;
        synchronized (LOCK) {
            if (classpath == null) {
                classpath = getProjectClasspath(project);
            }
            cp = classpath;
        }
        FileObject toRet = cp.findResource(filename);
        if (toRet == null) {
            LOG.log(Level.FINE, "#221053: Cannot find FileObject for {0}", filename);
        }
        return toRet;
    }

    private ClassPath getProjectClasspath(Project p) {
        ClassPath result;
        ClassPathProvider cpp = p.getLookup().lookup(ClassPathProvider.class);
        Set<FileObject> roots = new HashSet<FileObject>();
        Sources sources = ProjectUtils.getSources(p);
        if (sources != null) {
            SourceGroup[] groups = sources.getSourceGroups(JavaProjectConstants.SOURCES_TYPE_JAVA);
            for (SourceGroup group : groups) {
                roots.add(group.getRootFolder());
            }
        }

        Set<ClassPath> setCP = new HashSet<ClassPath>();
        if (cpp != null) {
            for (FileObject file : roots) {
                ClassPath path = cpp.findClassPath(file, ClassPath.COMPILE);
                setCP.add(path);
            }
        }

        for (ClassPath cp : setCP) {
            FileObject[] rootsCP = cp.getRoots();
            for (FileObject fo : rootsCP) {
                FileObject[] aaa = SourceForBinaryQuery.findSourceRoots(fo.toURL()).getRoots();
                roots.addAll(Arrays.asList(aaa));
            }
        }
        JavaPlatform platform = p.getLookup().lookup(ActiveJ2SEPlatformProvider.class).getJavaPlatform();

        if (platform != null) {
            roots.addAll(Arrays.asList(platform.getSourceFolders().getRoots()));
        }
        result = ClassPathSupport.createClassPath(roots.toArray(new FileObject[roots.size()]));
        return result;
    }
}
