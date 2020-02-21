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

package org.netbeans.modules.cnd.highlight.error;

import java.io.File;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import org.netbeans.modules.cnd.api.model.CsmFile;
import org.netbeans.modules.cnd.modelimpl.csm.core.ModelImplTest;
import org.netbeans.modules.cnd.modelimpl.trace.TraceModelFileFilter;

/**
 * Test for IdentifierErrorProvider.
 *
 */
public class UnresolvedIdentifierTest extends ErrorHighlightingBaseTestCase {

    static {
        //System.setProperty("cnd.identifier.error.provider", "true");
    }

    public UnresolvedIdentifierTest(String testName) {
        super(testName);
    }
    
    @Override
    protected TraceModelFileFilter getTraceModelFileFilter() {
        String simpleName = SimpleFileFilter.testNameToFileName(getName());
        switch (simpleName) {
            case "IZ162745":
                return new SimpleFileFilter("unnamed_enum_typedef"); 
            case "TemplateParameterTypes":
                return new SimpleFileFilter("templates"); 
            case "TemplateParameterAncestor":
                return new SimpleFileFilter("template_parameter_ancestor"); 
            case "TypedefTemplate":
                return new SimpleFileFilter("typedef_templ"); 
            case "ForwardClassDecl":
                return new SimpleFileFilter("forward_class_decl"); 
            case "SkipSomeInstructionsBlock":
                return new SimpleFileFilter("skipBlocks"); 
            default:
                return new SimpleFileFilter(simpleName); 
        }
    }

    public void test234573() throws Exception {
        // #234573 - unresolved usage of static namespace functions
        performStaticTest("namespace234573.cpp");
    }
    
    public void test218303() throws Exception {
        // #218303 - Unresolved identifiers in preprocessor directives with alternative tokens
        performStaticTest("iz218303.cpp");
    }

    public void test212841() throws Exception {
        // #212841 - C++11 strongly typed enum incorrectly handled by code assistance
        performStaticTest("iz212841.cpp");
    }

    public void test191610() throws Exception {
        // #191610 unresolved reference to class declared with outer scope 
        performStaticTest("iz191610.cc");
    }

    public void test191515() throws Exception {
        // 191515: incorrect scope resolving for typedefs
        performStaticTest("iz191515.cpp");
    }
    
    public void testIZ191336() throws Exception {
        // 191336: Regression in test results (Oracle Solaris Studio projects)
        performStaticTest("iz191336.cpp");
    }
    
    public void testArrow_Deref_Of_This() throws Exception {
        performStaticTest("arrow_deref_of_this.cpp");
    }

    public void testIZ162745() throws Exception {
        // IZ#162745:unnamed_enum_typedef.cpp
        performStaticTest("unnamed_enum_typedef.cpp");
    }
    
    public void testDDD() throws Exception {
        // test for number of DDD problems
        performStaticTest("ddd_errors.cpp");
    }

    public void testIZ145280() throws Exception {
        // IZ#145280: IDE highlights code with '__attribute__((unused))' as wrong
        performStaticTest("iz145280.cc");
    }
    
    public void testSimple() throws Exception {
        performStaticTest("simple.cpp");
    }

    public void testTemplateParameterTypes() throws Exception {
        performStaticTest("templates.cpp");
    }

    public void testMacros() throws Exception {
        performStaticTest("macros.cpp");
    }

    public void testAttributes() throws Exception {
        performStaticTest("attributes.cpp");
    }

    public void testTypedefTemplate() throws Exception {
        performStaticTest("typedef_templ.cpp");
    }

    public void testSkipSomeInstructionsBlock() throws Exception {
        performStaticTest("skipBlocks.cpp");
    }

    public void testIZ144537() throws Exception {
        performStaticTest("iz144537.cpp");
    }

    public void testForwardClassDecl() throws Exception {
        performStaticTest("forward_class_decl.cpp");
    }
    
    public void testTemplateParameterAncestor() throws Exception {
        performStaticTest("template_parameter_ancestor.cpp");
    }
    
    public void testIZ_144873() throws Exception {
        performStaticTest("iz_144873.cpp");
    }

    public void testIZ_145118() throws Exception {
        performStaticTest("iz_145118.cpp");
    }

    public void testIZ155112() throws Exception {
        performStaticTest("iz155112.cpp");
    }

    public void testIZ158216() throws Exception {
        // IZ#158216 : Unresolved ids in compiler extensions
        performStaticTest("iz158216.cpp");
    }

