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
import java.util.logging.Logger;
import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.api.project.Project;
import org.netbeans.modules.css.prep.CssPreprocessorType;
import org.netbeans.modules.css.prep.preferences.SassPreferences;
import org.netbeans.modules.css.prep.sass.SassCli;
import org.netbeans.modules.css.prep.sass.SassCssPreprocessor;
import org.netbeans.modules.css.prep.util.InvalidExternalExecutableException;
import org.netbeans.modules.css.prep.util.UiUtils;
import org.netbeans.modules.css.prep.util.Warnings;
import org.openide.filesystems.FileObject;
import org.openide.util.Pair;

public final class SassProcessor extends BaseProcessor {

    private static final Logger LOGGER = Logger.getLogger(SassProcessor.class.getName());


    public SassProcessor(SassCssPreprocessor cssPreprocessor) {
        super(cssPreprocessor);
    }

    @Override
    protected boolean isEnabledInternal(Project project) {
        return SassPreferences.getInstance().isEnabled(project);
    }

    @Override
    protected boolean isSupportedFile(FileObject fileObject) {
        return CssPreprocessorType.SASS.getFileExtensions().contains(fileObject.getExt());
    }

    @Override
    protected boolean isPartial(FileObject fileObject) {
        return fileObject.getName().startsWith("_"); // NOI18N
    }

    @Override
    protected List<Pair<String, String>> getMappings(Project project) {
        return SassPreferences.getInstance().getMappings(project);
    }

    @Override
    protected String getCompilerOptions(Project project) {
        return SassPreferences.getInstance().getCompilerOptions(project);
    }

    @Override
    protected void compileInternal(Project project, File workDir, File source, File target, List<String> compilerOptions) {
        SassCli sass = getSass(project);
        if (sass == null) {
            return;
        }
        try {
            sass.compile(workDir, source, target, compilerOptions);
        } catch (ExecutionException ex) {
            if (Warnings.showWarning(CssPreprocessorType.SASS)) {
                UiUtils.processExecutionException(ex);
            }
        }
    }

    @CheckForNull
    private SassCli getSass(Project project) {
        try {
            return SassCli.getDefault();
        } catch (InvalidExternalExecutableException ex) {
            cssPreprocessor.fireProcessingErrorOccured(project, ex.getLocalizedMessage());
        }
        return null;
    }

}
