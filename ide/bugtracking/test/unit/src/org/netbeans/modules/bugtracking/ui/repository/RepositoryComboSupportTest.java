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

package org.netbeans.modules.bugtracking.ui.repository;

import java.awt.EventQueue;
import java.io.File;
import java.io.IOException;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import javax.swing.JComboBox;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.netbeans.modules.bugtracking.dummies.DummyBugtrackingConnector;
import org.netbeans.modules.bugtracking.dummies.DummyBugtrackingOwnerSupport;
import org.netbeans.modules.bugtracking.dummies.DummyRepositories;
import org.netbeans.modules.bugtracking.dummies.DummyNode;
import org.netbeans.modules.bugtracking.dummies.DummyTopComponentRegistry;
import org.netbeans.modules.bugtracking.dummies.DummyWindowManager;
import org.netbeans.modules.bugtracking.ui.repository.RepositoryComboSupport.Progress;
import org.openide.nodes.Node;
import org.openide.util.lookup.AbstractLookup;
import org.openide.util.lookup.InstanceContent;
import static java.lang.Boolean.FALSE;
import static java.lang.Boolean.TRUE;
import static org.junit.Assert.*;
import org.netbeans.modules.bugtracking.BugtrackingManager;
import org.netbeans.modules.bugtracking.DelegatingConnector;
import org.netbeans.modules.bugtracking.api.IssueQuickSearch.RepositoryFilter;
import org.netbeans.modules.bugtracking.api.Repository;
import org.netbeans.modules.bugtracking.dummies.DummyProjectServices;
import static org.netbeans.modules.bugtracking.ui.repository.RepositoryComboSupport.LOADING_REPOSITORIES;
import static org.netbeans.modules.bugtracking.ui.repository.RepositoryComboSupport.SELECT_REPOSITORY;
import static org.netbeans.modules.bugtracking.ui.repository.RepositoryComboSupportTest.ThreadType.AWT;
import static org.netbeans.modules.bugtracking.ui.repository.RepositoryComboSupportTest.ThreadType.NON_AWT;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;
import org.openide.util.test.MockLookup;

/**
 *
 * @author Marian Petras
 */
public class RepositoryComboSupportTest {
    private volatile JComboBox comboBox;
    private volatile RepositoryComboSupport comboSupport;

    @BeforeClass
    public static void setLookup() {
        MockLookup.setLayersAndInstances(
            new DummyRepositories(), 
            new DummyWindowManager(), 
            new DummyBugtrackingOwnerSupport(),
            new DummyProjectServices(),
            new DelegatingConnector(
                new DummyBugtrackingConnector(),
                DummyBugtrackingConnector.ID,
                DummyBugtrackingConnector.DISPLAY_NAME,
                DummyBugtrackingConnector.TOOLTIP,
                null));
    }

    @Before
    public void createJComboBox() {
        comboBox = new JComboBox();
    }

    @After
    public void tidyUp() {
        comboBox = null;
        comboSupport = null;
        DummyBugtrackingConnector.instance.reset();
        getTopComponentRegistry().reset();
        getBugtrackingOwnerSupport().reset();
    }

    private static DummyTopComponentRegistry getTopComponentRegistry() {
        return Lookup.getDefault().lookup(DummyWindowManager.class).registry;
    }

    private static DummyBugtrackingOwnerSupport getBugtrackingOwnerSupport() {
        return Lookup.getDefault().lookup(DummyBugtrackingOwnerSupport.class);
    }

    abstract class AbstractRepositoryComboTezt {

        protected DummyBugtrackingConnector connector = DummyBugtrackingConnector.instance;

        protected Node node1;
        protected Node node2;
        protected Node node3;
        protected Node repoNode1;
        protected Node repoNode2;
        protected Node repoNode3;
        protected Repository repository1;
        protected Repository repository2;
        protected Repository repository3;

        public AbstractRepositoryComboTezt() {
            node1 = new DummyNode("node1");
            node2 = new DummyNode("node2");
            node3 = new DummyNode("node3");
            
        
            DelegatingConnector[] conns = BugtrackingManager.getInstance().getConnectors();
            for (DelegatingConnector dc : conns) {
                if(dc.getDelegate() instanceof DummyBugtrackingConnector) {
                    connector = (DummyBugtrackingConnector) dc.getDelegate();
                    break;
                }
            }
        }
        
        protected void createRepository1() {
            repository1 = connector.createRepository("alpha");
            repoNode1 = createDummyNode("node1", repository1);
        }

        protected void createRepository2() {
            repository2 = connector.createRepository("beta", false);
            repoNode2 = createDummyNode("node2", repository2);
        }

