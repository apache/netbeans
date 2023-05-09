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
package org.netbeans.modules.ant.hints.errors;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.project.FileOwnerQuery;
import org.openide.filesystems.FileObject;
import org.openide.util.Parameters;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.modules.java.hints.spi.preview.PreviewEnabler;
import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.openide.util.EditableProperties;
import org.openide.util.Mutex;
import org.openide.util.MutexException;
import org.openide.util.lookup.ServiceProvider;

/**
 * Handle error rule "compiler.err.preview.feature.disabled.plural" and provide
 * the fix for Ant type project.
 *
 * @author arusinha
 */
public class EnablePreviewAntProj implements PreviewEnabler {

    private static final String ENABLE_PREVIEW_FLAG = "--enable-preview";   // NOI18N
    private static final String JAVAC_SOURCE = "javac.source"; // NOI18N
    private static final String JAVAC_TARGET = "javac.target"; // NOI18N
    private static final String JAVAC_COMPILER_ARGS = "javac.compilerargs"; // NOI18N
    private static final String RUN_JVMARGS = "run.jvmargs"; // NOI18N

    private final Project prj;

    public EnablePreviewAntProj(Project prj) {
        this.prj = prj;
    }

    @Override
    public void enablePreview(String newSourceLevel) throws Exception {
        new ResolveAntFix(prj).enablePreview(newSourceLevel);
    }

    @ServiceProvider(service=Factory.class, position=1000)
    public static final class FactoryImpl implements Factory {

        @Override
        public PreviewEnabler enablerFor(FileObject file) {
            final Project prj = FileOwnerQuery.getOwner(file);

            if (isAntProject(prj)) {
                return new EnablePreviewAntProj(prj);
            } else {
                return null;
            }
        }

    }

    private static final class ResolveAntFix {

        private final Project prj;

        ResolveAntFix(@NonNull final Project prj) {
            Parameters.notNull("prj", prj); //NOI18N
            this.prj = prj;
        }

        public void enablePreview(String newSourceLevel) throws Exception {
            EditableProperties ep = getEditableProperties(prj, AntProjectHelper.PROJECT_PROPERTIES_PATH);

            String compilerArgs = ep.getProperty(JAVAC_COMPILER_ARGS);
            compilerArgs = compilerArgs != null ? compilerArgs + " " + ENABLE_PREVIEW_FLAG : ENABLE_PREVIEW_FLAG;

            if (newSourceLevel != null) {
                ep.setProperty(JAVAC_SOURCE, newSourceLevel);
                if (ep.getProperty(JAVAC_TARGET) != null) {
                    ep.setProperty(JAVAC_TARGET, newSourceLevel);
                }
            }

            String runJVMArgs = ep.getProperty(RUN_JVMARGS);
            if (runJVMArgs == null) {
                runJVMArgs = ENABLE_PREVIEW_FLAG;
            } else if (!runJVMArgs.contains(ENABLE_PREVIEW_FLAG)) {
                runJVMArgs = runJVMArgs + " " + ENABLE_PREVIEW_FLAG;
            }

            ep.setProperty(JAVAC_COMPILER_ARGS, compilerArgs);
            ep.setProperty(RUN_JVMARGS, runJVMArgs);
            storeEditableProperties(prj, AntProjectHelper.PROJECT_PROPERTIES_PATH, ep);
        }

    }

    private static void storeEditableProperties(final Project prj, final String propertiesPath, final EditableProperties ep)
            throws IOException {
        try {
            ProjectManager.mutex().writeAccess(new Mutex.ExceptionAction<Void>() {
                @Override
                public Void run() throws IOException {
                    FileObject propertiesFo = prj.getProjectDirectory().getFileObject(propertiesPath);
                    if (propertiesFo != null) {
                        OutputStream os = null;
                        try {
                            os = propertiesFo.getOutputStream();
                            ep.store(os);
                        } finally {
                            if (os != null) {
                                os.close();
                            }
                        }
                    }
                    return null;
                }
            });
        } catch (MutexException ex) {
        }
    }

    private static EditableProperties getEditableProperties(final Project prj, final String propertiesPath)
            throws IOException {
        try {
            return ProjectManager.mutex().readAccess(new Mutex.ExceptionAction<EditableProperties>() {
                @Override
                public EditableProperties run() throws IOException {
                    FileObject propertiesFo = prj.getProjectDirectory().getFileObject(propertiesPath);
                    EditableProperties ep = null;
                    if (propertiesFo != null) {
                        InputStream is = null;
                        ep = new EditableProperties(false);
                        try {
                            is = propertiesFo.getInputStream();
                            ep.load(is);
                        } finally {
                            if (is != null) {
                                is.close();
                            }
                        }
                    }
                    return ep;
                }
            });
        } catch (MutexException ex) {
            return null;
        }
    }

    private static boolean isAntProject(Project prj) {
        if (prj == null) {
            return false;
        }
        FileObject prjDir = prj.getProjectDirectory();
        if (prjDir == null) {
            return false;
        }
        List<FileObject> antProjectFiles = new ArrayList<>();
        antProjectFiles.add(prjDir.getFileObject("build.xml"));   // NOI18N
        antProjectFiles.add(prjDir.getFileObject("nbproject/project.properties"));   // NOI18N
        antProjectFiles.add(prjDir.getFileObject("nbproject/project.xml"));   // NOI18N
        boolean isAntProject = true;
        for (FileObject file : antProjectFiles) {
            if (!(file != null && file.isValid())) {
                isAntProject = false;
                break;
            }
        }

        return isAntProject;

    }

}
