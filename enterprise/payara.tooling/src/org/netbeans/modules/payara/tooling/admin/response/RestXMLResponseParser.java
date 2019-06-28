/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.netbeans.modules.payara.tooling.admin.response;

import java.io.InputStream;
import java.util.*;
import javax.xml.stream.EventFilter;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.Attribute;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;
import org.netbeans.modules.payara.tooling.PayaraIdeException;

/**
 * Response parser implementation that can parse XML responses
 * returned by REST admin interface.
 * <p>
 * @author Tomas Kraus, Peter Benedikovic
 */
public class RestXMLResponseParser extends RestResponseParser {

    private static final String ENTRY = "entry";
    private static final String MAP = "map";

    private static final XMLInputFactory factory = XMLInputFactory.newInstance();
    private static final RestXMLResponseFilter filter = new RestXMLResponseFilter();

    public RestXMLResponseParser() {

    }

    /**
     * Parse implementation for XML REST response.
     * <p>
     * This implementation is based on Stax parser. Currently REST admin service
     * does not use any schema for XML responses so this implementation is based
     * on the code that generates the response on server side.
     * <p>
     * @param in {@link InputStream} with XML REST response.
     * @return Response returned by REST administration service.
     */
    @Override
    public RestActionReport parse(InputStream in) {
        //System.out.println("FACTORY: " + factory);
        try {
            XMLEventReader reader = factory.createFilteredReader(factory.createXMLEventReader(in), filter);
            if (reader.hasNext() && MAP.equals(reader.nextEvent().asStartElement().getName().getLocalPart())) {
                return parseReport(reader);
            } else {
                return null;
            }
        } catch (XMLStreamException ex) {
            throw new PayaraIdeException("Unable to parse XML Rest response.", ex);
        }
    }

    private RestActionReport parseReport(XMLEventReader reader) throws XMLStreamException {
        int level = 0;
        RestActionReport report = new RestActionReport();
        while (reader.hasNext() && (level > -1)) {
                XMLEvent event = reader.nextEvent();
                if (event.isStartElement()) {

                    if (level++ == 0) {
                        StartElement element = event.asStartElement();
                        String elementName = element.getName().getLocalPart();
                        if (ENTRY.equals(elementName)) {
                            Map<String, String> m = getMapEntry(element);
                            String key = m.get("key");
                            String value = m.get("value");
                            switch (key) {
                                case "message":
                                    report.setMessage(value);
                                    break;
                                case "exit_code":
                                    report.setExitCode(ActionReport.ExitCode.valueOf(value));
                                    break;
                                case "command":
                                    report.setActionDescription(value);
                                    break;
                                case "children":
                                    report.topMessagePart.children = parseChildrenMessages(reader);
                                    level--;
                                    break;
                                case "subReports":
                                    report.subActions = parseSubReports(reader);
                                    break;
                            }
                        }
                    }

                }

                if (event.isEndElement()) {
                    level--;
                }
            }
        return report;
    }

    private HashMap<String, String> getMapEntry(StartElement entry) {
        HashMap<String, String> entryMap = new HashMap<>();
        Iterator iter = entry.getAttributes();
        while (iter.hasNext()) {
            Attribute att = (Attribute) iter.next();
            entryMap.put(att.getName().getLocalPart(), att.getValue());
        }
        return entryMap;
    }

    private List<MessagePart> parseChildrenMessages(XMLEventReader reader) throws XMLStreamException {
        ArrayList<MessagePart> messages = new ArrayList<>();
        int level = 0;
        while (reader.hasNext() && (level > -1)) {
            XMLEvent event = reader.nextEvent();
            if (event.isStartElement()) {
                if (level++ == 1) {
                    StartElement element = event.asStartElement();
                    String elementName = element.getName().getLocalPart();
                    if (MAP.equals(elementName)) {
                        messages.add(parseChildMessage(reader));
                        level--;
                    }
                }
            } else {
                level--;
            }

        }
        return messages;
    }

    private MessagePart parseChildMessage(XMLEventReader reader) throws XMLStreamException {
        MessagePart msg = new MessagePart();
        int level = 0;
        while (reader.hasNext() && (level > -1)) {
            XMLEvent event = reader.nextEvent();
            if (event.isStartElement()) {
                if (level++ == 0) {
                    StartElement element = event.asStartElement();
                    String elementName = element.getName().getLocalPart();
                    if (ENTRY.equals(elementName)) {
                        Map<String, String> m = getMapEntry(element);
                        String key = m.get("key");
                        String value = m.get("value");
                        switch (key) {
                            case "message":
                                msg.setMessage(value);
                                break;
                            case "properties":
                                msg.props = parseProperties(reader);
                                break;
                            case "children":
                                msg.children = parseChildrenMessages(reader);
                                break;
                        }
                    }
                }
            } else {
                level--;
            }
        }
        return msg;
    }

    private Properties parseProperties(XMLEventReader reader) {
        Properties props = new Properties();
        // TODO parsing of properties
        return props;
    }

    private List<? extends ActionReport> parseSubReports(XMLEventReader reader) {
        ArrayList<RestActionReport> subReports = new ArrayList<>();
        return subReports;
    }


    static private class RestXMLResponseFilter implements EventFilter {

        @Override
        public boolean accept(XMLEvent event) {
            if (event.isStartElement() || event.isEndElement()) {
                return true;
            } else {
                return false;
            }
        }

    }

}
