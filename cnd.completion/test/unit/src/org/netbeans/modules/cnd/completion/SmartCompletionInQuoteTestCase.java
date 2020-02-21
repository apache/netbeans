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
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.cnd.completion;

import java.io.File;
import org.netbeans.junit.Manager;
import org.netbeans.modules.cnd.completion.cplusplus.ext.CompletionBaseTestCase;
import org.netbeans.modules.cnd.completion.cplusplus.ext.CompletionTestPerformer;
import org.netbeans.modules.cnd.completion.csm.CompletionResolver;

/**
 *
 *
 */
public class SmartCompletionInQuoteTestCase extends CompletionBaseTestCase {

    public SmartCompletionInQuoteTestCase(String name) {
        super(name, false); // we do not plan to modify or insert something in this test case
    }

    @Override 
    protected File getTestCaseDataDir() {
        return getQuoteDataDir();
    } 

    protected final File getQuoteDataDir() {
        return Manager.normalizeFile(new File(getDataDir(), "common/quote_nosyshdr"));
    }

    @Override
    protected CompletionTestPerformer createTestPerformer() {
        return new CompletionTestPerformer(CompletionResolver.QueryScope.SMART_QUERY);
    }

    public void testInCpuConstructorImpl() throws Exception {
        super.performTest("cpu.cc", 48, 9);
    }

    public void testInCpuComputeSupportMetricImplInExpr() throws Exception {
        super.performTest("cpu.cc", 58, 27);
    }

    public void testInCpuComputeSupportMetricImplInSwitch() throws Exception {
        super.performTest("cpu.cc", 60, 14);
    }

    public void testInCpuComputeSupportMetricImplInCase() throws Exception {
        super.performTest("cpu.cc", 61, 16);
    }

    public void testInCpuComputeSupportMetricImplInMethodCall() throws Exception {
        super.performTest("cpu.cc", 70, 7);
    }

    public void testInCpuComputeSupportMetricImplInMethodCallParam() throws Exception {
        super.performTest("cpu.cc", 70, 23);
    }

    public void testClassesInParameters() throws Exception {
        super.performTest("customer.cc", 57, 45);
    }

    public void testCCAfterSemicolon() throws Exception {
        super.performTest("quote.cc", 142, 28);
    }
}
