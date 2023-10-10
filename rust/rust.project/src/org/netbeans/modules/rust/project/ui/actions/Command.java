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
package org.netbeans.modules.rust.project.ui.actions;

import java.util.logging.Logger;
import org.netbeans.modules.rust.project.RustProject;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Lookup;


public abstract class Command {

    protected final Logger logger = Logger.getLogger(getClass().getName());

    private final RustProject project;

    public Command(RustProject project) {
        assert project != null;
        this.project = project;
    }

    public abstract String getCommandId();

    public abstract boolean isActionEnabledInternal(Lookup context);

    public abstract void invokeActionInternal(Lookup context);

    public final boolean isActionEnabled(Lookup context) {
        // PHP checks for terminally broken projects first, might be helpful for
        // Rust too
        return isActionEnabledInternal(context);
    }

    public final void invokeAction(Lookup context) {
        if (!validateInvokeAction(context)) {
            return;
        }
        invokeActionInternal(context);
    }

    protected boolean validateInvokeAction(Lookup context) {
        // PHP checks for terminally broken projects, might be helpful for
        // Rust too
        return true;
    }

    public boolean asyncCallRequired() {
        return true;
    }

    public boolean saveRequired() {
        return true;
    }

    public boolean isFileSensitive() {
        return false;
    }

    public final RustProject getProject() {
        return project;
    }

    protected boolean isTestFile(FileObject fileObj) {
        if (fileObj == null) {
            return false;
        }
        if (!fileObj.isData()) {
            return false;
        }
        FileObject testFolder = project
                .getProjectDirectory()
                .getFileObject("test");
        if (testFolder == null) {
            return false;
        }
        return  FileUtil.isParentOf(testFolder, fileObj);
    }
}
