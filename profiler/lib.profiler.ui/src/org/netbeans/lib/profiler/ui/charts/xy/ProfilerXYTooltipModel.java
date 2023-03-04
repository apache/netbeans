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

package org.netbeans.lib.profiler.ui.charts.xy;

import java.awt.Color;

/**
 *
 * @author Jiri Sedlacek
 */
public interface ProfilerXYTooltipModel {

    public String getTimeValue      (long timestamp);

    public int    getRowsCount      ();
    public String getRowName        (int index);
    public Color  getRowColor       (int index);
    public String getRowValue       (int index, long itemValue);
    public String getRowUnits       (int index);

    public int    getExtraRowsCount ();
    public String getExtraRowName   (int index);
    public Color  getExtraRowColor  (int index);
    public String getExtraRowValue  (int index);
    public String getExtraRowUnits  (int index);

}
