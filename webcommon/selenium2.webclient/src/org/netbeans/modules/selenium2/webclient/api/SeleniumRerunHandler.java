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
            runTests(tests2run.toArray(new FileObject[0]));
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
