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

package org.netbeans.modules.gradle.test;

import java.util.Arrays;
import org.netbeans.modules.gradle.api.NbGradleProject;
import java.util.Collection;
import org.netbeans.modules.gradle.spi.GradleProgressListenerProvider;
import java.util.EnumSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.gradle.tooling.Failure;
import org.gradle.tooling.events.OperationDescriptor;
import org.gradle.tooling.events.OperationType;
import org.gradle.tooling.events.ProgressEvent;
import org.gradle.tooling.events.ProgressListener;
import org.gradle.tooling.events.test.Destination;
import org.gradle.tooling.events.test.JvmTestOperationDescriptor;
import org.gradle.tooling.events.test.TestFailureResult;
import org.gradle.tooling.events.test.TestFinishEvent;
import org.gradle.tooling.events.test.TestOperationDescriptor;
import org.gradle.tooling.events.test.TestOperationResult;
import org.gradle.tooling.events.test.TestOutputDescriptor;
import org.gradle.tooling.events.test.TestOutputEvent;
import org.gradle.tooling.events.test.TestProgressEvent;
import org.gradle.tooling.events.test.TestSkippedResult;
import org.gradle.tooling.events.test.TestStartEvent;
import org.gradle.tooling.events.test.TestSuccessResult;
import org.netbeans.api.project.Project;
import org.netbeans.modules.gsf.testrunner.api.CommonUtils;
import org.netbeans.modules.gsf.testrunner.api.CoreManager;
import org.netbeans.modules.gsf.testrunner.api.Report;
import org.netbeans.modules.gsf.testrunner.api.Status;
import org.netbeans.modules.gsf.testrunner.api.TestSession;
import org.netbeans.modules.gsf.testrunner.api.TestSuite;
import org.netbeans.modules.gsf.testrunner.api.Testcase;
import org.netbeans.modules.gsf.testrunner.api.Trouble;
import org.netbeans.spi.project.ProjectServiceProvider;
import org.openide.util.Lookup;

/**
 *
 * @author Laszlo Kishalmi
 */
@ProjectServiceProvider(service = GradleProgressListenerProvider.class, projectType = NbGradleProject.GRADLE_PLUGIN_TYPE + "/java")
public final class GradleTestProgressListener implements ProgressListener, GradleProgressListenerProvider {

    final private Project project;
    TestSession session;

    Map<String, Testcase> runningTests = new ConcurrentHashMap<>();

    public GradleTestProgressListener(Project project) {
        this.project = project;
    }

    @Override
    public void statusChanged(ProgressEvent evt) {
        if (evt instanceof TestOutputEvent) {
            processTestOutput((TestOutputEvent) evt);
        }
        if (evt instanceof TestProgressEvent) {
            processTestProgress((TestProgressEvent) evt);
        }
    }

    private void processTestProgress(TestProgressEvent evt) {
        TestOperationDescriptor desc = (TestOperationDescriptor) evt.getDescriptor();
        if (evt instanceof TestStartEvent) {
            TestStartEvent start = (TestStartEvent) evt;
            if (desc.getParent() == null) {
                sessionStart(start);
            } else if (desc instanceof JvmTestOperationDescriptor) {
                JvmTestOperationDescriptor jvmTest = (JvmTestOperationDescriptor) desc;
                switch (jvmTest.getJvmTestKind()) {
                    case ATOMIC: {
                        caseStart(start, jvmTest);
                        break;
                    }
                    case SUITE: {
                        suiteStart(start, jvmTest);
                        break;
                    }
                    default: {
                        //TODO: Handle unknown kinds.
                    }
                }
            }
        }
        if (evt instanceof TestFinishEvent) {
            TestFinishEvent finish = (TestFinishEvent) evt;
            if (desc.getParent() == null) {
                sessionFinish(finish);
            } else if (desc instanceof JvmTestOperationDescriptor) {
                JvmTestOperationDescriptor jvmTest = (JvmTestOperationDescriptor) desc;
                switch (jvmTest.getJvmTestKind()) {
                    case ATOMIC: {
                        caseFinish(finish, jvmTest);
                        break;
                    }
                    case SUITE: {
                        suiteFinish(finish, jvmTest);
                        break;
                    }
                    default: {
                        //TODO: Handle unknown kinds.
                    }
                }
            }
        }
    }

    private void processTestOutput(TestOutputEvent evt) {
        TestOutputDescriptor desc = evt.getDescriptor();
        OperationDescriptor parent = desc.getParent();
        CoreManager manager = getManager();
        if (manager != null) {
            manager.displayOutput(session, desc.getMessage(), desc.getDestination().equals(Destination.StdErr));
        }
        if ((parent != null) && (parent instanceof JvmTestOperationDescriptor)) {
            Testcase tc = runningTests.get(getTestOpKey((JvmTestOperationDescriptor) parent));
            tc.addOutputLines(Arrays.asList(desc.getMessage().split("\\R")));
        }
    }


    private void sessionStart(TestStartEvent evt) {
        session = new TestSession(evt.getDisplayName(), project, TestSession.SessionType.TEST);
        runningTests.clear();
        CoreManager manager = getManager();
        if (manager != null) {
            manager.registerNodeFactory();
            manager.testStarted(session);
        }
    }

