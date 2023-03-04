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

package org.netbeans.modules.profiler.snaptracer;

import java.text.NumberFormat;

/**
 * This class is responsible for formatting item values in the UI. In the current
 * version it formats values for chart tooltips, chart units (min/max values) and
 * details table.
 *
 * @author Jiri Sedlacek
 */
public abstract class ItemValueFormatter {

    /**
     * Code for tooltip formatting.
     */
    public static final int FORMAT_TOOLTIP = 0;
    /**
     * Code for units (min/max values) formatting.
     */
    public static final int FORMAT_UNITS = 1;
    /**
     * Code for details table formatting.
     */
    public static final int FORMAT_DETAILS = 2;
    /**
     * Code for exported data formatting.
     */
    public static final int FORMAT_EXPORT = 3;

    /**
     * Predefined formatter providing simple numeric values.
     * Uses Number.getInstance().toString().
     */
    public static final ItemValueFormatter SIMPLE = new ItemValueFormatter() {
        public String formatValue(long value, int format) {
            return Long.toString(value);
        }
        public String getUnits(int format) {
            return null;
        }
    };

    /**
     * Predefined formatter for decimal values with custom units.
     * Uses Number.getInstance().toString().
     */
    public static final ItemValueFormatter DEFAULT_DECIMAL = new Decimal();
    /**
     * Predefined formatter for memory values. Uses B (Bytes) for tooltip,
     * details table and export, uses MB for units (min/max values).
     * Uses Number.getInstance().toString().
     */
    public static final ItemValueFormatter DEFAULT_BYTES = new Bytes();
    /**
     * Predefined formatter for percent values with custom factor.
     * Uses Number.getPercentInstance().toString().
     */
    public static final ItemValueFormatter DEFAULT_PERCENT = new Percent();


    /**
     * Returns value formatted in the requested format.
     *
     * @param value value to be formatted
     * @param format format to be used
     * @return value formatted in the requested format
     */
    public abstract String formatValue(long value, int format);

    /**
     * Returns value units for the requested format.
     *
     * @param format format to be used
     * @return value units for the requested format or null for no units
     */
    public abstract String getUnits(int format);


    /**
     * Predefined formatter for decimal values with custom factor and units.
     * Uses Number.getInstance().toString().
     */
    public static final class Decimal extends ItemValueFormatter {
        
        private static final NumberFormat FORMAT = NumberFormat.getInstance();

        private final int factor;
        private final String units;


        Decimal() {
            this(1, null);
        }

        /**
         * Creates new instance of Decimal formatter with the defined units.
         * The values are computed as value / factor.
         *
         * @param factor factor for computing values
         * @param units units
         */
        public Decimal(int factor, String units) {
            this.factor = factor;
            this.units = units;
        }


        public String formatValue(long value, int format) {
            return FORMAT.format(value / factor);
        }
        
        public String getUnits(int format) {
            return units;
        }
        
    }


    /**
     * Predefined formatter for memory values. Uses B (Bytes) for tooltip,
     * details table and export, uses MB for units (min/max values).
     * Uses Number.getInstance().toString().
     */
    private static final class Bytes extends ItemValueFormatter {

        private static final NumberFormat FORMAT = NumberFormat.getInstance();


        Bytes() {}


        public String formatValue(long value, int format) {
            switch (format) {
                case FORMAT_TOOLTIP:
                case FORMAT_DETAILS:
                case FORMAT_EXPORT:
                    return FORMAT.format(value);
                case FORMAT_UNITS:
                    String est = value == 0 ? "" : "~";
                    return est + FORMAT.format(Math.round(value / 1024 / 1024));
                default:
                    return null;
            }
        }
        
        public String getUnits(int format) {
            switch (format) {
                case FORMAT_TOOLTIP:
                case FORMAT_DETAILS:
                case FORMAT_EXPORT:
                    return "B";
                case FORMAT_UNITS:
                    return "MB";
                default:
                    return null;
            }
        }
        
    }


    /**
     * Predefined formatter for percent values with custom factor.
     * Uses Number.getPercentInstance().toString().
     */
    public static final class Percent extends ItemValueFormatter {

        private static final NumberFormat PERCENT_FORMAT;
        private static final NumberFormat NUMBER_FORMAT;

        static {
            PERCENT_FORMAT = NumberFormat.getPercentInstance();
            PERCENT_FORMAT.setMinimumFractionDigits(1);
            PERCENT_FORMAT.setMaximumIntegerDigits(3);
            NUMBER_FORMAT = NumberFormat.getInstance();
            NUMBER_FORMAT.setMinimumFractionDigits(1);
            NUMBER_FORMAT.setMaximumIntegerDigits(3);
        }
        
        private double factor;


        Percent() {
            this(3);
        }

        /**
         * Creates new instance of Percent formatter with the defined decimal
         * exponent. The values are computed as value / Math.pow(10, decexp).
         *
         * @param decexp decimal exponent for computing values
         */
        public Percent(int decexp) {
            factor = Math.pow(10, decexp);
        }


        public String formatValue(long value, int format) {
            switch (format) {
                case FORMAT_TOOLTIP:
                case FORMAT_UNITS:
                    return PERCENT_FORMAT.format(value / factor);
                case FORMAT_DETAILS:
                case FORMAT_EXPORT:
                    return NUMBER_FORMAT.format(value * 100 / factor);
                default:
                    return null;
            }
        }
        
        public String getUnits(int format) {
            switch (format) {
                case FORMAT_TOOLTIP:
                case FORMAT_UNITS:
                    return null; // '%' provided by NumberFormat.getPercentInstance()
                case FORMAT_DETAILS:
                case FORMAT_EXPORT:
                    return "%"; // '%' is part of column header
                default:
                    return null;
            }
        }

    }

}
