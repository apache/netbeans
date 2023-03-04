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

package org.netbeans.modules.hudson.api;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Describes Hudson version.
 */
public final class HudsonVersion implements Comparable<HudsonVersion> {
    
    private static final Pattern VERSION_PATTERN = Pattern.compile("(\\d+)\\.(\\d+)\\b.*"); // NOI18N

    /**
     * Supported version
     */
    public static final HudsonVersion SUPPORTED_VERSION = new HudsonVersion("1.291"); // NOI18N
    // XXX produce a warning about unsupported features from:
    // <1.294 - no Maven module support
    
    private final int major;
    private final int minor;

    public HudsonVersion(String version) {
        Matcher m = VERSION_PATTERN.matcher(version);
        if (!m.matches()) {
            throw new IllegalArgumentException(version);
        }
        this.major = Integer.parseInt(m.group(1));
        this.minor = Integer.parseInt(m.group(2));
    }

    /**
     * Returns major version
     *
     * @return major version
     */
    public int getMajorVersion() {
        return major;
    }
    
    /**
     * Returns minor version
     *
     * @return minor version
     */
    public int getMinorVersion() {
        return minor;
    }
    
    public @Override String toString() {
        return major + "." + minor; // NOI18N
    }

    public @Override boolean equals(Object o) {
        if (!(o instanceof HudsonVersion)) {
            return false;
        }
        HudsonVersion oV = (HudsonVersion) o;
        return major == oV.major && minor == oV.minor;
    }

    public @Override int hashCode() {
        return toString().hashCode();
    }

    public int compareTo(HudsonVersion o) {
        if (this.equals(o)) {
            return 0;
        }
        return (major < o.major) ? -1 :
            (major > o.major) ? 1 :
                (minor < o.minor) ? -1 :
                    (minor > o.minor) ? 1 : 0;
    }

}
