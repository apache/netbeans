/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.netbeans.installer.utils;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 *
 * @author Kirill Sorokin
 */
public final class DateUtils {

    /////////////////////////////////////////////////////////////////////////////////
    // Constants
    public static final DateTimeFormatter COMPACT_TIMESTAMP = DateTimeFormatter.ofPattern("yyyyMMddHHmmss"); // NOI18N
    public static final DateTimeFormatter DETAILED_TIMESTAMP = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS"); // NOI18N

    /////////////////////////////////////////////////////////////////////////////////
    // Static
    public static String getTimestamp() {
        return COMPACT_TIMESTAMP.format(LocalDateTime.now());
    }
    
    public static String getFormattedTimestamp() {
        return DETAILED_TIMESTAMP.format(LocalDateTime.now());
    }
    
    /////////////////////////////////////////////////////////////////////////////////
    // Instance
    private DateUtils() {
        // does nothing
    }

}
