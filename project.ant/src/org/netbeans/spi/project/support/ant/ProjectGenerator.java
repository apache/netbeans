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

package org.netbeans.spi.project.support.ant;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Collections;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.SimpleFormatter;
import java.util.logging.StreamHandler;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.modules.project.ant.AntBasedProjectFactorySingleton;
import org.netbeans.modules.project.ant.ProjectLibraryProvider;
import org.netbeans.spi.project.ui.support.ProjectConvertors;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Lookup;
import org.openide.util.Mutex;
import org.openide.util.MutexException;
import org.openide.util.lookup.Lookups;
import org.openide.xml.XMLUtil;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Utilities to create new Ant-based projects on disk.
 * @author Jesse Glick
 */
public class ProjectGenerator {
    
    private ProjectGenerator() {}
    
    /**
     * Create a new Ant-based project on disk.
     * It will initially be only minimally configured - just a skeleton <code>project.xml</code>.
     * It will be marked as modified.
     * <p>In order to fill in various details of it, call normal methods on the returned
     * helper object, then save the project when you are done.
     * (You can use {@link ProjectManager} to find the project object to be saved.)
     * <p>No <code>build-impl.xml</code> will be created immediately; once you save the project
     * changes, it will be created. If you wish to create a top-level <code>build.xml</code>
     * use {@link GeneratedFilesHelper#generateBuildScriptFromStylesheet} after
     * (or while) saving the project.
     * <p>Acquires write access. But you are advised to acquire a write lock for
     * the entire operation of creating, configuring, and saving the new project,
     * and creating its initial <code>build.xml</code>.
     * @param directory the main project directory to create it in
     *                  (see {@link AntProjectHelper#getProjectDirectory})
     * @param type a unique project type identifier (see {@link AntBasedProjectType#getType})
     * @return an associated helper object
     * @throws IOException if there is a problem physically creating the project
     * @throws IllegalArgumentException if the project type does not match a registered
     *                                  Ant-based project type factory or if the directory
     *                                  is already recognized as some kind of project or if the
     *                                  new project on disk is recognized by some other factory
     */
    public static AntProjectHelper createProject(final FileObject directory, final String type) throws IOException, IllegalArgumentException {
        return createProject0(directory, type, null, null);
    }
    
    /**
     * See {@link #createProject(FileObject, String)} for more details. This
     * method in addition allows to setup shared libraries location
     * @param directory the main project directory to create it in
     *                  (see {@link AntProjectHelper#getProjectDirectory})
     * @param type a unique project type identifier (see {@link AntBasedProjectType#getType})
     * @param librariesDefinition relative or absolute OS path; can be null
     */
    public static AntProjectHelper createProject(final FileObject directory, final String type, 
            final String librariesDefinition) throws IOException, IllegalArgumentException {
        return createProject0(directory, type, null, librariesDefinition);
    }
    
