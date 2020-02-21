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

package org.netbeans.modules.cnd.modelimpl.trace;

import java.io.File;
import java.io.FileWriter;
import java.util.Collection;
import org.netbeans.modules.cnd.apt.debug.APTTraceFlags;
import org.netbeans.modules.cnd.apt.support.APTMacroCallback;
import org.netbeans.modules.cnd.apt.support.api.PreprocHandler;
import org.netbeans.modules.cnd.modelimpl.csm.core.FileImpl;

/**
 * Just a continuation of the FileModelTest
 * (which became too large)
 */
public class FileModel2Test extends TraceModelTestBase {

    public FileModel2Test(String testName) {
        super(testName);
    }

    @Override
    protected void setUp() throws Exception {
        System.setProperty("cnd.modelimpl.tracemodel.project.name", "DummyProject"); // NOI18N
        System.setProperty("parser.report.errors", "true");
        System.setProperty("antlr.exceptions.hideExpectedTokens", "true");
//        System.setProperty("apt.use.clank", "true");
//        System.setProperty("cnd.modelimpl.parser.threads", "1");
        super.setUp();
    }

    @Override
    protected void postSetUp() {
        // init flags needed for file model tests
        getTraceModel().setDumpModel(true);
        getTraceModel().setDumpPPState(true);
    }

    public void test215640() throws Exception {
        // #215640 - Code model parser is failed if preprocessor directive is started after comment
        performTest("iz215640.cpp"); // NOI18N
    }
    
    public void test229003() throws Exception {
        // #229003 - inaccuracy tests: Perl project has unresolved identifiers
        performTest("iz229003.cpp"); // NOI18N
    }
    
    public void test231216() throws Exception {
        // #231216 - IDE doesn't recognize operator~ 
        performTest("iz231216.cc"); // NOI18N
    }
    
    public void testPreprocDirectives() throws Exception {
        // #218190 - NPE in APTPredefinedMacroMap
        performTest("preproc_directives.cc"); // NOI18N
    }

    public void testPreprocDirectives2() throws Exception {
        // #218344 - any #warning breaks parser
        // #218308 - Empty preprocessor directives not supported
        performTest("sharp_pp_directives.cc"); // NOI18N
    }

    public void test222800() throws Exception {
        // #222800 - Unexpected token "{" in declaration with __attribute__ ((packed)) 
        performTest("iz222800.cc");
    }
    
    public void test217089() throws Exception {
        // #217089: "#define AAA 1 ## 0" line breaks parser
        performTest("iz217089.c");
    }
    
    public void test217711() throws Exception {
        // #217711: complain about recursive include while #pragma once guarding the file
        performTest("iz217711.h");
    }

    public void test205270() throws Exception {
        // #205270 - R-value references breaks code model parser
        performTest("iz205270.cc");
    }
    
    public void test199899() throws Exception {
        // #199899 - Parser fails on Solaris Studio specific keywords        
        performTest("iz199899.c");
    }
    
    public void test197997() throws Exception {
        // in clank mode err file has twice reported warning starting from Clank 3.9:
        // - during top level parse 
        // - and when restore lazy body
        // #197997 - Macro interpreter does not support macro evaluation if expression has in expansion 'defined' operator  
        performTest("iz197997.cc");
    }
    
    protected TraceModelFileFilter getProjectFileFilter() {
        if (true) return super.getProjectFileFilter();
        return new TraceModelFileFilter() {
            @Override
            public boolean isProjectFile(String filename) {
                return filename.endsWith("iz191446.cc");
            }
        };
    }
    
    public void test191446() throws Exception {
        // #191446 - no code assistance for elementes #include'ed in namespace body
        performTest("iz191446.cc");
    }
    
    public void test191799() throws Exception {
        // #191799: testSelectModelGetFunctions is failing
        performTest("iz191799.cc");
    }
    
    public void test191598() throws Exception {
        // #191598 -  parser errors in /usr/include/tr1_impl/type_traits
        performTest("iz191598.cc");
    }
    
    public void testIZ191085() throws Exception {
        // #191085:Parser fails in sy_defined_(node) macro
        performTest("iz191085.cc");
    }
    
    public void testIZ190821() throws Exception {
        // #190413:  wrong message SEVERE [org.netbeans.modules.cnd.apt]: # is not followed by a macro parameter
        performTest("iz190821.cc");
    }
    
    public void testIZ190782() throws Exception {
        // #190413:  enum based variables are not resolved (top issue in driver)
        performTest("iz190782.c");
    }
    
    public void testIZ190413() throws Exception {
        // #190413:  preprocessor incorrectly evaluate char-based expression
        performTest("iz190413.c");
    }
    
