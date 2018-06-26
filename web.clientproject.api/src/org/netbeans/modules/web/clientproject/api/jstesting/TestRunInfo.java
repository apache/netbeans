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

package org.netbeans.modules.web.clientproject.api.jstesting;

import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.annotations.common.NullAllowed;
import org.openide.util.Parameters;

/**
 * Class holding information about a test run.
 * <p>
 * This class is thread-safe.
 * @since 1.49
 */
public final class TestRunInfo {

    /**
     * Session type.
     */
    public static enum SessionType {
        /**
         * Normal run.
         */
        TEST,
        /**
         * Run under debugger.
         */
        DEBUG,
    }

    private final SessionType sessionType;
    private final String testFile;


    private TestRunInfo(Builder builder) {
        assert builder != null;
        assert builder.sessionType != null;
        sessionType = builder.sessionType;
        testFile = builder.testFile;
    }

    /**
     * Get session type.
     * @return session type
     */
    public SessionType getSessionType() {
        return sessionType;
    }

    /**
     * Get test file. Can be {@code null} (in such case, all the test file should be run).
     * @return test file, can be {@code null}
     */
    @CheckForNull
    public String getTestFile() {
        return testFile;
    }

    @Override
    public String toString() {
        return "TestRunInfo{" + "sessionType=" + sessionType + ", testFile=" + testFile + '}'; // NOI18N
    }

    //~ Inner classes

    /**
     * Builder for {@link TestRunInfo}.
     * <p>
     * The default {@link Builder#setSessionType(org.netbeans.modules.web.clientproject.api.jstesting.TestRunInfo.SessionType) session type}
     * is {@link SessionType#TEST}.
     * @since 1.49
     */
    public static final class Builder {

        SessionType sessionType = SessionType.TEST;
        String testFile;


        /**
         * Set session type.
         * @param sessionType session type
         * @return this instance
         */
        public Builder setSessionType(@NonNull SessionType sessionType) {
            this.sessionType = sessionType;
            return this;
        }

        /**
         * Set test file.
         * @param testFile test file to be tested, can be {@code null} (in such case,
         *        all the test file should be run)
         * @return this instance
         */
        public Builder setTestFile(@NullAllowed String testFile) {
            this.testFile = testFile;
            return this;
        }

        /**
         * Create {@link TestRunInfo}.
         * @return {@link TestRunInfo} instance
         */
        public TestRunInfo build() {
            Parameters.notNull("sessionType", sessionType); // NOI18N
            return new TestRunInfo(this);
        }

    }

}