    public void testIZ158730() throws Exception {
        // IZ#158730 : False positive error highlighting on nested types in templates
        performStaticTest("iz158730.cpp");
    }

    public void testIZ158831() throws Exception {
        // IZ#158831 : False positive error highlighting errors on typedefs in local methods of templates
        performStaticTest("iz158831.cpp");
    }

    public void testIZ158873() throws Exception {
        Level oldLevel = Logger.getLogger("cnd.logger").getLevel();
        Logger.getLogger("cnd.logger").setLevel(Level.SEVERE);
        // IZ#158873 : recursion in Instantiation.Type.isInstantiation()
        performStaticTest("iz158873.cpp");
        Logger.getLogger("cnd.logger").setLevel(oldLevel);
    }

    public void testIZ159615() throws Exception {
        Level oldLevel = Logger.getLogger("cnd.logger").getLevel();
        Logger.getLogger("cnd.logger").setLevel(Level.SEVERE);
        // IZ#159615 : recursion in CsmCompletionQuery.getClassifier()
        performStaticTest("iz159615.cpp");
        Logger.getLogger("cnd.logger").setLevel(oldLevel);
    }

    public void testIZ143044() throws Exception {
        // IZ#143044 : Wrong overloaded method is not highlighted as error
        performStaticTest("iz143044.cpp");
    }

    public void testIZ151909() throws Exception {
        // IZ#151909 : Template friend classes (parser problem)
        performStaticTest("iz151909.cpp");
    }

    public void testIZ148236() throws Exception {
        // IZ#148236 : IDE highlights some operator's definitions as wrong code
        performStaticTest("iz148236.cpp");
    }

    public void testIZ155459() throws Exception {
        // IZ#155459 : unresolved forward template declaration
        performStaticTest("iz155459.cpp");
    }

    public void testIZ160542() throws Exception {
        Handler h = new Handler() {
            @Override
            public void publish(LogRecord record) {
                assert(false);
            }
            @Override
            public void flush() {
            }
            @Override
            public void close() throws SecurityException {
            }
        };
        Logger.getLogger("cnd.logger").addHandler(h);
        // IZ#160542 : Assertions on template instantiations
        performStaticTest("iz160542.cpp");
        Logger.getLogger("cnd.logger").removeHandler(h);
    }

    public void testIZ151054() throws Exception {
        // IZ#151054 : False recognition of operator ->
        performStaticTest("iz151054.cpp");
    }

    public void testIZ150827() throws Exception {
        // IZ#150827 : Expression statement with & is treated as a declaration
        performStaticTest("iz150827.cpp");
    }

    public void testIZ142674() throws Exception {
        // IZ#142674 : Function-try-catch (C++) in editor shows error
        performStaticTest("iz142674.cpp");
    }

    public void testIZ149285() throws Exception {
        // IZ#149285 : A problem with function bodies in different condition branches in headers
        performStaticTest("iz149285.cpp");
        performStaticTest("iz149285.h");
    }

    public void testIZ158280() throws Exception {
        // IZ#158280 : False positive error highlighting on templates in case of macro usage
        performStaticTest("iz158280.cpp");
    }

    public void testIZ163135() throws Exception {
        // IZ#163135 : False positive error highlighting on templates with template parameter as parent
        performStaticTest("iz163135.cpp");
    }

    public void testIZ155054() throws Exception {
        // IZ#155054 : False positive error highlighting errors on template specializations
        performStaticTest("iz155054.cpp");
    }
    
    public void testIZ172227() throws Exception {
        // IZ#172227 : Unable to resolve identifier path although code compiles allright
        performStaticTest("iz172227.cpp");
    }

    public void testIZ171453() throws Exception {
        // IZ#171453 : Private Inheretence: Using Directive
        performStaticTest("iz171453.cpp");
    }

    public void testIZ175782() throws Exception {
        // IZ#175782 : False positive used prior to declaration warnings
        performStaticTest("iz175782.cpp");
    }

    public void testIZ175782_2() throws Exception {
        // IZ#175782 : False positive used prior to declaration warnings
        performStaticTest("iz175782_2.cpp");
    }

    public void testBug186638() throws Exception {
        // Bug 186638 - Wrong error highlighting for template based object in case cast
        performStaticTest("bug186638.cpp");
    }

    public void testBug201258() throws Exception {
        // Bug 201258 - Forward declarations not resolved
        performStaticTest("bug201258.cpp");
    }


