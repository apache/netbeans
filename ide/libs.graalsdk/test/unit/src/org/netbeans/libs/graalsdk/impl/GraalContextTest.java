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
package org.netbeans.libs.graalsdk.impl;

import java.io.Reader;
import java.io.StringReader;
import javax.script.ScriptEngine;
import javax.script.ScriptException;
import javax.script.SimpleBindings;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import org.junit.Assume;
import org.junit.Test;
import org.netbeans.api.scripting.Scripting;

public class GraalContextTest {

    public GraalContextTest() {
    }

    @Test
    public void setReaderWords() throws Exception {
        ScriptEngine js = Scripting.createManager().getEngineByMimeType("text/javascript");
        Assume.assumeNotNull("Need js", js);
        String jsName = js.getFactory().getEngineName();
        Reader my = new StringReader("Hello\nthere\n!");
        js.getContext().setReader(my);
        js.eval("10");
        assertEquals("My reader is set", my, js.getContext().getReader());

        try {
            js.getContext().setReader(new StringReader("another"));
            assertTrue("Only nashorn can change reader: " + jsName, jsName.contains("Nashorn"));
        } catch (IllegalStateException ex) {
            assertEquals("Graal.js throws exception", "GraalVM:js", jsName);
        }
    }

    @Test
    public void cannotUseAlternativeBindingsReader() throws ScriptException {
        ScriptEngine js = Scripting.createManager().getEngineByMimeType("text/javascript");
        String jsName = js.getFactory().getEngineName();

        try {
            Object obj = js.eval(new StringReader("42"), new SimpleBindings());
            assertTrue("It is a number " + obj, obj instanceof Number);
            assertEquals(42, ((Number) obj).intValue());
        } catch (ScriptException ex) {
            assertEquals("GraalVM:js", jsName);
        }
    }

    @Test
    public void cannotUseAlternativeBindings() throws ScriptException {
        ScriptEngine js = Scripting.createManager().getEngineByMimeType("text/javascript");
        String jsName = js.getFactory().getEngineName();

        try {
            Object obj = js.eval("42", new SimpleBindings());
            assertTrue("It is a number " + obj, obj instanceof Number);
            assertEquals(42, ((Number) obj).intValue());
        } catch (ScriptException ex) {
            assertEquals("GraalVM:js", jsName);
        }

    }
}
