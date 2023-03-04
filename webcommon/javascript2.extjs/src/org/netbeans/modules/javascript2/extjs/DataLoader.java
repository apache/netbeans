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
package org.netbeans.modules.javascript2.extjs;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import org.openide.util.Exceptions;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

/**
 *
 * @author Petr Pisl
 */
public class DataLoader extends DefaultHandler {
    
    private static final Logger LOGGER = Logger.getLogger(DataLoader.class.getName());
    
    private static Map<String, Collection<ExtJsDataItem>> result = new HashMap<String, Collection<ExtJsDataItem>>();
    
    public static Map<String, Collection<ExtJsDataItem>> getData(File file) {
        result.clear();
        try {
            long start = System.currentTimeMillis();
            SAXParserFactory factory = SAXParserFactory.newInstance();
            SAXParser parser = factory.newSAXParser();
            DefaultHandler handler = new DataLoader();
            parser.parse(file, handler);
            long end = System.currentTimeMillis();
            LOGGER.log(Level.FINE, "Loading data from file took {0}ms ",  (end - start)); //NOI18N
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        } catch (ParserConfigurationException ex) {
            Exceptions.printStackTrace(ex);
        } catch (SAXException ex) {
            Exceptions.printStackTrace(ex);
        }
        return result;
    }
    
    private static final String TYPE = "type";   //NOI18N
    private static final String NAME = "name";   //NOI18N
    
    private enum Tag {
        object, property, doc, template, notinterested;
    }
    
    private String objectName;
    private String name;
    private String type;
    private String documentation;
    private String template;
    private List<ExtJsDataItem> items;
    
    private Tag inTag = Tag.notinterested;
    
    @Override
    public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
        if (qName.equals(Tag.object.name())) {
            objectName = attributes.getValue(NAME);
            items = new ArrayList<ExtJsDataItem>();
        } else if (qName.equals(Tag.property.name())) {
            name = attributes.getValue(NAME);
            type = attributes.getValue(TYPE);
            documentation = "";
            template = "";
        }
        try {
            inTag = Tag.valueOf(qName); 
        } catch (IllegalArgumentException iae) {
            inTag = Tag.notinterested;
        }
    }

    @Override
    public void endElement(String uri, String localName, String qName) throws SAXException {
        if (qName.equals(Tag.object.name())) {
            result.put(objectName, items);
        } else if (qName.equals(Tag.property.name())) {
            items.add(new ExtJsDataItem(name, type, documentation, template));
        }
    }

    @Override
    public void characters(char[] ch, int start, int length) throws SAXException {
        switch (inTag) {
            case doc:
                documentation = documentation + new String(ch, start, length);
                break;
            case template:
                template = template + new String(ch, start, length);
                break;
            default:
        }
    }
}
