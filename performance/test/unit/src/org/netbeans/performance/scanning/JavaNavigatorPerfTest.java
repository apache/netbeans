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
 * Portions Copyrighted 2009 Sun Microsystems, Inc.
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
