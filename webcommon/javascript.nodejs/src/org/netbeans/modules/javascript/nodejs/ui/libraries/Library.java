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

package org.netbeans.modules.javascript.nodejs.ui.libraries;

import java.util.Objects;

/**
 * npm package/library.
 *
 * @author Jan Stola
 */
public class Library {
    /** Name of the library. */
    private final String name;
    /** Versions of the library. */
    private Library.Version[] versions;
    /** Latest version of the library. */
    private Library.Version latestVersion;
    /** Description of the library. */
    private String description;
    /** Keywords for the library. */
    private String[] keywords;

    /**
     * Creates a new {@code Library} with the given name.
     * 
     * @param name name of the library.
     */
    Library(String name) {
        this.name = name;
    }

    /**
     * Returns the name of this library.
     * 
     * @return name of this library.
     */
    public String getName() {
        return name;
    }

    /**
     * Returns versions of the library.
     * 
     * @return versions of the library.
     */
    @org.netbeans.api.annotations.common.SuppressWarnings(value = "EI_EXPOSE_REP", justification = "Internal usage only")
    public Library.Version[] getVersions() {
        return versions;
    }

    /**
     * Sets versions of the library.
     * 
     * @param versions versions of the library.
     */
    void setVersions(Library.Version[] versions) {
        this.versions = versions;
    }

    /**
     * Returns the latest version of the library.
     * 
     * @return latest version of the library.
     */
    public Library.Version getLatestVersion() {
        return latestVersion;
    }

    /**
     * Sets the latest version of the library.
     * 
     * @param latestVersion latest version of the library.
     */
    void setLatestVersion(Library.Version latestVersion) {
        this.latestVersion = latestVersion;
    }

    /**
     * Returns the description of the library.
     * 
     * @return description of the library.
     */
    public String getDescription() {
        return description;
    }

    /**
     * Sets the description of the library.
     * 
     * @param description description of the library.
     */
    void setDescription(String description) {
        this.description = description;
    }

    /**
     * Returns the keywords for this library.
     * 
     * @return keywords for this library.
     */
    @org.netbeans.api.annotations.common.SuppressWarnings(value = "EI_EXPOSE_REP", justification = "Internal usage only")
    public String[] getKeywords() {
        return keywords == null ? new String[0] : keywords;
    }

    /**
     * Sets the keywords for this library.
     * 
     * @param keywords keywords for this library.
     */
    void setKeywords(String[] keywords) {
        this.keywords = keywords;
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(this.name);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final Library other = (Library) obj;
        return Objects.equals(this.name, other.name);
    }

    /**
     * Version of a npm package/library.
     */
    public static class Version {
        /** Owning library. */
        private final Library library;
        /** Name/number of the version. */
        private final String name;

        /**
         * Creates a new {@code Version}.
         * 
         * @param library owning library.
         * @param name version name/number.
         */
        Version(Library library, String name) {
            this.library = library;
            this.name = name;
        }

        /**
         * Returns the owning library.
         * 
         * @return owning library.
         */
        public Library getLibrary() {
            return library;
        }

        /**
         * Returns name/number of the version.
         * 
         * @return name/number of the version.
         */
        public String getName() {
            return name;
        }

        @Override
        public int hashCode() {
            int hash = 7;
            hash = 59 * hash + Objects.hashCode(this.library);
            hash = 59 * hash + Objects.hashCode(this.name);
            return hash;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            final Version other = (Version) obj;
            return Objects.equals(this.library, other.library)
                    && Objects.equals(this.name, other.name);
        }

    }

}
