/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.netbeans.modules.css.prep;

import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.text.Document;
import org.netbeans.api.editor.mimelookup.MimeRegistration;
import org.netbeans.api.editor.mimelookup.MimeRegistrations;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.modules.csl.api.DataLoadersBridge;
import org.netbeans.modules.css.prep.util.CssPreprocessorUtils;
import org.netbeans.spi.editor.document.OnSaveTask;
import org.openide.filesystems.FileObject;
import org.openide.util.RequestProcessor;

@MimeRegistrations({
    @MimeRegistration(mimeType = "text/scss", service = OnSaveTask.Factory.class, position = 2000),
    @MimeRegistration(mimeType = "text/sass", service = OnSaveTask.Factory.class, position = 2000),
    @MimeRegistration(mimeType = "text/less", service = OnSaveTask.Factory.class, position = 2000),})
public class CPOnSaveHook implements OnSaveTask.Factory {

    private static final RequestProcessor RP = new RequestProcessor(CPOnSaveHook.class);
    private static final Logger LOG = Logger.getLogger(CPOnSaveHook.class.getSimpleName());
    
    @Override
    public OnSaveTask createTask(OnSaveTask.Context context) {
        Document doc = context.getDocument();
        final FileObject fileObject = DataLoadersBridge.getDefault().getFileObject(doc);
        final Project project = fileObject == null ? null : FileOwnerQuery.getOwner(fileObject);

        return project == null ? null : new OnSaveTask() {
            private boolean cancelled;

            @Override
            public void performTask() {
                if (!cancelled) {
                    //run the CssPreprocessorUtils.processSavedFile() out of the original thread
                    RP.post(new Runnable() {

                        @Override
                        public void run() {
                            String mimeType = fileObject.getMIMEType();
                            CssPreprocessorType type = CssPreprocessorType.find(mimeType);
                            if (type == null) {
                                // #244470
                                LOG.log(Level.WARNING, "Cannot find CssPreprocessorType for MIME type {0} (filename: {1})", new Object[] {mimeType, fileObject.getNameExt()});
                                return;
                            }
                            CssPreprocessorUtils.processSavedFile(project, type);
                            LOG.log(Level.INFO, "processSavedFile called for {0} type on project {1}.", new Object[]{type.getDisplayName(), project.getProjectDirectory().getPath()});
                        }
                        
                    });
                }
            }

            @Override
            public void runLocked(Runnable run) {
                run.run(); //better keep running in the original thread as the tasks may be nested
            }

            @Override
            public boolean cancel() {
                return cancelled = true;
            }
        };
    }
}
