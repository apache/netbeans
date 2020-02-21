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
package org.netbeans.modules.cnd.discovery.api;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.modules.cnd.api.project.IncludePath;
import org.netbeans.modules.cnd.api.remote.RemoteFileUtil;
import org.netbeans.modules.cnd.api.toolchain.CompilerSet;
import org.netbeans.modules.cnd.api.toolchain.PredefinedToolKind;
import org.netbeans.modules.cnd.utils.CndPathUtilities;
import org.netbeans.modules.cnd.makeproject.api.configurations.MakeConfiguration;
import org.netbeans.modules.cnd.makeproject.api.configurations.QmakeConfiguration;
import org.netbeans.modules.cnd.api.toolchain.Tool;
import org.netbeans.modules.cnd.discovery.wizard.api.support.ProjectBridge;
import org.netbeans.modules.cnd.dwarfdump.source.Artifacts;
import org.netbeans.modules.cnd.dwarfdump.source.CompileLineOrigin;
import org.netbeans.modules.cnd.dwarfdump.source.Driver;
import org.netbeans.modules.cnd.utils.FSPath;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;
import org.netbeans.modules.nativeexecution.api.HostInfo;
import org.netbeans.modules.nativeexecution.api.NativeProcess;
import org.netbeans.modules.nativeexecution.api.NativeProcessBuilder;
import org.netbeans.modules.nativeexecution.api.util.ConnectionManager;
import org.netbeans.modules.nativeexecution.api.util.HostInfoUtils;
import org.netbeans.modules.nativeexecution.api.util.ProcessUtils;
import org.netbeans.modules.remote.spi.FileSystemProvider;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileSystem;
import org.openide.util.Exceptions;
import org.openide.util.Pair;

/**
 * Utility to find Qt include directories for project configuration.
 *
 */
public abstract class QtInfoProvider {

    private static final QtInfoProvider DEFAULT = new Default();
    private static final Logger LOGGER = Logger.getLogger(QtInfoProvider.class.getName());

    private QtInfoProvider() {
    }

    public static QtInfoProvider getDefault() {
        return DEFAULT;
    }

    public abstract List<String> getQtAdditionalMacros(MakeConfiguration conf);

    public abstract List<IncludePath> getQtIncludeDirectories(MakeConfiguration conf);

    private static class Default extends QtInfoProvider {

        private final Map<String, Pair<String, String>> cache;

        private Default() {
            cache = new HashMap<>();
        }

        @Override
        public List<String> getQtAdditionalMacros(MakeConfiguration conf) {
            final String CXXFLAGS = "CXXFLAGS"; //NOI18N
            Map<String, String> vars = new TreeMap<>();
            FileObject projectDir = conf.getBaseFSPath().getFileObject();
            if (projectDir != null && projectDir.isValid()) {
                try {
                    FileObject qtMakeFile = RemoteFileUtil.getFileObject(projectDir, MakeConfiguration.NBPROJECT_FOLDER + "/qt-" + conf.getName() + ".mk"); //NOI18N
                    Project project = ProjectManager.getDefault().findProject(projectDir);
                    if (project != null && qtMakeFile != null && qtMakeFile.isValid()) {
                        try (BufferedReader reader = new BufferedReader(new InputStreamReader(qtMakeFile.getInputStream()))) {
                            String str;
                            while ((str = reader.readLine()) != null) {
                                String[] lines = str.split("="); //NOI18N
                                if (lines.length == 2) {
                                    String key = lines[0].trim();
                                    vars.put(lines[0].trim(), lines[1].trim());
                                    if (key.equals(CXXFLAGS)) {
                                        ProjectBridge projectBridge = new ProjectBridge(project);
                                        Driver driver = DriverFactory.getDriver(projectBridge.getCompilerSet());
                                        Artifacts artifacts = driver.gatherCompilerLine(getActualVarValue(vars, CXXFLAGS), CompileLineOrigin.BuildLog, true);
                                        List<String> result = new ArrayList<>(artifacts.getUserMacros().size());
                                        for (Map.Entry<String, String> pair : artifacts.getUserMacros().entrySet()) {
                                            if (pair.getValue() == null) {
                                                result.add(pair.getKey());
                                            } else {
                                                result.add(pair.getKey() + "=" + pair.getValue()); //NOI18N
                                            }
                                        }
                                        return result;
                                    }
                                }
                            }
                        }
                    }
                } catch (IOException | IllegalArgumentException ex) {
                    Exceptions.printStackTrace(ex);
                }
            }
            return java.util.Collections.emptyList();
        }

        private String getActualVarValue(Map<String, String> vars, String var) {
            String result = vars.get(var);
            for (String v : vars.keySet()) {
                result = result.replace("$(" + v + ")", vars.get(v)); //NOI18N
            }
            return result;
        }

