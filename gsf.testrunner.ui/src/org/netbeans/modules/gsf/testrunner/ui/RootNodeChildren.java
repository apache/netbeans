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

import org.netbeans.modules.gsf.testrunner.ui.api.TestsuiteNode;
import java.awt.EventQueue;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import org.netbeans.modules.gsf.testrunner.api.Report;
import org.netbeans.modules.gsf.testrunner.api.TestSession;
import org.netbeans.modules.gsf.testrunner.api.TestSuite;
import org.netbeans.modules.gsf.testrunner.ui.api.Manager;
import org.openide.nodes.ChildFactory;
import org.openide.nodes.Node;

/**
 *
 * @author Marian Petras
 */
final class RootNodeChildren extends ChildFactory<TestsuiteNode> {

    /** */
    private volatile int filterMask;
    /** */
    private Collection<Report> reports;
    /** */
    private volatile int passedSuites;
    /** */
    private volatile int failedSuites;
    /** */
    private String runningSuiteName;
    /** */
    private TestsuiteNode runningSuiteNode;

    private final TestSession session;

    private final List<TestsuiteNode> suiteNodes;
    
    /**
     * Creates a new instance of ReportRootNode
     */
    RootNodeChildren(TestSession session, int filterMask) {
        super();
        this.filterMask = filterMask;
        this.session = session;
        suiteNodes = Collections.synchronizedList(new ArrayList<TestsuiteNode>());
        refresh(false);
    }
    
    @Override
    protected boolean createKeys(List<TestsuiteNode> toPopulate) {
        synchronized (suiteNodes) {
            for (TestsuiteNode suite : suiteNodes) {
                Report report = suite.getReport();
                if ((report != null) && !isMaskApplied(report, filterMask)) {
                    toPopulate.add(suite);
                }
            }
        }
        return true;
    }

    @Override
    protected Node createNodeForKey(TestsuiteNode suite) {
        Report report = suite.getReport();
        if ((suite != runningSuiteNode) && (report != null) && isMaskApplied(report, filterMask)) {
            return null;
        } else {
            return suite;
        }
    }

    void notifyTestSuiteFinished() {
        synchronized (suiteNodes) {
            if (suiteNodes.size() > 0) {
                suiteNodes.get(suiteNodes.size() - 1).notifyTestSuiteFinished();
            }
        }
    }
    
    private boolean isNewRunningSuite() {
        synchronized (suiteNodes) {
            for (TestsuiteNode node : suiteNodes) {
                if (node.getDisplayName().equals(runningSuiteName)) {
                    return false;
                }
            }
        }
        return true;
    }
    
    /**
     * Displays a node with a message about a test suite running.
     *
     * @param  suiteName  name of the running test suite,
     *                    or {@code ANONYMOUS_SUITE} for anonymous suites
     * @see  ResultDisplayHandler#ANONYMOUS_SUITE
     */
    void displaySuiteRunning(final String suiteName) {
        
        /*
         * Called from the EventDispatch thread.
         */
        
        assert EventQueue.isDispatchThread();
        
        runningSuiteName = suiteName;
        
        maybeInitializeNewSuiteNode(null);
        refresh(false);
    }

    /**
     * Displays a node with a message about a test suite running.
     *
     * @param  suite  running test suite,
     *                    or {@code ANONYMOUS_TEST_SUITE} for anonymous suites
     * @see  ResultDisplayHandler#ANONYMOUS_TEST_SUITE
     */
    void displaySuiteRunning(final TestSuite suite) {

        /*
         * Called from the EventDispatch thread.
         */

        assert EventQueue.isDispatchThread();

        runningSuiteName = suite.getName();

        maybeInitializeNewSuiteNode(suite);
        refresh(false);
    }

    Collection<Report> getReports(){
        return reports;
    }

