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

import org.netbeans.junit.NbTestCase;

/**
 * Test of FlyOffsetGapList correctness.
 *
 * @author mmetelka
 */
public class FlyOffsetGapListTest extends NbTestCase {

    public FlyOffsetGapListTest(java.lang.String testName) {
        super(testName);
    }

    @SuppressWarnings("unchecked")
    public void test() throws Exception {
        FOGL fogl = new FOGL(3); // start offset is 3
        assertEquals(3, fogl.elementOrEndOffset(0));

        try {
            fogl.elementOffset(0);
            fail("Exception not thrown");
        } catch (IndexOutOfBoundsException e) {
            // Expected
        }
        try {
            fogl.elementOrEndOffset(1);
            fail("Exception not thrown");
        } catch (IndexOutOfBoundsException e) {
            // Expected
        }
        try {
            fogl.elementOrEndOffset(-1);
            fail("Exception not thrown");
        } catch (IndexOutOfBoundsException e) {
            // Expected
        }
        
        fogl.add(new Element(-1, 5));
        fogl.defaultInsertUpdate(3, 5);
        assertEquals(3, fogl.elementOrEndOffset(0));
        assertEquals(8, fogl.elementOrEndOffset(1));
    }
    
    private static final class FOGL extends FlyOffsetGapList<Element> {
        
        private int startOffset;
        
        FOGL(int startOffset) {
            this.startOffset = startOffset;
        }
        
        @Override
        protected int startOffset() {
            return startOffset;
        }

        protected boolean isElementFlyweight(Element elem) {
            return elem.isFlyweight();
        }
        
        protected int elementLength(Element elem) {
            return elem.length();
        }

        protected int elementRawOffset(Element elem) {
            return elem.rawOffset();
        }
        
        protected void setElementRawOffset(Element elem, int rawOffset) {
            elem.setRawOffset(rawOffset);
        }

    }
    
    private static final class Element {
        
        private int rawOffset;
        
        private final int length;
        
        public Element(int rawOffset, int length) {
            this.rawOffset = rawOffset;
            this.length = length;
        }
        
        public int rawOffset() {
            return rawOffset;
        }
        
        public void setRawOffset(int rawOffset) {
            this.rawOffset = rawOffset;
        }
        
        public int length() {
            return length;
        }
        
        public boolean isFlyweight() {
            return (rawOffset == -1);
        }

    }
    
}
