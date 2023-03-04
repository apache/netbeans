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

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Random;
import javax.swing.JEditorPane;
import javax.swing.text.AttributeSet;
import javax.swing.text.DefaultEditorKit;
import javax.swing.text.PlainDocument;
import javax.swing.text.Position;
import javax.swing.text.SimpleAttributeSet;
import junit.framework.Test;
import junit.framework.TestResult;
import org.netbeans.api.editor.mimelookup.MimeLookup;
import org.netbeans.api.editor.mimelookup.MimePath;
import org.netbeans.api.editor.settings.FontColorSettings;
import org.netbeans.junit.NbTestCase;
import org.netbeans.junit.NbTestSuite;
import org.netbeans.junit.RandomlyFails;
import org.netbeans.spi.editor.highlighting.support.OffsetsBag;
import org.netbeans.spi.editor.highlighting.HighlightsChangeEvent;
import org.netbeans.spi.editor.highlighting.HighlightsChangeListener;
import org.netbeans.spi.editor.highlighting.HighlightsContainer;
import org.netbeans.spi.editor.highlighting.HighlightsLayer;
import org.netbeans.spi.editor.highlighting.HighlightsLayerFactory;
import org.netbeans.spi.editor.highlighting.HighlightsSequence;
import org.netbeans.spi.editor.highlighting.ReleasableHighlightsContainer;
import org.netbeans.spi.editor.highlighting.ZOrder;
import org.netbeans.spi.editor.highlighting.support.AbstractHighlightsContainer;
import org.openide.util.Lookup;

/**
 *
 * @author vita
 */
public class HighlightingManagerTest extends NbTestCase {
    
    public static Test suite() {
        return new NbTestSuite(HighlightingManagerTest.class) {
            public @Override void run(TestResult result) {
// XXX: needs a way how to run each test case in a separate JVM or how to replace HighlightingManager.LINEWRAP_ENABLED
//                System.out.println("Running tests for: org.netbeans.editor.linewrap=true");
//                System.setProperty("org.netbeans.editor.linewrap", "true");
//                super.run(result);

                System.out.println("Running tests for: org.netbeans.editor.linewrap=false");
                System.setProperty("org.netbeans.editor.linewrap", "false");
                super.run(result);
            }
        };
    }

    /** Creates a new instance of HighlightingManagerTest */
    public HighlightingManagerTest(String name) {
        super(name);
    }
    
    @RandomlyFails
    public void testSimple() {
        JEditorPane pane = new JEditorPane();
        pane.putClientProperty("HighlightsLayerExcludes", "^org\\.netbeans\\.modules\\.editor\\.lib2\\.highlighting\\..*$");
        HighlightingManager hm = HighlightingManager.getInstance(pane);
        assertNotNull("Can't get instance of HighlightingManager", hm);
        HighlightsContainer hc = hm.getHighlights(HighlightsLayerFilter.IDENTITY);
        assertNotNull("Can't get fixed HighlightsContainer", hc);
        assertEquals(0, pane.getDocument().getLength());
        assertFalse("There should be no fixed highlights", hc.getHighlights(0, Integer.MAX_VALUE).moveNext());
    }
    
    public void testSimpleLayer() {
        OffsetsBag bag = new OffsetsBag(new PlainDocument());
        
        MemoryMimeDataProvider.reset(null);
        MemoryMimeDataProvider.addInstances(
            "text/plain", new SingletonLayerFactory("layer", ZOrder.DEFAULT_RACK, true, bag));

        JEditorPane pane = new JEditorPane();
        pane.putClientProperty("HighlightsLayerExcludes", "^org\\.netbeans\\.modules\\.editor\\.lib2\\.highlighting\\..*$");
        pane.setContentType("text/plain");
        assertEquals("The pane has got wrong mime type", "text/plain", pane.getContentType());
        
        HighlightingManager hm = HighlightingManager.getInstance(pane);
        HighlightsContainer hc = hm.getHighlights(HighlightsLayerFilter.IDENTITY);
        assertNotNull("Can't get fixed HighlightsContainer", hc);
        assertFalse("There should be no fixed highlights", hc.getHighlights(0, Integer.MAX_VALUE).moveNext());
        
        SimpleAttributeSet attributes = new SimpleAttributeSet();
        attributes.addAttribute("attrib-A", "value");
        
        bag.addHighlight(10, 20, attributes);
        
        HighlightsSequence highlights = hc.getHighlights(0, Integer.MAX_VALUE);
        assertTrue("Highlight has not been added", moveNextSkipNullAttrs(highlights));
        assertEquals("Wrong start offset", 10, highlights.getStartOffset());
        assertEquals("Wrong end offset", 20, highlights.getEndOffset());
        assertEquals("Can't find attribute", "value", highlights.getAttributes().getAttribute("attrib-A"));
    }
    
    public void testExcludeTwoLayers() {
        OffsetsBag bag = new OffsetsBag(new PlainDocument());
        
        MemoryMimeDataProvider.reset(null);
        MemoryMimeDataProvider.addInstances(
            "text/plain", new SingletonLayerFactory("layer", ZOrder.DEFAULT_RACK, true, bag));

        JEditorPane pane = new JEditorPane();
        String [] removed = new String[] {"^org\\.netbeans\\.modules\\.editor\\.lib2\\.highlighting\\..*$", "^org\\.netbeans\\.modules\\.editor\\.lib2\\.highlighting\\.TextSelectionHighlighting$"};
        pane.putClientProperty("HighlightsLayerExcludes", removed);
        pane.setContentType("text/plain");
        assertEquals("The pane has got wrong mime type", "text/plain", pane.getContentType());
        
        HighlightingManager hm = HighlightingManager.getInstance(pane);
        HighlightsContainer hc = hm.getHighlights(HighlightsLayerFilter.IDENTITY);

        assertNotNull("Can't get fixed HighlightsContainer", hc);
        assertFalse("There should be no fixed highlights", hc.getHighlights(0, Integer.MAX_VALUE).moveNext());
    }

