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
package org.netbeans.modules.hudson.api.ui;

import java.util.ArrayList;
import java.util.List;
import org.netbeans.modules.hudson.api.HudsonJobBuild;
import org.netbeans.modules.hudson.api.HudsonMavenModuleBuild;
import org.netbeans.modules.hudson.spi.BuilderConnector.FailureDataProvider;
import org.netbeans.modules.hudson.spi.FailureDataDisplayerImpl;

/**
 * Displayer of failure data. Instances of this class will be passed to
 * {@link FailureDataProvider#showFailures(HudsonJobBuild, FailureDataDisplayer)}
 * and
 * {@link FailureDataProvider#showFailures(HudsonMavenModuleBuild, FailureDataDisplayer)}.
 *
 * Do not implement this interface in your classes. Use
 * {@link FailureDataDisplayerImpl} instead.
 *
 * @author jhavlin
 */
public interface FailureDataDisplayer {

    /**
     * Prepare the displayer for writing. Prepare needed resources.
     */
    void open();

    /**
     * Show a test suite.
     *
     * @param suite Test suite data.
     */
    void showSuite(Suite suite);

    /**
     * Finish writing to the displayer. Close all resources.
     */
    void close();

    /**
     * Info about failed test suite.
     */
    public static final class Suite {

        private String name;
        private String stdout;
        private String stderr;
        private long duration;
        private final List<Case> cases = new ArrayList<Case>();

        public String getName() {
            return name;
        }

        public String getStdout() {
            return stdout;
        }

        public String getStderr() {
            return stderr;
        }

        public long getDuration() {
            return duration;
        }

        public void setName(String name) {
            this.name = name;
        }

        public void setStdout(String stdout) {
            this.stdout = stdout;
        }

        public void setStderr(String stderr) {
            this.stderr = stderr;
        }

        public void setDuration(long duration) {
            this.duration = duration;
        }

        public List<Case> getCases() {
            return cases;
        }

        public void addCase(Case cs) {
            cases.add(cs);
        }
    }

    /**
     * Info about failed test case.
     */
    public static final class Case {

        private String className;
        private String name;
        private String errorStackTrace;
        private long duration;

        public String getClassName() {
            return className;
        }

        public String getName() {
            return name;
        }

        public String getErrorStackTrace() {
            return errorStackTrace;
        }

        public long getDuration() {
            return duration;
        }

        public void setClassName(String className) {
            this.className = className;
        }

        public void setName(String name) {
            this.name = name;
        }

        public void setErrorStackTrace(String errorStackTrace) {
            this.errorStackTrace = errorStackTrace;
        }

        public void setDuration(long duration) {
            this.duration = duration;
        }
    }
}
