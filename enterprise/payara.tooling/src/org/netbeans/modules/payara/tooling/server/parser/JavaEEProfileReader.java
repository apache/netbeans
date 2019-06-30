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
 * <code>profile</code> Java EE configuration XML element reader.
 * <p/>
 * @author Peter Benedikovic, Tomas Kraus
 */
public class JavaEEProfileReader extends AbstractReader {

    ////////////////////////////////////////////////////////////////////////////
    // Inner classes                                                          //
    ////////////////////////////////////////////////////////////////////////////

    /** Java EE profile values from XML element. */
    public class Profile {

        /** Java EE profile version. */
        final String version;

        /** Java EE profile type. */
        final String type;

        /** Java EE profile check reference. */
        final String check;

        /**
         * Creates an instance of Java EE profile values from XML element.
         * <p/>
         * @param version Java EE profile version.
         * @param type    Java EE profile type.
         * @param check   Java EE profile check reference.
         */
        Profile(final String version, final String type, final String check) {
            this.version = version;
            this.type = type;
            this.check = check;
        }

        /**
         * Get Java EE profile version.
         * <p/>
         * @return Java EE profile version.
         */
        public String getVersion() {
            return version;
        }

        /**
         * Get Java EE profile type.
         * <p/>
         * @return Java EE profile type.
         */
        public String getType() {
            return type;
        }

        /**
         * Get Java EE profile check reference.
         * <p/>
         * @return Java EE profile check reference.
         */
        public String getCheck() {
            return check;
        }
    }

    ////////////////////////////////////////////////////////////////////////////
    // Class attributes                                                       //
    ////////////////////////////////////////////////////////////////////////////

    /** <code>javaee</code> XML element name. */
    private static final String NODE = "profile";

    /** <code>version</code> XML element attribute name. */
    private static final String VERSION_ATTR = "version";

    /** <code>type</code> XML element attribute name. */
    private static final String TYPE_ATTR = "type";

    /** <code>check</code> XML element attribute name. */
    private static final String CHECK_ATTR = "check";

    ////////////////////////////////////////////////////////////////////////////
    // Instance attributes                                                    //
    ////////////////////////////////////////////////////////////////////////////

    /** Profiles retrieved from XML elements. */
    private List<Profile> profiles;


    ////////////////////////////////////////////////////////////////////////////
    // Constructors                                                           //
    ////////////////////////////////////////////////////////////////////////////

    /**
     * Creates an instance of <code>profile</code> Java EE configuration
     * XML element reader.
     * <p/>
     * @param pathPrefix Tree parser path prefix to be prepended before
     *        current XML element.
     */
    JavaEEProfileReader(final String pathPrefix) throws ServerConfigException {
        super(pathPrefix, NODE);
        profiles = new LinkedList<>();
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
        profiles.add(new Profile(attributes.getValue(VERSION_ATTR),
                attributes.getValue(TYPE_ATTR),
                attributes.getValue(CHECK_ATTR)));
    }

    ////////////////////////////////////////////////////////////////////////////
    // Getters and setters                                                    //
    ////////////////////////////////////////////////////////////////////////////

    /**
     * Get profiles retrieved from XML elements.
     * <p/>
     * @return Profiles retrieved from XML elements.
     */
    public List<Profile> getProfiles() {
        return profiles;
    }

    ////////////////////////////////////////////////////////////////////////////
    // Methods                                                                //
    ////////////////////////////////////////////////////////////////////////////

    /**
     * Reset this XML element reader.
     */
    public void reset() {
        profiles = new LinkedList<>();
    }

}
