/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.netbeans.modules.gsf.testrunner.ui.api;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.lang.reflect.InvocationTargetException;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;
import java.util.concurrent.Semaphore;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JSplitPane;
import javax.swing.SwingUtilities;
import javax.swing.event.AncestorEvent;
import javax.swing.event.AncestorListener;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.modules.gsf.testrunner.api.Report;
import org.netbeans.modules.gsf.testrunner.api.TestSession;
import org.netbeans.modules.gsf.testrunner.api.TestSession.SessionResult;
import org.netbeans.modules.gsf.testrunner.api.TestSuite;
import org.netbeans.modules.gsf.testrunner.api.CommonUtils;
import org.netbeans.modules.gsf.testrunner.api.CoreManager;
import org.netbeans.modules.gsf.testrunner.ui.ResultDisplayHandler;
import org.netbeans.modules.gsf.testrunner.ui.ResultWindow;
import org.netbeans.modules.gsf.testrunner.ui.StatisticsPanel;
import org.openide.awt.Notification;
import org.openide.awt.NotificationDisplayer;
import org.openide.nodes.Node;
import org.openide.util.*;
import org.openide.windows.InputOutput;
import org.openide.windows.Mode;
import org.openide.windows.OutputWriter;
import org.openide.windows.WindowManager;

/**
 * This class gets informed about started and finished test sessions
 * and manages that the result windows and reports in them are appropriately
 * displayed, closed etc.
 * <p/>
 * <i>This is a modified copy of <code>o.n.m.junit.output.Manager</code></i>.
 * @author Marian Petras, Erno Mononen
 */
public final class Manager {

    /**
     * reference to the singleton of this class.
     * Strong references to the singleton are kept in instances of
     * {@link JUnitOutputReader JUnitOutputReader}.
     */
    private static Reference<Manager> instanceRef;

    /**
     * The current test sessions.
     */
    private final Set<TestSession> testSessions = Collections.newSetFromMap(new WeakHashMap<>(5));

    /**
     * if {@code true}, the window will only be promoted
     * at the end of the test session
     */
    private final boolean lateWindowPromotion;

    private static final Logger LOGGER = Logger.getLogger(Manager.class.getName());
    
    public static final String JUNIT_TF = CommonUtils.JUNIT_TF;
    public static final String TESTNG_TF = CommonUtils.TESTNG_TF;
    private Notification bubbleNotification = null;
    private final RequestProcessor.Task bubbleTask;
    private final RequestProcessor RP = new RequestProcessor(Manager.class.getName(), 1, true);
    private TestRunnerNodeFactory nodeFactory;

    /**
     * The line handler to use for printing output.
     */
    private OutputLineHandler lineHandler;

    private static final OutputLineHandler DEFAULT_LINE_HANDLER = new OutputLineHandler() {

        public void handleLine(OutputWriter out, String text) {
            out.println(text);
        }
    };

    /**
     * Gets the line handler for printing output. If no line handler is set, will
     * return the default handler that prints lines without any output listeners
     * (so that e.g. file locations are not clickable).
     * 
     * @return the line handler for printing; never <code>null</code>.
     */
    public OutputLineHandler getOutputLineHandler() {
        return lineHandler != null ? lineHandler : DEFAULT_LINE_HANDLER;
    }

    /**
     * Sets the line handler to use for printing.
     * 
     * @param lineHandler the handler to use.
     */
    public void setOutputLineHandler(OutputLineHandler lineHandler) {
        Parameters.notNull("lineHandler", lineHandler);
        this.lineHandler = lineHandler;
    }

    /**
     *
     * @return the set {@link TestRunnerNodeFactory} or a {@link DefaultTestRunnerNodeFactory} if none was set already.
     */
    public TestRunnerNodeFactory getNodeFactory() {
        if(nodeFactory == null) {
            nodeFactory = new DefaultTestRunnerNodeFactory();
        }
        return nodeFactory;
    }

    /**
     * An appropriate {@link TestRunnerNodeFactory} should be set by a registered {@link CoreManager}.
     * This will take care of the creation of {@link Node}s in the Test Results Window.
     *
     * @param nodeFactory the {@link TestRunnerNodeFactory} to set
     */
    public void setNodeFactory(TestRunnerNodeFactory nodeFactory) {
        this.nodeFactory = nodeFactory;
    }
    
    /**
     * 
     * @see CommonUtils#setTestingFramework(java.lang.String) 
     */
    public void setTestingFramework(String testingFramework) {
        CommonUtils.getInstance().setTestingFramework(testingFramework);
    }