    private void sessionFinish(TestFinishEvent evt) {
        runningTests.clear();
        CoreManager manager = getManager();
        if (manager != null) {
            manager.sessionFinished(session);
        }
    }

    private void suiteStart(TestStartEvent evt, JvmTestOperationDescriptor op) {
    }

    private void suiteFinish(TestFinishEvent evt, JvmTestOperationDescriptor op) {
        TestOperationResult result = evt.getResult();
        TestSuite currentSuite = session.getCurrentSuite();
        String suiteName = GradleTestSuite.suiteName(op);
        if (suiteName.equals(currentSuite.getName())) {
            Report report = session.getReport(result.getEndTime() - result.getStartTime());
            CoreManager manager = getManager();
            if (manager != null) {
                manager.displayReport(session, report, true);
            }
        }
    }

    private void caseStart(TestStartEvent evt, JvmTestOperationDescriptor op) {
        assert session != null;
        assert op.getParent() != null;
        TestSuite currentSuite = session.getCurrentSuite();
        TestSuite newSuite = new GradleTestSuite((JvmTestOperationDescriptor) op.getParent());
        if ((currentSuite == null) || !currentSuite.equals(newSuite)) {
            session.addSuite(newSuite);
            CoreManager manager = getManager();
            if (manager != null) {
                manager.displaySuiteRunning(session, newSuite);
            }
        }
        Testcase tc = new GradleTestcase(op, session);
        runningTests.put(getTestOpKey(op), tc);
        session.addTestCase(tc);
    }

    private void caseFinish(TestFinishEvent evt, JvmTestOperationDescriptor op) {
        Testcase tc = runningTests.get(getTestOpKey(op));
        if (tc != null) {
            TestOperationResult result = evt.getResult();
            long time = result.getEndTime() - result.getStartTime();
            tc.setTimeMillis(time);
            tc.setLocation(searchLocation(op.getClassName(), op.getMethodName(), null));
            if (result instanceof TestSuccessResult) {
                tc.setStatus(Status.PASSED);
            }
            if (result instanceof TestSkippedResult) {
                tc.setStatus(Status.SKIPPED);
            }
            if (result instanceof TestFailureResult) {
                tc.setStatus(Status.ERROR);
                TestFailureResult fail = (TestFailureResult) result;
                Failure failure = fail.getFailures().isEmpty() ? null : fail.getFailures().iterator().next();
                if (failure != null) {
                    Trouble trouble = new Trouble(failure.getMessage() == null);
                    if (failure.getMessage() != null) {
                        tc.setStatus(Status.FAILED);
                        Matcher m = Pattern.compile("expected:(.+) but was:(.+)").matcher(failure.getMessage());
                        if (m.matches()) {
                            trouble.setComparisonFailure(
                                    new Trouble.ComparisonFailure(m.group(1), m.group(2))
                            );
                        }
                    }
                    String desc = failure.getDescription();
                    String[] stackTrace = null;
                    if (desc != null) {
                        stackTrace = desc.split("\\n");
                        trouble.setStackTrace(stackTrace);
                    }
                    tc.setLocation(searchLocation(op.getClassName(), op.getMethodName(), stackTrace));
                    tc.setTrouble(trouble);
                }

            }
            runningTests.remove(getTestOpKey(op));
        }

    }

    private static String getTestOpKey(JvmTestOperationDescriptor op) {
        return op.getClassName() + "." + op.getMethodName();
    }

    private static CoreManager getManager() {
        Collection<? extends Lookup.Item<CoreManager>> providers = Lookup.getDefault().lookupResult(CoreManager.class).allItems();
        for (Lookup.Item<CoreManager> provider : providers) {
            if (provider.getDisplayName().equals(NbGradleProject.GRADLE_PROJECT_TYPE.concat("_").concat(CommonUtils.JUNIT_TF))) {
                return provider.getInstance();
            }
        }
        return null;

    }

    private String searchLocation(String className, String methodName, String[] stackTrace) {
        StringBuilder ret = new StringBuilder(className.length() + methodName.length() + 10);
        String fileName = null;
        String line = null;
        if (stackTrace != null) {
            String fullMethodName = className + "." + methodName;
            String failedAt = null;
            for (String st : stackTrace) {
                int i = st.indexOf(fullMethodName);
                if (i > 0) {
                    failedAt = st.substring(i + fullMethodName.length() + 1, st.length() - 1);
                    break;
                }
            }
            if ((failedAt != null) && (failedAt.contains(":"))) {
                int sepa = failedAt.indexOf(':');
                fileName = failedAt.substring(0, sepa);
                line = failedAt.substring(sepa + 1);
                try {
                    Integer.parseInt(line);
                } catch (NumberFormatException ex) {
                    line = null;
                }
            }
        }
        int lastDot = className.lastIndexOf('.');
        String pkg = lastDot > 0 ? className.substring(0, lastDot) : "";
        if (fileName != null) {
            ret.append(pkg.replace('.', '/')).append('/').append(fileName);
        } else {
            ret.append(className.replace('.', '/')).append(".java");
        }
        ret.append(':');
        ret.append(line != null ? line : methodName);
        return ret.toString();
    }

    @Override
    public ProgressListener getProgressListener() {
        return this;
    }

    @Override
    public Set<OperationType> getSupportedOperationTypes() {
        return EnumSet.of(OperationType.TEST, OperationType.TEST_OUTPUT);
    }

}
