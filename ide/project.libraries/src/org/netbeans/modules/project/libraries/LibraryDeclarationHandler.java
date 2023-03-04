/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.netbeans.modules.project.libraries;

import org.xml.sax.*;

public interface LibraryDeclarationHandler {

    /**
     * Parser starts document parsing
     */
    public void startDocument();

    /**
     * Parser ended document parsing
     */
    public void endDocument();

    /**
     * A container element start event handling method.
     * @param meta attributes
     *
     */
    public void start_volume(final Attributes meta) throws SAXException;

    /**
     * A container element end event handling method.
     *
     */
    public void end_volume() throws SAXException;

    /**
     * A data element event handling method.
     * @param data value or null
     * @param meta attributes
     *
     */
    public void handle_type(final java.lang.String data, final Attributes meta) throws SAXException;
    
    /**
     * A container element start event handling method.
     * @param meta attributes
     *
     */
    public void handle_description(final java.lang.String data, final Attributes meta) throws SAXException;
    
    /**
     * A container element start event handling method.
     * @param meta attributes
     *
     */
    public String start_library(final String nameSpace, final Attributes meta) throws SAXException;
    
    /**
     * A container element end event handling method.
     *
     */
    public void end_library() throws SAXException;
    
    /**
     * A data element event handling method.
     * @param data value or null
     * @param meta attributes
     *
     */
    public void handle_resource(final java.net.URL data, final Attributes meta) throws SAXException;

    /**
     * A data element event handling method.
     * @param data value or null
     * @param meta attributes
     *
     */
    public void handle_name(final java.lang.String data, final Attributes meta) throws SAXException;

    public void handle_localizingBundle (final String data, final Attributes meta) throws SAXException;

    public void handle_displayName (String data, Attributes meta) throws SAXException;

    public void start_properties(final Attributes meta) throws SAXException;
    
    public void end_properties() throws SAXException;
    
    public void start_property(final Attributes meta) throws SAXException;
    
    public void end_property() throws SAXException;
    
    public void handle_value (String data, Attributes meta) throws SAXException;
}

