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

package org.netbeans.test.ide;

import java.awt.EventQueue;
import java.io.File;
import java.lang.reflect.Method;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Arrays;
import java.util.List;
import junit.framework.Test;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.api.project.ui.OpenProjects;
import org.netbeans.junit.NbModuleSuite;
import org.netbeans.junit.NbTestCase;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

/**
 * Whitelist test
 * see details on http://wiki.netbeans.org/FitnessViaWhiteAndBlackList
 *
 * To run this test do the following:
 * 1. execute test/whitelist/prepare.bat to prepare Tomcat project
 * 2. execute test/whitelist/test.bat to do the measurement
 * 3. execute test/whitelist/unprepare.bat to restore the environment
 *
 * @author mrkam@netbeans.org
 */
public class WhitelistTest extends NbTestCase {

    private static int stage;

    private static boolean initBlacklistedClassesHandler() {        
        String whitelistFN = new WhitelistTest("Dummy").getDataDir()
                + File.separator + "whitelist_" + stage + ".txt";
        BlacklistedClassesHandler bcHandler = BlacklistedClassesHandlerSingleton.getInstance();
        
        System.out.println("BlacklistedClassesHandler will be initialized with " + whitelistFN);
        if (bcHandler.initSingleton(null, whitelistFN, false)) {
            bcHandler.register();
            System.out.println("BlacklistedClassesHandler handler added");
            System.setProperty("netbeans.warmup.skip", "true");
            System.out.println("Warmup disabled");
            return true;
        } else {
            return false;
        }
    }
    
    public WhitelistTest(String name) {
        super(name);
    }
    
    public static Test suite() throws URISyntaxException {
       URL u = WhitelistTest.class.getProtectionDomain().getCodeSource().getLocation();
        File f = new File(u.toURI());
        while (f != null) {
            File hg = new File(f, ".hg");
            if (hg.isDirectory()) {
                System.setProperty("versioning.unversionedFolders", f.getPath());
                System.err.println("ignoring Hg folder: " + f);
                break;
            }
            f = f.getParentFile();
        }
        stage = Integer.getInteger("test.whitelist.stage", 1);
        
        initBlacklistedClassesHandler();
        
        NbModuleSuite.Configuration conf = NbModuleSuite.createConfiguration(
            WhitelistTest.class
        ).clusters("(?!ergonomics).*").honorAutoloadEager(true).reuseUserDir(stage > 1)
        .enableClasspathModules(false).addStartupArgument("--branding", "nb");
        
        conf = conf.addTest("testWhitelist" + stage);
        
        return conf.suite();
    }

    public void testWhitelist1() throws Exception {
        stage = 1;
        Thread.sleep(3000);
        testWhitelist();
    }

    public void testWhitelist2() throws Exception {
        stage = 2;
        try {
            Thread.sleep(3000);
            testWhitelist();
        } finally {
            openTomcat6Project();
        }
    }

    public void testWhitelist3() throws Exception {
        stage = 3;
        long start = System.currentTimeMillis();
        System.out.println("TRACE 0 0");

        OpenProjects.getDefault().openProjects().get();

        System.out.println("TRACE 1 " + (System.currentTimeMillis() - start));

        Thread.sleep(1000);

        waitParsingFinished();

        System.out.println("TRACE 2 " + (System.currentTimeMillis() - start));

        testWhitelist();
    }

    public void testWhitelist() throws Exception {
        BlacklistedClassesHandler bcHandler = BlacklistedClassesHandlerSingleton.getBlacklistedClassesHandler();
        assertNotNull("BlacklistedClassesHandler should be available", bcHandler);
        bcHandler.saveWhiteList(getLog("loadedClasses_" + stage + ".txt"));
        try {
            bcHandler.listViolations(getLog("whitelist_violators_" + stage + ".txt"), false);
            bcHandler.listViolations(getLog("report_" + stage + ".txt"), false, true);

            int allowed = Integer.getInteger("allowed.violations", 0);
            int number = bcHandler.getNumberOfViolations();
            String txt = null;
            if (number > 0) {
                txt = bcHandler.reportViolations(getLog("violations_" + stage + ".xml"));
                bcHandler.writeViolationsSnapshot(new File(getWorkDir(),"violations_" + stage + ".npss"));
            }
            if (number > allowed) {
                fail(
                    "Too many violations. Allowed only " + allowed + " but was: " + number + ":\n" +
                    txt
                );
            }
        } finally {
            bcHandler.unregister();
        }
    }

    public void openProject(String projectPath) throws Exception {
        File path=new File(getDataDir()+"/../../../../test/qa-functional/data").getCanonicalFile();
        final File pp = new File(path, projectPath);
        FileObject projectsDir = FileUtil.toFileObject(pp);
        assertNotNull("Can find fileObject: " + pp, projectsDir);
        Project p = ProjectManager.getDefault().findProject(projectsDir);
        assertNotNull("Project for " + projectsDir + " found", p);
        OpenProjects.getDefault().open(new Project[] { p }, false);
        List<Project> arr = Arrays.asList(OpenProjects.getDefault().openProjects().get());
        assertTrue("My project is open: " + arr, arr.contains(p));
        waitParsingFinished();
    }

    public void openTomcat6Project() throws Exception {
        openProject("tomcat6");
    }

    private static void waitParsingFinished() throws Exception {
        Project[] arr = OpenProjects.getDefault().openProjects().get();
        assertEquals("One project is open", 1, arr.length);
        assertEquals("project dir is OK", "tomcat6", arr[0].getProjectDirectory().getNameExt());

        class R implements Runnable {
            boolean done;
            @Override
            public void run() {
                if (done) {
                    return;
                }
                if (EventQueue.isDispatchThread()) {
                    done = true;
                 //   ScanDialog.runWhenScanFinished(this, "Test waits scanning finished");
                } else {
                    try {
                        EventQueue.invokeAndWait(this);
                    } catch (Exception ex) {
                        throw new IllegalStateException(ex);
                    }
                }
            }
        }
        R run = new R();

        run.run();


        Class<?> taskClass = Class.forName("org.netbeans.modules.tasklist.impl.TaskManagerImpl");
        Method getter = taskClass.getDeclaredMethod("getInstance");
        Object taskManager = getter.invoke(null);
        Method working = taskClass.getDeclaredMethod("isWorking");
        working.setAccessible(true);
        if (Boolean.TRUE.equals(working.invoke(taskManager))) {
            Method waiter = taskClass.getDeclaredMethod("waitFinished");
            waiter.setAccessible(true);
            waiter.invoke(taskManager);
        }
    }

}
