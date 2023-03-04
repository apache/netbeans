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
package org.netbeans.modules.javaee.wildfly.config.xml;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;

/**
 *
 * @author Emmanuel Hugonnet (ehsavoie) <ehsavoie@netbeans.org>
 */
public class AbstractHierarchicalHandler extends DefaultHandler {

    protected final DefaultHandler parent;
    protected final XMLReader parser;

    public AbstractHierarchicalHandler(DefaultHandler parent, XMLReader parser) {
        this.parent = parent;
        this.parser = parser;
    }

    public void start(String uri, String localName, String qName, Attributes attributes) throws SAXException {
        parser.setContentHandler(this);
        parser.setEntityResolver(this);
        parser.setErrorHandler(this);
        parser.setDTDHandler(this);
        startElement(uri, localName, qName, attributes);
    }

    public void end(String uri, String localName, String qName) throws SAXException {
        parser.setContentHandler(parent);
        parser.setEntityResolver(parent);
        parser.setErrorHandler(parent);
        parser.setDTDHandler(parent);
        parent.endElement(uri, localName, qName);
    }
}
