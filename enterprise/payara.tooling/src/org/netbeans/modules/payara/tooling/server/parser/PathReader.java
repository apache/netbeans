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
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

/**
 * <code>file</code> library configuration XML element reader.
 * <p/>
 * @author Peter Benedikovic, Tomas Kraus
 */
public class PathReader extends AbstractReader {

    ////////////////////////////////////////////////////////////////////////////
    // Class attributes                                                       //
    ////////////////////////////////////////////////////////////////////////////

    /** <code>file</code> XML element name. */
    static final String NODE = "file";

    ////////////////////////////////////////////////////////////////////////////
    // Instance attributes                                                    //
    ////////////////////////////////////////////////////////////////////////////

    /** Paths retrieved from XML elements. */
    private List<String> paths = new LinkedList<>();

    ////////////////////////////////////////////////////////////////////////////
    // Constructors                                                           //
    ////////////////////////////////////////////////////////////////////////////

    /**
     * Creates an instance of Java EE platform check configuration
     * XML element reader.
     * <p/>
     * @param pathPrefix Tree parser path prefix to be prepended before
     *                   current XML element.
     */
    PathReader(final String pathPrefix) {
        super(pathPrefix, NODE);
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
        paths.add(attributes.getValue("path"));
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

    ////////////////////////////////////////////////////////////////////////////
    // Methods                                                                //
    ////////////////////////////////////////////////////////////////////////////

    /**
     * Reset this XML element reader.
     */
    public void reset() {
        paths = new LinkedList<>();
    }

}
