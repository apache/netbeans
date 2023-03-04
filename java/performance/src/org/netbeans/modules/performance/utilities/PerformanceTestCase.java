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
package org.netbeans.modules.performance.utilities;

import java.awt.Component;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.DataOutputStream;
import java.io.File;
import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.lang.reflect.Method;
import java.net.JarURLConnection;
import java.net.URL;
import java.util.ConcurrentModificationException;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import java.util.prefs.Preferences;
import javax.swing.Action;
import junit.framework.AssertionFailedError;
import org.netbeans.jellytools.TopComponentOperator;
import org.netbeans.jemmy.QueueTool;
import org.netbeans.jemmy.JemmyProperties;
import org.netbeans.jemmy.operators.ComponentOperator;
import org.netbeans.jemmy.operators.WindowOperator;
import org.netbeans.jemmy.util.PNGEncoder;
import org.netbeans.junit.NbPerformanceTest;
import org.netbeans.modules.performance.guitracker.ActionTracker;
import org.netbeans.modules.performance.guitracker.LoggingRepaintManager;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Lookup;
import org.openide.util.RequestProcessor;

/**
 * Test case with implemented Performance Tests Validation support stuff. This
 * class provide methods for QA Performance measurement. Implemented methods:
 * <pre>
 * doMeasurement();
 * measureTime();
 * measureMemoryUsage();
 * </pre>
 *
 *
 * Number of repeatedly measured time can be set by system property
 * <b> org.netbeans.performance.repeat </b>. If property isn't set time is
 * measured only once.
 *
 * @author mmirilovic@netbeans.org, rkubacki@netbeans.org,
 * anebuzelsky@netbeans.org, mrkam@netbeans.org
 */
public abstract class PerformanceTestCase extends PerformanceTestCase2 implements NbPerformanceTest {

    public static final String OPEN_AFTER = "OPEN - after";
    public static final String OPEN_BEFORE = "OPEN - before";

    private static final boolean logMemory = Boolean.getBoolean("org.netbeans.performance.memory.usage.log");

    /**
     * Constant defining maximum time delay for "ui-response" of actions that
     * needs to react quickly to keep the user's flow to stay uninterrupted.
     * This is set to 1000ms.
     */
    protected static final long WINDOW_OPEN = 1000;

    /**
     * Constant defining maximum time delay for "ui-response" of actions that
     * needs to react instantaneously. This is set to 100ms.
     */
    protected static final long UI_RESPONSE = 100;

    /**
     * Expected time in which the measured action should be completed. Usualy
     * should be set to WINDOW_OPEN or UI_RESPONSE.
     * <br><b>default</b> = UI_RESPONSE
     */
    public long expectedTime = UI_RESPONSE;

    public int iteration = 1;

    /**
     * Maximum number of iterations to wait for last paint on
     * component/container.
     * <br><b>default</b> = 10 iterations
     */
    public int MAX_ITERATION = 10;

    /**
     * Defines delay between checks if the component/container is painted.
     * <br><b>default</b> = 1000 ms
     */
    public int WAIT_PAINT = 1000;

    /**
     * Wait No Event in the Event Queue after call method <code>open()</code>.
     * <br><b>default</b> = 1000 ms
     */
    public int WAIT_AFTER_OPEN = 1000;

    /**
     * Wait No Event in the Event Queue after call method
     * <code>prepare()</code>.
     * <br><b>default</b> = 1000 ms
     */
    public int WAIT_AFTER_PREPARE = 1000;

    /**
     * Wait No Event in the Event Queue after call method {@link close}.
     * <br><b>default</b> = 1000 ms
     */
    public int WAIT_AFTER_CLOSE = 1000;

    /**
     * Factor for wait_after_open_heuristic timeout, negative HEURISTIC_FACTOR
     * disables heuristic
     */
    public double HEURISTIC_FACTOR = 1.25;

    /**
     * Count of repeats
     */
    protected static int repeat = Integer.getInteger("org.netbeans.performance.repeat", 4).intValue();

    /**
     * Count of repeats for measure memory usage
     */
    protected static int repeat_memory = Integer.getInteger("org.netbeans.performance.memory.repeat", -1).intValue();

    /**
     * Performance data.
     */
    private static java.util.ArrayList<NbPerformanceTest.PerformanceData> data = new java.util.ArrayList<NbPerformanceTest.PerformanceData>();

    /**
     * Measure from last MOUSE event, you can define your own , by default it's
     * MOUSE_RELEASE
     */
    protected int track_mouse_event = ActionTracker.TRACK_MOUSE_RELEASE;

    /**
     * Define start event - measured time will start by this event
     */
    protected int MY_START_EVENT = MY_EVENT_NOT_AVAILABLE;

    /**
     * Define end event - measured time will end by this event
     */
    protected int MY_END_EVENT = MY_EVENT_NOT_AVAILABLE;

    /**
     * Not set event - default for START/END events
     */
    protected static final int MY_EVENT_NOT_AVAILABLE = -10;

    /**
     * tracker for UI activities
     */
    private static ActionTracker tr;

    private static LoggingRepaintManager rm;

    private static final Logger LOG = Logger.getLogger(PerformanceTestCase.class.getName());
    /**
     * Constants for managing caret blink rate.
     */
    private int defaultCaretBlinkRate;
    private boolean caretBlinkingDisabled = false;
    private static final String CARET_BLINK_RATE_KEY = "caret-blink-rate";

