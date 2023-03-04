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

package org.netbeans.modules.j2ee.sun.ide.editors;

import junit.framework.TestCase;

/**
 *
 * @author vkraemer
 */
public class CharsetDisplayPreferenceEditorTest extends TestCase {

    public void testGetSetAsString() {
        CharsetDisplayPreferenceEditor foo =
            new CharsetDisplayPreferenceEditor();

        String ret = null;
        foo.setAsText(foo.choices[0]);
        ret = foo.getAsText();
        assertEquals(foo.choices[0], ret);
        foo.setAsText(foo.choices[1]);
        ret = foo.getAsText();
        assertEquals(foo.choices[1], ret);
        foo.setAsText(foo.choices[2]);
        ret = foo.getAsText();
        assertEquals(foo.choices[2], ret);
        foo.setAsText("bogus");
        ret = foo.getAsText();
        assertEquals(foo.choices[1], ret);
    }
        
    
    public void testGetSet() {
        CharsetDisplayPreferenceEditor foo =
            new CharsetDisplayPreferenceEditor();

        Integer ret = null;
        foo.setValue(Integer.valueOf("0"));
        ret = (Integer) foo.getValue();
        assertEquals(Integer.valueOf("0"), ret);
        foo.setValue(Integer.valueOf("1"));
        ret = (Integer) foo.getValue();
        assertEquals(Integer.valueOf("1"),ret);
        foo.setValue(Integer.valueOf("2"));
        ret = (Integer) foo.getValue();
        assertEquals(Integer.valueOf("2"), ret);
        foo.setValue(Integer.valueOf("3"));
        ret = (Integer) foo.getValue();
        assertEquals(Integer.valueOf("1"),ret);
        foo.setValue(Integer.valueOf("-1"));
        ret = (Integer) foo.getValue();
        assertEquals(Integer.valueOf("1"), ret );
        
    }
        
    
    public void testCreate() {
        CharsetDisplayPreferenceEditor foo =
            new CharsetDisplayPreferenceEditor();
    }
    
    public CharsetDisplayPreferenceEditorTest(String testName) {
        super(testName);
    }
    
}
