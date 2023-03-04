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

package org.netbeans.lib.profiler.ui.charts;

import java.awt.Color;


/**
 *
 * @author Jiri Sedlacek
 */
public interface PieChartModel {
    //~ Methods ------------------------------------------------------------------------------------------------------------------

    public Color getItemColor(int index); // color of item

    public int getItemCount(); // number of displayed (processed) items

    public String getItemName(int index); // name of item

    public double getItemValue(int index); // value of item

    public double getItemValueRel(int index); // relative item value (<0, 1>, E(items) = 1)

    public boolean isSelectable(int index); // is the given item ready to be selected?

    /**
     * Adds new ChartModel listener.
     * @param listener ChartModel listener to add
     */
    public void addChartModelListener(ChartModelListener listener);

    public boolean hasData(); // does the model contain some non-zero item?

    /**
     * Removes ChartModel listener.
     * @param listener ChartModel listener to remove
     */
    public void removeChartModelListener(ChartModelListener listener);
}