    /**
     */
    public TestsuiteNode displayReport(final Report report) {
        assert EventQueue.isDispatchThread();
        
        TestsuiteNode correspondingNode;
        
        if (reports == null) {
            reports = new ArrayList<Report>(10);
        }
        if (!reports.contains(report))
            reports.add(report);

        updateStatistics(report);
        
        if (runningSuiteNode != null) {
            if (report.isCompleted() || !isMaskApplied(report, filterMask)) {
                runningSuiteNode.displayReport(report);
            }
            if (report.isCompleted() && isMaskApplied(report, filterMask)) {
                runningSuiteNode = null;
            }
            correspondingNode = runningSuiteNode;
        } else {
            if (!(report.isCompleted() && isMaskApplied(report, filterMask))) {
                correspondingNode = getNode(report);
            } else {
                correspondingNode = null;
            }
        }
        
        if (report.isCompleted()){
            runningSuiteName = null;
            runningSuiteNode = null;
        }
        refresh(false);
        return correspondingNode;
    }
    
    /**
     */
    void displayReports(final Collection<Report> newReports) {
        assert EventQueue.isDispatchThread();
        
        if (reports == null) {
            reports = new ArrayList<Report>(newReports);
        } else {
            reports.addAll(newReports);
        }
        
        if (runningSuiteNode == null) {
            for (Report report : reports) {
                updateStatistics(report);
            }
        } else {
            Node[] nodesToAdd;
            if (filterMask == 0) {
                nodesToAdd = new Node[newReports.size()];
                int index = 0;
                for (Report report : newReports) {
                    updateStatistics(report);
                    nodesToAdd[index++] = getNode(report);
                }
            } else {
                List<Node> toAdd = new ArrayList<Node>(newReports.size());
                for (Report report : newReports) {
                    boolean isFailed = !updateStatistics(report);
                    if (isFailed) {
                        toAdd.add(getNode(report));
                    }
                }
                if (!toAdd.isEmpty()) {
                    nodesToAdd = toAdd.toArray(new Node[toAdd.size()]);
                }
            }
        }
        refresh(false);
    }
    
    /**
     * Updates statistics of reports (passed/failed test suites).
     * It is called when a report node is about to be added.
     *
     * @param  report  report for which a node is to be added
     * @return  <code>true</code> if the report reports a passed test suite,
     *          <code>false</code> if the report reports a failed test suite
     */
    private boolean updateStatistics(final Report report) {
        
        /* Called from the EventDispatch thread */
        
        boolean isPassedSuite;
        passedSuites = 0;
        failedSuites = 0;
        for(Report rep: reports){
            isPassedSuite = !rep.containsFailed();
            if (isPassedSuite) {
                passedSuites++;
            } else {
                failedSuites++;
            }
        }
        return !report.containsFailed();
    }
    
    private TestsuiteNode getNode(final Report report) {
        synchronized (suiteNodes) {
            for (TestsuiteNode node : suiteNodes) {
                if (node.getReport() == null || node.getReport() == report) {
                    return node;
                }
            }
        }
        TestsuiteNode node = new TestsuiteNode(report, filterMask != 0);
        node.setFilterMask(filterMask);
        notifyTestSuiteFinished();
        suiteNodes.add(node);
        return node;
    }
    
    private void maybeInitializeNewSuiteNode(TestSuite suite) {
        if (isNewRunningSuite()) {
//            runningSuiteNode = session.getNodeFactory().createTestSuiteNode(runningSuiteName, filterMask != 0);
            runningSuiteNode = Manager.getInstance().getNodeFactory().createTestSuiteNode(runningSuiteName, filterMask != 0);
            runningSuiteNode.setFilterMask(filterMask);
            runningSuiteNode.setSuite(suite);
            notifyTestSuiteFinished();
            suiteNodes.add(runningSuiteNode);
        }
    }
    
    /**
     */
    void setFilterMask(final int filterMask) {
        assert EventQueue.isDispatchThread();
        
        if (filterMask == this.filterMask) {
            return;
        }
        this.filterMask = filterMask;
        
        if (reports == null) {
            return;
        }
        
        synchronized (suiteNodes) {
            for (TestsuiteNode suite : suiteNodes) {
                suite.setFilterMask(filterMask);
                refresh(false);
            }
        }

    }
    
    private boolean isMaskApplied(Report report, int mask){
        return (report.getStatusMask() & ~mask) == 0;
    }
}
