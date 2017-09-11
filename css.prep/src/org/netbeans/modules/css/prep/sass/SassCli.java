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
package org.netbeans.modules.css.prep.sass;

import java.awt.EventQueue;
import java.io.File;
import java.util.List;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.annotations.common.NullAllowed;
import org.netbeans.api.extexecution.ExecutionDescriptor;
import org.netbeans.modules.css.prep.options.CssPrepOptions;
import org.netbeans.modules.css.prep.util.InvalidExternalExecutableException;
import org.netbeans.modules.css.prep.util.UiUtils;
import org.netbeans.modules.web.common.api.Version;
import org.netbeans.modules.web.common.ui.api.CssPreprocessorsUI;
import org.netbeans.modules.web.common.ui.api.ExternalExecutable;
import org.netbeans.modules.web.common.ui.api.ExternalExecutableValidator;
import org.openide.filesystems.FileUtil;
import org.openide.util.NbBundle;
import org.openide.windows.IOProvider;

/**
 * Class representing a general Sass compiler. Currently, it
 * supports only {@link SassExecutable the official Sass compiler}
 * or any {@link LibSassExecutable libsass frontend} (currently, via
 * a system property only, adding no additional command line parameters).
 */
public abstract class SassCli {

    private static final Logger LOGGER = Logger.getLogger(SassCli.class.getName());

    // #247890
    /**
     * System property to be set to "true" if <tt>libsass</tt> should be used.
     */
    private static final boolean USE_LIBSASS = Boolean.getBoolean("nb.sass.libsass"); // NOI18N

    // version of the compiler set in ide options
    @NullAllowed
    protected static volatile Version version;

    @NonNull
    private final String sassPath;


    protected SassCli(String sassPath) {
        assert sassPath != null;
        this.sassPath = sassPath;
    }

    /**
     * Get the default, <b>valid only</b> Sass CLI.
     * @return the default, <b>valid only</b> Sass CLI.
     * @throws InvalidExternalExecutableException if Sass CLI is not valid.
     */
    public static SassCli getDefault() throws InvalidExternalExecutableException {
        String path = CssPrepOptions.getInstance().getSassPath();
        String error = validate(path);
        if (error != null) {
            throw new InvalidExternalExecutableException(error);
        }
        if (USE_LIBSASS) {
            return new LibSassExecutable(path);
        }
        return new SassExecutable(path);
    }

    @NbBundle.Messages("SassCli.executable.label=Sass executable")
    public static String validate(String path) {
        return ExternalExecutableValidator.validateCommand(path, Bundle.SassCli_executable_label());
    }

    public static void resetVersion() {
        version = null;
    }

    /**
     * Must return array of at least 2 values.
     * @return array of at least 2 values
     */
    public static String[] getExecutableNames() {
        if (USE_LIBSASS) {
            return LibSassExecutable.EXECUTABLE_NAMES;
        }
        return SassExecutable.EXECUTABLE_NAMES;
    }

    protected abstract List<String> getParameters(File inputFile, File outputFile, List<String> compilerOptions);

    @NbBundle.Messages("SassCli.compile=Sass (compile)")
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
            getExecutable(Bundle.SassCli_compile(), workDir)
                    .additionalParameters(getParameters(source, target, compilerOptions))
                    .runAndWait(getDescriptor(new Runnable() {
                @Override
                public void run() {
                    FileUtil.refreshFor(targetDir);
                    UiUtils.refreshCssInBrowser(target);
                }
            }), "Compiling sass files..."); // NOI18N
        } catch (CancellationException ex) {
            // cancelled
        } catch (ExecutionException ex) {
            LOGGER.log(Level.INFO, null, ex);
            throw ex;
        }
    }

    protected ExternalExecutable getExecutable(String title, File workDir) {
        return new ExternalExecutable(sassPath)
                .workDir(workDir)
                .displayName(title)
                .optionsPath(CssPreprocessorsUI.OPTIONS_PATH);
    }

    private ExecutionDescriptor getDescriptor(Runnable postTask) {
        return new ExecutionDescriptor()
                .inputOutput(IOProvider.getDefault().getIO(Bundle.SassCli_compile(), false))
                .inputVisible(false)
                .frontWindow(false)
                .frontWindowOnError(CssPrepOptions.getInstance().getSassOutputOnError())
                .noReset(true)
                .showProgress(true)
                .postExecution(postTask);
    }

}
