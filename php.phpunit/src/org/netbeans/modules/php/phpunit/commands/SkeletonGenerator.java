/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2015 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2015 Sun Microsystems, Inc.
 */
package org.netbeans.modules.php.phpunit.commands;

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
import org.netbeans.modules.php.api.executable.InvalidPhpExecutableException;
import org.netbeans.modules.php.api.executable.PhpExecutable;
import org.netbeans.modules.php.api.executable.PhpExecutableValidator;
import org.netbeans.modules.php.api.phpmodule.PhpModule;
import org.netbeans.modules.php.api.util.FileUtils;
import org.netbeans.modules.php.phpunit.options.PhpUnitOptions;
import org.netbeans.modules.php.phpunit.preferences.PhpUnitPreferences;
import org.netbeans.modules.php.phpunit.ui.UiUtils;
import org.netbeans.modules.php.phpunit.ui.options.PhpUnitOptionsPanelController;
import org.netbeans.modules.php.phpunit.util.PhpUnitUtils;
import org.netbeans.modules.php.phpunit.util.VersionOutputProcessorFactory;
import org.netbeans.spi.project.support.ant.PropertyUtils;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.NbBundle;
import org.openide.windows.InputOutput;

/**
 * Represents <tt>phpunit-skelgen</tt> command line tool.
 */
public final class SkeletonGenerator {

    private static final Logger LOGGER = Logger.getLogger(SkeletonGenerator.class.getName());

    public static final String SCRIPT_NAME = "phpunit-skelgen"; // NOI18N
    public static final String SCRIPT_NAME_LONG = SCRIPT_NAME + FileUtils.getScriptExtension(true);
    public static final String SCRIPT_NAME_PHAR = SCRIPT_NAME + ".phar"; // NOI18N

    // version
    static final String VERSION_PATTERN = "(?:phpunit\\-skelgen|PHPUnit\\s+Skeleton\\s+Generator)\\s+(\\d+(?:\\.\\d+)*)"; // NOI18N
    private static volatile String version;

    // params
    private static final String VERSION_PARAM = "--version"; // NOI18N
    // v1
    private static final String BOOTSTRAP_PARAM_V1 = "--bootstrap"; // NOI18N
    private static final String TEST_PARAM = "--test"; // NOI18N
    private static final String SEPARATOR_PARAM = "--"; // NOI18N
    // v2
    private static final String ANSI_PARAM = "--ansi"; // NOI18N
    private static final String GENERATE_TEST_PARAM = "generate-test"; // NOI18N
    private static final String BOOTSTRAP_PARAM_V2 = "--bootstrap=%s"; // NOI18N

    private final String skelGenPath;


    private SkeletonGenerator(String skelGenPath) {
        assert skelGenPath != null;
        this.skelGenPath = skelGenPath;
    }

    /**
     * Get the default, <b>valid only</b> SkeletonGenerator script.
     * @return the default, <b>valid only</b> SkeletonGenerator script
     * @throws InvalidPhpExecutableException if SkeletonGenerator script is not valid.
     */
    public static SkeletonGenerator getDefault() throws InvalidPhpExecutableException {
        String script = PhpUnitOptions.getInstance().getSkeletonGeneratorPath();
        String error = validate(script);
        if (error != null) {
            throw new InvalidPhpExecutableException(error);
        }
        return new SkeletonGenerator(script);
    }

    @NbBundle.Messages("SkeletonGenerator.script.label=Skeleton generator script")
    public static String validate(String command) {
        return PhpExecutableValidator.validateCommand(command, Bundle.SkeletonGenerator_script_label());
    }

    public static void resetVersion() {
        version = null;
    }

    @CheckForNull
    private static String getVersion() {
        assert !EventQueue.isDispatchThread();
        if (version != null) {
            return version;
        }
        VersionOutputProcessorFactory versionOutputProcessorFactory = new VersionOutputProcessorFactory(VERSION_PATTERN);
        try {
            SkeletonGenerator skeletonGenerator = getDefault();
            skeletonGenerator.getExecutable("Skeleton Generator version", PhpUnitUtils.TMP_DIR) // NOI18N
                    .additionalParameters(Collections.singletonList(VERSION_PARAM))
                    .runAndWait(getSilentDescriptor(), versionOutputProcessorFactory, "Detecting Skeleton Generator version..."); // NOI18N
            String detectedVersion = versionOutputProcessorFactory.getVersion();
            if (detectedVersion != null) {
                version = detectedVersion;
                return version;
            }
        } catch (CancellationException ex) {
            // cancelled, cannot happen
            assert false;
        } catch (ExecutionException ex) {
            LOGGER.log(Level.INFO, null, ex);
        } catch (InvalidPhpExecutableException ex) {
            // cannot happen
            LOGGER.log(Level.WARNING, null, ex);
            assert false;
        }
        return null;
    }