        protected void createRepository3() {
            repository3 = connector.createRepository("gamma");
            repoNode3 = createDummyNode("node3", repository3);
        }

        protected void createRepositories() {
            createRepository1();
            createRepository2();
            createRepository3();
        }

        protected void setUpEnvironment() {
            //the default implementation does nothing
        }

        abstract RepositoryComboSupport setupComboSupport(JComboBox comboBox);

        protected void scheduleTests(ProgressTester progressTester) {
            progressTester.scheduleTest          (Progress.STARTED, AWT,
                                                    new ComboBoxItemsTezt(
                                                            LOADING_REPOSITORIES));
            progressTester.scheduleTest          (Progress.WILL_LOAD_REPOS, NON_AWT);
            progressTester.scheduleTest          (Progress.LOADED_REPOS, NON_AWT);
            progressTester.scheduleTest          (Progress.WILL_SCHEDULE_DISPLAY_OF_REPOS, NON_AWT);
            progressTester.scheduleSuspendingTest(Progress.SCHEDULED_DISPLAY_OF_REPOS, NON_AWT);
            progressTester.scheduleTest          (Progress.WILL_DISPLAY_REPOS, AWT);
        }
    }    

    private Node createDummyNode(String node, Repository repo) {
        try {
            File f = File.createTempFile("repotest-" + node, "");
            FileObject fo = FileUtil.toFileObject(f);
            return new DummyNode(node, repo, fo);
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
            fail();
        }
        return null;
    }

    abstract class SingleRepoComboTezt extends AbstractRepositoryComboTezt {
        @Override
        protected void createRepositories() {
            createRepository1();
        }
    }

    @Test(timeout=10000)
    public void testNoRepoAvailable() throws InterruptedException {
        printTestName("testNoRepoAvailable");
        runRepositoryComboTest(new AbstractRepositoryComboTezt() {
            @Override
            protected void createRepositories() {
                //do not create any repository
            }
            @Override
            RepositoryComboSupport setupComboSupport(JComboBox comboBox) {
                return RepositoryComboSupport.setup(null, comboBox, true);
            }
            @Override
            protected void scheduleTests(ProgressTester progressTester) {
                super.scheduleTests(progressTester);
                progressTester.scheduleResumingTest  (Progress.DISPLAYED_REPOS, AWT,
                                                        new ComboBoxItemsTezt(
                                                                RepositoryComboSupport.NO_REPOSITORIES),
                                                        new SelectedItemtezt(
                                                                RepositoryComboSupport.NO_REPOSITORIES));
            }
        });
    }

    /**
     * Tests that the routine for determination if initial repository selection
     * is skipped (not performed) if there is only one repository available
     * and method {@code setup()} was passed {@code true} as its third argument.
     *
     * @see #testSingleRepoNoNodeFalse
     * @see #testSingleRepoNoMatchingNodeFalse
     * @see #testSingleRepoMatchingNodeFalse
     */
    @Test(timeout=10000)
    public void testSingleRepoTrue() throws InterruptedException {
        printTestName("testSingleRepoNoNodeTrue");
        runRepositoryComboTest(new SingleRepoComboTezt() {
            @Override
            RepositoryComboSupport setupComboSupport(JComboBox comboBox) {
                return RepositoryComboSupport.setup(null, comboBox, true);
            }
            @Override
            protected void scheduleTests(ProgressTester progressTester) {
                super.scheduleTests(progressTester);
                progressTester.scheduleResumingTest  (Progress.DISPLAYED_REPOS, AWT,
                                                        new ComboBoxItemsTezt(
                                                                repository1),
                                                        new SelectedItemtezt(
                                                                repository1));
            }
        });
    }

    /**
     * Tests that the routine for determination if initial repository selection
     * is performed (not skipped) if there is only one repository available
     * and method {@code setup()} was passed {@code false} as its third
     * argument. It also tests that the routine correctly determines no
     * repository should be preselected if there is no node selected.
     */
    @Test(timeout=10000)
    public void testSingleRepoNoNodeFalse() throws InterruptedException {
        printTestName("testSingleRepoNoNodeFalse");
        runRepositoryComboTest(new SingleRepoComboTezt() {
            @Override
            RepositoryComboSupport setupComboSupport(JComboBox comboBox) {
                return RepositoryComboSupport.setup(null, comboBox, false);
            }
            @Override
            protected void scheduleTests(ProgressTester progressTester) {
                super.scheduleTests(progressTester);
                progressTester.scheduleResumingTest  (Progress.DISPLAYED_REPOS, AWT,
                                                        new ComboBoxItemsTezt(
                                                                SELECT_REPOSITORY,
                                                                repository1),
                                                        new SelectedItemtezt(
                                                                SELECT_REPOSITORY));
                progressTester.scheduleTest          (Progress.WILL_DETERMINE_DEFAULT_REPO, NON_AWT);
                progressTester.scheduleTest          (Progress.DETERMINED_DEFAULT_REPO, NON_AWT);
            }
        });
    }

