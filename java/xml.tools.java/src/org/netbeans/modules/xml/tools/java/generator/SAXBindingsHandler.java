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
package org.netbeans.modules.xml.tools.java.generator;

import org.xml.sax.*;

public interface SAXBindingsHandler {

    /**
     * An empty element event handling method.
     * @param data value or null
     */
    public void handle_parslet(final Attributes meta) throws SAXException;

    /**
     * A container element start event handling method.
     * @param meta attributes
     */
    public void start_SAX_bindings(final Attributes meta) throws SAXException;

    /**
     * A container element end event handling method.
     */
    public void end_SAX_bindings() throws SAXException;
    
    /**
     * An empty element event handling method.
     * @param data value or null
     */
    public void handle_attbind(final Attributes meta) throws SAXException;
    
    /**
     * A container element start event handling method.
     * @param meta attributes
     */
    public void start_bind(final Attributes meta) throws SAXException;
    
    /**
     * A container element end event handling method.
     */
    public void end_bind() throws SAXException;
    
}
