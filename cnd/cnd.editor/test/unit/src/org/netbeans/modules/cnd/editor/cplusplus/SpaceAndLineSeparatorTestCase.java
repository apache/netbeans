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

package org.netbeans.modules.cnd.editor.cplusplus;

import junit.framework.TestCase;

/**
 *
 */
public class SpaceAndLineSeparatorTestCase extends TestCase {
    
    public SpaceAndLineSeparatorTestCase(String testName) {
        super(testName);
    }
    
    public void testIsSpace() {
        boolean res = Character.isSpaceChar(' ');
        assertTrue("Character.isSpaceChar for ' ' must be true", res);
        res = Character.isSpaceChar('\n');
        assertFalse("Character.isSpaceChar for '\\n' must be false ", res);
        res = Character.isSpaceChar('\t');
        assertFalse("Character.isSpaceChar for '\\t' must be false", res);
        res = Character.isSpaceChar('\r');
        assertFalse("Character.isSpaceChar for '\\r' must be false", res);
    }
    
    public void testIsWhitespace() {
        boolean res = Character.isWhitespace(' ');
        assertTrue("Character.isWhitespace for ' ' must be true", res);
        res = Character.isWhitespace('\n');
        assertTrue("Character.isWhitespace for '\\n' must be true ", res);
        res = Character.isWhitespace('\t');
        assertTrue("Character.isWhitespace for '\\t' must be true", res);
        res = Character.isWhitespace('\r');
        assertTrue("Character.isWhitespace for '\\r' must be true", res);
    }
    
}
