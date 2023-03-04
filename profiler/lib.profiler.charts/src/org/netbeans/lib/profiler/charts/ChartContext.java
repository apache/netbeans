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

/**
 *
 * @author Jiri Sedlacek
 */
public interface ChartContext {

    // --- Chart orientation ---------------------------------------------------

    public boolean isRightBased();

    public boolean isBottomBased();


    // --- Fixed scale ---------------------------------------------------------

    public boolean fitsWidth();

    public boolean fitsHeight();


    // --- Chart bounds --------------------------------------------------------

    public long getDataOffsetX();

    public long getDataOffsetY();

    public long getDataWidth();

    public long getDataHeight();

    public long getViewWidth();

    public long getViewHeight();


    // --- Viewport bounds -----------------------------------------------------

    public long getViewportOffsetX();

    public long getViewportOffsetY();

    public int getViewportWidth();

    public int getViewportHeight();


    // --- Data to View --------------------------------------------------------

    public double getViewX(double dataX);

    public double getReversedViewX(double dataX);

    public double getViewY(double dataY);

    public double getReversedViewY(double dataY);

    public double getViewWidth(double dataWidth);

    public double getViewHeight(double dataHeight);

    public LongRect getViewRect(LongRect dataRect);

//    public LongRect getReversedViewRect(LongRect dataRect);


    // --- View to Data --------------------------------------------------------

    public double getDataX(double viewX);

    public double getReversedDataX(double viewX);

    public double getDataY(double viewY);

    public double getReversedDataY(double viewY);

    public double getDataWidth(double viewWidth);

    public double getDataHeight(double viewHeight);

//    public LongRect getDataRect(LongRect viewRect);
//
//    public LongRect getReversedDataRect(LongRect viewRect);

}
