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
package org.netbeans.modules.java.hints.errors;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import org.netbeans.modules.java.hints.errors.ImportClass.FixImport;
import org.netbeans.modules.java.hints.infrastructure.HintsTestBase;
import org.netbeans.spi.editor.hints.Fix;

/**
 *
 * @author Jan Lahoda
 */
public class ImportClassTest extends HintsTestBase {
    
    /** Creates a new instance of ImportClassEnablerTest */
    public ImportClassTest(String name) {
        super(name);
    }

    public void testImportHint() throws Exception {
        performTest("ImportTest", "java.util.List", 22, 13);
    }

    public void testImportHint2() throws Exception {
        performTest("ImportTest2", "java.util.List", 18, 13);
    }
    
    public void testImportHint3() throws Exception {
        performTest("ImportTest3", "java.util.ArrayList", 9, 13);
    }
    
    public void testImportHint4() throws Exception {
        performTest("ImportTest4", "java.util.Collections", 7, 13);
    }
    
    public void testImportHint5() throws Exception {
        performTest("ImportTest5", "java.util.Map", 7, 13);
    }
    
    public void testImportHint6() throws Exception {
        performTest("ImportTest6", "java.util.Collections", 7, 13);
    }
    
    public void testImportHintDoNotPropose1() throws Exception {
        performTestDoNotPerform("ImportHintDoNotPropose", 10, 24);
    }

    public void testImportHintDoNotPropose2() throws Exception {
        performTestDoNotPerform("ImportHintDoNotPropose", 11, 24);
    }

    public void testImportHint118714() throws Exception {
        performTestDoNotPerform("ImportTest118714", 8, 11);
    }

    public void testImportHint86932() throws Exception {
        performTestDoNotPerform("ImportTest86932", 6, 25);
    }

    public void testImportHint194018a() throws Exception {
        performTest("ImportInImport", "java.util.Map", 3, 8);
    }

    public void testImportHint194018b() throws Exception {
        performTest("ImportInImport", "java.util.Map", 4, 8);
    }

    public void testImportHint194018c() throws Exception {
        performTest("ImportInImport", "java.util.Collections", 5, 8);
    }

    public void testImportHint194018d() throws Exception {
        performTest("ImportInImport", "java.util.Collections", 6, 8);
    }
    
    @Override
    protected String testDataExtension() {
        return "org/netbeans/test/java/hints/ImportClassEnablerTest/";
    }
    
    @Override
    protected String layer() {
        return "org/netbeans/modules/java/hints/errors/only-imports-layer.xml";
    }

    private static final Set<String> IGNORED_IMPORTS = new HashSet<String>(Arrays.asList("com.sun.tools.javac.util.List", "com.sun.xml.internal.bind.v2.schemagen.xmlschema.List", "com.sun.xml.internal.ws.policy.privateutil.PolicyUtils.Collections"));
    @Override
    protected boolean includeFix(Fix f) {
        if (!(f instanceof FixImport)) {
            return true;
        }

        for (String ignore : IGNORED_IMPORTS) {
            if (f.getText().contains(ignore)) {
                return false;
            }
        }
        
        return true;
    }


}