    static {
        if (repeat_memory == -1) {
            tr = ActionTracker.getInstance();
            rm = new LoggingRepaintManager(tr);
            rm.setEnabled(true);
        }
        System.setProperty("org.netbeans.core.TimeableEventQueue.quantum", "100000"); // disable slowness detector
        // disable animation on Windows. It can produce false events - see issue 100961
        System.setProperty("swing.disablevistaanimation", "true");
        
        URL u = PerformanceTestCase.class.getProtectionDomain().getCodeSource().getLocation();
        try {
            // disable Mercurial if running from NetBeans source tree
            if ("jar".equals(u.getProtocol())) { // NOI18N
                u = ((JarURLConnection) u.openConnection()).getJarFileURL();
            }
            File f = new File(u.toURI());
            while (f != null) {
                File hg = new File(f, ".hg");
                if (hg.isDirectory()) {
                    System.setProperty("versioning.unversionedFolders", f.getPath());
                    LOG.log(Level.INFO, "ignoring Hg folder: {0}", f);
                    break;
                }
                f = f.getParentFile();
            }
        } catch (Exception ex) {
            LOG.log(Level.WARNING, "Problem looking up " + u, ex);
        }
    }

    /**
     * Tested component operator.
     */
    protected ComponentOperator testedComponentOperator;

    /**
     * Name of test case should be changed.
     */
    protected HashMap<String, String> renamedTestCaseName;

    /**
     * Use order just for indentify first and next run, not specific run order
     */
    public boolean useTwoOrderTypes = true;

    /**
     * Group identification for traced refs that do not have special category.
     */
    private final Object DEFAULT_REFS_GROUP = new Object();

    /**
     * Set of references to traced object that ought to be GCed after tests runs
     * and their informational messages.
     */
    private static Map<Object, Map<Reference<Object>, String>> tracedRefs
            = new HashMap<Object, Map<Reference<Object>, String>>();
    private Profile profile;

    /**
     * Creates a new instance of PerformanceTestCase
     *
     * @param testName name of the test
     */
    public PerformanceTestCase(String testName) {
        super(testName);
        renamedTestCaseName = new HashMap<String, String>();
    }

    /**
     * Creates a new instance of PerformanceTestCase
     *
     * @param testName name of the test
     * @param performanceDataName name for measured performance data, measured
     * values are stored to results under this name
     */
    public PerformanceTestCase(String testName, String performanceDataName) {
        this(testName);
        setTestCaseName(testName, performanceDataName);
    }

    /**
     * SetUp test cases: redirect log/ref, initialize performance data.
     */
    @Override
    public void setUp() {
        data = new java.util.ArrayList<NbPerformanceTest.PerformanceData>();
    }

    /**
     * Getter for LoggingRepaintManager.
     *
     * @return LoggingRepaintManager
     */
    protected LoggingRepaintManager repaintManager() {
        return rm;
    }

    /**
     * TearDown test cases: call method <code>call()</code> and closing all
     * modal dialogs.
     *
     * @see close
     */
    @Override
    public void tearDown() {
        // tr = null;
        //close();
        closeAllModal();
    }

    /**
     * Switch to measured methods. Now all test can be used for measure UI
     * responsiveness or look for memory leaks.
     */
    public void doMeasurement() {
        if (repeat_memory == -1) {
            measureTime();
        } else {
            measureMemoryUsage();
        }
        restoreEditorCaretBlinking();
    }

