/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2014 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2014 Sun Microsystems, Inc.
 */
package org.netbeans.modules.selenium2.webclient.api;

import java.io.File;
import java.util.ArrayList;
import java.util.Set;
import javax.swing.event.ChangeListener;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.modules.gsf.testrunner.api.RerunHandler;
import org.netbeans.modules.gsf.testrunner.api.RerunType;
import org.netbeans.modules.gsf.testrunner.api.Testcase;
import org.netbeans.modules.web.clientproject.api.jstesting.JsTestingProvider;
import org.netbeans.modules.web.clientproject.api.jstesting.JsTestingProviders;
import org.netbeans.modules.web.clientproject.api.jstesting.TestRunInfo;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.ChangeSupport;
import org.openide.util.Pair;
import org.openide.util.RequestProcessor;

/**
 *
 * @author Theofanis Oikonomou
 */
public final class SeleniumRerunHandler implements RerunHandler {

    private final ChangeSupport changeSupport = new ChangeSupport(this);
    private static final RequestProcessor RP = new RequestProcessor(SeleniumRerunHandler.class.getName(), 1);

    private volatile boolean enabled = true;
    private final FileObject[] activatedFOs;
    private final Project project;
    private final boolean isSelenium;
    private final String identifier;

    public SeleniumRerunHandler(Project project, FileObject[] activatedFOs, String identifier, boolean isSelenium) {
        this.project = project;
        this.activatedFOs = activatedFOs;
        this.isSelenium = isSelenium;
        this.identifier = identifier;
    }

    @Override
    public void rerun() {
        setEnabled(false);
        runTests(activatedFOs);
        setEnabled(true);
    }

    @Override
    public void rerun(Set<Testcase> tests) {
        setEnabled(false);
        ArrayList<FileObject> tests2run = new ArrayList<>();
        for (Testcase testcase : tests) {
            testcase.getTrouble().getStackTrace();
            TestRunnerReporter.CallStackCallback callStackCallback = new TestRunnerReporter.CallStackCallback(project);
            for (String callstackFrameInfo : testcase.getTrouble().getStackTrace()) {
                Pair<File, int[]> pair = callStackCallback.parseLocation(callstackFrameInfo, true);
                if (pair != null) {
                    FileObject fo = FileUtil.toFileObject(pair.first());
                    if (!tests2run.contains(fo)) {
                        tests2run.add(fo);
                        break;
                    }
                }
            }
        }
        if (!tests2run.isEmpty()) {
            runTests(tests2run.toArray(new FileObject[tests2run.size()]));
        }
        setEnabled(true);
    }
    
    private void runTests(final FileObject[] testFOs) {
        RP.post(new Runnable() {
            @Override
            public void run() {
                Project p = FileOwnerQuery.getOwner(testFOs[0]);
                if (p == null) {
                    return;
                }
                if (isSelenium) {
                    SeleniumTestingProvider provider = SeleniumTestingProviders.getDefault().findSeleniumTestingProvider(identifier);
                    if (provider != null) {
                        provider.runTests(testFOs);
                    }
                } else {
                    JsTestingProvider provider = JsTestingProviders.getDefault().findJsTestingProvider(identifier);
                    if (provider != null) {
                        provider.runTests(project, new TestRunInfo.Builder().setSessionType(TestRunInfo.SessionType.TEST).setTestFile(null).build());
                    }
                }
            }
        });
    }

    @Override
    public boolean enabled(RerunType type) {
        switch (type) {
            case ALL:
                return enabled;
            case CUSTOM:
                return enabled;
            default:
                assert false : "Unknown rerun type: " + type;
        }
        return false;
    }

    @Override
    public void addChangeListener(ChangeListener listener) {
        changeSupport.addChangeListener(listener);
    }

    @Override
    public void removeChangeListener(ChangeListener listener) {
        changeSupport.removeChangeListener(listener);
    }

    private void setEnabled(boolean newEnabled) {
        if (enabled != newEnabled) {
            enabled = newEnabled;
            changeSupport.fireChange();
        }
    }
}
