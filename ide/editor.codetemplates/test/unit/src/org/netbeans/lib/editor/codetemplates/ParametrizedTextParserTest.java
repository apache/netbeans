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

package org.netbeans.lib.editor.codetemplates;

import java.util.Map;
import org.netbeans.junit.NbTestCase;

/**
 *
 * @author: Arthur Sadykov
 */
public class ParametrizedTextParserTest extends NbTestCase {

    public ParametrizedTextParserTest(String testName) {
        super(testName);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
    }

    public void testGetParametrizedFragmentsByOrdinals() {
        ParametrizedTextParser parser = new ParametrizedTextParser(
                null,
                "public ${Type type=\"java.lang.String\" editable=false} ${name newVarName}() {\n"
                + "    ${cursor}\n"
                + "}");
        parser.parse();
        Map<Integer, Object> parametrizedFragmentsByOrdinals = parser.getParametrizedFragmentsByOrdinals();
        assertEquals(7, parametrizedFragmentsByOrdinals.size());
        assertEquals(parametrizedFragmentsByOrdinals.get(0), "public ");
        assertEquals("name=Type, slave=false, value=\"Type\"",
                ((CodeTemplateParameterImpl)parametrizedFragmentsByOrdinals.get(1)).toString());
        assertEquals(parametrizedFragmentsByOrdinals.get(2), " ");
        assertEquals("name=name, slave=false, value=\"name\"",
                ((CodeTemplateParameterImpl)parametrizedFragmentsByOrdinals.get(3)).toString());
        assertEquals(parametrizedFragmentsByOrdinals.get(4), "() {\n    ");
        assertEquals("name=cursor, slave=false, value=\"\"",
                ((CodeTemplateParameterImpl)parametrizedFragmentsByOrdinals.get(5)).toString());
        assertEquals(parametrizedFragmentsByOrdinals.get(6), "\n}");
    }
}
