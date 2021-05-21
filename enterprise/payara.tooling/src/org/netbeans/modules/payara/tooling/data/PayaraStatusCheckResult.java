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
package org.netbeans.modules.payara.tooling.data;

/**
 * Individual server check status returned.
 * <p/>
 * There is also minimal algebra defined to support <code>AND</code>
 * and <code>OR</code>.
 * <p/>
 * @author Tomas Kraus
 */
public enum PayaraStatusCheckResult {

    /** Server status check passed. */
    SUCCESS,

    /** Server status check failed with <code>FAILED</code> result. */
    FAILED;

    ////////////////////////////////////////////////////////////////////////////
    // Class attributes                                                       //
    ////////////////////////////////////////////////////////////////////////////

    /** Full <code>AND</code> operator state space. */
    private static final PayaraStatusCheckResult[][] and = {
      // SUCCESS  FAILED
        {SUCCESS, FAILED}, // SUCCESS
        { FAILED, FAILED}  // FAILED
    };

    /** Full <code>OR</code> operator state space. */
    private static final PayaraStatusCheckResult[][] or = {
      // SUCCESS   FAILED
        {SUCCESS, SUCCESS}, // SUCCESS
        {SUCCESS,  FAILED}  // FAILED
    };

    ////////////////////////////////////////////////////////////////////////////
    // Static methods                                                         //
    ////////////////////////////////////////////////////////////////////////////

    /**
     * Compute logical <code>AND</code> of two status values.
     * <p/>
     * @param s1 First operand.
     * @param s2 Second operand.
     */
    public static PayaraStatusCheckResult and(
            final PayaraStatusCheckResult s1,
            final PayaraStatusCheckResult s2) {
        return and[s1.ordinal()][s2.ordinal()];
    }

    /**
     * Compute logical <code>OR</code> of two status values.
     * <p/>
     * @param s1 First operand.
     * @param s2 Second operand.
     */
    public static  PayaraStatusCheckResult or(
            final PayaraStatusCheckResult s1,
            final PayaraStatusCheckResult s2) {
        return or[s1.ordinal()][s2.ordinal()];
    }

    /**
     * Compute logical <code>AND</code> of three status values.
     * <p/>
     * @param s1 First operand.
     * @param s2 Second operand.
     * @param s3 Third operand.
     */
    public static PayaraStatusCheckResult and(
            final PayaraStatusCheckResult s1,
            final PayaraStatusCheckResult s2,
            final PayaraStatusCheckResult s3) {
        return and[s1.ordinal()][and[s2.ordinal()][s3.ordinal()].ordinal()];
    }

    /**
     * Compute logical <code>OR</code> of three status values.
     * <p/>
     * @param s1 First operand.
     * @param s2 Second operand.
     * @param s3 Third operand.
     */
    public static PayaraStatusCheckResult or(
            final PayaraStatusCheckResult s1,
            final PayaraStatusCheckResult s2,
            final PayaraStatusCheckResult s3) {
        return or[s1.ordinal()][or[s2.ordinal()][s3.ordinal()].ordinal()];
    }

    ////////////////////////////////////////////////////////////////////////////
    // Methods                                                                //
    ////////////////////////////////////////////////////////////////////////////

    /**
     * Convert <code>PayaraStatusCheckResult</code> value
     * to <code>String</code>.
     * <p/>
     * @return A <code>String</code> representation of the value
     *         of this object.
     */
    @Override
    public String toString() {
        switch(this) {
            case SUCCESS:   return "SUCCESS";
            case FAILED:    return "FAILED";
            default:
                throw new IllegalStateException("Unknown Status value");
        }
    }

}

