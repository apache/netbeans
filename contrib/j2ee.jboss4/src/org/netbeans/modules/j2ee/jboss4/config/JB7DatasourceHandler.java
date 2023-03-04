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
package org.netbeans.modules.j2ee.jboss4.config;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

/**
 *
 * @author Petr Hejl
 */
public class JB7DatasourceHandler extends DefaultHandler {

    private final List<JBossDatasource> datasources = new ArrayList<JBossDatasource>();

    private StringBuilder content = new StringBuilder();

    private boolean isDatasources;

    private boolean isDatasource;

    private boolean isSecurity;

    private String jndiName;

    private String url;

    private String driverClass;

    private String username;

    private String password;

    public List<JBossDatasource> getDatasources() {
        return datasources;
    }

    @Override
    public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
        content.setLength(0);
        if ("datasources".equals(qName)) {
            isDatasources = true;
        } else if (isDatasources && "datasource".equals(qName)) {
            isDatasource = true;
            jndiName = attributes.getValue("jndi-name");
        } else if (isDatasource && "security".equals(qName)) {
            isSecurity = true;
        }
    }

    @Override
    public void endElement(String uri, String localName, String qName) throws SAXException {
        // FIXME XADataSources support ???
        if (isSecurity) {
            if ("security".equals(qName)) {
                isSecurity = false;
            } else if ("user-name".equals(qName)) {
                username = content.toString();
            } else if ("password".equals(qName)) {
                password = content.toString();
            }
        } else if (isDatasource) {
            if ("datasource".equals(qName)) {
                isDatasource = false;
                if (jndiName != null && url != null) {
                    datasources.add(new JBossDatasource(
                            jndiName, url, username, password, driverClass));
                } else {
                    Logger.getLogger(JB7DatasourceHandler.class.getName()).log(Level.INFO, "Malformed datasource found");
                }
            } else if ("connection-url".equals(qName)) {
                url = content.toString();
            } else if ("driver-class".equals(qName)) {
                // not manadatory
                driverClass = content.toString();
            }    
        } else if (isDatasources) {
            if ("datasources".equals(qName)) {
                isSecurity = false;
                isDatasource = false;
                isDatasources = false;
            }
        }
    }

    @Override
    public void characters(char[] ch, int start, int length) throws SAXException {
        if (isDatasource) {
            content.append(ch, start, length);
        }
    }
}
