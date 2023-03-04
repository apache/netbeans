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

package org.netbeans.modules.websvc.wsstack.api;

/** Provides WS Stack Version information. 
 * WSStackVersion consists of 4 numbers.
 * Example: Version "1.7.0.3-b32 is parsed in a following way :
 * major = 1, minor = 7, micro = 0, update = 3 (the last part is not important)
 *
 * @author mkuchtiak
 */
public final class WSStackVersion implements Comparable<WSStackVersion> {
    private final int major, minor, micro, update;
    
    /** Constructor for WSStackVersion.
     * The constructor is only be used by valueOf method.
     * 
     */
    private WSStackVersion(int major, int minor, int micro, int update) {
        this.major = major;
        this.minor = minor;
        this.micro = micro;
        this.update = update;
    }
    
    /** Major version number of WS Stack. It's the first version number.
     * 
     * @return major(first) version part
     */

    public int getMajor() {
        return major;
    }
    
    /** Minor version number of WS Stack. It's the second version number.
     * 
     * @return minor(second) version part
     */
    public int getMinor() {
        return minor;
    }
    
    /** Micro version number of WS Stack. It's the third version number.
     * 
     * @return micro(third) version part
     */
    public int getMicro() {
        return micro;
    }
    
    /** Update version number of WS Stack. It's the fourth version number.
     * 
     * @return update(fourth) version part
     */
    public int getUpdate() {
        return update;
    }
    
    /** Get WSStackVersion from 4 numbers.
     * Usage: WSStackVersion version = WSStackVersion.valueOf(2,1,3,0); // version "2.1.3"
     * 
     * @param major "major" part of version
     * @param minor "minor" part of version
     * @param micro "micro" part of version
     * @param update "update" part of version
     * @return WSStackVersion object
     */
    public static WSStackVersion valueOf(int major, int minor, int micro, int update) {
        if (major < 0 || minor < 0 || micro  < 0 || update < 0) {
            throw new IllegalArgumentException("Negative version number");
        }
        return new WSStackVersion(major, minor, micro, update);
    }

    /** Compare two versions.
     *
     * if v1 == v2, return 0.
     * if v1 &lt; v2, return a negative number.
     * if v1 &gt; v2, return a positive number.
     * 
     * @param v2 version to compare
     * @return 0(if equals), 1(if greater). -1(if less) 
     */
    public int compareTo(WSStackVersion v2) {

        if (v2 == null) {
            throw new IllegalArgumentException("Cannot pass null as parameter of WSStackVersion.compareTo(WSStackVersion)"); //NOI18N
        }
        // Compare identity
        if (this == v2) return 0;
        
        // Compare major version
        int result = this.major - v2.major;
        if (result != 0) return result/Math.abs(result);
        
        // Compare minor version
        result = this.minor - v2.minor;
        if (result != 0) return result/Math.abs(result);
        
        // Compare micro version
        result = this.micro - v2.micro;
        if (result != 0) return result/Math.abs(result);
        
        // Compare update version
        result = this.update - v2.update;
        if (result != 0) return result/Math.abs(result);
        else return 0;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final WSStackVersion other = (WSStackVersion) obj;
        if (this.major == other.major && this.minor == other.minor && this.micro == other.micro && this.update == other.update) {
            return true;
        }
        return false;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 73 * hash + this.major;
        hash = 73 * hash + this.minor;
        hash = 73 * hash + this.micro;
        hash = 73 * hash + this.update;
        return hash;
    }

    @Override
    public String toString() {
        return String.valueOf(major)+"."+ //NOI18N
               String.valueOf(minor)+"."+ //NOI18N
               String.valueOf(micro)+"."+ //NOI18N
               String.valueOf(update); //NOI18N
    }

    
}
    
