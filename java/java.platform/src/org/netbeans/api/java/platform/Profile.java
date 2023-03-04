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
package org.netbeans.api.java.platform;

import org.openide.modules.SpecificationVersion;

/**
 * Represents profile installed in the Java SDK
 */
public class Profile {

    private String name;
    private SpecificationVersion version;

    /**
     * Creates new Profile
     * @param name of the profile, e.g. MIDP
     * @param version of the profile, e.g. 1.0
     */
    public Profile (String name, SpecificationVersion version) {
        this.name = name;
        this.version = version;
    }

    /**
     * Returns the name of the profile
     * @return String
     */
    public final String getName () {
        return this.name;
    }

    /**
     * Returns the version of the profile
     * @return String
     */
    public final SpecificationVersion getVersion () {
        return this.version;
    }


    public int hashCode () {
        int hc = 0;
        if (name != null)
            hc = name.hashCode() << 16;
        if (version != null)
            hc += version.hashCode();
        return hc;
    }

    public boolean equals (Object other) {
        if (other instanceof Profile) {
            Profile op = (Profile) other;
            return this.name == null ? op.name == null : this.name.equals(op.name) &&
                   this.version == null ? op.version == null : this.version.equals (op.version);
        }
        else
            return false;
    }

    public String toString () {
        String str;
        str = this.name == null ? "" : this.name;
        str += " " + this.version == null ? "" : this.version.toString(); // NOI18N
        return str;
    }
}
