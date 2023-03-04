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

import java.awt.Rectangle;
import java.util.List;

/**
 *
 * @author Jiri Sedlacek
 */
public interface ChartSelectionModel {

    public static final int SELECTION_NONE = 0;
    public static final int SELECTION_LINE_V = 1;
    public static final int SELECTION_LINE_H = 2;
    public static final int SELECTION_CROSS = 3;
    public static final int SELECTION_RECT = 4;

    public static final int HOVER_NONE = 100;
    public static final int HOVER_NEAREST = 101;
    public static final int HOVER_EACH_NEAREST = 102;

    public static final int HOVER_DISTANCE_LIMIT_NONE = -1;


    // --- Selection mode ------------------------------------------------------

    public void setMoveMode(int mode);

    public int getMoveMode();

    public void setDragMode(int mode);

    public int getDragMode();

    public int getSelectionMode();

    public void setHoverMode(int mode);

    public int getHoverMode();

    public void setHoverDistanceLimit(int limit);

    public int getHoverDistanceLimit();


    // --- Bounds selection ----------------------------------------------------

    public void setSelectionBounds(Rectangle selectionBounds);

    public Rectangle getSelectionBounds();

    
    // --- Items selection -----------------------------------------------------

    public void setHighlightedItems(List<ItemSelection> items);

    public List<ItemSelection> getHighlightedItems();

    public void setSelectedItems(List<ItemSelection> items);

    public List<ItemSelection> getSelectedItems();


    // --- Selection listeners -------------------------------------------------

    public void addSelectionListener(ChartSelectionListener listener);

    public void removeSelectionListener(ChartSelectionListener listener);

}
