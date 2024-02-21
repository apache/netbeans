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

/*
 * ProxyHighlightLayerTest.java
 * JUnit based test
 *
 * Created on June 28, 2006, 5:44 PM
 */

package org.netbeans.modules.editor.lib2.highlighting;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Random;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.PlainDocument;
import javax.swing.text.Position;
import javax.swing.text.SimpleAttributeSet;
import org.netbeans.api.editor.settings.AttributesUtilities;
import org.netbeans.spi.editor.highlighting.support.PositionsBag;
import org.netbeans.spi.editor.highlighting.HighlightsContainer;
import org.netbeans.spi.editor.highlighting.HighlightsChangeEvent;
import org.netbeans.spi.editor.highlighting.HighlightsChangeListener;
import org.netbeans.spi.editor.highlighting.HighlightsSequence;
import org.netbeans.junit.NbTestCase;
import org.netbeans.spi.editor.highlighting.support.AbstractHighlightsContainer;

/**
 *
 * @author vita
 */
public class CompoundHighlightsContainerTest extends NbTestCase {
    
    public CompoundHighlightsContainerTest(String testName) {
        super(testName);
    }

    public void testSimple() {
        PlainDocument doc = new PlainDocument();
        HighlightsContainer layer = createRandomBag(doc, "layer");
        HighlightsSequence highlights = layer.getHighlights(0, 100);
        
        CompoundHighlightsContainer proxyLayer = new CompoundHighlightsContainer(doc, new HighlightsContainer [] { layer });
        HighlightsSequence proxyHighlights = proxyLayer.getHighlights(0, 100);

        for ( ; highlights.moveNext(); ) {
            // Ignore empty highlights
            if (highlights.getStartOffset() == highlights.getEndOffset()) {
                continue;
            }

            assertTrue("Wrong number of proxy highlights", proxyHighlights.moveNext());

            assertEquals("Start offset does not match", highlights.getStartOffset(), proxyHighlights.getStartOffset());
            assertEquals("End offset does not match", highlights.getEndOffset(), proxyHighlights.getEndOffset());
            assertTrue("Attributes do not match", highlights.getAttributes().isEqual(proxyHighlights.getAttributes()));
        }
    }

    public void testOrdering() {
        PlainDocument doc = new PlainDocument();
        PositionsBag hsA = new PositionsBag(doc);
        PositionsBag hsB = new PositionsBag(doc);

        SimpleAttributeSet attribsA = new SimpleAttributeSet();
        SimpleAttributeSet attribsB = new SimpleAttributeSet();
        
        attribsA.addAttribute("attribute", "value-A");
        attribsB.addAttribute("attribute", "value-B");
        
        hsA.addHighlight(new SimplePosition(5), new SimplePosition(15), attribsA);
        hsB.addHighlight(new SimplePosition(10), new SimplePosition(20), attribsB);
        
        CompoundHighlightsContainer chc = new CompoundHighlightsContainer(doc, new HighlightsContainer [] { hsA, hsB });
        HighlightsSequence highlights = chc.getHighlights(0, Integer.MAX_VALUE);

        assertTrue("Wrong number of highlights", highlights.moveNext());
        assertEquals("1. highlight - wrong attribs", "value-A", highlights.getAttributes().getAttribute("attribute"));

        assertTrue("Wrong number of highlights", highlights.moveNext());
        assertEquals("2. highlight - wrong attribs", "value-B", highlights.getAttributes().getAttribute("attribute"));

        assertTrue("Wrong number of highlights", highlights.moveNext());
        assertEquals("3. highlight - wrong attribs", "value-B", highlights.getAttributes().getAttribute("attribute"));
    }

    public void testConcurrentModification() throws Exception {
        PlainDocument doc = new PlainDocument();
        PositionsBag bag = createRandomBag(doc, "layer");
        HighlightsContainer [] layers = new HighlightsContainer [] { bag };
        
        CompoundHighlightsContainer hb = new CompoundHighlightsContainer(doc, layers);
        HighlightsSequence hs = hb.getHighlights(0, Integer.MAX_VALUE);
        
        assertTrue("No highlights", hs.moveNext());
        int s = hs.getStartOffset();
        int e = hs.getEndOffset();
        AttributeSet a = hs.getAttributes();

        // Change the layers
        hb.setLayers(doc, layers);
        
        assertEquals("Different startOffset", s, hs.getStartOffset());
        assertEquals("Different endOffset", e, hs.getEndOffset());
        assertEquals("Different attributes", a, hs.getAttributes());
        
        assertFalse("There should be no further highlighs after co-modification", hs.moveNext());
    }

