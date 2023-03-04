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

package org.netbeans.lib.profiler.charts.axis;

import java.text.MessageFormat;
import java.text.NumberFormat;
import java.util.ResourceBundle;

/**
 *
 * @author Jiri Sedlacek
 */
public class BytesAxisUtils {

    // -----
    // I18N String constants
    private static final ResourceBundle messages = ResourceBundle.getBundle("org.netbeans.lib.profiler.charts.axis.Bundle"); // NOI18N
    public static final String UNITS_B = messages.getString("BytesAxisUtils_AbbrBytes"); // NOI18N
    public static final String UNITS_KB = messages.getString("BytesAxisUtils_AbbrKiloBytes"); // NOI18N
    public static final String UNITS_MB = messages.getString("BytesAxisUtils_AbbrMegaBytes"); // NOI18N
    public static final String UNITS_GB = messages.getString("BytesAxisUtils_AbbrGigaBytes"); // NOI18N
    public static final String UNITS_TB = messages.getString("BytesAxisUtils_AbbrTeraBytes"); // NOI18N
    public static final String UNITS_PB = messages.getString("BytesAxisUtils_AbbrPetaBytes"); // NOI18N
    private static final String SIZE_FORMAT = messages.getString("BytesAxisUtils_SizeFormat"); // NOI18N
    // -----

    public static final long[] bytesUnitsGrid = new long[] { 1, 2, 5, 10, 25, 50, 100, 250, 500 };
    public static final String[] radixUnits = new String[] { UNITS_B, UNITS_KB, UNITS_MB, UNITS_GB, UNITS_TB, UNITS_PB };

    private static final NumberFormat FORMAT = NumberFormat.getInstance();

    public static long[] getBytesUnits(double scale, int minDistance) {
        if (Double.isNaN(scale) || scale == Double.POSITIVE_INFINITY || scale <= 0)
            return new long[] { -1, -1 };

        long bytesFactor = 1;
        long bytesRadix  = 0;

        while (true) {
            for (int i = 0; i < bytesUnitsGrid.length; i++)
                if ((bytesUnitsGrid[i] * scale * bytesFactor) >= minDistance)
                    return new long[] { bytesUnitsGrid[i] * bytesFactor, bytesRadix };

            bytesFactor *= 1024;
            bytesRadix  += 1;
        }
    }

    public static String getRadixUnits(BytesMark mark) {
        int radix = mark.getRadix();
        if (radix < 0 || radix >= radixUnits.length) return ""; // NOI18N
        return radixUnits[radix];
    }

    public static String formatBytes(BytesMark mark) {
        int radix = mark.getRadix();
        long value = mark.getValue() / (long)Math.pow(1024, radix);
        String units = getRadixUnits(mark);

        return MessageFormat.format(SIZE_FORMAT, new Object[] { FORMAT.format(value), units });
    }

}