    /**
     * Tests that the routine for determination if initial repository selection
     * is performed (not skipped) if there is only one repository available
     * and method {@code setup()} was passed {@code false} as its third
     * argument. It also tests that the routine correctly determines no
     * repository should be preselected if there is no node selected that
     * would refer to the repository.
     */
    @Test(timeout=10000)
    public void testSingleRepoNoMatchingNodeFalse() throws InterruptedException {
        printTestName("testSingleRepoNoMatchingNodeFalse");
        runRepositoryComboTest(new SingleRepoComboTezt() {
            @Override
            protected void setUpEnvironment() {
                selectNodes(node1);
            }
            @Override
            RepositoryComboSupport setupComboSupport(JComboBox comboBox) {
                return RepositoryComboSupport.setup(null, comboBox, false);
            }
            @Override
            protected void scheduleTests(ProgressTester progressTester) {
                super.scheduleTests(progressTester);
                progressTester.scheduleResumingTest  (Progress.DISPLAYED_REPOS, AWT,
                                                        new ComboBoxItemsTezt(
                                                                SELECT_REPOSITORY,
                                                                repository1),
                                                        new SelectedItemtezt(
                                                                SELECT_REPOSITORY));
                progressTester.scheduleTest          (Progress.WILL_DETERMINE_DEFAULT_REPO, NON_AWT);
                progressTester.scheduleTest          (Progress.DETERMINED_DEFAULT_REPO, NON_AWT);
            }
        });
    }

    /**
     * Tests that the routine for determination if initial repository selection
     * is performed (not skipped) if there is only one repository available
     * and method {@code setup()} was passed {@code false} as its third
     * argument. It also tests that the routine correctly determines that
     * the repository should be preselected.
     */
    @Test(timeout=10000)
    public void testSingleRepoMatchingNodeFalse() throws InterruptedException {
        printTestName("testSingleRepoMatchingNodeFalse");
        runRepositoryComboTest(new SingleRepoComboTezt() {
            @Override
            protected void setUpEnvironment() {
                selectNodes(repoNode1);
            }
            @Override
            RepositoryComboSupport setupComboSupport(JComboBox comboBox) {
                return RepositoryComboSupport.setup(null, comboBox, false);
            }
            @Override
            protected void scheduleTests(ProgressTester progressTester) {
                super.scheduleTests(progressTester);
                progressTester.scheduleResumingTest  (Progress.DISPLAYED_REPOS, AWT,
                                                        new ComboBoxItemsTezt(
                                                                SELECT_REPOSITORY,
                                                                repository1),
                                                        new SelectedItemtezt(
                                                                SELECT_REPOSITORY));
                progressTester.scheduleTest          (Progress.WILL_DETERMINE_DEFAULT_REPO, NON_AWT);
                progressTester.scheduleTest          (Progress.DETERMINED_DEFAULT_REPO, NON_AWT);
                progressTester.scheduleTest          (Progress.WILL_SCHEDULE_SELECTION_OF_DEFAULT_REPO, NON_AWT);
                progressTester.scheduleSuspendingTest(Progress.SCHEDULED_SELECTION_OF_DEFAULT_REPO, NON_AWT);
                progressTester.scheduleTest          (Progress.WILL_SELECT_DEFAULT_REPO, AWT);
                progressTester.scheduleResumingTest  (Progress.SELECTED_DEFAULT_REPO, AWT,
                                                        new ComboBoxItemsTezt(
                                                                repository1),
                                                        new SelectedItemtezt(
                                                                repository1));
            }
        });
    }

