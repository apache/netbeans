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
 *  Enumerated types for Servlet Version
 *
 * @author Peter Williams
 */
public final class ServletVersion extends J2EEBaseVersion {

    /** Represents servlet version 2.3
     */
    public static final ServletVersion SERVLET_2_3 = new ServletVersion(
        "2.3", 2300,	// NOI18N
        "1.3", 1300	// NOI18N
        );

    /** Represents servlet version 2.4
     */
    public static final ServletVersion SERVLET_2_4 = new ServletVersion(
        "2.4", 2401,	// NOI18N
        "1.4", 1400	// NOI18N
        );

    /** Represents servlet version 2.5
     */
    public static final ServletVersion SERVLET_2_5 = new ServletVersion(
        "2.5", 2500,	// NOI18N
        "5.0", 5000	// NOI18N
        );

    /** Represents servlet version 3.0
     */
    public static final ServletVersion SERVLET_3_0 = new ServletVersion(
        "3.0", 3000,	// NOI18N
        "6.0", 6000	// NOI18N
        );
    
    /** Represents servlet version 3.1
     */
    public static final ServletVersion SERVLET_3_1 = new ServletVersion(
        "3.1", 3100,	// NOI18N
        "7.0", 7000	// NOI18N
        );
    
    /** Represents servlet version 4.0
     */
    public static final ServletVersion SERVLET_4_0 = new ServletVersion(
        "4.0", 4000,	// NOI18N
        "8.0", 8000	// NOI18N
        );
    
    /** Represents servlet version 5.0
     */
    public static final ServletVersion SERVLET_5_0 = new ServletVersion(
        "5.0", 5000,	// NOI18N
        "9.0", 9000	// NOI18N
        );
    
    /** Represents servlet version 6.0
     */
    public static final ServletVersion SERVLET_6_0 = new ServletVersion(
        "6.0", 6000,	// NOI18N
        "10.0", 10000	// NOI18N
        );
    
    /** Represents servlet version 6.1
     */
    public static final ServletVersion SERVLET_6_1 = new ServletVersion(
        "6.1", 6100,	// NOI18N
        "11.0", 11000	// NOI18N
        );

    /** -----------------------------------------------------------------------
     *  Implementation
     */

    /** Creates a new instance of ServletVersion 
     */
    private ServletVersion(String version, int nv, String specVersion, int nsv) {
        super(version, nv, specVersion, nsv);
    }

    /** Comparator implementation that works only on ServletVersion objects
     *
     *  @param obj ServletVersion to compare with.
     *  @return -1, 0, or 1 if this version is less than, equal to, or greater
     *     than the version passed in as an argument.
     *  @throws ClassCastException if obj is not a ServletVersion object.
     */
    public int compareTo(Object obj) {
        ServletVersion target = (ServletVersion) obj;
        return numericCompare(target);
    }

    public static ServletVersion getServletVersion(String version) {
        ServletVersion result = null;

        if(SERVLET_2_3.toString().equals(version)) {
            result = SERVLET_2_3;
        } else if(SERVLET_2_4.toString().equals(version)) {
            result = SERVLET_2_4;
        } else if(SERVLET_2_5.toString().equals(version)) {
            result = SERVLET_2_5;
        } else if(SERVLET_3_0.toString().equals(version)) {
            result = SERVLET_3_0;
        } else if(SERVLET_3_1.toString().equals(version)) {
            result = SERVLET_3_1;
        } else if(SERVLET_4_0.toString().equals(version)) {
            result = SERVLET_4_0;
        } else if(SERVLET_5_0.toString().equals(version)) {
            result = SERVLET_5_0;
        } else if(SERVLET_6_0.toString().equals(version)) {
            result = SERVLET_6_0;
        }

        return result;
    }
}
