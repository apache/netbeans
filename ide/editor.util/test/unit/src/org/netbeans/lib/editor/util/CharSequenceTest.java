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

package org.netbeans.lib.editor.util;

import junit.framework.TestCase;

public class CharSequenceTest extends TestCase {

    public CharSequenceTest(String testName) {
        super(testName);
    }

    public void testCharSequenceStringLike() {
        CharSequence text = new DelegateCharSequence("abcde");
        assertTrue(text.equals("abcde"));
        CharSequence subText = text.subSequence(1, 3);
        assertTrue(subText.equals("bc"));
    }
    
    private static final class DelegateCharSequence extends AbstractCharSequence.StringLike {
        
        private final String text;
        
        DelegateCharSequence(String text) {
            this.text = text;
        }
        
        public char charAt(int index) {
            return text.charAt(index);
        }
        
        public int length() {
            return text.length();
        }

    }

}
