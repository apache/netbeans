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

import java.awt.*;
import java.util.HashMap;


/**
 *
 * @author  Jiri Sedlacek
 */
public class DecimalAxisUtils {
    //~ Static fields/initializers -----------------------------------------------------------------------------------------------

    private static final long[] timeUnitsGrid = new long[] { 1, 2, 5 };

    //~ Methods ------------------------------------------------------------------------------------------------------------------

    public static long getOptimalUnits(double factor, int minimumVerticalMarksDistance) {
        if (factor <= 0) {
            return 0;
        }

        long decimalFactor = 1;

        while (true) {
            for (int i = 0; i < timeUnitsGrid.length; i++) {
                if ((timeUnitsGrid[i] * decimalFactor * factor) >= minimumVerticalMarksDistance) {
                    return timeUnitsGrid[i] * decimalFactor;
                }
            }

            decimalFactor *= 10;
        }
    }
}
