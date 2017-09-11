/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2013 Oracle and/or its affiliates. All rights reserved.
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
 *
 * Portions Copyrighted 2013 Sun Microsystems, Inc.
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
