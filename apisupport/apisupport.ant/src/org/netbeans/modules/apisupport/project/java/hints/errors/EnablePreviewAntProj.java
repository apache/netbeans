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
package org.netbeans.modules.apisupport.project.java.hints.errors;

import com.sun.source.util.TreePath;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.lang.model.SourceVersion;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.java.source.CompilationInfo;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.modules.java.hints.spi.ErrorRule;
import org.netbeans.spi.editor.hints.ChangeInfo;
import org.netbeans.spi.editor.hints.Fix;
import org.openide.filesystems.FileObject;
import org.openide.util.NbBundle;
import org.openide.util.Parameters;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.openide.util.EditableProperties;
import org.openide.util.Mutex;
import org.openide.util.MutexException;

/**
 * Handle error rule "compiler.err.preview.feature.disabled.plural" and provide
 * the fix for Ant type project.
 *
 * @author arusinha
 */

@NbBundle.Messages({
    "FIX_EnablePreviewFeature=Enable Preview Feature" // NOI18N
})
public class EnablePreviewAntProj implements ErrorRule<Void> {

    private static final Set<String> ERROR_CODES = new HashSet<String>(Arrays.asList(
            "compiler.err.preview.feature.disabled.plural")); // NOI18N
    private static final String ENABLE_PREVIEW_FLAG = "--enable-preview";   // NOI18N
    private static final String JAVAC_COMPILER_ARGS = "javac.compilerargs"; // NOI18N
    private static final String RUN_JVMARGS = "run.jvmargs"; // NOI18N

    @Override
    public Set<String> getCodes() {
        return Collections.unmodifiableSet(ERROR_CODES);
    }

    @Override
    @NonNull
    public List<Fix> run(CompilationInfo compilationInfo, String diagnosticKey, int offset, TreePath treePath, Data<Void> data) {

        System.out.println("ANT projectSourceVersion.latest() =="+ SourceVersion.latest() );
        System.out.println("ANT compilationInfo.getSourceVersion() =="+ compilationInfo.getSourceVersion());
        if (SourceVersion.latest() != compilationInfo.getSourceVersion()) {
            return Collections.<Fix>emptyList();
        }

        final FileObject file = compilationInfo.getFileObject();
        Fix fix = null;
        if (file != null) {
            final Project prj = FileOwnerQuery.getOwner(file);

            if (isAntProject(prj)) {
                fix = new EnablePreviewAntProj.ResolveAntFix(prj);
            } else {
                fix = null;
            }
        }
        return (fix != null) ? Collections.<Fix>singletonList(fix) : Collections.<Fix>emptyList();
    }

    @Override
    public String getId() {
        return EnablePreviewAntProj.class.getName();
    }

    @Override
    public String getDisplayName() {
        return NbBundle.getMessage(EnablePreviewAntProj.class, "FIX_EnablePreviewFeature"); // NOI18N
    }

    public String getDescription() {
        return NbBundle.getMessage(EnablePreviewAntProj.class, "FIX_EnablePreviewFeature"); // NOI18N
    }

    @Override
    public void cancel() {
    }

    private static final class ResolveAntFix implements Fix {

        private final Project prj;

        ResolveAntFix(@NonNull final Project prj) {
            Parameters.notNull("prj", prj); //NOI18N
            this.prj = prj;
        }

        @Override
        public String getText() {
            return NbBundle.getMessage(EnablePreviewAntProj.class, "FIX_EnablePreviewFeature");  // NOI18N
        }

        @Override
        public ChangeInfo implement() throws Exception {

            EditableProperties ep = getEditableProperties(prj, AntProjectHelper.PROJECT_PROPERTIES_PATH);

            String compilerArgs = ep.getProperty(JAVAC_COMPILER_ARGS);
            compilerArgs = compilerArgs != null ? compilerArgs + " " + ENABLE_PREVIEW_FLAG : ENABLE_PREVIEW_FLAG;

            String runJVMArgs = ep.getProperty(RUN_JVMARGS);
            runJVMArgs = runJVMArgs != null ? runJVMArgs + " " + ENABLE_PREVIEW_FLAG : ENABLE_PREVIEW_FLAG;

            ep.setProperty(JAVAC_COMPILER_ARGS, compilerArgs);
            ep.setProperty(RUN_JVMARGS, runJVMArgs);
            storeEditableProperties(prj, AntProjectHelper.PROJECT_PROPERTIES_PATH, ep);
            return null;
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

    private boolean isAntProject(Project prj) {

        List<FileObject> antProjectFiles = new ArrayList();
        antProjectFiles.add(prj.getProjectDirectory().getFileObject("build.xml"));   // NOI18N
        antProjectFiles.add(prj.getProjectDirectory().getFileObject("nbproject/project.properties"));   // NOI18N
        antProjectFiles.add(prj.getProjectDirectory().getFileObject("nbproject/project.xml"));   // NOI18N
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
