/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2013 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2013 Sun Microsystems, Inc.
 */
package org.netbeans.modules.cnd.modelimpl.trace;

/**
 *
 */
public class FileModelNewGrammarTest extends TraceModelTestBase {
    
    public FileModelNewGrammarTest(String testName) {
        super(testName);
    }

    @Override
    protected void setUp() throws Exception {
        System.setProperty("cnd.modelimpl.tracemodel.project.name", "DummyProject"); // NOI18N
        System.setProperty("parser.report.errors", "true");
        System.setProperty("antlr.exceptions.hideExpectedTokens", "true");
        System.setProperty("cnd.language.flavor.cpp11", "true");
        System.setProperty("cnd.modelimpl.cpp.parser.new.grammar", "true");
        System.setProperty("cnd.modelimpl.parse.headers.with.sources", "true");
        super.setUp();
    }

    @Override
    protected void postSetUp() {
        // init flags needed for file model tests
        getTraceModel().setDumpModel(true);
        getTraceModel().setDumpPPState(true);
    }

    @Override
    protected void postTest(String[] args, Object... params) throws Exception {
        System.setProperty("cnd.language.flavor.cpp11", "false"); 
    }
    
    public void testTypedefEnum() throws Exception {
        performTest("typedefEnum.cpp");
    }
    
    public void testDestructor() throws Exception {
        performTest("destructor.cpp");
    }
    
    public void testConstMethod() throws Exception {
        performTest("constMethod.cpp");
    }
    
    public void testFriendFunction() throws Exception {
        performTest("friendFunction.cpp");
    }
    
    public void testParameters() throws Exception {
        performTest("parameters.cpp");
    }
    
    public void testAccessModifiers() throws Exception {
        performTest("accessModifiers.cpp");
    }
    
    public void testClassForwardScope() throws Exception { 
        performTest("classForwardScope.cpp");
    }
    
    public void testConstructorImportSymbols() throws Exception {
        performTest("constructorImportSymbols.cpp");
    }
    
    public void testClearingImportedSymbols() throws Exception {
        performTest("clearingImportedSymbols.cpp");
    }    
    
    public void testMultipleDeclarativeRegions() throws Exception {
        performTest("multipleDeclarativeRegions.cpp");
    }
    
    public void testNestedClassScope() throws Exception {
        performTest("nestedClassScope.cpp");
    }    
    
    public void testConstructorInitializerListScope() throws Exception {
        performTest("constructorInitializerListScope.cpp");
    }    
    
    public void testBitFields() throws Exception {
        performTest("bitFields.cpp");
    }    

    public void testFunctionParametersInFunctionBody() throws Exception {
        performTest("functionParametersInFunctionBody.cpp");
    }    
    
//    public void testMultipleMemberDeclaration() throws Exception {
//        performTest("typedefEnum.cpp");
//    } 
    
}
