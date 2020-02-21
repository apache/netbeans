/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2015 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2015 Sun Microsystems, Inc.
 */
package org.netbeans.modules.cnd.highlight.hints.formatstring;

import java.io.File;
import java.util.Arrays;
import java.util.List;
import org.netbeans.modules.cnd.highlight.error.ErrorHighlightingBaseTestCase;

/**
 * Tests for the format string checks
 */
public class FormatStringTest extends ErrorHighlightingBaseTestCase {
    
    public FormatStringTest(String testName) {
        super(testName);
    }
    
    @Override
    protected File[] changeDefProjectDirBeforeParsingProjectIfNeeded(File projectDir) {
        File srcDir = new File(projectDir, "src"); // NOI18N
        File incl1 = new File(projectDir, "sys_includes"); // NOI18N
        List<String> sysIncludes = Arrays.asList(incl1.getAbsolutePath());
        super.setSysIncludes(srcDir.getAbsolutePath(), sysIncludes);
        return new File[]{srcDir};
    }
    
    public void testGeneralPositive() throws Exception {
        performStaticTest("src/general_positive_test.cpp"); // NOI18N
    }
    
    public void testGeneralNegative() throws Exception {
        performStaticTest("src/general_negative_test.cpp"); // NOI18N
    }
    
    public void testBug254435() throws Exception {
        performStaticTest("src/bug254435.c"); // NOI18N
    }
    
    public void testBug254472() throws Exception {
        performStaticTest("src/bug254472.c"); // NOI18N
    }
    
    public void testBug254469() throws Exception {
        performStaticTest("src/bug254469.cpp"); // NOI18N
    }
    
    public void testBug254476() throws Exception {
        performStaticTest("src/bug254476.c"); // NOI18N
    }
    
    public void testBug254475() throws Exception {
        performStaticTest("src/bug254475.cpp"); // NOI18N
    }
    
    public void testBug254508() throws Exception {
        performStaticTest("src/bug254508.c"); // NOI18N
    }
    
    public void testBug254500() throws Exception {
        performStaticTest("src/bug254500.c"); // NOI18N
    }
    
    public void testBug254545() throws Exception {
        performStaticTest("src/bug254545.c"); // NOI18N
    }
    
    public void testBug254803() throws Exception {
        performStaticTest("src/bug254803.cpp"); // NOI18N
    }
    
    public void testBug255083() throws Exception {
        performStaticTest("src/bug255083.c"); // NOI18N
    }
    
    public void testBug255270() throws Exception {
        performStaticTest("src/bug255270.c"); // NOI18N
    }
    
    public void testBug255378() throws Exception {
        performStaticTest("src/bug255378.c"); // NOI18N
    }
    
    public void testBug254580() throws Exception {
        performStaticTest("src/bug254580.c"); // NOI18N
    }
    
    public void testBug256254() throws Exception {
        performStaticTest("src/bug256254.c"); // NOI18N
    }
    
    public void testBug255693() throws Exception {
        performStaticTest("src/bug255693.c"); // NOI18N
    }
    
    public void testBug256321() throws Exception {
        performStaticTest("src/bug256321.c"); // NOI18N
    }
    
    public void testBug257545() throws Exception {
        performStaticTest("src/bug257545.c"); // NOI18N
    }
    
    public void testBug259130() throws Exception {
        performStaticTest("src/bug259130.cpp"); // NOI18N
    }
    
    public void testBug267505() throws Exception {
        performFixesTest("src/bug267505.cpp"); // NOI18N
    }
}
