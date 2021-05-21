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

import java.util.LinkedList;
import java.util.List;
import org.netbeans.modules.payara.tooling.server.config.ServerConfigException;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

/**
 * <code>platform</code> Java SE configuration XML element reader.
 * <p/>
 * @author Peter Benedikovic, Tomas Kraus
 */
public class JavaSEPlatformReader extends AbstractReader {

    ////////////////////////////////////////////////////////////////////////////
    // Class attributes                                                       //
    ////////////////////////////////////////////////////////////////////////////

    /** <code>platform</code> XML element name. */
    private static final String NODE = "platform";

    /** <code>type</code> XML element attribute name. */
    private static final String VERSION_ATTR = "version";

    ////////////////////////////////////////////////////////////////////////////
    // Instance attributes                                                    //
    ////////////////////////////////////////////////////////////////////////////

    /** Platforms retrieved from XML elements. */
    private List<String> platforms;

    ////////////////////////////////////////////////////////////////////////////
    // Constructors                                                           //
    ////////////////////////////////////////////////////////////////////////////

    /**
     * Creates an instance of <code>platform</code> Java EE configuration
     * XML element reader.
     * <p/>
     * @param pathPrefix Tree parser path prefix to be prepended before
     *        current XML element.
     */
    JavaSEPlatformReader(final String pathPrefix) throws ServerConfigException {
        super(pathPrefix, NODE);
        platforms = new LinkedList<>();
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
        platforms.add(attributes.getValue(VERSION_ATTR));
    }

    ////////////////////////////////////////////////////////////////////////////
    // Getters and setters                                                    //
    ////////////////////////////////////////////////////////////////////////////

    /**
     * Get platforms retrieved from XML elements.
     * <p/>
     * @return Platforms retrieved from XML elements.
     */
    public List<String> getPlatforms() {
        return platforms;
    }

    ////////////////////////////////////////////////////////////////////////////
    // Methods                                                                //
    ////////////////////////////////////////////////////////////////////////////

    /**
     * Reset this XML element reader.
     */
    public void reset() {
        platforms = new LinkedList<>();
    }

}
