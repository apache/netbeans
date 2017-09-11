/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
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