    // test multiple layers, merging, ordering
    
    public void testMultipleLayers() {
        OffsetsBag bagA = new OffsetsBag(new PlainDocument());
        OffsetsBag bagB = new OffsetsBag(new PlainDocument());
        OffsetsBag bagC = new OffsetsBag(new PlainDocument());
        OffsetsBag bagD = new OffsetsBag(new PlainDocument());

        MemoryMimeDataProvider.reset(null);
        MemoryMimeDataProvider.addInstances("text/plain",
            new SingletonLayerFactory("layerB", ZOrder.DEFAULT_RACK.forPosition(2), false, bagB),
            new SingletonLayerFactory("layerD", ZOrder.DEFAULT_RACK.forPosition(6), true, bagD),
            new SingletonLayerFactory("layerA", ZOrder.DEFAULT_RACK, true, bagA),
            new SingletonLayerFactory("layerC", ZOrder.DEFAULT_RACK.forPosition(4), true, bagC)
        );
        
        JEditorPane pane = new JEditorPane();
        pane.putClientProperty("HighlightsLayerExcludes", "^org\\.netbeans\\.modules\\.editor\\.lib2\\.highlighting\\..*$");
        pane.setContentType("text/plain");
        assertEquals("The pane has got wrong mime type", "text/plain", pane.getContentType());
        
        HighlightingManager hm = HighlightingManager.getInstance(pane);
        HighlightsContainer variableHC = hm.getHighlights(VARIABLE_SIZE_LAYERS);
        assertNotNull("Can't get variable HighlightsContainer", variableHC);
        assertFalse("There should be no variable highlights", variableHC.getHighlights(0, Integer.MAX_VALUE).moveNext());

        HighlightsContainer fixedHC = hm.getHighlights(FIXED_SIZE_LAYERS);
        assertNotNull("Can't get fixed HighlightsContainer", fixedHC);
        assertFalse("There should be no fixed highlights", fixedHC.getHighlights(0, Integer.MAX_VALUE).moveNext());

        SimpleAttributeSet attribsA = new SimpleAttributeSet();
        SimpleAttributeSet attribsB = new SimpleAttributeSet();
        SimpleAttributeSet attribsC = new SimpleAttributeSet();
        SimpleAttributeSet attribsD = new SimpleAttributeSet();
        attribsA.addAttribute("set-A", "value");
        attribsA.addAttribute("commonAttribute", "set-A-value");
        attribsB.addAttribute("set-B", "value");
        attribsB.addAttribute("commonAttribute", "set-B-value");
        attribsC.addAttribute("set-C", "value");
        attribsC.addAttribute("commonAttribute", "set-C-value");
        attribsD.addAttribute("set-D", "value");
        attribsD.addAttribute("commonAttribute", "set-D-value");

        bagA.addHighlight(10, 20, attribsA);
        bagB.addHighlight(15, 25, attribsB);
        bagC.addHighlight(10, 20, attribsC);
        bagD.addHighlight(15, 25, attribsD);


        // Check fixed-size leyers sequence - should be C, D
        HighlightsSequence fixed = fixedHC.getHighlights(0, Integer.MAX_VALUE);
        // Check 1. highlight
        assertTrue("Wrong number of highlights", moveNextSkipNullAttrs(fixed));
        assertEquals("Wrong start offset", 10, fixed.getStartOffset());
        assertEquals("Wrong end offset", 15, fixed.getEndOffset());
        assertAttribContains("Can't find attribute", fixed.getAttributes(), "set-C", "commonAttribute");
        assertAttribNotContains("The attribute should not be there", fixed.getAttributes(), "set-A", "set-B", "set-D");
        assertEquals("Wrong commonAttribute value", "set-C-value", fixed.getAttributes().getAttribute("commonAttribute"));
        // Check 2. highlight
        assertTrue("Wrong number of highlights", moveNextSkipNullAttrs(fixed));
        assertEquals("Wrong start offset", 15, fixed.getStartOffset());
        assertEquals("Wrong end offset", 20, fixed.getEndOffset());
        assertAttribContains("Can't find attribute", fixed.getAttributes(), "set-C", "set-D", "commonAttribute");
        assertAttribNotContains("The attribute should not be there", fixed.getAttributes(), "set-A", "set-B");
        assertEquals("Wrong commonAttribute value", "set-D-value", fixed.getAttributes().getAttribute("commonAttribute"));
        // Check 3. highlight
        assertTrue("Wrong number of highlights", moveNextSkipNullAttrs(fixed));
        assertEquals("Wrong start offset", 20, fixed.getStartOffset());
        assertEquals("Wrong end offset", 25, fixed.getEndOffset());
        assertAttribContains("Can't find attribute", fixed.getAttributes(), "set-D", "commonAttribute");
        assertAttribNotContains("The attribute should not be there", fixed.getAttributes(), "set-A", "set-B", "set-C");
        assertEquals("Wrong commonAttribute value", "set-D-value", fixed.getAttributes().getAttribute("commonAttribute"));

        
        // Check variable-size leyers sequence - should be A, B
        HighlightsSequence variable = variableHC.getHighlights(0, Integer.MAX_VALUE);
        // Check 1. highlight
        assertTrue("Wrong number of highlights", moveNextSkipNullAttrs(variable));
        assertEquals("Wrong start offset", 10, variable.getStartOffset());
        assertEquals("Wrong end offset", 15, variable.getEndOffset());
        assertAttribContains("Can't find attribute", variable.getAttributes(), "set-A", "commonAttribute");
        assertAttribNotContains("The attribute should not be there", variable.getAttributes(), "set-C", "set-D", "set-B");
        assertEquals("Wrong commonAttribute value", "set-A-value", variable.getAttributes().getAttribute("commonAttribute"));
        // Check 2. highlight
        assertTrue("Wrong number of highlights", moveNextSkipNullAttrs(variable));
        assertEquals("Wrong start offset", 15, variable.getStartOffset());
        assertEquals("Wrong end offset", 20, variable.getEndOffset());
        assertAttribContains("Can't find attribute", variable.getAttributes(), "set-A", "set-B", "commonAttribute");
        assertAttribNotContains("The attribute should not be there", variable.getAttributes(), "set-C", "set-D");
        assertEquals("Wrong commonAttribute value", "set-B-value", variable.getAttributes().getAttribute("commonAttribute"));
        // Check 3. highlight
        assertTrue("Wrong number of highlights", moveNextSkipNullAttrs(variable));
        assertEquals("Wrong start offset", 20, variable.getStartOffset());
        assertEquals("Wrong end offset", 25, variable.getEndOffset());
        assertAttribContains("Can't find attribute", variable.getAttributes(), "set-B", "commonAttribute");
        assertAttribNotContains("The attribute should not be there", variable.getAttributes(), "set-C", "set-D", "set-A");
        assertEquals("Wrong commonAttribute value", "set-B-value", variable.getAttributes().getAttribute("commonAttribute"));

        bagA.clear();
        bagB.clear();
        bagC.clear();
        bagD.clear();
    }
    
