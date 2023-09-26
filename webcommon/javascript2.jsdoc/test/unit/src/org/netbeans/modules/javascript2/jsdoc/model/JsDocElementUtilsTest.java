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
package org.netbeans.modules.javascript2.jsdoc.model;

import java.util.List;
import org.junit.Test;
import org.netbeans.modules.javascript2.types.api.Type;

import static org.junit.Assert.assertEquals;

public class JsDocElementUtilsTest {

    public JsDocElementUtilsTest() {
    }

    @Test
    public void testTruncatedParam() {
        JsDocElementUtils.createElementForType(JsDocElementType.PARAM, "Demo with {", 0);
    }

    @Test
    public void testParseTypes() {
        List<Type> results;

        results = JsDocElementUtils.parseTypes("SimpleType", 20);
        assertEquals(1, results.size());
        assertEquals("SimpleType", results.get(0).getType());
        assertEquals(20, results.get(0).getOffset());

        results = JsDocElementUtils.parseTypes("  SimpleType", 20);
        assertEquals(1, results.size());
        assertEquals("SimpleType", results.get(0).getType());
        assertEquals(22, results.get(0).getOffset());

        results = JsDocElementUtils.parseTypes("(test|null)", 20);
        assertEquals(2, results.size());
        assertEquals("test", results.get(0).getType());
        assertEquals(21, results.get(0).getOffset());
        assertEquals("null", results.get(1).getType());
        assertEquals(26, results.get(1).getOffset());

        results = JsDocElementUtils.parseTypes("  (  test | null ) ", 20);
        assertEquals(2, results.size());
        assertEquals("test", results.get(0).getType());
        assertEquals(25, results.get(0).getOffset());
        assertEquals("null", results.get(1).getType());
        assertEquals(32, results.get(1).getOffset());
    }
}
