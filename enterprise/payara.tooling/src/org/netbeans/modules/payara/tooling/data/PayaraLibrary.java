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

import java.net.URL;
import java.util.List;

/**
 * Payara library entity.
 * <p/>
 * @author Peter Benedikovic, Tomas Kraus
 */
public class PayaraLibrary {

    /**
     * Maven related information for JARs.
     */
    public static class Maven {

        /** Maven Group ID. */
        private final String groupId;

        /** Maven Artifact ID. */
        private final String artifactId;

        /** Maven Version. */
        private final String version;

        /**
         * Creates an instance of Maven related information.
         * <p/>
         * @param groupId    Maven Group ID.
         * @param artifactId Maven Artifact ID.
         * @param version    Maven Version.
         */
        public Maven(final String groupId,
                final String artifactId, final String version) {
            this.groupId = groupId;
            this.artifactId = artifactId;
            this.version = version;
        }
    }

   /** Library name (unique identifier). */
    private final String libraryID;

    /** Library class path containing all JARs to be addedx to project. */
    private final List<URL> classpath;

    /** Library java doc. */
    private final List<URL> javadocs;

    /** Library java doc. */
    private final List<String> javadocLookups;

    /** Library sources. */
    private final List<URL> sources;

    /** Maven artifact information. */
    private final List<Maven> maven;

    /**
     * Creates an instance of Payara library entity.
     * <p/>
     * @param libraryID Library name (unique identifier).
     * @param classpath Library class path.
     * @param javadocs  Library java doc.
     * @param sources   Library sources.
     */
    public PayaraLibrary(final String libraryID,
            final List<URL> classpath, final List<URL> javadocs,
            List<String> javadocLookups,
            final List<URL> sources, final List<Maven> maven) {
        this.libraryID = libraryID;
        this.classpath = classpath;
        this.javadocs = javadocs;
        this.javadocLookups = javadocLookups;
        this.sources = sources;
        this.maven = maven;
    }

    /**
     * Get library name (unique identifier).
     * <p/>
     * @return Library name (unique identifier).
     */
    public String getLibraryID() {
        return libraryID;
    }

    /**
     * Get library class path.
     * <p/>
     * @return Library class path.
     */
    public List<URL> getClasspath() {
        return classpath;
    }

    /**
     * Get library java doc.
     * <p/>
     * @return Library java doc.
     */
    public List<URL> getJavadocs() {
        return javadocs;
    }

    /**
     * Get library java doc lookups.
     * <p/>
     * @return Library java doc lookups.
     */
    public List<String> getJavadocLookups() {
        return javadocLookups;
    }

    /**
     * get library sources.
     * <p/>
     * @return Library sources.
     */
    public List<URL> getSources() {
        return sources;
    }

    /**
     * Get Maven dependencies as
     * <code>&lt;groupId&gt; ':' &lt;artifactId&gt; ':' &lt;version&gt; ':jar'
     * { ' ' &lt;groupId&gt; ':' &lt;artifactId&gt; ':' &lt;version&gt; ':jar'
     * }</ code> {@link String}.
     * <p/>
     * @return Maven dependencies <code>String</code>.
     */
    public String getMavenDeps() {
        StringBuilder sb = new StringBuilder();
        boolean first = true;
        for (Maven mvn : maven) {
            if (first) {
                first = false;
            } else {
                sb.append(' ');
            }
            sb.append(mvn.groupId);
            sb.append(':');
            sb.append(mvn.artifactId);
            sb.append(':');
            sb.append(mvn.version);
            sb.append(":jar");
        }
        return sb.toString();
    }

}
