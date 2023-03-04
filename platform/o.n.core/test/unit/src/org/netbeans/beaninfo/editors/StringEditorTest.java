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

package org.netbeans.beaninfo.editors;

import java.beans.PropertyEditor;
import java.beans.PropertyEditorManager;
import java.lang.reflect.InvocationTargetException;
import junit.framework.TestCase;
import org.openide.nodes.Node;

/**
 *
 * @author rkubacki
 */
public class StringEditorTest extends TestCase {
    static {
        PropertyEditorManager.registerEditor (String.class, StringEditor.class);
    }

    public void testNullValueSupport() throws Exception {
        NP np = new NP();
        String defaultValue = "<null value>";
        String customValue = "Hello world!";
        np.setValue(ObjectEditor.PROP_NULL, defaultValue);
        
        PropertyEditor p = np.getPropertyEditor();
        assertNotNull("There is some editor", p);
        assertEquals("It is StringEditor", StringEditor.class, p.getClass());
        ((StringEditor) p).readEnv(np);
        
        p.setValue(null);
        String value = (String)p.getValue ();
        assertNull(value);
        assertEquals(defaultValue, p.getAsText());

        p.setValue(customValue);
        value = (String)p.getValue ();
        assertEquals(customValue, value);
        assertEquals(customValue, p.getAsText());

        np.setValue(ObjectEditor.PROP_NULL, Boolean.TRUE);
        ((StringEditor) p).readEnv(np);
        p.setValue(null);
        value = (String)p.getValue ();
        assertNull(value);
        assertFalse("we've better than default 'null' string", "null".equals(defaultValue));
    }

    public void testNoCustomEditorWithoutPropertyEnv() {
        NP np = new NP();
        PropertyEditor p = np.getPropertyEditor();
        assertNotNull("There is some editor", p);
        assertEquals("It is StringEditor", StringEditor.class, p.getClass());

        assertFalse("Custom property editor not working without PropertyEnv, so should not be offered.",
                    p.supportsCustomEditor());
    }

    class NP extends Node.Property<String> {
        public String value;
        
        public NP () {
            super (String.class);
        }

        public @Override void setValue(String val) throws IllegalAccessException, IllegalArgumentException, InvocationTargetException {
            value = val;
        }

        public @Override String getValue() throws IllegalAccessException, InvocationTargetException {
            return value;
        }

        public @Override boolean canWrite() {
            return true;
        }

        public @Override boolean canRead() {
            return true;
        }
    }
}
