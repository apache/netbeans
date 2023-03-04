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
