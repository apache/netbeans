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

package org.netbeans.modules.j2ee.deployment.common.api;

import java.io.Serializable;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.api.annotations.common.NonNull;
import org.openide.util.Parameters;

/**
 * Represents the generic version. Useful for libraries, products etc.
 * <p>
 * This class is <i>Immutable</i>.
 *
 * @author Petr Hejl
 * @since 1.68
 */
// TODO add JBoss notation parsing MAJOR.MINOR.MICRO.QUALIFIER

// TODO: I copied this class to org.netbeans.modules.web.common.api.Version
//       so that it can be reused in other places. Perhaps it should be deprecated
//       here in favor of web.common one???

public final class Version implements Serializable {

    private static final Pattern JSR277_PATTERN = Pattern.compile(
            "(\\d+)(\\.(\\d+)(\\.(\\d+)(\\.(\\d+))?)?)?(-((\\w|-)+))?");
    
    private static final Pattern DOTTED_PATTERN = Pattern.compile(
            "(\\d+)(\\.(\\d+)(\\.(\\d+)(\\.(\\d+)(\\.(\\d+))?)?)?)?(\\.(\\d+))*");    

    private final String version;

    private final Integer majorNumber;

    private final Integer minorNumber;

    private final Integer microNumber;

    private final Integer updateNumber;

    private final String qualifier;

    private Version(String version, Integer majorNumber, Integer minorNumber,
            Integer microNumber, Integer updateNumber, String qualifier) {
        this.version = version;
        this.majorNumber = majorNumber;
        this.minorNumber = minorNumber;
        this.microNumber = microNumber;
        this.updateNumber = updateNumber;
        this.qualifier = qualifier;
    }

    /**
     * Creates the version from the spec version string.
     * Expected format is <code>MAJOR_NUMBER[.MINOR_NUMBER[.MICRO_NUMBER[.UPDATE_NUMBER]]][-QUALIFIER]</code>
     * or <code>MAJOR_NUMBER[.MINOR_NUMBER[.MICRO_NUMBER[.UPDATE_NUMBER[.QUALIFIER]]]]</code>
     * or <code>GENERIC_VERSION_STRING</code>. The string is evaluated in this order.
     *
     * @param version spec version string
     */        
    public static @NonNull Version fromJsr277OrDottedNotationWithFallback(@NonNull String version) {
        Parameters.notNull("version", version);

        Version parsed = fromJsr277NotationWithFallback(version);
        if (parsed.getMajor() == null) {
            return fromDottedNotationWithFallback(version);
        }
        return parsed;
    }
    
    /**
     * Creates the version from the spec version string.
     * Expected format is <code>MAJOR_NUMBER[.MINOR_NUMBER[.MICRO_NUMBER[.UPDATE_NUMBER]]][-QUALIFIER]</code>
     * or <code>GENERIC_VERSION_STRING</code>.
     *
     * @param version spec version string with the following format:
     *             <code>MAJOR_NUMBER[.MINOR_NUMBER[.MICRO_NUMBER[.UPDATE_NUMBER]]][-QUALIFIER]</code>
     *             or <code>GENERIC_VERSION_STRING</code>
     */
    public static @NonNull Version fromJsr277NotationWithFallback(@NonNull String version) {
        Parameters.notNull("version", version);

        Matcher matcher = JSR277_PATTERN.matcher(version);
        if (matcher.matches()) {
            String fragment = matcher.group(1);
            Integer majorNumber = fragment != null ? Integer.valueOf(fragment) : null;
            fragment = matcher.group(3);
            Integer minorNumber = fragment != null ? Integer.valueOf(fragment) : null;
            fragment = matcher.group(5);
            Integer microNumber = fragment != null ? Integer.valueOf(fragment) : null;
            fragment = matcher.group(7);
            Integer updateNumber = fragment != null ? Integer.valueOf(fragment) : null;
            String qualifier = matcher.group(9);

            return new Version(version, majorNumber, minorNumber,
                    microNumber, updateNumber, qualifier);
        } else {
            return new Version(version, null, null, null, null, null);
        }
    }
    
    /**
     * Creates the version from the spec version string.
     * Expected format is <code>MAJOR_NUMBER[.MINOR_NUMBER[.MICRO_NUMBER[.UPDATE_NUMBER[.QUALIFIER]]]]</code>
     * or <code>GENERIC_VERSION_STRING</code>.
     *
     * @param version spec version string with the following format:
     *             <code>MAJOR_NUMBER[.MINOR_NUMBER[.MICRO_NUMBER[.UPDATE_NUMBER[.QUALIFIER]]]]</code>
     *             or <code>GENERIC_VERSION_STRING</code>
     */
    public static @NonNull Version fromDottedNotationWithFallback(@NonNull String version) {
        Parameters.notNull("version", version);

        Matcher matcher = DOTTED_PATTERN.matcher(version);
        if (matcher.matches()) {
            String fragment = matcher.group(1);
            Integer majorNumber = fragment != null ? Integer.valueOf(fragment) : null;
            fragment = matcher.group(3);
            Integer minorNumber = fragment != null ? Integer.valueOf(fragment) : null;
            fragment = matcher.group(5);
            Integer microNumber = fragment != null ? Integer.valueOf(fragment) : null;
            fragment = matcher.group(7);
            Integer updateNumber = fragment != null ? Integer.valueOf(fragment) : null;
            String qualifier = matcher.group(9);

            return new Version(version, majorNumber, minorNumber,
                    microNumber, updateNumber, qualifier);
        } else {
            return new Version(version, null, null, null, null, null);
        }
    }    

