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

package org.netbeans.lib.profiler.ui;

import java.awt.*;


/** Various UI Constants used in the JFluid UI
 *
 * @author Ian Formanek
 */
public interface UIConstants {
    //~ Static fields/initializers -----------------------------------------------------------------------------------------------

    /** Color used to draw vertical gridlines in JTables */
    public static final Color TABLE_VERTICAL_GRID_COLOR = !UIUtils.isDarkResultsBackground() ?
                              new Color(214, 223, 247) : new Color(84, 93, 117);

    /** if true, results tables display the horizontal grid lines */
    public static final boolean SHOW_TABLE_HORIZONTAL_GRID = false;

    /** if true, results tables display the vertical grid lines */
    public static final boolean SHOW_TABLE_VERTICAL_GRID = true;

    /** Color used for painting selected cell background in JTables */
    public static final Color TABLE_SELECTION_BACKGROUND_COLOR = new Color(193, 210, 238); //(253, 249, 237)

    /** Color used for painting selected cell foreground in JTables */
    public static final Color TABLE_SELECTION_FOREGROUND_COLOR = Color.BLACK;
    public static final int TABLE_ROW_MARGIN = 0;
    
    public static final String PROFILER_PANELS_BACKGROUND = "ProfilerPanels.background"; // NOI18N
}