    // test events fired from HCs when changing highlights on a layer
    
    public void testChangesInLayerFireEvents() {
        OffsetsBag bagA = new OffsetsBag(new PlainDocument());
        OffsetsBag bagB = new OffsetsBag(new PlainDocument());
        OffsetsBag bagC = new OffsetsBag(new PlainDocument());
        OffsetsBag bagD = new OffsetsBag(new PlainDocument());

        MemoryMimeDataProvider.reset(null);
        MemoryMimeDataProvider.addInstances("text/plain",
            new SingletonLayerFactory("layerB", ZOrder.DEFAULT_RACK.forPosition(2), false, bagB),
            new SingletonLayerFactory("layerD", ZOrder.DEFAULT_RACK.forPosition(6), true, bagD),
            new SingletonLayerFactory("layerA", ZOrder.DEFAULT_RACK, true, bagA),
            new SingletonLayerFactory("layerC", ZOrder.DEFAULT_RACK.forPosition(4), true, bagC)
        );

        JEditorPane pane = new JEditorPane();
        pane.putClientProperty("HighlightsLayerExcludes", "^org\\.netbeans\\.modules\\.editor\\.lib2\\.highlighting\\..*$");
        pane.setContentType("text/plain");
        assertEquals("The pane has got wrong mime type", "text/plain", pane.getContentType());
        
        HighlightingManager hm = HighlightingManager.getInstance(pane);
        
        // Test the variable-size layers - A,B
        Listener variableL = new Listener();
        HighlightsContainer variableHC = hm.getHighlights(VARIABLE_SIZE_LAYERS);
        assertNotNull("Can't get variable HighlightsContainer", variableHC);
        assertFalse("There should be no variable highlights", variableHC.getHighlights(0, Integer.MAX_VALUE).moveNext());

        variableHC.addHighlightsChangeListener(variableL);
        bagA.addHighlight(10, 20, SimpleAttributeSet.EMPTY);
        assertEquals("Wrong number of events", 1, variableL.eventsCnt);
        assertEquals("Wrong change start offset", 10, variableL.lastStartOffset);
        assertEquals("Wrong change end offset", 20, variableL.lastEndOffset);

        variableL.reset();
        bagB.addHighlight(5, 15, SimpleAttributeSet.EMPTY);
        assertEquals("Wrong number of events", 1, variableL.eventsCnt);
        assertEquals("Wrong change start offset", 5, variableL.lastStartOffset);
        assertEquals("Wrong change end offset", 15, variableL.lastEndOffset);

        // Test the fixed-size layers
        Listener fixedL = new Listener();
        HighlightsContainer fixedHC = hm.getHighlights(FIXED_SIZE_LAYERS);
        assertNotNull("Can't get fixed HighlightsContainer", fixedHC);
        assertFalse("There should be no fixed highlights", fixedHC.getHighlights(0, Integer.MAX_VALUE).moveNext());
        
        fixedHC.addHighlightsChangeListener(fixedL);
        bagC.addHighlight(20, 50, SimpleAttributeSet.EMPTY);
        assertEquals("Wrong number of events", 1, fixedL.eventsCnt);
        assertEquals("Wrong change start offset", 20, fixedL.lastStartOffset);
        assertEquals("Wrong change end offset", 50, fixedL.lastEndOffset);

        fixedL.reset();
        bagD.addHighlight(0, 30, SimpleAttributeSet.EMPTY);
        assertEquals("Wrong number of events", 1, fixedL.eventsCnt);
        assertEquals("Wrong change start offset", 0, fixedL.lastStartOffset);
        assertEquals("Wrong change end offset", 30, fixedL.lastEndOffset);
    }
    
    // test adding/removing a layer
    
