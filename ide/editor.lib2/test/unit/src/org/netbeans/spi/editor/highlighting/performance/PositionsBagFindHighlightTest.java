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

package org.netbeans.spi.editor.highlighting.performance;

import javax.swing.text.PlainDocument;
import javax.swing.text.SimpleAttributeSet;
import junit.framework.TestSuite;
import org.netbeans.junit.NbTestCase;
import org.netbeans.junit.NbTestSuite;
import org.netbeans.spi.editor.highlighting.support.PositionsBag;
import org.netbeans.spi.editor.highlighting.HighlightsSequence;

/**
 *
 * @author vita
 */
public class PositionsBagFindHighlightTest extends NbTestCase {

    public static TestSuite suite() {
        return NbTestSuite.speedSuite(PositionsBagFindHighlightTest.class, 2, 3);
    }

    private int cnt = 0;
    private PositionsBag bag = null;
    private int startOffset;
    private int endOffset;
    
    /** Creates a new instance of PerfTest */
    public PositionsBagFindHighlightTest(String name) {
        super(name);
    }
    
    protected @Override void setUp() {
        cnt = this.getTestNumber();
        bag = new PositionsBag(new PlainDocument(), false);
        
        for(int i = 0; i < cnt; i++) {
            bag.addHighlight(new SimplePosition(i * 10), new SimplePosition(i * 10 + 5), SimpleAttributeSet.EMPTY);
        }

        startOffset = 10 * cnt / 5 - 1;
        endOffset = 10 * (cnt/ 5 + 1) - 1;
        
//        System.out.println("cnt = " + cnt + " : startOffset = " + startOffset + " : endOffset = " + endOffset);
    }
    
    public void testFindHighlight10000() {
        HighlightsSequence seq = bag.getHighlights(startOffset, endOffset);
        while (seq.moveNext()) { }
    }
    
    public void testFindHighlight100000() {
        HighlightsSequence seq = bag.getHighlights(startOffset, endOffset);
        while (seq.moveNext()) { }
    }
}
