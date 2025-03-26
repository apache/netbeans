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
package org.netbeans.modules.glassfish.eecommon.api.config;


/**
 *  Enumerated types for Application Client Version
 *
 * @author Peter Williams
 */
public final class AppClientVersion extends J2EEBaseVersion {

    /** Represents application-client version 1.3
     */
    public static final AppClientVersion APP_CLIENT_1_3 = new AppClientVersion(
        "1.3", 1300,	// NOI18N
        "1.3", 1300	// NOI18N
        );

    /** Represents application-client version 1.4
     */
    public static final AppClientVersion APP_CLIENT_1_4 = new AppClientVersion(
        "1.4", 1400,	// NOI18N
        "1.4", 1400	// NOI18N
        );

    /** Represents application-client version 5.0
     */
    public static final AppClientVersion APP_CLIENT_5_0 = new AppClientVersion(
        "5.0", 5000,	// NOI18N
        "5.0", 5000	// NOI18N
        );

    /** Represents application-client version 6.0
     */
    public static final AppClientVersion APP_CLIENT_6_0 = new AppClientVersion(
        "6.0", 6000,	// NOI18N
        "6.0", 6000	// NOI18N
        );
    
    /** Represents application-client version 7.0
     */
    public static final AppClientVersion APP_CLIENT_7_0 = new AppClientVersion(
        "7.0", 7000,	// NOI18N
        "7.0", 7000	// NOI18N
        );
    
    /** Represents application-client version 8.0
     */
    public static final AppClientVersion APP_CLIENT_8_0 = new AppClientVersion(
        "8.0", 8000,	// NOI18N
        "8.0", 8000	// NOI18N
        );
    
    /** Represents application-client version 9.0
     */
    public static final AppClientVersion APP_CLIENT_9_0 = new AppClientVersion(
        "9.0", 9000,	// NOI18N
        "9.0", 9000	// NOI18N
        );
    
    /** Represents application-client version 10.0
     */
    public static final AppClientVersion APP_CLIENT_10_0 = new AppClientVersion(
        "10.0", 10000,	// NOI18N
        "10.0", 10000	// NOI18N
        );
    
    /** Represents application-client version 11.0
     */
    public static final AppClientVersion APP_CLIENT_11_0 = new AppClientVersion(
        "11.0", 11000,	// NOI18N
        "11.0", 11000	// NOI18N
        );
    /** -----------------------------------------------------------------------
     *  Implementation
     */

    /** Creates a new instance of AppClientVersion 
     */
    private AppClientVersion(String version, int nv, String specVersion, int nsv) {
        super(version, nv, specVersion, nsv);
    }

    /** Comparator implementation that works only on AppClientVersion objects
     *
     *  @param obj AppClientVersion to compare with.
     *  @return -1, 0, or 1 if this version is less than, equal to, or greater
     *     than the version passed in as an argument.
     *  @throws ClassCastException if obj is not a AppClientVersion object.
     */
    public int compareTo(Object obj) {
        AppClientVersion target = (AppClientVersion) obj;
        return numericCompare(target);
    }

    public static AppClientVersion getAppClientVersion(String version) {
        AppClientVersion result = APP_CLIENT_6_0;

        if(APP_CLIENT_1_3.toString().equals(version)) {
            result = APP_CLIENT_1_3;
        } else if(APP_CLIENT_1_4.toString().equals(version)) {
            result = APP_CLIENT_1_4;
        } else if(APP_CLIENT_5_0.toString().equals(version)) {
            result = APP_CLIENT_5_0;
        } else if(APP_CLIENT_6_0.toString().equals(version)) {
            result = APP_CLIENT_6_0;
        } else if(APP_CLIENT_7_0.toString().equals(version)) {
            result = APP_CLIENT_7_0;
        } else if(APP_CLIENT_8_0.toString().equals(version)) {
            result = APP_CLIENT_8_0;
        } else if(APP_CLIENT_9_0.toString().equals(version)) {
            result = APP_CLIENT_9_0;
        } else if(APP_CLIENT_10_0.toString().equals(version)) {
            result = APP_CLIENT_10_0;
        } else if(APP_CLIENT_11_0.toString().equals(version)) {
            result = APP_CLIENT_11_0;
        }

        return result;
    }
}
