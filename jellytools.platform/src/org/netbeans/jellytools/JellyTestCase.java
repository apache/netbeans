/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
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
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
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
 */
package org.netbeans.jellytools;

import java.awt.Component;
import java.awt.Window;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.lang.reflect.Array;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import javax.swing.JDialog;
import javax.swing.SwingUtilities;
import junit.framework.AssertionFailedError;
import org.netbeans.jemmy.ComponentChooser;
import org.netbeans.jemmy.DialogWaiter;
import org.netbeans.jemmy.EventTool;
import org.netbeans.jemmy.JemmyException;
import org.netbeans.jemmy.JemmyProperties;
import org.netbeans.jemmy.TestOut;
import org.netbeans.jemmy.operators.JDialogOperator;
import org.netbeans.jemmy.util.Dumper;
import org.netbeans.jemmy.util.PNGEncoder;
import org.netbeans.junit.AssertionFailedErrorException;
import org.netbeans.junit.NbModuleSuite;
import org.netbeans.junit.NbTestCase;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Exceptions;

/** JUnit test case with implemented Jemmy/JellyTools support stuff.
 *
 * @author Adam Sotona
 */
public class JellyTestCase extends NbTestCase {

    /** screen capture feature in case of failure is enabled by default
     */
    public boolean captureScreen = Boolean.valueOf(System.getProperty("jemmy.screen.capture", "true")).booleanValue();
    /** screen XML dump feature in case of failure is disabled by default
     */
    public boolean dumpScreen = Boolean.getBoolean("jemmy.screen.xmldump");
    /** closing all modal dialogs after each test case is disabled by default
     */
    public boolean closeAllModal = Boolean.valueOf(System.getProperty("jelly.close.modal", "true")).booleanValue();
    /** Wait 1000 ms before every test case */
    public boolean waitNoEvent = Boolean.valueOf(System.getProperty("jelly.wait.no.event", "true")).booleanValue();

    /** constructor required by JUnit
     * @param testName method name to be used as testcase
     */
    public JellyTestCase(String testName) {
        super(testName);
    }

    /** Inits environment before test case is executed. It can be overridden
     * in sub class but it is recommended to call super.initEnvironment() at
     * the beginning.
     * <br>
     * Default initialization: output messages from jemmy are redirected
     * to jemmy.log file in workdir; jemmy timeouts are loaded from 
     * org.netbeans.jellytools.timeouts and if system property jelly.timeouts_resource
     * or jelly.timeouts_file are set, timeouts are loaded from specified
     * resource/file;
     */
    protected void initEnvironment() {
        // redirect log messages from jemmy to jemmy.log file in workdir
        PrintStream jemmyLog = getLog("jemmy.log");
        JemmyProperties.setCurrentOutput(new TestOut(System.in, jemmyLog, jemmyLog));
        // load timeouts
        String timeoutsResource = System.getProperty("jelly.timeouts_resource");
        String timeoutsFile = System.getProperty("jelly.timeouts_file");
        try {
            JemmyProperties.getCurrentTimeouts().load(getClass().getClassLoader().
                    getResourceAsStream("org/netbeans/jellytools/timeouts"));
            if (timeoutsResource != null && !"".equals(timeoutsResource)) {
                JemmyProperties.getCurrentTimeouts().load(
                        getClass().getClassLoader().getResourceAsStream(timeoutsResource));
            } else if (timeoutsFile != null && !"".equals(timeoutsFile)) {
                JemmyProperties.getCurrentTimeouts().load(timeoutsFile);
            }
        } catch (Exception e) {
            throw new JemmyException("Initialization of timeouts failed.", e);
        }
    }
    
    /** Overridden method from JUnit framework execution to perform conditional
     * screen shot and conversion from TimeoutExpiredException to AssertionFailedError. <br>
     * Waits a second before test execution.
     * @throws Throwable Throwable
     */
    @Override
    public void runBare() throws Throwable {
        initEnvironment();
        // wait 
        if (waitNoEvent) {
            new EventTool().waitNoEvent(1000);
        }
        try {
            super.runBare();
        } catch (ThreadDeath td) {
            // ThreadDead must be re-throwed immediately
            throw td;
        } catch (Throwable th) {
            // suite is notified about test failure so it can do some debug actions
            try {
                failNotify(th);
            } catch (Exception e3) {
            }
            captureScreen();
            // closes all modal dialogs in dependency on systems property
            if (closeAllModal) {
                try {
                    closeAllModal();
                } catch (Exception e) {
                }
            }
            if (th instanceof JemmyException) {
                // all instancies of JemmyException are re-throwed as AssertionError (test failed)
                throw new AssertionFailedErrorException(th.getMessage(), th);
            } else {
                throw th;
            }
        }
    }

