/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2014 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2014 Sun Microsystems, Inc.
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
