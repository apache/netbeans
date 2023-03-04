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

import org.netbeans.modules.xml.tools.java.generator.ParsletBindings;
import org.netbeans.modules.xml.tools.java.generator.ElementBindings;
import org.xml.sax.*;

public class SAXBindingsHandlerImpl implements SAXBindingsHandler {

    private ParsletBindings parslets = new ParsletBindings();
    private ElementBindings elements = new ElementBindings();

    private static final String ATT_PARSLET = "parslet"; // NOI18N
    private static final String ATT_RETURN = "return"; // NOI18N

    private static final String ATT_ELEMENT = "element"; // NOI18N
    private static final String ATT_TYPE = "type"; // NOI18N
    private static final String ATT_METHOD = "method"; // NOI18N
    
    public ParsletBindings getParsletBindings() {
        if (parslets.isEmpty()) return null;
        return parslets;
    }
    
    public ElementBindings getElementBindings() {
        if (elements.isEmpty()) return null;
        return elements;
    }
    
    public void handle_parslet(final Attributes meta) throws SAXException {
        //if ( Util.THIS.isLoggable() ) /* then */ Util.THIS.debug ("handle_parslet: " + meta); // NOI18N
        
        String parslet = meta.getValue(ATT_PARSLET);
        String back = meta.getValue(ATT_RETURN);
        
        parslets.put(parslet, back);
    }
    
    public void start_SAX_bindings(final Attributes meta) throws SAXException {
        //if ( Util.THIS.isLoggable() ) /* then */ Util.THIS.debug ("start_SAX_bindings: " + meta); // NOI18N
                
    }
    
    public void end_SAX_bindings() throws SAXException {
        //if ( Util.THIS.isLoggable() ) /* then */ Util.THIS.debug ("end_SAX_bindings()"); // NOI18N
    }
    
    public void start_bind(final Attributes meta) throws SAXException {
        //if ( Util.THIS.isLoggable() ) /* then */ Util.THIS.debug ("start_bind: " + meta); // NOI18N
        
        String element = meta.getValue(ATT_ELEMENT);
        String method = meta.getValue(ATT_METHOD);
        String parslet = meta.getValue(ATT_PARSLET);
        String type = meta.getValue(ATT_TYPE);
        
        elements.put(element, method, parslet, type);
    }
    
    public void end_bind() throws SAXException {
        //if ( Util.THIS.isLoggable() ) /* then */ Util.THIS.debug ("end_bind()"); // NOI18N
    }
    
    /**
     * An empty element event handling method.
     * @param data value or null
     */
    public void handle_attbind(Attributes meta) throws SAXException {
    }
    
}
