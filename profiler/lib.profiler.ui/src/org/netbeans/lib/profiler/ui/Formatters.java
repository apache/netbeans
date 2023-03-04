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

import java.text.Format;
import java.text.MessageFormat;
import java.text.NumberFormat;
import java.util.ResourceBundle;

/**
 *
 * @author Jiri Sedlacek
 */
public final class Formatters {
    
    private static final ResourceBundle BUNDLE = ResourceBundle.getBundle("org.netbeans.lib.profiler.ui.Bundle"); // NOI18N
    
    private static NumberFormat NUMBER_FORMAT;
    /**
     * Returns Format instance for formatting numbers according to current Locale.
     * 
     * @return Format instance for formatting numbers according to current Locale
     */
    public static Format numberFormat() {
        if (NUMBER_FORMAT == null) {
            NUMBER_FORMAT = NumberFormat.getNumberInstance();
            NUMBER_FORMAT.setGroupingUsed(true);
        }
        return NUMBER_FORMAT;
    }
    
    private static NumberFormat PERCENT_FORMAT;
    /**
     * Returns Format instance for formatting percents according to current Locale.
     * 
     * @return Format instance for formatting percents according to current Locale
     */
    public static Format percentFormat() {
        if (PERCENT_FORMAT == null) {
            PERCENT_FORMAT = NumberFormat.getPercentInstance();
            PERCENT_FORMAT.setMaximumFractionDigits(1);
            PERCENT_FORMAT.setMinimumFractionDigits(0);
        }
        return PERCENT_FORMAT;
    }
    
    private static Format MILLISECONDS_FORMAT;
    /**
     * Returns Format instance to post-process a formatted milliseconds value.
     * By default adds a " ms" suffix to a formatted long value.
     * 
     * @return Format instance to post-process a formatted milliseconds value
     */
    public static Format millisecondsFormat() {
        if (MILLISECONDS_FORMAT == null) {
            MILLISECONDS_FORMAT = new MessageFormat(BUNDLE.getString("Formatters.MillisecondsFormat")); // NOI18N
        }
        return MILLISECONDS_FORMAT;
    }
    
    private static Format BYTES_FORMAT;
    /**
     * Returns Format instance to post-process a formatted Bytes (B) value.
     * By default adds a " B" suffix to a formatted long value.
     * 
     * @return Format instance to post-process a formatted Bytes value
     */
    public static Format bytesFormat() {
        if (BYTES_FORMAT == null) {
            BYTES_FORMAT = new MessageFormat(BUNDLE.getString("Formatters.BytesFormat")); // NOI18N
        }
        return BYTES_FORMAT;
    }
    
}
