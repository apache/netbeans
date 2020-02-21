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
package org.netbeans.modules.cnd.makeproject.api.support;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.SimpleFormatter;
import java.util.logging.StreamHandler;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.modules.cnd.makeproject.MakeBasedProjectFactorySingleton;
import org.netbeans.modules.cnd.makeproject.MakeProjectHelperImpl;
import org.netbeans.modules.cnd.makeproject.MakeProjectTypeImpl;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Lookup;
import org.openide.util.Mutex;
import org.openide.util.MutexException;
import org.openide.xml.XMLUtil;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Creates a MakeProject from scratch according to some initial configuration.
 */
public final class MakeProjectGenerator {

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
    public static MakeProjectHelper createProject(final FileObject directory, final String type) throws IOException, IllegalArgumentException {
        try {
            return ProjectManager.mutex().writeAccess((Mutex.ExceptionAction<MakeProjectHelper>) () -> {
                directory.refresh();
                FileObject projectXml = directory.getFileObject(MakeProjectHelper.PROJECT_XML_PATH);
                if (projectXml != null) {
                    throw new IllegalArgumentException("Already a " + projectXml); // NOI18N
                }
                Project prj = ProjectManager.getDefault().findProject(directory);
                if (prj != null && prj.getProjectDirectory().getChildren().length == 0) {
                    // #139769: try to cleanse ProjectManager's cache of it.
                    MakeProjectHelper h = MakeBasedProjectFactorySingleton.getHelperFor(prj);
                    if (h != null) {
                        h.notifyDeleted();
                        prj = ProjectManager.getDefault().findProject(directory);
                    }
                }
                if (prj != null) {
                    throw new IllegalArgumentException("Already a " + prj.getClass().getName() + " in " + directory); // NOI18N
                }
                projectXml = FileUtil.createData(directory, MakeProjectHelper.PROJECT_XML_PATH);
                Document doc = XMLUtil.createDocument("project", MakeProjectHelper.PROJECT_NS, null, null); // NOI18N
                Element el = doc.createElementNS(MakeProjectHelper.PROJECT_NS, "type"); // NOI18N
                el.appendChild(doc.createTextNode(type));
                doc.getDocumentElement().appendChild(el);
                el = doc.createElementNS(MakeProjectHelper.PROJECT_NS, "configuration"); // NOI18N
                doc.getDocumentElement().appendChild(el);
                
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                XMLUtil.write(doc, baos, "UTF-8"); // NOI18N
                final byte[] data = SmartOutputStream.convertLineSeparator(baos, projectXml, projectXml.getParent());
                OutputStream os = SmartOutputStream.getSmartOutputStream(projectXml);
                try {
                    os.write(data);
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
                    Level oldLevel = MakeBasedProjectFactorySingleton.LOG.getLevel();
                    MakeBasedProjectFactorySingleton.LOG.setLevel(Level.ALL);
                    MakeBasedProjectFactorySingleton.LOG.addHandler(diagHandler);
                    try {
                        p = ProjectManager.getDefault().findProject(directory);
                    } finally {
                        MakeBasedProjectFactorySingleton.LOG.removeHandler(diagHandler);
                        MakeBasedProjectFactorySingleton.LOG.setLevel(oldLevel);
                        diagHandler.close();
                    }
                } else {
                    p = ProjectManager.getDefault().findProject(directory);
                }
                if (p == null) {
                    // Something is wrong, it is not being recognized.
                    for (MakeProjectTypeImpl abpt : Lookup.getDefault().lookupAll(MakeProjectTypeImpl.class)) {
                        if (abpt.getType().equals(type)) {
                            // Well, the factory was there.
                            throw new IllegalArgumentException("For some reason the folder " + directory + // NOI18N
                                    " with a new project of type " + type + " is still not recognized" + diagStream); // NOI18N
                        }
                    }
                    throw new IllegalArgumentException("No Ant-based project factory for type " + type); // NOI18N
                }
                MakeProjectHelperImpl helper = MakeBasedProjectFactorySingleton.getHelperFor(p);
                if (helper == null) {
                    throw new IllegalArgumentException("Project " + p + " was not recognized as an Ant-based project"); // NOI18N
                }
                helper.markModified();
                return helper;
            });
        } catch (MutexException e) {
            throw (IOException)e.getException();
        }
    }
}