        /**
         * Finds Qt include directories for given project configuration.
         *
         * @param conf Qt project configuration
         * @return list of include directories, may be empty if qmake is not
         * found
         */
        @Override
        public List<IncludePath> getQtIncludeDirectories(MakeConfiguration conf) {
            ExecutionEnvironment execEnv = conf.getDevelopmentHost().getExecutionEnvironment();
            FileSystem systemFS = FileSystemProvider.getFileSystem(execEnv);
            FileSystem projectFS = conf.getBaseFSPath().getFileSystem();
            char separator = FileSystemProvider.getFileSeparatorChar(execEnv);
            Pair<String, String> baseDir = getBaseQtIncludeDir(conf);
            List<IncludePath> result;
            if (baseDir != null && (baseDir.first() != null || baseDir.second() != null)) {
                result = new ArrayList<>();
                if (baseDir.first() != null) {
                    result.add(new IncludePath(systemFS, baseDir.first()));
                }
                QmakeConfiguration qmakeConfiguration = conf.getQmakeConfiguration();
                if (qmakeConfiguration.isCoreEnabled().getValue()) {
                    if (baseDir.second() != null) {
                        result.add(new IncludePath(systemFS, baseDir.second() + separator + "QtCore.framework/Headers")); // NOI18N
                    }
                    if (baseDir.first() != null) {
                        result.add(new IncludePath(systemFS, baseDir.first() + separator + "QtCore")); // NOI18N
                    }
                }
                if (qmakeConfiguration.isWidgetsEnabled().getValue()) {
                    if (baseDir.second() != null) {
                        result.add(new IncludePath(systemFS, baseDir.second() + separator + "QtWidgets.framework/Headers")); // NOI18N
                    }
                    if (baseDir.first() != null) {
                        result.add(new IncludePath(systemFS, baseDir.first() + separator + "QtWidgets")); // NOI18N
                    }
                }
                if (qmakeConfiguration.isGuiEnabled().getValue()) {
                    if (baseDir.second() != null) {
                        result.add(new IncludePath(systemFS, baseDir.second() + separator + "QtGui.framework/Headers")); // NOI18N
                    }
                    if (baseDir.first() != null) {
                        result.add(new IncludePath(systemFS, baseDir.first() + separator + "QtGui")); // NOI18N
                    }
                }
                if (qmakeConfiguration.isNetworkEnabled().getValue()) {
                    if (baseDir.second() != null) {
                        result.add(new IncludePath(systemFS, baseDir.second() + separator + "QtNetwork.framework/Headers")); // NOI18N
                    }
                    if (baseDir.first() != null) {
                        result.add(new IncludePath(systemFS, baseDir.first() + separator + "QtNetwork")); // NOI18N
                    }
                }
                if (qmakeConfiguration.isOpenglEnabled().getValue()) {
                    if (baseDir.second() != null) {
                        result.add(new IncludePath(systemFS, baseDir.second() + separator + "QtOpenGL.framework/Headers")); // NOI18N
                    }
                    if (baseDir.first() != null) {
                        result.add(new IncludePath(systemFS, baseDir.first() + separator + "QtOpenGL")); // NOI18N
                    }
                }
                if (qmakeConfiguration.isPhononEnabled().getValue()) {
                    if (baseDir.second() != null) {
                        result.add(new IncludePath(systemFS, baseDir.second() + separator + "phonon.framework/Headers")); // NOI18N
                    }
                    if (baseDir.first() != null) {
                        result.add(new IncludePath(systemFS, baseDir.first() + separator + "phonon")); // NOI18N
                    }
                }
                if (qmakeConfiguration.isQt3SupportEnabled().getValue()) {
                    if (baseDir.second() != null) {
                        result.add(new IncludePath(systemFS, baseDir.second() + separator + "Qt3Support.framework/Headers")); // NOI18N
                    }
                    if (baseDir.first() != null) {
                        result.add(new IncludePath(systemFS, baseDir.first() + separator + "Qt3Support")); // NOI18N
                    }
                }
                if (qmakeConfiguration.isPrintSupportEnabled().getValue()) {
                    if (baseDir.second() != null) {
                        result.add(new IncludePath(systemFS, baseDir.second() + separator + "QtPrintSupport.framework/Headers")); // NOI18N
                    }
                    if (baseDir.first() != null) {
                        result.add(new IncludePath(systemFS, baseDir.first() + separator + "QtPrintSupport")); // NOI18N
                    }
                }
                if (qmakeConfiguration.isSqlEnabled().getValue()) {
                    if (baseDir.second() != null) {
                        result.add(new IncludePath(systemFS, baseDir.second() + separator + "QtSql.framework/Headers")); // NOI18N
                    }
                    if (baseDir.first() != null) {
                        result.add(new IncludePath(systemFS, baseDir.first() + separator + "QtSql")); // NOI18N
                    }
                }
                if (qmakeConfiguration.isSvgEnabled().getValue()) {
                    if (baseDir.second() != null) {
                        result.add(new IncludePath(systemFS, baseDir.second() + separator + "QtSvg.framework/Headers")); // NOI18N
                    }
                    if (baseDir.first() != null) {
                        result.add(new IncludePath(systemFS, baseDir.first() + separator + "QtSvg")); // NOI18N
                    }
                }
                if (qmakeConfiguration.isXmlEnabled().getValue()) {
                    if (baseDir.second() != null) {
                        result.add(new IncludePath(systemFS, baseDir.second() + separator + "QtXml.framework/Headers")); // NOI18N
                    }
                    if (baseDir.first() != null) {
                        result.add(new IncludePath(systemFS, baseDir.first() + separator + "QtXml")); // NOI18N
                    }
                }
                if (qmakeConfiguration.isWebkitEnabled().getValue()) {
                    if (baseDir.second() != null) {
                        result.add(new IncludePath(systemFS, baseDir.second() + separator + "QtWebKit.framework/Headers")); // NOI18N
                    }
                    if (baseDir.first() != null) {
                        result.add(new IncludePath(systemFS, baseDir.first() + separator + "QtWebKit")); // NOI18N
                    }
                }
                String uiDir = qmakeConfiguration.getUiDir().getValue();
                if (CndPathUtilities.isPathAbsolute(uiDir)) {
                    result.add(new IncludePath(systemFS, uiDir));
                } else {
                    result.add(new IncludePath(projectFS, conf.getBaseDir() + separator + uiDir));
                }
            } else {
                result = Collections.emptyList();
            }
            return result;
        }

