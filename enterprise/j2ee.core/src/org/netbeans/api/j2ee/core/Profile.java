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

package org.netbeans.api.j2ee.core;

import java.util.Comparator;
import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.annotations.common.NullAllowed;
import org.openide.util.NbBundle;
import org.openide.util.NbBundle.Messages;

/**
 * Represents the defined Java EE profiles.
 *
 * @author Petr Hejl
 */
public enum Profile {

    // !!! ATTENTION: BE AWARE OF THE ENUM ORDER! It controls compatibility and UI position.
    // Do not ever change name of this constant - it is copied from j2eeserver
    @Messages("J2EE_13.displayName=J2EE 1.3")
    J2EE_13("1.3"),

    // Do not ever change name of this constant - it is copied from j2eeserver
    @Messages("J2EE_14.displayName=J2EE 1.4")
    J2EE_14("1.4"),

    // Do not ever change name of this constant - it is copied from j2eeserver
    @Messages("JAVA_EE_5.displayName=Java EE 5")
    JAVA_EE_5("1.5"),

    @Messages("JAVA_EE_6_WEB.displayName=Java EE 6 Web")
    JAVA_EE_6_WEB("1.6", "web"),

    @Messages("JAVA_EE_6_FULL.displayName=Java EE 6")
    JAVA_EE_6_FULL("1.6"),

    @Messages("JAVA_EE_7_WEB.displayName=Java EE 7 Web")
    JAVA_EE_7_WEB("1.7", "web"),

    @Messages("JAVA_EE_7_FULL.displayName=Java EE 7")
    JAVA_EE_7_FULL("1.7"),

    @Messages("JAVA_EE_8_WEB.displayName=Java EE 8 Web")
    JAVA_EE_8_WEB("1.8", "web"),

    @Messages("JAVA_EE_8_FULL.displayName=Java EE 8")
    JAVA_EE_8_FULL("1.8"),

    @Messages("JAKARTA_EE_8_WEB.displayName=Jakarta EE 8 Web")
    JAKARTA_EE_8_WEB("8.0", "web"),

    @Messages("JAKARTA_EE_8_FULL.displayName=Jakarta EE 8")
    JAKARTA_EE_8_FULL("8.0"),

    @Messages("JAKARTA_EE_9_WEB.displayName=Jakarta EE 9 Web")
    JAKARTA_EE_9_WEB("9.0", "web"),

    @Messages("JAKARTA_EE_9_FULL.displayName=Jakarta EE 9")
    JAKARTA_EE_9_FULL("9.0"),

    @Messages("JAKARTA_EE_9_1_WEB.displayName=Jakarta EE 9.1 Web")
    JAKARTA_EE_9_1_WEB("9.1", "web"),

    @Messages("JAKARTA_EE_9_1_FULL.displayName=Jakarta EE 9.1")
    JAKARTA_EE_9_1_FULL("9.1"),

    @Messages("JAKARTA_EE_10_WEB.displayName=Jakarta EE 10 Web")
    JAKARTA_EE_10_WEB("10", "web"),

    @Messages("JAKARTA_EE_10_FULL.displayName=Jakarta EE 10")
    JAKARTA_EE_10_FULL("10"),
    
    @Messages("JAKARTA_EE_11_WEB.displayName=Jakarta EE 11 Web")
    JAKARTA_EE_11_WEB("11", "web"),
    
    @Messages("JAKARTA_EE_11_FULL.displayName=Jakarta EE 11")
    JAKARTA_EE_11_FULL("11");
    // !!! ATTENTION: BE AWARE OF THE ENUM ORDER! It controls compatibility and UI position.

    public static final Comparator<Profile> UI_COMPARATOR = (Profile o1, Profile o2) -> -(o1.ordinal() - o2.ordinal());

    // cache
    private final String propertiesString;

    private Profile(String canonicalName) {
        this.propertiesString = canonicalName;
    }

    private Profile(String canonicalName, String profile) {
        this.propertiesString = canonicalName + "-" + profile;
    }

    /**
     * Returns the UI visible description of the profile.
     *
     * @return the UI visible description of the profile
     */
    @NonNull
    public String getDisplayName() {
        return NbBundle.getMessage(Profile.class, this.name() + ".displayName");
    }

    @NonNull
    public String toPropertiesString() {
        return propertiesString;
    }
    
    /**
     * Find out if this profile is a Web profile Platform.
     *
     * @return true if this is a Java/Jakarta EE Web profile, false if is a Full
     * Platform
     */
    @NonNull
    public boolean isWebProfile() {
        return propertiesString.endsWith("web");
    }
    
    /**
     * Find out if this profile is a Full profile Platform.
     *
     * @return true if this is a Java/Jakarta EE Full profile, false if is a Web
     * profile Platform
     */
    @NonNull
    public boolean isFullProfile() {
        return !propertiesString.endsWith("web");
    }

    /**
     * Find out if the version of the profile is equal or higher to given profile.
     *
     * Please be aware of the following rules:
     * <br/><br/>
     *
     * 1) Each Java/Jakarta EE X version is considered as lower than Java EE X+1 version
     * (this applies regardless on Web/Full specification and in reality it means
     * that even Java EE 6 Full version is considered as lower than Java EE 7 Web)
     * <br/><br/>
     *
     * 2) Each Java/Jakarta EE X Web version is considered as lower than Java/Jakarta EE X Full
     * <br/>
     *
     * @param profile profile to compare against
     * @return true if this profile is equal or higher to given one,
     *         false otherwise
     * @since 1.19
     */
    public boolean isAtLeast(@NonNull Profile profile) {
        return this.ordinal() >= profile.ordinal();
    }
    
    /**
     * Find out if the version of the profile is equal or lower to given profile.
     *
     * Please be aware of the following rules:
     * <br/><br/>
     *
     * 1) Each Java/Jakarta EE X version is considered as lower than Java/Jakarta EE X+1 version
     * (this applies regardless on Web/Full specification and in reality it means
     * that even Java EE 6 Full version is considered as lower than Java EE 7 Web)
     * <br/><br/>
     *
     * 2) Each Java/Jakarta EE X Web version is considered as lower than Java/Jakarta EE X Full
     * <br/>
     *
     * @param profile profile to compare against
     * @return true if this profile is equal or lower to given one,
     *         false otherwise
     * @since 1.19
     */
    public boolean isAtMost(@NonNull Profile profile) {
        return this.ordinal() <= profile.ordinal();
    }

    @Override
    public String toString() {
        return toPropertiesString();
    }

    @CheckForNull
    public static Profile fromPropertiesString(@NullAllowed String value) {
        if (value == null) {
            return null;
        }

        String valueMinusQuotes = value.replace("\"","");

        for (Profile profile : Profile.values()) {
            if (profile.toPropertiesString().equals(valueMinusQuotes)
                    || profile.name().equals(valueMinusQuotes)
                    || (valueMinusQuotes.startsWith("EE_") && profile.name().endsWith(valueMinusQuotes))) {
                return profile;
            }
        }

        return null;
    }
}
