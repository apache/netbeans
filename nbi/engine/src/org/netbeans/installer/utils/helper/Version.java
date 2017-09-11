/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU General
 * Public License Version 2 only ("GPL") or the Common Development and Distribution
 * License("CDDL") (collectively, the "License"). You may not use this file except in
 * compliance with the License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html or nbbuild/licenses/CDDL-GPL-2-CP. See the
 * License for the specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header Notice in
 * each file and include the License file at nbbuild/licenses/CDDL-GPL-2-CP.  Oracle
 * designates this particular file as subject to the "Classpath" exception as
 * provided by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the License Header,
 * with the fields enclosed by brackets [] replaced by your own identifying
 * information: "Portions Copyrighted [year] [name of copyright owner]"
 * 
 * Contributor(s):
 * 
 * The Original Software is NetBeans. The Initial Developer of the Original Software
 * is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun Microsystems, Inc. All
 * Rights Reserved.
 * 
 * If you wish your version of this file to be governed by only the CDDL or only the
 * GPL Version 2, indicate your decision by adding "[Contributor] elects to include
 * this software in this distribution under the [CDDL or GPL Version 2] license." If
 * you do not indicate a single choice of license, a recipient has the option to
 * distribute your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above. However, if
 * you add GPL Version 2 code and therefore, elected the GPL Version 2 license, then
 * the option applies only if the new code is made subject to such option by the
 * copyright holder.
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
    
    private Version(
            final String string) {
        String[] split = string.split("[\\._\\-]+"); //NOI18N

        if (split.length > 0) {
            major = new Long(split[0]);
        }
        if (split.length > 1) {
            minor = new Long(split[1]);
        }
        if (split.length > 2) {
            micro = new Long(split[2]);
        }
        if (split.length > 3) {
            update = new Long(split[3]);
        }
        if (split.length > 4) {
            build = new Long(split[4]);
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
