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
package org.netbeans.modules.gsf.testrunner.ui;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.EventQueue;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JSplitPane;
import org.netbeans.modules.gsf.testrunner.ui.TestRunnerSettings.DividerSettings;
import org.netbeans.modules.gsf.testrunner.api.Report;
import org.netbeans.modules.gsf.testrunner.api.TestSession;
import org.netbeans.modules.gsf.testrunner.api.TestSuite;
import org.netbeans.modules.gsf.testrunner.ui.api.Manager;
import org.openide.ErrorManager;
import org.openide.util.Lookup;
import org.openide.windows.IOContainer;
import org.openide.windows.IOProvider;
import org.openide.windows.InputOutput;
import org.openide.windows.OutputWriter;

/**
 *
 * @author Marian Petras. Erno Mononen
 */
public final class ResultDisplayHandler {

    private static final Logger LOGGER = Logger.getLogger(ResultDisplayHandler.class.getName());

    /** */
    private static java.util.ResourceBundle bundle = org.openide.util.NbBundle.getBundle(
            ResultDisplayHandler.class);
    /** */
    private ResultPanelTree treePanel;

    /** */
    private JSplitPane displayComp;

    private JComponent outputComponent;

    private InputOutput inOut;

    private final TestSession session;

    private Lookup l;
    
    private StatisticsPanel statisticsPanel;

    /** Creates a new instance of ResultDisplayHandler */
    public ResultDisplayHandler(TestSession session) {
        this.session = session;
    }

    public JComponent getOutputComponent() {
        if (outputComponent == null) {
            outputComponent = new JPanel(new BorderLayout());
            outputComponent.setBorder(
                    BorderFactory.createEmptyBorder(0, 10, 0, 0)); // #181873
        }
        return outputComponent;
    }

    public InputOutput createIO(IOContainer ioContainer) {
        inOut = IOProvider.getDefault().getIO("test-results", null, ioContainer); //NOI18N
        return inOut;
    }


    public TestSession getSession() {
        return session;
    }

    /**
     */
    public JSplitPane getDisplayComponent() {
        if (displayComp == null) {
            displayComp = createDisplayComp();
        }
        return displayComp;
    }

    /**
     */
    private JSplitPane createDisplayComp() {
        DividerSettings dividerSettings = TestRunnerSettings.getDefault().getDividerSettings(null);
        statisticsPanel = new StatisticsPanel(this);
        return createDisplayComp(statisticsPanel,
                getOutputComponent(),
                dividerSettings.getOrientation(),
                dividerSettings.getLocation());
    }

    public int getTotalTests() {
        return statisticsPanel.getTreePanel().getTotalTests();
    }

