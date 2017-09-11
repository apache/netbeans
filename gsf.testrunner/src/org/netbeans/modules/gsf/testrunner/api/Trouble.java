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
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */

package org.netbeans.modules.gsf.testrunner.api;

import org.openide.util.Parameters;

/**
 * Represents a cause for a test failure.
 */
public final class Trouble {

    private boolean error;
    private String[] stackTrace;
    private ComparisonFailure comparisonFailure;

    public Trouble(boolean error) {
        super();
        this.error = error;
    }

    /** 
     * @return  {@code true} if error, {@code false} if failure
     */
    public boolean isError() {
        return error;
    }

    /**
     * @param error - true if error, false if failure
     */
    public void setError(boolean error) {
        this.error = error;
    }

    /**
     * @return the stackTrace
     */
    public String[] getStackTrace() {
        return stackTrace;
    }

    /**
     * @param stackTrace the stackTrace for the failure. The first
     * item in the array is treated as the failure message.
     */
    public void setStackTrace(String[] stackTrace) {
        this.stackTrace = stackTrace;
    }

    /**
     * @return the comparison failure or <code>null</code>.
     */
    public ComparisonFailure getComparisonFailure() {
        return comparisonFailure;
    }

    /**
     * @param comparisonFailure the failure to set. May be <code>null</code>.
     */
    public void setComparisonFailure(ComparisonFailure comparisonFailure) {
        this.comparisonFailure = comparisonFailure;
    }

    /**
     * Represents a comparison failure for two Strings, e.g. an assert_equals failure.
     */
    public static final class ComparisonFailure {

        private final String expected;
        private final String actual;
        private final String mimeType;
        private static final String DEFAULT_MIME_TYPE = "text/plain"; //NOI18N

        /**
         * Constructs a new ComparisonFailure using the default mime type.
         * @param expected the expected value.
         * @param actual the actual value.
         */
        public ComparisonFailure(String expected, String actual) {
            this(expected, actual, DEFAULT_MIME_TYPE);
        }

        /**
         * Constructs a new ComparisonFailure.
         * @param expected the expected value.
         * @param actual the actual value.
         * @param mimeType the mime type for the comparison; must not be <code>null</code>
         * or an empty String.
         */
        public ComparisonFailure(String expected, String actual, String mimeType) {
            Parameters.notEmpty("mimeType", mimeType);
            this.expected = expected;
            this.actual = actual;
            this.mimeType = mimeType;
        }


        public String getActual() {
            return actual;
        }

        public String getExpected() {
            return expected;
        }

        public String getMimeType() {
            return mimeType;
        }
    }

}
