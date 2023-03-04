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
package org.netbeans.modules.css.prep.ui.less;

import org.netbeans.api.project.Project;
import org.netbeans.modules.css.prep.CssPreprocessorType;
import org.netbeans.modules.css.prep.less.LessCssPreprocessor;
import org.netbeans.modules.css.prep.problems.LessProjectProblemsProvider;
import org.netbeans.modules.css.prep.ui.customizer.CustomizerImpl;
import org.netbeans.modules.css.prep.ui.options.LessOptions;
import org.netbeans.modules.web.common.api.CssPreprocessors;
import org.netbeans.modules.web.common.ui.spi.CssPreprocessorUIImplementation;
import org.netbeans.spi.project.ui.ProjectProblemsProvider;
import org.openide.util.lookup.Lookups;
import org.openide.util.lookup.ServiceProvider;

@ServiceProvider(service = CssPreprocessorUIImplementation.class, path = CssPreprocessors.PREPROCESSORS_PATH, position = 400)
public final class LessCssPreprocessorUI implements CssPreprocessorUIImplementation {

    @Override
    public String getIdentifier() {
        return LessCssPreprocessor.IDENTIFIER;
    }

    private LessCssPreprocessor getProcessorInstance() {
        return Lookups.forPath(CssPreprocessors.PREPROCESSORS_PATH).lookup(LessCssPreprocessor.class);
    }

    @Override
    public Customizer createCustomizer(Project project) {
        return new CustomizerImpl(getProcessorInstance(), project, CssPreprocessorType.LESS);
    }

    @Override
    public ProjectProblemsProvider createProjectProblemsProvider(Project project) {
        return new LessProjectProblemsProvider(project);
    }

    @Override
    public Options createOptions() {
        return new LessOptions(getProcessorInstance());
    }

}
