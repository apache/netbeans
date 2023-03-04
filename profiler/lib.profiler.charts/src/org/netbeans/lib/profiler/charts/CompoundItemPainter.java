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

package org.netbeans.lib.profiler.charts;

import org.netbeans.lib.profiler.charts.swing.LongRect;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.util.List;

/**
 *
 * @author Jiri Sedlacek
 */
public class CompoundItemPainter implements ItemPainter {
    
    private ItemPainter painter1;
    private ItemPainter painter2;


    public CompoundItemPainter(ItemPainter painter1, ItemPainter painter2) {
        this.painter1 = painter1;
        this.painter2 = painter2;
    }


    public LongRect getItemBounds(ChartItem item) {
        LongRect itemBounds = painter1.getItemBounds(item);
        LongRect.add(itemBounds, painter2.getItemBounds(item));
        return itemBounds;
    }

    public LongRect getItemBounds(ChartItem item, ChartContext context) {
        LongRect itemBounds = painter1.getItemBounds(item, context);
        LongRect.add(itemBounds, painter2.getItemBounds(item, context));
        return itemBounds;
    }


    public boolean isBoundsChange(ChartItemChange itemChange) {
        return painter1.isBoundsChange(itemChange) ||
               painter2.isBoundsChange(itemChange);
    }

    public boolean isAppearanceChange(ChartItemChange itemChange) {
        return painter1.isAppearanceChange(itemChange) ||
               painter2.isAppearanceChange(itemChange);
    }

    public LongRect getDirtyBounds(ChartItemChange itemChange, ChartContext context) {
        LongRect dirtyBounds = painter1.getDirtyBounds(itemChange, context);
        LongRect.add(dirtyBounds, painter2.getDirtyBounds(itemChange, context));
        return dirtyBounds;
    }
    

    public boolean supportsHovering(ChartItem item) {
        return painter1.supportsHovering(item) || painter2.supportsHovering(item);
    }

    public boolean supportsSelecting(ChartItem item) {
        return painter1.supportsSelecting(item) || painter2.supportsSelecting(item);
    }

    public LongRect getSelectionBounds(ItemSelection selection, ChartContext context) {
        LongRect bounds1 = painter1.supportsHovering(selection.getItem()) ? painter1.getSelectionBounds(selection, context) : new LongRect();
        LongRect bounds2 = painter2.supportsHovering(selection.getItem()) ? painter2.getSelectionBounds(selection, context) : new LongRect();
        LongRect.add(bounds1, bounds2);
        return bounds1;
    }

    public ItemSelection getClosestSelection(ChartItem item, int viewX, int viewY, ChartContext context) {
        ItemSelection selection1 = painter1.supportsHovering(item) ? painter1.getClosestSelection(item, viewX, viewY, context) : null;
        ItemSelection selection2 = painter2.supportsHovering(item) ? painter2.getClosestSelection(item, viewX, viewY, context) : null;

        if (selection1 == null) return selection2;
        else if (selection2 == null) return selection1;
        else if (selection1.getDistance() < selection2.getDistance()) return selection1;
        else return selection2;
    }


    public void paintItem(ChartItem item, List<ItemSelection> highlighted, List<ItemSelection> selected, Graphics2D g, Rectangle dirtyArea, ChartContext context) {
        painter1.paintItem(item, highlighted, selected, g, dirtyArea, context);
        painter2.paintItem(item, highlighted, selected, g, dirtyArea, context);
    }


    protected ItemPainter getPainter1() {
        return painter1;
    }

    protected ItemPainter getPainter2() {
        return painter2;
    }

}