    /**
     *
     * @see CommonUtils#getTestingFramework()
     */
    public String getTestingFramework() {
        return CommonUtils.getInstance().getTestingFramework();
    }

    /**
     * Returns a singleton instance of this class.
     * If no instance exists at the moment, a new instance is created.
     *
     * @return  singleton of this class
     */
    public static Manager getInstance() {
        if (instanceRef != null) {
	    Manager manager = instanceRef.get();
	    if (manager != null) {
		return manager;
	    }
        }

        final Manager instance = new Manager();
        
        ResultWindow.getInstance().addAncestorListener(new AncestorListener() {

            public void ancestorAdded(AncestorEvent event) {
                instance.updateDisplayHandlerLayouts();
            }

            public void ancestorRemoved(AncestorEvent event) {
                instance.updateDisplayHandlerLayouts();
            }

            public void ancestorMoved(AncestorEvent event) {
                instance.updateDisplayHandlerLayouts();
            }
        });
        instanceRef = new WeakReference<Manager>(instance);
        return instance;
    }

    /**
     * Updates the layout orientation of the test result window based on the
     * dimensions of the ResultWindow in its position.
     */
    private void updateDisplayHandlerLayouts() {
        int x = ResultWindow.getInstance().getWidth();
        int y = ResultWindow.getInstance().getHeight();

        int orientation = x > y
                ? JSplitPane.HORIZONTAL_SPLIT
                : JSplitPane.VERTICAL_SPLIT;

        ResultWindow.getInstance().setOrientation(orientation);
    }

    private Manager() {
        lateWindowPromotion = true;
        bubbleTask = RP.create(new Runnable() {

            @Override
            public void run() {
                bubbleNotification.clear();
            }
        });
    }

    public synchronized void emptyTestRun(TestSession session) {
        testStarted(session);
        sessionFinished(session);
    }
    /**
     * Called when a task running tests is started. Displays a message in the test results window.
     *
     * @param session the {@link TestSession} that is started
     */
    @NbBundle.Messages({"LBL_RunningTests=Running..."})
    public synchronized void testStarted(final TestSession session) {
        displayMessage(session, Bundle.LBL_RunningTests());

        if (session.getStartingMsg() != null) {
            displayOutput(session, session.getStartingMsg(), true);
        }
    }

    /**
     * Called when a task finishes running a test session.
     *
     * @param session the {@link TestSession} that is finished
     */
    public synchronized void sessionFinished(final TestSession session) {
        if (!testSessions.contains(session)) {
            /* This session did not run the "junit" task. */
            if (LOGGER.isLoggable(Level.FINE)) {
                LOGGER.log(Level.FINE, "Finishing an unknown session: " + session);
            }
            return;
        }

        displayMessage(session, null, true);  //updates the display

        if (LOGGER.isLoggable(Level.FINE)) {
            LOGGER.log(Level.FINE, "Finishing session: " + session);
        }

        displayHandlers.remove(session);
        testSessions.remove(session);   //must be after displayMessage(...)
                                         //otherwise the window would get
                                         //activated
    }

    /**
     * Called when a task needs to display some output of a running test session.
     *
     * @param session the {@link TestSession} for which output needs to be displayed
     * @param text the text to display
     * @param error {@code true} if this is an error, {@code false} otherwise
     */
    public synchronized void displayOutput(final TestSession session,
                       final String text,
                       final boolean error) {

        final TestResultDisplayHandler displayHandler = getDisplayHandler(session);
        displayHandler.displayOutput(text, error);
        displayInWindow(session, displayHandler);
    }

    /**
     * Called when a task needs to communicate that a test suite is running.
     *
     * @param session the {@link TestSession} that is running
     * @param suiteName name of the running suite; or {@code null} in the case of anonymous suite
     */
    public synchronized void displaySuiteRunning(final TestSession session,
                             final String suiteName) {

        final TestResultDisplayHandler displayHandler = getDisplayHandler(session);
        displayHandler.displaySuiteRunning(suiteName);
        displayInWindow(session, displayHandler);
    }

    /**
     * Called when a task needs to communicate that a test suite is running.
     *
     * @param session the {@link TestSession} that is running
     * @param suite the {@link TestSuite} that is running
     */
    public synchronized void displaySuiteRunning(final TestSession session,
                             final TestSuite suite) {

        final TestResultDisplayHandler displayHandler = getDisplayHandler(session);
        displayHandler.displaySuiteRunning(suite);
        displayInWindow(session, displayHandler);
    }