    /**
     * This test and test {@code testTwoReposNoMatchingNodeTrue} verify that
     * value of parameter {@code selectRepoIfSingle} of method
     * {@code setup()} has no impact if there are two repositories.
     */
    @Test(timeout=10000)
    public void testMoreReposNoMatchingNodeFalse() throws InterruptedException {
        printTestName("testMoreReposNoMatchingNodeFalse");
        runRepositoryComboTest(new AbstractRepositoryComboTezt() {
            @Override
            protected void setUpEnvironment() {
                selectNodes(node1);
            }
            @Override
            RepositoryComboSupport setupComboSupport(JComboBox comboBox) {
                return RepositoryComboSupport.setup(null, comboBox, false);
            }
            @Override
            protected void scheduleTests(ProgressTester progressTester) {
                super.scheduleTests(progressTester);
                progressTester.scheduleResumingTest  (Progress.DISPLAYED_REPOS, AWT,
                                                        new ComboBoxItemsTezt(
                                                    SELECT_REPOSITORY,
                                                    repository1,
                                                    repository2,
                                                                repository3),
                                                        new SelectedItemtezt(
                                                                SELECT_REPOSITORY));
                progressTester.scheduleTest          (Progress.WILL_DETERMINE_DEFAULT_REPO, NON_AWT);
                progressTester.scheduleTest          (Progress.DETERMINED_DEFAULT_REPO, NON_AWT);
            }
        });
    }

    /**
     * This test and test {@code testTwoReposNoMatchingNodeFalse} verify that
     * value of parameter {@code selectRepoIfSingle} of method
     * {@code setup()} has no impact if there are two repositories.
     */
    @Test(timeout=10000)
    public void testMoreReposNoMatchingNodeTrue() throws InterruptedException {
        printTestName("testMoreReposNoMatchingNodeTrue");
        runRepositoryComboTest(new AbstractRepositoryComboTezt() {
            @Override
            protected void setUpEnvironment() {
                selectNodes(node1);
            }
            @Override
            RepositoryComboSupport setupComboSupport(JComboBox comboBox) {
                return RepositoryComboSupport.setup(null, comboBox, true);
            }
            @Override
            protected void scheduleTests(ProgressTester progressTester) {
                super.scheduleTests(progressTester);
                progressTester.scheduleResumingTest  (Progress.DISPLAYED_REPOS, AWT,
                                                        new ComboBoxItemsTezt(
                                                                SELECT_REPOSITORY,
                                                                repository1,
                                                                repository2,
                                                                repository3),
                                                        new SelectedItemtezt(
                                                                SELECT_REPOSITORY));
                progressTester.scheduleTest          (Progress.WILL_DETERMINE_DEFAULT_REPO, NON_AWT);
                progressTester.scheduleTest          (Progress.DETERMINED_DEFAULT_REPO, NON_AWT);
            }
        });
    }
    
    @Test(timeout=100000)
    public void testMoreReposOneNotAttachingFilesFilteredOut() throws InterruptedException {
        testMoreReposAttachingFiles("testMoreReposOneNotAttachingFilesFilteredOut", true);
    }
    
    @Test(timeout=100000)
    public void testMoreReposOneNotAttachingFilesNotFilteredOut() throws InterruptedException {
        testMoreReposAttachingFiles("testMoreReposOneNotAttachingFilesNotFilteredOut", false);
    }
    
    private void testMoreReposAttachingFiles(String testName, final boolean onlyCanAttach) throws InterruptedException {
        printTestName(testName);
        runRepositoryComboTest(new AbstractRepositoryComboTezt() {
            @Override
            protected void setUpEnvironment() {
                selectNodes(node1);
            }
            @Override
            RepositoryComboSupport setupComboSupport(JComboBox comboBox) {
                return RepositoryComboSupport.setup(null, comboBox, onlyCanAttach ? RepositoryFilter.ATTACH_FILE : RepositoryFilter.ALL , true);
            }
            @Override
            protected void scheduleTests(ProgressTester progressTester) {
                super.scheduleTests(progressTester);
                progressTester.scheduleResumingTest  (Progress.DISPLAYED_REPOS, AWT,
                                                        onlyCanAttach ?  
                                                            new ComboBoxItemsTezt(
                                                                    SELECT_REPOSITORY,
                                                                    repository1,
                                                                    repository3) : 
                                                            new ComboBoxItemsTezt(
                                                                    SELECT_REPOSITORY,
                                                                    repository1,
                                                                    repository2,
                                                                    repository3),
                                                        new SelectedItemtezt(
                                                                SELECT_REPOSITORY));
                progressTester.scheduleTest          (Progress.WILL_DETERMINE_DEFAULT_REPO, NON_AWT);
                progressTester.scheduleTest          (Progress.DETERMINED_DEFAULT_REPO, NON_AWT);
            }
        });
    }

