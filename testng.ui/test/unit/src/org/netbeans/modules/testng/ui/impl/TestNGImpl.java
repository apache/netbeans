/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright © 2008-2012 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */
package org.netbeans.modules.testng.ui.impl;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.netbeans.api.project.Project;
import org.netbeans.modules.testng.api.TestNGSupport.Action;
import org.netbeans.modules.testng.spi.TestConfig;
import org.netbeans.modules.testng.spi.TestNGSupportImplementation;
import org.openide.filesystems.FileObject;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author lukas
 */
@ServiceProvider(service = TestNGSupportImplementation.class,
    supersedes = {"org.netbeans.modules.testng.ant.AntTestNGSupport",
                  "org.netbeans.modules.testng.maven.MavenTestNGSupport"})
public class TestNGImpl extends TestNGSupportImplementation {

    private static List<Action> sa = new ArrayList<Action>();
    private boolean configured = false;
    private static TestExecutorImpl te = new TestExecutorImpl();

    public static void setSupportedActions(Action... a) {
        sa.clear();
        sa = Arrays.asList(a);
    }

    public static TestExecutorImpl getTestExecutor() {
        return te;
    }

    public boolean isConfigured() {
        return configured;
    }

    @Override
    public boolean isActionSupported(Action action, Project p) {
        return sa.contains(action);
    }

    @Override
    public void configureProject(FileObject createdFile) {
        configured = true;
    }

    @Override
    public TestExecutor createExecutor(Project p) {
        te.p = p;
        return te;
    }

    @Override
    public boolean isSupportEnabled(FileObject[] activatedFOs) {
        return true;
    }

    public static class TestExecutorImpl implements TestExecutor {

        private boolean hasFailed = false;
        private Project p;
        private Action executedAction = null;
        private TestConfig testConfig = null;

        public void setHasFailed(boolean hasFailed) {
            this.hasFailed = hasFailed;
        }

        public Action getExecutedAction() {
            return executedAction;
        }

        public TestConfig getTestConfig() {
            return testConfig;
        }

        public boolean hasFailedTests() {
            return hasFailed;
        }

        public void execute(Action action, TestConfig config) throws IOException {
            executedAction = action;
            testConfig = config;
        }
    }
}
