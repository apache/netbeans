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
package org.netbeans.modules.testng.ui;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.openide.filesystems.FileObject;
import org.openide.xml.XMLUtil;
import org.testng.xml.TestNGContentHandler;
import org.xml.sax.*;

/**
 *
 * @author lukas
 */
public class XmlSuiteHandler extends TestNGContentHandler {

    private static final Logger LOGGER = Logger.getLogger(XmlSuiteHandler.class.getName());
    private Locator loc;
    private String suite;
    private int line;
    private int column;

    private XmlSuiteHandler(String fName, String name) {
        super(fName, false);
        suite = name;
    }

    public static int[] getSuiteLocation(FileObject suiteFile, String suiteName) {
        int[] location = new int[]{0, 0};
        try {
            XMLReader r = XMLUtil.createXMLReader(false, false);
            XmlSuiteHandler sl = new XmlSuiteHandler(suiteFile.getName(), suiteName);
            r.setContentHandler(sl);
            r.parse(new InputSource(suiteFile.getInputStream()));
            location[0] = sl.getLine();
            location[1] = sl.getColumn();
        } catch (IOException ex) {
            LOGGER.log(Level.WARNING, null, ex);
        } catch (SAXException ex) {
            LOGGER.log(Level.WARNING, null, ex);
        }
        return location;
    }

    @Override
    public void setDocumentLocator(Locator locator) {
        loc = locator;
    }

    @Override
    public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
        if ("test".equals(qName) && attributes != null && suite.equals(attributes.getValue("name"))) {
            line = loc.getLineNumber();
            column = loc.getColumnNumber() - suite.length() - 3;
        }
    }

    @Override
    public void endElement(String uri, String localName, String qName) throws SAXException {
    }

    public int getLine() {
        return line;
    }

    public int getColumn() {
        return column;
    }
}
