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

package org.netbeans.modules.cnd.debugger.dbx;

import java.util.HashMap;
import java.util.Map;
import org.junit.Before;
import org.junit.Test;



/**
 *
 */
public class VariablesTestCase extends DebuggerTestCase {

    public VariablesTestCase(String name) {
        super(name);
    }

    @Before
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        debugger = new MockDebugger();
    }

    @Test
    public void testLocalSimple() {
        String name = "test";
        String type = "type";
        String value = "value";
        debugger.addVar(name, type, value);
        
        DebuggerVariable var = new DebuggerVariable(null, null, name, type, value);

        assertEquals("Incorrect name,", name, var.getVariableName());
        assertEquals("Incorrect type,", type, var.getType());
        assertEquals("Incorrect value,", value, var.getAsText());
    }

/* LATER
    @Test
    public void testWatchSimple() {
        String name = "test";
        String type = "type";
        String value = "value";
        ((MockDebugger)debugger).addVar(name, type, value);

        AbstractVariable var = new GdbWatchVariable(debugger, dm.createWatch(name));

        assertEquals("Incorrect name,", name, var.getName());
        assertEquals("Incorrect type,", type, var.getType());
        assertEquals("Incorrect value,", value, var.getValue());
    }

    @Test
    public void testLocalInt() {
        String name = "test";
        String type = "int";
        String value = "5";
        ((MockDebugger)debugger).addVar(name, type, value, type);

        AbstractVariable var = new GdbLocalVariable(debugger, name);

        assertEquals("Incorrect name,", name, var.getName());
        assertEquals("Incorrect type,", type, var.getType());
        assertEquals("Incorrect value,", value, var.getValue());

        // Should have no fields
        assertEquals(0, var.getFieldsCount());
    }

    @Test
    public void testLocalString() {
        String name = "test";
        String type = "char *";
        String value = "\"abcd\"";
        ((MockDebugger)debugger).addVar(name, type, value, type);

        AbstractVariable var = new GdbLocalVariable(debugger, name);

        assertEquals("Incorrect name,", name, var.getName());
        assertEquals("Incorrect type,", type, var.getType());
        assertEquals("Incorrect value,", value, var.getValue());

        // Should have no fields
        assertEquals(0, var.getFieldsCount());
    }

    @Test
    public void testLocalStringSTD() {
        String name = "test";
        String type = "string";
        String value = VariableInfo.getStDStringValue("abc");
        ((MockDebugger)debugger).addVar(name, type, value, VariableInfo.STD_STRING_PTYPE);

        AbstractVariable var = new GdbLocalVariable(debugger, name);

        assertEquals("Incorrect name,", name, var.getName());
        assertEquals("Incorrect type,", type, var.getType());
        assertEquals("Incorrect value,", value, var.getValue());

        // Should have no fields
        assertTrue(var.getFieldsCount() > 0);
        assertEquals(2, var.getFields().length);
    }

    @Test
    public void testLocalIntArray() {
        String name = "test";
        String type = "int[2]";
        String value = "{5, 4}";
        ((MockDebugger)debugger).addVar(name, type, value, type);

        AbstractVariable var = new GdbLocalVariable(debugger, name);

        assertEquals("Incorrect name,", name, var.getName());
        assertEquals("Incorrect type,", type, var.getType());
        assertEquals("Incorrect value,", value, var.getValue());

        // Should have 2 fields
        assertEquals(2, var.getFields().length);
        assertEquals("int", var.getFields()[0].getType());
        assertEquals("5", var.getFields()[0].getValue());
        assertEquals("int", var.getFields()[1].getType());
        assertEquals("4", var.getFields()[1].getValue());
    }

    @Test
    public void testLocalIntRepeatingArray() {
        String name = "test";
        String type = "int[3]";
        String value = "{5, 4 <repeats 2 times>}";
        ((MockDebugger)debugger).addVar(name, type, value, type);

        AbstractVariable var = new GdbLocalVariable(debugger, name);

        assertEquals("Incorrect name,", name, var.getName());
        assertEquals("Incorrect type,", type, var.getType());
        assertEquals("Incorrect value,", value, var.getValue());

        // Should have 3 fields
        assertEquals(3, var.getFields().length);
        assertEquals("int", var.getFields()[0].getType());
        assertEquals("5", var.getFields()[0].getValue());
        assertEquals("int", var.getFields()[1].getType());
        assertEquals("4", var.getFields()[1].getValue());
        assertEquals("int", var.getFields()[2].getType());
        assertEquals("4", var.getFields()[2].getValue());
    }

    @Test
    public void testLocalCharArray() {
        String name = "test";
        String type = "char[2]";
        String value = "\"a\", \"b\"";
        ((MockDebugger)debugger).addVar(name, type, value, type);

        AbstractVariable var = new GdbLocalVariable(debugger, name);

        assertEquals("Incorrect name,", name, var.getName());
        assertEquals("Incorrect type,", type, var.getType());
        assertEquals("Incorrect value,", value, var.getValue());

        // Should have 2 fields
        assertEquals(2, var.getFields().length);
        assertEquals("char", var.getFields()[0].getType());
        assertEquals("\"a\"", var.getFields()[0].getValue());
        assertEquals("char", var.getFields()[1].getType());
        assertEquals("\"b\"", var.getFields()[1].getValue());
    }

    @Test
    public void testLocalCharRepeatingArray() {
        String name = "test";
        String type = "char[3]";
        String value = "\\\"a\\\", 'b' <repeats 2 times>";
        ((MockDebugger)debugger).addVar(name, type, value, type);

        AbstractVariable var = new GdbLocalVariable(debugger, name);

        assertEquals("Incorrect name,", name, var.getName());
        assertEquals("Incorrect type,", type, var.getType());
        // FIXME: Value changes in reality - not good
        //assertEquals("Incorrect value,", value, var.getValue());

        // Should have 3 fields
        assertEquals(3, var.getFields().length);
        assertEquals("char", var.getFields()[0].getType());
        assertEquals("'a'", var.getFields()[0].getValue());
        assertEquals("char", var.getFields()[1].getType());
        assertEquals("'b'", var.getFields()[1].getValue());
        assertEquals("char", var.getFields()[2].getType());
        assertEquals("'b'", var.getFields()[2].getValue());
    }

    @Test
    public void testLocalIntArray2() {
        String name = "test";
        String type = "int[2][3]";
        String value = "{{5, 4, 3},{2, 1, 0}}";
        ((MockDebugger)debugger).addVar(name, type, value, type);

        AbstractVariable var = new GdbLocalVariable(debugger, name);

        assertEquals("Incorrect name,", name, var.getName());
        assertEquals("Incorrect type,", type, var.getType());
        assertEquals("Incorrect value,", value, var.getValue());

        // Should have 2 fields
        assertEquals(2, var.getFields().length);
        assertEquals("int[3]", var.getFields()[0].getType());
        assertEquals("{5, 4, 3}", var.getFields()[0].getValue());
        assertEquals("int[3]", var.getFields()[1].getType());
        assertEquals("{2, 1, 0}", var.getFields()[1].getValue());
    }

    @Test
    public void testLocalCharArray2() {
        String name = "test";
        String type = "char[2][3]";
        String value = "{\"abc\", \"xyz\"}";
        ((MockDebugger)debugger).addVar(name, type, value, type);

        AbstractVariable var = new GdbLocalVariable(debugger, name);

        assertEquals("Incorrect name,", name, var.getName());
        assertEquals("Incorrect type,", type, var.getType());
        assertEquals("Incorrect value,", value, var.getValue());

        // Should have 2 fields
        assertEquals(2, var.getFields().length);
        assertEquals("char[3]", var.getFields()[0].getType());
        assertEquals("\"abc\"", var.getFields()[0].getValue());
        assertEquals("char[3]", var.getFields()[1].getType());
        assertEquals("\"xyz\"", var.getFields()[1].getValue());
    }
*/

    public static class MockDebugger {
        private final Map<String, String> values = new HashMap<String, String>();
        private final Map<String, String> types = new HashMap<String, String>();
        private final Map<String, String> ptypes = new HashMap<String, String>();
        
        private void addVar(String name, String type, String value) {
            values.put(name, value);
            types.put(name, type);
        }

        private void addVar(String name, String type, String value, String ptype) {
            values.put(name, value);
            types.put(name, type);
            ptypes.put(name, ptype);
        }

    }
}