    private JSplitPane createDisplayComp(Component left, Component right, int orientation, final int location) {

        final JSplitPane splitPane = new JSplitPane(orientation, true, left, right);
        splitPane.setBorder(BorderFactory.createEmptyBorder());
        splitPane.setDividerLocation(location);
        splitPane.getAccessibleContext().setAccessibleName(bundle.getString("ACSN_ResultPanelTree"));
        splitPane.getAccessibleContext().setAccessibleDescription(bundle.getString("ACSD_ResultPanelTree"));
        splitPane.addPropertyChangeListener(JSplitPane.DIVIDER_LOCATION_PROPERTY, new PropertyChangeListener() {

            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                DividerSettings dividerSettings = new DividerSettings(splitPane.getOrientation(), splitPane.getDividerLocation());
                TestRunnerSettings.getDefault().setDividerSettings(dividerSettings);
            }
        });
        splitPane.addPropertyChangeListener(JSplitPane.ORIENTATION_PROPERTY, new PropertyChangeListener() {

            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                DividerSettings dividerSettings = new DividerSettings(splitPane.getOrientation(), splitPane.getDividerLocation());
                TestRunnerSettings.getDefault().setDividerSettings(dividerSettings);
            }
        });
        splitPane.setToolTipText(session.getName());
        return splitPane;
    }

    /**
     */
    void displayShown() {
        //
        //PENDING
        //
    }

    /**
     */
    void displayHidden() {
        //
        //PENDING
        //
    }
    //------------------ DISPLAYING OUTPUT ----------------------//
    static final Object[] EMPTY_QUEUE = new Object[0];
    private final Object queueLock = new Object();
    private volatile Object[] outputQueue;
    private volatile int outputQueueSize = 0;

    /**
     */
    Object getOutputQueueLock() {
        return queueLock;
    }

    /**
     */
    public void displayOutput(final String text, final boolean error) {

        /* Called from the AntLogger's thread */

        if (inOut != null) {
            OutputWriter out = error ? inOut.getErr() : inOut.getOut();
            Manager.getInstance().getOutputLineHandler().handleLine(out, text);
        } else {
            // log a warning - should rather throw an exception, but we're too close to release now
            LOGGER.log(Level.WARNING, "Tried to display output before inOut was initialized. Output text: {0}", text);
        }
    }

    /**
     */
    Object[] consumeOutput() {
        synchronized (queueLock) {
            if (outputQueueSize == 0) {
                return EMPTY_QUEUE;
            }
            Object[] passedQueue = outputQueue;
            outputQueue = null;
            outputQueueSize = 0;
            return passedQueue;
        }
    }
    //-----------------------------------------------------------//
    //------------------- DISPLAYING TREE -----------------------//
    /**
     * name of the currently running suite - to be passed to the
     * {@link #treePanel} once it is initialized
     */
    private String runningSuite;
    private final List<Report> reports = new ArrayList<Report>();
    private String message;
    boolean sessionFinished;

    /**
     *
     * @param  suiteName  name of the running suite; or {@code null} in the case
     *                    of anonymous suite
     */
    public void displaySuiteRunning(String suiteName) {

        synchronized (this) {

            assert runningSuite == null;

            suiteName = (suiteName != null) ? suiteName : TestSuite.ANONYMOUS_SUITE;

            if (treePanel == null) {
                runningSuite = suiteName;
                return;
            }
        }
        displayInDispatchThread(prepareMethod("displaySuiteRunning", String.class), suiteName);      //NOI18N
    }

    /**
     *
     * @param  suite  name of the running suite
     */
    public void displaySuiteRunning(TestSuite suite) {
        synchronized (this) {
            assert runningSuite == null;
            suite = (suite != null) ? suite : TestSuite.ANONYMOUS_TEST_SUITE;
            if (treePanel == null) {
                runningSuite = suite.getName();
                return;
            }
        }
        displayInDispatchThread(prepareMethod("displaySuiteRunning", TestSuite.class), suite);      //NOI18N
    }

    /**
     */
    public void displayReport(final Report report) {

        synchronized (this) {
            if (treePanel == null) {
                if (!reports.contains(report))
                    reports.add(report);
                runningSuite = null;
                return;
            }
            assert runningSuite == null;
        }

        displayInDispatchThread(prepareMethod("displayReport", Report.class), report);               //NOI18N

    }

    /**
     */
    public void displayMessage(final String msg) {

        /* Called from the AntLogger's thread */

        synchronized (this) {
            if (treePanel == null) {
                message = msg;
                return;
            }
        }

        displayInDispatchThread(prepareMethod("displayMsg", String.class), msg);                     //NOI18N

    }

    /**
     */
    public void displayMessageSessionFinished(final String msg) {

        /* Called from the AntLogger's thread */

        synchronized (this) {
            if (treePanel == null) {
                message = msg;
                return;
            }
            sessionFinished = true;
        }

        displayInDispatchThread(prepareMethod("displayMsgSessionFinished", String.class), msg);        //NOI18N

    }
    /** */
    private Map<String, Method> methodsMap = new HashMap<String, Method>();

    /**
     * Calls a given display-method of class {@code ResutlPanelTree}
     * in the AWT event queue thread.
     *
     * @param  methodName  name of the {@code ResultPanelTree} method
     * @param  param  argument to be passed to the method
     */
    private void displayInDispatchThread(final Method method, final Object param) {
        assert method != null;

        final Method finalMethod = method;

        EventQueue.invokeLater(new Runnable() {

            @Override
            public void run() {
                try {
                    if (LOGGER.isLoggable(Level.FINE)) {
                        LOGGER.log(Level.FINE, "Invoking: {0} with param: {1}",
                                new Object[]{method.getName(), param});
                    }
                    finalMethod.invoke(treePanel, new Object[]{param});
                } catch (InvocationTargetException ex) {
                    ErrorManager.getDefault().notify(ex.getTargetException());
                } catch (Exception ex) {
                    ErrorManager.getDefault().notify(ErrorManager.ERROR, ex);
                }
            }
        });
    }

    /**
     */
    private synchronized Method prepareMethod(final String methodName, final Class paramType) {
        Method method = methodsMap.get(methodName + "_" + paramType.getName()); //NOI18N

        if (method == null) {
            try {
                method = ResultPanelTree.class.getDeclaredMethod(methodName, new Class[]{paramType});
            } catch (Exception ex) {
                method = null;
                ErrorManager.getDefault().notify(ErrorManager.ERROR, ex);
            }
            methodsMap.put(methodName + "_" + paramType.getName(), method); //NOI18N
        }

        return method;
    }

    /**
     */
    public synchronized void setTreePanel(final ResultPanelTree treePanel) {
        assert EventQueue.isDispatchThread();

        /* Called from the EventDispatch thread */

        if (this.treePanel != null) {
            return;
        }

        this.treePanel = treePanel;

        if (message != null) {
            treePanel.displayMsg(message);
            message = null;
        }
        if (!reports.isEmpty()) {
            treePanel.displayReports(reports);
            reports.clear();
        }
        if (runningSuite != null) {
            treePanel.displaySuiteRunning(runningSuite != TestSuite.ANONYMOUS_SUITE
                    ? runningSuite
                    : null);
            runningSuite = null;
        }
        if (sessionFinished) {
            treePanel.displayMsgSessionFinished(message);
        }
    }

    void setLookup(Lookup l) {
        this.l = l;
    }

    Lookup getLookup() {
        return l;
    }
}
