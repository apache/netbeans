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
package org.netbeans.modules.payara.tooling.data;

import java.util.Optional;

public final class JDKVersion {

    /**
     * Major version number.
     */
    private final short major;

    /**
     * Minor version number.
     */
    private final Optional<Short> minor;

    /**
     * Sub-minor version number.
     */
    private final Optional<Short> subminor;

    /**
     * Update version number.
     */
    private final Optional<Short> update;

    /**
     * JDK vendor
     */
    private final Optional<String> vendor;

    /**
     * JDK vm
     */
    private final Optional<String> vm;

    private static final int MAJOR_INDEX = 0;
    private static final int MINOR_INDEX = 1;
    private static final int SUBMINOR_INDEX = 2;
    private static final int UPDATE_INDEX = 3;

    private static JDKVersion IDE_JDK_VERSION;

    private static final String VERSION_MATCHER = "(\\d+(\\.\\d+)*)([_u\\-]+[\\S]+)*";

    private static final Short DEFAULT_VALUE = 0;

    private JDKVersion(String version, String vendor, String vm) {
        short[] versions = parseVersions(version);
        this.major = versions[MAJOR_INDEX];
        this.minor = Optional.ofNullable(versions[MINOR_INDEX]);
        this.subminor = Optional.ofNullable(versions[SUBMINOR_INDEX]);
        this.update = Optional.ofNullable(versions[UPDATE_INDEX]);
        this.vendor = Optional.ofNullable(vendor);
        this.vm = Optional.ofNullable(vm);
    }

    JDKVersion(Short major, Optional<Short> minor, Optional<Short> subminor, Optional<Short> update, Optional<String> vendor, Optional<String> vm) {
        this.major = major;
        this.minor = minor;
        this.subminor = subminor;
        this.update = update;
        this.vendor = vendor;
        this.vm = vm;
    }

    /**
     * Get major version number.
     *
     * @return Major version number.
     */
    public short getMajor() {
        return major;
    }

    /**
     * Get minor version number.
     *
     * @return Minor version number.
     */
    public Optional<Short> getMinor() {
        return minor;
    }

    /**
     * Get sub-minor version number.
     *
     * @return Sub-Minor version number.
     */
    public Optional<Short> getSubMinor() {
        return subminor;
    }

    /**
     * Get update version number.
     *
     * @return Update version number.
     */
    public Optional<Short> getUpdate() {
        return update;
    }

    /**
     * Get JDK Vendor name.
     *
     * @return JDK vendor.
     */
    public Optional<String> getVendor() {
        return vendor;
    }

    /**
     * Get JDK VM name.
     *
     * @return JDK vm.
     */
    public Optional<String> getVM() {
        return vm;
    }