    /**
     * Called when a task needs to update the corresponding report of a running test session.
     * Merely invokes {@link #displayReport(TestSession, Report, boolean)} with {@code false} as the value of the last parameter.
     *
     * @param session the {@link TestSession} that is running
     * @param report the {@link Report} to be displayed
     * @see #displayReport(TestSession, Report, boolean) 
     */
    public void displayReport(final TestSession session,
                       final Report report) {
        displayReport(session, report, true);
    }

    /**
     * Called when a task needs to update the corresponding report of a running test session.
     *
     * @param session the {@link TestSession} that is running
     * @param report the {@link Report} to be displayed
     * @param completed {@code true} if the {@link TestSession} is completed, {@code false} otherwise
     */
    public synchronized void displayReport(final TestSession session,
                       final Report report, boolean completed) {

        /* Called from the AntLogger's thread */
        report.setCompleted(completed);
        final TestResultDisplayHandler displayHandler = getDisplayHandler(session);
        displayHandler.displayReport(report);
        displayInWindow(session, displayHandler);
    }

    /**
     * Displays a message in the JUnit results window.
     * If this is the first display in the window, it also promotes
     * (displays, activates) it.
     *
     * @param  message  message to be displayed
     */
    private void displayMessage(final TestSession session,
                                final String message) {
        displayMessage(session, message, false);
    }

    /**
     * Displays a message in the JUnit results window.
     * If this is the first display in the window, it also promotes
     * (displays, activates) it.
     *
     * @param  message  message to be displayed
     */
    private void displayMessage(final TestSession session,
                                final String message,
                                final boolean sessionEnd) {
        
        /* Called from the AntLogger's thread */

        final TestResultDisplayHandler displayHandler = getDisplayHandler(session);
        displayInWindow(session, displayHandler, sessionEnd);
        if (!sessionEnd) {
            displayHandler.displayMessage(message);
        } else {
            displayHandler.displayMessageSessionFinished(message);
        }
    }

    /**
     */
    private void displayInWindow(final TestSession session,
                                 final TestResultDisplayHandler displayHandler) {
         displayInWindow(session, displayHandler, false);
    }

    /**
     */
    @NbBundle.Messages({"# {0} - number of successful tests", "# {1} - project", 
        "LBL_NotificationDisplayer_title=Tests ({0}) finished successfully for project: {1}",
        "# {0} - project", 
        "LBL_NotificationDisplayer_NoTestsExecuted_title=No tests executed for project: {0}",
        "LBL_NotificationDisplayer_detailsText=Open Test Results Window"})
    private void displayInWindow(final TestSession session,
                                 final TestResultDisplayHandler displayHandler,
                                 final boolean sessionEnd) {
        final boolean firstDisplay = (testSessions.add(session) == true);

        final boolean promote = session.getSessionType() == TestSession.SessionType.TEST
                ? firstDisplay || sessionEnd
                : sessionEnd;

        final SessionResult sessionResult = session.getSessionResult();
	boolean automaticallyOpen = NbPreferences.forModule(StatisticsPanel.class).getBoolean(StatisticsPanel.PROP_ALWAYS_OPEN_TRW, false);
        if (automaticallyOpen || sessionResult.getErrors() + sessionResult.getFailed() > 0) {
            int displayIndex = getDisplayIndex(session);
            if (displayIndex == -1) {
                addDisplay(session);
                Mutex.EVENT.writeAccess(new Displayer(displayHandler, promote));
            } else if (promote) {
                Mutex.EVENT.writeAccess(new Displayer(null, promote));
            }
        } else {
            if (sessionEnd) {
                Mutex.EVENT.writeAccess(new Runnable() {

                    @Override
                    public void run() {
                        final ResultWindow window = ResultWindow.getInstance();
                        Mode mode = WindowManager.getDefault().findMode(window);
                        boolean isInSlidingMode = mode != null && mode.getName().contains("SlidingSide");   //NOI18N
                        if (window.isOpened() && !isInSlidingMode) {
                            window.promote();
                        } else if (!window.isOpened() || (window.isOpened() && !window.isShowing() && isInSlidingMode)) {
                            Icon icon = ImageUtilities.loadIcon("org/netbeans/modules/gsf/testrunner/ui/resources/testResults.png");   //NOI18N
                            String projectname = ProjectUtils.getInformation(session.getProject()).getDisplayName();
                            int total = displayHandler.getTotalTests();
                            String title = total == 0 ? Bundle.LBL_NotificationDisplayer_NoTestsExecuted_title(projectname) : Bundle.LBL_NotificationDisplayer_title(total, projectname);
                            
                            if(bubbleTask.cancel()) {
                                bubbleTask.schedule(0);
                            }
                            bubbleNotification = NotificationDisplayer.getDefault().notify(title, icon,
                                    Bundle.LBL_NotificationDisplayer_detailsText(), new ActionListener() {

                                @Override
                                public void actionPerformed(ActionEvent e) {
                                    window.promote();
                                    bubbleTask.cancel();
                                }
                            });
                            bubbleTask.schedule(15000);
                        }
                    }
                });
            }
        }
    }

