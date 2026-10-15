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

package org.netbeans.modules.gsf.testrunner.ui;

import org.netbeans.modules.gsf.testrunner.ui.api.TestsuiteNode;
import org.netbeans.modules.gsf.testrunner.ui.api.TestMethodNode;
import org.netbeans.modules.gsf.testrunner.ui.api.CallstackFrameNode;
import java.awt.BorderLayout;
import java.awt.EventQueue;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyVetoException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.accessibility.AccessibleContext;
import javax.swing.ActionMap;
import javax.swing.BorderFactory;
import javax.swing.JPanel;
import javax.swing.JToolBar;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.netbeans.modules.gsf.testrunner.ui.api.Locator;
import org.netbeans.modules.gsf.testrunner.api.Report;
import org.netbeans.modules.gsf.testrunner.api.Status;
import org.netbeans.modules.gsf.testrunner.api.TestSuite;
import org.netbeans.modules.gsf.testrunner.api.Testcase;
import org.openide.ErrorManager;
import org.openide.explorer.ExplorerManager;
import org.openide.explorer.ExplorerUtils;
import org.openide.nodes.Node;
import org.openide.util.NbBundle;

/**
 *
 * @author Marian Petras, Erno Mononen
 */
final class ResultPanelTree extends JPanel implements ExplorerManager.Provider, PropertyChangeListener {

    /** manages the tree of nodes representing found objects */
    private final ExplorerManager explorerManager;
    /** root node of the tree */
    private final RootNode rootNode;
    /** */
    private final ResultTreeView treeView;
    /** should the results be filtered (only failures and errors displayed)? */
    private int filterMask = 0;
    /** */
    private ChangeListener changeListener;
    /** */
    private ChangeEvent changeEvent;
    /** */
    private final ResultDisplayHandler displayHandler;

    private final ResultBar resultBar = new ResultBar();
    private final StatisticsPanel statPanel;

    ResultPanelTree(ResultDisplayHandler displayHandler, StatisticsPanel statPanel) {
        super(new BorderLayout());
        treeView = new ResultTreeView();
        treeView.getAccessibleContext().setAccessibleName(Bundle.ACSN_TestResults());
        treeView.getAccessibleContext().setAccessibleDescription(Bundle.ACSD_TestResults());
        treeView.setBorder(BorderFactory.createEtchedBorder());
//        resultBar.setPassedPercentage(0.0f);
        JToolBar toolBar = new JToolBar();
        toolBar.setFloatable(false);
        toolBar.add(resultBar);
        toolBar.setBorder(BorderFactory.createEtchedBorder());

        add(toolBar, BorderLayout.NORTH);
        add(treeView, BorderLayout.CENTER);

        explorerManager = new ExplorerManager();
        explorerManager.setRootContext(rootNode = new RootNode(displayHandler.getSession(), filterMask));
        explorerManager.addPropertyChangeListener(this);

        initAccessibility();

        this.displayHandler = displayHandler;
        this.statPanel = statPanel;
        displayHandler.setLookup(ExplorerUtils.createLookup(explorerManager, new ActionMap()));
    }

    @NbBundle.Messages({"ACSN_HorizontalScrollbar=Horizontal scrollbar of the results panel",
        "ACSN_VerticalScrollbar=Vertical scrollbar of the results panel"})
    private void initAccessibility() {
        AccessibleContext accessCtx;

        accessCtx = getAccessibleContext();
        accessCtx.setAccessibleName(Bundle.ACSN_ResultPanelTree());
        accessCtx.setAccessibleDescription(Bundle.ACSD_ResultPanelTree());

        accessCtx = treeView.getHorizontalScrollBar().getAccessibleContext();
        accessCtx.setAccessibleName(Bundle.ACSN_HorizontalScrollbar());

        accessCtx = treeView.getVerticalScrollBar().getAccessibleContext();
        accessCtx.setAccessibleName(Bundle.ACSN_VerticalScrollbar());

    }
    
