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
package org.netbeans.modules.rust.cargo.api;

import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * RustPackageVersion represents the version of a Rust package. This may be a
 * semver number, or a github reference, or even an indicator that the version
 * is inherited from a workspace version.
 * 
 * @see <a href="https://semver.org/">https://semver.org</a>
 */
public final class RustPackageVersion {

    private static final Pattern SEMVER_PATTERN = Pattern.compile("^(0|[1-9]\\d*)\\.(0|[1-9]\\d*)\\.(0|[1-9]\\d*)(?:-((?:0|[1-9]\\d*|\\d*[a-zA-Z-][0-9a-zA-Z-]*)(?:\\.(?:0|[1-9]\\d*|\\d*[a-zA-Z-][0-9a-zA-Z-]*))*))?(?:\\+([0-9a-zA-Z-]+(?:\\.[0-9a-zA-Z-]+)*))?$");

    private class Semver implements Comparable<Semver> {

        private final String semver;
        private final Long major;
        private final Long minor;
        private final Long patch;

        private Semver(String semver) {
            this.semver = semver;
            Matcher m = SEMVER_PATTERN.matcher(semver);
            if (!m.matches()) {
                this.major = -1L;
                this.minor = this.major;
                this.patch = this.major;
            } else {
                this.major = Long.valueOf(m.group(1));
                this.minor = Long.valueOf(m.group(2));
                this.patch = Long.valueOf(m.group(3));
            }
        }

        public String getSemver() {
            return semver;
        }

        public Long getMajor() {
            return major;
        }

        public Long getMinor() {
            return minor;
        }

        public Long getPatch() {
            return patch;
        }

        @Override
        public int compareTo(Semver o) {
            long diff = Long.compare(major, o.major);
            if (diff == 0) {
                diff = Long.compare(minor, o.minor);
                if (diff == 0) {
                    diff = Long.compare(patch, o.patch);
                }
            }
            return diff < 0 ? -1 : (diff == 0 ? 0 : 1);
        }

        @Override
        public int hashCode() {
            int hash = 5;
            hash = 47 * hash + Objects.hashCode(this.major);
            hash = 47 * hash + Objects.hashCode(this.minor);
            hash = 47 * hash + Objects.hashCode(this.patch);
            return hash;
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
            final Semver other = (Semver) obj;
            return compareTo(other) == 0;
        }

        @Override
        public String toString() {
            return semver;
        }
    }

    private RustPackageVersion() {

    }

    public static RustPackageVersion fromString(String version) {
        return new RustPackageVersion();
    }

}