    public void testAddingRemovingLayers() {
        final String mimeType = "text/plain";
        
        OffsetsBag bagA = new OffsetsBag(new PlainDocument());
        OffsetsBag bagB = new OffsetsBag(new PlainDocument());
        OffsetsBag bagC = new OffsetsBag(new PlainDocument());
        OffsetsBag bagD = new OffsetsBag(new PlainDocument());

        SimpleAttributeSet attribsA = new SimpleAttributeSet();
        SimpleAttributeSet attribsB = new SimpleAttributeSet();
        SimpleAttributeSet attribsC = new SimpleAttributeSet();
        SimpleAttributeSet attribsD = new SimpleAttributeSet();
        attribsA.addAttribute("set-A", "value");
        attribsA.addAttribute("commonAttribute", "set-A-value");
        attribsB.addAttribute("set-B", "value");
        attribsB.addAttribute("commonAttribute", "set-B-value");
        attribsC.addAttribute("set-C", "value");
        attribsC.addAttribute("commonAttribute", "set-C-value");
        attribsD.addAttribute("set-D", "value");
        attribsD.addAttribute("commonAttribute", "set-D-value");

        bagA.addHighlight(10, 20, attribsA);
        bagB.addHighlight(15, 25, attribsB);
        bagC.addHighlight(50, 60, attribsC);
        bagD.addHighlight(55, 65, attribsD);
        
        SingletonLayerFactory layerA = new SingletonLayerFactory("layerA", ZOrder.DEFAULT_RACK, true, bagA);
        SingletonLayerFactory layerB = new SingletonLayerFactory("layerB", ZOrder.DEFAULT_RACK.forPosition(1), false, bagB);
        SingletonLayerFactory layerC = new SingletonLayerFactory("layerC", ZOrder.DEFAULT_RACK.forPosition(2), true, bagC);
        SingletonLayerFactory layerD = new SingletonLayerFactory("layerD", ZOrder.DEFAULT_RACK.forPosition(3), true, bagD);
        
        MemoryMimeDataProvider.reset(null);
        MemoryMimeDataProvider.addInstances(mimeType, layerA, layerD);

        JEditorPane pane = new JEditorPane();
        pane.putClientProperty("HighlightsLayerExcludes", "^org\\.netbeans\\.modules\\.editor\\.lib2\\.highlighting\\..*$");
        pane.setEditorKit(new SimpleKit(mimeType));
        assertEquals("The pane has got wrong mime type", mimeType, pane.getContentType());
        
        HighlightingManager hm = HighlightingManager.getInstance(pane);

        assertNotNull("Can't get variable HighlightsContainer", hm.getHighlights(VARIABLE_SIZE_LAYERS));
        assertNotNull("Can't get fixed HighlightsContainer", hm.getHighlights(FIXED_SIZE_LAYERS));
        
        {
            HighlightsSequence variable = hm.getHighlights(VARIABLE_SIZE_LAYERS).getHighlights(0, Integer.MAX_VALUE);
            assertFalse("There should be no variable highlights", variable.moveNext());
        }
        
        {
            HighlightsSequence fixed = hm.getHighlights(FIXED_SIZE_LAYERS).getHighlights(0, Integer.MAX_VALUE);
            // Check 1. highlight
            assertTrue("Wrong number of highlights", moveNextSkipNullAttrs(fixed));
            assertEquals("Wrong start offset", 10, fixed.getStartOffset());
            assertEquals("Wrong end offset", 20, fixed.getEndOffset());
            assertAttribContains("Can't find attribute", fixed.getAttributes(), "set-A", "commonAttribute");
            assertAttribNotContains("The attribute should not be there", fixed.getAttributes(), "set-B", "set-C", "set-D");
            assertEquals("Wrong commonAttribute value", "set-A-value", fixed.getAttributes().getAttribute("commonAttribute"));
            // Check 2. highlight
            assertTrue("Wrong number of highlights", moveNextSkipNullAttrs(fixed));
            assertEquals("Wrong start offset", 55, fixed.getStartOffset());
            assertEquals("Wrong end offset", 65, fixed.getEndOffset());
            assertAttribContains("Can't find attribute", fixed.getAttributes(), "set-D", "commonAttribute");
            assertAttribNotContains("The attribute should not be there", fixed.getAttributes(), "set-A", "set-B", "set-C");
            assertEquals("Wrong commonAttribute value", "set-D-value", fixed.getAttributes().getAttribute("commonAttribute"));
        }

        // Add layer B - that should put A, B in VARIABLE_SIZE_LAYERS and leave D in FIXED_SIZE_LAYERS
        MemoryMimeDataProvider.addInstances(mimeType, layerB);
        
        {
            HighlightsSequence variable = hm.getHighlights(VARIABLE_SIZE_LAYERS).getHighlights(0, Integer.MAX_VALUE);
            // Check 1. highlight
            assertTrue("Wrong number of highlights", moveNextSkipNullAttrs(variable));
            assertEquals("Wrong start offset", 10, variable.getStartOffset());
            assertEquals("Wrong end offset", 15, variable.getEndOffset());
            assertAttribContains("Can't find attribute", variable.getAttributes(), "set-A", "commonAttribute");
            assertAttribNotContains("The attribute should not be there", variable.getAttributes(), "set-B", "set-C", "set-D");
            assertEquals("Wrong commonAttribute value", "set-A-value", variable.getAttributes().getAttribute("commonAttribute"));
            // Check 2. highlight
            assertTrue("Wrong number of highlights", moveNextSkipNullAttrs(variable));
            assertEquals("Wrong start offset", 15, variable.getStartOffset());
            assertEquals("Wrong end offset", 20, variable.getEndOffset());
            assertAttribContains("Can't find attribute", variable.getAttributes(), "set-A", "set-B", "commonAttribute");
            assertAttribNotContains("The attribute should not be there", variable.getAttributes(), "set-C", "set-D");
            assertEquals("Wrong commonAttribute value", "set-B-value", variable.getAttributes().getAttribute("commonAttribute"));
            // Check 3. highlight
            assertTrue("Wrong number of highlights", moveNextSkipNullAttrs(variable));
            assertEquals("Wrong start offset", 20, variable.getStartOffset());
            assertEquals("Wrong end offset", 25, variable.getEndOffset());
            assertAttribContains("Can't find attribute", variable.getAttributes(), "set-B", "commonAttribute");
            assertAttribNotContains("The attribute should not be there", variable.getAttributes(), "set-A", "set-C", "set-D");
            assertEquals("Wrong commonAttribute value", "set-B-value", variable.getAttributes().getAttribute("commonAttribute"));
        }

        {
            HighlightsSequence fixed = hm.getHighlights(FIXED_SIZE_LAYERS).getHighlights(0, Integer.MAX_VALUE);
            // Check 1. highlight
            assertTrue("Wrong number of highlights", moveNextSkipNullAttrs(fixed));
            assertEquals("Wrong start offset", 55, fixed.getStartOffset());
            assertEquals("Wrong end offset", 65, fixed.getEndOffset());
            assertAttribContains("Can't find attribute", fixed.getAttributes(), "set-D", "commonAttribute");
            assertAttribNotContains("The attribute should not be there", fixed.getAttributes(), "set-A", "set-B", "set-C");
            assertEquals("Wrong commonAttribute value", "set-D-value", fixed.getAttributes().getAttribute("commonAttribute"));
        }
        
        // Add layer C - that should leave A, B in VARIABLE_SIZE_LAYERS, should also leave D in FIXED_SIZE_LAYERS and add C in FIXED_SIZE_LAYERS
        MemoryMimeDataProvider.addInstances(mimeType, layerC);

        {
            HighlightsSequence variable = hm.getHighlights(VARIABLE_SIZE_LAYERS).getHighlights(0, Integer.MAX_VALUE);
            // Check 1. highlight
            assertTrue("Wrong number of highlights", moveNextSkipNullAttrs(variable));
            assertEquals("Wrong start offset", 10, variable.getStartOffset());
            assertEquals("Wrong end offset", 15, variable.getEndOffset());
            assertAttribContains("Can't find attribute", variable.getAttributes(), "set-A", "commonAttribute");
            assertAttribNotContains("The attribute should not be there", variable.getAttributes(), "set-B", "set-C", "set-D");
            assertEquals("Wrong commonAttribute value", "set-A-value", variable.getAttributes().getAttribute("commonAttribute"));
            // Check 2. highlight
            assertTrue("Wrong number of highlights", moveNextSkipNullAttrs(variable));
            assertEquals("Wrong start offset", 15, variable.getStartOffset());
            assertEquals("Wrong end offset", 20, variable.getEndOffset());
            assertAttribContains("Can't find attribute", variable.getAttributes(), "set-A", "set-B", "commonAttribute");
            assertAttribNotContains("The attribute should not be there", variable.getAttributes(), "set-C", "set-D");
            assertEquals("Wrong commonAttribute value", "set-B-value", variable.getAttributes().getAttribute("commonAttribute"));
            // Check 3. highlight
            assertTrue("Wrong number of highlights", moveNextSkipNullAttrs(variable));
            assertEquals("Wrong start offset", 20, variable.getStartOffset());
            assertEquals("Wrong end offset", 25, variable.getEndOffset());
            assertAttribContains("Can't find attribute", variable.getAttributes(), "set-B", "commonAttribute");
            assertAttribNotContains("The attribute should not be there", variable.getAttributes(), "set-A", "set-C", "set-D");
            assertEquals("Wrong commonAttribute value", "set-B-value", variable.getAttributes().getAttribute("commonAttribute"));
        }

        {
            HighlightsSequence fixed = hm.getHighlights(FIXED_SIZE_LAYERS).getHighlights(0, Integer.MAX_VALUE);
            // Check 1. highlight
            assertTrue("Wrong number of highlights", moveNextSkipNullAttrs(fixed));
            assertEquals("Wrong start offset", 50, fixed.getStartOffset());
            assertEquals("Wrong end offset", 55, fixed.getEndOffset());
            assertAttribContains("Can't find attribute", fixed.getAttributes(), "set-C", "commonAttribute");
            assertAttribNotContains("The attribute should not be there", fixed.getAttributes(), "set-A", "set-B", "set-D");
            assertEquals("Wrong commonAttribute value", "set-C-value", fixed.getAttributes().getAttribute("commonAttribute"));
            // Check 2. highlight
            assertTrue("Wrong number of highlights", moveNextSkipNullAttrs(fixed));
            assertEquals("Wrong start offset", 55, fixed.getStartOffset());
            assertEquals("Wrong end offset", 60, fixed.getEndOffset());
            assertAttribContains("Can't find attribute", fixed.getAttributes(), "set-C", "set-D", "commonAttribute");
            assertAttribNotContains("The attribute should not be there", fixed.getAttributes(), "set-A", "set-B");
            assertEquals("Wrong commonAttribute value", "set-D-value", fixed.getAttributes().getAttribute("commonAttribute"));
            // Check 3. highlight
            assertTrue("Wrong number of highlights", moveNextSkipNullAttrs(fixed));
            assertEquals("Wrong start offset", 60, fixed.getStartOffset());
            assertEquals("Wrong end offset", 65, fixed.getEndOffset());
            assertAttribContains("Can't find attribute", fixed.getAttributes(), "set-D", "commonAttribute");
            assertAttribNotContains("The attribute should not be there", fixed.getAttributes(), "set-A", "set-B", "set-C");
            assertEquals("Wrong commonAttribute value", "set-D-value", fixed.getAttributes().getAttribute("commonAttribute"));
        }

        // Remove layer B - that should put A in FIXED_SIZE_LAYERS, should also leave C, D in FIXED_SIZE_LAYERS
        MemoryMimeDataProvider.removeInstances(mimeType, layerB);
        
        {
            HighlightsSequence variable = hm.getHighlights(VARIABLE_SIZE_LAYERS).getHighlights(0, Integer.MAX_VALUE);
            assertFalse("There should be no variable highlights", variable.moveNext());
        }
        
        {
            HighlightsSequence fixed = hm.getHighlights(FIXED_SIZE_LAYERS).getHighlights(0, Integer.MAX_VALUE);
            // Check 1. highlight
            assertTrue("Wrong number of highlights", moveNextSkipNullAttrs(fixed));
            assertEquals("Wrong start offset", 10, fixed.getStartOffset());
            assertEquals("Wrong end offset", 20, fixed.getEndOffset());
            assertAttribContains("Can't find attribute", fixed.getAttributes(), "set-A", "commonAttribute");
            assertAttribNotContains("The attribute should not be there", fixed.getAttributes(), "set-B", "set-C", "set-D");
            assertEquals("Wrong commonAttribute value", "set-A-value", fixed.getAttributes().getAttribute("commonAttribute"));
            // Check 2. highlight
            assertTrue("Wrong number of highlights", moveNextSkipNullAttrs(fixed));
            assertEquals("Wrong start offset", 50, fixed.getStartOffset());
            assertEquals("Wrong end offset", 55, fixed.getEndOffset());
            assertAttribContains("Can't find attribute", fixed.getAttributes(), "set-C", "commonAttribute");
            assertAttribNotContains("The attribute should not be there", fixed.getAttributes(), "set-A", "set-B", "set-D");
            assertEquals("Wrong commonAttribute value", "set-C-value", fixed.getAttributes().getAttribute("commonAttribute"));
            // Check 3. highlight
            assertTrue("Wrong number of highlights", moveNextSkipNullAttrs(fixed));
            assertEquals("Wrong start offset", 55, fixed.getStartOffset());
            assertEquals("Wrong end offset", 60, fixed.getEndOffset());
            assertAttribContains("Can't find attribute", fixed.getAttributes(), "set-C", "set-D", "commonAttribute");
            assertAttribNotContains("The attribute should not be there", fixed.getAttributes(), "set-A", "set-B");
            assertEquals("Wrong commonAttribute value", "set-D-value", fixed.getAttributes().getAttribute("commonAttribute"));
            // Check 4. highlight
            assertTrue("Wrong number of highlights", moveNextSkipNullAttrs(fixed));
            assertEquals("Wrong start offset", 60, fixed.getStartOffset());
            assertEquals("Wrong end offset", 65, fixed.getEndOffset());
            assertAttribContains("Can't find attribute", fixed.getAttributes(), "set-D", "commonAttribute");
            assertAttribNotContains("The attribute should not be there", fixed.getAttributes(), "set-A", "set-B", "set-C");
            assertEquals("Wrong commonAttribute value", "set-D-value", fixed.getAttributes().getAttribute("commonAttribute"));
        }
        
        // Remove all remaining layers - that should remove all highlighs
        MemoryMimeDataProvider.removeInstances(mimeType, layerA, layerC, layerD);
        
        {
            HighlightsSequence variable = hm.getHighlights(VARIABLE_SIZE_LAYERS).getHighlights(0, Integer.MAX_VALUE);
            assertFalse("There should be no variable highlights", variable.moveNext());
        }

        {
            HighlightsSequence fixed = hm.getHighlights(FIXED_SIZE_LAYERS).getHighlights(0, Integer.MAX_VALUE);
            assertFalse("There should be no fixed highlights", fixed.moveNext());
        }
    }
    
