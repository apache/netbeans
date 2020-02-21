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

/**
 *
 */
public class Cpp11TooltipsTestCase extends TooltipsBaseTestCase {

    public Cpp11TooltipsTestCase(String testName) {
        super(testName);
    }
    
    @Override
    protected void setUp() throws Exception {
        System.setProperty("cnd.modelimpl.tracemodel.project.name", "DummyProject"); // NOI18N
        System.setProperty("parser.report.errors", "true");
        System.setProperty("antlr.exceptions.hideExpectedTokens", "true");
        System.setProperty("cnd.language.flavor.cpp11", "true");         
        super.setUp();
    }
    
    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
        System.setProperty("cnd.language.flavor.cpp11", "false");
    }

    public void testBug247751() throws Exception {
        // Bug #247751 - Provide tooltips and navigation for auto variables
        performPlainTooltipTest("bug247751.cpp", 75, 15,
            "Variable var\n" + 
            "bug247751::std247751::tuple247751<bug247751::std247751::make_tuple247751::Elements &>"
        );        
        performPlainTooltipTest("bug247751.cpp", 76, 15,
            "Variable elem0\n" + 
            "bug247751::AAA247751 &"
        );  
        performPlainTooltipTest("bug247751.cpp", 77, 15,
            "Variable elem1\n" + 
            "bug247751::BBB247751 &"
        );
        performPlainTooltipTest("bug247751.cpp", 78, 15,
            "Variable elem2\n" + 
            "bug247751::CCC247751 &"
        );
        performPlainTooltipTest("bug247751.cpp", 80, 15,
            "Variable mapElem\n" + 
            "int *"
        );
        performPlainTooltipTest("bug247751.cpp", 82, 15,
            "Variable stringVar\n" + 
            "bug247751::std247751::string247751"
        );        
    }    
    
    public void testBug250845() throws Exception {
        // Bug #250845 - Wrong deduced type shown for auto in some cases
        performPlainTooltipTest("bug250845.cpp", 4, 14,
            "Variable x\n" + 
            "int"
        );
        performPlainTooltipTest("bug250845.cpp", 5, 14,
            "Variable y\n" + 
            "const int *"
        );
        performPlainTooltipTest("bug250845.cpp", 7, 14,
            "Variable z\n" + 
            "const int *"
        );
        performPlainTooltipTest("bug250845.cpp", 8, 15,
            "Variable zz\n" + 
            "const int *"
        );
        performPlainTooltipTest("bug250845.cpp", 9, 21,
            "Variable zzz\n" + 
            "const int *"
        );
        performPlainTooltipTest("bug250845.cpp", 12, 19,
            "Variable elem\n" + 
            "int"
        );
        performPlainTooltipTest("bug250845.cpp", 17, 19,
            "Variable elem\n" + 
            "const int *"
        );
        performPlainTooltipTest("bug250845.cpp", 24, 20,
            "Variable elem\n" + 
            "int *&"
        );
        performPlainTooltipTest("bug250845.cpp", 29, 26,
            "Variable elem\n" + 
            "const int &"
        );
    }
    
    public void testBug261006() throws Exception {
        // Bug 261006 - "using namespace std" breaks "introduce variable" in simple scenario
        performPlainTooltipTest("bug261006.cpp", 12, 15,
            "Variable var1\n" + 
            "int"
        );
        performPlainTooltipTest("bug261006.cpp", 13, 15,
            "Variable var2\n" + 
            "bug261006::AAA261006"
        );
    }
    
    public void testBug261517() throws Exception {
        // Bug 261517 - Bad implicit type conversion
        performPlainTooltipTest("bug261517.cpp", 3, 16,
            "Variable var1\n" + 
            "int"
        );
        performPlainTooltipTest("bug261517.cpp", 4, 16,
            "Variable var2\n" + 
            "unsigned long long"
        );
        performPlainTooltipTest("bug261517.cpp", 5, 16,
            "Variable var3\n" + 
            "long long"
        );
        performPlainTooltipTest("bug261517.cpp", 6, 16,
            "Variable var4\n" + 
            "int"
        );
    }
    
    public void testBug257827() throws Exception {
        // Bug 257827 - Auto insert function stubs not copying all const keywords
        performPlainTooltipTest("bug257827.cpp", 8, 48,
            "Variable var1\n" + 
            "AAA257827<const bug257827::BBB257827> var1"
        );
        performPlainTooltipTest("bug257827.cpp", 9, 42,
            "Variable var2\n" + 
            "AAA257827<volatile BBB257827*> var2"
        );
        performPlainTooltipTest("bug257827.cpp", 10, 46,
            "Variable var3\n" + 
            "AAA257827<const volatile BBB257827> var3"
        );
    }
}
