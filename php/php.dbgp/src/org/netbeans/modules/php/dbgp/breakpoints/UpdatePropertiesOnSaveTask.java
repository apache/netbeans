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
package org.netbeans.modules.php.dbgp.breakpoints;

import javax.swing.text.Document;
import org.netbeans.api.debugger.Breakpoint;
import org.netbeans.api.debugger.DebuggerManager;
import org.netbeans.api.editor.mimelookup.MimeRegistration;
import org.netbeans.modules.editor.NbEditorUtilities;
import org.netbeans.modules.php.api.util.FileUtils;
import org.netbeans.spi.editor.document.OnSaveTask;
import org.openide.filesystems.FileObject;

/**
 * Save line number on save.
 */
public class UpdatePropertiesOnSaveTask implements OnSaveTask {

    private final Context context;

    private UpdatePropertiesOnSaveTask(Context context) {
        this.context = context;
    }

    @Override
    public void performTask() {
        Document document = context.getDocument();
        FileObject fileObject = NbEditorUtilities.getFileObject(document);
        if (fileObject == null) {
            return;
        }
        String fileUrl = fileObject.toURL().toString();
        DebuggerManager manager = DebuggerManager.getDebuggerManager();
        for (Breakpoint breakpoint : manager.getBreakpoints()) {
            if (breakpoint instanceof LineBreakpoint) {
                LineBreakpoint lineBreakpoint = (LineBreakpoint) breakpoint;
                if (fileUrl.equals(lineBreakpoint.getFileUrl())) {
                    lineBreakpoint.fireLineNumberChanged();
                }
            }
        }
    }

    @Override
    public void runLocked(Runnable run) {
        run.run();
    }

    @Override
    public boolean cancel() {
        return true;
    }

    @MimeRegistration(mimeType = FileUtils.PHP_MIME_TYPE, service = OnSaveTask.Factory.class, position = 1100)
    public static final class FactoryImpl implements Factory {

        @Override
        public OnSaveTask createTask(Context context) {
            return new UpdatePropertiesOnSaveTask(context);
        }
    }
}
