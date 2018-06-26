/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2013 Oracle and/or its affiliates. All rights reserved.
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
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2013 Sun Microsystems, Inc.
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