    /**
     * Test that measures time betwen generated AWT event and last paint event
     * that finishes painting of component/container. It uses
     * <code>ROBOT_MODEL_MASK</code> as an event dispatching model when user's
     * activity is simulated.</p>
     * <p>
     * To initialize the test {@link prepare()} method is invoked at the
     * begining and processing is delayed until there is a period of time at
     * least <code>WAIT_AFTER_PREPARE</code>ms long.</p>
     * <p>
     * The {@link open()} method is called then to perform the measured action,
     * tests waits for no event in <code>WAIT_AFTER_OPEN</code>ms and until
     * component/container is fully painted. Meaure time and report measured
     * time.
     * <br>
     * <br>If during measurement exception arise - test fails and no value is
     * reported as Performance Data.
     * <br>If measuredTime as longer than expectedTime test fails.</p>
     * <p>
     * Each test should reset the state in {@link close()} method. Again there
     * is a waiting for quiet period of time after this call.</p>
     */
    public void measureTime() {
        Exception exceptionDuringMeasurement = null;

        long wait_after_open_heuristic = WAIT_AFTER_OPEN;

        long[] measuredTime = new long[repeat + 1];

        // issue 56091 and applied workarround on the next line
        // JemmyProperties.setCurrentDispatchingModel(JemmyProperties.ROBOT_MODEL_MASK);
        JemmyProperties.setCurrentDispatchingModel(JemmyProperties.getCurrentDispatchingModel() | JemmyProperties.ROBOT_MODEL_MASK);
        JemmyProperties.setCurrentTimeout("EventDispatcher.RobotAutoDelay", 1);
        log("----------------------- DISPATCHING MODEL = " + JemmyProperties.getCurrentDispatchingModel());

        String performanceDataName = setPerformanceName();

        tr.startNewEventList(performanceDataName);
        tr.add(ActionTracker.TRACK_CONFIG_APPLICATION_MESSAGE, "Expected_time=" + expectedTime
                + ", Repeat=" + repeat
                + ", Wait_after_prepare=" + WAIT_AFTER_PREPARE
                + ", Wait_after_open=" + WAIT_AFTER_OPEN
                + ", Wait_after_close=" + WAIT_AFTER_CLOSE
                + ", Wait_paint=" + WAIT_PAINT
                + ", Max_iteration=" + MAX_ITERATION
                + ", logMemory=" + logMemory);

        checkScanFinished(); // just to be sure, that during measurement we will not wait for scanning dialog
        try {
            initialize();

            for (int i = 1; i <= repeat && exceptionDuringMeasurement == null; i++) {
                try {
                    iteration = i;
                    testedComponentOperator = null;
                    tr.startNewEventList("Iteration no." + i);
                    tr.connectToAWT(true);
                    prepare();
                    waitNoEvent(WAIT_AFTER_PREPARE);

                    // Uncomment if you want to run with analyzer tool
                    // com.sun.forte.st.collector.CollectorAPI.resume ();
                    // to be sure EventQueue is empty
                    new QueueTool().waitEmpty();

                    logMemoryUsage();

                    //initializeProfiling();
                    tr.add(ActionTracker.TRACK_TRACE_MESSAGE, OPEN_BEFORE);
                    testedComponentOperator = open();
                    tr.add(ActionTracker.TRACK_TRACE_MESSAGE, OPEN_AFTER);
                    //finishProfiling(i);

                    // this is to optimize delays
                    long wait_time = (wait_after_open_heuristic > WAIT_AFTER_OPEN) ? WAIT_AFTER_OPEN : wait_after_open_heuristic;
                    tr.add(ActionTracker.TRACK_CONFIG_APPLICATION_MESSAGE, "Wait_after_open_heuristic=" + wait_time);
                    Thread.sleep(wait_time);
                    waitNoEvent(wait_time / 4);
                    logMemoryUsage();

                    // we were waiting for painting the component, but after
                    // starting to use RepaintManager it's not possible, so at least
                    // wait for empty EventQueue
                    new QueueTool().waitEmpty();

                    measuredTime[i] = getMeasuredTime();
                    tr.add(ActionTracker.TRACK_APPLICATION_MESSAGE, "Measured Time=" + measuredTime[i], true);
                    // negative HEURISTIC_FACTOR disables heuristic
                    if (HEURISTIC_FACTOR > 0) {
                        wait_after_open_heuristic = (long) (measuredTime[i] * HEURISTIC_FACTOR);
                    }

                    log("Measured Time [" + performanceDataName + " | " + i + "] = " + measuredTime[i]);

                    // the measured time could be 0 (on Windows averything under 7-8 ms is logged as 0), but it shouldn't be under 0
                    if (measuredTime[i] < 0) {
                        System.out.println("@@@ Measured Time is less than 0"); // NOI18N
                        measuredTime[i] = 0;
                    }
                    //throw new Exception("Measured value ["+measuredTime[i]+"] < 0 !!!");
                    reportPerformance(performanceDataName, measuredTime[i], "ms", i, expectedTime);

                    //getScreenshotOfMeasuredIDEInTimeOfMeasurement(i);
                } catch (Exception exc) { // catch for prepare(), open()
                    log("------- [ " + i + " ] ---------------- Exception rises while measuring performance: " + exc);
                    exc.printStackTrace(getLog());
                    getScreenshot("exception_during_open");
                    exceptionDuringMeasurement = exc;
                    // throw new JemmyException("Exception arises during measurement:"+exc.getMessage());
                } finally { // finally for prepare(), open()
                    try {
                        // Uncomment if you want to run with analyzer tool
                        // com.sun.forte.st.collector.CollectorAPI.pause ();

                        tr.add(ActionTracker.TRACK_TRACE_MESSAGE, "CLOSE - before");
                        close();

                        closeAllModal();
                        waitNoEvent(WAIT_AFTER_CLOSE);

                    } catch (Exception e) { // catch for close()
                        log("------- [ " + i + " ] ---------------- Exception rises while closing tested component: " + e);
                        e.printStackTrace(getLog());
                        getScreenshot("exception_during_close");
                        exceptionDuringMeasurement = e;
                        //throw new JemmyException("Exception arises while closing tested component :"+e.getMessage());
                    } finally { // finally for close()
                        tr.connectToAWT(false);
                    }
                }
            }

            tr.startNewEventList("shutdown hooks");
            shutdown();
            closeAllDialogs();
            tr.add(ActionTracker.TRACK_APPLICATION_MESSAGE, "AFTER SHUTDOWN");
        } catch (Exception e) { // catch for initialize(), shutdown(), closeAllDialogs()
            log("----------------------- Exception rises while shuting down / initializing: " + e);
            e.printStackTrace(getLog());
            getScreenshot("exception_during_init_or_shutdown");
            // throw new JemmyException("Exception rises while shuting down :"+e.getMessage());
            exceptionDuringMeasurement = e;
        } finally { // finally for initialize(), shutdown(), closeAllDialogs()
            repaintManager().resetRegionFilters();
        }

        dumpLog();
        if (exceptionDuringMeasurement != null) {
            throw new RuntimeException("Exception {" + exceptionDuringMeasurement + "} rises during measurement.", exceptionDuringMeasurement);
        }
        compare(performanceDataName, measuredTime);
    }

