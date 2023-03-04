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
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import javax.swing.SwingUtilities;
import junit.framework.Test;
import org.netbeans.junit.NbModuleSuite;
import org.netbeans.junit.NbPerformanceTest.PerformanceData;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.project.ui.ProjectTab;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

/**
 *
 * @author Pavel Flaška
 */
public class JavaNavigatorPerfTest extends NbTestCase {

    private final List<PerformanceData> data;
    private static String logged;

    public JavaNavigatorPerfTest(String name) {
        super(name);
        data = new ArrayList<>();
    }

    /**
     * Set-up the services and project
     *
     * @throws java.io.IOException
     */
    @Override
    protected void setUp() throws IOException {
        System.out.println("###########  " + getName() + " ###########");
    }

    public void testEditorPaneSwitch() throws Exception {
        String projectName = "jEdit";
        File projectsDir = getWorkDir();
        Utilities.projectDownloadAndUnzip(projectName, projectsDir);
        final File projectDir = new File(projectsDir, projectName);

        Logger navigatorUpdater = Logger.getLogger("org.netbeans.modules.java.navigation.ClassMemberPanelUI.perf");
        navigatorUpdater.setLevel(Level.FINE);
        NavigatorHandler handler = new NavigatorHandler();
        navigatorUpdater.addHandler(handler);

        Utilities.openProjects(projectsDir, projectName);
        Utilities.waitScanningFinished(new File(projectsDir, projectName));
        SwingUtilities.invokeAndWait(new Runnable() {

            @Override
            public void run() {
                ProjectTab pt = ProjectTab.findDefault(ProjectTab.ID_LOGICAL);
                pt.requestActive();
                FileObject testFile = FileUtil.toFileObject(new File(projectDir, "src/bsh/This.java"));
                pt.selectNodeAsync(testFile);
            }
        });
        if(!"This".equals(logged)) {
            synchronized(handler) {
                handler.wait(5000);
            }
        }
        SwingUtilities.invokeAndWait(new Runnable() {

            @Override
            public void run() {
                ProjectTab pt = ProjectTab.findDefault(ProjectTab.ID_LOGICAL);
                pt.requestActive();
                FileObject testFile = FileUtil.toFileObject(new File(projectDir, "src/org/gjt/sp/jedit/jEdit.java"));
                pt.selectNodeAsync(testFile);
            }
        });
        if(!"jEdit".equals(logged)) {
            synchronized(handler) {
                handler.wait(5000);
            }
        }
        SwingUtilities.invokeAndWait(new Runnable() {

            @Override
            public void run() {
                Logger.getAnonymousLogger().log(Level.INFO, "Test finished execution.");
            }
        });
    }

    @Override
    protected void tearDown() throws Exception {
        Logger.getAnonymousLogger().log(Level.INFO, "Processing results.");
        super.tearDown();
        for (PerformanceData rec : data) {
            Utilities.processUnitTestsResults(JavaNavigatorPerfTest.class.getCanonicalName(), rec);
        }
        data.clear();
    }

    public static Test suite() throws InterruptedException {
        return NbModuleSuite.createConfiguration(JavaNavigatorPerfTest.class).
                clusters(".*").enableModules(".*").suite();
    }

    private class NavigatorHandler extends Handler {

        @Override
        public synchronized void publish(LogRecord record) {
            PerformanceData perfRec = new PerformanceData();
            perfRec.name = (String) record.getParameters()[0];
            perfRec.value = (Long) record.getParameters()[1];
            perfRec.unit = "ms";
            perfRec.runOrder = 0;
            perfRec.threshold = 1000;
            System.err.println(perfRec.name);
            data.add(perfRec);
            logged = perfRec.name;
            notifyAll();
        }

        @Override
        public void flush() {
        }

        @Override
        public void close() throws SecurityException {
        }
    }
}
