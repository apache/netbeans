/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2014 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2014 Sun Microsystems, Inc.
 */
package org.netbeans.modules.javascript.cdnjs;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Library.
 * 
 * @author Jan Stola
 */
public final class Library {
    /** Name of this library. */
    private String name;
    /** Description of this library. */
    private String description;
    /** Home page of this library. */
    private String homePage;
    /** Versions of this library. */
    private Version[] versions;

    /**
     * Creates a new {@code Library}.
     */
    Library() {
    }

    /**
     * Sets the name of the library.
     * 
     * @param name name of the library.
     */
    void setName(String name) {
        this.name = name;
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
     * Sets the home page of the library.
     * 
     * @param homePage home page of the library.
     */
    void setHomePage(String homePage) {
        this.homePage = homePage;
    }

    /**
     * Sets the versions of the library.
     * 
     * @param versions versions of the library.
     */
    void setVersions(Version[] versions) {
        this.versions = versions;
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
     * Returns the description of this library.
     * 
     * @return description of this library.
     */
    public String getDescription() {
        return description;
    }

    /**
     * Returns the home page of this library.
     * 
     * @return home page of this library.
     */
    public String getHomePage() {
        return homePage;
    }

    /**
     * Returns the versions of this library.
     * 
     * @return versions of this library.
     */
    @org.netbeans.api.annotations.common.SuppressWarnings(value = "EI_EXPOSE_REP", justification = "Internal usage only")
    public Version[] getVersions() {
        return versions;
    }

    /**
     * Library version.
     */
    public final static class Version {
        /** Owning library. */
        private final Library library;
        /** Whether this version is already part of the project. */
        private final boolean persisted;
        /** Name or number of the version. */
        private String name;
        /** Files of this version (relative path as specified in CDNJS meta-data). */
        private String[] files;
        /** Local files of this version (relative path from project's folder). */
        private String[] localFiles;

        /**
        * Creates a new {@code Version}.
        */
        Version(Library library, boolean persisted) {
            this.library = library;
            this.persisted = persisted;
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
         * Checks whether the library already is part of the project.
         * <p>
         * In other words, whether the library already is saved in project's
         * metadata (therefore its local files should be on the disk).
         * @return {@code true} if the library already is part of the project.
         */
        boolean isPersisted() {
            return persisted;
        }

        /**
         * Sets the name/number of the version.
         * 
         * @param name name of the version.
         */
        void setName(String name) {
            this.name = name;
        }

        /**
         * Sets the file information of the version.
         * 
         * @param files files of the version.
         * @param localFiles local files of the version (can be {@code null}).
         */
        void setFileInfo(String[] files, String[] localFiles) {
            assert (localFiles == null || files.length == localFiles.length);
            this.files = files;
            this.localFiles = localFiles;
        }

        /**
         * Returns the name/number of this version.
         * 
         * @return name/number of this version.
         */
        public String getName() {
            return name;
        }

        /**
         * Returns the files of this version (as specified by CDNJS meta-data).
         * 
         * @return files of this version.
         */
        @org.netbeans.api.annotations.common.SuppressWarnings(value = "EI_EXPOSE_REP", justification = "Internal usage only")
        public String[] getFiles() {
            return files;
        }

        /**
         * Returns the local files of this version (relative paths
         * from project's folder).
         * 
         * @return local files of this version (or {@code null} when
         * this library version is not installed).
         */
        @org.netbeans.api.annotations.common.SuppressWarnings(value = "EI_EXPOSE_REP", justification = "Internal usage only")
        public String[] getLocalFiles() {
            return localFiles;
        }

        /**
         * Returns a clone of this library version that doesn't contain
         * the specified files. The clone references the same library but
         * is not added into the array of versions of this library.
         * 
         * @param refusedFiles files that should not be included.
         * @return library version that doesn't contain the specified files.
         */
        public Library.Version filterVersion(Collection<String> refusedFiles) {
            Library.Version clone = new Library.Version(library, persisted);
            clone.setName(name);
            List<String> fileList = new ArrayList<>();
            for (String file : files) {
                if (!refusedFiles.contains(file)) {
                    fileList.add(file);
                }
            }
            clone.setFileInfo(fileList.toArray(new String[fileList.size()]), null);
            return clone;
        }

    }
    
}
