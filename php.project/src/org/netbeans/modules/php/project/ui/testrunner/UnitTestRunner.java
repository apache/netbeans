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

package org.netbeans.modules.php.project.ui.testrunner;

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.modules.gsf.testrunner.ui.api.Manager;
import org.netbeans.modules.gsf.testrunner.api.TestSession;
import org.netbeans.modules.php.api.phpmodule.PhpModule;
import org.netbeans.modules.php.project.PhpProject;
import org.netbeans.modules.php.project.ui.codecoverage.PhpCoverageProvider;
import org.netbeans.modules.php.project.ui.customizer.CompositePanelProviderImpl;
import org.netbeans.modules.php.project.util.PhpProjectUtils;
import org.netbeans.modules.php.project.util.UsageLogging;
import org.netbeans.modules.php.spi.testing.locate.Locations;
import org.netbeans.modules.php.spi.testing.PhpTestingProvider;
import org.netbeans.modules.php.spi.testing.coverage.Coverage;
import org.netbeans.modules.php.spi.testing.run.TestRunException;
import org.netbeans.modules.php.spi.testing.run.TestRunInfo;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.NbBundle;

/**
 * Test runner UI for PHP unit tests.
 * <p>
 * All the times are in milliseconds.
 */
public final class UnitTestRunner {
    private static final Logger LOGGER = Logger.getLogger(UnitTestRunner.class.getName());
    private static final Manager MANAGER = Manager.getInstance();

    private final PhpProject project;
    private final TestSession testSession;
    private final TestRunInfo info;
    private final ControllableRerunHandler rerunHandler;
    private final PhpCoverageProvider coverageProvider;
    private final List<PhpTestingProvider> testingProviders;


    public UnitTestRunner(PhpProject project, TestRunInfo info, ControllableRerunHandler rerunHandler) {
        this(project, info, rerunHandler, project.getTestingProviders());
    }

    public UnitTestRunner(PhpProject project, TestRunInfo info, ControllableRerunHandler rerunHandler, List<PhpTestingProvider> testingProviders) {
        assert project != null;
        assert rerunHandler != null;
        assert info != null;
        assert testingProviders != null;

        this.project = project;
        this.info = info;
        this.rerunHandler = rerunHandler;
        coverageProvider = project.getLookup().lookup(PhpCoverageProvider.class);
        assert coverageProvider != null;
        this.testingProviders = testingProviders;

        MANAGER.setNodeFactory(new PhpTestRunnerNodeFactory(new CallStackCallback(project)));
        testSession = new TestSession(getOutputTitle(project, info), project, map(info.getSessionType()));
        testSession.setRerunHandler(rerunHandler);
    }

    public void run() {
        if (!checkTestingProviders()) {
            return;
        }
        UsageLogging.logPhpTestRun(project, testingProviders);
        try {
            rerunHandler.disable();
            MANAGER.testStarted(testSession);
            TestSessions sessions = runInternal();
            handleCodeCoverage(sessions);
        } catch (Exception ex) {
            LOGGER.log(Level.SEVERE, null, ex);
        } finally {
            MANAGER.sessionFinished(testSession);
            rerunHandler.enable();
        }
    }

    @NbBundle.Messages("UnitTestRunner.error.running=Perhaps error occurred, verify in Output window.")
    private TestSessions runInternal() {
        TestSessions testSessions = new TestSessions();
        boolean error = false;
        for (PhpTestingProvider testingProvider : testingProviders) {
            TestSessionImpl testSessionImpl = new TestSessionImpl(MANAGER, testSession, testingProvider);
            testSessions.addTestSession(testSessionImpl);
            try {
                LOGGER.log(Level.FINE, "Running {0} tests...", testingProvider.getIdentifier());
                testingProvider.runTests(project.getPhpModule(), info, testSessionImpl);
                LOGGER.fine("Test run finished");
            } catch (TestRunException exc) {
                LOGGER.log(Level.INFO, null, exc);
                error = true;
                testSessionImpl.setTestException(true);
            } finally {
                testSessionImpl.freeze();
            }
        }
        if (error) {
            MANAGER.displayOutput(testSession, Bundle.UnitTestRunner_error_running(), true);
        }
        return testSessions;
    }