    /**
     * Tests that setup of the combo-box is done by one job scheduled to the EDT
     * (event-dispatch thread) if all information (list of repositories,
     * plus the repository to be preselected, if any) is available at the moment
     * the EDT is about to display the list of available repositories.
     */
    @Test(timeout=10000)
    public void testMoreReposNoMatchingNodeAwtRetarded() throws InterruptedException {
        printTestName("testMoreReposNoMatchingNodeAwtRetarded");
        runRepositoryComboTest(new AbstractRepositoryComboTezt() {
            @Override
            RepositoryComboSupport setupComboSupport(JComboBox comboBox) {
                return RepositoryComboSupport.setup(null, comboBox, false);
            }
            @Override
            protected void scheduleTests(ProgressTester progressTester) {
                progressTester.scheduleTest          (Progress.STARTED, AWT,
                                                        new ComboBoxItemsTezt(
                                                                LOADING_REPOSITORIES));
                progressTester.scheduleTest          (Progress.WILL_LOAD_REPOS, NON_AWT);
                progressTester.scheduleTest          (Progress.LOADED_REPOS, NON_AWT);
                progressTester.scheduleTest          (Progress.WILL_SCHEDULE_DISPLAY_OF_REPOS, NON_AWT);
                progressTester.scheduleSuspendingTest(Progress.WILL_DISPLAY_REPOS, AWT);
                progressTester.scheduleTest          (Progress.SCHEDULED_DISPLAY_OF_REPOS, NON_AWT);
                progressTester.scheduleTest          (Progress.WILL_DETERMINE_DEFAULT_REPO, NON_AWT);
                progressTester.scheduleResumingTest  (Progress.DETERMINED_DEFAULT_REPO, NON_AWT);
                progressTester.scheduleTest          (Progress.DISPLAYED_REPOS, AWT,
                                                        new ComboBoxItemsTezt(
                                                                SELECT_REPOSITORY,
                                                                repository1,
                                                                repository2,
                                                                repository3),
                                                        new SelectedItemtezt(
                                                                SELECT_REPOSITORY));
            }
        });
    }

    private void printTestName(String testName) {
        System.out.println();
        System.out.println("--- " + testName + " ---");
    }

    private void runRepositoryComboTest(AbstractRepositoryComboTezt test)
                                                throws InterruptedException {
        test.createRepositories();
        test.setUpEnvironment();
        comboSupport = test.setupComboSupport(comboBox);
        assertNotNull(comboSupport);
        assertSame(Progress.INITIALIZED, comboSupport.getProgress());

        final Object testLock = new Object();
        ProgressTester progressListener = new ProgressTester(testLock);
        test.scheduleTests(progressListener);
        progressListener.startListening();
        synchronized (testLock) {
            /*
             * testLock.notify() in ProgressTester must not be called
             * before testLock.wait() here - so we must schedule start
             * of the RepositoryComboSupport while we are holding the testLock,
             * i.e. in the synchronized block.
             */
            EventQueue.invokeLater(new Runnable() {
                public void run() {
                    comboSupport.start();
                }
            });
            testLock.wait();
        }

        if (progressListener.failure != null) {
            if (progressListener.failure instanceof Error) {
                throw (Error) progressListener.failure;
            }
            if (progressListener.failure instanceof RuntimeException) {
                throw (RuntimeException) progressListener.failure;
            }
            assert false;
            fail(progressListener.failure.getClass().getName() + ": "
                 + progressListener.failure.getMessage());
        }
    }

    private static void selectNodes(Node... nodes) {
        if ((nodes == null) || (nodes.length == 0)) {
            throw new IllegalArgumentException("specify a non-empty list of nodes");
        }
        getTopComponentRegistry().setNodes(nodes);
    }

    public static final class TestLookup extends AbstractLookup {
        public TestLookup() {
            this(new InstanceContent());
        }
        private TestLookup(InstanceContent ic) {
            super(ic);
            ic.add(new DummyRepositories());
            ic.add(new DummyBugtrackingConnector());
            ic.add(new DummyWindowManager());
            ic.add(new DummyBugtrackingOwnerSupport());
        }
    }

    static enum ThreadType {
        AWT(true, "an AWT"),
        NON_AWT(false, "a non-AWT");

        private final boolean boolValue;
        private final String displayName;
        ThreadType(boolean boolValue, String displayName) {
            this.boolValue = boolValue;
            this.displayName = displayName;
        }
        boolean booleanValue() {
            return boolValue;
        }
        static ThreadType forBoolean(boolean booleanValue) {
            return (booleanValue == AWT.boolValue) ? AWT : NON_AWT;
        }
        String getDisplayName() {
            return displayName;
        }
    }

    final class ComboBoxItemsTezt implements Runnable {
        private final Object[] expectedItems;

        ComboBoxItemsTezt(Object... expectedItems) {
            this.expectedItems = expectedItems;
        }

        public void run() {
            System.out.println("Comparing combo-box items.");
            Object[] actualItems = getActualItems();
            assertArrayEquals("Different content of the combo-box expected",
                              expectedItems, actualItems);
        }

