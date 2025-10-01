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

import java.nio.file.Path;
import java.util.Arrays;
import java.util.Collection;
import java.util.EnumSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import javax.lang.model.element.ElementKind;
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
import org.netbeans.api.java.source.ClasspathInfo;
import org.netbeans.api.java.source.ElementHandle;
import org.netbeans.api.java.source.SourceUtils;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.modules.gradle.api.NbGradleProject;
import org.netbeans.modules.gradle.java.api.GradleJavaProject;
import org.netbeans.modules.gradle.java.api.GradleJavaSourceSet.SourceType;
import org.netbeans.modules.gradle.spi.GradleProgressListenerProvider;
import org.netbeans.modules.gsf.testrunner.api.CommonUtils;
import org.netbeans.modules.gsf.testrunner.api.CoreManager;
import org.netbeans.modules.gsf.testrunner.api.Report;
import org.netbeans.modules.gsf.testrunner.api.Status;
import org.netbeans.modules.gsf.testrunner.api.TestSession;
import org.netbeans.modules.gsf.testrunner.api.TestSuite;
import org.netbeans.modules.gsf.testrunner.api.Testcase;
import org.netbeans.modules.gsf.testrunner.api.Trouble;
import org.netbeans.spi.project.ProjectServiceProvider;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Lookup;

/**
 *
 * @author Laszlo Kishalmi
 */
@ProjectServiceProvider(service = GradleProgressListenerProvider.class, projectType = NbGradleProject.GRADLE_PROJECT_TYPE )
public final class GradleTestProgressListener implements ProgressListener, GradleProgressListenerProvider {

