/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2015, 2016 Oracle and/or its affiliates. All rights reserved.
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
 */
package org.netbeans.modules.glassfish.tooling.data;

/**
 * Individual server check status returned.
 * <p/>
 * There is also minimal algebra defined to support <code>AND</code>
 * and <code>OR</code>.
 * <p/>
 * @author Tomas Kraus
 */
public enum GlassFishStatusCheckResult {

    /** Server status check passed. */
    SUCCESS,

    /** Server status check failed with <code>FAILED</code> result. */
    FAILED;

    ////////////////////////////////////////////////////////////////////////////
    // Class attributes                                                       //
    ////////////////////////////////////////////////////////////////////////////

    /** Full <code>AND</code> operator state space. */
    private static final GlassFishStatusCheckResult[][] and = {
      // SUCCESS  FAILED
        {SUCCESS, FAILED}, // SUCCESS
        { FAILED, FAILED}  // FAILED
    };

    /** Full <code>OR</code> operator state space. */
    private static final GlassFishStatusCheckResult[][] or = {
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
    public static GlassFishStatusCheckResult and(
            final GlassFishStatusCheckResult s1,
            final GlassFishStatusCheckResult s2) {
        return and[s1.ordinal()][s2.ordinal()];
    }

    /**
     * Compute logical <code>OR</code> of two status values.
     * <p/>
     * @param s1 First operand.
     * @param s2 Second operand.
     */
    public static  GlassFishStatusCheckResult or(
            final GlassFishStatusCheckResult s1,
            final GlassFishStatusCheckResult s2) {
        return or[s1.ordinal()][s2.ordinal()];
    }

    /**
     * Compute logical <code>AND</code> of three status values.
     * <p/>
     * @param s1 First operand.
     * @param s2 Second operand.
     * @param s3 Third operand.
     */
    public static GlassFishStatusCheckResult and(
            final GlassFishStatusCheckResult s1,
            final GlassFishStatusCheckResult s2,
            final GlassFishStatusCheckResult s3) {
        return and[s1.ordinal()][and[s2.ordinal()][s3.ordinal()].ordinal()];
    }

    /**
     * Compute logical <code>OR</code> of three status values.
     * <p/>
     * @param s1 First operand.
     * @param s2 Second operand.
     * @param s3 Third operand.
     */
    public static GlassFishStatusCheckResult or(
            final GlassFishStatusCheckResult s1,
            final GlassFishStatusCheckResult s2,
            final GlassFishStatusCheckResult s3) {
        return or[s1.ordinal()][or[s2.ordinal()][s3.ordinal()].ordinal()];
    }

    ////////////////////////////////////////////////////////////////////////////
    // Methods                                                                //
    ////////////////////////////////////////////////////////////////////////////

    /**
     * Convert <code>GlassFishStatusCheckResult</code> value
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