    // test events fired from HCs when adding/removing a layer
    
    public void testEventsWhenAddingRemovingLayers() {
        OffsetsBag bagA = new OffsetsBag(new PlainDocument());
        OffsetsBag bagB = new OffsetsBag(new PlainDocument());

        SingletonLayerFactory layerA = new SingletonLayerFactory("layerA", ZOrder.DEFAULT_RACK, true, bagA);
        SingletonLayerFactory layerB = new SingletonLayerFactory("layerB", ZOrder.DEFAULT_RACK, false, bagB);
        
        MemoryMimeDataProvider.reset(null);

        JEditorPane pane = new JEditorPane();
        pane.putClientProperty("HighlightsLayerExcludes", "^org\\.netbeans\\.modules\\.editor\\.lib2\\.highlighting\\..*$");
        pane.setContentType("text/plain");
        assertEquals("The pane has got wrong mime type", "text/plain", pane.getContentType());
        
        final HighlightingManager hm = HighlightingManager.getInstance(pane);
        final HighlightsContainer hc = hm.getHighlights(HighlightsLayerFilter.IDENTITY);

        assertNotNull("Can't get fixed HighlightsContainer", hc);

        // There should be no layers and no highlights
        
        {
            HighlightsSequence fixed = hc.getHighlights(0, Integer.MAX_VALUE);
            assertFalse("There should be no highlights", fixed.moveNext());
        }
    
        Listener listener = new Listener();
        hc.addHighlightsChangeListener(listener);

        // Add layer A - it's a fixed-size layer
        listener.reset();
        MemoryMimeDataProvider.addInstances("text/plain", layerA);

// Current HM impl rebuilds merging containers from scratch so the following checks would fail        
//        assertEquals("Wrong number of events", 1, listener.eventsCnt);
//        assertNull("Wrong change start position", listener.lastStartPosition);
//        assertNull("Wrong change end position", listener.lastEndPosition);
//        assertEquals("Wrong change start offset", 0, listener.lastStartOffset);
//        assertEquals("Wrong change end offset", Integer.MAX_VALUE, listener.lastEndOffset);
        
        {
            HighlightsSequence fixed = hc.getHighlights(0, Integer.MAX_VALUE);
            assertFalse("There should be no highlights", fixed.moveNext());
        }

        // Add layer B - it's a variable-size layer
        listener.reset();
        MemoryMimeDataProvider.addInstances("text/plain", layerB);
        
// Current HM impl rebuilds merging containers from scratch so the following checks would fail        
//        assertEquals("Wrong number of events", 1, listener.eventsCnt);
//        assertNull("Wrong change start position", listener.lastStartPosition);
//        assertNull("Wrong change end position", listener.lastEndPosition);
//        assertEquals("Wrong change start offset", 0, listener.lastStartOffset);
//        assertEquals("Wrong change end offset", Integer.MAX_VALUE, listener.lastEndOffset);
        
        {
            HighlightsSequence fixed = hc.getHighlights(0, Integer.MAX_VALUE);
            assertFalse("There should be no highlights", fixed.moveNext());
        }
        
        // Remove layer A - it's a fixed-size layer
        listener.reset();
        MemoryMimeDataProvider.removeInstances("text/plain", layerA);
        
// Current HM impl rebuilds merging containers from scratch so the following checks would fail        
//        assertEquals("Wrong number of events", 1, listener.eventsCnt);
//        assertNull("Wrong change start position", listener.lastStartPosition);
//        assertNull("Wrong change end position", listener.lastEndPosition);
//        assertEquals("Wrong change start offset", 0, listener.lastStartOffset);
//        assertEquals("Wrong change end offset", Integer.MAX_VALUE, listener.lastEndOffset);
        
        {
            HighlightsSequence fixed = hc.getHighlights(0, Integer.MAX_VALUE);
            assertFalse("There should be no highlights", fixed.moveNext());
        }

        // Remove layer B - it's a variable-size layer
        listener.reset();
        MemoryMimeDataProvider.removeInstances("text/plain", layerB);
        
// Current HM impl rebuilds merging containers from scratch so the following checks would fail        
//        assertEquals("Wrong number of events", 1, listener.eventsCnt);
//        assertNull("Wrong change start position", listener.lastStartPosition);
//        assertNull("Wrong change end position", listener.lastEndPosition);
//        assertEquals("Wrong change start offset", 0, listener.lastStartOffset);
//        assertEquals("Wrong change end offset", Integer.MAX_VALUE, listener.lastEndOffset);
        
        {
            HighlightsSequence fixed = hc.getHighlights(0, Integer.MAX_VALUE);
            assertFalse("There should be no highlights", fixed.moveNext());
        }
    }