    int getTotalTests() {
        return rootNode.getTotalTests();
    }

    /**
     */
    void displayMsg(String msg) {
        assert EventQueue.isDispatchThread();

        /* Called from the EventDispatch thread */

        rootNode.displayMessage(msg);
    }

    /**
     */
    void displayMsgSessionFinished(String msg) {
        assert EventQueue.isDispatchThread();

        /* Called from the EventDispatch thread */

        rootNode.displayMessageSessionFinished(msg);
        resultBar.stop();
        statPanel.updateButtons();
    }

    /**
     */
    @Override
    public void addNotify() {
        super.addNotify();

        displayHandler.setTreePanel(this);
    }

    /**
     * Displays a message about a running suite.
     *
     * @param  suiteName  name of the running suite,
     *                    or {@code ANONYMOUS_SUITE} for anonymous suites
     * @see  ResultDisplayHandler#ANONYMOUS_SUITE
     */
    void displaySuiteRunning(final String suiteName) {
        assert EventQueue.isDispatchThread();

        /* Called from the EventDispatch thread */

        rootNode.displaySuiteRunning(suiteName);
    }

    /**
     * Displays a message about a running suite.
     *
     * @param  suite  running suite,
     *                    or {@code ANONYMOUS_TEST_SUITE} for anonymous suites
     * @see  ResultDisplayHandler#ANONYMOUS_TEST_SUITE
     */
    void displaySuiteRunning(final TestSuite suite) {
        assert EventQueue.isDispatchThread();

        /* Called from the EventDispatch thread */

        rootNode.displaySuiteRunning(suite);
    }

    /**
     */
    void displayReport(final Report report) {
        assert EventQueue.isDispatchThread();

        /* Called from the EventDispatch thread */

        TestsuiteNode node = rootNode.displayReport(report);
        if ((node != null) && report.isCompleted() && (report.getTotalTests() == 1 || report.containsFailed() || Status.PENDING == report.getStatus())) {
            if (node.getChildren().getNodesCount() != 0){
                treeView.expandReportNode(node);
            }
        }
        resultBar.setPassedPercentage(rootNode.getPassedPercentage());
        resultBar.setSkippedPercentage(rootNode.getSkippedPercentage());
        resultBar.setAbortedPercentage(rootNode.getAbortedPercentage());
        statPanel.updateButtons();
    }

    /**
     * @param  reports  non-empty list of reports to be displayed
     */
    public void displayReports(final List<Report> reports) {
        assert EventQueue.isDispatchThread();

        /* Called from the EventDispatch thread */

        if (reports.size() == 1) {
            displayReport(reports.get(0));
        } else {
            rootNode.displayReports(reports);
        }
        resultBar.setPassedPercentage(rootNode.getPassedPercentage());
        resultBar.setSkippedPercentage(rootNode.getSkippedPercentage());
        resultBar.setAbortedPercentage(rootNode.getAbortedPercentage());
        statPanel.updateButtons();
   }

    /**
     */
    int getSuccessDisplayedLevel() {
        return rootNode.getSuccessDisplayedLevel();
    }

    /**
     */
    void viewOpened() {
        assert EventQueue.isDispatchThread();

        //PENDING:
        //selectAndActivateNode(rootNode);
    }

    /**
     */
    void setFilterMask(final int filterMask) {
        if (filterMask == this.filterMask) {
            return;
        }

        this.filterMask = filterMask;
        rootNode.setFilterMask(filterMask);
    }

    /**
     */
    @Override
    public void propertyChange(PropertyChangeEvent e) {
        if (ExplorerManager.PROP_SELECTED_NODES.equals(
                        e.getPropertyName())) {
            nodeSelectionChanged();
        }
    }

    /**
     */
    private void nodeSelectionChanged() {
        assert EventQueue.isDispatchThread();

        fireChange();
    }