    private final Project project;
    private final Map<String, TestSession> sessions = new ConcurrentHashMap<>();
    private final Map<TestSession, Map<String, TestSuite>> runningSuites = new ConcurrentHashMap<>();
    private final Map<TestSession, Map<String, Testcase>> runningTests = new ConcurrentHashMap<>();

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
        TestOperationDescriptor desc = evt.getDescriptor();
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
        TestSession session = sessions.get(getSessionKey(evt.getDescriptor()));
        assert session != null;
        if (session == null) {
            throw new IllegalArgumentException("TestSession is null");
        }
        TestOutputDescriptor desc = evt.getDescriptor();
        OperationDescriptor parent = desc.getParent();
        CoreManager manager = getManager();
        String msg = desc.getMessage();
        if (msg != null && msg.endsWith("\n")) {
            msg = msg.substring(0, msg.length() - 1);
            if (manager != null && session != null) {
                manager.displayOutput(session, msg, desc.getDestination().equals(Destination.StdErr));
            }
            if (parent instanceof JvmTestOperationDescriptor) {
                Testcase tc = runningTests.get(session).get(getTestOpKey((JvmTestOperationDescriptor) parent));
                if (tc != null) {
                    tc.addOutputLines(Arrays.asList(msg.split("\\R")));
                }
            }
        }
    }


    private void sessionStart(TestStartEvent evt) {
        String key = getSessionKey(evt.getDescriptor());
        TestSession session;
        synchronized (this) {
            session = sessions.computeIfAbsent(key, name -> new TestSession(name, getProject(key), TestSession.SessionType.TEST));
            runningTests.put(session, new ConcurrentHashMap<>());
        }
        CoreManager manager = getManager();
        if (manager != null) {
            manager.registerNodeFactory();
            manager.testStarted(session);
        }
    }

    private void sessionFinish(TestFinishEvent evt) {
        TestSession session;
        synchronized (this) {
            session = sessions.remove(getSessionKey(evt.getDescriptor()));
            assert session != null;
            runningTests.remove(session);
        }
        CoreManager manager = getManager();
        if (manager != null) {
            manager.sessionFinished(session);
        }
    }

    private void suiteStart(TestStartEvent evt, JvmTestOperationDescriptor op) {
    }

    private void suiteFinish(TestFinishEvent evt, JvmTestOperationDescriptor op) {
        TestSession session = sessions.get(getSessionKey(evt.getDescriptor()));
        assert session != null;
        TestOperationResult result = evt.getResult();
        String suiteName = GradleTestSuite.suiteName(op);
        // In the NetBeans wording a testsuite is the class grouping multiple
        // methods (testcase). In the gradle wording a suite can be nested, for
        // example the hieararchy can be:
        // - Gradle Test Executor <Number> started
        // - Test class <Class> started
        // - @ParameterizedTest method name
        // => We flatten the list (suites are registered base on executed
        //    cases (see caseStart)
        TestSuite testSuite = runningSuites.get(session).remove(suiteName);
        if (testSuite != null) {
            Report report = session.getReport(result.getEndTime() - result.getStartTime());
            session.finishSuite(testSuite);
            CoreManager manager = getManager();
            if (manager != null) {
                manager.displaySuiteRunning(session, testSuite);
                manager.displayReport(session, report, true);
            }
        }
    }

    private void caseStart(TestStartEvent evt, JvmTestOperationDescriptor op) {
        TestSession session = sessions.get(getSessionKey(evt.getDescriptor()));
        assert session != null;
        assert op.getParent() != null;
        String suiteName = GradleTestSuite.suiteName(op.getParent());
        Map<String, TestSuite> sessionSuites = runningSuites.computeIfAbsent(session, s -> new ConcurrentHashMap<>());
        TestSuite ts = sessionSuites.computeIfAbsent(suiteName, s -> {
                    TestSuite suite = new GradleTestSuite(getSuiteOpDesc((JvmTestOperationDescriptor) op.getParent(), op.getClassName()));
                    session.addSuite(suite);
                    return suite;
                });
        CoreManager manager = getManager();
        if (manager != null && sessionSuites.size() == 1) {
            manager.displaySuiteRunning(session, ts);
        }
        Testcase tc = new GradleTestcase(op, session);
        synchronized (this) {
            runningTests.get(session).put(getTestOpKey(op), tc);
            session.addTestCase(tc);
        }
    }

    private void caseFinish(TestFinishEvent evt, JvmTestOperationDescriptor op) {   
        Testcase tc;
        synchronized (this) {
            TestSession session = sessions.get(getSessionKey(evt.getDescriptor()));
            assert session != null;
            tc = runningTests.get(session).remove(getTestOpKey(op));    
        }
        if (tc != null) {
            TestOperationResult result = evt.getResult();
            long time = result.getEndTime() - result.getStartTime();
            tc.setTimeMillis(time);
            tc.setLocation(searchLocation(tc, op.getClassName(), op.getMethodName(), null));
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
                    tc.setLocation(searchLocation(tc, op.getClassName(), op.getMethodName(), stackTrace));
                    tc.setTrouble(trouble);
                }

            }
        }

    }

    private static final String GRADLE_TEST_RUN = "Gradle Test Run :"; // NOI18N
    private static String TEST = ":test";

    private Project getProject(String key) {
        if (key != null && key.startsWith(GRADLE_TEST_RUN)) {
            key = key.substring(GRADLE_TEST_RUN.length());
            if (key.endsWith(TEST)) {
                key = key.substring(0, key.length() - TEST.length()).trim();
                if (!key.isEmpty()) {
                    for (Project containedPrj : ProjectUtils.getContainedProjects(project, true)) {
                        if (key.equals(containedPrj.getProjectDirectory().getName())) {
                            return containedPrj;
                        }
                    }
                }
            }
        }
        return project;
    }

    private static String getSessionKey(OperationDescriptor op) {
        String id = "";
        for (OperationDescriptor descriptor = op; descriptor != null; descriptor = descriptor.getParent()) {
            id = descriptor.getName();
        }
        return id;
    }

    private static JvmTestOperationDescriptor getSuiteOpDesc(JvmTestOperationDescriptor op, String className) {
        for (JvmTestOperationDescriptor descriptor = op; descriptor != null; descriptor = (JvmTestOperationDescriptor) descriptor.getParent()) {
            if (className == null || className.equals(descriptor.getClassName())) {
                return descriptor;
            }
        }
        return op;
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

    private String searchLocation(Testcase tc, String className, String methodName, String[] stackTrace) {
        Map<ClasspathInfo, Path> classpathInfo = Map.of();
        NbGradleProject nbGradleProject = tc.getSession()
                .getProject()
                .getLookup()
                .lookup(NbGradleProject.class);
        GradleJavaProject gradleJavaProject = nbGradleProject != null ? nbGradleProject.projectLookup(GradleJavaProject.class) : null;
        if (gradleJavaProject != null) {
            classpathInfo = gradleJavaProject
                    .getSourceSets()
                    .values()
                    .stream()
                    .flatMap(gradleJavaSourceSet -> gradleJavaSourceSet.getSourceDirs(SourceType.JAVA).stream())
                    .collect(
                            Collectors.toMap(
                                    f -> ClasspathInfo.create(f),
                                    f -> f.toPath()
                            )
                    );
        }

        String relativePath = null;
        for (Map.Entry<ClasspathInfo, Path> ci : classpathInfo.entrySet()) {
            if (ci.getKey() == null) continue;
            FileObject fo = SourceUtils.getFile(ElementHandle.createTypeElementHandle(ElementKind.CLASS, className), ci.getKey());
            if (fo != null) {
                relativePath = ci.getValue().relativize(FileUtil.toFile(fo).toPath()).toString();
                break;
            }
        }
        if (relativePath != null) {
            return relativePath;
        }

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
