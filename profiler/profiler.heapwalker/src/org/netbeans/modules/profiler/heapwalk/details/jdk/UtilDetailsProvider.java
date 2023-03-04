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
package org.netbeans.modules.profiler.heapwalk.details.jdk;

import java.util.Date;
import org.netbeans.lib.profiler.heap.Heap;
import org.netbeans.lib.profiler.heap.Instance;
import org.netbeans.modules.profiler.heapwalk.details.spi.DetailsProvider;
import org.netbeans.modules.profiler.heapwalk.details.spi.DetailsUtils;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author Jiri Sedlacek
 */
@ServiceProvider(service=DetailsProvider.class)
public final class UtilDetailsProvider extends DetailsProvider.Basic {
    
    private static final String LOGGER_MASK = "java.util.logging.Logger+";      // NOI18N
    private static final String LEVEL_MASK = "java.util.logging.Level+";        // NOI18N
    private static final String LOCALE_MASK = "java.util.Locale";               // NOI18N
    private static final String DATE_MASK = "java.util.Date+";                  // NOI18N
    private static final String TIMEZONE_MASK = "java.util.TimeZone+";          // NOI18N
    private static final String PATTERN_MASK = "java.util.regex.Pattern";       // NOI18N
    private static final String CURRENCY_MASK = "java.util.Currency";           // NOI18N
    private static final String ZIPENTRY_MASK = "java.util.zip.ZipEntry+";           // NOI18N
    
    public UtilDetailsProvider() {
        super(LOGGER_MASK, LEVEL_MASK, LOCALE_MASK, DATE_MASK, TIMEZONE_MASK,
              PATTERN_MASK, CURRENCY_MASK, ZIPENTRY_MASK);
    }
    
    public String getDetailsString(String className, Instance instance, Heap heap) {
        if (LOGGER_MASK.equals(className) ||
            LEVEL_MASK.equals(className)) {
            return DetailsUtils.getInstanceFieldString(instance, "name", heap); // NOI18N
        } else if (LOCALE_MASK.equals(className)) {
            String language = DetailsUtils.getInstanceFieldString(
                    instance, "language", heap);                                // NOI18N
            if (language == null) language = "";                                // NOI18N
            String country = DetailsUtils.getInstanceFieldString(
                    instance, "country", heap);                                 // NOI18N
            if (country == null) country = "";                                  // NOI18N
            if (!language.isEmpty() || !country.isEmpty()) {
                if (language.isEmpty() || country.isEmpty())
                    return language + country;
                else
                    return language + "_" + country;                            // NOI18N
            }
        } else if (DATE_MASK.equals(className)) {
            long fastTime = DetailsUtils.getLongFieldValue(
                    instance, "fastTime", -1);                                  // NOI18N
            return new Date(fastTime).toString();
        } else if (TIMEZONE_MASK.equals(className)) {
            return DetailsUtils.getInstanceFieldString(
                    instance, "ID", heap);                                      // NOI18N
        } else if (PATTERN_MASK.equals(className)) {
            return DetailsUtils.getInstanceFieldString(
                    instance, "pattern", heap);                                 // NOI18N
        } else if (CURRENCY_MASK.equals(className)) {
            return DetailsUtils.getInstanceFieldString(
                    instance, "currencyCode", heap);                            // NOI18N
        } else if (ZIPENTRY_MASK.equals(className)) {
            String name = DetailsUtils.getInstanceFieldString(
                    instance, "name", heap);                                    // NOI18N
            long size = DetailsUtils.getLongFieldValue(
                    instance, "size", -1);                                      // NOI18N
            if (name != null && size != -1) {
                return String.format("%s, size=%d", name, size);               // NOI18N
            }
            return name;
        }
        return null;
    }
    
}
