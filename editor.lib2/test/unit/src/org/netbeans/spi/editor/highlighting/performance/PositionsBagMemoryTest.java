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

package org.netbeans.spi.editor.highlighting.performance;

import java.lang.reflect.Method;
import java.util.Collections;
import javax.swing.text.AttributeSet;
import javax.swing.text.Document;
import javax.swing.text.PlainDocument;
import javax.swing.text.Position;
import javax.swing.text.SimpleAttributeSet;
import org.netbeans.junit.MemoryFilter;
import org.netbeans.junit.NbTestCase;
import org.netbeans.lib.editor.util.GapList;
import org.netbeans.spi.editor.highlighting.*;
import org.netbeans.spi.editor.highlighting.support.PositionsBag;

/**
 *
 * @author Vita Stejskal
 */
public class PositionsBagMemoryTest extends NbTestCase {

    private static final int CNT = 1000;
    
    /** Creates a new instance of HighlightsBagPerformanceTest */
    public PositionsBagMemoryTest(String name) {
        super(name);
    }
    
    public void testMemoryConsumptionBestCase() {
        checkMemoryConsumption(false, true);
    }
    
    public void testMemoryConsumptionWorstCase() {
        checkMemoryConsumption(false, false);
    }
    
    public void testIteratorMemoryConsumptionBestCase() {
        checkIteratorMemoryConsumption(false, true);
    }
    
    public void testIteratorMemoryConsumptionWorstCase() {
        checkIteratorMemoryConsumption(false, false);
    }
    
    public void testMergingBagMemoryConsumptionBestCase() {
        checkMemoryConsumption(true, true);
    }
    
    public void testMergingBagMemoryConsumptionWorstCase() {
        checkMemoryConsumption(true, false);
    }
    
    public void testMergingBagIteratorMemoryConsumptionBestCase() {
        checkIteratorMemoryConsumption(true, true);
    }
    
    public void testMergingBagIteratorMemoryConsumptionWorst() {
        checkIteratorMemoryConsumption(true, false);
    }

    private void checkMemoryConsumption(boolean merging, boolean bestCase) {
        PositionsBag bag = new PositionsBag(new PlainDocument(), merging);

        for(int i = 0; i < CNT; i++) {
            if (bestCase) {
                bag.addHighlight(new SimplePosition(i * 10), new SimplePosition((i + 1) * 10), SimpleAttributeSet.EMPTY);
            } else {
                bag.addHighlight(new SimplePosition(i * 10), new SimplePosition(i* 10 + 5), SimpleAttributeSet.EMPTY);
            }
        }

        compact(bag);
        
        assertSize("PositionsBag of " + CNT + " highlights " + (bestCase ? "(best case)" : "(worst case)"),
            Collections.singleton(bag), bestCase ? 8500 : 16500, new MF());
    }

    private void checkIteratorMemoryConsumption(boolean merging, boolean bestCase) {
        PositionsBag bag = new PositionsBag(new PlainDocument(), merging);

        for(int i = 0; i < CNT; i++) {
            if (bestCase) {
                bag.addHighlight(new SimplePosition(i * 10), new SimplePosition((i + 1) * 10), SimpleAttributeSet.EMPTY);
            } else {
                bag.addHighlight(new SimplePosition(i * 10), new SimplePosition(i* 10 + 5), SimpleAttributeSet.EMPTY);
            }
        }

        compact(bag);
        
        HighlightsSequence sequence = bag.getHighlights(Integer.MIN_VALUE, Integer.MAX_VALUE);

        // Do not iterate through the whole sequence, otherwise it will discard its contents
        for(int i = 0; i < CNT - 1; i++) {
            boolean hasHighlight = sequence.moveNext();

            assertTrue("Wrong number of highlights in the sequence; found only " + i, hasHighlight);
            assertEquals("Wrong start offset of " + i + ". highlight", 
                i * 10, sequence.getStartOffset());
            assertEquals("Wrong end offset of " + i + ". highlight", 
                bestCase ? (i + 1) * 10 : i * 10 + 5, sequence.getEndOffset());
            assertSame("Wrong attributes of " + i + ". highlight", SimpleAttributeSet.EMPTY, sequence.getAttributes());
        }
        
        assertSize("HighlightsSequence of " + CNT + " highlights " + (bestCase ? "(best case)" : "(worst case)"),
            Collections.singleton(sequence), bestCase ? 8500 : 16500, new MF());
    }

    @SuppressWarnings("unchecked")
    private void compact(PositionsBag bag) {
        try {
            Method m = bag.getClass().getDeclaredMethod("getMarks");
            m.setAccessible(true);
            GapList<Position> marks = (GapList<Position>) m.invoke(bag);
            marks.trimToSize();

            m = bag.getClass().getDeclaredMethod("getAttributes");
            m.setAccessible(true);
            GapList<Position> attributes = (GapList<Position>) m.invoke(bag);
            attributes.trimToSize();
        } catch (Exception e) {
            AssertionError ae = new AssertionError(e.getMessage());
            ae.initCause(e);
            throw ae;
        }
    }
    
    private static final class MF implements MemoryFilter {
        public boolean reject(Object obj) {
            if (Position.class.isAssignableFrom(obj.getClass()) ||
                AttributeSet.class.isAssignableFrom(obj.getClass()) ||
                Document.class.isAssignableFrom(obj.getClass()))
            {
                return true;
            } else {
                return false;
            }
        }
    } // End of MF class
}
