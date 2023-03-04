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

package org.netbeans.spi.editor.highlighting;

import org.netbeans.junit.NbTestCase;
import org.openide.util.TopologicalSortException;

/**
 *
 * @author vita
 */
public class ZOrderTest extends NbTestCase {
    
    /** Creates a new instance of ZOrderTest */
    public ZOrderTest(String name) {
        super(name);
    }
    
    public void testOrder() throws Exception {
        ZOrder zOrderA = ZOrder.DEFAULT_RACK;
        ZOrder zOrderB = ZOrder.DEFAULT_RACK.forPosition(10);
        
        HighlightsLayer [] layers = new HighlightsLayer [] {
            simpleLayer("layerB", zOrderB),
            simpleLayer("layerA", zOrderA),
        };
        
        HighlightsLayer [] sortedLayers = ZOrder.sort(layers);
        
        assertNotNull("Sorted layers should not be null", sortedLayers);
        assertEquals("Wrong size of sortedLayers array", layers.length, sortedLayers.length);
        
        assertSame("Wrong order", layers[0], sortedLayers[1]);
        assertSame("Wrong order", layers[1], sortedLayers[0]);
    }

    public void testOrder2() throws Exception {
        ZOrder zOrderA = ZOrder.DEFAULT_RACK.forPosition(1);
        ZOrder zOrderB = ZOrder.DEFAULT_RACK.forPosition(2);
        ZOrder zOrderC = ZOrder.DEFAULT_RACK.forPosition(3);
        ZOrder zOrderD = ZOrder.DEFAULT_RACK.forPosition(4);
        ZOrder zOrderE = ZOrder.DEFAULT_RACK.forPosition(5);
        
        HighlightsLayer [] layers = new HighlightsLayer [] {
            simpleLayer("layerD", zOrderD),
            simpleLayer("layerC", zOrderC),
            simpleLayer("layerA", zOrderA),
            simpleLayer("layerE", zOrderE),
            simpleLayer("layerB", zOrderB),
        };
        
        HighlightsLayer [] sortedLayers = ZOrder.sort(layers);
        
        assertNotNull("Sorted layers should not be null", sortedLayers);
        assertEquals("Wrong size of sortedLayers array", layers.length, sortedLayers.length);

        char ch = 'A';
        for (int i = 0; i < sortedLayers.length; i++) {
            String expectedLayerName = "layer" + ch++;
            assertEquals("Wrong order", expectedLayerName, sortedLayers[i].getLayerTypeId());
        }
    }

    public void testRacks() throws TopologicalSortException {
        HighlightsLayer [] layers = new HighlightsLayer [] {
            simpleLayer("layerE", ZOrder.SHOW_OFF_RACK),
            simpleLayer("layerC", ZOrder.CARET_RACK),
            simpleLayer("layerF", ZOrder.TOP_RACK),
            simpleLayer("layerA", ZOrder.BOTTOM_RACK),
            simpleLayer("layerD", ZOrder.DEFAULT_RACK),
            simpleLayer("layerB", ZOrder.SYNTAX_RACK),
        };
        
        HighlightsLayer [] sortedLayers = ZOrder.sort(layers);
        
        assertNotNull("Sorted layers should not be null", sortedLayers);
        assertEquals("Wrong size of sortedLayers array", layers.length, sortedLayers.length);
        
        char ch = 'A';
        for(int i = 0; i < sortedLayers.length; i++) {
            assertEquals("Wrong order", "layer" + ch, sortedLayers[i].getLayerTypeId());
            ch++;
        }
    }
    
    public void testComplex() throws TopologicalSortException {
        ZOrder zOrderA = ZOrder.BOTTOM_RACK;
        ZOrder zOrderB = ZOrder.DEFAULT_RACK.forPosition(-10);
        ZOrder zOrderC = ZOrder.DEFAULT_RACK;
        ZOrder zOrderD = ZOrder.DEFAULT_RACK.forPosition(10);
        ZOrder zOrderE = ZOrder.TOP_RACK;
        
        HighlightsLayer [] layers = new HighlightsLayer [] {
            simpleLayer("layerD", zOrderD),
            simpleLayer("layerC", zOrderC),
            simpleLayer("layerA", zOrderA),
            simpleLayer("layerE", zOrderE),
            simpleLayer("layerB", zOrderB),
        };
        
        HighlightsLayer [] sortedLayers = ZOrder.sort(layers);
        
        assertNotNull("Sorted layers should not be null", sortedLayers);
        assertEquals("Wrong size of sortedLayers array", layers.length, sortedLayers.length);
        
        char ch = 'A';
        for (int i = 0; i < sortedLayers.length; i++) {
            String expectedLayerName = "layer" + ch++;
            assertEquals("Wrong order", expectedLayerName, sortedLayers[i].getLayerTypeId());
        }
    }

    private HighlightsLayer simpleLayer(String layerId, ZOrder zOrder) {
        return HighlightsLayer.create(layerId, zOrder, true, null);
    }
}
