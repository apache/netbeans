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
package org.netbeans.modules.payara.eecommon.api.config;

/**
 * Enumerated types for various J2EE versions.
 *
 * Be careful with the compareTo method of this class. It is there for comparing
 * like versions (e.g. servlet 2.3 versus 2.4) only, but there is no type safety
 * to prevent doing dumb things like comparing J2EE 1.4 with servlet 2.3.
 *
 * Perhaps I can think of a better design in the next version.
 *
 * @author Peter Williams
 */
public final class J2EEVersion extends J2EEBaseVersion {

    /**
     * Represents J2EE version 1.3
     */
    public static final J2EEVersion J2EE_1_3 = new J2EEVersion(
            "1.3", 1300, // NOI18N
            "1.3", 1300);   // NOI18N

    /**
     * Represents J2EE version 1.4
     */
    public static final J2EEVersion J2EE_1_4 = new J2EEVersion(
            "1.4", 1400, // NOI18N
            "1.4", 1400);   // NOI18N	

    /**
     * Represents JavaEE version 5.0
     */
    public static final J2EEVersion JAVAEE_5_0 = new J2EEVersion(
            "5.0", 5000, // NOI18N
            "5.0", 5000);   // NOI18N	

    /**
     * Represents JavaEE version 6.0
     */
    public static final J2EEVersion JAVAEE_6_0 = new J2EEVersion(
            "6.0", 6000, // NOI18N
            "6.0", 6000);   // NOI18N

    /**
     * Represents JavaEE version 7.0
     */
    public static final J2EEVersion JAVAEE_7_0 = new J2EEVersion(
            "7.0", 7000, // NOI18N
            "7.0", 7000);   // NOI18N

    /**
     * Represents JavaEE version 8.0
     */
    public static final J2EEVersion JAVAEE_8_0 = new J2EEVersion(
            "8.0", 8000, // NOI18N
            "8.0", 8000);   // NOI18N

    /**
     * -----------------------------------------------------------------------
     * Implementation
     */
    /**
     * Creates a new instance of J2EEVersion
     */
    private J2EEVersion(String version, int nv, String specVersion, int nsv) {
        super(version, nv, specVersion, nsv);
    }

    /**
     * Comparator implementation that works only on J2EEVersion objects
     *
     * @param obj J2EEVersion to compare with.
     * @return -1, 0, or 1 if this version is less than, equal to, or greater
     * than the version passed in as an argument.
     * @throws ClassCastException if obj is not a J2EEVersion object.
     */
    public int compareTo(Object obj) {
        J2EEVersion target = (J2EEVersion) obj;
        return numericCompare(target);
    }

    public static J2EEVersion getJ2EEVersion(String version) {
        J2EEVersion result = null;

        if (J2EE_1_3.toString().equals(version)) {
            result = J2EE_1_3;
        } else if (J2EE_1_4.toString().equals(version)) {
            result = J2EE_1_4;
        } else if (JAVAEE_5_0.toString().equals(version)) {
            result = JAVAEE_5_0;
        } else if (JAVAEE_6_0.toString().equals(version)) {
            result = JAVAEE_6_0;
        } else if(JAVAEE_7_0.toString().equals(version)) {
            result = JAVAEE_7_0;
        } else if(JAVAEE_8_0.toString().equals(version)) {
            result = JAVAEE_8_0;
        }

        return result;
    }
}
