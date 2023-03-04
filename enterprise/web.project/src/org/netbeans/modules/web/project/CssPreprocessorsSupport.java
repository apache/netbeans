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
package org.netbeans.modules.web.project;

import org.netbeans.api.project.Project;
import org.netbeans.modules.web.common.api.CssPreprocessor;
import org.netbeans.modules.web.common.api.CssPreprocessors;
import org.netbeans.modules.web.common.api.CssPreprocessorsListener;
import org.openide.filesystems.FileObject;

public class CssPreprocessorsSupport implements CssPreprocessorsListener {

    private WebProject p;

    public CssPreprocessorsSupport(WebProject p) {
        this.p = p;
    }

    public void recompileSources(CssPreprocessor cssPreprocessor) {
        assert cssPreprocessor != null;
        FileObject docBase = p.getAPIWebModule().getDocumentBase();
        if (docBase == null) {
            return;
        }
        // force recompiling
        CssPreprocessors.getDefault().process(cssPreprocessor, p, docBase);
    }

    @Override
    public void preprocessorsChanged() {
    }

    @Override
    public void optionsChanged(CssPreprocessor cssPreprocessor) {
        recompileSources(cssPreprocessor);
    }

    @Override
    public void customizerChanged(Project project, CssPreprocessor cssPreprocessor) {
        if (project.equals(p)) {
            recompileSources(cssPreprocessor);
        }
    }

    @Override
    public void processingErrorOccured(Project project, CssPreprocessor cssPreprocessor, String error) {
    }

}
