/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
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
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
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
 */
package org.netbeans.performance.j2se.startup;

import org.netbeans.modules.performance.utilities.MeasureStartupTimeTestCase;

/**
 * Measure startup time by org.netbeans.core.perftool.StartLog. Number of starts
 * with new userdir is defined by property
 * <br> <code> org.netbeans.performance.repeat.with.new.userdir </code>
 * <br> and number of starts with old userdir is defined by property
 * <br> <code> org.netbeans.performance.repeat </code> Run measurement defined
 * number times, but forget first measured value, it's a attempt to have still
 * the same testing conditions with loaded and cached files.
 *
 * @author mmirilovic@netbeans.org
 */
public class OutOfTheBoxStartup extends MeasureStartupTimeTestCase {

    public static final String suiteName = "J2SE Startup suite";

    /**
     * Define test case
     *
     * @param testName name of the test case
     */
    public OutOfTheBoxStartup(String testName) {
        super(testName);
    }

    /**
     * Testing start of IDE with measurement of the startup time.
     *
     * @throws java.io.IOException
     */
    public void testStartIDE() throws java.io.IOException {
        String performanceDataName = "Startup Time";

        // don't report first run, try to have still the same testing conditions
        runIDE(getIdeHome(), new java.io.File(getWorkDir(), "ideuserdir_prepare"), getMeasureFile(0, 0), 1000);

        for (int n = 1; n <= repeatNewUserdir; n++) {
            for (int i = 1; i <= repeat; i++) {
                long measuredTime = runIDEandMeasureStartup(performanceDataName, getMeasureFile(i, n), getUserdirFile(n), 10000);
                reportPerformance(performanceDataName, measuredTime, "ms", i > 1 ? 2 : 1);
            }
        }
        PerformanceData[] pData = this.getPerformanceData();
        for (PerformanceData pData1 : pData) {
            org.netbeans.modules.performance.utilities.CommonUtilities.processUnitTestsResults(this.getClass().getName(), System.getProperty("suitename"), pData1);
        }
    }
}