        private Object[] getActualItems() {
            Object[] result = new Object[comboBox.getItemCount()];
            for (int i = 0; i < result.length; i++) {
                result[i] = comboBox.getItemAt(i);
            }
            return result;
        }

    }

    final class SelectedItemtezt implements Runnable {

        private final Object expectedSelectedItem;

        SelectedItemtezt(Object expectedSelectedItem) {
            this.expectedSelectedItem = expectedSelectedItem;
        }

        public void run() {
            System.out.println("Checking selected combo-box item.");
            assertSame("Different item should be selected in the combo-box",
                       expectedSelectedItem, comboBox.getSelectedItem());
        }

    }

    static final class ProgressTesst {
        static final Boolean SUSPENDING = Boolean.TRUE;
        static final Boolean RESUMING = Boolean.FALSE;
        private final Progress progressState;
        private final ThreadType threadType;
        private final Runnable[] additionalTests;
        private final Boolean synchrType;
        private final StackTraceElement[] callStack;
        ProgressTesst(Progress progressState,
                     ThreadType threadType) {
            this(progressState, threadType, (Runnable[]) null);
        }
        ProgressTesst(Progress progressState,
                     ThreadType threadType,
                     Runnable... additionalTests) {
            this(null, progressState, threadType, additionalTests);
        }
        ProgressTesst(Boolean synchronizationType,
                     Progress progressState,
                     ThreadType threadType,
                     Runnable... additionalTests) {
            this.progressState = progressState;
            this.threadType = threadType;
            this.additionalTests
                    = (additionalTests != null) && (additionalTests.length != 0)
                      ? additionalTests
                      : null;
            this.synchrType = synchronizationType;
            this.callStack = Thread.currentThread().getStackTrace();
        }
        boolean isSuspending() {
            return synchrType == SUSPENDING;
        }
        boolean isResuming() {
            return synchrType == RESUMING;
        }
        @Override
        public String toString() {
            return "ProgressTest(" + getParamString() + ')';
        }

        private String getParamString() {
            StringBuilder buf = new StringBuilder(100);
            buf.append(progressState).append(", ").append(threadType);
            if (additionalTests != null) {
                for (Runnable additionalTest : additionalTests) {
                    buf.append(", ");
                    buf.append(additionalTest.getClass().getSimpleName());
                }
            }
            if (isSuspending()) {
                buf.append(", suspending");
            } else if (isResuming()) {
                buf.append(", resuming");
            }
            return buf.toString();
        }
    }

    private final class ProgressTester implements ChangeListener {

        private final Object testLock;
        private final Object suspendedThreadLock = new Object();
        private final Queue<ProgressTesst> progressTestQueue
                                  = new ConcurrentLinkedQueue<ProgressTesst>();
        private volatile boolean listening;
        private volatile Throwable failure;
        private Boolean[] threadFinished
                                   = new Boolean[ThreadType.values().length];
        private int numRunningThreads;
        private ProgressTesst pendingSuspendingProgressTest;
        private boolean pendingSuspendingProgressTestResumeExecuted;

        /**
         * {@code ThreadType} of the last unpaired suspending test.
         * &quot;unpaired suspending test&quot; means a suspending test
         * for which a corresponding resuming test has not been scheduled yet.
         */
        private ThreadType unpairedSuspendingTestType;

        ProgressTester(Object testLock) {
            this.testLock = testLock;
        }

        private void scheduleTest(Progress progressState,
                                  ThreadType threadType) {
            scheduleTest(progressState, threadType, (Runnable[]) null);
        }

        private void scheduleTest(Progress progressState,
                                  ThreadType threadType,
                                  Runnable... additionalTests) {
            checkNonSuspendingTestType(threadType);
            scheduleTest(new ProgressTesst(progressState, threadType,
                                          additionalTests));
        }

        /**
         * Schedules a progress test which, after executed, suspends the current
         * thread (unless the test failed). This allows that other tests
         * being executed in other threads finish before next tests
         * for the current are started.
         *
         * @param  progressState
         * @param  threadType
         * @see  #scheduleResumingTest
         */
        private void scheduleSuspendingTest(Progress progressState,
                                            ThreadType threadType) {
            scheduleSuspendingTest(progressState, threadType, (Runnable[]) null);
        }

