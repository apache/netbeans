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
package org.netbeans.modules.cnd.utils;

import java.util.Collections;
import java.util.Map;
import org.netbeans.modules.cnd.spi.utils.UsagesCounterProvider;
import org.openide.util.Lookup;

/**
 *
 */
public final class OSSComponentUsages {

    public static final String IDE = "IDE"; //NOI18N
    public static final String GIZMO = "gizmo"; //NOI18N
    public static final String CODE_ANALYZER = "analytics"; //NOI18N
    public static final String DESKTOP_DISTRIBUTION = "winide"; //NOI18N
    public static final String PROJECT_CREATOR = "createprj"; //NOI18N
    public static final String DBXTOOL = "dbxtool"; //NOI18N
    
    public static final String USED_PROVIDER = "oss_check_update"; //NOI18N
    
    private static UsagesCounterProvider.UsagesCounter counter = null;

    private OSSComponentUsages() {
    }

    public static void countIDEUsage() {
        countUsage(IDE, null, Collections.EMPTY_MAP);
    }

    public static void countIDEFeatureUsage(String feature) {
        countUsage(IDE, feature, Collections.EMPTY_MAP);
    }

    public static void countGizmoUsage() {
        countUsage(GIZMO, null, Collections.EMPTY_MAP);
    }

    public static void countGizmoFeatureUsage(String feature) {
        countUsage(GIZMO, feature, Collections.EMPTY_MAP);
    }

    public static void countCodeAnalyzerUsage() {
        countUsage(CODE_ANALYZER, null, Collections.EMPTY_MAP);
    }

    public static void countCodeAnalyzerFeatureUsage(String feature) {
        countUsage(CODE_ANALYZER, feature, Collections.EMPTY_MAP);
    }

    public static void countDesktopDistributionUsage() {
        countUsage(DESKTOP_DISTRIBUTION, null, Collections.EMPTY_MAP);
    }

    public static void countDesktopDistributionFeatureUsage(String feature) {
        countUsage(DESKTOP_DISTRIBUTION, feature, Collections.EMPTY_MAP);
    }

    public static void countProjectCreatorUsage() {
        countUsage(PROJECT_CREATOR, null, Collections.EMPTY_MAP);
    }

    public static void countProjectCreatorFeatureUsage(String feature) {
        countUsage(PROJECT_CREATOR, feature, Collections.EMPTY_MAP);
    }

    public static void countDbxtoolUsage() {
        countUsage(DBXTOOL, null, Collections.EMPTY_MAP);
    }

    public static void countDbxtoolFeatureUsage(String feature) {
        countUsage(DBXTOOL, feature, Collections.EMPTY_MAP);
    }

    public synchronized static void countUsage(String component, String feature, Map<String, String> additionalInformation) {
        if (counter == null) {
            for (UsagesCounterProvider provider : Lookup.getDefault().lookupAll(UsagesCounterProvider.class)) {
                counter = provider.getUsageCounter(USED_PROVIDER);
                if (counter != null) {
                    break;
                }
            }
        }
        if (counter != null) {
            counter.count(component, feature, additionalInformation);
        }
    }
}
