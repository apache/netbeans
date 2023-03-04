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
package org.netbeans.modules.java.api.common.project;

import java.util.Set;
import java.util.function.Supplier;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.project.JavaProjectConstants;
import org.netbeans.api.project.Project;
import org.netbeans.modules.java.api.common.SourceRoots;
import org.netbeans.modules.java.api.common.ant.UpdateHelper;
import org.netbeans.modules.java.api.common.classpath.AbstractClassPathProvider;
import org.netbeans.modules.java.api.common.project.JavaActionProvider.CompileOnSaveOperation;
import org.netbeans.spi.project.ActionProvider;
import org.netbeans.spi.project.SingleMethod;
import org.netbeans.spi.project.support.ant.PropertyEvaluator;
import org.openide.util.Parameters;

/**
 * A builder of {@link ActionProvider} for Multi Module project.
 * @since 1.102
 * @author Tomas Zezula
 */
public final class MultiModuleActionProviderBuilder {

        private final JavaActionProvider.Builder builder;
        private final PropertyEvaluator evaluator;

        private MultiModuleActionProviderBuilder(
                @NonNull final JavaActionProvider.Builder builder,
                @NonNull final PropertyEvaluator evaluator) {
            Parameters.notNull("builder", builder); //NOI18N
            Parameters.notNull("evaluator", evaluator); //NOI18N
            this.builder = builder;
            this.evaluator = evaluator;
        }

    @NonNull
    private MultiModuleActionProviderBuilder addProjectSensitiveActions() {
        builder.addAction(builder.createDefaultScriptAction(ActionProvider.COMMAND_CLEAN, false, false, true, "clean")); //NOI18N
        builder.addAction(builder.createDefaultScriptAction(ActionProvider.COMMAND_BUILD, false, false,  true, ActionProviderSupport.createConditionalTarget(
                                    evaluator,
                                    ActionProviderSupport.createJarEnabledPredicate(),
                                    new String[] {"jar"},   //NOI18N
                                    new String[] {"compile"})));    //NOI18N
        builder.addAction(builder.createDefaultScriptAction(ActionProvider.COMMAND_REBUILD, false, false, true, ActionProviderSupport.createConditionalTarget(
                                    evaluator,
                                    ActionProviderSupport.createJarEnabledPredicate(),
                                    new String[] {"clean", "jar"},  //NOI18N
                                    new String[] {"clean", "compile"})));   //NOI18N
        builder.addAction(builder.createDefaultScriptAction(ActionProvider.COMMAND_RUN, false, true, true, "run"));  //NOI18N
        builder.addAction(builder.createDefaultScriptAction(ActionProvider.COMMAND_DEBUG, false, true, true, "debug"));  //NOI18N
        builder.addAction(builder.createDefaultScriptAction(ActionProvider.COMMAND_PROFILE, false, false, true, "profile"));  //NOI18N
        builder.addAction(builder.createDefaultScriptAction(ActionProvider.COMMAND_TEST, false, false, true, "test"));   //NOI18N
        builder.addAction(builder.createDefaultScriptAction(ActionProvider.COMMAND_COMPILE_SINGLE, false, false, false, "compile-single"));   //NOI18N
        builder.addAction(builder.createDefaultScriptAction(ActionProvider.COMMAND_RUN_SINGLE, false, true, true, "run-single"));   //NOI18N
        builder.addAction(builder.createDefaultScriptAction(ActionProvider.COMMAND_DEBUG_SINGLE, false, true, true, "debug-single"));   //NOI18N
        builder.addAction(builder.createDefaultScriptAction(ActionProvider.COMMAND_PROFILE_SINGLE, false, false, true, "profile-single"));   //NOI18N
        builder.addAction(builder.createDefaultScriptAction(ActionProvider.COMMAND_TEST_SINGLE, false, false, true, "test-single"));   //NOI18N
        builder.addAction(builder.createDefaultScriptAction(ActionProvider.COMMAND_DEBUG_TEST_SINGLE, false, false, true, "debug-test"));   //NOI18N
        builder.addAction(builder.createDefaultScriptAction(ActionProvider.COMMAND_PROFILE_TEST_SINGLE, false, false, true, "profile-test"));   //NOI18N
        builder.addAction(builder.createDefaultScriptAction(SingleMethod.COMMAND_RUN_SINGLE_METHOD, false, true, true, "test-single-method"));   //NOI18N
        builder.addAction(builder.createDefaultScriptAction(SingleMethod.COMMAND_DEBUG_SINGLE_METHOD, false, true, true, "debug-single-method"));   //NOI18N
        builder.addAction(builder.createDefaultScriptAction(JavaProjectConstants.COMMAND_DEBUG_FIX, false, true, false, "debug-fix"));   //NOI18N
        return this;
    }

    @NonNull
    private MultiModuleActionProviderBuilder addProjectOperationsActions(String... commands) {
        for (String command : commands) {
            builder.addAction(builder.createProjectOperation(command));
        }
        return this;
    }

    @NonNull
    public MultiModuleActionProviderBuilder setCompileOnSaveOperationsProvider(@NonNull final Supplier<? extends Set<? extends CompileOnSaveOperation>> cosOpsProvider) {
        builder.setCompileOnSaveOperationsProvider(cosOpsProvider);
        return this;
    }

    @NonNull
    public JavaActionProvider build() {
        addProjectOperationsActions(
                ActionProvider.COMMAND_DELETE,
                ActionProvider.COMMAND_MOVE,
                ActionProvider.COMMAND_COPY,
                ActionProvider.COMMAND_RENAME);
        addProjectSensitiveActions();
        return builder.build();
    }

    @NonNull
    public static MultiModuleActionProviderBuilder newInstance(
            @NonNull final Project project,
            @NonNull final UpdateHelper updateHelper,
            @NonNull final PropertyEvaluator evaluator,
            @NonNull final SourceRoots sourceRoots,
            @NonNull final SourceRoots testSourceRoots,
            @NonNull final AbstractClassPathProvider cpp) {
        return new MultiModuleActionProviderBuilder(JavaActionProvider.Builder.newInstance(
                project,
                updateHelper,
                evaluator,
                sourceRoots,
                testSourceRoots,
                (id) -> {
                    final ClassPath[] cps = cpp.getProjectClassPaths(id);
                    if (cps == null || cps.length < 1) {
                        return null;
                    }
                    return cps[0];
                }), evaluator);
    }
}
