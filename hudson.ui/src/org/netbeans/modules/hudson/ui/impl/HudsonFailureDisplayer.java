/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2013 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2013 Sun Microsystems, Inc.
 */

package org.netbeans.modules.hudson.ui.impl;

import java.awt.event.ActionEvent;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.swing.AbstractAction;
import javax.swing.Action;
import org.netbeans.api.java.classpath.GlobalPathRegistry;
import org.netbeans.api.project.Project;
import org.netbeans.modules.gsf.testrunner.ui.api.CallstackFrameNode;
import org.netbeans.modules.gsf.testrunner.ui.api.DiffViewAction;
import org.netbeans.modules.gsf.testrunner.ui.api.Manager;
import org.netbeans.modules.gsf.testrunner.ui.api.TestMethodNode;
import org.netbeans.modules.gsf.testrunner.ui.api.TestRunnerNodeFactory;
import org.netbeans.modules.gsf.testrunner.api.TestSession;
import org.netbeans.modules.gsf.testrunner.api.TestSuite;
import org.netbeans.modules.gsf.testrunner.api.Testcase;
import org.netbeans.modules.gsf.testrunner.ui.api.TestsuiteNode;
import org.netbeans.modules.gsf.testrunner.api.Trouble;
import org.netbeans.modules.hudson.api.HudsonJob;
import org.netbeans.modules.hudson.api.HudsonJobBuild;
import org.netbeans.modules.hudson.api.HudsonMavenModuleBuild;
import org.netbeans.modules.hudson.api.ui.FailureDataDisplayer;
import org.netbeans.modules.hudson.api.ui.OpenableInBrowser;
import org.netbeans.modules.hudson.spi.FailureDataDisplayerImpl;
import org.netbeans.modules.hudson.ui.actions.Hyperlinker;
import org.netbeans.modules.hudson.ui.actions.OpenUrlAction;
import org.openide.awt.StatusDisplayer;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;
import org.openide.windows.IOProvider;
import org.openide.windows.InputOutput;
import org.openide.windows.OutputWriter;

/**
 *
 * @author jhavlin
 */
@NbBundle.Messages({
    "# {0} - job #build", "ShowFailures.title={0} Test Failures",
    "# {0} - class & method name of failed test", "# {1} - suite name of failed test", "ShowFailures.from_suite={0} (from {1})",
    "LBL_GotoSource=Go to Source",
    "# {0} - Java source file resource path", "no_source_to_hyperlink=Could not find {0} among open projects."
})
public class HudsonFailureDisplayer extends FailureDataDisplayerImpl {

    private static final RequestProcessor RP = new RequestProcessor(
            HudsonFailureDisplayer.class.getName());
    private static final Logger LOG = Logger.getLogger(
            HudsonFailureDisplayer.class.getName());
    private static final Pattern ASSERTION_FAILURE = Pattern.compile(
            "(?m)junit[.]framework[.](AssertionFailedError|(Array)?ComparisonFailure)|java[.]lang[.]AssertionError($|: )"); //NOI18N

    private final Project project;
    private final TestSession session;
    private final Hyperlinker hyperlinker;
    private InputOutput io;

    private final HudsonJob job;
    private final HudsonJobBuild build;
    private final String displayName;

    public HudsonFailureDisplayer(HudsonJobBuild build) {
        this(build, build.getDisplayName());
    }

    public HudsonFailureDisplayer(HudsonMavenModuleBuild moduleBuild) {
        this(moduleBuild.getBuild(), moduleBuild.getDisplayName());
    }

    private HudsonFailureDisplayer(HudsonJobBuild build,
            String displayName) {
        this.build = build;
        this.job = this.build.getJob();
        this.displayName = displayName;
        this.hyperlinker = new Hyperlinker(job);
        // Store reference to project here, as reference in test session
        // is weak.
        this.project = createDummyProject();
        this.session = createTestSession(displayName);
    }

    private Project createDummyProject() {
        return new Project() {
            public @Override
            FileObject getProjectDirectory() {
                return FileUtil.createMemoryFileSystem().getRoot();
            }

            public @Override
            Lookup getLookup() {
                return Lookup.EMPTY;
            }
        };
    }

    private TestSession createTestSession(String displayName) {
        Manager.getInstance().setNodeFactory(new HudsonTestRunnerNodeFactory());
        return new TestSession(displayName, project, TestSession.SessionType.TEST);
    }

    private void prepareOutput() {
        if (io == null) {
            String title = Bundle.ShowFailures_title(displayName);
            io = IOProvider.getDefault().getIO(title, new Action[0]);
            io.select();
            Manager.getInstance().testStarted(session);
        }
    }

    @Override
    public void open() {
    }