    public void testIZ189777() throws Exception {
        // IZ#189777:  unresolved enum with bits info 
        performTest("iz189777.c");
    }
    
    public void testIZ164583() throws Exception {
        // IZ#164583: Inaccuracy tests: unstable results in MySQL
        performTest("iz164583.cpp");
    }

    public void testIZ149525() throws Exception {
        // IZ#149525: can't process lazy body of macro expanded function
        performTest("iz149525.cc");
    }
    
    public void testIZ162280() throws Exception {
        // IZ#162280: Inaccuracy tests: regression in Boost and Vlc
        performTest("iz162280_friend_fwd_cls.cpp");
    }
    
    public void testIZ156061() throws Exception {
        // IZ156061: structure typedefs are highlighted as error
        performTest("iz156061.cc");
    }
    
    public void testIZ154276() throws Exception {
        // IZ154276: functions are creted instead of fields
        performTest("iz154276.cc");
    }

    public void testIZ154196() throws Exception {
        // IZ154196: Regression in LiteSQL (Error Highlighting)
        performTest("iz154196.cc");
    }

    public void testIZ136887() throws Exception {
        // IZ136887: Model do not support bit fields
        performTest("iz136887.cc");
    }

    public void testIZ149505() throws Exception {
        // IZ#149505: special handling of __VA_ARGS__ with preceding comma
        performTest("iz149505.cc");
    }

    public void testIZ195560() throws Exception {
        // IZ#195560: more support for variadic variables in macro
        performTest("iz195560.cc");
    }
    
    public void testIZ145280() throws Exception {
        // IZ#145280: IDE highlights code with '__attribute__((unused))' as wrong
        performTest("iz145280.cc");
    }

    public void testIZ143977_0() throws Exception {
        // IZ#143977: Impl::Parm1 in Factory.h in Loki is unresolved
        performTest("iz143977_0.cc");
    }

    public void testIZ143977_1() throws Exception {
        // IZ#143977: Impl::Parm1 in Factory.h in Loki is unresolved
        performTest("iz143977_1.cc");
    }

    public void testIZ143977_2() throws Exception {
        // IZ#143977: Impl::Parm1 in Factory.h in Loki is unresolved
        performTest("iz143977_2.cc");
    }

    public void testIZ143977_3() throws Exception {
        // IZ#143977: Impl::Parm1 in Factory.h in Loki is unresolved
        performTest("iz143977_3.cc");
    }

    public void testIZ103462_1() throws Exception {
        // IZ#103462: Errors in template typedef processing:   'first' and 'second' are missed in Code Completion listbox
        performTest("iz103462_first_and_second_1.cc");
    }

    public void testHeaderWithCKeywords() throws Exception {
        // IZ#144403: restrict keywords are flagged as ERRORs in C header files
        performTest("testHeaderWithCKeywords.c");
    }

    public void testNamesakes() throws Exception {
        // IZ#145553 Class in the same namespace should have priority over a global one
        performTest("iz_145553_namesakes.cc");
    }

    public void testIZ146560() throws Exception {
        // IZ#146560 Internal C++ compiler does not accept 'struct' after 'new'
        performTest("iz146560.cc");
    }

    public void testIZ147284isDefined() throws Exception {
        if (APTTraceFlags.USE_CLANK) {
          // this is the test for non-clank mode only
          return;
        }
        // IZ#147284 APTMacroCallback.isDefined(CharSequence) ignores #undef
        String base = "iz147284_is_defined";
        performTest(base + ".cc");
        FileImpl fileImpl = findFile(base + ".h");
        assertNotNull(fileImpl);
        Collection<PreprocHandler> handlers = fileImpl.getFileContainerOwnPreprocHandlersToDump();
        assertEquals(handlers.size(), 1);
        String macro = "MAC";
        assertFalse(macro + " should be undefined!", ((APTMacroCallback)handlers.iterator().next().getMacroMap()).isDefined(macro));
    }

    public void testIZ147574() throws Exception {
        // IZ#147574 Parser cann't recognize code in yy.tab.c file correctly
        performTest("iz147574.c");
    }

    public void testIZ148014() throws Exception {
        // IZ#148014 Unable to resolve pure virtual method that throws
        performTest("iz148014.cc");
    }

    public void testIZ149225() throws Exception {
        // IZ#149225 incorrect concatenation with token that starts with digit
        performTest("iz149225.c");
    }

    public void testIZ151621() throws Exception {
        // IZ#151621 no support for __thread keyword
        performTest("iz151621.c");
    }

    public void testInitializerInExpression() throws Exception {
        // IZ#152872: parser error in VLC on cast expression
        performTest("iz152872_initializer_in_expression.c");
    }

