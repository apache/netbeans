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
import org.netbeans.modules.j2ee.deployment.common.api.MessageDestination.Type;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

/**
 *
 * @author Petr Hejl
 */
public class JB7MessageDestinationHandler extends DefaultHandler {

    private final List<JBossMessageDestination> messageDestinations = new ArrayList<JBossMessageDestination>();

    private boolean isDestinations;

    private boolean isDestination;

    private List<String> jndiNames = new ArrayList<String>();

    public List<JBossMessageDestination> getMessageDestinations() {
        return messageDestinations;
    }

    @Override
    public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
        if ("jms-destinations".equals(qName)) {
            isDestinations = true;
        } else if (isDestinations && ("jms-queue".equals(qName) || "jms-topic".equals(qName))) {
            isDestination = true;
        } else if (isDestination && "entry".equals(qName)) {
            jndiNames.add(attributes.getValue("name"));
        }
    }

    @Override
    public void endElement(String uri, String localName, String qName) throws SAXException {
        if (isDestination) {
            if ("jms-queue".equals(qName)) {
                isDestination = false;
                for (String name : jndiNames) {
                    messageDestinations.add(new JBossMessageDestination(name, Type.QUEUE));
                }
                jndiNames.clear();
            } else if ("jms-topic".equals(qName)) {
                isDestination = false;
                for (String name : jndiNames) {
                    messageDestinations.add(new JBossMessageDestination(name, Type.TOPIC));
                }
                jndiNames.clear();
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
