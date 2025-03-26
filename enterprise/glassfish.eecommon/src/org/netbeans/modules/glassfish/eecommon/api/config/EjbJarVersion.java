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
 *  Enumerated types for EjbJar Version
 *
 * @author Peter Williams
 */
public final class EjbJarVersion extends J2EEBaseVersion {

    /**
     * Represents ejbjar version 2.0
     */
    public static final EjbJarVersion EJBJAR_2_0 = new EjbJarVersion(
            "2.0", 2000, // NOI18N
            "1.3", 1300 // NOI18N
    );

    /**
     * Represents ejbjar version 2.1
     */
    public static final EjbJarVersion EJBJAR_2_1 = new EjbJarVersion(
            "2.1", 2101, // NOI18N
            "1.4", 1400 // NOI18N
    );

    /**
     * Represents ejbjar version 3.0
     */
    public static final EjbJarVersion EJBJAR_3_0 = new EjbJarVersion(
            "3.0", 3000, // NOI18N
            "5.0", 5000 // NOI18N
    );

    /**
     * Represents ejbjar version 3.1
     */
    public static final EjbJarVersion EJBJAR_3_1 = new EjbJarVersion(
            "3.1", 3100, // NOI18N
            "6.0", 6000 // NOI18N
    );

    /**
     * Represents ejbjar version 3.2
     */
    public static final EjbJarVersion EJBJAR_3_2 = new EjbJarVersion(
            "3.2", 3200, // NOI18N
            "7.0", 7000 // NOI18N
    );

    /**
     * Represents ejbjar version 3.2.6
     */
    public static final EjbJarVersion EJBJAR_3_2_6 = new EjbJarVersion(
            "3.2.6", 3260, // NOI18N
            "8.0", 8000 // NOI18N
    );

    /**
     * Represents ejbjar version 4.0
     */
    public static final EjbJarVersion EJBJAR_4_0 = new EjbJarVersion(
            "4.0", 4000, // NOI18N
            "9.0", 9000 // NOI18N
    );
    
    /**
     * Represents ejbjar version 4.0.1
     */
    public static final EjbJarVersion EJBJAR_4_0_1 = new EjbJarVersion(
            "4.0.1", 4010, // NOI18N
            "10.0", 10000 // NOI18N
    );
    /** -----------------------------------------------------------------------
     *  Implementation
     */

    /** Creates a new instance of EjbJarVersion 
     */
    private EjbJarVersion(String moduleVersion, int nv, String specVersion, int nsv) {
        super(moduleVersion, nv, specVersion, nsv);
    }

    /** Comparator implementation that works only on EjbJarVersion objects
     *
     *  @param obj EjbJarVersion to compare with.
     *  @return -1, 0, or 1 if this version is less than, equal to, or greater
     *     than the version passed in as an argument.
     *  @throws ClassCastException if obj is not a EjbJarVersion object.
     */
    @Override
    public int compareTo(Object obj) {
        EjbJarVersion target = (EjbJarVersion) obj;
        return numericCompare(target);
    }

    public static EjbJarVersion getEjbJarVersion(String version) {
        EjbJarVersion result = null;

        if(EJBJAR_2_0.toString().equals(version)) {
            result = EJBJAR_2_0;
        } else if(EJBJAR_2_1.toString().equals(version)) {
            result = EJBJAR_2_1;
        } else if(EJBJAR_3_0.toString().equals(version)) {
            result = EJBJAR_3_0;
        } else if(EJBJAR_3_1.toString().equals(version)) {
            result = EJBJAR_3_1;
        } else if(EJBJAR_3_2.toString().equals(version)) {
            result = EJBJAR_3_2;
        } else if(EJBJAR_3_2_6.toString().equals(version)) {
            result = EJBJAR_3_2_6;
        } else if(EJBJAR_4_0.toString().equals(version)) {
            result = EJBJAR_4_0;
        }

        return result;
    }
}
