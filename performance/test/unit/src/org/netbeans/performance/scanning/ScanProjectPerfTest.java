/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */
package org.netbeans.performance.scanning;

import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import junit.framework.Test;
import org.netbeans.api.project.ui.OpenProjects;
import org.netbeans.junit.NbModuleSuite;
import org.netbeans.junit.NbPerformanceTest.PerformanceData;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.parsing.impl.indexing.RepositoryUpdater;
import org.netbeans.performance.scanning.ScanningHandler.ScanType;

/**
 *
 * @author Pavel Flaska
 */
public class ScanProjectPerfTest extends NbTestCase {

    public ScanProjectPerfTest(String name) {
        super(name);
    }

    private ScanningHandler handler;

    /**
     * Set-up the services and project
     *
     * @throws java.io.IOException
     */
    @Override
    protected void setUp() throws IOException {
        System.out.println("###########  " + getName() + " ###########");
        Logger.getLogger(RepositoryUpdater.class.getName()).setLevel(Level.INFO);
        Logger.getLogger("org.netbeans.api.java.source.ClassIndex").setLevel(Level.WARNING);
        Logger.getLogger("SpringBinaryIndexer").setLevel(Level.WARNING);
        clearWorkDir();
        Utilities.setCacheFolder(getWorkDir());
    }

    @Override
    protected int timeOut() {
        return 15 * 60000; // 15min
    }

    public void testScanJEdit() throws Exception {
        scanProject("jEdit", 25000, 35000, 200, 8000);
    }

    public void testPhpProject() throws Exception {
        scanProject("mediawiki-1.14.0", 0, 100000, 0, 1200);
    }

    public void testWebProject() throws Exception {
        scanProject("FrankioskiProject", 40000, 35000, 600, 15000);
    }

    public void testTomcat() throws Exception {
        scanProject("tomcat6", 10000, 50000, 200, 15000);
    }

    private void scanProject(String projectName, long thBinaryScan, long thSourceScan, long thBinaryUpdate, long thSourceUpdate) throws Exception {
        File projectsDir = getWorkDir();
        Utilities.projectDownloadAndUnzip(projectName, projectsDir);

        Logger repositoryUpdater = Logger.getLogger(RepositoryUpdater.class.getName());
        repositoryUpdater.setLevel(Level.INFO);
        handler = new ScanningHandler(projectName, thBinaryScan, thSourceScan, thBinaryUpdate, thSourceUpdate);
        repositoryUpdater.addHandler(handler);

        Logger log = Logger.getLogger("org.openide.filesystems.MIMESupport");
        log.setLevel(Level.WARNING);
        Utilities.ReadingHandler readHandler = new Utilities.ReadingHandler();
        log.addHandler(readHandler);

        Utilities.openProjects(projectsDir, projectName);
        Utilities.waitScanningFinished(new File(projectsDir, projectName));
        handler.setType(ScanType.UP_TO_DATE);
        Utilities.refreshIndexes();
        Utilities.waitScanningFinished(new File(projectsDir, projectName));
        OpenProjects.getDefault().close(OpenProjects.getDefault().getOpenProjects());
        repositoryUpdater.removeHandler(handler);
        // wait for scanning of unowned roots after all projects are closed
        synchronized(this) {
            this.wait(10000);
    }
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
        if (handler != null) {
            for (PerformanceData rec : handler.getData()) {
                Utilities.processUnitTestsResults(ScanProjectPerfTest.class.getCanonicalName(), rec);
            }
            handler.clear();
        }
    }

    public static Test suite() throws InterruptedException {
        return NbModuleSuite.createConfiguration(ScanProjectPerfTest.class).
                clusters(".*").enableModules(".*").
                suite();
    }
}