    public void testReleaseLayers() {
        final String mimeType = "text/plain";
        TestReleaseHighlightsContainer releasableContainer = new TestReleaseHighlightsContainer();
        SingletonLayerFactory releasableLayer = new SingletonLayerFactory(
                "releasableLayer", ZOrder.DEFAULT_RACK.forPosition(1), true, releasableContainer);

        MemoryMimeDataProvider.reset(null);
        MemoryMimeDataProvider.addInstances(mimeType, releasableLayer);

        JEditorPane pane = new JEditorPane(mimeType, "Hello");
        HighlightingManager hm = HighlightingManager.getInstance(pane); // Ensure layers get created
        hm.getHighlights(HighlightsLayerFilter.IDENTITY);
        pane.setEditorKit(new SimpleKit(mimeType));
        // Do not check against concrete release count since there are e.g. mime lookup rebuild notifications
        // that lead to HM.rebuildAllLayers() which increases the releaseCount too.
        assertTrue("Highlights container releasing not performed after pane.setEditorKit()", releasableContainer.releaseCount > 0);
    }
    
    // test getting independent HCs for different JEditorPanes with the same mime type
    
    public void testCaching() {
        MemoryMimeDataProvider.reset(null);
        JEditorPane pane1 = new JEditorPane();
        HighlightingManager hm1 = HighlightingManager.getInstance(pane1);

        pane1.setContentType("text/plain");
        assertEquals("The pane has got wrong mime type", "text/plain", pane1.getContentType());
        
        JEditorPane pane2 = new JEditorPane();
        HighlightingManager hm2 = HighlightingManager.getInstance(pane2);
        pane2.setContentType("text/plain");
        assertEquals("The pane has got wrong mime type", "text/plain", pane2.getContentType());
        
        {
            HighlightsContainer hc1_A = hm1.getHighlights(HighlightsLayerFilter.IDENTITY);
            HighlightsContainer hc1_B = hm1.getHighlights(HighlightsLayerFilter.IDENTITY);
// Current impl of hm.getHighlights() does not cache HC instances so the following test would fail
//            assertSame("HighlightsContainer is not cached", hc1_A, hc1_B);

            HighlightsContainer hc2 = hm2.getHighlights(HighlightsLayerFilter.IDENTITY);
            assertNotSame("HighlightsContainer should not be shared between JEPs", hc1_A, hc2);
        }
        
        gc();
        
        {
            int hc1_A_hash = System.identityHashCode(hm1.getHighlights(HighlightsLayerFilter.IDENTITY));
            int hc1_B_hash = System.identityHashCode(hm1.getHighlights(HighlightsLayerFilter.IDENTITY));
// Current impl of hm.getHighlights() does not cache HC instances so the following test would fail
//            assertEquals("HighlightsContainer is not cached (different hash codes)", hc1_A_hash, hc1_B_hash);
        }
    }
    