        private static String getQmakePath(MakeConfiguration conf) {
            CompilerSet compilerSet = conf.getCompilerSet().getCompilerSet();
            if (compilerSet != null) {
                Tool qmakeTool = compilerSet.getTool(PredefinedToolKind.QMakeTool);
                if (qmakeTool != null && 0 < qmakeTool.getPath().length()) {
                    return qmakeTool.getPath();
                }
            }
            return "qmake"; // NOI18N
        }

        private static String getCacheKey(MakeConfiguration conf) {
            return conf.getDevelopmentHost().getHostKey() + '/' + getQmakePath(conf); // NOI18N
        }

        private Pair<String, String> getBaseQtIncludeDir(MakeConfiguration conf) {
            String cacheKey = getCacheKey(conf);
            Pair<String, String> baseDir;
            synchronized (cache) {
                if (cache.containsKey(cacheKey)) {
                    baseDir = cache.get(cacheKey);
                } else {
                    ExecutionEnvironment execEnv = conf.getDevelopmentHost().getExecutionEnvironment();
                    String qmakePath = getQmakePath(conf);
                    if (ConnectionManager.getInstance().isConnectedTo(execEnv)) {
                        boolean isMac = false;
                        try {
                            isMac = HostInfoUtils.getHostInfo(execEnv).getOSFamily() == HostInfo.OSFamily.MACOSX;
                        } catch (IOException | ConnectionManager.CancellationException ex) {
                            ex.printStackTrace(System.err);
                        }
                        String baseInc = queryBaseQtIncludeDir(execEnv, qmakePath);
                        String baseLib = null;
                        if (isMac) {
                            baseLib = queryBaseQtLibsDir(execEnv, qmakePath);
                        }
                        baseDir = Pair.of(baseInc, baseLib);
                        cache.put(cacheKey, baseDir);
                    } else {
                        String baseInc = guessBaseQtIncludeDir(qmakePath);
                        baseDir = Pair.of(baseInc, null);
                        // do not cache this result, so that we can
                        // really query qmake once connection is up
                    }
                }
            }
            if (LOGGER.isLoggable(Level.FINE)) {
                LOGGER.log(Level.FINE, "Qt include dir for {0} = {1}", new Object[]{cacheKey, baseDir});
            }
            return baseDir;
        }

        private static String queryBaseQtIncludeDir(ExecutionEnvironment execEnv, String qmakePath) {
            NativeProcessBuilder npb = NativeProcessBuilder.newProcessBuilder(execEnv);
            npb.setExecutable(qmakePath);
            npb.setArguments("-query", "QT_INSTALL_HEADERS"); // NOI18N
            ProcessUtils.ExitStatus res = ProcessUtils.execute(npb);
            if (res.isOK()) {
                String output = res.getOutputString().trim();
                if (!output.isEmpty()) {
                    return output;
                }
            }
            return null;
        }

        private static String queryBaseQtLibsDir(ExecutionEnvironment execEnv, String qmakePath) {
            NativeProcessBuilder npb = NativeProcessBuilder.newProcessBuilder(execEnv);
            npb.setExecutable(qmakePath);
            npb.setArguments("-query", "QT_INSTALL_LIBS"); // NOI18N
            ProcessUtils.ExitStatus res = ProcessUtils.execute(npb);
            if (res.isOK()) {
                String output = res.getOutputString().trim();
                if (!output.isEmpty()) {
                    return output;
                }
            }
            return null;
        }

        private static String guessBaseQtIncludeDir(String qmakePath) {
            // .../bin/qmake -> .../include/qt4
            String binDir = CndPathUtilities.getDirName(qmakePath);
            if (binDir != null) {
                String baseDir = CndPathUtilities.getDirName(binDir);
                if (baseDir != null) {
                    return baseDir + "/include/qt4"; // NOI18N
                }
            }
            return "/usr/include/qt4"; // NOI18N
        }
    }
}
