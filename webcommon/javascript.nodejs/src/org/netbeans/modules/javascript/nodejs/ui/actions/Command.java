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
package org.netbeans.modules.javascript.nodejs.ui.actions;

import java.awt.EventQueue;
import java.io.File;
import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.api.project.Project;
import org.netbeans.modules.javascript.nodejs.exec.NodeExecutable;
import org.netbeans.modules.javascript.nodejs.ui.customizer.NodeJsCustomizerProvider;
import org.netbeans.modules.javascript.nodejs.util.FileUtils;
import org.netbeans.modules.javascript.nodejs.util.RunInfo;
import org.netbeans.modules.web.common.api.ValidationResult;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Lookup;

abstract class Command {

    protected final Project project;


    Command(Project project) {
        assert project != null;
        this.project = project;
    }

    public abstract String getCommandId();

    public abstract boolean isEnabled(Lookup context);

    abstract void runInternal(Lookup context);

    abstract ValidationResult validateRunInfo(RunInfo runInfo);

    public void run(Lookup context) {
        assert !EventQueue.isDispatchThread();
        runInternal(context);
    }

    @CheckForNull
    protected NodeExecutable getNode() {
        return NodeExecutable.forProject(project, true);
    }

    @CheckForNull
    protected RunInfo getRunInfo() {
        RunInfo runInfo = new RunInfo(project);
        ValidationResult result = validateRunInfo(runInfo);
        if (!result.isFaultless()) {
            NodeJsCustomizerProvider.openCustomizer(project, result);
            return null;
        }
        return runInfo;
    }

    @CheckForNull
    protected FileObject lookupFileObject(Lookup context) {
        return context.lookup(FileObject.class);
    }

    @CheckForNull
    protected File lookupFile(Lookup context) {
        FileObject fo = lookupFileObject(context);
        if (fo == null) {
            return null;
        }
        File file = FileUtil.toFile(fo);
        assert file != null : fo;
        return file;
    }

    @CheckForNull
    protected File lookupJavaScriptFile(Lookup context) {
        FileObject file = lookupFileObject(context);
        if (file == null) {
            return null;
        }
        if (!FileUtils.isJavaScriptFile(file)) {
            return null;
        }
        return FileUtil.toFile(file);
    }

}