    /**
     * Test that measures memory consumption after each invocation of measured
     * aciotn. Tet finds the lowest value of measured memory consumption and
     * compute all deltas against this value. This method contains the same
     * pattern as previously used method for measuring UI responsiveness
     * {@link measureTime()} . Memory consumption is computed as difference
     * between used and allocated memory (heap). Garbage Collection
     * {@link runGC()} is called then to each measurement of action
     * {@link open()}.
     * <br>
     * <br>If during measurement exception arise - test fails and no value is
     * reported as Performance Data.
     * <p>
     * Each test should reset the state in {@link close()} method. Again there
     * is a waiting for quiet period of time after this call.</p>
     */
    public void measureMemoryUsage() {

        Exception exceptionDuringMeasurement = null;
        long wait_after_open_heuristic = WAIT_AFTER_OPEN;

        long memoryUsageMinimum = 0;
        long[] memoryUsage = new long[repeat_memory + 1];

        useTwoOrderTypes = false;

        JemmyProperties.setCurrentDispatchingModel(JemmyProperties.ROBOT_MODEL_MASK);
        JemmyProperties.setCurrentTimeout("EventDispatcher.RobotAutoDelay", 1);
        log("----------------------- DISPATCHING MODEL = " + JemmyProperties.getCurrentDispatchingModel());

        checkScanFinished(); // just to be sure, that during measurement we will not wait for scanning dialog

        runGC(5);

        initialize();

        for (int i = 1; i <= repeat_memory && exceptionDuringMeasurement == null; i++) {
            try {
                testedComponentOperator = null;

                prepare();

                waitNoEvent(WAIT_AFTER_PREPARE);

                // Uncomment if you want to run with analyzer tool
                // com.sun.forte.st.collector.CollectorAPI.resume ();
                // to be sure EventQueue is empty
                new QueueTool().waitEmpty();

                testedComponentOperator = open();

                long wait_time = (wait_after_open_heuristic > WAIT_AFTER_OPEN) ? WAIT_AFTER_OPEN : wait_after_open_heuristic;
                waitNoEvent(wait_time);

                new QueueTool().waitEmpty();

            } catch (Exception exc) { // catch for prepare(), open()
                exc.printStackTrace(getLog());
                exceptionDuringMeasurement = exc;
                getScreenshot("exception_during_open");
                // throw new JemmyException("Exception arises during measurement:"+exc.getMessage());
            } finally {
                try {
                    // Uncomment if you want to run with analyzer tool
                    // com.sun.forte.st.collector.CollectorAPI.pause ();

                    close();

                    closeAllModal();
                    waitNoEvent(WAIT_AFTER_CLOSE);

                } catch (Exception e) {
                    e.printStackTrace(getLog());
                    getScreenshot("exception_during_close");
                    exceptionDuringMeasurement = e;
                } finally { // finally for initialize(), shutdown(), closeAllDialogs()
                    // XXX export results?
                }
            }

            runGC(3);

            Runtime runtime = Runtime.getRuntime();
            memoryUsage[i] = runtime.totalMemory() - runtime.freeMemory();
            log("Used Memory [" + i + "] = " + memoryUsage[i]);

            if (memoryUsageMinimum == 0 || memoryUsageMinimum > memoryUsage[i]) {
                memoryUsageMinimum = memoryUsage[i];
            }

        }

        // set Performance Data Name
        String performanceDataName = setPerformanceName();

        // report deltas against minimum of measured values
        for (int i = 1; i <= repeat_memory; i++) {
            //String performanceDataName = setPerformanceName(i);
            log("Used Memory [" + performanceDataName + " | " + i + "] = " + memoryUsage[i]);

            reportPerformance(performanceDataName, memoryUsage[i] - memoryUsageMinimum, "bytes", i);
        }

        try {
            shutdown();
            closeAllDialogs();
        } catch (Exception e) {
            e.printStackTrace(getLog());
            //getScreenshot("shutdown");
            exceptionDuringMeasurement = e;
        } finally {
        }

        if (exceptionDuringMeasurement != null) {
            throw new RuntimeException("Exception rises during measurement, look at appropriate log file for stack trace(s).");
        }

    }

    /**
     * Initialize callback that is called once before the repeated sequence of
     * testet operation is perfromed. Default implementation is empty.
     */
    protected void initialize() {
    }

    /**
     * Prepare method is called before at the begining of each measurement The
     * system should be ready to perform measured action when work requested by
     * this method is completed. Default implementation is empty.
     */
    public abstract void prepare();

    /**
     * This method should be overriden in subclasses to triger the measured
     * action. Only last action before UI changing must be specified here (push
     * button, select menuitem, expand tree, ...). Whole method uses for
     * dispatching ROBOT_MODEL_MASK in testing measurement. Default
     * implementation is empty.
     *
     * @return tested component operator that will be later passed to close
     * method
     */
    public abstract ComponentOperator open();

    /**
     * Close opened window, or invoked popup menu. If tested component controled
     * by testedCompponentOperator is Window it will be closed, if it is
     * component ESC key is pressed.
     */
    public void close() {
        if (testedComponentOperator != null && testedComponentOperator.isShowing()) {
            if (testedComponentOperator instanceof WindowOperator) {
                ((WindowOperator) testedComponentOperator).requestClose();
            } else if (testedComponentOperator instanceof TopComponentOperator) {
                ((TopComponentOperator)testedComponentOperator).close();
            } else if (testedComponentOperator instanceof ComponentOperator) {
                testedComponentOperator.pushKey(java.awt.event.KeyEvent.VK_ESCAPE);
                //testedComponentOperator.pressKey(java.awt.event.KeyEvent.VK_ESCAPE);
                //testedComponentOperator.releaseKey(java.awt.event.KeyEvent.VK_ESCAPE);
            }
        }
    }

    /**
     * Shutdown method resets the state of system when all test invocation are
     * done. Default implementation is empty.
     */
    protected void shutdown() {
    }

    /**
     * Method for storing and reporting measured performance value
     *
     * @param name measured value name
     * @param value measured perofrmance value
     * @param unit unit name of measured value
     * @param runOrder order in which the data was measured (1st, 2nd, ...)
     * @param threshold the limit for an action, menu or dialog
     */
    public void reportPerformance(String name, long value, String unit, int runOrder, long threshold) {
        NbPerformanceTest.PerformanceData d = new NbPerformanceTest.PerformanceData();
        d.name = name == null ? getName() : name;
        d.value = value;
        d.unit = unit;
        d.runOrder = (useTwoOrderTypes && runOrder > 1) ? 2 : runOrder;
        d.threshold = threshold;
        data.add(d);
    }

    /**
     * Method for storing and reporting measured performance value
     *
     * @param name measured value name
     * @param value measured perofrmance value
     * @param unit unit name of measured value
     * @param runOrder order in which the data was measured (1st, 2nd, ...)
     */
    public void reportPerformance(String name, long value, String unit, int runOrder) {
        NbPerformanceTest.PerformanceData d = new NbPerformanceTest.PerformanceData();
        d.name = name == null ? getName() : name;
        d.value = value;
        d.unit = unit;
        d.runOrder = (useTwoOrderTypes && runOrder > 1) ? 2 : runOrder;
        data.add(d);
    }

