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
package org.netbeans.modules.css.prep.process;

import java.io.File;
import java.util.List;
import java.util.concurrent.ExecutionException;
import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.api.project.Project;
import org.netbeans.modules.css.prep.CssPreprocessorType;
import org.netbeans.modules.css.prep.less.LessCssPreprocessor;
import org.netbeans.modules.css.prep.less.LessExecutable;
import org.netbeans.modules.css.prep.preferences.LessPreferences;
import org.netbeans.modules.css.prep.util.InvalidExternalExecutableException;
import org.netbeans.modules.css.prep.util.UiUtils;
import org.netbeans.modules.css.prep.util.Warnings;
import org.openide.filesystems.FileObject;
import org.openide.util.Pair;

public final class LessProcessor extends BaseProcessor {


    public LessProcessor(LessCssPreprocessor cssPreprocessor) {
        super(cssPreprocessor);
    }

    @Override
    protected boolean isEnabledInternal(Project project) {
        return LessPreferences.getInstance().isEnabled(project);
    }

    @Override
    protected boolean isSupportedFile(FileObject fileObject) {
        return CssPreprocessorType.LESS.getFileExtensions().contains(fileObject.getExt());
    }

    @Override
    protected boolean isPartial(FileObject fileObject) {
        // less does not support partials
        return false;
    }

    @Override
    protected List<Pair<String, String>> getMappings(Project project) {
        return LessPreferences.getInstance().getMappings(project);
    }

    @Override
    protected String getCompilerOptions(Project project) {
        return LessPreferences.getInstance().getCompilerOptions(project);
    }

    @Override
    protected void compileInternal(Project project, File workDir, File source, File target, List<String> compilerOptions) {
        LessExecutable less = getLess(project);
        if (less == null) {
            return;
        }
        try {
            less.compile(workDir, source, target, compilerOptions);
        } catch (ExecutionException ex) {
            if (Warnings.showWarning(CssPreprocessorType.LESS)) {
                UiUtils.processExecutionException(ex);
            }
        }
    }

    @CheckForNull
    private LessExecutable getLess(Project project) {
        try {
            return LessExecutable.getDefault();
        } catch (InvalidExternalExecutableException ex) {
            cssPreprocessor.fireProcessingErrorOccured(project, ex.getLocalizedMessage());
        }
        return null;
    }

}