    @CheckForNull
    public FileObject generateTest(PhpModule phpModule, FileObject sourceClassFile, String sourceClassName) throws ExecutionException {
        FileObject sourceDir = phpModule.getSourceDirectory();
        assert sourceDir != null;
        FileObject testDir = phpModule.getTestDirectory(sourceClassFile);
        assert testDir != null;
        FileObject commonRoot = FileUtils.getCommonRoot(sourceClassFile, testDir);
        if (commonRoot == null
                || !FileUtil.isParentOf(sourceDir, commonRoot)) {
            // look only inside project source dir
            commonRoot = sourceDir;
        }
        assert commonRoot != null;
        String relativePath = PropertyUtils.relativizeFile(FileUtil.toFile(commonRoot), FileUtil.toFile(sourceClassFile));
        assert relativePath != null;
        assert !relativePath.startsWith("../") : "Unexpected relative path: " + relativePath + " for " + commonRoot + " and " + sourceClassFile;
        String relativeTestPath = relativePath.substring(0, relativePath.length() - sourceClassFile.getExt().length() - 1);
        File testFile = PropertyUtils.resolveFile(FileUtil.toFile(testDir), PhpUnit.makeTestFile(relativeTestPath));
        FileObject testFo = FileUtil.toFileObject(testFile);
        if (testFo != null && testFo.isValid()) {
            return testFo;
        }
        if (!ensureTestFolderExists(testFile)) {
            return null;
        }
        String testClassName = PhpUnit.makeTestClass(sourceClassName);
        List<String> params = getParams(phpModule, sourceClassName, sourceClassFile, testClassName, testFile);
        PhpExecutable skelGen = getExecutable(phpModule, params);
        if (skelGen == null) {
            return null;
        }
        try {
            Integer status = skelGen.runAndWait(getDescriptor(), "Generating test..."); // NOI18N
            if (status != null
                    && status == 0) {
                // refresh fs
                FileUtil.refreshFor(testFile.getParentFile());
                testFo = FileUtil.toFileObject(testFile);
                if (testFo == null) {
                    // #239795
                    boolean testFileExists = testFile.exists();
                    LOGGER.log(testFileExists ? Level.WARNING : Level.INFO,
                            "FileObject for generated test not found (java.io.File for test file exists: {0})", testFileExists);
                    return null;
                }
                return testFo;
            }
        } catch (CancellationException ex) {
            // canceled
            LOGGER.log(Level.FINE, "Test creating cancelled", ex);
        }
        return null;
    }

    private List<String> getParams(PhpModule phpModule, String sourceClassName, FileObject sourceClassFile, String testClassName, File testFile) {
        List<String> params = new ArrayList<>();
        String bootstrap = null;
        if (PhpUnitPreferences.isBootstrapEnabled(phpModule)
                && PhpUnitPreferences.isBootstrapForCreateTests(phpModule)) {
            bootstrap = PhpUnitPreferences.getBootstrapPath(phpModule);
        }
        String ver = getVersion();
        if (ver != null
                && ver.startsWith("1.")) { // NOI18N
            // version 1
            if (bootstrap != null) {
                params.add(BOOTSTRAP_PARAM_V1);
                params.add(bootstrap);
            }
            params.add(TEST_PARAM);
            params.add(SEPARATOR_PARAM);
        } else {
            // version 2+ (and possible fallback)
            params.add(ANSI_PARAM);
            params.add(GENERATE_TEST_PARAM);
            if (bootstrap != null) {
                params.add(String.format(BOOTSTRAP_PARAM_V2, bootstrap));
            }
        }
        params.add(sanitizeClassName(sourceClassName));
        params.add(FileUtil.toFile(sourceClassFile).getAbsolutePath());
        params.add(sanitizeClassName(testClassName));
        params.add(testFile.getAbsolutePath());
        return params;
    }

    @NbBundle.Messages({
        "# {0} - project name",
        "SkeletonGenerator.run.title=PHPUnit Skeleton Generator ({0})",
    })
    @CheckForNull
    private PhpExecutable getExecutable(PhpModule phpModule, List<String> params) {
        FileObject sourceDirectory = phpModule.getSourceDirectory();
        if (sourceDirectory == null) {
            UiUtils.warnNoSources(phpModule.getDisplayName());
            return null;
        }

        return getExecutable(Bundle.SkeletonGenerator_run_title(phpModule.getDisplayName()), FileUtil.toFile(sourceDirectory))
                .optionsSubcategory(PhpUnitOptionsPanelController.OPTIONS_SUB_PATH)
                .additionalParameters(params);
    }

    private PhpExecutable getExecutable(String title, File workDir) {
        return new PhpExecutable(skelGenPath)
                .workDir(workDir)
                .displayName(title);
    }

    private ExecutionDescriptor getDescriptor() {
        return PhpExecutable.DEFAULT_EXECUTION_DESCRIPTOR
                .optionsPath(PhpUnitOptionsPanelController.OPTIONS_PATH)
                .inputVisible(false);
    }

    private static ExecutionDescriptor getSilentDescriptor() {
        return new ExecutionDescriptor()
                .inputOutput(InputOutput.NULL)
                .inputVisible(false)
                .frontWindow(false)
                .showProgress(false);
    }

    // https://github.com/sebastianbergmann/phpunit-skeleton-generator/issues/1
    private String sanitizeClassName(String className) {
        if (className.startsWith("\\")) { // NOI18N
            className = className.substring(1);
        }
        return className;
    }

    // #210123
    private boolean ensureTestFolderExists(File testClassFile) {
        File parent = testClassFile.getParentFile();
        if (!parent.isDirectory()) {
            if (!parent.mkdirs()) {
                return false;
            }
            FileUtil.refreshFor(parent);
        }
        return true;
    }

}
