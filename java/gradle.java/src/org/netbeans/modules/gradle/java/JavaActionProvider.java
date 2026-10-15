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

package org.netbeans.modules.gradle.java;

import org.netbeans.modules.gradle.api.GradleBaseProject;
import org.netbeans.modules.gradle.api.execute.RunUtils;
import org.netbeans.modules.gradle.java.api.GradleJavaProject;
import org.netbeans.modules.gradle.java.api.GradleJavaSourceSet;
import org.netbeans.modules.gradle.spi.actions.DefaultGradleActionsProvider;
import org.netbeans.modules.gradle.spi.actions.GradleActionsProvider;
import static org.netbeans.modules.gradle.java.api.GradleJavaSourceSet.SourceType.*;
import java.io.File;
import java.util.Set;
import static org.netbeans.spi.project.ActionProvider.*;
import static org.netbeans.api.java.project.JavaProjectConstants.*;
import org.netbeans.api.java.source.ClasspathInfo;
import org.netbeans.api.java.source.SourceUtils;
import org.netbeans.api.project.Project;
import org.netbeans.api.queries.FileBuiltQuery;
import org.netbeans.modules.gradle.api.execute.ActionMapping;
import org.netbeans.modules.gradle.api.execute.GradleCommandLine;
import org.netbeans.modules.gradle.spi.actions.ProjectActionMappingProvider;
import static org.netbeans.spi.project.SingleMethod.COMMAND_DEBUG_SINGLE_METHOD;
import static org.netbeans.spi.project.SingleMethod.COMMAND_RUN_SINGLE_METHOD;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Lookup;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author Laszlo Kishalmi
 */
@ServiceProvider(service = GradleActionsProvider.class)
public class JavaActionProvider extends DefaultGradleActionsProvider {

    private static final String GATLING_PLUGIN = "com.github.lkishalmi.gatling"; //NOI18N
    private static final String SIMULATION_POSTFIX = "Simulation.scala"; //NOI18N

    /**
     * Name of the 'download.sources' standard action
     */
    public static final String COMMAND_DL_SOURCES = "download.sources"; //NOI18N

    /**
     * Name of the 'download.javadoc' standard action
     */
    public static final String COMMAND_DL_JAVADOC = "download.javadoc"; //NOI18N


    private static final String[] SUPPORTED = new String[]{
        COMMAND_BUILD,
        COMMAND_CLEAN,
        COMMAND_REBUILD,
        COMMAND_RUN,
        COMMAND_DEBUG,
        COMMAND_TEST,
        COMMAND_TEST_PARALLEL,
        COMMAND_TEST_SINGLE,
        COMMAND_DEBUG_TEST_SINGLE,
        COMMAND_RUN_SINGLE_METHOD,
        COMMAND_DEBUG_SINGLE_METHOD,
        COMMAND_JAVADOC,
        COMMAND_DEBUG_FIX,
        COMMAND_RUN_SINGLE,
        COMMAND_DEBUG_SINGLE,
        COMMAND_COMPILE_SINGLE,
        COMMAND_DELETE,
        
        COMMAND_DL_JAVADOC,
        COMMAND_DL_SOURCES
    };

    public JavaActionProvider() {
        super(SUPPORTED);
    }

    @Override
    public boolean isActionEnabled(String action, Project project, Lookup context) {
        boolean ret = super.isActionEnabled(action, project, context);
        if (ret) {
            GradleBaseProject gbp = GradleBaseProject.get(project);
            FileObject fo = RunUtils.extractFileObjectfromLookup(context);
            if (fo != null) {
                if (gbp.hasPlugins(GATLING_PLUGIN) && COMMAND_RUN_SINGLE.equals(action)) {
                    ret = fo.getNameExt().endsWith(SIMULATION_POSTFIX);
                } else {
                    GradleJavaProject gjp = GradleJavaProject.get(project);
                    if ( gjp != null ) {
                        ret = false;
                        switch (action) {
                            case COMMAND_COMPILE_SINGLE:
                                FileBuiltQuery.Status status = FileBuiltQuery.getStatus(fo);
                                ret = status == null || !status.isBuilt();
                                break;
                            case COMMAND_DEBUG_SINGLE:
                            case COMMAND_RUN_SINGLE:
                                ProjectActionMappingProvider pamp = RunUtils.findActionProvider(project, context);
                                ActionMapping runSingleMapping = pamp.findMapping(action);
                                if (ActionMapping.isDisabled(runSingleMapping)) {
                                    return false;
                                }
                                GradleCommandLine cli = new GradleCommandLine(RunUtils.evaluateActionArgs(project, action, runSingleMapping.getArgs(), context));
                                Set<String> runSingleTasks = cli.getTasks();
                                if (gbp.getTaskNames().containsAll(runSingleTasks) || RunUtils.isAugmentedBuildEnabled(project)) {
                                    File f = FileUtil.toFile(fo);
                                    GradleJavaSourceSet sourceSet = gjp.containingSourceSet(f);
                                    if ((sourceSet != null) && fo.isData()) {
                                        String relPath = sourceSet.relativePath(f);
                                        if (relPath != null) {
                                            relPath = relPath.substring(0, relPath.lastIndexOf('.')).replace('/', '.');
                                            ret = SourceUtils.isMainClass(relPath, ClasspathInfo.create(fo), true);
                                        }
                                    }
                                }
                                break;

                            case COMMAND_TEST_SINGLE:
                            case COMMAND_DEBUG_TEST_SINGLE:
                            case COMMAND_RUN_SINGLE_METHOD:
                            case COMMAND_DEBUG_SINGLE_METHOD:
                                if ("text/x-java".equals(fo.getMIMEType()) || "text/x-groovy".equals(fo.getMIMEType()) || "text/x-kotlin".equals(fo.getMIMEType())) { //NOI18N
                                    File f = FileUtil.toFile(fo);
                                    GradleJavaSourceSet sourceSet = gjp.containingSourceSet(f);
                                    ret = sourceSet != null && sourceSet.isTestSourceSet() && sourceSet.getSourceType(f) != RESOURCES;
                                }
                                if ( fo.isFolder() ) {
                                    File dir = FileUtil.toFile(fo);
                                    GradleJavaSourceSet sourceSet = gjp.containingSourceSet(dir);
                                    ret = sourceSet != null && sourceSet.getSourceType(dir) != RESOURCES;
                                }
                                break;
                            default:
                                ret = true;
                        }
                    }
                }
            }
        }
        return ret;
    }

}