    /**
     */
    void setChangeListener(ChangeListener l) {
        assert EventQueue.isDispatchThread();

        this.changeListener = l;
        if (changeListener == null) {
            changeEvent = null;
        } else if (changeEvent == null) {
            changeEvent = new ChangeEvent(this);
        }
    }

    /**
     */
    private void fireNodeSelectionChange() {
        fireChange();
    }

    /**
     */
    private void fireChange() {
        assert EventQueue.isDispatchThread();

        if (changeListener != null) {
            changeListener.stateChanged(changeEvent);
        }
    }

    /**
     */
    Node[] getSelectedNodes() {
        return explorerManager.getSelectedNodes();
    }

    Set<Testcase> getFailedTests(){
        Set<Testcase> failedTests = new HashSet<>();
        for(Testcase tc:displayHandler.getSession().getAllTestCases()){
            if (Status.isFailureOrError(tc.getStatus())){
                failedTests.add(tc);
            }
        }
        return failedTests;
    }

    /**
     * Selects and activates a given node.
     * Selects a given node in the tree.
     * If the nodes cannot be selected and/or activated,
     * clears the selection (and notifies that no node is currently
     * activated).
     *
     * @param  node  node to be selected and activated
     */
    private void selectAndActivateNode(final Node node) {
        Node[] nodeArray = new Node[] {node};
        try {
            explorerManager.setSelectedNodes(nodeArray);
            fireNodeSelectionChange();
        } catch (PropertyVetoException ex) {
            ErrorManager.getDefault().notify(ErrorManager.EXCEPTION, ex);
            nodeArray = new Node[0];
            try {
                explorerManager.setSelectedNodes(nodeArray);
                fireNodeSelectionChange();
            } catch (PropertyVetoException ex2) {
                ErrorManager.getDefault().notify(ErrorManager.EXCEPTION, ex2);
            }
        }
        Locator locator = node.getLookup().lookup(Locator.class);
        if (locator != null) {
            locator.jumpToSource(node);
        }
    }

    private List<TestMethodNode> getFailedTestMethodNodes() {
        List<TestMethodNode> result = new ArrayList<>();
        for (Node each : explorerManager.getRootContext().getChildren().getNodes()) {
            if (each instanceof TestsuiteNode) {
                TestsuiteNode suite = (TestsuiteNode) each;
                for (Node node : suite.getChildren().getNodes()) {
                    if (node instanceof TestMethodNode) {
                        TestMethodNode testMethod = (TestMethodNode) node;
                        if (testMethod.failed()) {
                            result.add(testMethod);
                        }
                    }
                }
            }
        }
        return result;
    }

    private TestMethodNode getFirstFailedTestMethodNode() {
        List<TestMethodNode> failed = getFailedTestMethodNodes();
        return failed.isEmpty() ? null : failed.get(0);
    }

    private List<TestsuiteNode> getFailedSuiteNodes(TestsuiteNode selected) {
        List<TestsuiteNode> before = new ArrayList<>();
        List<TestsuiteNode> after = new ArrayList<>();
        boolean selectedEncountered = false;
        for (Node each : explorerManager.getRootContext().getChildren().getNodes()) {
            if (each instanceof TestsuiteNode) {
                TestsuiteNode suite = (TestsuiteNode) each;
                if (suite.equals(selected)) {
                    selectedEncountered = true;
                }
                for (Node node : suite.getChildren().getNodes()) {
                    if (node instanceof TestMethodNode) {
                        TestMethodNode testMethod = (TestMethodNode) node;
                        if (testMethod.failed()) {
                            if (selectedEncountered) {
                                after.add(suite);
                            } else {
                                before.add(suite);
                            }
                            break;
                        }
                    }
                }
            }
        }
        after.addAll(before);
        return after;
    }


