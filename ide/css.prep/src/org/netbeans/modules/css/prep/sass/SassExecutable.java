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