    /**
     * Registers an object to be tracked and later verified in
     * @link #testGC
     *
     * @param message informantion message associated with object
     * @param object traced object
     * @param group mark grouping more refrenced together to test them at once
     * or <CODE>null</CODE>
     */
    protected void reportReference(String message, Object object, Object group) {
        Object g = group == null ? DEFAULT_REFS_GROUP : group;
        if (!tracedRefs.containsKey(g)) {
            tracedRefs.put(g, new HashMap<Reference<Object>, String>());
        }
        tracedRefs.get(g).put(new WeakReference<Object>(object), message);
    }

    /**
     * Generic test case checking if all objects registered with
     * @link #reportReference can be garbage collected.
     *
     * Set of traced objects is cleared after this test.
     * It is supposed that this method will be added to a suite
     * typically at the end.
     *
     * @param group group
     * @throws Exception
     */
    protected void runTestGC(Object group) throws Exception {
        Object g = group == null ? DEFAULT_REFS_GROUP : group;
        try {
            AssertionFailedError afe = null;
            for (Map.Entry<Reference<Object>, String> entry : tracedRefs.get(g).entrySet()) {
                try {
                    assertGC(entry.getValue(), entry.getKey());
                } catch (AssertionFailedError e) {
                    if (afe != null) {
                        Throwable t = e;
                        while (t.getCause() != null) {
                            t = t.getCause();
                        }
                        t.initCause(afe);
                    }
                    afe = e;
                }
            }
            if (afe != null) {
                throw afe;
            }
        } finally {
            tracedRefs.get(g).clear();
        }
    }

    /**
     * Turns off blinking of the caret in the editor. It is restored at the end
     * of test case in {@link #doMeasurement} method
     */
    protected void disableEditorCaretBlinking() {
        Preferences prefs = getMimeLookupPreferences();
        defaultCaretBlinkRate = prefs.getInt(CARET_BLINK_RATE_KEY, 0);
        prefs.putInt(CARET_BLINK_RATE_KEY, 0);
        caretBlinkingDisabled = true;
    }

    /**
     * Restores blinking of the caret in the editor.
     */
    protected void restoreEditorCaretBlinking() {
        if (caretBlinkingDisabled && defaultCaretBlinkRate != 0) {
            getMimeLookupPreferences().putInt(CARET_BLINK_RATE_KEY, defaultCaretBlinkRate);
            caretBlinkingDisabled = false;
        }
    }

