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

package org.netbeans.modules.javascript.karma.coverage;

import java.awt.EventQueue;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.modules.web.clientproject.api.jstesting.Coverage;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.util.NbBundle;

public final class CoverageProcessor {

    private static final Logger LOGGER = Logger.getLogger(CoverageProcessor.class.getName());

    private static volatile boolean debugCoverageWarningShown = false;

    private final Coverage coverage;
    private final File sourceDir;
    private final File logFile;


    public CoverageProcessor(Coverage coverage, File sourceDir, File logFile) {
        assert coverage != null;
        assert sourceDir.isDirectory() : sourceDir;
        assert logFile.isFile() : logFile;
        this.coverage = coverage;
        this.sourceDir = sourceDir;
        this.logFile = logFile;
    }

    @NbBundle.Messages("CoverageProcessor.warn.debugCoverage=Coverage is automatically disabled in Karma Debug mode.")
    public static void warnDebugCoverage() {
        if (debugCoverageWarningShown) {
            // already warned
            return;
        }
        debugCoverageWarningShown = true;
        DialogDisplayer.getDefault().notifyLater(new NotifyDescriptor.Message(Bundle.CoverageProcessor_warn_debugCoverage()));
    }

    public void process() {
        assert coverage.isEnabled();
        assert !EventQueue.isDispatchThread();
        Reader reader;
        try {
            reader = new BufferedReader(new InputStreamReader(new FileInputStream(logFile), StandardCharsets.UTF_8));
        } catch (FileNotFoundException ex) {
            LOGGER.log(Level.WARNING, null, ex);
            return;
        }
        List<Coverage.File> files = CloverLogParser.parse(reader, sourceDir);
        if (files == null) {
            LOGGER.info("Parsed coverage data expected but some error occured");
            return;
        }
        coverage.setFiles(files);
    }

}