    public void testRandomMerging() {
        String [] layerNames = new String [] {
            "layer-1",
            "layer-2",
            "layer-3",
        };
        
        PlainDocument doc = new PlainDocument();
        HighlightsContainer [] layers = new HighlightsContainer [layerNames.length];
        for(int i = 0; i < layers.length; i++) {
            layers[i] = createRandomBag(doc, layerNames[i]);
        }
        
        CompoundHighlightsContainer proxyLayer = new CompoundHighlightsContainer(doc, layers);
        
        for (int pointer = 0; pointer <= 100; pointer++) {
            
            // Check the highlights
            String failMsg = null;
            Highlight [] highestPair = new Highlight [] { null, null };
            Highlight [] proxyPair = new Highlight [] { null, null };
            
            
            try {
                highestPair = new Highlight [] { null, null };
                proxyPair = new Highlight [] { null, null };
                
                // Find all highlights at the position
                ArrayList<AttributeSet> leftHighlights = new ArrayList<AttributeSet>();
                ArrayList<AttributeSet> rightHighlights = new ArrayList<AttributeSet>();
                for (int i = 0; i < layers.length; i++) {
                    Highlight [] layerPair = findPair(pointer, layers[i].getHighlights(0, 100));
                    if (layerPair[0] != null) {
                        leftHighlights.add(layerPair[0].getAttributes());
                    }
                    if (layerPair[1] != null) {
                        rightHighlights.add(layerPair[1].getAttributes());
                    }
                }
                
                if (!leftHighlights.isEmpty()) {
                    highestPair[0] = new Highlight(pointer, pointer, AttributesUtilities.createComposite(leftHighlights.toArray(new AttributeSet[0])));
                }
                if (!rightHighlights.isEmpty()) {
                    highestPair[1] = new Highlight(pointer, pointer, AttributesUtilities.createComposite(rightHighlights.toArray(new AttributeSet[0])));
                }
                
                // Find the proxy layer highlight at the position
                proxyPair = findPair(pointer, proxyLayer.getHighlights(0, 100));

                for (int i = 0; i < 2; i++) {
                    if (highestPair[i] != null && proxyPair[i] != null) {
                        // Both highlights exist -> check they are the same
                        if (!highestPair[i].getAttributes().isEqual(proxyPair[i].getAttributes())) {
                            failMsg = (i == 0 ? "Left" : "Right") + "pair attributes do not match";
                        }
                    } else if (highestPair[i] != null || proxyPair[i] != null) {
                        // Both highlights should be null otherwise they would not match
                        failMsg = (i == 0 ? "Left" : "Right") + " highlight doesn't match";
                    }
                }
            } catch (Throwable e) {
                failMsg = e.getMessage();
            }
            
            if (failMsg != null) {
                // Dump the layers
                System.out.println("Dumping layers:");
                for (int i = 0; i < layers.length; i++) {
                    System.out.println("    layer[" + i + "] = " + layerNames[i] + "{");
                    for (HighlightsSequence highlights = layers[i].getHighlights(0, 100); highlights.moveNext(); ) {
                        Highlight h = copyCurrentHighlight(highlights);
                        System.out.println("        " + dumpHighlight(h));
                    }
                    System.out.println("    } End of layer[" + i + "] -----------------");
                }
                System.out.println("Dumping proxy layer: {");
                for (HighlightsSequence proxyHighlights = proxyLayer.getHighlights(0, 100); proxyHighlights.moveNext(); ) {
                    Highlight h = copyCurrentHighlight(proxyHighlights);
                    System.out.println("    " + dumpHighlight(h));
                }
                System.out.println("} End of proxy layer -----------------------");

                // Dump the pair that failed
                System.out.println("highest pair (pos = " + pointer + ") : " + dumpHighlight(highestPair[0]) + ", " + dumpHighlight(highestPair[1]));
                System.out.println("  proxy pair (pos = " + pointer + ") : " + dumpHighlight(proxyPair[0]) + ", " + dumpHighlight(proxyPair[1]));

                fail(failMsg + " (position = " + pointer + ")");
            }
        }
    }