    public void testNamespaceAlias() throws Exception {
        // IZ#151957: 9 parser's errors in boost 1.36
        performTest("iz151957_namespace_alias.cc");
    }

    public void testIZ154349() throws Exception {
        // IZ#154349: wrongly flagged errors for destructor during template specialization
        performTest("iz154349.cc");
    }

    public void testIZ157603() throws Exception {
        // IZ#157603 : Code model does not understand __attribute, constructor, destructor keywords (GNU)
        performTest("iz157603.cc");
    }

    public void testIZ157836() throws Exception {
        // IZ#157836 : parser incorrectly handles expression in else without {}
        performTest("iz157836.cc");
    }

    public void testIZ156004() throws Exception {
        // IZ#156004 : Unexpected token = in variable declaration
        performTest("iz156004.cc");
    }

    public void testIZ159324() throws Exception {
        // IZ#159324 : Unresolved variable definition
        performTest("iz159324.cc");
    }

    public void testIZ158872() throws Exception {
        // IZ#158872 : inline keyword break code model for template definition
        performTest("iz158872.cc");
    }

    public void testIZ159238() throws Exception {
        // IZ#159238 : parser fails on attribute after friend
        performTest("iz159238.cc");
    }

    public void testIZ158124() throws Exception {
        // IZ#158124 : parser breaks on (( ))
        performTest("iz158124.cc");
    }

    public void testIZ156009() throws Exception {
        // IZ#156009 : parser fails on declaration with __attribute__
        performTest("iz156009.cc");
    }

    public void testIZ158615() throws Exception {
        // IZ#158615 : Intervals are unresolved
        performTest("iz158615.cc");
    }

    public void testIZ158684() throws Exception {
        // IZ#158684 : Invalid syntax error
        performTest("iz158684.cc");
    }

    public void testIZ134182() throws Exception {
        // IZ#134182 : missed const in function parameter
        performTest("iz134182.cc");
    }

    public void testIZ156696() throws Exception {
        // IZ#156696 : model miss extern property if declaration statement has two objects
        performTest("iz156696.cc");
    }

    public void testIZ142674() throws Exception {
        // IZ#142674 : Function-try-catch (C++) in editor shows error
        performTest("iz142674.cc");
    }

    public void testIZ165038() throws Exception {
        // IZ#165038 : parser fail on variable declaration
        performTest("iz165038.cc");
    }

    public void testIZ167547() throws Exception {
        // IZ#167547 : 100% CPU core usage with C++ project
        performTest("iz167547.cc");
    }
    
    public void testIZ166165() throws Exception {
        // IZ#166165 : Unresolved extern enum declaration
        performTest("iz166165.cc");
    }

    public void testIZ174256() throws Exception {
        // IZ#174256 : parser cant understand _Pragma operator
        performTest("iz174256.cc");
    }

    public void testIZ175324() throws Exception {
        // IZ#175324 : Bad code parsing
        performTest("iz175324.cc");
    }

    public void testIZ168253() throws Exception {
        // IZ#168253 : Unable to resolve identifier for some header files
        performTest("iz168253.cc");
    }

    public void testIZ175653() throws Exception {
        // IZ#175653 : Support for binary constants
        performTest("iz175653.cc");
    }

    public void testIZ176530() throws Exception {
        // IZ#176530 : Unresolved function parameters in function parameters
        performTest("iz176530.cc");
    }

    public void testIZ182510() throws Exception {
        // IZ#182510 : C comment block causes syntax coloring to lose sync

        File file = getDataFile("iz182510.cc");
        FileWriter writer = new FileWriter(file);
        try {
            // \r's are essential for this test, so write the test file here
            writer.write("//\\\r\n#define FOO 1\r\n#define BAR 2\r\n");
        } finally {
            writer.close();
        }

        // Test that trailing \\\r\n in line comment is interpreted as
        // single escaped newline, i.e. #define FOO is in comment.
        // Also test offsets of BAR definition.
        performTest("iz182510.cc");
    }

    public void testIZ190710() throws Exception {
        //  Bug 190710 - UI freeze due to function body parsing in EDT
        performTest("iz190710.cc");
    }

    public void testExplicitSpecialization() throws Exception {
        //  improving specialisations - skip explicit template function specialisation
        performTest("explicit_specialization.cc");
    }
    
    public void testBug195338() throws Exception {
        // Bug 195338 - Unnamed bit sets issue
        performTest("bug195338.cpp");
    }
    
    public void testBug227479() throws Exception {
        // Bug 227479 - SQL EXEC support is broken
        performTest("iz227479.pc");
    }

