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
package org.netbeans.modules.javaee.wildfly.config.xml.jms;

import java.util.ArrayList;
import java.util.List;
import org.netbeans.modules.j2ee.deployment.common.api.MessageDestination.Type;
import org.netbeans.modules.javaee.wildfly.config.WildflyMessageDestination;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

/**
 *
 * @author Petr Hejl
 */
public class WildflyMessageDestinationHandler extends DefaultHandler {

    private final List<WildflyMessageDestination> messageDestinations = new ArrayList<WildflyMessageDestination>();

    private boolean isDestinations;

    private boolean isDestination;

    private final List<String> jndiNames = new ArrayList<String>();

    private WildflyMessageDestination currentDestination;

    public List<WildflyMessageDestination> getMessageDestinations() {
        return messageDestinations;
    }

    @Override
    public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
        if ("jms-destinations".equals(qName)) {
            isDestinations = true;
        } else if (isDestinations && ("jms-queue".equals(qName) || "jms-topic".equals(qName))) {
            String name = attributes.getValue("name");
            if("jms-queue".equals(qName)) {
               currentDestination = new WildflyMessageDestination(name, Type.QUEUE);
            } else if ("jms-topic".equals(qName)) {
                currentDestination = new WildflyMessageDestination(name, Type.TOPIC);
            } else {
                currentDestination = null;
            }
            isDestination = true;
        } else if (isDestination && "entry".equals(qName)) {
            jndiNames.add(attributes.getValue("name"));
        }
    }

    @Override
    public void endElement(String uri, String localName, String qName) throws SAXException {
        if (isDestination) {
            if ("jms-queue".equals(qName) || "jms-topic".equals(qName)) {
                isDestination = false;
                for (String jndiName : jndiNames) {
                    currentDestination.addEntry(jndiName);
                }
                jndiNames.clear();
                messageDestinations.add(currentDestination);
                currentDestination = null;
            }
        } else if (isDestinations) {
            if ("jms-destinations".equals(qName)) {
                jndiNames.clear();
                isDestination = false;
                isDestinations = false;
            }
        }
    }

}
