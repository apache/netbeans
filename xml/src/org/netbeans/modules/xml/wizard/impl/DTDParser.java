/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
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
