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
public interface ItemPainter {

    // --- Item bounds ---------------------------------------------------------

    // General item bounds without decorations, data space [0, 0]-based
    public LongRect getItemBounds(ChartItem item);

    // Concrete item bounds for given ChartContext, display space
    public LongRect getItemBounds(ChartItem item, ChartContext context);


    // --- Item change support -------------------------------------------------

    public boolean isBoundsChange(ChartItemChange itemChange);

    public boolean isAppearanceChange(ChartItemChange itemChange);

    public LongRect getDirtyBounds(ChartItemChange itemChange, ChartContext context);


    // --- Item location -------------------------------------------------------

    public boolean supportsHovering(ChartItem item);

    public boolean supportsSelecting(ChartItem item);

    public LongRect getSelectionBounds(ItemSelection selection, ChartContext context);

    public ItemSelection getClosestSelection(ChartItem item, int viewX, int viewY, ChartContext context);


    // --- Item appearance -----------------------------------------------------
    
    public void paintItem(ChartItem item, List<ItemSelection> highlighted,
                          List<ItemSelection> selected, Graphics2D g,
                          Rectangle dirtyArea, ChartContext context);

}
