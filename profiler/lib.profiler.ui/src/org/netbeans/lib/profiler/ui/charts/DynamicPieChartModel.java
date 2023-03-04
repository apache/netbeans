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
public class DynamicPieChartModel extends AbstractPieChartModel {
    //~ Instance fields ----------------------------------------------------------------------------------------------------------

    protected Color[] itemColors;
    protected String[] itemNames;
    protected double[] itemValues;
    protected double[] itemValuesRel;
    protected boolean hasData = false;
    protected int itemCount = 0;

    //~ Methods ------------------------------------------------------------------------------------------------------------------

    public Color getItemColor(int index) {
        return itemColors[index];
    }

    // --- Abstract PieChartModel ------------------------------------------------
    public int getItemCount() {
        return itemCount;
    }

    public String getItemName(int index) {
        return itemNames[index];
    }

    public double getItemValue(int index) {
        return itemValues[index];
    }

    public double getItemValueRel(int index) {
        return itemValuesRel[index];
    }

    public void setItemValues(double[] itemValues) {
        if (itemValues.length != itemCount) {
            hasData = false;
            throw new RuntimeException("Unexpected number of values."); // NOI18N
        } else {
            this.itemValues = itemValues;
            updateItemValuesRel();
        }

        ;

        fireChartDataChanged();
    }

    public boolean isSelectable(int index) {
        return true;
    }

    public boolean hasData() {
        return hasData;
    }

    public void setupModel(String[] itemNames, Color[] itemColors) {
        this.itemNames = itemNames;
        this.itemColors = itemColors;

        if (itemNames.length != itemColors.length) {
            itemCount = 0;
            throw new RuntimeException("Counts of item names and item colors don't match."); // NOI18N
        } else {
            itemCount = itemNames.length;
        }

        itemValues = null;
        itemValuesRel = new double[itemCount];
        hasData = false;
    }

    // --- Private Implementation ------------------------------------------------

    // computes relative item values
    // O(n) = 2n
    private void updateItemValuesRel() {
        double sum = 0d;

        // compute sum of all item values
        for (int i = 0; i < itemValues.length; i++) {
            sum += itemValues[i];
        }

        // compute new relative item values
        if (sum == 0) {
            for (int i = 0; i < itemValues.length; i++) {
                itemValuesRel[i] = 0;
            }

            hasData = false;
        } else {
            for (int i = 0; i < itemValues.length; i++) {
                itemValuesRel[i] = itemValues[i] / sum;
            }

            hasData = true;
        }
    }
}
