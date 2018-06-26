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
package org.netbeans.modules.php.spi.testing.run;

import java.util.concurrent.Callable;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.annotations.common.NullAllowed;
import org.netbeans.modules.php.api.util.StringUtils;
import org.netbeans.modules.php.spi.testing.locate.Locations;

/**
 * Interface for a test case.
 */
public interface TestCase {

    /**
     * Possible statuses of test case.
     */
    enum Status {
        PASSED,
        PENDING,
        FAILED,
        ERROR,
        ABORTED,
        SKIPPED,
        PASSEDWITHERRORS,
        IGNORED,
    }

    /**
     * Set class name of this test case.
     * @param className class name of this test case
     * @since 0.2
     */
    void setClassName(@NonNull String className);

    /**
     * Set location of this test case.
     * @param location location of this test case
     * @since 0.2
     */
    void setLocation(@NonNull Locations.Line location);

    /**
     * Set time of this test case run, in milliseconds.
     * @param time time of this test case run, in milliseconds
     * @since 0.2
     */
    void setTime(long time);

    /**
     * Set status of this test case.
     * @param status status of this test case
     * @since 0.2
     */
    void setStatus(@NonNull Status status);

    /**
     * Set stack trace of this test case for the failure.
     * @param message failure message
     * @param stackTrace stack trace of this test case for the failure
     * @param error {@code true} if this test case failed, {@code false} otherwise
     * @param diff difference for the test failure, can be {@link Diff#NOT_KNOWN not known}
     * @since 0.2
     */
    void setFailureInfo(@NonNull String message, @NonNull String[] stackTrace, boolean error, @NonNull Diff diff);

    //~ Inner classes

    /**
     * Difference for the test failure.
     */
    final class Diff {

        private static final Logger LOGGER = Logger.getLogger(Diff.class.getName());

        /**
         * {@link #isValid() Invalid} instance for not known differences.
         * @since 0.2
         */
        public static final Diff NOT_KNOWN = new Diff((String) null, (String) null);

        private final Callable<String> expectedTask;
        private final Callable<String> actualTask;

        private volatile String expected;
        private volatile String actual;


        /**
         * Create new difference for the test failure.
         * @param expected the expected value
         * @param actual actual value
         */
        public Diff(@NullAllowed String expected, @NullAllowed String actual) {
            this.expected = expected;
            this.actual = actual;
            expectedTask = null;
            actualTask = null;
        }

        /**
         * Create new difference for the test failure.
         * @param expectedTask task that returns the expected value (should be thread-safe)
         * @param actualTask task that returns actual value (should be thread-safe)
         * @since 0.8
         */
        public Diff(@NullAllowed Callable<String> expectedTask, @NullAllowed Callable<String> actualTask) {
            this.expectedTask = expectedTask;
            this.actualTask = actualTask;
        }

        /**
         * Get the expected value, can be {@code null}.
         * @return the expected value, can be {@code null}
         */
        @CheckForNull
        public String getExpected() {
            if (expected != null) {
                return expected;
            }
            if (expectedTask == null) {
                return null;
            }
            try {
                expected = expectedTask.call();
            } catch (Exception ex) {
                LOGGER.log(Level.INFO, null, ex);
            }
            return expected;
        }

        /**
         * Get the actual value, can be {@code null}.
         * @return the actual value, can be {@code null}
         */
        @CheckForNull
        public String getActual() {
            if (actual != null) {
                return actual;
            }
            if (actualTask == null) {
                return null;
            }
            try {
                actual = actualTask.call();
            } catch (Exception ex) {
                LOGGER.log(Level.INFO, null, ex);
            }
            return actual;
        }

        /**
         * Check validity of this difference, it means that expected
         * or actual value must contain any characters.
         * @return {@code true} if expected or actual value contains any characters
         */
        public boolean isValid() {
            return StringUtils.hasText(getExpected()) || StringUtils.hasText(getActual());
        }

        @Override
        public String toString() {
            return "Diff{" + "expected=" + getExpected() + ", actual=" + getActual() + '}'; // NOI18N
        }

    }

}
