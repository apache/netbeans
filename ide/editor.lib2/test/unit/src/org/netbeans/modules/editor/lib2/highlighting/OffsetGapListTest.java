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
package org.netbeans.modules.editor.lib2.highlighting;

import org.netbeans.junit.NbTestCase;

/**
 *
 * @author vita
 */
public class OffsetGapListTest extends NbTestCase {

    public OffsetGapListTest(String name) {
        super(name);
    }

    public void testMovingZeroOffset() {
        OffsetGapList<OffsetGapList.Offset> ogl = new OffsetGapList<OffsetGapList.Offset>();
        OffsetGapList.Offset offsetA = new OffsetGapList.Offset(0);
        OffsetGapList.Offset offsetB = new OffsetGapList.Offset(100);
        
        ogl.add(offsetA);
        ogl.add(offsetB);
        assertEquals("Wrong initial offset A", 0, offsetA.getOffset());
        assertEquals("Wrong initial offset B", 100, offsetB.getOffset());
        
        ogl.defaultInsertUpdate(0, 10); // simulate insert at the zero offset
        assertEquals("Offset A should have been moved", 10, offsetA.getOffset());
        assertEquals("Offset B should have been moved", 110, offsetB.getOffset());
    }
    
    public void testFixedZeroOffset() {
        OffsetGapList<OffsetGapList.Offset> ogl = new OffsetGapList<OffsetGapList.Offset>(true);
        OffsetGapList.Offset offsetA = new OffsetGapList.Offset(0);
        OffsetGapList.Offset offsetB = new OffsetGapList.Offset(100);
        
        ogl.add(offsetA);
        ogl.add(offsetB);
        assertEquals("Wrong initial offset A", 0, offsetA.getOffset());
        assertEquals("Wrong initial offset B", 100, offsetB.getOffset());
        
        ogl.defaultInsertUpdate(0, 10); // simulate insert at the zero offset
        assertEquals("Offset A should not have been moved", 0, offsetA.getOffset());
        assertEquals("Offset B should have been moved", 110, offsetB.getOffset());
        
        ogl.defaultRemoveUpdate(0, 10); // simulate remove at the zero offset
        assertEquals("Offset A should not have been moved", 0, offsetA.getOffset());
        assertEquals("Offset B should have been moved back", 100, offsetB.getOffset());
        
        ogl.defaultInsertUpdate(0, 3); // simulate insert at the zero offset
        assertEquals("Offset A should not have been moved", 0, offsetA.getOffset());
        assertEquals("Offset B should have been moved again", 103, offsetB.getOffset());
    }
    
}
