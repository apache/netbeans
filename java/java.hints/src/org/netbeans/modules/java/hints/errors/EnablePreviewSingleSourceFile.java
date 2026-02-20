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

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.project.FileOwnerQuery;
import org.openide.filesystems.FileObject;
import org.openide.util.Parameters;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.modules.java.hints.spi.preview.PreviewEnabler;
import org.openide.util.EditableProperties;
import org.openide.util.Mutex;
import org.openide.util.MutexException;
import org.openide.util.lookup.ServiceProvider;

/**
 * Handle error rule "compiler.err.preview.feature.disabled.plural" and provide
 * the fix for Single Source Java File.
 *
 * @author Arunava Sinha
 */
public class EnablePreviewSingleSourceFile implements PreviewEnabler {

    private static final String ENABLE_PREVIEW_FLAG = "--enable-preview";   // NOI18N
    private static final String SOURCE_FLAG = "--source";   // NOI18N
    private static final Pattern SOURCE_FLAG_PATTERN = Pattern.compile(SOURCE_FLAG + "[ \t]+[0-9]+");

    private static final String FILE_VM_OPTIONS = "single_file_vm_options"; //NOI18N

    private final FileObject file;

    private EnablePreviewSingleSourceFile(@NonNull FileObject file) {
        Parameters.notNull("file", file); //NOI18N
        this.file = file;
    }

    @Override
    public void enablePreview(String newSourceLevel) throws Exception {
        EditableProperties ep = getEditableProperties(file);
        String compilerArgs = (String) file.getAttribute(FILE_VM_OPTIONS);

        if (compilerArgs == null) {
            compilerArgs = "";
        }

        Matcher m = SOURCE_FLAG_PATTERN.matcher(compilerArgs);

        if (newSourceLevel == null) {
            newSourceLevel = Integer.toString(Runtime.version().feature());
        }

        if (compilerArgs.contains(SOURCE_FLAG)) {
            compilerArgs = m.replaceAll("--enable-preview " + SOURCE_FLAG + " " + newSourceLevel);
        } else {
            compilerArgs += (compilerArgs.isEmpty() ? "" : " ") + ENABLE_PREVIEW_FLAG + " " + SOURCE_FLAG + " " + newSourceLevel;
        }
        file.setAttribute(FILE_VM_OPTIONS, compilerArgs);
        storeEditableProperties(ep, file);
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

    @ServiceProvider(service=Factory.class)
    public static final class FactoryImpl implements Factory {

        @Override
        public PreviewEnabler enablerFor(FileObject file) {
            if (file != null) {
                final Project prj = FileOwnerQuery.getOwner(file);
                if (prj == null) {
                    return new EnablePreviewSingleSourceFile(file);
                }
            }

            return null;
        }

    }

}
