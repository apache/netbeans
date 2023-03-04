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
package org.netbeans.modules.javaee.wildfly.config.xml.ds;

import org.netbeans.modules.javaee.wildfly.config.xml.AbstractHierarchicalHandler;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;

/**
 *
 * @author Emmanuel Hugonnet (ehsavoie) <ehsavoie@netbeans.org>
 */
public class WildflyDriverHandler extends AbstractHierarchicalHandler {

    private WildflyDriver driver;
    private StringBuilder buffer;

    public WildflyDriverHandler(DefaultHandler parent, XMLReader parser) {
        super(parent, parser);
    }

    @Override
    public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
        if ("driver".equals(qName)) {
            driver = new WildflyDriver(attributes.getValue(uri, "name"));
        } else if ("xa-datasource-class".equals(qName) || "datasource-class".equals(qName) || "driver-class".equals(qName)) {
            buffer = new StringBuilder();
        }

    }

    @Override
    public void characters(char[] ch, int start, int length) throws SAXException {
        if (buffer != null) {
            buffer.append(ch, start, length);
        }
    }

    @Override
    public void endElement(String uri, String localName, String qName) throws SAXException {
        if ("driver".equals(qName)) {
            end(uri, localName, qName);
        } else if ("driver-class".equals(qName)) {
            driver.setDriverClass(buffer.toString());
        } else if ("xa-datasource-class".equals(qName) || "datasource-class".equals(qName)) {
            if (null == driver.getDriverClass()) {
                driver.setDriverClass("");
            }
        }/*else if ("xa-datasource-class".equals(qName) || "datasource-class".equals(qName) || "driver-class".equals(qName)) {
         driver.setDriverClass(buffer.toString());
         } */

    }

    public WildflyDriver getDriver() {
        return driver;
    }
}
