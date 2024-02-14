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

package org.netbeans.spi.editor.highlighting.support;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Random;
import javax.swing.text.AttributeSet;
import javax.swing.text.PlainDocument;
import javax.swing.text.Position;
import javax.swing.text.SimpleAttributeSet;
import org.netbeans.api.editor.settings.AttributesUtilities;
import org.netbeans.junit.NbTestCase;
import org.netbeans.spi.editor.highlighting.*;

/**
 *
 * @author Vita Stejskal
 */
public class PositionsBagRandomTest extends NbTestCase {
    
    private static final int START = 0;
    private static final int END = 100;

    private final Random RAND = new Random();
    private String [] layerNames;
    private HighlightsContainer[] containers;
    
    public PositionsBagRandomTest(String testName) {
        super(testName);
    }

    protected void setUp() {
        RAND.setSeed(System.currentTimeMillis());

        layerNames = new String [] {
            "layer-A",
            "layer-B",
            "layer-C",
        };
        
        containers = new HighlightsContainer [layerNames.length];
        
        for (int i = 0; i < layerNames.length; i++) {
            containers[i] = createRandomBag(layerNames[i]);
        }

    }
    
    public void testMerging() {
        HighlightsContainer composite = mergeContainers(true, layerNames, containers);
        
        for (int pointer = START; pointer <= END; pointer++) {
            String failMsg = null;
            Highlight [] highestPair = new Highlight [] { null, null };
            Highlight [] compositePair = new Highlight [] { null, null };
            
            try {
                highestPair = new Highlight [] { null, null };
                compositePair = new Highlight [] { null, null };
                
                // Find all highlights at the position
                ArrayList<AttributeSet> leftHighlights = new ArrayList<AttributeSet>();
                ArrayList<AttributeSet> rightHighlights = new ArrayList<AttributeSet>();
                for (int i = 0; i < containers.length; i++) {
                    Highlight [] containerPair = findPair(pointer, containers[i].getHighlights(START, END));
                    if (containerPair[0] != null) {
                        leftHighlights.add(containerPair[0].getAttributes());
                    }
                    if (containerPair[1] != null) {
                        rightHighlights.add(containerPair[1].getAttributes());
                    }
                }
                
                if (!leftHighlights.isEmpty()) {
                    highestPair[0] = new Highlight(pointer, pointer, AttributesUtilities.createComposite(leftHighlights.toArray(new AttributeSet[0])));
                }
                if (!rightHighlights.isEmpty()) {
                    highestPair[1] = new Highlight(pointer, pointer, AttributesUtilities.createComposite(rightHighlights.toArray(new AttributeSet[0])));
                }
                
                // Find the composite container highlight at the position
                compositePair = findPair(pointer, composite.getHighlights(START, END));

                for (int i = 0; i < 2; i++) {
                    if (highestPair[i] != null && compositePair[i] != null) {
                        // Both highlights exist -> check they are the same
                        if (!highestPair[i].getAttributes().isEqual(compositePair[i].getAttributes())) {
                            failMsg = (i == 0 ? "Left" : "Right") + "pair attributes do not match";
                        }
                    } else if (highestPair[i] != null || compositePair[i] != null) {
                        // Both highlights should be null otherwise they would not match
                        failMsg = (i == 0 ? "Left" : "Right") + " highlight doesn't match";
                    }
                }
            } catch (Throwable e) {
                failMsg = e.getMessage();
            }
            
            if (failMsg != null) {
                dumpAll(pointer, layerNames, containers, composite);

                // Dump the pair that failed
                System.out.println("highest pair (pos = " + pointer + ") : " + dumpHighlight(highestPair[0]) + ", " + dumpHighlight(highestPair[1]));
                System.out.println("  proxy pair (pos = " + pointer + ") : " + dumpHighlight(compositePair[0]) + ", " + dumpHighlight(compositePair[1]));
                
                fail(failMsg + " (position = " + pointer + ")");
            }
        }
    }