    public void testEvents() {
        PlainDocument doc = new PlainDocument();
        PositionsBag hsA = new PositionsBag(doc);
        PositionsBag hsB = new PositionsBag(doc);
        
        CompoundHighlightsContainer chc = new CompoundHighlightsContainer(doc, new HighlightsContainer [] { hsA, hsB });
        Listener listener = new Listener();
        chc.addHighlightsChangeListener(listener);
        
        hsA.addHighlight(new SimplePosition(10), new SimplePosition(20), new SimpleAttributeSet());
        assertEquals("Wrong number of events", 1, listener.eventsCnt);
        assertEquals("Wrong change start offset", 10, listener.lastEventStartOffset);
        assertEquals("Wrong change end offset", 20, listener.lastEventEndOffset);

        listener.reset();
        hsB.addHighlight(new SimplePosition(11), new SimplePosition(12), new SimpleAttributeSet());
        assertEquals("Wrong number of events", 1, listener.eventsCnt);
        assertEquals("Wrong change start offset", 11, listener.lastEventStartOffset);
        assertEquals("Wrong change end offset", 12, listener.lastEventEndOffset);
    }

    public void testEvents2() {
        PlainDocument doc = new PlainDocument();
        PositionsBag hsA = new PositionsBag(doc);
        PositionsBag hsB = new PositionsBag(doc);

        hsA.addHighlight(new SimplePosition(10), new SimplePosition(20), new SimpleAttributeSet());
        hsB.addHighlight(new SimplePosition(11), new SimplePosition(12), new SimpleAttributeSet());

        CompoundHighlightsContainer chc = new CompoundHighlightsContainer();
        Listener listener = new Listener();
        chc.addHighlightsChangeListener(listener);

        // changing delegate layers fires event covering 'all' offsets
        chc.setLayers(doc, new HighlightsContainer [] { hsA, hsB });
        assertEquals("Wrong number of events", 1, listener.eventsCnt);
        assertEquals("Wrong change start offset", 0, listener.lastEventStartOffset);
        assertEquals("Wrong change end offset", Integer.MAX_VALUE, listener.lastEventEndOffset);
    }

    public void testZeroPosition() throws BadLocationException {
        PlainDocument doc = new PlainDocument();
        TestHighlighsContainer thc = new TestHighlighsContainer();
        CompoundHighlightsContainer chc = new CompoundHighlightsContainer();

        chc.setLayers(doc, new HighlightsContainer[] { thc });
        doc.insertString(0, "0123456789", null);

        chc.getHighlights(0, Integer.MAX_VALUE);
        assertEquals("Should have been queried", 2, thc.queries.size());
        assertEquals("Wrong query startOffset", 0, (int) thc.queries.get(0));

        thc.queries.clear();
        doc.insertString(0, "abcd", null);
        assertEquals("Should not have been queried", 0, thc.queries.size());

        chc.getHighlights(0, Integer.MAX_VALUE);
        assertEquals("Should have been queried again", 2, thc.queries.size());
        assertEquals("Wrong query startOffset", 0, (int) thc.queries.get(0));

        thc.queries.clear();
        chc.getHighlights(0, Integer.MAX_VALUE);
        assertEquals("Should not have been queried again", 0, thc.queries.size());
    }

    private Highlight [] findPair(int offset, HighlightsSequence highlights) {
        Highlight left = null;
        Highlight right = null;
        
        for ( ; highlights.moveNext(); ) {
            if (highlights.getStartOffset() == highlights.getEndOffset()) {
                // ignore empty offsets
                continue;
            }
            
            if (offset > highlights.getStartOffset() && offset < highlights.getEndOffset()) {
                left = right = copyCurrentHighlight(highlights);
            } else if (offset == highlights.getEndOffset()) {
                left = copyCurrentHighlight(highlights);
            } else if (offset == highlights.getStartOffset()) {
                right = copyCurrentHighlight(highlights);
            }
        }
        
        return new Highlight [] { left, right };
    }

    private Highlight copyCurrentHighlight(HighlightsSequence iterator) {
        return new Highlight(
            iterator.getStartOffset(), 
            iterator.getEndOffset(), 
            iterator.getAttributes()
        );
    }
    
