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

package org.netbeans.installer.utils;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 *
 * @author Kirill Sorokin
 */
public final class DateUtils {
    /////////////////////////////////////////////////////////////////////////////////
    // Static
    public static String getTimestamp() {
        return COMPACT_TIMESTAMP.format(new Date());
    }
    
    public static String getFormattedTimestamp() {
        return DETAILED_TIMESTAMP.format(new Date());
    }
    
    /////////////////////////////////////////////////////////////////////////////////
    // Instance
    private DateUtils() {
        // does nothing
    }
    
    /////////////////////////////////////////////////////////////////////////////////
    // Constants
    public static final DateFormat COMPACT_TIMESTAMP =
            new SimpleDateFormat("yyyyMMddHHmmss"); // NOI18N
    
    public static final DateFormat DETAILED_TIMESTAMP =
            new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS"); // NOI18N
}
