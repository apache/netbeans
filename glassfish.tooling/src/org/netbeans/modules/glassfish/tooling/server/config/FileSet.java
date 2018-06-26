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
package org.netbeans.modules.glassfish.tooling.server.config;

import java.util.List;
import java.util.Map;

/**
 * Library content set for library content for GlassFish features configuration.
 * <p/>
 * @author Peter Benedikovic, Tomas Kraus
 */
public class FileSet {

    ////////////////////////////////////////////////////////////////////////////
    // Instance attributes                                                    //
    ////////////////////////////////////////////////////////////////////////////

    /** Paths retrieved from XML elements. */
    private final List<String> paths;

    /** Links retrieved from XML elements. */
    private final List<String> links;

    /** File sets retrieved from XML elements. */
    private final Map<String, List<String>> filesets;

    /** Links retrieved from XML elements. */
    private final List<String> lookups;

    ////////////////////////////////////////////////////////////////////////////
    // Constructors                                                           //
    ////////////////////////////////////////////////////////////////////////////

    /**
     * Creates an instance of Library content for GlassFish libraries
     * configuration.
     * <p/>
     * @param paths    Paths retrieved from XML elements.
     * @param links    Links retrieved from XML elements.
     * @param filesets File sets retrieved from XML elements.
     * @param lookups  Lookups retrieved from XML elements.
     */
    public FileSet(final List<String> paths, final List<String> links,
            final Map<String, List<String>> filesets,
            final List<String> lookups) {
        this.paths = paths;
        this.links = links;
        this.filesets = filesets;
        this.lookups = lookups;
    }

    /**
     * Creates an instance of Library content for GlassFish libraries
     * configuration.
     * <p/>
     * Content of links and lookups is set to <code>null</code>.
     * <p/>
     * @param paths    Paths retrieved from XML elements.
     * @param filesets File sets retrieved from XML elements.
     */
    public FileSet(final List<String> paths,
            final Map<String, List<String>> filesets) {
        this(paths, null, filesets, null);
    }
    
    ////////////////////////////////////////////////////////////////////////////
    // Getters and setters                                                    //
    ////////////////////////////////////////////////////////////////////////////

    /**
     * Get paths retrieved from XML elements.
     * <p/>
     * @return Paths sets retrieved from XML elements.
     */
    public List<String> getPaths() {
        return paths;
    }

    /**
     * Get links retrieved from XML elements.
     * <p/>
     * @return Links sets retrieved from XML elements.
     */
    public List<String> getLinks() {
        return links;
    }

    /**
     * Get file sets retrieved from XML elements.
     * <p/>
     * @return File sets retrieved from XML elements.
     */
    public Map<String, List<String>> getFilesets() {
        return filesets;
    }
    
    /**
     * Get lookups retrieved from XML elements.
     * <p/>
     * @return Links sets retrieved from XML elements.
     */
    public List<String> getLookups() {
        return lookups;
    }

}