    /**
     * Ensures that screen capture is done before the tearDown() is called
     * @throws java.lang.Throwable
     */
    @Override
    protected void runTest() throws Throwable {
        try {
            super.runTest();
        } catch (ThreadDeath td) {
            // ThreadDead must be re-throwed immediately
            throw td;
        } catch (Throwable th) {
            captureScreen();
            throw th;
        }
    }
    private boolean isScreenCaptured = false;

    private void captureScreen() {
        if (!isScreenCaptured) {
            // screen capture is performed when test fails and in dependency on system property
            if (captureScreen) {
                try {
                    String captureFile = getWorkDir().getAbsolutePath() + File.separator + "screen.png";
                    PNGEncoder.captureScreen(captureFile, PNGEncoder.COLOR_MODE);
                    noteCaptureFile(captureFile);
                } catch (Exception ex) {
                    ex.printStackTrace(getLog());
                }
            }
            // send thread dump to log
            PrintStream w = getLog();
            w.println("thread dump just after screen capture:");
            w.println(threadDump());
            // XML dump is performed when test fails and in dependency on system property
            if (dumpScreen) {
                try {
                    String captureFile = getWorkDir().getAbsolutePath() + File.separator + "screen.xml";
                    Dumper.dumpAll(captureFile);
                    noteCaptureFile(captureFile);
                } catch (Exception ex) {
                    ex.printStackTrace(getLog());
                }
            }
            isScreenCaptured = true;
        }
    }

    /**
     * If running inside Hudson, print a message to make it easy to see where the capture file might be archived.
     */
    private void noteCaptureFile(String captureFile) {
        String hudsonURL = System.getenv("HUDSON_URL");
        if (hudsonURL == null) {
            return;
        }
        String workspace = System.getenv("WORKSPACE");
        if (!workspace.endsWith(File.separator)) {
            workspace += File.separator;
        }
        if (!captureFile.startsWith(workspace)) {
            return;
        }
        String relCaptureFile = captureFile.substring(workspace.length()).replace(File.separatorChar, '/');
        System.err.println("Capturing to:");
        System.err.println(hudsonURL + "job/" + System.getenv("JOB_NAME") + "/"
                + System.getenv("BUILD_NUMBER") + "/artifact/" + relCaptureFile);
    }

    /** Method called in case of fail or error just after screen shot and XML dumps. <br>
     * Override this method when you need to be notified about test failures or errors 
     * but avoid any exception to be throwed from this method.<br>
     * super.failNotify() does not need to be called because it is empty.
     * @param reason Throwable reason of current fail */
    protected void failNotify(Throwable reason) {
    }

    /** Closes all opened modal dialogs. Non-modal stay opened. */
    public static void closeAllModal() {
        JDialog dialog;
        ComponentChooser chooser = new ComponentChooser() {

            @Override
            public boolean checkComponent(Component comp) {
                return (comp instanceof JDialog
                        && comp.isShowing()
                        && ((JDialog) comp).isModal());
            }

            @Override
            public String getDescription() {
                return ("Modal dialog");
            }
        };
        while ((dialog = (JDialog) DialogWaiter.getDialog(chooser)) != null) {
            closeDialogs(findBottomDialog(dialog, chooser), chooser);
        }
    }

    private static JDialog findBottomDialog(JDialog dialog, ComponentChooser chooser) {
        Window owner = dialog.getOwner();
        if (chooser.checkComponent(owner)) {
            return (findBottomDialog((JDialog) owner, chooser));
        }
        return (dialog);
    }

    private static void closeDialogs(JDialog dialog, ComponentChooser chooser) {
        Window[] ownees = dialog.getOwnedWindows();
        for (int i = 0; i < ownees.length; i++) {
            if (chooser.checkComponent(ownees[i])) {
                closeDialogs((JDialog) ownees[i], chooser);
            }
        }
        new JDialogOperator(dialog).requestClose();
    }

    /** Finishes test with status Fail
     * @param t Throwable reason of test failure
     */
    public void fail(Throwable t) {
        t.printStackTrace(getLog());
        throw new AssertionFailedErrorException(t);
    }
    /*
     * methods for managing failures of group of dependent tests
     * Usage: each method involved in a group must start with 
     * startTest() call and when finished it must perform endTest().
     * To clear the test status (new group of tests does not depend on 
     * previous tests), method must call clearTestStatus() prior to startTest()
     * Example:
     * public void myTest() {
     *     startTest();
     *      // do my stuff
     *     endTest();
     * }
     */
    /** private variable for holding state whether test was finished
     */
    private boolean testStatus = true;

    /** Checks whether previous test finished correctly and
     *  sets test status to 'not finished' state
     *
     */
    protected void startTest() {
        if (!testStatus) {
            fail("Depending on previous test, but it failed");
        }
        testStatus = false;
    }

