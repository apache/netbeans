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
package org.netbeans.modules.java.hints.errors;

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
import org.openide.util.EditableProperties;
import org.openide.util.Mutex;
import org.openide.util.MutexException;

/**
 * Handle error rule "compiler.err.preview.feature.disabled.plural" and provide
 * the fix for Single Source Java File.
 *
 * @author Arunava Sinha
 */
public class EnablePreviewSingleSourceFile implements ErrorRule<Void> {

    private static final Set<String> ERROR_CODES = new HashSet<String>(Arrays.asList(
            "compiler.err.preview.feature.disabled",           //NOI18N  
            "compiler.err.preview.feature.disabled.plural",    // NOI18N
            "compiler.err.is.preview"));                       // NOI18N
    private static final String ENABLE_PREVIEW_FLAG = "--enable-preview";   // NOI18N
    private static final String SOURCE_FLAG = "--source";   // NOI18N

    private static final String FILE_VM_OPTIONS = "single_file_vm_options"; //NOI18N

    @Override
    public Set<String> getCodes() {
        return Collections.unmodifiableSet(ERROR_CODES);
    }

    @Override
    @NonNull
    public List<Fix> run(CompilationInfo compilationInfo, String diagnosticKey, int offset, TreePath treePath, Data<Void> data) {
        if (SourceVersion.latest() != compilationInfo.getSourceVersion()) {
            return Collections.<Fix>emptyList();
        }

        final FileObject file = compilationInfo.getFileObject();

        Fix fix = null;
        if (file != null) {
            final Project prj = FileOwnerQuery.getOwner(file);
            if (prj == null) {
                fix = new EnablePreviewSingleSourceFile.ResolveFix(file);
            } else {
                fix = null;
            }
        }
        return (fix != null) ? Collections.<Fix>singletonList(fix) : Collections.<Fix>emptyList();
    }

    @Override
    public String getId() {
        return EnablePreviewSingleSourceFile.class.getName();
    }

    @Override
    public String getDisplayName() {
        return NbBundle.getMessage(EnablePreviewSingleSourceFile.class, "FIX_EnablePreviewFeature"); // NOI18N
    }

    public String getDescription() {
        return NbBundle.getMessage(EnablePreviewSingleSourceFile.class, "FIX_EnablePreviewFeature"); // NOI18N
    }

    @Override
    public void cancel() {
    }

    private static final class ResolveFix implements Fix {

        private FileObject file;

        ResolveFix(@NonNull FileObject file) {
            Parameters.notNull("file", file); //NOI18N
            this.file = file;
        }

        @Override
        public String getText() {
            return NbBundle.getMessage(EnablePreviewSingleSourceFile.class, "FIX_EnablePreviewFeature");  // NOI18N
        }

        @Override
        public ChangeInfo implement() throws Exception {

            EditableProperties ep = getEditableProperties(file);
            String compilerArgs = (String) file.getAttribute(FILE_VM_OPTIONS);

            if (compilerArgs != null && !compilerArgs.isEmpty()) {
                compilerArgs = compilerArgs.contains(SOURCE_FLAG) ? compilerArgs + " " + ENABLE_PREVIEW_FLAG : compilerArgs + " " + ENABLE_PREVIEW_FLAG + " " + SOURCE_FLAG + " " + getJdkRunVersion();
            } else {
                compilerArgs = ENABLE_PREVIEW_FLAG + " " + SOURCE_FLAG + " " + getJdkRunVersion();
            }
            file.setAttribute(FILE_VM_OPTIONS, compilerArgs);
            storeEditableProperties(ep, file);
            return null;
        }

    }

    private static EditableProperties getEditableProperties(FileObject file)
            throws IOException {
        try {
            return ProjectManager.mutex().readAccess(new Mutex.ExceptionAction<EditableProperties>() {
                @Override
                public EditableProperties run() throws IOException {
                    FileObject propertiesFo = file;
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

    private static void storeEditableProperties(final EditableProperties ep, FileObject file)
            throws IOException {
        try {
            ProjectManager.mutex().writeAccess(new Mutex.ExceptionAction<Void>() {
                @Override
                public Void run() throws IOException {
                    FileObject propertiesFo = null;
                    if (file != null) {
                        propertiesFo = file;
                    }

                    if (propertiesFo != null) {
                        OutputStream os = null;
                        try {
                            os = propertiesFo.getOutputStream();
                            ep.store(os);
                        } finally {
                            if (os != null) {
                                os.flush();
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

    private static String getJdkRunVersion() {
        String javaVersion = System.getProperty("java.specification.version"); //NOI18N 
        if (javaVersion.startsWith("1.")) { //NOI18N
            javaVersion = javaVersion.substring(2);
        }

        return javaVersion;
    }

}
