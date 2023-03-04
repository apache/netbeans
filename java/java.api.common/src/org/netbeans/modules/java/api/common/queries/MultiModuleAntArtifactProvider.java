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
package org.netbeans.modules.java.api.common.queries;

import java.io.File;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Properties;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.java.project.JavaProjectConstants;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ant.AntArtifact;
import org.netbeans.modules.java.api.common.impl.MultiModule;
import org.netbeans.modules.java.api.common.project.ProjectProperties;
import org.netbeans.spi.project.ant.AntArtifactProvider;
import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.netbeans.spi.project.support.ant.PropertyEvaluator;
import org.netbeans.spi.project.support.ant.PropertyUtils;
import org.openide.util.Parameters;

/**
 *
 * @author Tomas Zezula
 */
final class MultiModuleAntArtifactProvider implements AntArtifactProvider {

    private final AntProjectHelper helper;
    private final PropertyEvaluator eval;
    private final MultiModule srcModules;
    private final String buildTarget;
    private final String cleanTarget;

    MultiModuleAntArtifactProvider(
            @NonNull final AntProjectHelper helper,
            @NonNull final PropertyEvaluator eval,
            @NonNull final MultiModule model,
            @NonNull final String buildTarget,
            @NonNull final String cleanTarget) {
        Parameters.notNull("helper", helper);   //NOI18N
        Parameters.notNull("eval", eval);   //NOI18N
        Parameters.notNull("model", model); //NOI18N
        Parameters.notNull("buildTarget", buildTarget); //NOI18N
        Parameters.notNull("cleanTarget", cleanTarget); //NOI18N
        this.helper = helper;
        this.eval = eval;
        this.srcModules = model;
        this.buildTarget = buildTarget;
        this.cleanTarget = cleanTarget;
    }

    @Override
    public AntArtifact[] getBuildArtifacts() {
        final List<AntArtifact> artifacts = new ArrayList<>();
        final EvaluatorPropertyProvider pp = new EvaluatorPropertyProvider(eval);
        for (String modName : srcModules.getModuleNames()) {
            final String moduleDistJarKey = String.format(
                    "module.%s.dist.jar",   //NOI18N
                    modName);
            final String moduleDistJarVal = String.format(
                    "${%s}/%s.jar", //NOI18N
                    ProjectProperties.DIST_DIR,
                    modName);
            final PropertyEvaluator extEval = PropertyUtils.sequentialPropertyEvaluator(
                    null,
                    pp,
                    PropertyUtils.fixedPropertyProvider(Collections.singletonMap(
                            moduleDistJarKey,
                            moduleDistJarVal)));
            final AntArtifact artifact = helper.createSimpleAntArtifact(
                    JavaProjectConstants.ARTIFACT_TYPE_JAR,
                    moduleDistJarKey,
                    extEval,
                    buildTarget,
                    cleanTarget,
                    ProjectProperties.BUILD_SCRIPT);
            artifacts.add(new ModuleIdDecorator(modName, artifact));
        }
        return artifacts.toArray(new AntArtifact[artifacts.size()]);
    }

    private static final class ModuleIdDecorator extends AntArtifact {
        private final String moduleName;
        private final AntArtifact delegate;

        ModuleIdDecorator(
                String moduleName,
                AntArtifact delegate) {
            Parameters.notNull("moduleName", moduleName);   //NOI18N
            Parameters.notNull("delegate", delegate);   //NOI18N
            this.moduleName = moduleName;
            this.delegate = delegate;
        }

        @Override
        public String getType() {
            return delegate.getType();
        }

        @Override
        public File getScriptLocation() {
            return delegate.getScriptLocation();
        }

        @Override
        public String getTargetName() {
            return delegate.getTargetName();
        }

        @Override
        public String getCleanTargetName() {
            return delegate.getCleanTargetName();
        }

        @Override
        public URI[] getArtifactLocations() {
            return delegate.getArtifactLocations();
        }

        @Override
        public URI getArtifactLocation() {
            return delegate.getArtifactLocation();
        }

        @Override
        public Project getProject() {
            return delegate.getProject();
        }

        @Override
        public Properties getProperties() {
            return delegate.getProperties();
        }

        @Override
        public String getID() {
            return String.format(
                    "%s.%s",    //NOI18N
                    moduleName,
                    super.getID());
        }
    }
}