    public void test210983() throws Exception {
        // 210983 - regression in inaccuracy tests (dbx projectl): forward declarations
        File sourceFile = getDataFile("bug210983_1.cpp");
        CsmFile csmFile = this.getCsmFile(sourceFile);
        assertNotNull(csmFile);
        // open file with duplicated struct
        performStaticTest("bug210983_1.cpp");
        // modify file and parse it
        ModelImplTest.fireFileChanged(csmFile);
        csmFile.scheduleParsing(true);
        // open other file where same named, but different structure is used
        performStaticTest("bug210983_2.cpp"); // there was unresolved 'savefd'
        // check other files just to be sure
        performStaticTest("bug210983_1.cpp");
        performStaticTest("inc210983_1.h");
        performStaticTest("inc210983_2.h");
    }

    public void test211143() throws Exception {
        // 211143 - regression in inaccuracy tests (vlc projectl)
        File sourceFile = getDataFile("bug211143_1.cpp");
        CsmFile csmFile = this.getCsmFile(sourceFile);
        assertNotNull(csmFile);
        // open file with duplicated struct
        performStaticTest("bug211143_1.cpp");
        // modify file and parse it
        ModelImplTest.fireFileChanged(csmFile);
        csmFile.scheduleParsing(true);
        // open other file where same named, but different structure is used
        performStaticTest("bug211143_2.cpp"); // there was unresolved 'savefd'
        // check other files just to be sure
        performStaticTest("bug211143_1.cpp");
        performStaticTest("inc211143.h");
    }

    
    public void testBug212905() throws Exception {
        Level oldLevel = Logger.getLogger("cnd.logger").getLevel();
        Logger.getLogger("cnd.logger").setLevel(Level.SEVERE);
        // Bug 212905 - StackOverflowError at java.util.WeakHashMap.eq
        performStaticTest("bug212905.cpp");
        Logger.getLogger("cnd.logger").setLevel(oldLevel);
    }
    
    public void testBug218192() throws Exception {
        // Bug 218192 - Exception: attempt to put local declaration FUNCTION_FRIEND_DEFINITION
        performStaticTest("bug218192.cpp");
    }      

    public void testBug215225() throws Exception {
        // Bug 215225 - Infinite loop in TemplateUtils.checkTemplateType
        performStaticTest("bug215225.cpp");
    }      

    public void testBug215225_2() throws Exception {
        // Bug 215225 - Infinite loop in TemplateUtils.checkTemplateType
        performStaticTest("bug215225_2.cpp");
    }      

    public void testBug218759() throws Exception {
        // Bug 218759 - inaccuracy tests: a lot of parser errors in some files (a order of declaration specifiers)
        performStaticTest("bug218759.cpp");
    }      

    public void testBug217798() throws Exception {
        // Bug 217798 - "unexpected token: static" in valid C code (no explicit type given)
        performStaticTest("bug217798.cpp");
    }      
    
    public void testBug223298() throws Exception {
        // Bug 223298 - Wrong recognition of function
        performStaticTest("bug223298.cpp");
    }            

    public void testBug222883() throws Exception {
        // Bug 222883 - goto labes ("Unable to resolve identifier G_1")
        performStaticTest("bug222883.cpp");
    }
    
    public void testBug227266() throws Exception {
        // Bug 227266 - incorrect error HL
        performStaticTest("bug227266.cpp");
    }
    
    public void testIZ161565() throws Exception {
        // IZ#161565 : Usage of not-yet-declared function is not highlighted as error
        performStaticTest("iz161565.cpp");
    }

    public void testIZ175231() throws Exception {
        // IZ#175231 : Error parser doesn't handle linenumber:column correctly
        performStaticTest("iz175231.cpp");
    }
    
    public void testBug248749() throws Exception {
        // Bug 248749 - Template based identifier is marked as red instead of yellow
        performStaticTest("bug248749.cpp");
    }
    
    public void testBug257030() throws Exception {
        // Bug 257030 - Unresolved identifiers in template if variable has 'auto' type
        performStaticTest("bug257030.cpp");
    }

    /////////////////////////////////////////////////////////////////////
    // FAILS

    public static class Failed extends ErrorHighlightingBaseTestCase {

        public Failed(String testName) {
            super(testName);
        }

        @Override
        protected Class<?> getTestCaseDataClass() {
            return UnresolvedIdentifierTest.class;
        }

    }
}
