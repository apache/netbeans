/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.netbeans.modules.testng;

import java.util.MissingResourceException;
import java.util.ResourceBundle;
import org.openide.ErrorManager;
import org.openide.util.NbBundle;

/**
 * Helper class representing reasons for skipping a class in the test 
 * generation process. The class enumerates known reasons, why a class may 
 * not be considered testable, allows to combine the reasons and provide 
 * human-readable representation  of them.
 */
@NbBundle.Messages({"TestabilityResult_OK=testable",
"TestabilityResult_Private=private",
"TestabilityResult_PkgPrivate=package-private",
"TestabilityResult_ExceptionClass=an exception class",
"TestabilityResult_NonstaticInnerClass=nonstatic nested",
"TestabilityResult_AbstractClass=abstract",
"TestabilityResult_TestClass=a test class",
"TestabilityResult_NoTestableMethods=contains no testable methods"})
final class TestabilityResult {
    // bitfield of reasons for skipping a class
    private long reason;

    // reason constants
    public static final TestabilityResult OK = new TestabilityResult(0);
    public static final TestabilityResult PACKAGE_PRIVATE_CLASS = new TestabilityResult(1);
    public static final TestabilityResult NO_TESTEABLE_METHODS = new TestabilityResult(2);
    public static final TestabilityResult TEST_CLASS = new TestabilityResult(4);
    public static final TestabilityResult ABSTRACT_CLASS = new TestabilityResult(8);
    public static final TestabilityResult NONSTATIC_INNER_CLASS = new TestabilityResult(16);
    public static final TestabilityResult EXCEPTION_CLASS = new TestabilityResult(32);
    public static final TestabilityResult PRIVATE_CLASS = new TestabilityResult(64);


    // bundle keys for reason descriptions
    private static final String [] reasonBundleKeys = {
        Bundle.TestabilityResult_PkgPrivate(),
        Bundle.TestabilityResult_NoTestableMethods(),
        Bundle.TestabilityResult_TestClass(),
        Bundle.TestabilityResult_AbstractClass(),
        Bundle.TestabilityResult_NonstaticInnerClass(),
        Bundle.TestabilityResult_ExceptionClass(),
        Bundle.TestabilityResult_Private()};

    private TestabilityResult(long reason) {
        this.reason = reason;
    }

    /**
     * Combine two result reasons into a new one.
     *
     * The combination is the union
     * of the failure reasons represented by the two results. Thus,
     * if both are success (no failure), the combination is a success. If 
     * some of them is failed, the result is failed.
     *
     * @param lhs the first TestabilityResult
     * @param rhs the second TestabilityResult
     * @return a new TestabilityResult representing the combination of the two 
     *         results
     **/
    public static TestabilityResult combine(TestabilityResult lhs, TestabilityResult rhs) {
        return new TestabilityResult(lhs.reason | rhs.reason);
    }

   /**
    * Removes reasons from result according to the specified {@code mask}
    * (bit mask). If a bit in the {@code mask} is 1 then the corresponding
    * bit in the returned {@code TestabilityResult} will be set to 0.
    * 
    * @param tr the {@code TestabilityResult} which reasons should be filtered.
    * @param mask the filter bit mask.
    * @return New {@code TestabilityResult} where some bits are set according to
    *         the {@code mask}.
    */
    public static TestabilityResult filter(TestabilityResult tr, long mask) {
        return new TestabilityResult(tr.reason & ~mask);
    }

    /**
     * Returns true if the result is for a testable class.
     * @return true or false
     */
    public boolean isTestable() {
        return reason == 0;
    }

    /**
     * Returns true if the result is for a non-testable class.
     * @return true if the result is for a non-testable class.
     */
    public boolean isFailed() {
        return reason != 0;
    }

    public long getReasonValue(){
        return reason;
    }
    /**
     * Returns a human-readable representation of the reason. If the reason 
     * is a combination of multiple reasons, they are separated with ",".
     * @return String
     */
    public String getReason() {
        return getReason(", ", ", ");                                   //NOI18N
    }

    /**
     * Returns {@link #getReason()}.
     * @return String
     */
    @Override
    public String toString() { 
        return getReason(", ", ", ");                                   //NOI18N
    }

    /** 
     * Returns a human-readable representation of the reason. If the reason 
     * is a combination of multiple reasons, they are separated with 
     * {@code separ} except for the last reason, which is separated 
     * with {@code terminalSepar}
     * <p>
     * For example: getReason(", ", " or ") might return 
     * "abstract, package private or without testable methods".
     *
     * @return String
     */
    public String getReason(String separ, String terminalSepar) {
        try {
            if (reason == 0) {
                return Bundle.TestabilityResult_OK();
            } else {
                String str = "";                                        //NOI18N
                boolean lastPrep = true;
                for (long i = 0, r = reason; r > 0; r >>= 1, i++) {
                    if ((r & 1) != 0) {
                        if (str.length() > 0) {
                            if (lastPrep) {
                                str = terminalSepar + str;
                                lastPrep = false;
                            } else {
                                str = separ + str;
                            }
                        }
                        str = reasonBundleKeys[(int)i] + str;
                    }
                } 
                return str;
            }
        } catch (MissingResourceException ex) {
            ErrorManager.getDefault().notify(ex);
            return "";
        }
    }
    
    /**
     * Class for holding name of a skipped java class
     * together with the reason why it was skipped.
     */
    static final class SkippedClass {
        final String clsName;
        final TestabilityResult reason;
        SkippedClass(String clsName,
                     TestabilityResult reason) {
            this.clsName = clsName;
            this.reason = reason;
        }
    }

}
