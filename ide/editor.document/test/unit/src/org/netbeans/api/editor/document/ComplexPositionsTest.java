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
package org.netbeans.api.editor.document;

import javax.swing.text.Document;
import javax.swing.text.PlainDocument;
import javax.swing.text.Position;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Miloslav Metelka
 */
public class ComplexPositionsTest {
    
    public ComplexPositionsTest() {
    }

    @Test
    public void testPos() throws Exception {
        Document doc = new PlainDocument();
        doc.insertString(0, "\t\t\n\n", null);
        Position pos1 = doc.createPosition(1);
        Position pos2 = doc.createPosition(2);
        
        Position pos10 = ComplexPositions.create(pos1, 0);
        Position pos11 = ComplexPositions.create(pos1, 1);
        Position pos20 = ComplexPositions.create(pos2, 0);
        Position pos21 = ComplexPositions.create(pos2, 1);
        
        assertEquals(0, ComplexPositions.getSplitOffset(pos1));
        assertEquals(0, ComplexPositions.getSplitOffset(pos10));
        assertEquals(1, ComplexPositions.getSplitOffset(pos11));
        comparePos(pos1, pos10, 0);
        comparePos(pos10, pos11, -1);
        comparePos(pos1, pos2, -1);
        comparePos(pos10, pos20, -1);
        comparePos(pos20, pos21, -1);
    }
    
    private void comparePos(Position pos1, Position pos2, int expectedResult) {
        comparePosImpl(pos1, pos2, expectedResult, true);
    }

    private void comparePosImpl(Position pos1, Position pos2, int expectedResult, boolean reverseCompare) {
        int result = ComplexPositions.compare(pos1, pos2);
        assertEquals("Invalid result=" + result + " when comparing positions pos1=" +
                pos1 + " to pos2=" + pos2, expectedResult, result);

        result = ComplexPositions.compare(pos1.getOffset(), ComplexPositions.getSplitOffset(pos1),
                pos2.getOffset(), ComplexPositions.getSplitOffset(pos2));
        assertEquals("Invalid result=" + result + " when comparing positions pos1=" +
                pos1 + " to pos2=" + pos2, expectedResult, result);

        if (reverseCompare) {
            comparePosImpl(pos2, pos1, -expectedResult, false);
        }
    }

}
