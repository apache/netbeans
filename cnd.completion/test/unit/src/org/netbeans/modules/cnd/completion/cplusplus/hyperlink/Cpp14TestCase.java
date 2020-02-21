/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2016 Oracle and/or its affiliates. All rights reserved.
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
package org.netbeans.modules.cnd.completion.cplusplus.hyperlink;

import org.netbeans.modules.cnd.modelimpl.trace.TraceModelFileFilter;

/**
 *
 *
 */
public class Cpp14TestCase extends HyperlinkBaseTestCase {

    public Cpp14TestCase(String testName) {
        super(testName, true);
    }

    @Override
    protected void setUp() throws Exception {
        System.setProperty("cnd.modelimpl.tracemodel.project.name", "DummyProject"); // NOI18N
        System.setProperty("parser.report.errors", "true");
        System.setProperty("antlr.exceptions.hideExpectedTokens", "true");
        System.setProperty("cnd.language.flavor.cpp14", "true");         
        super.setUp();
    }
    
    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
        System.setProperty("cnd.language.flavor.cpp14", "false");
    }

    public void testBug268671() throws Exception {
        // Bug 268671 - C++14: IDE parser fails on "Variable templates"
        performTest("bug268671.cpp", 15, 32, "bug268671.cpp", 6, 9);
        performTest("bug268671.cpp", 16, 39, "bug268671.cpp", 6, 9);
    }
    
    public void testBug269290() throws Exception {
        // Bug 269290 - C++14: unresolved return type of function with auto type
        performTest("bug269290.cpp", 25, 23, "bug269290.cpp", 3, 9);
        performTest("bug269290.cpp", 27, 21, "bug269290.cpp", 3, 9);
    }
    
    public void testBug269292() throws Exception {
        // Bug 269292 - С++14: decltype(auto) is not supported
        performTest("bug269292.cpp", 17, 23, "bug269292.cpp", 3, 9);
        performTest("bug269292.cpp", 18, 23, "bug269292.cpp", 3, 9);
        performTest("bug269292.cpp", 19, 23, "bug269292.cpp", 7, 9);
        performTest("bug269292.cpp", 21, 15, "bug269292.cpp", 7, 9);
    }
}