    // test that bags and everything is GCed when the JEditorPane, which they were created for, is gone

    public void testCachedInstancesGCed() {
        MemoryMimeDataProvider.reset(null);
        
        // Hold MimePath instance and lookup result; the highlighting container should still
        // be GCed
        final MimePath mimePath = MimePath.parse("text/plain");
        final Lookup.Result<FontColorSettings> lookupResult = MimeLookup.getLookup(mimePath).lookupResult(FontColorSettings.class);
        Collection<? extends FontColorSettings> fcs = lookupResult.allInstances();
        assertTrue("There should be FontColorSettings for " + mimePath.getPath(), fcs.size() > 0);
        
        JEditorPane pane = new JEditorPane();
        HighlightingManager hm = HighlightingManager.getInstance(pane);
        pane.setContentType("text/plain");
        assertEquals("The pane has got wrong mime type", "text/plain", pane.getContentType());

        HighlightsContainer hc = hm.getHighlights(HighlightsLayerFilter.IDENTITY);
        assertNotNull("Can't get HighlightsContainer", hc);

        WeakReference<JEditorPane> refPane = new WeakReference<JEditorPane>(pane);
        WeakReference<HighlightsContainer> refHc = new WeakReference<HighlightsContainer>(hc);
        
        // reset hard references
        pane = null;
        hm = null;
        hc = null;
        
        assertGC("JEP has not been GCed", refPane);
        assertGC("HC has not been GCed", refHc);
    }
    