        /**
         * Schedules a progress test which, after executed, suspends the current
         * thread (unless the test failed). This allows that other tests
         * being executed in other threads finish before next tests
         * for the current are started. If the third parameter is specified
         * (non-null), the given routine is executed after the given test
         * passes (and before the current thread is suspended, of course).
         *
         * @param  progressState
         * @param  threadType
         * @param  additionalTest
         * @see  #scheduleResumingTest
         */
        private void scheduleSuspendingTest(Progress progressState,
                                            ThreadType threadType,
                                            Runnable... additionalTests) {
            if (unpairedSuspendingTestType != null) {
                throw new IllegalStateException(
                        "Cannot schedule a suspending test until a resuming" +
                        " test is scheduled for the previously scheduled" +
                        " suspending test");
            }
            scheduleTest(new ProgressTesst(ProgressTesst.SUSPENDING,
                                          progressState, threadType,
                                          additionalTests));
            unpairedSuspendingTestType = threadType;
        }

        /**
         *
         * @param progressState
         * @param threadType
         */
        private void scheduleResumingTest(Progress progressState,
                                          ThreadType threadType) {
            scheduleResumingTest(progressState, threadType, (Runnable[]) null);
        }

        /**
         *
         * @param progressState
         * @param threadType
         * @param additionalTest
         */
        private void scheduleResumingTest(Progress progressState,
                                          ThreadType threadType,
                                          Runnable... additionalTests) {
            if (unpairedSuspendingTestType == null) {
                throw new IllegalStateException(
                        "Cannot schedule a resuming test because no" +
                        " corresponding suspending test has been scheduled.");
            }
            checkNonSuspendingTestType(threadType);
            scheduleTest(new ProgressTesst(ProgressTesst.RESUMING,
                                          progressState, threadType,
                                          additionalTests));
            unpairedSuspendingTestType = null;
        }

        private void scheduleTest(ProgressTesst progressTest) {
            if (progressTest.progressState == Progress.THREAD_PROGRESS_DONE) {
                throw new IllegalArgumentException(
                        "Test for progress " + Progress.THREAD_PROGRESS_DONE
                        + " cannot be scheduled.");
            }

            progressTestQueue.add(progressTest);
        }

        private void checkNonSuspendingTestType(ThreadType threadType) {
            if (unpairedSuspendingTestType == null) {
                return;
            }

            if (threadType == unpairedSuspendingTestType) {
                throw new IllegalStateException(
                        "Cannot schedule a ProgressTest of the same thread" +
                        " type as that of the unpaired suspending thread.");
            }
        }

        public void stateChanged(ChangeEvent e) {
            ProgressTesst progressTest;
            boolean isPendingSuspendingTest = false;
            boolean shouldSuspend = false;
            boolean shouldResume = false;
            final boolean isLastTest;

            synchronized (suspendedThreadLock) {
                if ((pendingSuspendingProgressTest != null)
                        && (getCurrThreadType() == pendingSuspendingProgressTest.threadType)) {
                    isPendingSuspendingTest = true;
                    progressTest = pendingSuspendingProgressTest;
                    System.out.println("Hit the stored suspending test (" + pendingSuspendingProgressTest + " by "
                                       + getCurrThreadType().getDisplayName() + " thread.");
                    pendingSuspendingProgressTest = null;
                } else {
                    boolean skip = false;
                    if (comboSupport.getProgress() == Progress.THREAD_PROGRESS_DONE) {
                        progressTest = progressTestQueue.peek();
                        skip = (progressTest == null)
                               || (progressTest.threadType != getCurrThreadType());
                    }
                    progressTest = skip ? null : progressTestQueue.poll();
                    System.out.println("Polled a test (" + progressTest + ") by "
                                       + getCurrThreadType().getDisplayName() + " thread.");
                    if ((progressTest != null) && progressTest.isSuspending()) {
                        assert pendingSuspendingProgressTest == null;
                        if (getCurrThreadType() != progressTest.threadType) {
                            pendingSuspendingProgressTest = progressTest;
                            pendingSuspendingProgressTestResumeExecuted = false;
                            System.out.println(" - it's not a test for the current thread - test stored");
                            progressTest = progressTestQueue.poll();  //poll the next test
                            System.out.println("Polled a test (" + progressTest + ") by "
                                               + getCurrThreadType().getDisplayName() + " thread.");
                        }
                    }
                }

                if (progressTest == null) {
                    //do nothing
                } else if (progressTest.isSuspending()) {
                    if (isPendingSuspendingTest && pendingSuspendingProgressTestResumeExecuted) {
                        System.out.println(
                                " - it's a suspending thread but the corresponding"
                                + " resuming test has been already executed"
                                + " so we will not suspend");
                    } else if (progressTestQueue.isEmpty()) {
                        System.out.println(" - it's a suspending thread but"
                                           + " it is the last remaining test"
                                           + " so we will not suspend");
                    } else {
                        shouldSuspend = true;
                    }
                } else if (progressTest.isResuming()) {
                    shouldResume = true;
                    if (pendingSuspendingProgressTest != null) {
                        pendingSuspendingProgressTestResumeExecuted = true;
                    }
                }

                isLastTest = progressTestQueue.isEmpty() && (pendingSuspendingProgressTest == null);

                performTest(progressTest);

                if (isLastTest && (numRunningThreads == 0)
                        || (failure != null)) {
                    System.out.println("   - IS LAST TEST - WILL RESUME (if there is a test thread suspended)");
                    stopListening();
                    resumeSuspendedThread();
                    synchronized (testLock) {
                        testLock.notify();      //resumes the thread running the unit test
                    }
                } else if (shouldSuspend) {
                    System.out.println("   - WILL SUSPEND");
                    suspendCurrentThread(progressTest);
                } else if (shouldResume) {
                    System.out.println("   - WILL RESUME");
                    resumeSuspendedThread();
                }
            }
        }