    private String dumpHighlight(Highlight h) {
        if (h == null) {
            return "< , , >";
        } else {
            StringBuilder sb = new StringBuilder();

            sb.append("<");
            sb.append(h.getStartOffset());
            sb.append(",");
            sb.append(h.getEndOffset());
            sb.append(",");
            
            Enumeration en = h.getAttributes().getAttributeNames();
            while (en.hasMoreElements()) {
                Object attrName = en.nextElement();
                Object attrValue = h.getAttributes().getAttribute(attrName);

                sb.append("'");
                sb.append(attrName.toString());
                sb.append("' = '");
                sb.append(attrValue == null ? "null" : attrValue.toString());
                sb.append("'");
                if (en.hasMoreElements()) {
                    sb.append(", ");
                }
            }

            sb.append(">");
            
            return sb.toString();
        }
    }
    
    private PositionsBag createRandomBag(Document doc, String bagId) {

        PositionsBag bag = new PositionsBag(doc, false);

        Random rand = new Random(System.currentTimeMillis());
        int attrIdx = 0;
        int startOffset = 0;
        int endOffset = 100;

        int maxGapSize = Math.max((int) (endOffset - startOffset) / 10, 1);
        int maxHighlightSize = Math.max((int) (endOffset - startOffset) / 3, 1);

        for (int pointer = startOffset + rand.nextInt(maxGapSize); pointer <= endOffset; ) {
            int highlightSize = rand.nextInt(maxHighlightSize);
            SimpleAttributeSet attributes = new SimpleAttributeSet();
            attributes.addAttribute("AttrName-" + bagId + "-" + attrIdx, "AttrValue");
            attrIdx++;

            if (pointer + highlightSize < endOffset) {
                bag.addHighlight(
                    new SimplePosition(pointer), new SimplePosition(pointer + highlightSize), attributes);
            } else {
                bag.addHighlight(
                    new SimplePosition(pointer), new SimplePosition(endOffset), attributes);
            }

            // move the pointer
            pointer += highlightSize + rand.nextInt(maxGapSize);
        }
        
        return bag;
    }

    private static final class Highlight {
        private int startOffset;
        private int endOffset;
        private AttributeSet attributes;
        
        public Highlight(int startOffset, int endOffset, AttributeSet attributes) {
            this.startOffset = startOffset;
            this.endOffset = endOffset;
            this.attributes = attributes;
        }

        public int getStartOffset() {
            return startOffset;
        }

        public void setStartOffset(int startOffset) {
            this.startOffset = startOffset;
        }

        public int getEndOffset() {
            return endOffset;
        }

        public void setEndOffset(int endOffset) {
            this.endOffset = endOffset;
        }

        public AttributeSet getAttributes() {
            return attributes;
        }

        public void setAttributes(AttributeSet attributes) {
            this.attributes = attributes;
        }
        
    } // End of H class

    private static final class Listener implements HighlightsChangeListener {
        public int eventsCnt = 0;
        public int lastEventStartOffset = 0;
        public int lastEventEndOffset = 0;
        
        public void highlightChanged(HighlightsChangeEvent event) {
            eventsCnt++;
            lastEventStartOffset = event.getStartOffset();
            lastEventEndOffset = event.getEndOffset();
        }
        
        public void reset() {
            eventsCnt = 0;
            lastEventStartOffset = 0;
            lastEventEndOffset = 0;
        }
    } // End of Listener class

    private static final class SimplePosition implements Position {
        private int offset;
        
        public SimplePosition(int offset) {
            this.offset = offset;
        }
        
        public int getOffset() {
            return offset;
        }
    } // End of SimplePosition class

    private static final class TestHighlighsContainer extends AbstractHighlightsContainer {
        public final List<Integer> queries = new ArrayList<Integer>();

        @Override
        public HighlightsSequence getHighlights(int startOffset, int endOffset) {
            queries.add(startOffset);
            queries.add(endOffset);
            return HighlightsSequence.EMPTY;
        }
    } // End of TestHighlighsContainer class

    private void dumpHighlights(HighlightsSequence seq) {
        System.out.println("Dumping highlights from: " + seq + "{");
        while(seq.moveNext()) {
            System.out.println("<" + seq.getStartOffset() + ", " + seq.getEndOffset() + ", " + seq.getAttributes() + ">");
        }
        System.out.println("} --- End of Dumping highlights from: " + seq + " ---------------------");
    }
}
