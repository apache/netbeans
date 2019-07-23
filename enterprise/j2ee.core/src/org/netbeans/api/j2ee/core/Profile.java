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

/**
 * Represents the defined Java EE profiles.
 *
 * @author Petr Hejl
 */
public final class Profile {

    public static final Comparator<Profile> UI_COMPARATOR = new Comparator<Profile>() {

        @Override
        public int compare(Profile o1, Profile o2) {
            return -(o1.order - o2.order);
        }
    };

    // Do not ever change name of this constant - it is copied from j2eeserver
    public static final Profile J2EE_13  = new Profile(1, "1.3", null, "J2EE13.displayName");

    // Do not ever change name of this constant - it is copied from j2eeserver
    public static final Profile J2EE_14  = new Profile(2, "1.4", null, "J2EE14.displayName");

    // Do not ever change name of this constant - it is copied from j2eeserver
    public static final Profile JAVA_EE_5  = new Profile(3, "1.5", null, "JavaEE5.displayName");

    public static final Profile JAVA_EE_6_FULL  = new Profile(4, "1.6", null, "JavaEE6Full.displayName");

    public static final Profile JAVA_EE_6_WEB  = new Profile(5, "1.6", "web", "JavaEE6Web.displayName");

    public static final Profile JAVA_EE_7_FULL  = new Profile(6, "1.7", null, "JavaEE7Full.displayName");

    public static final Profile JAVA_EE_7_WEB  = new Profile(7, "1.7", "web", "JavaEE7Web.displayName");

    public static final Profile JAVA_EE_8_FULL  = new Profile(8, "1.8", null, "JavaEE8Full.displayName");

    public static final Profile JAVA_EE_8_WEB  = new Profile(9, "1.8", "web", "JavaEE8Web.displayName");

    private final int order;

    // cache
    private final String propertiesString;

    private final String bundleKey;

    private Profile(int order, String canonicalName, String profile, String bundleKey) {
        this.order = order;
        this.bundleKey = bundleKey;

        StringBuilder builder = new StringBuilder(canonicalName);
        if (profile != null) {
            builder.append("-").append(profile); // NOI18N
        }
        this.propertiesString = builder.toString();
    }

    /**
     * Returns the UI visible description of the profile.
     *
     * @return the UI visible description of the profile
     */
    @NonNull
    public String getDisplayName() {
        return NbBundle.getMessage(Profile.class, bundleKey);
    }

    @NonNull
    public String toPropertiesString() {
        return propertiesString;
    }

    /**
     * Find out if the version of the profile is equal or higher to given profile.
     *
     * Please be aware of the following rules:
     * <br/><br/>
     *
     * 1) Each Java EE X version is considered as lower than Java EE X+1 version
     * (this applies regardless on Web/Full specification and in reality it means
     * that even Java EE 6 Full version is considered as lower than Java EE 7 Web)
     * <br/><br/>
     *
     * 2) Each Java EE X Web version is considered as lower than Java EE X Full
     * <br/>
     *
     * @param profile profile to compare against
     * @return true if this profile is equal or higher to given one,
     *         false otherwise
     * @since 1.19
     */
    public boolean isAtLeast(@NonNull Profile profile) {
        return isVersionEqualOrHigher(this, profile);
    }

    @Override
    public String toString() {
        return toPropertiesString();
    }

