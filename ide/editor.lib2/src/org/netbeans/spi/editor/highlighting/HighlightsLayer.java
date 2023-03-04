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

import java.util.Collection;
import java.util.List;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;
import org.netbeans.modules.editor.lib2.highlighting.HighlightingSpiPackageAccessor;
import org.netbeans.modules.editor.lib2.highlighting.HighlightsLayerAccessor;
import org.openide.util.TopologicalSortException;

/**
 * The highlight layer defines a set of highlights used for rendering a document.
 * 
 * <p>There can be multiple highlight layers participating in rendering one
 * document. Each highlight layer provides its z-order, which defines an order
 * in which the layers are used. The higher the z-order the more 'visible' the
 * layer is. In other word highlights provided by a layer with higher
 * z-order can hide highlights provided by a layer with lower z-order
 * for the same part of a document.
 *
 * <p>The highlights provided by a <code>HighlightsLayer</code> can
 * define any attributes affecting text rendering including attributes that affect the
 * size of the view containing the rendered text. The layers that use those attributes
 * have to participate in the view layout and therefore need to be treated in a
 * special way. The <code>isFixedSize</code> attribute can be <code>true</code>
 * if and only if layer's highlights will never contain attributes that influence the
 * layout of a view (e.g. font size).
 *
 * <div class="nonnormative">
 * <p>For example attributes changing foreground or background color such as
 * {@link javax.swing.text.StyleConstants#Foreground}
 * or {@link javax.swing.text.StyleConstants#Background}
 * do not affect metrics while the following attributes do affect it
 * {@link javax.swing.text.StyleConstants#FontFamily},
 * {@link javax.swing.text.StyleConstants#FontSize},
 * {@link javax.swing.text.StyleConstants#Bold},
 * {@link javax.swing.text.StyleConstants#Italic}.
 * </div>
 * 
 * @author Miloslav Metelka
 * @author Vita Stejskal
 * @version 1.00
 */

public final class HighlightsLayer {

    /**
     * Creates a new <code>HighlightsLayer</code> with contents defined by
     * <code>HighlightsContainer</code>.
     *
     * @param layerTypeId    The unique identifier of the new layer. This id is
     *   used for identifying this layer among other layers that may be created
     *   for the same <code>Document</code> and it is used for example for
     *   the purpose of z-order.
     * @param zOrder         The layer's z-order.
     * @param fixedSize      Whether this layer defines any attributes
     *    affecting text rendering; including attributes that affect the size
     *    of the view containing the rendered text. Pass in <code>true</code>
     *    <b>if and only if</b> the layer does not change metrics of rendered
     *    text.
     * @param container    The <code>HighlightsContainer</code> that should be used
     *    as a contents of this layer.
     * 
     * @see org.netbeans.spi.editor.highlighting.ZOrder
     * @see org.netbeans.spi.editor.highlighting.HighlightsContainer
     */
    public static HighlightsLayer create(
        String layerTypeId, 
        ZOrder zOrder, 
        boolean fixedSize, 
        HighlightsContainer container
    ) {
        return new HighlightsLayer(layerTypeId, zOrder, fixedSize, container);
    }
    
    private final String layerTypeId;
    private final ZOrder zOrder;
    private final boolean fixedSize;

    private HighlightsContainer container;

    private HighlightsLayerAccessor accessor;
    
    /**
     * Creates a new <code>HighlightsLayer</code> as a proxy to another
     * <code>HighlightsContainer</code>.
     *
     * @param layerTypeId    The unique identifier of the new layer. This id is
     *   used for identifying this layer among other layers that may be created
     *   for the same <code>Document</code> and it is used for example for
     *   the purpose of z-order.
     * @param zOrder         The layer's z-order.
     * @param fixedSize      Whether this layer defines any attributes
     *    affecting text rendering including attributes that affect the size
     *    of the view containing the rendered text.
     * @param container    <code>HighlightsContainer</code> that should be used
     *    as a contents of this layer.
     * 
     * @see org.netbeans.spi.editor.highlighting.ZOrder
     */
    private HighlightsLayer(
        String layerTypeId, 
        ZOrder zOrder, 
        boolean fixedSize, 
        HighlightsContainer container
    ) {
        assert layerTypeId != null : "The layerId parameter must not be null.";
        assert zOrder != null : "The zOrder parameter must not be null.";
        
        this.layerTypeId = layerTypeId;
        this.zOrder = zOrder;
        this.fixedSize = fixedSize;
        this.container = container;
    }
    
    /**
     * Gets the unique identifier of this layer.
     *
     * @return The layer id.
     * @see org.netbeans.spi.editor.highlighting.ZOrder
     */
    /* package */ String getLayerTypeId() {
        return layerTypeId;
    }
    
    /**
     * Gets the z-order of this layer. The z-order defines a relative position
     * of this layer among other layers participating in drawing the same
     * document. The higher the z-order the higher this layer will be placed
     * and the 'more visible' its highlights will be.
     *
     * @return The layer's z-order.
     */
    /* package */ ZOrder getZOrder() {
        return zOrder;
    }
    
    /**
     * Specifies whether highlights from this layer affect size of the rendered text.
     *
     * <p>If this layer provides highlights that change the size
     * of the view containing the rendered text (e.g. the font size) the infrastructure
     * needs to know about it and use the layer for laying out the views. Such
     * a layer should return <code>true</code> from this method.
     *
     * <p>Layers at the top (according to z-order) with highlights
     * that do not change metrics can be skipped during the views creation phase
     * and used later when drawing is done.
     *
     * @return <code>false</code> if the layer does not provide any highlights
     * that would alter the size of the text; otherwise <code>true</code>.
     */
    /* package */ boolean isFixedSize() {
        return fixedSize;
    }
    
    /* package */ HighlightsContainer getContainer() {
        return container;
    }

    @Override
    public String toString() {
        return "HighlightsLayer@" + System.identityHashCode(this) + // NOI18N
                (isFixedSize() ? "(F)" : "") + ":typeId=" + layerTypeId; // NOI18N
    }
    
    static {
        HighlightingSpiPackageAccessor.register(new PackageAccessor());
    }

    private static final class PackageAccessor extends HighlightingSpiPackageAccessor {

        /** Creates a new instance of PackageAccessor */
        public PackageAccessor() {
        }

        public HighlightsLayerFactory.Context createFactoryContext(Document document, JTextComponent component) {
            return new HighlightsLayerFactory.Context(document, component);
        }

        public List<? extends HighlightsLayer> sort(Collection<? extends HighlightsLayer> layers) throws TopologicalSortException {
            return ZOrder.sort(layers);
        }
        
        public HighlightsLayerAccessor getHighlightsLayerAccessor(final HighlightsLayer layer) {
            if (layer.accessor == null) {
                layer.accessor = new HighlightsLayerAccessor() {
                    public String getLayerTypeId() { return layer.getLayerTypeId(); }
                    public boolean isFixedSize() { return layer.isFixedSize(); }
                    public ZOrder getZOrder() { return layer.getZOrder(); }
                    public HighlightsContainer getContainer() { return layer.getContainer(); }
                };
            }
            
            return layer.accessor;
        }
        
        public int getZOrderRack(ZOrder zOrder) {
            return zOrder.getRack();
        }
    } // End of PackageAccessor class
}
