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
 *  Enumerated types for Application Version
 *
 * @author Peter Williams
 */
public final class ApplicationVersion extends J2EEBaseVersion {

    /** Represents application version 1.3
     */
    public static final ApplicationVersion APPLICATION_1_3 = new ApplicationVersion(
        "1.3", 1300,	// NOI18N
        "1.3", 1300	// NOI18N
        );

    /** Represents application version 1.4
     */
    public static final ApplicationVersion APPLICATION_1_4 = new ApplicationVersion(
        "1.4", 1400,	// NOI18N
        "1.4", 1400	// NOI18N
        );

    /** Represents application version 5.0
     */
    public static final ApplicationVersion APPLICATION_5_0 = new ApplicationVersion(
        "5.0", 5000,	// NOI18N
        "5.0", 5000	// NOI18N
        );

    /** Represents application version 6.0
     */
    public static final ApplicationVersion APPLICATION_6_0 = new ApplicationVersion(
        "6.0", 6000,	// NOI18N
        "6.0", 6000	// NOI18N
        );
    
    /** Represents application version 7.0
     */
    public static final ApplicationVersion APPLICATION_7_0 = new ApplicationVersion(
        "7.0", 7000,	// NOI18N
        "7.0", 7000	// NOI18N
        );
    
    /** Represents application version 8.0
     */
    public static final ApplicationVersion APPLICATION_8_0 = new ApplicationVersion(
        "8.0", 8000,	// NOI18N
        "8.0", 8000	// NOI18N
        );
    
    /** Represents application version 9.0
     */
    public static final ApplicationVersion APPLICATION_9_0 = new ApplicationVersion(
        "9.0", 9000,	// NOI18N
        "9.0", 9000	// NOI18N
        );

    /** Represents application version 10.0
     */
    public static final ApplicationVersion APPLICATION_10_0 = new ApplicationVersion(
        "10.0", 10000,	// NOI18N
        "10.0", 10000	// NOI18N
        );
    
    /** Represents application version 11.0
     */
    public static final ApplicationVersion APPLICATION_11_0 = new ApplicationVersion(
        "11.0", 11000,	// NOI18N
        "11.0", 11000	// NOI18N
        );

    /** -----------------------------------------------------------------------
     *  Implementation
     */

    /** Creates a new instance of ApplicationVersion 
     */
    private ApplicationVersion(String version, int nv, String specVersion, int nsv) {
        super(version, nv, specVersion, nsv);
    }

    /** Comparator implementation that works only on ApplicationVersion objects
     *
     *  @param obj ApplicationVersion to compare with.
     *  @return -1, 0, or 1 if this version is less than, equal to, or greater
     *     than the version passed in as an argument.
     *  @throws ClassCastException if obj is not a ApplicationVersion object.
     */
    public int compareTo(Object obj) {
        ApplicationVersion target = (ApplicationVersion) obj;
        return numericCompare(target);
    }

    public static ApplicationVersion getApplicationVersion(String version) {
        ApplicationVersion result = null;

        if(APPLICATION_1_3.toString().equals(version)) {
            result = APPLICATION_1_3;
        } else if(APPLICATION_1_4.toString().equals(version)) {
            result = APPLICATION_1_4;
        } else if(APPLICATION_5_0.toString().equals(version)) {
            result = APPLICATION_5_0;
        } else if(APPLICATION_6_0.toString().equals(version)) {
            result = APPLICATION_6_0;
        } else if(APPLICATION_7_0.toString().equals(version)) {
            result = APPLICATION_7_0;
        } else if(APPLICATION_8_0.toString().equals(version)) {
            result = APPLICATION_8_0;
        } else if(APPLICATION_9_0.toString().equals(version)) {
            result = APPLICATION_9_0;
        }else if(APPLICATION_10_0.toString().equals(version)) {
            result = APPLICATION_10_0;
        }

        return result;
    }
}
