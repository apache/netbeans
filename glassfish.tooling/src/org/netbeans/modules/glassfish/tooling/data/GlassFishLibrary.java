/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2015, 2016 Oracle and/or its affiliates. All rights reserved.
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
 */
package org.netbeans.modules.glassfish.tooling.data;

import java.net.URL;
import java.util.List;

/**
 * GlassFish library entity.
 * <p/>
 * @author Peter Benedikovic, Tomas Kraus
 */
public class GlassFishLibrary {

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
     * Creates an instance of GlassFish library entity.
     * <p/>
     * @param libraryID Library name (unique identifier).
     * @param classpath Library class path.
     * @param javadocs  Library java doc.
     * @param sources   Library sources.
     */
    public GlassFishLibrary(final String libraryID,
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
