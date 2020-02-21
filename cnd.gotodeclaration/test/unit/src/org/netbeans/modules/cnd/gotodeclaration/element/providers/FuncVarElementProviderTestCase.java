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

package org.netbeans.modules.cnd.gotodeclaration.element.providers;

import java.io.File;
import org.netbeans.spi.jumpto.type.SearchType;

/**
 */
public class FuncVarElementProviderTestCase extends CppSymbolBaseTestCase {

    public FuncVarElementProviderTestCase(String testName) {
        super(testName);
    }

    @Override
    protected void setUp() throws Exception {
        System.setProperty("cnd.modelimpl.tracemodel.project.name", "DummyProject"); // NOI18N
        super.setUp();
    }

    @Override
    protected File getTestCaseDataDir() {
        return getQuoteDataDir();
    }

   public void testFuncVarAllRegexp() throws Exception {
        peformTest("*", SearchType.REGEXP);
    }

    public void testFuncVarDotRegexp() throws Exception {
        peformTest("ma?n", SearchType.REGEXP);
    }

    public void testFuncVarCaseInsensitiveRegexp() throws Exception {
        peformTest("MA?*n", SearchType.CASE_INSENSITIVE_REGEXP);
    }
    
    public void testFuncVarCamelCase() throws Exception {
        peformTest("GC", SearchType.CAMEL_CASE);
    }

    public void testFuncVarPrefix() throws Exception {
        peformTest("ope", SearchType.PREFIX);
    }

    public void testFuncVarCaseInsensitivePrefix() throws Exception {
        peformTest("OpE", SearchType.CASE_INSENSITIVE_PREFIX);
    }

    public void testFuncVarExactName() throws Exception {
        peformTest("main", SearchType.EXACT_NAME);
    }

    public void testFuncVarCaseInsensitiveExactName() throws Exception {
        peformTest("Main", SearchType.CASE_INSENSITIVE_EXACT_NAME);
    }

    public void testMacroDotRegexp() throws Exception {
        peformTest("CPU?H", SearchType.REGEXP);
    }

    public void testMacroCaseInsensitiveRegexp() throws Exception {
        peformTest("_cUs?*r_h", SearchType.CASE_INSENSITIVE_REGEXP);
    }

    public void testMacroCamelCase() throws Exception {
        peformTest("GD", SearchType.CAMEL_CASE);
    }

    public void testMacroPrefix() throws Exception {
        peformTest("M", SearchType.PREFIX);
    }

    public void testMacroCaseInsensitivePrefix() throws Exception {
        peformTest("m", SearchType.CASE_INSENSITIVE_PREFIX);
    }

    public void testMacroExactName() throws Exception {
        peformTest("DISK_H", SearchType.EXACT_NAME);
    }

    public void testMacroCaseInsensitiveExactName() throws Exception {
        peformTest("disk_h", SearchType.CASE_INSENSITIVE_EXACT_NAME);
    }
}
