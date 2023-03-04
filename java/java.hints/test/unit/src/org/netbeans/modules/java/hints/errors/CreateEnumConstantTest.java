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

import com.sun.source.util.TreePath;
import java.util.LinkedList;
import java.util.List;
import org.netbeans.api.java.source.CompilationInfo;
import org.netbeans.modules.java.hints.infrastructure.ErrorHintsTestBase;
import org.netbeans.spi.editor.hints.Fix;

/**
 * Tests for creation of enum constant
 *
 * @author Max Sauer
 */
public class CreateEnumConstantTest extends ErrorHintsTestBase {

    public CreateEnumConstantTest(String name) {
        super(name);
    }

    public void testEnumCreation0() throws Exception {
        performFixTest("test/Test.java",
                "package test; public class Test {enum Name { A, B, C; } Name someName = Name.|D;}",
                "CreateEnumConstant:D:test.Test.Name:test.Test.Name",
                "package test; public class Test {enum Name { A, B, C, D; } Name someName = Name.D;}");
    }

    public void testEnumCreationOnProperPlace() throws Exception {
        performFixTest("test/Test.java",
                "package test; public class Test {enum Name { A, B, C; } static {Name n = Name.|EE;}}",
                "CreateEnumConstant:EE:test.Test.Name:test.Test.Name",
                "package test; public class Test {enum Name { A, B, C, EE; } static {Name n = Name.EE;}}");
    }

    public void testEnumCreationOnProperPlaceWithFieldPresent() throws Exception {
        performFixTest("test/Test.java",
                "package test; public class Test {enum Name { A, B, C; private int i; } static {Name n = Name.|EE;}}",
                "CreateEnumConstant:EE:test.Test.Name:test.Test.Name",
                "package test; public class Test {enum Name { A, B, C, EE; private int i; } static {Name n = Name.EE;}}");
    }
    
    @Override
    protected List<Fix> computeFixes(CompilationInfo info, int pos, TreePath path) throws Exception {
        List<Fix> fixes = CreateElement.analyze(info, pos);
        List<Fix> result=  new LinkedList<Fix>();
        
        for (Fix f : fixes) {
            if (f instanceof CreateEnumConstant)
                result.add(f);
        }
        
        return result;
    }

    @Override
    protected String toDebugString(CompilationInfo info, Fix f) {
        return ((CreateEnumConstant) f).toDebugString(info);
    }
    
}
