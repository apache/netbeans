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

import java.util.Collection;
import java.util.concurrent.CopyOnWriteArraySet;

/**
 *
 * @author Jiri Sedlacek
 */
public abstract class AbstractBarChartModel implements BarChartModel {
    //~ Instance fields ----------------------------------------------------------------------------------------------------------

    private Collection<ChartModelListener> listeners = new CopyOnWriteArraySet<ChartModelListener>(); // Data change listeners

    //~ Constructors -------------------------------------------------------------------------------------------------------------

    /** Creates a new instance of AbstractBarChartModel */
    public AbstractBarChartModel() {
    }

    //~ Methods ------------------------------------------------------------------------------------------------------------------

    public abstract String getXAxisDesc();

    // --- Abstract BarChartModel ------------------------------------------------
    public abstract String[] getXLabels();

    public abstract String getYAxisDesc();

    public abstract int[] getYValues();

    // ---------------------------------------------------------------------------

    // --- Listeners -------------------------------------------------------------

    /**
     * Adds new ChartModel listener.
     * @param listener ChartModel listener to add
     */
    public synchronized void addChartModelListener(ChartModelListener listener) {
        listeners.add(listener);
    }

    /**
     * Removes ChartModel listener.
     * @param listener ChartModel listener to remove
     */
    public synchronized void removeChartModelListener(ChartModelListener listener) {
        listeners.remove(listener);
    }

    /**
     * Notifies all listeners about the data change.
     */
    protected void fireChartDataChanged() {
        for(ChartModelListener l : listeners) {
            l.chartDataChanged();
        }
    }

    // ---------------------------------------------------------------------------
}
