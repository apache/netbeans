/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2015, 2016 Oracle and/or its affiliates. All rights reserved.
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
 */
package org.netbeans.modules.glassfish.tooling.admin.response;

import java.io.InputStream;
import java.util.*;
import javax.xml.stream.EventFilter;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.Attribute;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;
import org.netbeans.modules.glassfish.tooling.GlassFishIdeException;

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
            throw new GlassFishIdeException("Unable to parse XML Rest response.", ex);
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