    @Override
    public void showSuite(FailureDataDisplayer.Suite s) {

        prepareOutput();
        OutputWriter out = io.getOut();
        OutputWriter err = io.getErr();
        TestSuite suite = new TestSuite(s.getName());
        session.addSuite(suite);
        Manager.getInstance().displaySuiteRunning(session, suite.getName());
        if (s.getStderr() != null) {
            // XXX TR window does not seem to show only stdio from selected suite
            Manager.getInstance().displayOutput(session, s.getStderr(), true);
        }
        if (s.getStdout() != null) {
            Manager.getInstance().displayOutput(session, s.getStdout(), false);
        }
        for (final FailureDataDisplayer.Case c : s.getCases()) {
            if (c.getErrorStackTrace() == null) {
                continue;
            }
            String name = c.getClassName() + "." + c.getName(); //NOI18N
            String shortName = c.getName();
            if (s.getName() != null && !s.getName().equals(c.getClassName())) {
                shortName = name;
                name = Bundle.ShowFailures_from_suite(name, s.getName());
            }
            println();
            out.println("[" + name + "]"); // XXX use color printing to make it stand out? //NOI18N
            show(c.getErrorStackTrace(), /* err is too hard to read */ out);
            Testcase test = new Testcase(shortName, null, session);
            test.setClassName(c.getClassName());
            Trouble trouble = new Trouble(!ASSERTION_FAILURE.matcher(c.getErrorStackTrace()).lookingAt());
            trouble.setStackTrace(c.getErrorStackTrace().split("\r?\n")); //NOI18N
            // XXX call setComparisonFailure if matches "expected:<...> but was:<...>"
            test.setTrouble(trouble);
            LOG.log(Level.FINE, "got {0} as {1}", new Object[]{name, test.getStatus()}); //NOI18N
            test.setTimeMillis(c.getDuration());
            session.addTestCase(test);
        }
        if (s.getStderr() != null || s.getStdout() != null) {
            println();
            show(s.getStderr(), err);
            show(s.getStdout(), out);
        }
        Manager.getInstance().displayReport(session, session.getReport(s.getDuration()));
    }
    boolean firstLine = true;

    void println() {
        if (firstLine) {
            firstLine = false;
        } else {
            io.getOut().println();
        }
    }

    void show(String lines, OutputWriter w) {
        if (lines == null) {
            return;
        }
        for (String line : lines.split("\r\n?|\n")) { //NOI18N
            hyperlinker.handleLine(line, w);
        }
    }

    @Override
    public void close() {
        if (io != null) {
            io.getOut().close();
            io.getErr().close();
            Manager.getInstance().sessionFinished(session);
        }
    }

    private class HudsonTestRunnerNodeFactory extends TestRunnerNodeFactory {

        public HudsonTestRunnerNodeFactory() {
        }

        public @Override
        TestsuiteNode createTestSuiteNode(String suiteName, boolean filtered) {
            // XXX could add OpenableInBrowser
            return new TestsuiteNode(suiteName, filtered);
        }

        public @Override
        org.openide.nodes.Node createTestMethodNode(final Testcase testcase,
                Project project) {
            return new TestMethodNode(testcase, project) {
                public @Override
                Action[] getActions(boolean context) {
                    return new Action[]{
                        OpenUrlAction.forOpenable(new OpenableInBrowser() {
                            public @Override
                            String getUrl() {
                                return getUrl() + "testReport/"
                                        + testcase.getClassName().replaceFirst("[.][^.]+$", "") + "/" + testcase.getClassName().replaceFirst(".+[.]", "") + "/" + testcase.getName() + "/"; //NOI18N
                            }
                        }),
                        new DiffViewAction(testcase),};
                }
            };
        }

        public @Override
        org.openide.nodes.Node createCallstackFrameNode(String frameInfo, String displayName) {
            return new CallstackFrameNode(frameInfo, displayName) {
                public @Override
                Action getPreferredAction() {
                    return new AbstractAction(Bundle.LBL_GotoSource()) {
                        public @Override
                        void actionPerformed(ActionEvent e) {
                            // XXX should have utility API to parse stack traces
                            final Matcher m = Pattern.compile("\tat (.+[.])[^.]+[.][^.]+[(]([^.]+[.]java):([0-9]+)[)]").matcher(frameInfo); //NOI18N
                            if (m.matches()) {
                                final String resource = m.group(1).replace('.', '/') + m.group(2);
                                RP.post(new Runnable() {
                                    @Override
                                    public void run() {
                                        FileObject f = GlobalPathRegistry.getDefault().findResource(resource);
                                        LOG.log(Level.FINER, "matched {0} -> {1}", new Object[]{resource, f}); //NOI18N
                                        if (f != null) {
                                            HudsonLoggerHelper.openAt(f, Integer.parseInt(m.group(3)) - 1, -1, true);
                                        } else {
                                            StatusDisplayer.getDefault().setStatusText(Bundle.no_source_to_hyperlink(resource));
                                        }
                                    }
                                });
                            } else {
                                LOG.log(Level.FINER, "no match for {0}", frameInfo); //NOI18N
                            }
                        }
                    };
                }

                public @Override
                Action[] getActions(boolean context) {
                    return new Action[]{getPreferredAction()};
                }
            };
        }
    }
}
