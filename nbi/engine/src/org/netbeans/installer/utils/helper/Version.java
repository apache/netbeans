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

package org.netbeans.installer.utils.helper;

/**
 *
 * @author Kirill Sorokin
 */
public class Version {
    /////////////////////////////////////////////////////////////////////////////////
    // Static
    public static Version getVersion(
            final String string) {
        if (string!=null && string.matches("([0-9]+[\\._\\-]+)*[0-9]+")) {
            return new Version(string);
        } else {
            return null;
        }
    }
    
    /////////////////////////////////////////////////////////////////////////////////
    // Instance
    private long major;
    private long minor;
    private long micro;
    private long update;
    private long build;
    
    private Version(final String string) {
        String[] split = string.split("[\\._\\-]+"); //NOI18N

        if (split.length > 0) {
            major = Long.parseLong(split[0]);
        }
        if (split.length > 1) {
            minor = Long.parseLong(split[1]);
        }
        if (split.length > 2) {
            micro = Long.parseLong(split[2]);
        }
        if (split.length > 3) {
            update = Long.parseLong(split[3]);
        }
        if (split.length > 4) {
            build = Long.parseLong(split[4]);
        }
    }
    
    public boolean equals(
            final Version version) {
        return ((major == version.major) &&
                (minor == version.minor) &&
                (micro == version.micro) &&
                (update == version.update) &&
                (build == version.build));
    }
    
    public boolean newerThan(
            final Version version) {
        if (major > version.major) {
            return true;
        } else if (major == version.major) {
            if (minor > version.minor) {
                return true;
            } else if (minor == version.minor) {
                if (micro > version.micro) {
                    return true;
                } else if (micro == version.micro) {
                    if (update > version.update) {
                        return true;
                    } else if (update == version.update) {
                        if (build > version.build) {
                            return true;
                        }
                    }
                }
            }
        }
        
        return false;
    }
    
    public boolean newerOrEquals(
            final Version version) {
        return newerThan(version) || equals(version);
    }
    
    public boolean olderThan(
            final Version version) {
        return !newerOrEquals(version);
    }
    
    public boolean olderOrEquals(
            final Version version) {
        return !newerThan(version);
    }
    
    public VersionDistance getDistance(
            final Version version) {
        return new VersionDistance(this, version);
    }
    
    public long getMajor() {
        return major;
    }
    
    public long getMinor() {
        return minor;
    }
    
    public long getMicro() {
        return micro;
    }
    
    public long getUpdate() {
        return update;
    }
    
    public long getBuild() {
        return build;
    }
    
    @Override
    public String toString() {
        return "" + major + "." + minor + "." + micro + "." + update + "." + build;
    }
    
    public String toMajor() {
        return "" + major;
    }
    
    public String toMinor() {
        return "" + major + "." + minor;
    }
    
    public String toMicro() {
        return "" + major + "." + minor + "." + micro;
    }
    
    public String toJdkStyle() {
        return "" + major +
                "." + minor +
                "." + micro +
                (update != 0 ? "_" + (update < 10 ? "0" + update : update) : "");
    }
    
    /////////////////////////////////////////////////////////////////////////////////
    // Inner Classes
    public static class VersionDistance {
        private final long majorDistance;
        private final long minorDistance;
        private final long microDistance;
        private final long updateDistance;
        private final long buildDistance;
        
        private VersionDistance(
                final Version version1,
                final Version version2) {
            majorDistance = Math.abs(version1.getMajor() - version2.getMajor());
            minorDistance = Math.abs(version1.getMinor() - version2.getMinor());
            microDistance = Math.abs(version1.getMicro() - version2.getMicro());
            updateDistance = Math.abs(version1.getUpdate() - version2.getUpdate());
            buildDistance = Math.abs(version1.getBuild() - version2.getBuild());
        }
        
        public boolean equals(
                final VersionDistance distance) {
            return ((majorDistance == distance.majorDistance) &&
                    (minorDistance == distance.minorDistance) &&
                    (microDistance == distance.microDistance) &&
                    (updateDistance == distance.updateDistance) &&
                    (buildDistance == distance.buildDistance));
        }
        
        public boolean greaterThan(
                final VersionDistance distance) {
            if (majorDistance > distance.majorDistance) {
                return true;
            } else if (majorDistance == distance.majorDistance) {
                if (minorDistance > distance.minorDistance) {
                    return true;
                } else if (minorDistance == distance.minorDistance) {
                    if (microDistance > distance.microDistance) {
                        return true;
                    } else if (microDistance == distance.microDistance) {
                        if (updateDistance > distance.updateDistance) {
                            return true;
                        } else if (updateDistance == distance.updateDistance) {
                            if (buildDistance > distance.buildDistance) {
                                return true;
                            }
                        }
                    }
                }
            }
            
            return false;
        }
        
        public boolean greaterOrEquals(
                final VersionDistance version) {
            return greaterThan(version) || equals(version);
        }
        
        public boolean lessThan(
                final VersionDistance version) {
            return !greaterOrEquals(version);
        }
        
        public boolean lessOrEquals(
                final VersionDistance distance) {
            return !greaterThan(distance);
        }
    }
}