    private void assertAttribContains(String msg, AttributeSet as, String... keys) {
//        System.out.print("assertAttribContains: attributes: ");
//        for(Enumeration<?> attribKeys = as.getAttributeNames(); attribKeys.hasMoreElements(); ) {
//            Object key = attribKeys.nextElement();
//            Object value = as.getAttribute(key);
//            System.out.print("'" + key + "' = '" + value + "', ");
//        }
//        System.out.println();
        
        assertEquals(msg, keys.length, as.getAttributeCount());
        for (String key : keys) {
            if (null == as.getAttribute(key)) {
                fail(msg + " attribute key: " + key);
            }
        }
    }

    private void assertAttribNotContains(String msg, AttributeSet as, String... keys) {
//        System.out.print("assertAttribNotContains: attributes: ");
//        for(Enumeration<?> attribKeys = as.getAttributeNames(); attribKeys.hasMoreElements(); ) {
//            Object key = attribKeys.nextElement();
//            Object value = as.getAttribute(key);
//            System.out.print("'" + key + "' = '" + value + "', ");
//        }
//        System.out.println();
        
        for (String key : keys) {
            if (null != as.getAttribute(key) || as.isDefined(key)) {
                fail(msg + " attribute key: " + key);
            }
        }
    }

    private void gc() {
        Random rand = new Random(System.currentTimeMillis());
        for(int i = 0; i < 5; i++) {
            System.gc();
            try {
                Thread.sleep(123 + rand.nextInt(1000));
            } catch (InterruptedException e) {
                // ignore
            }
        }
    }
    
    private void dumpLookupContents(String mimePath) {
        Lookup lookup = MimeLookup.getLookup(MimePath.parse(mimePath));
        Lookup.Result<Object> result = lookup.lookupResult(Object.class);
        Collection<? extends Lookup.Item<Object>> items = result.allItems();
        
        System.out.println("Lookup for " + mimePath + " : {");
        for(Lookup.Item<Object> item : items) {
            System.out.println("    " + item.getDisplayName());
        }
        System.out.println("} end of Lookup for " + mimePath + " ----");
    }
    
    static boolean moveNextSkipNullAttrs(HighlightsSequence hs) {
        while (hs.moveNext()) {
            if (hs.getAttributes() != null) {
                return true;
            }
        }
        return false;
    }

    private static final class SingletonLayerFactory implements HighlightsLayerFactory
    {
        private String id;
        private ZOrder zOrder;
        private boolean fixed;
        private HighlightsContainer container;
        
        public SingletonLayerFactory(String id, ZOrder zOrder, boolean fixed, HighlightsContainer hc) {
            this.id = id;
            this.zOrder = zOrder;
            this.fixed = fixed;
            this.container = hc;
        }

        public HighlightsLayer[] createLayers(HighlightsLayerFactory.Context context) {
            return new HighlightsLayer [] { HighlightsLayer.create(id, zOrder, fixed, container) };
        }

        public @Override String toString() {
            return super.toString() + "; id = " + id;
        }
        
    } // End of HLFactory

    private static final class SimplePosition implements Position {
        private int offset;
        
        public SimplePosition(int offset) {
            this.offset = offset;
        }
        
        public int getOffset() {
            return offset;
        }
    } // End of SimplePosition class

    private static final class SimpleKit extends DefaultEditorKit {
        private String mimeType;
        
        public SimpleKit(String mimeType) {
            this.mimeType = mimeType;
        }

        public @Override String getContentType() {
            return mimeType;
        }
    } // End of SimpleKit class
    
    private static final class Listener implements HighlightsChangeListener {
        
        public int eventsCnt = 0;
        public int lastStartOffset;
        public int lastEndOffset;
        public Position lastStartPosition;
        public Position lastEndPosition;
        
        public void highlightChanged(HighlightsChangeEvent event) {
            eventsCnt++;
            lastStartOffset = event.getStartOffset();
            lastEndOffset = event.getEndOffset();
        }
        
        public void reset() {
            eventsCnt = 0;
            lastStartOffset = -1;
            lastEndOffset = -1;
        }
    } // End of Listener class
    
    private static final HighlightsLayerFilter FIXED_SIZE_LAYERS = new HighlightsLayerFilter() {
        public List<? extends HighlightsLayer> filterLayers(List<? extends HighlightsLayer> layers) {
            ArrayList<HighlightsLayer> filteredLayers = new ArrayList<HighlightsLayer>();
            
            for(int i = layers.size() - 1; i >= 0; i--) {
                HighlightsLayer layer = layers.get(i);
                HighlightsLayerAccessor layerAccessor = 
                    HighlightingSpiPackageAccessor.get().getHighlightsLayerAccessor(layer);
                
                if (!layerAccessor.isFixedSize()) {
                    break;
                }
                
                filteredLayers.add(0, layer);
            }
            
            return filteredLayers;
        }
    };
    
    private static final HighlightsLayerFilter VARIABLE_SIZE_LAYERS = new HighlightsLayerFilter() {
        public List<? extends HighlightsLayer> filterLayers(List<? extends HighlightsLayer> layers) {
            ArrayList<HighlightsLayer> filteredLayers = new ArrayList<HighlightsLayer>();
            boolean fixedSize = true;
            
            for(int i = layers.size() - 1; i >= 0; i--) {
                HighlightsLayer layer = layers.get(i);
                HighlightsLayerAccessor layerAccessor = 
                    HighlightingSpiPackageAccessor.get().getHighlightsLayerAccessor(layer);
                
                if (!layerAccessor.isFixedSize()) {
                    fixedSize = false;
                }
                
                if (!fixedSize) {
                    filteredLayers.add(0, layer);
                }
            }
            
            return filteredLayers;
        }
    };
    
    private static final class TestReleaseHighlightsContainer extends AbstractHighlightsContainer
            implements ReleasableHighlightsContainer
    {
        
        public int releaseCount;
        
        @Override
        public HighlightsSequence getHighlights(int startOffset, int endOffset) {
            return HighlightsSequence.EMPTY;
        }

        @Override
        public void released() {
            releaseCount++;
        }
        
    }
    
}
