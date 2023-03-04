/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.netbeans.modules.groovy.editor.imports;

import java.util.HashSet;
import java.util.Set;
import org.netbeans.modules.groovy.editor.spi.completion.DefaultImportsProvider;
import org.netbeans.modules.groovy.editor.test.GroovyTestBase;
import org.openide.util.test.MockLookup;

/**
 *
 * @author Martin Janicek
 */
public class ImportUtilsTest extends GroovyTestBase {

    public ImportUtilsTest(String testName) {
        super(testName);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        MockLookup.setInstances(new TestImportProvider());
    }

    public void testIsDefaultlyImported_directTypes() {
        assertTrue(ImportUtils.isDefaultlyImported("java.math.BigDecimal"));
        assertTrue(ImportUtils.isDefaultlyImported("java.math.BigInteger"));
    }

    public void testIsDefaultlyImported_typesFromImportedPackages() {
        assertTrue(ImportUtils.isDefaultlyImported("java.io.File"));
        assertTrue(ImportUtils.isDefaultlyImported("java.lang.Integer"));
        assertTrue(ImportUtils.isDefaultlyImported("java.net.Socket"));
        assertTrue(ImportUtils.isDefaultlyImported("java.util.Arrays"));
        assertTrue(ImportUtils.isDefaultlyImported("groovy.util.GroovyTestCase"));
        assertTrue(ImportUtils.isDefaultlyImported("groovy.lang.Singleton"));
    }

    public void testIsDefaultlyImported_typesFromProvidedPackages() {
        assertTrue(ImportUtils.isDefaultlyImported("test.whatever.ImaginaryType"));
        assertTrue(ImportUtils.isDefaultlyImported("test.somethingelse.ImaginaryType"));
    }

    public void testIsDefaultlyImported_typesFromProvidedClasses() {
        assertTrue(ImportUtils.isDefaultlyImported("abc.efd.Oops"));
        assertTrue(ImportUtils.isDefaultlyImported("qwe.rty.Psst"));
    }
    
    public static class TestImportProvider implements DefaultImportsProvider {

        @Override
        public Set<String> getDefaultImportPackages() {
            Set<String> additionalImports = new HashSet<String>();
            additionalImports.add("test.whatever");
            additionalImports.add("test.somethingelse");
            
            return additionalImports;
        }

        @Override
        public Set<String> getDefaultImportClasses() {
            Set<String> additionalImports = new HashSet<String>();
            additionalImports.add("abc.efd.Oops");
            additionalImports.add("qwe.rty.Psst");
            
            return additionalImports;
        }
    }
}