    public void testBug198460() throws Exception {
        // Bug 198460 - add support for gcc keyword __extension__
        performTest("bug198460.cpp");
    }
    
    public void testBug204497() throws Exception {
        // Bug 204497 - Inaccuracy tests: latest configured dbx project has parser error
        performTest("bug204497.cpp");
    }
    
    public void testBug205292() throws Exception {
        // Bug 205292 - parserhg status fails on declspec construction
        performTest("bug205292.cpp");
    }
    
    public void testBug209947() throws Exception {
        // Bug 209947 - parser fails if namespace and class has the same name
        performTest("bug209947.cpp");
    }

    public void testBug215605() throws Exception {
        // Bug 215605 - Parsing project never finishes
        performTest("bug215605.cpp");
    }

    public void testBug217390() throws Exception {
        // Bug 217390 - Code model is unstable on unknown specifiers
        performTest("bug217390.cpp");
    }
    
    public void testBug229069() throws Exception {
        performTest("bug229069.c"); // NOI18N
    }    
    
    public void testBug224027() throws Exception {
        // Bug 224027 - 'friend' is unexpected token
        performTest("bug224027.cpp"); // NOI18N
    }    
    
    public void testCNamespaces() throws Exception {
        // Error with existing namespaces in C language
        performTest("c_namespaces.c");
    }
    
    public void testBug238041() throws Exception {
        // Bug 238041 - Function inside a function (in C) breaks parser
        performTest("bug238041.c");
    }
    
    public void testBug243560() throws Exception {
        // Bug 243560 - inaccuracy tests (clang): explicit instantiation
        performTest("bug243560.cpp");
    }
    
    public void testBug243508() throws Exception {
        // Bug 243508 - inaccuracy tests (clang): 'using typename' in namespace
        performTest("bug243508.cpp");        
    }
    
    public void testFunctionReturningEnum() throws Exception {
        performTest("function_returning_enum.cpp");
    }
    
    public void testBug243262() throws Exception {
        // Bug 243262 - regression: template specializations parsed incorrectly 
        performTest("bug243262.cpp");
    }    
    
    public void testBug245802() throws Exception {
        // Bug 245802 - inaccuracy tests(regression): operator< breaks parser
        performTest("bug245802.cpp");
    }
    
    public void testBug254698() throws Exception {
        // Bug 254698 - java.io.UTFDataFormatException: encoded string too long: 169505 bytes
        performTest("bug254698.cpp");
    }
    
    public void testBug246693() throws Exception {
        // Bug 246693 - c99 initializer, unexpected token: == 
        performTest("bug246693.cpp");
    }
    
    public void testBug256281() throws Exception {
        // Bug 256281 - parser error in builtins.h
        performTest("bug256281.cpp");
    }
    
    public void testBug248661_2() throws Exception {
        // Bug 248661 -  Variable with initializer is parsed as function
        performTest("bug248661_2.cpp");
    }
    
    public void testBug257152() throws Exception {
        // Bug 257152 - was [Bug 256791 - failing SelectModelTestCase.testSelectModelGetFunctions on Ubuntu test machine]
        performTest("bug257152.cpp");
    }
    
    public void testBug257152c() throws Exception {
        // Bug 257152 - was [Bug 256791 - failing SelectModelTestCase.testSelectModelGetFunctions on Ubuntu test machine]
        performTest("bug257152c.c");
    }
    
    public void testBug260774() throws Exception {
        // Bug 260774 - Errors on anonymous class
        performTest("bug260774.cpp");
    }
    
    public void testBug256516() throws Exception {
        // Bug 256516 - StackOverflowError at org.netbeans.modules.cnd.repository.RepositoryImpl.get
        performTest("bug256516.cpp");
    }
    
    public void testBug256516c() throws Exception {
        // Bug 256516 - StackOverflowError at org.netbeans.modules.cnd.repository.RepositoryImpl.get
        performTest("bug256516.c");
    }
    
    public void testBug258327() throws Exception {
        // Bug 258327 - Unresolved _Atomic in C11 when used with parens
        performTest("bug258327.c");
    }
    
    public void testBug255724() throws Exception {
        // Bug 255724 - inaccuracy tests: regression in DDD
        performTest("bug255724.c");
    }
    
    public void testBug267668Enums() throws Exception {
        // Bug 267668 - Accuracy regression in LLVM since Aug 19.
        performTest("bug267668Enums.cpp");
    }
    
    public void testBug269245() throws Exception {
        // Bug 269245 - Static array indices in parameter declarations not recognized
        performTest("bug269245.c");
    }
    
    public void testBug268930() throws Exception {
        // Bug 268930 - C++11: user-defined literals
        performTest("bug268930.c");
    }
}
