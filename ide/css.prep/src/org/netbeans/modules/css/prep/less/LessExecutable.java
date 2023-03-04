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
package org.netbeans.modules.css.prep.less;

import java.awt.EventQueue;
import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.api.extexecution.ExecutionDescriptor;
import org.netbeans.modules.css.prep.options.CssPrepOptions;
import org.netbeans.modules.css.prep.util.FileUtils;
import org.netbeans.modules.css.prep.util.InvalidExternalExecutableException;
import org.netbeans.modules.css.prep.util.StringUtils;
import org.netbeans.modules.css.prep.util.UiUtils;
import org.netbeans.modules.css.prep.util.VersionOutputProcessorFactory;
import org.netbeans.modules.web.common.api.Version;
import org.netbeans.modules.web.common.ui.api.CssPreprocessorsUI;
import org.netbeans.modules.web.common.ui.api.ExternalExecutable;
import org.netbeans.modules.web.common.ui.api.ExternalExecutableValidator;
import org.netbeans.spi.project.support.ant.PropertyUtils;
import org.openide.filesystems.FileUtil;
import org.openide.util.NbBundle;
import org.openide.util.Utilities;
import org.openide.windows.IOProvider;
import org.openide.windows.InputOutput;

/**
 * Class representing <tt>lessc</tt> command line tool.
 */
public class LessExecutable {

    private static final Logger LOGGER = Logger.getLogger(LessExecutable.class.getName());

    public static final String EXECUTABLE_NAME = "lessc"; // NOI18N
    public static final String EXECUTABLE_LONG_NAME = EXECUTABLE_NAME + FileUtils.getScriptExtension(true, true);

    private static final String DEBUG_PARAM = "--line-numbers=all"; // NOI18N
    private static final String SOURCE_MAP_PARAM = "--source-map"; // NOI18N
    private static final String SOURCE_MAP_ROOTPATH_PARAM = "--source-map-rootpath=%s"; // NOI18N
    private static final String SOURCE_MAP_URL_PARAM = "--source-map-url=%s"; // NOI18N
    private static final String VERSION_PARAM = "--version"; // NOI18N

    private static final File TMP_DIR = new File(System.getProperty("java.io.tmpdir")); // NOI18N

    private static final Version MINIMAL_VERSION_WITH_SOURCEMAP = Version.fromDottedNotationWithFallback("1.5.0"); // NOI18N
    static final String VERSION_PATTERN = "lessc\\s+(\\d+(\\.\\d+)*)"; // NOI18N

    // version of the compiler set in ide options
    private static volatile Version version;

    protected final String lessPath;


    LessExecutable(String lessPath) {
        assert lessPath != null;
        this.lessPath = lessPath;
    }

    /**
     * Get the default, <b>valid only</b> Less executable.
     * @return the default, <b>valid only</b> Less executable.
     * @throws InvalidExternalExecutableException if Less executable is not valid.
     */
    public static LessExecutable getDefault() throws InvalidExternalExecutableException {
        String path = CssPrepOptions.getInstance().getLessPath();
        String error = validate(path);
        if (error != null) {
            throw new InvalidExternalExecutableException(error);
        }
        if (Utilities.isMac()) {
            return new MacLessExecutable(path);
        }
        return new LessExecutable(path);
    }

    @NbBundle.Messages("Less.executable.label=LESS executable")
    public static String validate(String path) {
        return ExternalExecutableValidator.validateCommand(path, Bundle.Less_executable_label());
    }

    public static void resetVersion() {
        version = null;
    }

    @CheckForNull
    private static Version getVersion() {
        assert !EventQueue.isDispatchThread();
        if (version != null) {
            return version;
        }
        VersionOutputProcessorFactory versionOutputProcessorFactory = new VersionOutputProcessorFactory(VERSION_PATTERN);
        try {
            LessExecutable lessExecutable = getDefault();
            lessExecutable.getExecutable("Less version", TMP_DIR) // NOI18N
                    .additionalParameters(lessExecutable.getVersionParameters())
                    .runAndWait(getSilentDescriptor(), versionOutputProcessorFactory, "Detecting Less version..."); // NOI18N
            String detectedVersion = versionOutputProcessorFactory.getVersion();
            if (detectedVersion != null) {
                version = Version.fromDottedNotationWithFallback(detectedVersion);
                return version;
            }
        } catch (CancellationException ex) {
            // cancelled, cannot happen
            assert false;
        } catch (ExecutionException ex) {
            LOGGER.log(Level.INFO, null, ex);
        } catch (InvalidExternalExecutableException ex) {
            // cannot happen
            LOGGER.log(Level.WARNING, null, ex);
            assert false;
        }
        return null;
    }

