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

package org.netbeans.modules.xml.wizard.impl;

import java.io.*;
import java.util.*;

import org.xml.sax.*;
import org.xml.sax.ext.*;
import org.xml.sax.helpers.*;

import org.openide.xml.*;

import org.netbeans.api.xml.services.UserCatalog;
import org.netbeans.api.xml.parsers.SAXEntityParser;
import org.netbeans.modules.xml.util.Util;

/**
 * Silently produces Set<String> of roots from passed SAX declaration handler events.
 *
 * @author  Petr Kuzel
 */
public final class DTDParser extends DefaultHandler implements DeclHandler {

    static final String SAX_PROPERTY = "http://xml.org/sax/properties/";        // NOI18N
    static final String DECL_HANDLER = "declaration-handler";                   // NOI18N
    
    private final Set roots = new TreeSet();
            
    /** Creates new DTDParser */
    public DTDParser() {
    }

    /**
     * @param in if filled only SID and PID the entity catalog "normalization" is used
     */
    public Set parse(InputSource in) {

        Util.THIS.debug("DTDParser started.");
                
        try {
            // we do not want Crimson, it does not understand relative SYSTEM ids
            XMLReader parser = XMLUtil.createXMLReader(true);   
            parser.setContentHandler(this);
            parser.setErrorHandler(this);
            parser.setProperty(SAX_PROPERTY + DECL_HANDLER, this);
            
            // provide fake entity resolver and source
            
            UserCatalog catalog = UserCatalog.getDefault();
            EntityResolver res = (catalog == null ? null : catalog.getEntityResolver());
            
            if (res != null) parser.setEntityResolver(res);
            
            SAXEntityParser dtdParser = new SAXEntityParser(parser, false);
            dtdParser.parse(in);
            
            throw new IllegalStateException("How we can get here?");            // NOI18N
        } catch (Stop stop) {
            return roots;  // expected
        } catch (SAXException ex) {
            Util.THIS.debug("Ignoring SAX ex. while parsing DTD:", ex);         // NOI18N
            if (ex.getException() instanceof RuntimeException) {
                Util.THIS.debug("Nested exception:", ex.getException());        // NOI18N
            }            
            return roots;  // better partial result than nothing
        } catch (IOException ex) {
            Util.THIS.debug("Ignoring I/O ex. while parsing DTD:", ex);         // NOI18N
            return roots;  // better partial result than nothing
        } finally {
            Util.THIS.debug("DTDParser stopped.");                              // NOI18N
        }
    }
            
    public void elementDecl(String name, String model) throws SAXException {
        Util.THIS.debug("\telementDecl(" + name + ",...)");                     // NOI18N
        roots.add(name);
    }

    public void externalEntityDecl(String name, String publicId, String systemId) throws SAXException {
    }

    public void attributeDecl(String eName, String aName, String type, String valueDefault, String value) throws SAXException {
    }

    public void internalEntityDecl(String name, String value) throws SAXException {
    }

    public void notationDecl (String name, String publicId, String systemId) throws SAXException {
    }                

    public void startElement (String namespaceURI, String localName, String qName, Attributes atts) throws SAXException {
        Util.THIS.debug("\tstopping parser!");                                  // NOI18N
        throw new Stop();
    }

    private class Stop extends SAXException {
        
        private static final long serialVersionUID = -64662796017444980L;
        
        Stop() {
            super("STOP");                                                      //NOI18N
        }
        
        public Throwable fillInStackTrace() {
            return this;
        }
    }
}
