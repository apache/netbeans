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

package org.netbeans.api.java.platform;

import java.util.Arrays;
import java.util.Locale;
import java.util.Objects;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.annotations.common.NullAllowed;
import org.openide.modules.SpecificationVersion;

/** 
 * Specification of the JDK.
 * <p>
 * Since version 1.77 the version is represented by a
 * {@link Runtime.Version}, and this class implements {@link Comparable}.
 * When creating a specification that carries a version, the constructors
 * accepting a {@link Runtime.Version} are preferred over those accepting a
 * {@link SpecificationVersion}: the latter convert the given version to
 * {@link Runtime.Version} internally.
 */
public final class Specification implements Comparable<Specification> {

    private final String name;
    private final Runtime.Version version;
    private final Profile[] profiles;
    private final String displayName;

    /**
     * The oldest parseable {@link Runtime.Version}; used in {@link #compareTo}
     * as the stand-in for specifications without a version.
     */
    private static final Runtime.Version VERSION_FLOOR = Runtime.Version.parse("1"); //NOI18N

    /**
     * Creates a new specification of a JDK without a version.
     * @param name of the specification e.g J2SE
     * @since 1.77
     */
    public Specification(@NullAllowed String name) {
        this(name, (Runtime.Version) null, null, null);
    }

    /**
     * Creates a new specification of a JDK with the given name and version.
     * Prefer {@link #Specification(String, Runtime.Version)}, which stores the
     * version directly.
     * @param name of the specification e.g J2SE
     * @param version of the specification e.g. 1.4
     */
    public Specification (@NullAllowed String name, @NullAllowed SpecificationVersion version) {
        this (name, version, null, null);
    }

    /**
     * Creates a new specification of a JDK with the given name, version
     * and supported profiles.
     * Prefer {@link #Specification(String, Runtime.Version, Profile[])}, which
     * stores the version directly.
     * @param name of the specification e.g J2SE
     * @param version of the specification e.g. 1.4
     * @param profiles of the JDK
     */
    public Specification (
        @NullAllowed final String name,
        @NullAllowed final SpecificationVersion version,
        @NullAllowed final Profile[] profiles) {
        this(name, version, null, profiles);
    }
    
    /**
     * Creates a new specification of a JDK with the given name, version,
     * display name and supported profiles.
     * Prefer {@link #Specification(String, Runtime.Version, String, Profile[])},
     * which stores the version directly.
     * @param name of the specification e.g J2SE
     * @param version of the specification e.g. 1.4
     * @param displayName the display name of the JDK e.g. "Java SE".
     * @param profiles of the JDK
     * @since 1.26
     */
    public Specification (
        @NullAllowed final String name,
        @NullAllowed final SpecificationVersion version,
        @NullAllowed final String displayName,
        @NullAllowed final Profile[] profiles) {
        this.name = name;
        this.version = toJdkVersion(version);
        this.displayName = displayName;
        this.profiles = profiles;
    }

    /**
     * Creates a new specification of a JDK with the given name and version.
     * @param name of the specification e.g J2SE
     * @param version of the specification, e.g. {@code Runtime.Version.parse("17")}
     * @since 1.77
     */
    public Specification(@NullAllowed String name, @NullAllowed Runtime.Version version) {
        this(name, version, null, null);
    }

    /**
     * Creates a new specification of a JDK with the given name, version
     * and supported profiles.
     * @param name of the specification e.g J2SE
     * @param version of the specification, e.g. {@code Runtime.Version.parse("17")}
     * @param profiles of the JDK
     * @since 1.77
     */
    public Specification(
        @NullAllowed String name,
        @NullAllowed Runtime.Version version,
        @NullAllowed Profile[] profiles) {
        this(name, version, null, profiles);
    }

    /**
     * Creates a new specification of a JDK with the given name, version,
     * display name and supported profiles.
     * @param name of the specification e.g J2SE
     * @param version of the specification, e.g. {@code Runtime.Version.parse("17")}
     * @param displayName the display name of the JDK e.g. "Java SE".
     * @param profiles of the JDK
     * @since 1.77
     */
    public Specification(
        @NullAllowed String name,
        @NullAllowed Runtime.Version version,
        @NullAllowed String displayName,
        @NullAllowed Profile[] profiles) {
        this.name = name;
        this.version = version;
        this.displayName = displayName;
        this.profiles = profiles;
    }

    /**
     * Returns the name of the Java specification, e.g. {@code j2se}
     * @return String
     */
    public final String getName () {
        return this.name;
    }

