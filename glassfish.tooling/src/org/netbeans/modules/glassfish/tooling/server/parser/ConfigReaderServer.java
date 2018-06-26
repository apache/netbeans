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
package org.netbeans.modules.glassfish.tooling.server.parser;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import org.netbeans.modules.glassfish.tooling.server.config.FileSet;
import org.netbeans.modules.glassfish.tooling.server.config.JavaEESet;
import org.netbeans.modules.glassfish.tooling.server.config.JavaSESet;
import org.netbeans.modules.glassfish.tooling.server.config.LibraryNode;
import org.netbeans.modules.glassfish.tooling.server.config.Tools;
import org.netbeans.modules.glassfish.tooling.server.parser.TreeParser.Path;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

/**
 * Whole <code>server</code> configuration XML element reader.
 * <p/>
 * @author Peter Benedikovic, Tomas Kraus
 */
public class ConfigReaderServer extends TreeParser.NodeListener implements
        XMLReader {

    ////////////////////////////////////////////////////////////////////////////
    // Instance attributes                                                    //
    ////////////////////////////////////////////////////////////////////////////

    /** <code>classpath</code> library configuration XML element reader. */
    private ConfigReaderClasspath classpathReader = new ConfigReaderClasspath();

    /** <code>javadocs</code> library configuration XML element reader. */
    private ConfigReaderJavadocs javadocsReader = new ConfigReaderJavadocs();

    /** <code>sources</code> library configuration XML element reader. */
    private ConfigReaderSources sourcesReader = new ConfigReaderSources();

    /** Java SE configuration XML element reader. */
    private ConfigReaderJavaSE javaSEReader = new ConfigReaderJavaSE("/server");

    /** Java EE configuration XML element reader. */
    private ConfigReaderJavaEE javaEEReader = new ConfigReaderJavaEE("/server");

    /** Tools configuration XML element reader. */
    private ConfigReaderTools configReaderTools
            = new ConfigReaderTools("/server");

    /** Libraries read from XML file. */
    private List<LibraryNode> libraries = new LinkedList<>();

    /** Library ID. */
    private String actualLibID;

    ////////////////////////////////////////////////////////////////////////////
    // XML reader methods                                                     //
    ////////////////////////////////////////////////////////////////////////////

    /**
     * Provide paths to listen on.
     * <p/>
     * Sets readers for <code>server</code> element and it's content.
     * <p/>
     * @return Paths that the reader listens to.
     */
    @Override
    public List<TreeParser.Path> getPathsToListen() {
        ArrayList<Path> paths = new ArrayList<>(14);
        paths.add(new Path("/server/library", this));
        paths.addAll(classpathReader.getPathsToListen());
        paths.addAll(javadocsReader.getPathsToListen());
        paths.addAll(sourcesReader.getPathsToListen());
        paths.addAll(javaSEReader.getPathsToListen());
        paths.addAll(javaEEReader.getPathsToListen());
        paths.addAll(configReaderTools.getPathsToListen());
        return paths;
    }

    /**
     * Process attributes from current XML element.
     * <p/>
     * @param qname      Not used.
     * @param attributes List of XML attributes.
     * @throws SAXException When any problem occurs.
     */
    @Override
    public void readAttributes(final String qname,
    final Attributes attributes) throws SAXException {
        actualLibID = attributes.getValue("id");
    }

    /**
     * Finish <code>javaee</code> element processing.
     * <p/>
     * @param qname Current XML element name.
     * @throws ServerConfigException when more than one <code>javaee</code>
     *         XML elements were found.
     */
    @Override
    public void endNode(final String qname) throws SAXException {
        if ("library".equals(qname)) {
            FileSet classpath = new FileSet(classpathReader.getPaths(),
                    classpathReader.getFilesets());
            FileSet javadocs = new FileSet(javadocsReader.getPaths(),
                    javadocsReader.getLinks(),
                    javadocsReader.getFilesets(),
                    javadocsReader.getLookups());
            FileSet sources = new FileSet(sourcesReader.getPaths(),
                    sourcesReader.getFilesets());
            LibraryNode config = new LibraryNode(actualLibID, classpath,
                    javadocs,
                    sources);
            libraries.add(config);
            actualLibID = null;
            classpathReader.reset();
            javadocsReader.reset();
            sourcesReader.reset();            
        }                        
    }

    ////////////////////////////////////////////////////////////////////////////
    // Getters and setters                                                    //
    ////////////////////////////////////////////////////////////////////////////

    /**
     * Get libraries read from XML file.
     * <p/>
     * @return Libraries read from XML file.
     */
    public List<LibraryNode> getLibraries() {
        return libraries;
    }

    /**
     * Get JavaEE set for GlassFish features configuration read from XML.
     * <p/>
     * @return JavaEE set for GlassFish features configuration read from XML.
     */
    public JavaEESet getJavaEE() {
        return javaEEReader.javaEE;
    }

    /**
     * Get JavaSE set for GlassFish features configuration read from XML.
     * <p/>
     * @return JavaSE set for GlassFish features configuration read from XML.
     */
    public JavaSESet getJavaSE() {
        return javaSEReader.javaSE;
    }

    /**
     * Get GlassFish tools configuration read from XML.
     * <p/>
     * @return GlassFish tools configuration read from XML.
     */
    public Tools getTools() {
        return configReaderTools.tools;
    }

}
