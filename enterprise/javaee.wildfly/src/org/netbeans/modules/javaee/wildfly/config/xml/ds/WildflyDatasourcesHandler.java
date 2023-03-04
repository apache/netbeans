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

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import org.netbeans.modules.j2ee.deployment.common.api.Datasource;
import org.netbeans.modules.javaee.wildfly.config.WildflyDatasource;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;

/**
 *
 * @author Emmanuel Hugonnet (ehsavoie) <ehsavoie@netbeans.org>
 */
public class WildflyDatasourcesHandler extends DefaultHandler {

    private final XMLReader parser;
    private final Set<Datasource> datasources = new HashSet<Datasource>();
    private final Set<WildflyDataSource> parsedDatasources = new HashSet<WildflyDataSource>();

    boolean isDatasource = false;

    private WildflyDatasourceHandler datasourceHandler;
    private WildflyDriversHandler driversHandler;

    public WildflyDatasourcesHandler(XMLReader parser) {
        this.parser = parser;
    }

    public Set<Datasource> getDatasources() {
        return datasources;
    }

    @Override
    public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
        isDatasource = isDatasource || "datasources".equals(qName);
        if (isDatasource) {
            if ("datasource".equals(qName)) {
                datasourceHandler = new WildflyDatasourceHandler(this, parser);
                datasourceHandler.start(uri, localName, qName, attributes);
            }
            else if ("drivers".equals(qName)) {
                driversHandler = new WildflyDriversHandler(this, parser);
                driversHandler.start(uri, localName, qName, attributes);
            }
        }
    }

    @Override
    public void endElement(String uri, String localName, String qName) throws SAXException {
        if (isDatasource) {
            if ("datasource".equals(qName)) {
                parsedDatasources.add(datasourceHandler.getDatasource());
                datasourceHandler = null;
            }
            else if ("drivers".equals(qName)) {
                Map<String, WildflyDriver> drivers = driversHandler.getDrivers();
                for (WildflyDataSource ds : parsedDatasources) {
                    if (drivers.containsKey(ds.getDriver())) {
                        ds.setDriver(drivers.get(ds.getDriver()).getDriverClass());
                    }
                }
                driversHandler = null;
            }
            else if ("datasources".equals(qName)) {
                for (WildflyDataSource ds : parsedDatasources) {
                    datasources.add(new WildflyDatasource(ds.getName(),
                            ds.getJndiName(), ds.getUrl(), ds.getUsername(),
                            ds.getPassword(), ds.getDriver()));
                }
            }
        }
    }
}