    /**
     *
     */
    private class Displayer implements Runnable {
        private final TestResultDisplayHandler displayHandler;
        private final boolean promote;
        Displayer(final TestResultDisplayHandler displayHandler,
                  final boolean promote) {
            this.displayHandler = displayHandler;
            this.promote = promote;
        }
        public void run() {
            final ResultWindow window = ResultWindow.getInstance();
            if (promote) {
                window.promote();
            }
        }
    }
    
    /** singleton of the <code>ResultDisplayHandler</code> */
    // the ResultDisplayHandler holds TestSession and is referenced from other
    // places so we use WeakReference, otherwise there would be memory leak
    private Map<TestSession,TestResultDisplayHandler> displayHandlers;
    private Semaphore lock;
    /**
     */
    @NbBundle.Messages({"Null_Session_Error=Test session passed was null"})
    private synchronized TestResultDisplayHandler getDisplayHandler(final TestSession session) {
        // just in case a client passes null as a test session catch it early here
        assert session != null : Bundle.Null_Session_Error();
        TestResultDisplayHandler displayHandler = null;
        if (displayHandlers != null) {
            displayHandler = displayHandlers.get(session);
        } else {
            displayHandlers = new WeakHashMap<TestSession,TestResultDisplayHandler>(7);
        }

        if (displayHandler == null) {
            displayHandler = TestResultDisplayHandler.create(session);
            displayHandlers.put(session, displayHandler);
            Object handlerToken = displayHandler.getToken() instanceof WeakReference ? ((WeakReference)displayHandler.getToken()).get() : null;
            if (handlerToken instanceof ResultDisplayHandler) {
                createIO((ResultDisplayHandler)handlerToken);
                lock = new Semaphore(1);
                try {
                    lock.acquire(1);
                } catch (InterruptedException e) {
                    LOGGER.log(Level.FINE, "Current thread was interrupted while acquiring a permit: {0}", e);
                }
                Mutex.EVENT.writeAccess(new Runnable() {

                    @Override
                    public void run() {
                        StatisticsPanel comp = (StatisticsPanel) ((ResultDisplayHandler)handlerToken).getDisplayComponent().getLeftComponent();
                        ((ResultDisplayHandler)handlerToken).setTreePanel(comp.getTreePanel());
                        lock.release();
                    }
                });
                try {
                    lock.acquire(1);
                } catch (InterruptedException e) {
                    LOGGER.log(Level.FINE, "Current thread was interrupted while acquiring a permit: {0}", e);
                }
            }
        }
        return displayHandler;
    }

    /**
     * Creates an <code>IOContainer</code> for the given <code>displayHandler</code>.
     *
     * @param displayHandler
     */
    private void createIO(final ResultDisplayHandler displayHandler) {
        try {
            Runnable r = new Runnable() {
                @Override
                public void run() {
                    final ResultWindow window = ResultWindow.getInstance();
                    window.setOutputComp(displayHandler.getOutputComponent());
                    InputOutput io = displayHandler.createIO(window.getIOContainer());
                    window.addDisplayComponent(displayHandler.getDisplayComponent(), io);
                }
            };
            if (SwingUtilities.isEventDispatchThread()){
                r.run();
            }else{
                SwingUtilities.invokeAndWait(r);
            }
        } catch (InterruptedException ex) {
            // The thread can be interrupted by pressing the Stop button.
            // Do nothing. #167514
        } catch (InvocationTargetException ex) {
            Exceptions.printStackTrace(ex);
        }
    }

    /** */
    private Map<TestSession,Boolean> displaysMap;

    /**
     */
    private int getDisplayIndex(final TestSession session) {
        if (displaysMap == null) {
            return -1;
        }
        Boolean o = displaysMap.get(session);
        return (o != null) ? 0 : -1;
    }

    /**
     */
    private void addDisplay(final TestSession session) {
        if (displaysMap == null) {
            displaysMap = new WeakHashMap<TestSession,Boolean>(4);
        }
        displaysMap.put(session, Boolean.TRUE);
    }

}
