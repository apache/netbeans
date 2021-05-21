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

import org.netbeans.modules.payara.tooling.server.config.ServerConfigException;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

/**
 * <code>asadmin</code> tool configuration XML element reader.
 * <p/>
 * @author Peter Benedikovic, Tomas Kraus
 */
public class ToolsAsadminReader extends AbstractReader {

    ////////////////////////////////////////////////////////////////////////////
    // Class attributes                                                       //
    ////////////////////////////////////////////////////////////////////////////

    /** <code>asadmin</code> XML element name. */
    private static final String NODE = "asadmin";

    /** <code>type</code> XML element attribute name. */
    private static final String JAR_ATTR = "jar";

    ////////////////////////////////////////////////////////////////////////////
    // Instance attributes                                                    //
    ////////////////////////////////////////////////////////////////////////////

    /** Platforms retrieved from XML elements. */
    private String jar;

    ////////////////////////////////////////////////////////////////////////////
    // Constructors                                                           //
    ////////////////////////////////////////////////////////////////////////////

    /**
     * Creates an instance of <code>asadmin</code> tool configuration
     * XML element reader.
     * <p/>
     * @param pathPrefix Tree parser path prefix to be prepended before
     *        current XML element.
     */
    ToolsAsadminReader(final String pathPrefix) throws ServerConfigException {
        super(pathPrefix, NODE);
        jar = null;
    }

    ////////////////////////////////////////////////////////////////////////////
    // Tree parser methods                                                    //
    ////////////////////////////////////////////////////////////////////////////

    /**
     * Process attributes from current XML element.
     * <p/>
     * @param qname      Not used.
     * @param attributes List of XML attributes.
     * @throws SAXException When any problem occurs.
     */
    @Override
    public void readAttributes(final String qname, final Attributes attributes)
            throws SAXException {
        jar = attributes.getValue(JAR_ATTR);
    }

    ////////////////////////////////////////////////////////////////////////////
    // Getters and setters                                                    //
    ////////////////////////////////////////////////////////////////////////////

    /**
     * Get asadmin tool JAR.
     * <p/>
     * @return Asadmin tool JAR.
     */
    String getJar() {
        return jar;
    }

    ////////////////////////////////////////////////////////////////////////////
    // Methods                                                                //
    ////////////////////////////////////////////////////////////////////////////

    /**
     * Reset this XML element reader.
     */
    void reset() {
        jar = null;
    }

}
