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

    public static JDKVersion IDE_JDK_VERSION;

    private static final String VERSION_MATCHER = "([0-9]+[\\._u\\-]+)*[0-9]+";

    // split java version into it's constituent parts, i.e.
    // 1.2.3.4 -> [ 1, 2, 3, 4]
    // 1.2.3u4 -> [ 1, 2, 3, 4]
    // 1.2.3_4 -> [ 1, 2, 3, 4]
    private static final String VERSION_SPLITTER = "[\\._u\\-]+";

    private static final Short DEFAULT_VALUE = 0;

    private JDKVersion(String string) {
        String[] split = string.split(VERSION_SPLITTER);
        major = split.length > 0 ? Short.parseShort(split[0]) : 0;
        minor = split.length > 1 ? Optional.of(Short.parseShort(split[1])) : Optional.empty();
        subminor = split.length > 2 ? Optional.of(Short.parseShort(split[2])) : Optional.empty();
        update = split.length > 3 ? Optional.of(Short.parseShort(split[3])) : Optional.empty();
    }

    private JDKVersion(Short major, Short minor, Short subminor, Short update) {
        this.major = major;
        this.minor = Optional.of(minor);
        this.subminor = Optional.of(subminor);
        this.update = Optional.of(update);
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
        StringBuilder value = new StringBuilder(major);
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
            return new JDKVersion(version);
        } else {
            return null;
        }
    }

    public static JDKVersion getDefaultPlatformVersion() {
        return IDE_JDK_VERSION;
    }

    public static boolean isCorrectJDK(JDKVersion jdkVersion, Optional<JDKVersion> minVersion, Optional<JDKVersion> maxVersion) {
        boolean correctJDK = true;
        if (minVersion.isPresent()) {
            correctJDK = jdkVersion.ge(minVersion.get());
        }
        if (correctJDK && maxVersion.isPresent()) {
            correctJDK = jdkVersion.le(maxVersion.get());
        }
        return correctJDK;
    }

    public static boolean isCorrectJDK(Optional<JDKVersion> minVersion, Optional<JDKVersion> maxVersion) {
        return isCorrectJDK(IDE_JDK_VERSION, minVersion, maxVersion);
    }

    static {
        initialize();
    }

    private static void initialize() {
        short major = 1;
        short minor = 0;
        short subminor = 0;
        short update = 0;
        try {
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
            String javaSpecVersion = System.getProperty("java.specification.version");
            String javaVersion = System.getProperty("java.version");
            String[] javaSpecVersionSplit = javaSpecVersion.split("\\.");
            if (javaSpecVersionSplit.length == 1) {
                // Handle Early Access build. e.g: 13-ea
                String[] javaVersionSplit = javaVersion.split("-");
                String javaVersionCategory = javaVersionSplit[0];
                String[] split = javaVersionCategory.split("[\\.]+");

                if (split.length > 0) {
                    if (split.length > 0) {
                        major = Short.parseShort(split[0]);
                    }
                    if (split.length > 1) {
                        minor = Short.parseShort(split[1]);
                    }
                    if (split.length > 2) {
                        subminor = Short.parseShort(split[2]);
                    }
                    if (split.length > 3) {
                        update = Short.parseShort(split[3]);
                    }
                }
            } else {
                if (javaVersion == null || javaVersion.length() <= 0) {
                    return;
                }

                String[] javaVersionSplit = javaVersion.split("\\.");
                if (javaVersionSplit.length < 3 || !javaVersionSplit[0].equals("1")) {
                    return;
                }

                major = Short.parseShort(javaVersionSplit[0]);
                minor = Short.parseShort(javaVersionSplit[1]);
                javaVersionSplit = javaVersionSplit[2].split("_");

                if (javaVersionSplit.length < 1) {
                    return;
                }

                subminor = Short.parseShort(javaVersionSplit[0]);

                if (javaVersionSplit.length > 1) {
                    update = Short.parseShort(javaVersionSplit[1]);
                }
            }
        } catch (Exception e) {
            // ignore
        }

        IDE_JDK_VERSION = new JDKVersion(major, minor, subminor, update);
    }
}