    @NbBundle.Messages("Less.compile=LESS (compile)")
    @CheckForNull
    public void compile(File workDir, File source, final File target, List<String> compilerOptions) throws ExecutionException {
        assert !EventQueue.isDispatchThread();
        assert workDir.isDirectory() : "Not directory given: " + workDir;
        assert source.isFile() : "Not file given: " + source;
        final File targetDir = target.getParentFile();
        if (!targetDir.isDirectory()) {
            if (!targetDir.mkdirs()) {
                LOGGER.log(Level.WARNING, "Cannot create directory {0}", targetDir);
                return;
            }
        }
        try {
            getExecutable(Bundle.Less_compile(), workDir)
                    .additionalParameters(getCompileParameters(source, target, compilerOptions))
                    .runAndWait(getDescriptor(new Runnable() {
                @Override
                public void run() {
                    FileUtil.refreshFor(targetDir);
                    UiUtils.refreshCssInBrowser(target);
                }
            }), "Compiling less files..."); // NOI18N
        } catch (CancellationException ex) {
            // cancelled
        } catch (ExecutionException ex) {
            LOGGER.log(Level.INFO, null, ex);
            throw ex;
        }
    }

    String getCommand() {
        return lessPath;
    }

    private ExternalExecutable getExecutable(String title, File workDir) {
        return new ExternalExecutable(getCommand())
                .workDir(workDir)
                .displayName(title)
                .optionsPath(CssPreprocessorsUI.OPTIONS_PATH);
    }

    private ExecutionDescriptor getDescriptor(Runnable postTask) {
        return new ExecutionDescriptor()
                .inputOutput(IOProvider.getDefault().getIO(Bundle.Less_compile(), false))
                .inputVisible(false)
                .frontWindow(false)
                .frontWindowOnError(CssPrepOptions.getInstance().getLessOutputOnError())
                .noReset(true)
                .showProgress(true)
                .postExecution(postTask);
    }

    private static ExecutionDescriptor getSilentDescriptor() {
        return new ExecutionDescriptor()
                .inputOutput(InputOutput.NULL)
                .inputVisible(false)
                .frontWindow(false)
                .showProgress(false);
    }

    List<String> getCompileParameters(File inputFile, File outputFile, List<String> compilerOptions) {
        List<String> params = new ArrayList<>();
        // debug
        boolean debug = CssPrepOptions.getInstance().getLessDebug();
        if (debug) {
            // #241628
            Version installedVersion = getVersion();
            if (installedVersion != null
                    && installedVersion.isAboveOrEqual(MINIMAL_VERSION_WITH_SOURCEMAP)) {
                params.add(SOURCE_MAP_PARAM);
                params.add(String.format(SOURCE_MAP_ROOTPATH_PARAM, getRelativeRootPath(inputFile, outputFile)));
                params.add(String.format(SOURCE_MAP_URL_PARAM, outputFile.getName() + ".map")); // NOI18N
            } else {
                // older versions
                params.add(DEBUG_PARAM);
            }
        }
        // compiler options
        params.addAll(compilerOptions);
        // input
        params.add(inputFile.getAbsolutePath());
        // output
        params.add(outputFile.getAbsolutePath());
        return params;
    }

    List<String> getVersionParameters() {
        return Collections.singletonList(VERSION_PARAM);
    }

    private String getRelativeRootPath(File inputFile, File outputFile) {
        String relativePath = PropertyUtils.relativizeFile(outputFile.getParentFile(), inputFile.getParentFile());
        assert relativePath != null : "input: " + inputFile + " ==> output: " + outputFile;
        return relativePath;
    }

    //~ Inner classes

    // #239065
    private static final class MacLessExecutable extends LessExecutable {

        private static final String BASH_COMMAND = "/bin/bash -lc"; // NOI18N


        MacLessExecutable(String lessPath) {
            super(lessPath);
        }

        @Override
        String getCommand() {
            return BASH_COMMAND;
        }

        @Override
        List<String> getCompileParameters(File inputFile, File outputFile, List<String> compilerOptions) {
            return getParams(super.getCompileParameters(inputFile, outputFile, compilerOptions));
        }

        @Override
        List<String> getVersionParameters() {
            return getParams(super.getVersionParameters());
        }

        private List<String> getParams(List<String> originalParams) {
            StringBuilder sb = new StringBuilder(200);
            sb.append("\""); // NOI18N
            sb.append(lessPath);
            sb.append("\" \""); // NOI18N
            sb.append(StringUtils.implode(originalParams, "\" \"")); // NOI18N
            sb.append("\""); // NOI18N
            return Collections.singletonList(sb.toString());
        }

    }

}