    /**
     * Returns the major number. May return <code>null</code>.
     *
     * @return the major number; may return <code>null</code>
     */
    @CheckForNull
    public Integer getMajor() {
        return majorNumber;
    }

    /**
     * Returns the minor number. May return <code>null</code>.
     *
     * @return the minor number; may return <code>null</code>
     */
    @CheckForNull
    public Integer getMinor() {
        return minorNumber;
    }

    /**
     * Returns the micro number. May return <code>null</code>.
     *
     * @return the micro number; may return <code>null</code>
     */
    @CheckForNull
    public Integer getMicro() {
        return microNumber;
    }

    /**
     * Returns the update. May return <code>null</code>.
     *
     * @return the update; may return <code>null</code>
     */
    @CheckForNull
    public Integer getUpdate() {
        return updateNumber;
    }

    /**
     * Returns the qualifier. May return <code>null</code>.
     *
     * @return the qualifier; may return <code>null</code>
     */
    @CheckForNull
    public String getQualifier() {
        return qualifier;
    }

    /**
     * {@inheritDoc}
     * <p>
     * Two versions are equal if and only if they have same major, minor,
     * micro, update number and qualifier. If the version does not conform to
     * notation the versions are equal only if the version strings exactly
     * matches.
     */
    @org.netbeans.api.annotations.common.SuppressWarnings("RC_REF_COMPARISON")
    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final Version other = (Version) obj;
        
        // non conform
        if ((this.majorNumber == null && other.majorNumber != null)
                || (this.majorNumber != null && other.majorNumber == null)) {
            return false;
        } else if (this.majorNumber == null && other.majorNumber == null) {
            return this.version.equals(other.version);
        }

        // standard
        if (this.majorNumber != other.majorNumber && (this.majorNumber == null || !this.majorNumber.equals(other.majorNumber))) {
            return false;
        }
        if (this.minorNumber != other.minorNumber && (this.minorNumber == null || !this.minorNumber.equals(other.minorNumber))) {
            return false;
        }
        if (this.microNumber != other.microNumber && (this.microNumber == null || !this.microNumber.equals(other.microNumber))) {
            return false;
        }
        if (this.updateNumber != other.updateNumber && (this.updateNumber == null || !this.updateNumber.equals(other.updateNumber))) {
            return false;
        }
        if ((this.qualifier == null) ? (other.qualifier != null) : !this.qualifier.equals(other.qualifier)) {
            return false;
        }
        return true;
    }

    /**
     * {@inheritDoc}
     * <p>
     * The implementation consistent with {@link #equals(Object)}.
     */
    @Override
    public int hashCode() {
        // non conform
        if (this.majorNumber == null) {
            return this.version.hashCode();
        }

        // standard
        int hash = 7;
        hash = 97 * hash + this.majorNumber.hashCode();
        hash = 97 * hash + (this.minorNumber != null ? this.minorNumber.hashCode() : 0);
        hash = 97 * hash + (this.microNumber != null ? this.microNumber.hashCode() : 0);
        hash = 97 * hash + (this.updateNumber != null ? this.updateNumber.hashCode() : 0);
        hash = 97 * hash + (this.qualifier != null ? this.qualifier.hashCode() : 0);
        return hash;
    }

    public boolean isAboveOrEqual(Version other) {
        if (this.majorNumber == null || other.majorNumber == null) {
            return this.equals(other);
        }

        return compareTo(other) >= 0;
    }

    public boolean isBelowOrEqual(Version other) {
        if (this.majorNumber == null || other.majorNumber == null) {
            return this.equals(other);
        }

        return compareTo(other) <= 0;
    }

    @Override
    public String toString() {
        return version;
    }

    /**
     * Expands the version to full dotted notation.
     *
     * @param qualifierDefault the qualifier to use if empty
     * @return the expanded version
     * @since 1.109
     */
    public Version expand(String qualifierDefault) {
        if (majorNumber == null) {
            return this;
        }
        StringBuilder sb = new StringBuilder();
        sb.append(majorNumber).append('.');
        sb.append(minorNumber == null ? 0 : minorNumber).append('.');
        sb.append(microNumber == null ? 0 : microNumber).append('.');
        sb.append(updateNumber == null ? 0 : updateNumber).append('.');
        sb.append(qualifier == null ? qualifierDefault : qualifier);

        return new Version(sb.toString(), majorNumber,
                minorNumber == null ? 0 : minorNumber,
                microNumber == null ? 0 : microNumber,
                updateNumber == null ? 0 : updateNumber,
                qualifier == null ? qualifierDefault : qualifier);
    }

    private int compareTo(Version o) {
        int comparison = compare(majorNumber, o.majorNumber);
        if (comparison != 0) {
            return comparison;
        }
        comparison = compare(minorNumber, o.minorNumber);
        if (comparison != 0) {
            return comparison;
        }
        comparison = compare(microNumber, o.microNumber);
        if (comparison != 0) {
            return comparison;
        }
        comparison = compare(updateNumber, o.updateNumber);
        if (comparison != 0) {
            return comparison;
        }
        return compare(qualifier, o.qualifier);
    }

    private static int compare(Integer o1, Integer o2) {
        if (o1 == null && o2 == null) {
            return 0;
        }
        if (o1 == null) {
            return Integer.valueOf(0).compareTo(o2);
        }
        if (o2 == null) {
            return o1.compareTo(0);
        }
        return o1.compareTo(o2);
    }

    private static int compare(String o1, String o2) {
        if (o1 == null && o2 == null) {
            return 0;
        }
        if (o1 == null) {
            return "".compareTo(o2);
        }
        if (o2 == null) {
            return o1.compareTo("");
        }
        return o1.compareTo(o2);
    }

}
