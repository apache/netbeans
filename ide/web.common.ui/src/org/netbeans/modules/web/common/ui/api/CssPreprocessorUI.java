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
package org.netbeans.modules.web.common.ui.api;

import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.project.Project;
import org.netbeans.modules.web.common.ui.cssprep.CssPreprocessorAccessor;
import org.netbeans.modules.web.common.ui.spi.CssPreprocessorUIImplementation;
import org.netbeans.spi.project.ui.ProjectProblemsProvider;
import org.openide.util.Parameters;

/**
 * The API representation of a single CSS preprocessor.
 * @since 1.0
 */
public final class CssPreprocessorUI {

    private final CssPreprocessorUIImplementation delegate;

    static {
        CssPreprocessorAccessor.setDefault(new CssPreprocessorAccessor() {
            @Override
            public CssPreprocessorUI create(CssPreprocessorUIImplementation cssPreprocessorImplementation) {
                return new CssPreprocessorUI(cssPreprocessorImplementation);
            }
            @Override
            public CssPreprocessorUIImplementation.Customizer createCustomizer(CssPreprocessorUI cssPreprocessor, Project project) {
                return cssPreprocessor.createCustomizer(project);
            }

            @Override
            public CssPreprocessorUIImplementation.Options createOptions(CssPreprocessorUI cssPreprocessor) {
                return cssPreprocessor.createOptions();
            }
            @Override
            public ProjectProblemsProvider createProjectProblemsProvider(CssPreprocessorUI cssPreprocessor, Project project) {
                return cssPreprocessor.createProjectProblemsProvider(project);
            }
        });
    }

    private CssPreprocessorUI(CssPreprocessorUIImplementation delegate) {
        this.delegate = delegate;
    }

    CssPreprocessorUIImplementation getDelegate() {
        return delegate;
    }

    CssPreprocessorUIImplementation.Customizer createCustomizer(@NonNull Project project) {
        return delegate.createCustomizer(project);
    }

    CssPreprocessorUIImplementation.Options createOptions() {
        return delegate.createOptions();
    }

    ProjectProblemsProvider createProjectProblemsProvider(@NonNull Project project) {
        Parameters.notNull("project", project); // NOI18N
        return delegate.createProjectProblemsProvider(project);
    }

    /**
     * Return the <b>non-localized (usually english)</b> identifier of this CSS preprocessor.
     * @return the <b>non-localized (usually english)</b> identifier; never {@code null}
     */
    @NonNull
    public String getIdentifier() {
        String identifier = delegate.getIdentifier();
        Parameters.notNull("identifier", identifier); // NOI18N
        return identifier;
    }

}
