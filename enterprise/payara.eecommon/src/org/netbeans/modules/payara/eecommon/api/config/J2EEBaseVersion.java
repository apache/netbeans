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

import org.netbeans.modules.j2ee.deployment.devmodules.api.J2eeModule;


/**
 *  Base class to relate enumerated types of various J2EE/JavaEE versions.
 *
 * @author Peter Williams
 */
public abstract class J2EEBaseVersion implements Comparable {	

    /** -----------------------------------------------------------------------
     *  Implementation
     */
    // This is the module version id, string and numeric form.
    private final String j2eeModuleVersion; // e.g. "2.5" (servlet 2.5), "3.0" (ejb 3.0), etc.
    private final int numericModuleVersion;

    // This is the j2ee/javaee spec version, string and numeric form.
    private final int numericSpecVersion;

    
    /** Creates a new instance of J2EEBaseVersion 
     */
    protected J2EEBaseVersion(String moduleVersion, int nv, String specVersion, int nsv) {
        j2eeModuleVersion = moduleVersion;
        numericModuleVersion = nv;
        numericSpecVersion = nsv;
    }

    /** The string representation of this version.
     *
     * @return String representing the module specification version, e.g. servlet 2.x
     *   ejb-jar 2.x, etc.
     */
    @Override
    public String toString() {
        return j2eeModuleVersion;
    }

    /** Compare the j2ee/javaee spec version of this instance with another (as
     *  opposed to comparing the module type version.
     *
     * @param target Version object to compare with
     * @return -1, 0, 1 if this spec version is less than, equal to, or greater than
     *   the target version.
     */
    public int compareSpecification(J2EEBaseVersion target) {
        if(numericSpecVersion < target.numericSpecVersion) {
            return -1;
        } else if(numericSpecVersion > target.numericSpecVersion) {
            return 1;
        } else {
            return 0;
        }
    }

    /** For use by derived class to compare numeric versions.  Derived class
     *  should ensure target is the appropriate type before invoking this method
     *  to compare the version numbers themselves.
     *
     * @param target Version object to compare with
     * @return -1, 0, 1 if this module version is less than, equal to, or greater than
     *   the target version.
     */
    protected int numericCompare(J2EEBaseVersion target) {
        if(numericModuleVersion < target.numericModuleVersion) {
            return -1;
        } else if(numericModuleVersion > target.numericModuleVersion) {
            return 1;
        } else {
            return 0;
        }
    }

    public static J2EEBaseVersion getVersion(J2eeModule.Type moduleType, String moduleVersion) {
        J2EEBaseVersion version = null;
        if(J2eeModule.Type.WAR.equals(moduleType)) {
            version = ServletVersion.getServletVersion(moduleVersion);
        } else if(J2eeModule.Type.EJB.equals(moduleType)) {
            version = EjbJarVersion.getEjbJarVersion(moduleVersion);
        } else if(J2eeModule.Type.EAR.equals(moduleType)) {
            version = ApplicationVersion.getApplicationVersion(moduleVersion);
        } else if(J2eeModule.Type.CAR.equals(moduleType)) {
            version = AppClientVersion.getAppClientVersion(moduleVersion);
        }
        return version;
    }

    /*
    public static J2EEBaseVersion getJ2EEVersion(String version) {
        J2EEBaseVersion result = null;


        if(J2EE_1_3.toString().equals(version)) {
            result = J2EE_1_3;
        } else if(J2EE_1_4.toString().equals(version)) {
            result = J2EE_1_4;
        }

        return result;
    }
    */
    @Override
    public boolean equals(final Object obj) {
        if (null == obj) {
            return false;
        }
        if (this.getClass().isInstance(obj)) {
            return compareTo(obj) == 0;
        }
        return false;
    }


    @Override
    public int hashCode() {
        return numericSpecVersion;
    }
}