    /**
     * Returns the version of the Java specification e.g 1.4
     * @return instance of SpecificationVersion derived from the stored
     * {@link Runtime.Version}, or null if the specification version is
     * not set
     */
    public final SpecificationVersion getVersion() {
        if (this.version == null) {
            return null;
        }
        // SpecificationVersion can only hold numeric components;
        // rebuild the version from its numeric parts, which drops
        // pre-release and build information,
        // e.g. 28-ea+21-1707 becomes 28, 17.0.2+9 becomes 17.0.2
        StringBuilder s = new StringBuilder();
        s.append(version.feature());
        if (version.interim() > 0 || version.update() > 0 || version.patch() > 0) {
            s.append('.').append(version.interim()); //NOI18N
        }
        if (version.update() > 0 || version.patch() > 0) {
            s.append('.').append(version.update()); //NOI18N
        }
        if (version.patch() > 0) {
            s.append('.').append(version.patch()); //NOI18N
        }
        return new SpecificationVersion(s.toString());
    }

    /**
     * Returns the version of the Java platform as a {@link Runtime.Version}.
     * @return the platform version, or null if not set.
     * @since 1.77
     */
    public Runtime.Version getJdkVersion() {
        return this.version;
    }

    /**
     * Returns JDK 8 profiles supported by the Java platform.
     * @return list of profiles, or null if not applicable
     */
    public final Profile[] getProfiles () {
        return this.profiles;
    }
    
    /**
     * Returns the display name of the JDK.
     * While the {@link Specification#getName()} is used as a system name
     * the {@link Specification#getDisplayName()} is used while presenting the
     * JDK to the user.
     * @return the user friendly name, e.g. "Java SE" for the "j2se" JDK.
     * @since 1.26
     */
    @NonNull
    public String getDisplayName() {
        if (displayName != null) {
            return displayName;
        }
        final String defaultName = getName();
        return defaultName == null ?
            "": //NOI18N
            defaultName.toUpperCase(Locale.ENGLISH);
    }

    @Override
    public int hashCode () {
        int hc = 0;
        if (this.name != null)
            hc = this.name.hashCode() << 16;
        if (this.version != null)
            hc += this.version.hashCode();
        return hc;
    }

    @Override
    public boolean equals(Object other) {
        if (other instanceof Specification os) {
            boolean re = this.name == null ? os.name == null : this.name.equals(os.name) &&
                          this.version == null ? os.version == null : this.version.equals (os.version);
            if (!re || this.profiles == null)
                return re;
            if (os.profiles == null || this.profiles.length != os.profiles.length)
                return false;
            for (int i=0; i<os.profiles.length; i++)
                re &= this.profiles[i].equals(os.profiles[i]);
            return re;
        }
        else
            return false;
    }

    @Override
    public String toString () {
        String str = this.name == null ? "" : this.name + " "; // NOI18N
        str += this.version == null ? "" : this.version + " "; // NOI18N
        if (this.profiles != null) {
            str+="["; // NOI18N
            for (int i = 0; i < profiles.length; i++) {
                str+= profiles[i]+ " "; // NOI18N
            }
            str+="]"; // NOI18N
        }
        return str;
    }

    private static Runtime.Version toJdkVersion(SpecificationVersion version) {
        if (version == null) {
            return null;
        }
        String s = version.toString();
        String[] parts = s.split("\\."); //NOI18N
        // Runtime.Version.parse() rejects a trailing zero component
        // (e.g. "9.0" or "1.8.0"); it is insignificant, strip it. Unparseable
        // input, e.g. a version starting with "0", is mapped to null below.
        int end = parts.length;
        while (end > 1 && "0".equals(parts[end-1])) { //NOI18N
            end--;
        }
        if (end < parts.length) {
            s = String.join(".", Arrays.copyOf(parts, end)); //NOI18N
        }
        try {
            return Runtime.Version.parse(s);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    /**
     * Compares this specification with the specified one for order,
     * by version; specifications without a version are treated as the
     * oldest possible ones. Specifications with equal versions are
     * compared by display name.
     * @param other the specification to compare to
     * @return a negative integer, zero, or a positive integer as this
     * specification is older than, the same as, or newer than the other
     * @since 1.77
     */
    @Override
    public int compareTo(Specification other) {
        Runtime.Version v1 = Objects.requireNonNullElse(this.getJdkVersion(), VERSION_FLOOR);
        Runtime.Version v2 = Objects.requireNonNullElse(other.getJdkVersion(), VERSION_FLOOR);
        int cmp = v1.compareTo(v2);
        if (cmp != 0) {
            return cmp;
        }
        return this.getDisplayName().compareTo(other.getDisplayName());
    }
}