    /**
     * Returns Preferences instance which enables to set editor defaults.
     *
     * @return MimeLookup.getLookup("").lookup(Preferences.class);
     */
    private static Preferences getMimeLookupPreferences() {
        try {
            // Lookup lookup = MimeLookup.getLookup("");
            Class<?> mimeLookupClass = Class.forName("org.netbeans.api.editor.mimelookup.MimeLookup", true, Thread.currentThread().getContextClassLoader());
            Method getLookupMethod = mimeLookupClass.getDeclaredMethod("getLookup", String.class);
            Lookup lookup = (Lookup) getLookupMethod.invoke(null, "");
            return lookup.lookup(Preferences.class);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Log used memory size. It can help with evaluation what happend during
     * measurement. If the size of the memory before and after open differs :
     * <li>if increases- there could be memory leak</li>
     * <li>if decreases- there was an garbage collection during measurement - it
     * prolongs the action time</li>
     */
    protected void logMemoryUsage() {
        // log memory usage after each test case
        if (logMemory) {
            Runtime runtime = Runtime.getRuntime();
            long totalMemory = runtime.totalMemory();
            long freeMemory = runtime.freeMemory();
            tr.add(ActionTracker.TRACK_APPLICATION_MESSAGE, "Memory used=" + (totalMemory - freeMemory) + " total=" + totalMemory);
        }
    }

    /**
     * Run Garbage Collection 3 times * number defined as a parameter for this
     * method
     *
     * @param i number of repeat (GC runs i*3 times)
     */
    public void runGC(int i) {
        while (i > 0) {
            try {
                System.runFinalization();
                System.gc();
                Thread.sleep(500);
                System.gc();
                Thread.sleep(500);
                System.gc();
                Thread.sleep(500);
            } catch (Exception exc) {
                exc.printStackTrace(System.err);
            }
            i--;
        }
    }

    /**
     * Set name for performance data. Measured value is stored to database under
     * this name.
     *
     * @return performance data name
     */
    public String setPerformanceName() {
        String performanceDataName = getPerformanceName();

        if (performanceDataName.equalsIgnoreCase("measureTime")) {
            performanceDataName = this.getClass().getName();
        }

        return performanceDataName;
    }

    /**
     * Compare each measured value with expected value. Test fails if more than
     * one of the measured value is bigger than expected one.
     *
     * @param performanceDataName perf name
     * @param measuredValues array of measured values
     */
    public void compare(String performanceDataName, long[] measuredValues) {
        boolean firstTimeUsageFail = false;
        int numberOfFails = 0;
        final int NUMBER_OF_FAILS_THRESHOLD = 1;
        String measuredValuesString = "";

        for (int i = 1; i < measuredValues.length; i++) {
            measuredValuesString = measuredValuesString + " " + measuredValues[i];

            if ((i > 1 && measuredValues[i] > expectedTime)
                    || (i == 1 && measuredValues.length == 1 && measuredValues[i] > expectedTime)) {
                // fail if it's subsequent usage and it's over expected time or it's first usage without any other usages and it's over expected time
                numberOfFails++;
            } else if (i == 1 && measuredValues.length > 1 && measuredValues[i] > 2 * expectedTime) {
                // fail if it's first usage and it isn't the last one and it's over 2-times expected time
                numberOfFails++;
                firstTimeUsageFail = true;
            }
        }

        String suite_fqn = System.getProperty("suitename", "org.netbeans.performance.unknown");
        String suiteName = System.getProperty("suite", "Unknown Test Suite");

        if (numberOfFails > NUMBER_OF_FAILS_THRESHOLD || firstTimeUsageFail) {
            CommonUtilities.xmlTestResults(this.getWorkDirPath(), suiteName, performanceDataName, this.getClass().getCanonicalName(), suite_fqn, "ms", "failed", expectedTime, measuredValues, repeat);
            captureScreen = false;
            fail(numberOfFails + " of the measuredTime(s) [" + measuredValuesString
                    + " ] > expectedTime[" + expectedTime
                    + "] - performance issue (it's ok if the first usage is in boundary of 0 to 2*expectedTime) .");
        }
        CommonUtilities.xmlTestResults(this.getWorkDirPath(), suiteName, performanceDataName, this.getClass().getCanonicalName(), suite_fqn, "ms", "passed", expectedTime, measuredValues, repeat);
    }

    /**
     * If scanning of classpath started wait till the scan finished (just to be
     * sure check it twice after short delay)
     */
    public void checkScanFinished() {
        CommonUtilities.waitScanFinished();
    }

    /**
     * This method returns meaasured time, it goes through all data logged by
     * guitracker (LoggingEventQueue and LoggingRepaintManager). The measured
     * time is the difference between :
     * <ul>
     * <li> last START or
     * <li> last MOUSE_PRESS (if the measure_mouse_press property is true)
     * <li> last MOUSE_RELEASE - by default (if the measure_mouse_press property
     * is false)
     * <li> last KEY_PRESS
     * </ul>
     * and
     * <ul>
     * <li> last or expected paint
     * <li> last FRAME_SHOW
     * <li> last DIALOG_SHOW
     * <li> last COMPONENT_SHOW
     * </ul>
     *
     * @return measured time
     */
    public long getMeasuredTime() {
        for (int attempt = 0;; attempt++) {

            ActionTracker.Tuple start = tr.getCurrentEvents().getFirst();
            ActionTracker.Tuple end = tr.getCurrentEvents().getFirst();

            try {
                for (ActionTracker.Tuple tuple : tr.getCurrentEvents()) {
                    if (tuple == null) {
                        // TODO: Investigate how can this happen?
                        continue;
                    }
                    int code = tuple.getCode();

                    // start 
                    if (code == MY_START_EVENT || (MY_START_EVENT == ActionTracker.TRACK_OPEN_BEFORE_TRACE_MESSAGE
                            && code == ActionTracker.TRACK_TRACE_MESSAGE && tuple.getName().equals(OPEN_BEFORE))
                            || (MY_START_EVENT == ActionTracker.TRACK_OPEN_AFTER_TRACE_MESSAGE
                            && code == ActionTracker.TRACK_TRACE_MESSAGE && tuple.getName().equals(OPEN_AFTER))) {
                        start = tuple;
                    } else if (MY_START_EVENT == MY_EVENT_NOT_AVAILABLE
                            && (code == ActionTracker.TRACK_START
                            || code == track_mouse_event // it could be ActionTracker.TRACK_MOUSE_RELEASE (by default) or ActionTracker.TRACK_MOUSE_PRESS or ActionTracker.TRACK_MOUSE_MOVE
                            || code == ActionTracker.TRACK_KEY_PRESS)) {
                        start = tuple;

                        //end 
                    } else if (code == MY_END_EVENT || (MY_END_EVENT == ActionTracker.TRACK_OPEN_BEFORE_TRACE_MESSAGE
                            && code == ActionTracker.TRACK_TRACE_MESSAGE && tuple.getName().equals(OPEN_BEFORE))
                            || (MY_END_EVENT == ActionTracker.TRACK_OPEN_AFTER_TRACE_MESSAGE
                            && code == ActionTracker.TRACK_TRACE_MESSAGE && tuple.getName().equals(OPEN_AFTER))) {
                        end = tuple;
                    } else if (MY_END_EVENT == MY_EVENT_NOT_AVAILABLE
                            && (code == ActionTracker.TRACK_PAINT
                            || code == ActionTracker.TRACK_FRAME_SHOW
                            || code == ActionTracker.TRACK_DIALOG_SHOW
                            || code == ActionTracker.TRACK_COMPONENT_SHOW)) {
                        end = tuple;
                    }
                }
            } catch (ConcurrentModificationException cme) {
                // It's okay to get it there, we just need to restart calculation                
                if (attempt == 10) {
                    throw new Error("Can't calculate result of measureTime for 10 iterations due to " + cme, cme);
                }
                continue;
            }

            start.setMeasured(true);
            end.setMeasured(true);

            long result = end.getTimeMillis() - start.getTimeMillis();
            System.out.println("@@@@ Start tuple:" + start);
            System.out.println("@@@@ End tuple:" + end);
            if (result < 0 || start.getTimeMillis() == 0) {
                System.out.println("!!!!! Measuring failed, because start [" + start.getTimeMillis() + "] > end [" + end.getTimeMillis() + "] or start=0. Threads in which the measurements were taken:" + start.getMeasurementThreadName() + "   " + end.getMeasurementThreadName() + " !!!!!");
                System.out.println("!*!*!Full tuples list for disgnostic purposes");
                ActionTracker.EventList el = tr.getCurrentEvents();
                for (ActionTracker.Tuple tuple : el) {
                    System.out.println(tuple);
                }
                result = 0;
            }
            return result;
        }
    }

    /**
     * Data are logged to the file, it helps with evaluation of the failure as
     * well as it shows what exactly is meaured (user can find the start event
     * and stop paint/show) .
     */
    public void dumpLog() {
        tr.stopRecording();
        try {
            tr.setXslLocation(getWorkDirPath());
            tr.exportAsXML(getLog("ActionTracker.xml"));
        } catch (Exception ex) {
            throw new Error("Exception while generating log", ex);
        }
        tr.forgetAllEvents();
        tr.startRecording();
    }

    /**
     * Waits for a period of time during which no event is processed by event
     * queue.
     *
     * @param time time to wait for after last event in EventQueue.
     */
    protected void waitNoEvent(long time) {
        if (repeat_memory != -1) {
            try {
                synchronized (Thread.currentThread()) {
                    Thread.currentThread().wait(time);
                }
            } catch (Exception exc) {
                log("Exception rises during waiting " + time + " ms");
                exc.printStackTrace(getLog());
            }
        } else {
            // XXX need to reimplement
            rm.waitNoPaintEvent(time);
        }
    }

    /**
     * Getter for all measured performance data from current test
     *
     * @return PerformanceData[] performance data
     */
    @Override
    public NbPerformanceTest.PerformanceData[] getPerformanceData() {
        if (data != null) {
            return data.toArray(new NbPerformanceTest.PerformanceData[0]);
        } else {
            return null;
        }
    }

    /**
     * Setter for test case name. It is possible to set name of test case, it is
     * useful if you have test suite where called test methods (with the same
     * name) are from different classes, which is true if your tests extend
     * PerformanceTestCase.
     *
     * @param oldName old TestCase name
     * @param newName new TestCase name
     */
    public void setTestCaseName(String oldName, String newName) {
        renamedTestCaseName.put(oldName, newName);
    }

    /**
     * Getter for test case name. It overwrites method getName() from
     * superclass. It is necessary to diversify method names if the test methods
     * (with the same name) are runned from different classes, which is done if
     * your tests extend PerformanceTestCase.
     *
     * @return testCaseName (all '|' are replaced by '#' if it was changed if
     * not call super.getName() !
     */
    @Override
    public String getName() {
        String originalTestCaseName = super.getName();

        if (renamedTestCaseName.containsKey(originalTestCaseName)) {
            return (renamedTestCaseName.get(originalTestCaseName)).replace('|', '-'); // workarround for problem on Win, there isn't possible cretae directories with '|'
        } else {
            return this.getClass().getSimpleName() + "." + originalTestCaseName;
        }
    }

    /**
     * Returns performance data name
     *
     * @return performance data name if it was changed if not call
     * super.getName() !
     */
    public String getPerformanceName() {
        String originalTestCaseName = super.getName();

        if (renamedTestCaseName.containsKey(originalTestCaseName)) {
            return renamedTestCaseName.get(originalTestCaseName);
        } else {
            return originalTestCaseName;
        }
    }

    /**
     * Closes all opened dialogs.
     */
    public static void closeAllDialogs() {
        javax.swing.JDialog dialog;
        org.netbeans.jemmy.ComponentChooser chooser = new org.netbeans.jemmy.ComponentChooser() {
            @Override
            public boolean checkComponent(Component comp) {
                return (comp instanceof javax.swing.JDialog && comp.isShowing());
            }

            @Override
            public String getDescription() {
                return ("Dialog");
            }
        };
        while ((dialog = (javax.swing.JDialog) org.netbeans.jemmy.DialogWaiter.getDialog(chooser)) != null) {
            closeDialogs(findBottomDialog(dialog, chooser), chooser);
        }
    }

    /**
     * Find Bottom dialogs.
     *
     * @param dialog find all dialogs of owner for this dialog
     * @param chooser chooser used for looking for dialogs
     * @return return bottm dialog
     */
    private static javax.swing.JDialog findBottomDialog(javax.swing.JDialog dialog, org.netbeans.jemmy.ComponentChooser chooser) {
        java.awt.Window owner = dialog.getOwner();
        if (chooser.checkComponent(owner)) {
            return (findBottomDialog((javax.swing.JDialog) owner, chooser));
        }
        return (dialog);
    }

    /**
     * Close dialogs
     *
     * @param dialog find all dialogs of owner for this dialog
     * @param chooser chooser used for looking for dialogs
     */
    private static void closeDialogs(javax.swing.JDialog dialog, org.netbeans.jemmy.ComponentChooser chooser) {
        for (Window window : dialog.getOwnedWindows()) {
            if (chooser.checkComponent(window)) {
                closeDialogs((javax.swing.JDialog) window, chooser);
            }
        }
        new org.netbeans.jemmy.operators.JDialogOperator(dialog).requestClose();
    }

    /**
     * Get screenshot - if testedComponentOperator=null - then grab whole screen
     * Black&White, if isn't grab area with testedComponent (-100,-100,
     * width+200, height+200)
     *
     * @param i order of measurement in one test case
     */
    protected void getScreenshotOfMeasuredIDEInTimeOfMeasurement(int i) {
        try {
            if (testedComponentOperator == null) {
                PNGEncoder.captureScreen(getWorkDir().getAbsolutePath() + java.io.File.separator + "screen_" + i + ".png", PNGEncoder.GREYSCALE_MODE);
            } else {
                java.awt.Point locationOnScreen = testedComponentOperator.getLocationOnScreen();
                java.awt.Rectangle bounds = testedComponentOperator.getBounds();
                java.awt.Rectangle bounds_new = new java.awt.Rectangle(locationOnScreen.x - 100, locationOnScreen.y - 100, bounds.width + 200, bounds.height + 200);
                java.awt.Rectangle screen_size = new java.awt.Rectangle(java.awt.Toolkit.getDefaultToolkit().getScreenSize());

                if (bounds_new.height > screen_size.height / 2 || bounds_new.width > screen_size.width / 2) {
                    PNGEncoder.captureScreen(getWorkDir().getAbsolutePath() + java.io.File.separator + "screen_" + i + ".png", PNGEncoder.GREYSCALE_MODE);
                } else {
                    PNGEncoder.captureScreen(bounds_new, getWorkDir().getAbsolutePath() + java.io.File.separator + "screen_" + i + ".png", PNGEncoder.GREYSCALE_MODE);
                }
                //System.err.println("XX "+rm.getRepaintedArea());
                //                PNGEncoder.captureScreen(rm.getRepaintedArea(),getWorkDir().getAbsolutePath()+java.io.File.separator+"screen_"+i+".png",PNGEncoder.GREYSCALE_MODE);
            }
        } catch (Exception exc) {
            log(" Exception rises during capturing screenshot of measurement ");
            exc.printStackTrace(getLog());
        }
    }

    /**
     * Get screenshot of whole screen if exception rise during initialize()
     *
     * @param title title is part of the screenshot file name
     */
    protected void getScreenshot(String title) {
        try {
            PNGEncoder.captureScreen(getWorkDir().getAbsolutePath() + java.io.File.separator + "error_screenshot_" + title + ".png");
        } catch (Exception exc) {
            log(" Exception rises during capturing screenshot ");
            exc.printStackTrace(getLog());
        }

    }

    /**
     * Workaround for issue 145119. Disables NetBeans status bar effects Invoke
     * this from suite() method
     */
    public static void disableStatusBarEffects() {
        System.setProperty("org.openide.awt.StatusDisplayer.DISPLAY_TIME", "0");
    }

    /**
     * Workaround for issue 148463. Disables PHP from opening readme html when
     * PHP Sample Project is created Invoke this from suite() method
     */
    public static void disablePHPReadmeHTML() {
        System.setProperty("org.netbeans.modules.php.samples.donotopenreadmehtml", "true");
    }

    /**
     * This method should be called from suite() method to initialize
     * environment before performance tests are executed
     */
    public static void prepareForMeasurements() {
        disableStatusBarEffects();
        disablePHPReadmeHTML();
    }

    private void initializeProfiling() {
        FileObject fo = FileUtil.getConfigFile("Actions/Profile/org-netbeans-modules-profiler-actions-SelfSamplerAction.instance");
        if (fo == null) {
            return;
        }
        Action a = (Action) fo.getAttribute("delegate"); // NOI18N
        if (a == null) {
            return;
        }
        profile = new Profile(a.getValue("logger-performance")); // NOI18N
    }

    private void finishProfiling(int round) throws Exception {
        if (profile != null) {
            profile.stop(round);
        }
    }

    private class Profile implements Runnable {

        Object profiler;
        boolean profiling;

        public Profile(Object profiler) {
            this.profiler = profiler;
            if (iteration == 1) {
                RequestProcessor.getDefault().post(this, (int) expectedTime * 2);
            } else {
                RequestProcessor.getDefault().post(this, (int) expectedTime);
            }
        }

        @Override
        public synchronized void run() {
            profiling = true;
            if (profiler instanceof Runnable) {
                Runnable r = (Runnable) profiler;
                r.run();
            }
        }

        private synchronized void stop(int round) throws Exception {
            ActionListener ss = (ActionListener) profiler;
            profiler = null;
            if (!profiling) {
                return;
            }
            FileObject wd = FileUtil.toFileObject(getWorkDir());
            String n = FileUtil.findFreeFileName(wd, "snapshot-" + round, "nps"); // NOI18N
            FileObject snapshot = wd.createData(n, "nps"); // NOI18N
            DataOutputStream dos = new DataOutputStream(snapshot.getOutputStream());
            ss.actionPerformed(new ActionEvent(dos, 0, "write")); // NOI18N
            dos.close();
            LOG.log(
                    Level.WARNING, "Profiling snapshot taken into {0}", snapshot.getPath()
            );
        }

    }

    PhaseHandler phaseHandler = new PhaseHandler();

    /**
     * Handler based on logic in
     * org.openide.text.CloneableEditorInitializer.Phase. While document is
     * opening in Editor it goes through several phases in the following order:
     * <pre>
     * FINE [TIMER]: Open Editor, phase DOCUMENT_OPEN, RP [ms]
     * FINE [TIMER]: Open Editor, phase HANDLE_USER_QUESTION_EXCEPTION, EDT [ms]
     * FINE [TIMER]: Open Editor, phase ACTION_MAP, EDT [ms]
     * FINE [TIMER]: Open Editor, phase INIT_KIT, RP [ms]
     * FINE [TIMER]: Open Editor, phase KIT_AND_DOCUMENT_TO_PANE, EDT [ms]
     * FINE [TIMER]: Open Editor, phase CUSTOM_EDITOR_AND_DECORATIONS, EDT [ms]
     * FINE [TIMER]: Open Editor, phase FIRE_PANE_READY, EDT [ms]
     * FINE [TIMER]: Open Editor, phase ANNOTATIONS, RP [ms]
     * </pre><br/>
     * We wait until last phase is finished and then stop recording.
     */
    class PhaseHandler extends Handler {

        @Override
        public void publish(LogRecord record) {
            if (record.getMessage().startsWith("Open Editor, phase ANNOTATIONS")) {
                ActionTracker.getInstance().add(ActionTracker.TRACK_COMPONENT_SHOW, "PhaseHandler - Editor opened.");
                ActionTracker.getInstance().stopRecording();
            }
        }

        @Override
        public void flush() {
        }

        @Override
        public void close() throws SecurityException {
        }
    }

    /**
     * Adds handler to TIMER logger and stop recording when document in editor
     * is completely opened. It should be called in {@link initialize()} method.
     */
    protected void addEditorPhaseHandler() {
        Logger.getLogger("TIMER").setLevel(Level.FINE);
        Logger.getLogger("TIMER").addHandler(phaseHandler);
    }

    /**
     * Removes TIMER handler. It should be called in {@link shutdown()} method.
     */
    protected void removeEditorPhaseHandler() {
        Logger.getLogger("TIMER").removeHandler(phaseHandler);
        Logger.getLogger("TIMER").setLevel(Level.OFF);
    }
}