    public boolean gt(JDKVersion version) {
        if (major > version.getMajor()) {
            return true;
        } else if (major == version.getMajor()) {
            if (JDKVersion.this.gt(minor, version.getMinor())) {
                return true;
            } else if (eq(minor, version.getMinor())) {
                if (JDKVersion.this.gt(subminor, version.getSubMinor())) {
                    return true;
                } else if (eq(subminor, version.getSubMinor())) {
                    if (JDKVersion.this.gt(update, version.getUpdate())) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    public boolean lt(JDKVersion version) {
        if (major < version.getMajor()) {
            return true;
        } else if (major == version.getMajor()) {
            if (lt(minor, version.getMinor())) {
                return true;
            } else if (eq(minor, version.getMinor())) {
                if (JDKVersion.this.lt(subminor, version.getSubMinor())) {
                    return true;
                } else if (eq(subminor, version.getSubMinor())) {
                    if (JDKVersion.this.lt(update, version.getUpdate())) {
                        return true;
                    }
                }
            }
        }

        return false;
    }

    public boolean ge(JDKVersion version) {
        return gt(version) || equals(version);
    }

    public boolean le(JDKVersion version) {
        return lt(version) || equals(version);
    }

    private boolean gt(Optional<Short> v1, Optional<Short> v2) {
        return v1.orElse(DEFAULT_VALUE) > v2.orElse(DEFAULT_VALUE);
    }

    private boolean lt(Optional<Short> v1, Optional<Short> v2) {
        return v1.orElse(DEFAULT_VALUE) < v2.orElse(DEFAULT_VALUE);
    }

    /**
     * if either v1 or v2 is empty, it is equals
     *
     * @param v1
     * @param v2
     * @return true if equals, otherwise false
     */
    private boolean eq(Optional<Short> v1, Optional<Short> v2) {
        if (!v1.isPresent() || !v2.isPresent()) {
            return true;
        }
        return v1.orElse(DEFAULT_VALUE).equals(v2.orElse(DEFAULT_VALUE));
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final JDKVersion other = (JDKVersion) obj;
        if (this.major != other.getMajor()) {
            return false;
        }
        if (!eq(this.minor, other.getMinor())) {
            return false;
        }
        if (!eq(this.subminor, other.getSubMinor())) {
            return false;
        }
        return eq(this.update, other.getUpdate());
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 89 * hash + this.major;
        hash = 89 * hash + this.minor.orElse(DEFAULT_VALUE);
        hash = 89 * hash + this.subminor.orElse(DEFAULT_VALUE);
        hash = 89 * hash + this.update.orElse(DEFAULT_VALUE);
        return hash;
    }

    @Override
    public String toString() {
        StringBuilder value = new StringBuilder();
        value.append(major);
        if (minor.isPresent()) {
            value.append('.').append(minor.get());
        }
        if (subminor.isPresent()) {
            value.append('.').append(subminor.get());
        }
        if (update.isPresent()) {
            value.append('.').append(update.get());
        }
        return value.toString();
    }

    public static JDKVersion toValue(String version) {
        if (version != null && version.matches(VERSION_MATCHER)) {
            return new JDKVersion(version, null, null);
        } else {
            return null;
        }
    }

    public static JDKVersion toValue(String version, String vendor) {
        if (version != null && version.matches(VERSION_MATCHER)) {
            return new JDKVersion(version, vendor, null);
        } else {
            return null;
        }
    }

    public static JDKVersion toValue(String version, String vendor, String vm) {
        if (version != null && version.matches(VERSION_MATCHER)) {
            return new JDKVersion(version, vendor, vm);
        } else {
            return null;
        }
    }

    public static JDKVersion getDefaultPlatformVersion() {
        return IDE_JDK_VERSION;
    }

    public static boolean isCorrectJDK(JDKVersion jdkVersion, Optional<String> vendorOrVM, Optional<JDKVersion> minVersion, Optional<JDKVersion> maxVersion) {
        boolean correctJDK = true;

        if (vendorOrVM.isPresent()) {
            correctJDK = jdkVersion.getVendor().map(vendor -> vendor.contains(vendorOrVM.get())).orElse(false)
                    || jdkVersion.getVM().map(vm -> vm.contains(vendorOrVM.get())).orElse(false);
        }
        if (correctJDK && minVersion.isPresent()) {
            correctJDK = jdkVersion.ge(minVersion.get());
        }
        if (correctJDK && maxVersion.isPresent()) {
            correctJDK = jdkVersion.le(maxVersion.get());
        }
        return correctJDK;
    }

    public static boolean isCorrectJDK(Optional<JDKVersion> minVersion, Optional<JDKVersion> maxVersion) {
        return isCorrectJDK(IDE_JDK_VERSION, Optional.empty(), minVersion, maxVersion);
    }

    static {
        initialize();
    }

    private static void initialize() {
        String vendor = System.getProperty("java.vendor"); // NOI18N
        String vm = System.getProperty("java.vm.name"); // NOI18N
        /*
            In JEP 223 java.specification.version will be a single number versioning , not a dotted versioning . 
            For JDK 8:
                java.specification.version  1.8
                java.version                1.8.0_212
            For JDK 9:
                java.specification.version  9
                java.version                9.0.4
            For JDK 11:
                java.specification.version  11
                java.version                11.0.3
         */
        String javaVersion = System.getProperty("java.version"); // NOI18N
        short[] versions = parseVersions(javaVersion);

        IDE_JDK_VERSION = new JDKVersion(
                versions[MAJOR_INDEX],
                Optional.of(versions[MINOR_INDEX]),
                Optional.of(versions[SUBMINOR_INDEX]),
                Optional.of(versions[UPDATE_INDEX]),
                Optional.of(vendor),
                Optional.of(vm)
        );
    }

    /**
     * Parses the java version text
     *
     * @param javaVersion the Java Version e.g 1.8.0u222,
     * 1.8.0_232-ea-8u232-b09-0ubuntu1-b09, 11.0.5
     * @return
     */
    static short[] parseVersions(String javaVersion) {

        short[] versions = {1, 0, 0, 0};
        if (javaVersion == null || javaVersion.length() <= 0) {
            return versions; // not likely!!
        }

        String[] javaVersionSplit = javaVersion.split("-"); // NOI18N
        String[] split = javaVersionSplit[0].split("\\."); // NOI18N

        if (split.length > 0) {
            if (split.length > 0) {
                versions[MAJOR_INDEX] = Short.parseShort(split[0]);
            }
            if (split.length > 1) {
                versions[MINOR_INDEX] = Short.parseShort(split[1]);
            }
            if (split.length > 2) {
                split = split[2].split("[_u]"); // NOI18N
                versions[SUBMINOR_INDEX] = Short.parseShort(split[0]);
                if (split.length > 1) {
                    versions[UPDATE_INDEX] = Short.parseShort(split[1]);
                }
            }
        }
        return versions;
    }
}
