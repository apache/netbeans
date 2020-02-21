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

package org.netbeans.modules.cnd.completion.cplusplus.hyperlink;

import org.netbeans.junit.RandomlyFails;

/**
 *
 */
@RandomlyFails
public class MetaprogrammingTestCase extends HyperlinkBaseTestCase {

    public MetaprogrammingTestCase(String testName) {
        super(testName);
    }

    @Override
    protected void setUp() throws Exception {
        System.setProperty("cnd.modelimpl.expression.evaluator.deep.variable.provider", "true");
        System.setProperty("cnd.modelimpl.expression.evaluator.recursive.calc", "true");
        System.setProperty("cnd.modelimpl.expression.evaluator.extra.spec.params.matching", "true");
        super.setUp();
    }

    public void testTemplateCalc() throws Exception {
        // Some calculations on templates
        performTest("template_calc.cpp", 10, 7, "template_calc.cpp", 5, 5);
    }
    
    public void testTemplateStaticCalc() throws Exception {
        // Some calculations on templates
        performTest("template_static_calc.cpp", 17, 7, "template_static_calc.cpp", 12, 5);
    }
    
    public void testBug172419() throws Exception {
        // Bug 172419 - Boost metaprogramming usage problem
        performTest("bug172419.cpp", 53, 9, "bug172419.cpp", 42, 5);
        performTest("bug172419.cpp", 59, 10, "bug172419.cpp", 42, 5);
    }    

    public void testBug172419_2() throws Exception {
        // Bug 172419 - Boost metaprogramming usage problem
        performTest("bug172419_2.cpp", 293, 12, "bug172419_2.cpp", 260, 5);
        performTest("bug172419_2.cpp", 296, 12, "bug172419_2.cpp", 260, 5);
        performTest("bug172419_2.cpp", 299, 12, "bug172419_2.cpp", 260, 5);
        performTest("bug172419_2.cpp", 302, 12, "bug172419_2.cpp", 260, 5);
        performTest("bug172419_2.cpp", 312, 12, "bug172419_2.cpp", 260, 5);
        performTest("bug172419_2.cpp", 322, 12, "bug172419_2.cpp", 260, 5);
        performTest("bug172419_2.cpp", 325, 12, "bug172419_2.cpp", 260, 5);
        performTest("bug172419_2.cpp", 331, 12, "bug172419_2.cpp", 260, 5);
        performTest("bug172419_2.cpp", 337, 13, "bug172419_2.cpp", 260, 5);
        performTest("bug172419_2.cpp", 344, 13, "bug172419_2.cpp", 260, 5);
        performTest("bug172419_2.cpp", 352, 13, "bug172419_2.cpp", 260, 5);
        performTest("bug172419_2.cpp", 355, 13, "bug172419_2.cpp", 260, 5);
        performTest("bug172419_2.cpp", 358, 13, "bug172419_2.cpp", 260, 5);
        performTest("bug172419_2.cpp", 361, 13, "bug172419_2.cpp", 260, 5);
        performTest("bug172419_2.cpp", 364, 13, "bug172419_2.cpp", 260, 5);
        performTest("bug172419_2.cpp", 367, 13, "bug172419_2.cpp", 260, 5);
        performTest("bug172419_2.cpp", 370, 13, "bug172419_2.cpp", 260, 5);
    }
    
    public void testBug172419_4() throws Exception {
        // Bug 172419 - Boost metaprogramming usage problem
        performTest("bug172419_4.cpp", 151, 11, "bug172419_4.cpp", 139, 5);
    }    
    
    public void testBug172419_5() throws Exception {
        // Bug 172419 - Boost metaprogramming usage problem
        performTest("bug172419_5.cpp", 58, 15, "bug172419_5.cpp", 14, 9); // this fails if TemplateParameterImpl.hashCode returns constant
        performTest("bug172419_5.cpp", 61, 15, "bug172419_5.cpp", 14, 9); // this fails always
    }    
    
    public void testBug240929() throws Exception {
        // Bug 240929 - Unresolved return type of casts functions in LLVM 3.4
        performTest("bug240929.cpp", 190, 30, "bug240929.cpp", 178, 9); 
        performTest("bug240929.cpp", 193, 30, "bug240929.cpp", 178, 9); 
        performTest("bug240929.cpp", 196, 32, "bug240929.cpp", 178, 9); 
        performTest("bug240929.cpp", 199, 32, "bug240929.cpp", 178, 9);         
    }
    
    public void testBug246068() throws Exception {
        // Bug 246068 - Unable to resolve identifier for boost::signal.connect()
        performTest("bug246068.cpp", 216, 15, "bug246068.cpp", 4, 9); 
        performTest("bug246068.cpp", 218, 15, "bug246068.cpp", 8, 9); 
        performTest("bug246068.cpp", 220, 15, "bug246068.cpp", 4, 9); 
        performTest("bug246068.cpp", 222, 15, "bug246068.cpp", 8, 9);         
    }
}