    /** Sets the test status to 'finished' state (test passed)
     */
    protected void endTest() {
        testStatus = true;
    }

    /** Clears test status (used when test does not depend on previous test)
     */
    protected void clearTestStatus() {
        testStatus = true;
    }
    private List<Object> openedProjects = null;

    /**
     * Waits for the source scanning to be finished.
     * @throws AssertionFailedError if scanning was canceled or other exception appears
     */
    public void waitScanFinished() {
        try {
            class Wait implements Runnable {

                boolean initialized;
                boolean ok;

                @Override
                public void run() {
                    if (initialized) {
                        ok = true;
                        return;
                    }
                    initialized = true;
                    try {
                        ClassLoader loader = Thread.currentThread().getContextClassLoader();
                        if (loader == null) {
                            loader = getClass().getClassLoader();
                        }
                        // reflection used becaues ScanDialog is in java.sourceui in java cluster
                        // ScanDialog.runWhenScanFinished(this, "tests");
                        Class<?> scanDialogClass = Class.forName("org.netbeans.api.java.source.ui.ScanDialog", true, loader);
                        Method runWhenScanFinishedMethod = scanDialogClass.getDeclaredMethod("runWhenScanFinished", Runnable.class, String.class);
                        boolean canceled = (Boolean) runWhenScanFinishedMethod.invoke(null, this, "tests");
                        assertFalse("Scanning canceled.", canceled);
                        assertTrue("Runnable run", ok);
                    } catch (Exception ex) {
                        throw new RuntimeException(ex);
                    }
                }
            }
            Wait wait = new Wait();
            SwingUtilities.invokeAndWait(wait);
        } catch (Exception ex) {
            throw (AssertionFailedError) new AssertionFailedError().initCause(ex);
        }
    }

    /**
     * Open projects.
     * All newly opened projects are remembered and could later be closed by 
     * closeOpenedProjects() method.
     * The method could be executed many times for one project - nothing
     * happens if project is opened already.
     * @param projects - paths to the project directories.
     * @throws IOException
     */
    public void openProjects(String... projects) throws IOException {
        try {
            Class openProjectsClass = getClass().getClassLoader().loadClass("org.netbeans.api.project.ui.OpenProjects");
            Class projectManagerClass = getClass().getClassLoader().loadClass("org.netbeans.api.project.ProjectManager");
            Method getDefaultOpenProjectsMethod = openProjectsClass.getMethod("getDefault");
            Object openProjectsInstance = getDefaultOpenProjectsMethod.invoke(null);
            Method getOpenProjectsMethod = openProjectsClass.getMethod("getOpenProjects");
            if (openedProjects == null) {
                openedProjects = new ArrayList();
            }
            List<Object> newProjects = new ArrayList<Object>();
            Object pr;
            for (String p : projects) {
                Method getDefaultMethod = projectManagerClass.getMethod("getDefault");
                Object projectManagerInstance = getDefaultMethod.invoke(null);
                Method findProjectMethod = projectManagerClass.getMethod("findProject", FileObject.class);
                pr = findProjectMethod.invoke(projectManagerInstance, FileUtil.toFileObject(new File(p)));
                //pr = ProjectManager.getDefault().findProject(FileUtil.toFileObject(new File(p)));
                Object openProjectsArray = getOpenProjectsMethod.invoke(openProjectsInstance);
                boolean alreadyOpened = false;
                for (int i = 0; i < Array.getLength(openProjectsArray); i++) {
                    if (pr.equals(Array.get(openProjectsArray, i))) {
                        alreadyOpened = true;
                    }
                }
                if (!alreadyOpened) {
                    newProjects.add(pr);
                }
            }
            Class projectClass;
            projectClass = getClass().getClassLoader().loadClass("org.netbeans.api.project.Project");
            Object projectsArray = Array.newInstance(projectClass, newProjects.size());
            for (int i = 0; i < newProjects.size(); i++) {
                Array.set(projectsArray, i, newProjects.get(i));
            }
            Method openMethod = openProjectsClass.getMethod("open", new Class[]{projectsArray.getClass(), Boolean.TYPE});
            openMethod.invoke(openProjectsInstance, projectsArray, false);
            openedProjects.addAll(newProjects);

            waitScanFinished();
        } catch (IllegalAccessException ex) {
            Exceptions.printStackTrace(ex);
        } catch (IllegalArgumentException ex) {
            Exceptions.printStackTrace(ex);
        } catch (InvocationTargetException ex) {
            Exceptions.printStackTrace(ex);
        } catch (ClassNotFoundException ex) {
            Exceptions.printStackTrace(ex);
        } catch (NoSuchMethodException ex) {
            Exceptions.printStackTrace(ex);
        }
    }

