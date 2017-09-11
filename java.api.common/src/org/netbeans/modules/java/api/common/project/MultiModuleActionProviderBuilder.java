/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2017 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 *
 * Contributor(s):
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