    void selectPreviousFailure() {
        Node[] selectedNodes = getSelectedNodes();
        if (selectedNodes.length == 0) {
            List<TestMethodNode> failedNodes = getFailedTestMethodNodes();
            if (!failedNodes.isEmpty()) {
                selectAndActivateNode(failedNodes.get(failedNodes.size() - 1));
            }
            return;
        }
        Node selected = selectedNodes[0];
        TestsuiteNode suite = getSelectedSuite(selected);
        if (suite == null) {
            return;
        }

        Node[] children = suite.getChildren().getNodes();
        boolean selectedEncountered = false;
        for (int i = children.length; i > 0; i--) {
            TestMethodNode testMethod = (TestMethodNode) children[i - 1];
            if (isSelected(testMethod, selected)) {
                selectedEncountered = true;
                continue;
            }
            if (selectedEncountered && testMethod.failed()) {
                selectAndActivateNode(testMethod);
                return;
            }
        }

        List<TestsuiteNode> failedSuites = getFailedSuiteNodes(suite);
        failedSuites.remove(suite);
        Collections.reverse(failedSuites);
        for (TestsuiteNode suiteNode : failedSuites) {
            children = suiteNode.getChildren().getNodes();
            for (int i = children.length; i > 0; i--) {
                TestMethodNode testMethod = (TestMethodNode) children[i - 1];
                if (testMethod.failed()) {
                    selectAndActivateNode(testMethod);
                    return;
                }
            }
        }
    }

    void selectNextFailure() {
        Node[] selectedNodes = getSelectedNodes();

        if (selectedNodes.length == 0) {
            Node firstFailed = getFirstFailedTestMethodNode();
            if (firstFailed != null) {
                selectAndActivateNode(firstFailed);
            }
            return;
        }

        Node selected = selectedNodes[0];
        TestsuiteNode suite = getSelectedSuite(selected);
        if (suite == null) {
            return;
        }
        boolean selectedEncountered = selected.equals(suite);
        for (Node child : suite.getChildren().getNodes()) {
            if (child instanceof TestMethodNode) {
                TestMethodNode testMethod = (TestMethodNode) child;
                if (!selectedEncountered && isSelected(testMethod, selected)) {
                    selectedEncountered = true;
                    continue;
                }
                if (selectedEncountered && testMethod.failed()) {
                    selectAndActivateNode(testMethod);
                    return;
                }
            }
        }
        List<TestsuiteNode> failedSuites = getFailedSuiteNodes(suite);
        if (selectedEncountered) {
            failedSuites.remove(suite);
        }
        for (TestsuiteNode suiteNode : failedSuites) {
            for (Node child : suiteNode.getChildren().getNodes()) {
                TestMethodNode testMethod = (TestMethodNode) child;
                if (testMethod.failed()) {
                    selectAndActivateNode(testMethod);
                    return;
                }
            }

        }
    }
    
    private boolean isSelected(TestMethodNode testMethod, Node selected) {
       if (testMethod.equals(selected)) {
           return true;
       }
       for (Node node : testMethod.getChildren().getNodes()) {
           if (node.equals(selected)) {
               return true;
           }
       }
       return false;
    }

    private TestsuiteNode getSelectedSuite(Node selected) {
        if (selected instanceof TestMethodNode) {
            return (TestsuiteNode) selected.getParentNode();
        } else if (selected instanceof TestsuiteNode) {
            return (TestsuiteNode) selected;
        } else if (selected instanceof CallstackFrameNode) {
            return (TestsuiteNode) selected.getParentNode().getParentNode();
        }
        return getFirstFailedSuite();
    }


    private TestsuiteNode getFirstFailedSuite() {
        List<TestsuiteNode> suites = getFailedSuiteNodes(null);
        return suites.isEmpty() ? null : suites.get(0);
    }
    /**
     */
    @Override
    public ExplorerManager getExplorerManager() {
        return explorerManager;
    }

    /**
     */
    @Override
    public boolean requestFocusInWindow() {
        return treeView.requestFocusInWindow();
    }

}
