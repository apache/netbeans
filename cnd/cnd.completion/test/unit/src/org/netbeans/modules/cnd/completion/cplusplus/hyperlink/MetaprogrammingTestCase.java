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