    public void testTrimming() {
        HighlightsContainer composite = mergeContainers(false, layerNames, containers);

        for (int pointer = START; pointer <= END; pointer++) {
            String failMsg = null;
            Highlight [] highestPair = new Highlight [] { null, null };
            Highlight [] compositePair = new Highlight [] { null, null };
            
            try {
                highestPair = new Highlight [] { null, null };
                compositePair = new Highlight [] { null, null };
                
                // Find the highest highlight at the position
                for (int i = containers.length - 1; i >= 0; i--) {
                    Highlight [] containerPair = findPair(pointer, containers[i].getHighlights(START, END));
                    if (highestPair[0] == null) {
                        highestPair[0] = containerPair[0];
                    }
                    if (highestPair[1] == null) {
                        highestPair[1] = containerPair[1];
                    }
                }
                
                // Find the composite container highlight at the position
                compositePair = findPair(pointer, composite.getHighlights(START, END));

                for (int i = 0; i < 2; i++) {
                    if (highestPair[i] != null && compositePair[i] != null) {
                        // Both highlights exist -> check they are the same
                        if (!highestPair[i].getAttributes().isEqual(compositePair[i].getAttributes())) {
                            failMsg = (i == 0 ? "Left" : "Right") + "pair attributes do not match";
                        }
                    } else if (highestPair[i] != null || compositePair[i] != null) {
                        // Both highlights should be null otherwise they would not match
                        failMsg = (i == 0 ? "Left" : "Right") + " highlight doesn't match";
                    }
                }
            } catch (Throwable e) {
                failMsg = e.getMessage();
            }
            
            if (failMsg != null) {
                dumpAll(pointer, layerNames, containers, composite);

                // Dump the pair that failed
                System.out.println("highest pair (pos = " + pointer + ") : " + dumpHighlight(highestPair[0]) + ", " + dumpHighlight(highestPair[1]));
                System.out.println("  proxy pair (pos = " + pointer + ") : " + dumpHighlight(compositePair[0]) + ", " + dumpHighlight(compositePair[1]));
                
                fail(failMsg + " (position = " + pointer + ")");
            }
        }
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

    private void dumpAll(int position, String [] layerNames, HighlightsContainer[] containers, HighlightsContainer composite) {
        // Dump the layers
        System.out.println("Dumping containers:");
        for (int i = 0; i < containers.length; i++) {
            System.out.println("    containers[" + i + "] " + layerNames[i] + " {");
            for (HighlightsSequence highlights = containers[i].getHighlights(START, END); highlights.moveNext(); ) {
                Highlight h = copyCurrentHighlight(highlights);
                System.out.println("        " + dumpHighlight(h));
            }
            System.out.println("    } End of containers[" + i + "] " + layerNames[i] + " -------------------------");
        }
        System.out.println("Dumping composite container: {");
        for (HighlightsSequence proxyHighlights = composite.getHighlights(START, END); proxyHighlights.moveNext(); ) {
            Highlight h = copyCurrentHighlight(proxyHighlights);
            System.out.println("    " + dumpHighlight(h));
        }
        System.out.println("} End of composite container -----------------------");
    }
    
    private PositionsBag createRandomBag(String bagId) {

        PositionsBag bag = new PositionsBag(new PlainDocument(), false);
        
        int attrIdx = 0;
        int startOffset = START;
        int endOffset = END;

        int maxGapSize = Math.max((int) (endOffset - startOffset) / 10, 1);
        int maxHighlightSize = Math.max((int) (endOffset - startOffset) / 2, 1);

        for (int pointer = startOffset + RAND.nextInt(maxGapSize); pointer <= endOffset; ) {
            int highlightSize = RAND.nextInt(maxHighlightSize);
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
            pointer += highlightSize + RAND.nextInt(maxGapSize);
        }
        
        return bag;
    }

    private PositionsBag mergeContainers(boolean merge, String [] layerNames, HighlightsContainer[] containers) {
        PositionsBag bag = new PositionsBag(new PlainDocument(), merge);

        for (int i = 0; i < containers.length; i++) {
            HighlightsSequence layerHighlights = 
                containers[i].getHighlights(START, END);
            
            for ( ; layerHighlights.moveNext(); ) {
                bag.addHighlight(
                    new SimplePosition(layerHighlights.getStartOffset()), 
                    new SimplePosition(layerHighlights.getEndOffset()), 
                    layerHighlights.getAttributes());
            }
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

    private static final class SimplePosition implements Position {
        private int offset;
        
        public SimplePosition(int offset) {
            this.offset = offset;
        }
        
        public int getOffset() {
            return offset;
        }
    } // End of SimplePosition class
    
}
