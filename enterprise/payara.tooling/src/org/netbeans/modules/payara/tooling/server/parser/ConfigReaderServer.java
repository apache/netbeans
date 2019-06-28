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
package org.netbeans.modules.payara.tooling.server.parser;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import org.netbeans.modules.payara.tooling.server.config.FileSet;
import org.netbeans.modules.payara.tooling.server.config.JavaEESet;
import org.netbeans.modules.payara.tooling.server.config.JavaSESet;
import org.netbeans.modules.payara.tooling.server.config.LibraryNode;
import org.netbeans.modules.payara.tooling.server.config.Tools;
import org.netbeans.modules.payara.tooling.server.parser.TreeParser.Path;
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
     * Get JavaEE set for Payara features configuration read from XML.
     * <p/>
     * @return JavaEE set for Payara features configuration read from XML.
     */
    public JavaEESet getJavaEE() {
        return javaEEReader.javaEE;
    }

    /**
     * Get JavaSE set for Payara features configuration read from XML.
     * <p/>
     * @return JavaSE set for Payara features configuration read from XML.
     */
    public JavaSESet getJavaSE() {
        return javaSEReader.javaSE;
    }

    /**
     * Get Payara tools configuration read from XML.
     * <p/>
     * @return Payara tools configuration read from XML.
     */
    public Tools getTools() {
        return configReaderTools.tools;
    }

}
