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
 * Portions Copyrighted 2007 Sun Microsystems, Inc.
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
