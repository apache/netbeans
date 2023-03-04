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

package org.netbeans.modules.bugzilla.util;

import org.eclipse.mylyn.internal.bugzilla.core.IBugzillaConstants;

/**
 *
 * @author Tomas Stupka
 */
public interface BugzillaConstants {
    public static final String URL_ADVANCED_BUG_LIST = IBugzillaConstants.URL_BUGLIST + "?query_format=advanced"; //NOI18N
    public static final String URL_BUG_IDS = IBugzillaConstants.URL_BUGLIST + "?bug_id="; //NOI18N
    public static final String URL_SHOW_BUG = IBugzillaConstants.URL_GET_SHOW_BUG;
    public static final String QUERY_COLUMN_LIST = "&columnlist=bug_severity%2Cpriority%2Cbug_status%2Cresolution%2Cshort_desc%2Cchangeddate"; //NOI18N

    public static final String MY_ISSUES_PARAMETERS_FORMAT =
            "&product={0}" +                                                    // NOI18N
            "&bug_status=UNCONFIRMED" +                                         // NOI18N
            "&bug_status=NEW" +                                                 // NOI18N
            "&bug_status=ASSIGNED" +                                            // NOI18N
            "&bug_status=REOPENED" +                                            // NOI18N
            "&emailassigned_to1=1" +                                            // NOI18N
            "&emailreporter1=1" +                                               // NOI18N
            "&emailtype1=exact" +                                               // NOI18N
            "&email1={1}";                                                      // NOI18N

    public static final String NB_MY_ISSUES_PARAMETERS_FORMAT =
            "&product={0}" +                                                    // NOI18N
            "&bug_status=NEW" +                                                 // NOI18N
            "&bug_status=STARTED" +                                             // NOI18N
            "&bug_status=REOPENED" +                                            // NOI18N
            "&emailassigned_to1=1" +                                            // NOI18N
            "&emailreporter1=1" +                                               // NOI18N
            "&emailtype1=exact" +                                               // NOI18N
            "&email1={1}";                                                      // NOI18N

    public static final String ALL_ISSUES_PARAMETERS =
            "&product={0}" +                                                    // NOI18N
            "&bug_status=UNCONFIRMED" +                                         // NOI18N
            "&bug_status=NEW" +                                                 // NOI18N
            "&bug_status=ASSIGNED" +                                            // NOI18N
            "&bug_status=REOPENED";                                             // NOI18N

    public static final String DEFAULT_STATUS_PARAMETERS =
            "&bug_status=NEW" +                                                 // NOI18N
            "&bug_status=ASSIGNED" +                                            // NOI18N
            "&bug_status=REOPENED";                                             // NOI18N

    public static final String DEFAULT_NB_STATUS_PARAMETERS =
            "&bug_status=NEW" +                                                 // NOI18N
            "&bug_status=STARTED" +                                             // NOI18N
            "&bug_status=REOPENED";                                             // NOI18N

}
