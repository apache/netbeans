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

package org.netbeans.modules.cnd.modelimpl.impl.services;

import java.io.File;
import java.util.Collection;
import java.util.Iterator;
import org.netbeans.junit.Manager;
import org.netbeans.modules.cnd.api.model.CsmFunction;
import org.netbeans.modules.cnd.api.model.CsmObject;
import org.netbeans.modules.cnd.api.model.CsmOffsetable;
import org.netbeans.modules.cnd.api.model.CsmProject;
import org.netbeans.modules.cnd.api.model.CsmTemplate;
import org.netbeans.modules.cnd.api.model.CsmVariable;
import org.netbeans.modules.cnd.api.model.services.CsmSymbolResolver;
import org.netbeans.modules.cnd.api.project.NativeProject;
import org.netbeans.modules.cnd.modelimpl.trace.FileModelCpp11Test;
import org.netbeans.modules.cnd.modelimpl.trace.FileModelCpp14Test;

/**
 *
 */
public class CsmSymbolResolverTestCase extends SelectTestBase {
    
    public CsmSymbolResolverTestCase(String name) {
        super(name);
    }

    @Override
    protected void setUp() throws Exception {
        System.setProperty("cnd.tests.cpp11directories", FileModelCpp11Test.class.getSimpleName()); // NOI18N
        System.setProperty("cnd.tests.cpp14directories", FileModelCpp14Test.class.getSimpleName()); // NOI18N
        super.setUp(); 
    }    
    
    @Override
    protected File getProjectRoot() {
        File dataDir = super.getDataDir();
        String fullClassName = getTestCaseDataClass().getName();
        String filePath = fullClassName.replace('.', File.separatorChar);
        return Manager.normalizeFile(new File(dataDir, filePath));        
    }

    @Override
    protected Iterator<CsmFunction> _getFunctions(CsmProject project, CsmFunction func) {
        String funText = func.getSignature().toString();
        funText = funText.replace(func.getName(), func.getQualifiedName());
        if (func instanceof CsmTemplate && ((CsmTemplate) func).isTemplate()) {
            funText = func.getReturnType().getCanonicalText() + " " + funText;
        }
        Collection<CsmOffsetable> result = CsmSymbolResolver.resolveSymbol((NativeProject) project.getPlatformProject(), funText);
        return ((Collection<CsmFunction>)(Object) result).iterator();
    }
    
    @Override
    protected Iterator<CsmVariable> _getVariables(CsmProject project, CsmVariable var) {
        String varText = var.getQualifiedName().toString();
        Collection<CsmOffsetable> result = CsmSymbolResolver.resolveSymbol((NativeProject) project.getPlatformProject(), varText);
        return ((Collection<CsmVariable>)(Object) result).iterator();
    }

    @Override
    protected boolean _checkFound(CsmObject obj, Iterator<? extends CsmObject> answer) {
        return answer.hasNext() && answer.next() == obj;
    }
    
    private void doTestSingle(String symbol, String fileName, int line, int column) {
        Collection<CsmOffsetable> result = CsmSymbolResolver.resolveSymbol(getProject(), symbol);
        assertFalse("Symbol '" + symbol + "' not found at " + fileName + ":" + line + ":" + column, result.isEmpty());
        CsmOffsetable obj = (CsmOffsetable) result.iterator().next();
        assertEquals(fileName, obj.getContainingFile().getName().toString());
        assertEquals(line, obj.getStartPosition().getLine());
        assertEquals(column, obj.getStartPosition().getColumn());
    }
    
    public void testSelectModelGetFunctions() throws Exception {
        doTestGetFunctions();
    }    
    
    public void testSelectModelGetMethods() throws Exception {
        doTestGetMethods();
    }       
    
    public void testSelectModelGetVariables() throws Exception {
        doTestGetVariables();
    }
    
    public void testSelectModelGetFields() throws Exception {
        doTestGetFields();
    }    
    
    public void testInstantiatedSignature() throws Exception {
        doTestSingle("double entity_resolver_test::boo<double>(double)", "simple_symbol_resolver_test.cpp", 43, 3);
    }
    
    public void testTemplatesSymbolResolverSymbols() throws Exception {
        doTestSingle("tpl_sr_test::AAA_sr_test<int>::T_var_1", "templates_symbol_resolver_test.cpp", 6, 7);
        doTestSingle("tpl_sr_test::ZZZ_sr_test::roo(tpl_sr_test::AAA_sr_test<int>::BBB__sr_test<int>)", "templates_symbol_resolver_test.cpp", 34, 7);
        doTestSingle("int tpl_sr_test::ZZZ_sr_test::zoo<int>(tpl_sr_test::AAA_sr_test<int>::BBB__sr_test<int>)", "templates_symbol_resolver_test.cpp", 36, 7);
        doTestSingle("tpl_sr_test::ZZZ_sr_test::var1", "templates_symbol_resolver_test.cpp", 31, 7);
        doTestSingle("tpl_sr_test::ZZZ_sr_test::var2", "templates_symbol_resolver_test.cpp", 32, 7);
        doTestSingle("tpl_sr_test::boo(tpl_sr_test::AAA_sr_test<int>)", "templates_symbol_resolver_test.cpp", 22, 3);
        doTestSingle("tpl_sr_test::boo(tpl_sr_test::AAA_sr_test<int> const&)", "templates_symbol_resolver_test.cpp", 26, 3);
        doTestSingle("double tpl_sr_test::foo<double>(double)", "templates_symbol_resolver_test.cpp", 15, 3);
        doTestSingle("int tpl_sr_test::foo<int>(int)", "templates_symbol_resolver_test.cpp", 15, 3);
    }         
    
    public void testSolarisSymbolResolverSymbols() throws Exception {
        doTestSingle("char*hello_solaris1()", "solaris_test_case.cpp", 1, 1);
        doTestSingle("char*solaris_test_case::hello_solaris2()", "solaris_test_case.cpp", 4, 3);
    }
}
