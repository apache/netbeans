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
package org.netbeans.modules.welcome.content;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 * Entity resolver to make it possible to parse an RSS Feed (Version 0.91) with
 * an XMLReader. The license of the specification and the grammar file is 
 * unclear. As the grammer is not used in the welcome module and the stream is
 * manually parsed, only entities need to be resolved by the XML parser.
 * The XML stream can contain Latin-1 entities, which are taken
 * from the HTML 3.2 specification. Instead of the HTML 3.2 DTDs (SGML), the
 * XHTML specification is used as a replacement here.
 */
public class RSSEntityResolver implements EntityResolver {

    private static final Logger LOG = Logger.getLogger(RSSEntityResolver.class.getName());
    private static final Map<String, String> EMBEDDED_DTDS = new HashMap<String, String>();

    static {
        String base = "/org/netbeans/modules/welcome/resources/";
        // This entry is intended to map the Netscape RSS publicId to the
        // DTD for XHTML 1
        EMBEDDED_DTDS.put("-//Netscape Communications//DTD RSS 0.91//EN", 
                RSSEntityResolver.class.getResource(base + "xhtml1-transitional.dtd")
                        .toExternalForm());
        EMBEDDED_DTDS.put("-//W3C//ENTITIES Latin 1 for XHTML//EN", 
                RSSEntityResolver.class.getResource(base + "xhtml-lat1.ent")
                        .toExternalForm());
        EMBEDDED_DTDS.put("-//W3C//ENTITIES Symbols for XHTML//EN", 
                RSSEntityResolver.class.getResource(base + "xhtml-symbol.ent")
                        .toExternalForm());
        EMBEDDED_DTDS.put("-//W3C//ENTITIES Special for XHTML//EN", 
                RSSEntityResolver.class.getResource(base + "xhtml-special.ent")
                        .toExternalForm());
    }

    @Override
    public InputSource resolveEntity(String publicId, String systemId) throws SAXException, IOException {
        String url = EMBEDDED_DTDS.get(publicId);
        LOG.log(Level.FINE,
                "Resolving publicId({0}) to: {1}", //NOI18N
                new Object[] {publicId, url});
        if (url != null) {
            return new InputSource(url);
        } else {
            return null;
        }
    }
}
