/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2010 Sun Microsystems, Inc.
 */

package org.netbeans.modules.web.common.api;

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
 */
// TODO add JBoss notation parsing MAJOR.MINOR.MICRO.QUALIFIER
public final class Version {

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
        hash = 97 * hash + (this.majorNumber != null ? this.majorNumber.hashCode() : 0);
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
            return o1.compareTo(Integer.valueOf(0));
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