        private void performTest(ProgressTesst progressTest) {
            try {
                final ThreadType currThreadType = getCurrThreadType();
                final int threadTypeIndex = currThreadType.ordinal();

                if (comboSupport.getProgress() == Progress.THREAD_PROGRESS_DONE) {
                    if (threadFinished[threadTypeIndex] == FALSE) {
                        numRunningThreads--;
                    }
                    threadFinished[threadTypeIndex] = TRUE;

                    /*
                     * if (progressTest == null)
                     *     OK
                     * else
                     *     the test below will fail
                     */
                } else {
                    if (threadFinished[threadTypeIndex] == TRUE) {
                        fail("Thread " + currThreadType + " has been already marked as finished.");
                    }
                    if (threadFinished[threadTypeIndex] == null) {
                        numRunningThreads++;
                    }
                    threadFinished[threadTypeIndex] = FALSE;

                    if (progressTest == null) {
                        fail("No more progress updates were expected for "
                             + currThreadType.getDisplayName() + " thread, "
                             + "but got " + comboSupport.getProgress() + '.');
                    }
                }

                if (progressTest != null) {
                    assertSame(progressTest.progressState, comboSupport.getProgress());
                    assertSame(progressTest.threadType,    getCurrThreadType());
                    if (progressTest.additionalTests != null) {
                        for (Runnable additionalTest : progressTest.additionalTests) {
                            additionalTest.run();
                        }
                    }
                }
            } catch (Throwable t) {
                handleException(t, (progressTest != null) ? progressTest.callStack
                                                          : null);
            }
        }

        private ThreadType getCurrThreadType() {
            return ThreadType.forBoolean(EventQueue.isDispatchThread());
        }

        private void suspendCurrentThread(ProgressTesst progressTest) {
            System.out.println("Suspending the current thread.");
            try {
                suspendedThreadLock.wait();
            } catch (InterruptedException ex) {
                handleException(ex, (progressTest != null) ? progressTest.callStack
                                                           : null);
            }
        }

        private void resumeSuspendedThread() {
            System.out.println("Resuming the suspended thread (if any).");
            suspendedThreadLock.notify();
        }

        private void handleException(Throwable t,
                                     StackTraceElement[] callersStackTrace) {
            if (failure == null) {
                if (callersStackTrace != null) {
                    failure = addCallersCallStack(t, callersStackTrace); //store information about the exception
                } else {
                    failure = t;
                }
            }
        }

        private Throwable addCallersCallStack(Throwable t,
                                              StackTraceElement[] callersCallStack) {
            t.setStackTrace(concatArrays(t.getStackTrace(), callersCallStack));
            return t;
        }

        private void startListening() {
            if (unpairedSuspendingTestType != null) {
                throw new IllegalStateException(
                        "There is a suspending test scheduled for which" +
                        " there is no corresponding resuming test scheduled.");
            }

            if (listening) {
                throw new IllegalStateException("already listening");
            }
            listening = true;

            comboSupport.setProgressListener(this);
        }

        private void stopListening() {
            comboSupport.setProgressListener(null);
        }

    }

    static <T> T[] concatArrays(T[] a, T[] b) {
        if (b.length == 0) {
            return a;
        }
        if (a.length == 0) {
            return b;
        }
        T[] result = (T[]) java.lang.reflect.Array.newInstance(
                                            a.getClass().getComponentType(),
                                            a.length + b.length);
        System.arraycopy(a, 0, result,        0, a.length);
        System.arraycopy(b, 0, result, a.length, b.length);
        return result;
    }

}