    private boolean checkTestingProviders() {
        if (!testingProviders.isEmpty()) {
            return true;
        }
        PhpProjectUtils.openCustomizer(project, CompositePanelProviderImpl.TESTING);
        return false;
    }

    @NbBundle.Messages({
        "# {0} - testing probider",
        "UnitTestRunner.error.coverage=Testing provider {0} does not support code coverage.",
    })
    private void handleCodeCoverage(TestSessions sessions) {
        if (!coverageProvider.isEnabled()) {
            // no code coverage at all
            return;
        }
        // #258296
        if (!info.isCoverageEnabled()) {
            // rerun test w/o coverage with meanwhile coverage enabled
            return;
        }
        // first coverage with data wins
        PhpModule phpModule = project.getPhpModule();
        for (TestSessionImpl session : sessions.getTestSessions()) {
            PhpTestingProvider testingProvider = session.getTestingProvider();
            if (!testingProvider.isCoverageSupported(phpModule)) {
                // no coverage supported
                MANAGER.displayOutput(testSession, Bundle.UnitTestRunner_error_coverage(testingProvider.getDisplayName()), true);
                continue;
            }
            if (session.isTestException()) {
                LOGGER.log(Level.FINE, "No coverage available due to test exception for {0}", testingProvider.getIdentifier());
                continue;
            }
            if (!session.isCoverageSet()) {
                throw new IllegalStateException("Code coverage was not set for " + testingProvider.getIdentifier()
                        + " (forgot to call TestSession.setCoverage(Coverage)?)");
            }
            Coverage coverage = session.getCoverage();
            if (coverage == null) {
                // some error, try next provider
                LOGGER.log(Level.INFO, "Code coverage set to null for provider {0}", testingProvider.getIdentifier());
                continue;
            }
            if (coverage.getFiles().isEmpty()) {
                // no code coverage data
                LOGGER.log(Level.INFO, "Ignoring code coverage for provider {0}, it contains no data", testingProvider.getIdentifier());
                continue;
            }
            if (info.allTests()) {
                coverageProvider.setCoverage(coverage);
            } else {
                coverageProvider.updateCoverage(coverage);
            }
        }
    }

    private String getOutputTitle(PhpProject project, TestRunInfo info) {
        StringBuilder sb = new StringBuilder(30);
        sb.append(project.getName());
        // #248701
        String testRootName = getTestRootName(project, info);
        if (testRootName != null) {
            sb.append("["); // NOI18N
            sb.append(testRootName);
            sb.append("]"); // NOI18N
        }
        String suiteName = info.getSuiteName();
        if (suiteName != null) {
            sb.append(":"); // NOI18N
            sb.append(suiteName);
        }
        return sb.toString();
    }

    @CheckForNull
    private String getTestRootName(PhpProject project, TestRunInfo info) {
        FileObject[] testRoots = project.getTestRoots().getRoots();
        if (testRoots.length == 1) {
            return null;
        }
        int idx = -1;
        FIND_ONE_ROOT:
        for (FileObject file : info.getStartFiles()) {
            for (int i = 0; i < testRoots.length; ++i) {
                FileObject testRoot = testRoots[i];
                if (FileUtil.isParentOf(testRoot, file)) {
                    if (idx == -1) {
                        idx = i;
                        break;
                    } else if (idx != i) {
                        idx = -1;
                        break FIND_ONE_ROOT;
                    }
                }
            }
        }
        if (idx == -1) {
            return null;
        }
        return project.getTestRoots().getPureRootNames()[idx];
    }

    //~ Mappers

    private TestSession.SessionType map(TestRunInfo.SessionType type) {
        return TestSession.SessionType.valueOf(type.name());
    }

    //~ Inner classes

    private static final class CallStackCallback implements JumpToCallStackAction.Callback {

        private final PhpProject project;

        public CallStackCallback(PhpProject project) {
            assert project != null;
            this.project = project;
        }

        @Override
        public Locations.Line parseLocation(String callStack) {
            List<PhpTestingProvider> providers = project.getTestingProviders();
            if (providers.isEmpty()) {
                PhpProjectUtils.openCustomizer(project, CompositePanelProviderImpl.TESTING);
                return null;
            }
            for (PhpTestingProvider testingProvider : providers) {
                Locations.Line location = testingProvider.parseFileFromOutput(callStack);
                if (location != null) {
                    return location;
                }
            }
            return null;
        }

    }

}
