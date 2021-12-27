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

package org.netbeans.modules.maven.api.execute;

import java.awt.Cursor;
import java.awt.EventQueue;
import java.awt.GraphicsEnvironment;
import java.io.File;
import java.util.List;
import javax.swing.JFrame;
import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.api.java.source.BuildArtifactMapper;
import org.netbeans.api.project.Project;
import org.netbeans.modules.maven.api.Constants;
import org.netbeans.modules.maven.execute.BeanRunConfig;
import org.netbeans.modules.maven.execute.MavenCommandLineExecutor;
import org.netbeans.spi.project.AuxiliaryProperties;
import org.openide.execution.ExecutorTask;
import org.openide.util.NbBundle;
import org.openide.windows.WindowManager;

/**
 * Utility method for executing a maven build, using the RunConfig.
 * @author mkleint
 */
public final class RunUtils {
    /** Creates a new instance of RunUtils */
    private RunUtils() {
    }

    /**
     * Runs Maven after checking prerequisites.
     * @param config a run configuration (try {@link #createRunConfig})
     * @return a task to track progress, or null if prerequisites were not satisfied
     * @see #executeMaven
     * @see PrerequisitesChecker
     * @since 2.18
     */
    public static @CheckForNull ExecutorTask run(RunConfig config) {
        invokeLaterWithUI(new Runnable() { //#233275
            @Override
            public void run() {
                JFrame frm = (JFrame) WindowManager.getDefault().getMainWindow();
                frm.getGlassPane().setVisible(true);
                frm.getGlassPane().setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
                frm.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
            }
        });
        try {
            for (PrerequisitesChecker elem : config.getProject().getLookup().lookupAll(PrerequisitesChecker.class)) {
                if (!elem.checkRunConfig(config)) {
                    return null;
                }
                if (config.getPreExecution() != null) {
                    if (!elem.checkRunConfig(config.getPreExecution())) {
                        return null;
                    }
                }
            }
            return executeMaven(config);
        } finally {
            invokeLaterWithUI(new Runnable() { //#233275
                @Override
                public void run() {
                    JFrame frm = (JFrame) WindowManager.getDefault().getMainWindow();
                    frm.getGlassPane().setVisible(false);
                    frm.getGlassPane().setCursor(null);
                    frm.setCursor(null);
                }
            });
        }
    }
    
    /**
     * Execute maven build in NetBeans execution engine.
     * Most callers should rather use {@link #run} as this variant does no (non-late-bound) prerequisite checks.
     * It is mostly suitable for cases where you need full control by the caller over the config, or want to rerun a previous execution.
     * @param config
     * @return 
     */
    public static ExecutorTask executeMaven(final RunConfig config) {
        return MavenCommandLineExecutor.executeMaven(config, null, null);
    }

    public static RunConfig createRunConfig(File execDir, Project prj, String displayName, List<String> goals)
    {
        BeanRunConfig brc = new BeanRunConfig();
        brc.setExecutionName(displayName);
        brc.setExecutionDirectory(execDir);
        brc.setProject(prj);
        brc.setTaskDisplayName(displayName);
        brc.setGoals(goals);
        return brc;
    }
    
    /**
     * return a new instance of runconfig by the template passed as parameter
     * @param original
     * @return 
     * @since 2.40
     */
    public static RunConfig cloneRunConfig(RunConfig original) {
        return new BeanRunConfig(original);
    }

    @NbBundle.Messages({
        "#compile on save: all, none",
        "#NOI18N",
        "DEFAULT_COMPILE_ON_SAVE=all"
    })
    public static boolean isCompileOnSaveEnabled(Project prj) {
        AuxiliaryProperties auxprops = prj.getLookup().lookup(AuxiliaryProperties.class);
        if (auxprops == null) {
            // Cannot use ProjectUtils.getPreferences due to compatibility.
            return false;
        }
        String cos = auxprops.get(Constants.HINT_COMPILE_ON_SAVE, true);
        if (cos == null) {
            cos = Bundle.DEFAULT_COMPILE_ON_SAVE();
        }
        return !"none".equalsIgnoreCase(cos) && BuildArtifactMapper.isCompileOnSaveSupported(); // NOI18N
    }
    
    public static boolean isCompileOnSaveEnabled(RunConfig config) {
        Project prj = config.getProject();
        if (prj != null) {
            return isCompileOnSaveEnabled(prj);
        }
        return false;
    }
    
    /**
     *
     * @param project
     * @return true if compile on save is allowed for running the application.
     */
    @Deprecated
    public static boolean hasApplicationCompileOnSaveEnabled(Project prj) {
        AuxiliaryProperties auxprops = prj.getLookup().lookup(AuxiliaryProperties.class);
        if (auxprops == null) {
            // Cannot use ProjectUtils.getPreferences due to compatibility.
            return false;
        }
        String cos = auxprops.get(Constants.HINT_COMPILE_ON_SAVE, true);
        if (cos == null) {
            cos = "all";
//            String packaging = prj.getLookup().lookup(NbMavenProject.class).getPackagingType();
//            if ("war".equals(packaging) || "ejb".equals(packaging) || "ear".equals(packaging)) {
//                cos = "app";
//            } else {
//                cos = "none";
//            }
        }
        return "all".equalsIgnoreCase(cos) || "app".equalsIgnoreCase(cos);
    }

    /**
     *
     * @param config
     * @return true if compile on save is allowed for running the application.
     */
    @Deprecated 
    public static boolean hasApplicationCompileOnSaveEnabled(RunConfig config) {
        Project prj = config.getProject();
        if (prj != null) {
            return hasApplicationCompileOnSaveEnabled(prj);
        }
        return false;
    }

    /**
     *
     * @param project
     * @return true if compile on save is allowed for running tests.
     */
    @Deprecated
    public static boolean hasTestCompileOnSaveEnabled(Project prj) {
        AuxiliaryProperties auxprops = prj.getLookup().lookup(AuxiliaryProperties.class);
        if (auxprops == null) {
            // Cannot use ProjectUtils.getPreferences due to compatibility.
            return true;
        }
        String cos = auxprops.get(Constants.HINT_COMPILE_ON_SAVE, true);
        if (cos == null) {
            cos = "all";

//            String packaging = prj.getLookup().lookup(NbMavenProject.class).getPackagingType();
//            if ("war".equals(packaging) || "ejb".equals(packaging) || "ear".equals(packaging)) {
//                cos = "app";
//            } else {
//                cos = "none";
//            }
        }
        return "all".equalsIgnoreCase(cos) || "test".equalsIgnoreCase(cos);
    }
    /**
     *
     * @param config
     * @return true if compile on save is allowed for running tests.
     */
    @Deprecated
    public static boolean hasTestCompileOnSaveEnabled(RunConfig config) {
        Project prj = config.getProject();
        if (prj != null) {
            return hasTestCompileOnSaveEnabled(prj);
        }
        return false;
    }

    private static void invokeLaterWithUI(Runnable runnable) {
        if (GraphicsEnvironment.isHeadless()) {
            return;
        }
        EventQueue.invokeLater(runnable);
    }
}