    /**
     * Open projects located within <code>NbTestCase.getDataDir();</code>
     * @param projects - relative to dataDir (test/qa-functional/data) path names. 
     * @throws IOException
     */
    public void openDataProjects(String... projects) throws IOException {

        String[] fullPaths = new String[projects.length];
        for (int i = 0; i < projects.length; i++) {
            fullPaths[i] = getDataDir().getAbsolutePath() + File.separator + projects[i];
        }
        openProjects(fullPaths);
    }

    /**
     * Close projects opened by openProjects(String ...) or openDataProjects(String ...)
     */
    public void closeOpenedProjects() {
        closeOpenedProjects(openedProjects.toArray());
        openedProjects.clear();
    }

    /**
     * Close projects opened by openProjects(String ...) or openDataProjects(String ...)
     */
    public void closeOpenedProjects(Object... projects) {
        try {
            Class openProjectsClass = getClass().getClassLoader().loadClass("org.netbeans.api.project.ui.OpenProjects");
            Method getDefaultOpenProjectsMethod = openProjectsClass.getMethod("getDefault");
            Object openProjectsInstance = getDefaultOpenProjectsMethod.invoke(null);
            Class projectClass = getClass().getClassLoader().loadClass("org.netbeans.api.project.Project");
            Object projectsArray = Array.newInstance(projectClass, projects.length);
            for (int i = 0; i < projects.length; i++) {
                Array.set(projectsArray, i, projects[i]);
            }
            Method closeMethod = openProjectsClass.getMethod("close", new Class[]{projectsArray.getClass()});
            closeMethod.invoke(openProjectsInstance, projectsArray);
            //OpenProjects.getDefault().close((Project[]) openedProjects.toArray(new Project[0]));
        } catch (IllegalAccessException ex) {
            Exceptions.printStackTrace(ex);
        } catch (IllegalArgumentException ex) {
            Exceptions.printStackTrace(ex);
        } catch (InvocationTargetException ex) {
            Exceptions.printStackTrace(ex);
        } catch (ClassNotFoundException ex) {
            Exceptions.printStackTrace(ex);
        } catch (NoSuchMethodException ex) {
            Exceptions.printStackTrace(ex);
        }
    }

    protected static junit.framework.Test createModuleTest(String modules, String clusters, Class testClass, String... testNames) {
        NbModuleSuite.Configuration conf = NbModuleSuite.createConfiguration(testClass);
        conf = conf.clusters(clusters);
        conf = conf.enableModules(modules);
        // #217226 - behave as run from command line
        conf = conf.honorAutoloadEager(true);
        if (testNames.length > 0) {
            conf = conf.addTest(testNames);
        }
        return conf.suite();
    }

    protected static junit.framework.Test createModuleTest(Class testClass, String... testNames) {
        return createModuleTest(".*", ".*", testClass, testNames);
    }
    
    /** Returns empty configuration with all clusters and modules enabled and
     * with default recommended settings.
     * @return default empty configuration
     */
    public static NbModuleSuite.Configuration emptyConfiguration() {
        NbModuleSuite.Configuration conf = NbModuleSuite.emptyConfiguration();
        // #217226 - behave as run from command line
        conf = conf.honorAutoloadEager(true);
        return conf.clusters(".*").enableModules(".*");
    }

    private static void appendThread(StringBuffer sb, String indent, Thread t, Map<Thread, StackTraceElement[]> data) {
        sb.append(indent).append("Thread ").append(t.getName()).append('\n');

        for (StackTraceElement e : data.get(t)) {
            sb.append("\tat ").append(e.getClassName()).append('.').append(e.getMethodName()).append('(').append(e.getFileName()).append(':').append(e.getLineNumber()).append(")\n");
        }
    }

    private static void appendGroup(StringBuffer sb, String indent, ThreadGroup tg, Map<Thread, StackTraceElement[]> data) {
        sb.append(indent).append("Group ").append(tg.getName()).append('\n');
        indent = indent.concat("  ");

        int groups = tg.activeGroupCount();
        ThreadGroup[] chg = new ThreadGroup[groups];
        tg.enumerate(chg, false);
        for (ThreadGroup inner : chg) {
            if (inner != null) {
                appendGroup(sb, indent, inner, data);
            }
        }

        int threads = tg.activeCount();
        Thread[] cht = new Thread[threads];
        tg.enumerate(cht, false);
        for (Thread t : cht) {
            if (t != null) {
                appendThread(sb, indent, t, data);
            }
        }
    }

    private static String threadDump() {
        Map<Thread, StackTraceElement[]> all = Thread.getAllStackTraces();
        ThreadGroup root = Thread.currentThread().getThreadGroup();
        while (root.getParent() != null) {
            root = root.getParent();
        }

        StringBuffer sb = new StringBuffer();
        appendGroup(sb, "", root, all);
        return sb.toString();
    }
}