    @CheckForNull
    public static Profile fromPropertiesString(@NullAllowed String value) {
        String valueMinusQuotes = value;
        if(value != null && value.contains("\"")){
            valueMinusQuotes = value.replace("\"","");
        }
        if (J2EE_13.toPropertiesString().equals(valueMinusQuotes)) {
            return J2EE_13;
        } else if (J2EE_14.toPropertiesString().equals(valueMinusQuotes)) {
            return J2EE_14;
        } else if (JAVA_EE_5.toPropertiesString().equals(valueMinusQuotes)) {
            return JAVA_EE_5;
        } else if (JAVA_EE_6_FULL.toPropertiesString().equals(valueMinusQuotes)
                || "EE_6_FULL".equals(value)){ // NOI18N
            return JAVA_EE_6_FULL;
        } else if (JAVA_EE_6_WEB.toPropertiesString().equals(valueMinusQuotes)
                || "EE_6_WEB".equals(value)) {
            return JAVA_EE_6_WEB;
        } else if (JAVA_EE_7_FULL.toPropertiesString().equals(valueMinusQuotes)
                || "EE_7_FULL".equals(value)) { // NOI18N
            return JAVA_EE_7_FULL;
        } else if (JAVA_EE_7_WEB.toPropertiesString().equals(valueMinusQuotes)
                || "EE_7_WEB".equals(value)) {
            return JAVA_EE_7_WEB;
        } else if (JAVA_EE_8_FULL.toPropertiesString().equals(valueMinusQuotes)
                || "EE_8_FULL".equals(value)) { // NOI18N
            return JAVA_EE_8_FULL;
        } else if (JAVA_EE_8_WEB.toPropertiesString().equals(valueMinusQuotes)
                || "EE_8_WEB".equals(value)) {
            return JAVA_EE_8_WEB;
        } else {
            return null;
        }
    }

    private static String getProfileVersion(@NonNull Profile profile) {
        String profileDetails = profile.toPropertiesString();
        int indexOfDash = profileDetails.indexOf("-");
        if (indexOfDash != -1) {
            return profileDetails.substring(0, indexOfDash);
        }
        return profileDetails;
    }

    private static boolean compareWebAndFull(@NonNull Profile profileToCompare, @NonNull Profile comparingVersion) {
        boolean isThisFullProfile = isFullProfile(profileToCompare);
        boolean isParamFullProfile = isFullProfile(comparingVersion);
        if (isThisFullProfile && isParamFullProfile) {
            // Both profiles are Java EE Full
            return true;
        }
        if (!isThisFullProfile && !isParamFullProfile) {
            // Both profiles are Java EE Web
            return true;
        }
        if (isThisFullProfile && !isParamFullProfile) {
            // profileToCompare is Java EE Full profile and comparingVersion is only Java EEWeb profile
            return true;
        }
        return false;
    }

    private static boolean isFullProfile(@NonNull Profile profile) {
        final String profileDetails = profile.toPropertiesString();
        if (profileDetails.indexOf("-") == -1) {
            return true;
        }
        return false;
    }

    /**
     * Compares if the first given profile has equal or higher Java EE version
     * in comparison to the second profile.
     *
     * Please be aware of the following rules:
     * <br/><br/>
     *
     * 1) Each Java EE X version is considered as lower than Java EE X+1 version
     * (this applies regardless on Web/Full specification and in reality it means
     * that even Java EE 6 Full version is considered as lower than Java EE 7 Web)
     * <br/><br/>
     *
     * 2) Each Java EE X Web version is considered as lower than Java EE X Full
     * <br/>
     *
     * @param profileToCompare profile that we want to compare
     * @param comparingVersion version which we are comparing with
     * @return <code>true</code> if the profile version is equal or higher in
     *         comparison with the second one, <code>false</code> otherwise
     * @since 1.19
     */
    private static boolean isVersionEqualOrHigher(@NonNull Profile profileToCompare, @NonNull Profile comparingVersion) {
        int comparisonResult = Profile.UI_COMPARATOR.compare(profileToCompare, comparingVersion);
        if (comparisonResult == 0) {
            // The same version for both
            return true;
        } else {
            String profileToCompareVersion = getProfileVersion(profileToCompare);
            String comparingProfileVersion = getProfileVersion(comparingVersion);
            if (profileToCompareVersion.equals(comparingProfileVersion)) {
                return compareWebAndFull(profileToCompare, comparingVersion);
            } else {
                if (comparisonResult > 0) {
                    // profileToCompare has lower version than comparingVersion
                    return false;
                } else {
                    return true;
                }
            }
        }
    }

}
