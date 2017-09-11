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
package org.netbeans.modules.css.prep.sass;

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
import org.netbeans.modules.css.prep.util.VersionOutputProcessorFactory;
import org.netbeans.modules.web.common.api.Version;
import org.openide.modules.Places;
import org.openide.windows.InputOutput;

/**
 * Class representing <tt>sass</tt> command line tool.
 * <p>
 * This is the only <b>officially</b> supported CLI as of NB 8.1.
 */
final class SassExecutable extends SassCli {

    private static final Logger LOGGER = Logger.getLogger(SassExecutable.class.getName());

    private static final String EXECUTABLE_NAME = "sass"; // NOI18N
    private static final String EXECUTABLE_LONG_NAME = EXECUTABLE_NAME + FileUtils.getScriptExtension(true, false);
    static final String[] EXECUTABLE_NAMES = new String[] {
        EXECUTABLE_NAME,
        EXECUTABLE_LONG_NAME,
    };

    private static final String DEBUG_PARAM = "--debug-info"; // NOI18N
    private static final String SOURCEMAP_PARAM = "--sourcemap"; // NOI18N
    private static final String SOURCEMAP_WITH_VALUE_PARAM = "--sourcemap=%s"; // NOI18N
    private static final String VERSION_PARAM = "--version"; // NOI18N
    private static final String CACHE_LOCATION_PARAM = "--cache-location"; // NOI18N

    private static final File TMP_DIR = new File(System.getProperty("java.io.tmpdir")); // NOI18N

    private static final Version MINIMAL_VERSION_WITH_SOURCEMAP = Version.fromDottedNotationWithFallback("3.3.0"); // NOI18N
    private static final Version VERSION_WITH_DEFAULT_SOURCEMAP = Version.fromDottedNotationWithFallback("3.4.0"); // NOI18N
    static final String VERSION_PATTERN = "Sass\\s+(\\d+(\\.\\d+)*)"; // NOI18N


    SassExecutable(String sassPath) {
        super(sassPath);
    }

    @CheckForNull
    private Version getVersion() {
        assert !EventQueue.isDispatchThread();
        if (version != null) {
            return version;
        }
        VersionOutputProcessorFactory versionOutputProcessorFactory = new VersionOutputProcessorFactory(VERSION_PATTERN);
        try {
            getExecutable("Sass version", TMP_DIR) // NOI18N
                    .additionalParameters(Collections.singletonList(VERSION_PARAM))
                    .runAndWait(getSilentDescriptor(), versionOutputProcessorFactory, "Detecting Sass version..."); // NOI18N
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
        }
        return null;
    }

    private static ExecutionDescriptor getSilentDescriptor() {
        return new ExecutionDescriptor()
                .inputOutput(InputOutput.NULL)
                .inputVisible(false)
                .frontWindow(false)
                .showProgress(false);
    }

    @Override
    protected List<String> getParameters(File inputFile, File outputFile, List<String> compilerOptions) {
        List<String> params = new ArrayList<>();
        // cache location
        params.add(CACHE_LOCATION_PARAM);
        params.add(Places.getCacheSubdirectory("sass-compiler").getAbsolutePath()); // NOI18N
        // debug
        boolean debug = CssPrepOptions.getInstance().getSassDebug();
        Version installedVersion = getVersion();
        if (debug) {
            if (installedVersion != null) {
                if (installedVersion.isAboveOrEqual(VERSION_WITH_DEFAULT_SOURCEMAP)) {
                    // noop, the 'auto' sourcemaps work just fine
                } else if (installedVersion.isAboveOrEqual(MINIMAL_VERSION_WITH_SOURCEMAP)) {
                    params.add(SOURCEMAP_PARAM);
                } else {
                    // older versions
                    params.add(DEBUG_PARAM);
                }
            } else {
                // unknwon sass version
                params.add(DEBUG_PARAM);
            }
        } else {
            if (installedVersion != null
                    && installedVersion.isAboveOrEqual(VERSION_WITH_DEFAULT_SOURCEMAP)) {
                params.add(String.format(SOURCEMAP_WITH_VALUE_PARAM, "none")); // NOI18N
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

}
