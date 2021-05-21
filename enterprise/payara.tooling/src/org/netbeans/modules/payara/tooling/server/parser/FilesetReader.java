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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

/**
 * <code>fileset</code> library configuration XML element reader.
 * <p/>
 * @author Peter Benedikovic, Tomas Kraus
 */
public class FilesetReader extends TreeParser.NodeListener {

    ////////////////////////////////////////////////////////////////////////////
    // Instance attributes                                                    //
    ////////////////////////////////////////////////////////////////////////////

    /** File sets retrieved from XML elements. */
    private Map<String, List<String>> filesets
            = new HashMap<String, List<String>>();

    /** File set being actually processed. */
    private List<String> actualFileset = null;

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
        String dirName = attributes.getValue("dir");
        if ((actualFileset = filesets.get(dirName)) == null) {
            actualFileset = new ArrayList<>();
            filesets.put(dirName, actualFileset);
        }
    }

    /**
     * Process child elements from current XML element.
     * <p/>
     * @param qname      Not used.
     * @param attributes List of XML attributes.
     * @throws SAXException When any problem occurs.
     */
    @Override
    public void readChildren(final String qname, final Attributes attributes)
            throws SAXException {
        actualFileset.add(attributes.getValue("name"));
    }
    
    ////////////////////////////////////////////////////////////////////////////
    // Getters and setters                                                    //
    ////////////////////////////////////////////////////////////////////////////

    /**
     * Get file sets retrieved from XML elements.
     * <p/>
     * @return File sets retrieved from XML elements.
     */
    public Map<String, List<String>> getFilesets() {
        return filesets;
    }
    
    ////////////////////////////////////////////////////////////////////////////
    // Methods                                                                //
    ////////////////////////////////////////////////////////////////////////////

    /**
     * Reset this XML element reader.
     */
    public void reset() {
        filesets = new HashMap<String, List<String>>();
        actualFileset = null;
    }

}
