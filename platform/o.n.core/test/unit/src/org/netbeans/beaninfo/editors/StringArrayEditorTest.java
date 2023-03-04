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
import java.util.Arrays;
import junit.framework.TestCase;
import org.openide.nodes.Node;

/**
 *
 * @author jarda
 */
public class StringArrayEditorTest extends TestCase {
    static {
        PropertyEditorManager.registerEditor (String[].class, StringArrayEditor.class);
    }

    public StringArrayEditorTest (String testName) {
        super (testName);
    }

    public void testTheEditorHonoursSeparatorAttribute () throws Exception {
        NP np = new NP ();
        np.setValue ("item.separator", "-");
        
        PropertyEditor p = np.getPropertyEditor ();
        assertNotNull ("There is some editor", p);
        assertEquals ("It is StringArrayEditor", StringArrayEditor.class, p.getClass ());
        ((StringArrayEditor)p).readEnv (np);
        
        p.setAsText ("A-B");
        
        String[] value = (String[])p.getValue ();
        
        assertNotNull ("Values is there", value);
        if (value.length != 2 || !"A".equals (value[0]) || !"B".equals(value[1])) {
            fail ("Unexpected arrays: " + Arrays.asList (value));
        }
        
        p.setValue (new String[] { "X", "Y" });
        String t = np.getPropertyEditor ().getAsText ();
        if (!"X- Y".equals (t)) {
            fail ("Wrong text: " + t);
        }
    }
    
    class NP extends Node.Property<String[]> {
        public String[] value;
        
        public NP () {
            super (String[].class);
        }

        public @Override void setValue(String[] val) throws IllegalAccessException, IllegalArgumentException, InvocationTargetException {
            value = val;
        }

        public @Override String[] getValue() throws IllegalAccessException, InvocationTargetException {
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