    private static AntProjectHelper createProject0(final FileObject directory, final String type, 
            final String name, final String librariesDefinition) throws IOException, IllegalArgumentException {
        try {
            return ProjectManager.mutex().writeAccess(new Mutex.ExceptionAction<AntProjectHelper>() {
                public @Override AntProjectHelper run() throws IOException {
                    directory.refresh();
                    FileObject projectXml = directory.getFileObject(AntProjectHelper.PROJECT_XML_PATH);
                    if (projectXml != null) {
                        throw new IllegalArgumentException("Already a " + projectXml); // NOI18N
                    }
                    Project prj = ProjectManager.getDefault().findProject(directory);
                    if (prj != null) {
                        if(ProjectConvertors.isConvertorProject(prj)) {
                            ProjectConvertors.unregisterConvertorProject(prj);
                            prj = null;
                        } else if (prj.getProjectDirectory().getChildren().length == 0) {
                            // #139769: try to cleanse ProjectManager's cache of it.
                            AntProjectHelper h = AntBasedProjectFactorySingleton.getHelperFor(prj);
                            if (h != null) {
                                h.notifyDeleted();
                                prj = ProjectManager.getDefault().findProject(directory);
                            }
                        }
                    }
                    if (prj != null) {
                        throw new IllegalArgumentException("Already a " + prj.getClass().getName() + " in " + directory); // NOI18N
                    }
                    projectXml = FileUtil.createData(directory, AntProjectHelper.PROJECT_XML_PATH);
                    Document doc = XMLUtil.createDocument("project", AntProjectHelper.PROJECT_NS, null, null); // NOI18N
                    Element el = doc.createElementNS(AntProjectHelper.PROJECT_NS, "type"); // NOI18N
                    el.appendChild(doc.createTextNode(type));
                    doc.getDocumentElement().appendChild(el);
                    if (name != null) {
                        el = doc.createElementNS(AntProjectHelper.PROJECT_NS, "name"); // NOI18N
                        el.appendChild(doc.createTextNode(name));
                        doc.getDocumentElement().appendChild(el);
                    }
                    el = doc.createElementNS(AntProjectHelper.PROJECT_NS, "configuration"); // NOI18N
                    doc.getDocumentElement().appendChild(el);
                    if (librariesDefinition != null) {
                        el.appendChild(ProjectLibraryProvider.createLibrariesElement(doc, librariesDefinition));
                        // create libraries property file if it does not exist:
                        File f = new File(librariesDefinition);
                        if (!f.isAbsolute()) {
                            f = new File(FileUtil.toFile(directory), librariesDefinition);
                        }
                        f = FileUtil.normalizeFile(f);
                        if (!f.exists()) {
                            FileUtil.createData(f);
                        }
                    }
                    OutputStream os = projectXml.getOutputStream();
                    try {
                        XMLUtil.write(doc, os, "UTF-8"); // NOI18N
                    } finally {
                        os.close();
                    }
                    // OK, disk file project.xml has been created.
                    // Load the project into memory and mark it as modified.
                    ProjectManager.getDefault().clearNonProjectCache();
                    ByteArrayOutputStream diagStream = new ByteArrayOutputStream();
                    Project p;
                    if (System.getProperty("java.class.path").contains("junit")) {
                        diagStream.write(':');
                        diagStream.write('\n');
                        Handler diagHandler = new StreamHandler(diagStream, new SimpleFormatter());
                        diagHandler.setLevel(Level.ALL);
                        Level oldLevel = AntBasedProjectFactorySingleton.LOG.getLevel();
                        AntBasedProjectFactorySingleton.LOG.setLevel(Level.ALL);
                        AntBasedProjectFactorySingleton.LOG.addHandler(diagHandler);
                        try {
                            p = ProjectManager.getDefault().findProject(directory);
                        } finally {
                            AntBasedProjectFactorySingleton.LOG.removeHandler(diagHandler);
                            AntBasedProjectFactorySingleton.LOG.setLevel(oldLevel);
                            diagHandler.close();
                        }
                    } else {
                        p = ProjectManager.getDefault().findProject(directory);
                    }
                    if (p == null) {
                        // Something is wrong, it is not being recognized.
                        for (AntBasedProjectType abpt : Lookup.getDefault().lookupAll(AntBasedProjectType.class)) {
                            if (abpt.getType().equals(type)) {
                                // Well, the factory was there.
                                throw new IllegalArgumentException("For some reason the folder " + directory +
                                        " with a new project of type " + type + " is still not recognized" + diagStream); // NOI18N
                            }
                        }
                        for (AntBasedProjectType abpt : Lookups.forPath("Services").lookupAll(AntBasedProjectType.class)) {
                            if (abpt.getType().equals(type)) {
                                throw new IllegalArgumentException("Factory type " + type + " is in Services lookup but not global");
                            }
                        }
                        if (FileUtil.getConfigFile("Services/AntBasedProjectTypes/" + type.replace('.', '-') + ".instance") != null) {
                            throw new IllegalArgumentException("Factory type " + type + " is registered but does not appear in lookup");
                        }
                        FileObject services = FileUtil.getConfigFile("Services");
                        if (services == null) {
                            FileObject r = FileUtil.getConfigRoot();
                            throw new IllegalArgumentException("No Services folder; SFS " + r.getFileSystem() + " contains: " + Collections.list(r.getChildren(true)));
                        }
                        throw new IllegalArgumentException("No Ant-based project factory for type " + type + "; Services folder contains: " + Collections.list(services.getChildren(true))); // NOI18N
                    }
                    AntProjectHelper helper = AntBasedProjectFactorySingleton.getHelperFor(p);
                    if (helper == null) {
                        throw new IllegalArgumentException("Project " + p + " was not recognized as an Ant-based project"); // NOI18N
                    }
                    helper.markModified();
                    return helper;
                }
            });
        } catch (MutexException e) {
            throw (IOException)e.getException();
        }
    }

}
