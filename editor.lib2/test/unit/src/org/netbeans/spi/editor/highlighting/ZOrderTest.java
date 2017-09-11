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
