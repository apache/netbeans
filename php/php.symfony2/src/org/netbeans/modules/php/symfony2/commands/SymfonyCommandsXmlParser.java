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
package org.netbeans.modules.php.symfony2.commands;

import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.modules.php.api.util.FileUtils;
import org.netbeans.modules.php.api.util.StringUtils;
import org.openide.util.NbBundle.Messages;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;

/**
 * Symfony 2/3 XML commands parser.
 */
public final class SymfonyCommandsXmlParser extends DefaultHandler {

    private static final Logger LOGGER = Logger.getLogger(SymfonyCommandsXmlParser.class.getName());

    private final XMLReader xmlReader;
    private final List<SymfonyCommandVO> commands;

    private String currentCommand = null;
    private List<StringBuilder> currentUsages = null;
    private String currentDescription = null;
    private StringBuilder currentHelp = null;
    private Content content = Content.NONE;


    private SymfonyCommandsXmlParser(List<SymfonyCommandVO> commands) throws SAXException {
        assert commands != null;

        this.commands = commands;
        xmlReader = FileUtils.createXmlReader();
    }

    public static void parse(Reader reader, List<SymfonyCommandVO> commands) {
        try {
            SymfonyCommandsXmlParser parser = new SymfonyCommandsXmlParser(commands);
            parser.xmlReader.setContentHandler(parser);
            parser.xmlReader.parse(new InputSource(reader));
        } catch (SAXException ex) {
            // incorrect xml provided by symfony?
            LOGGER.log(Level.INFO, null, ex);
        } catch (Throwable ex) {
            LOGGER.log(Level.WARNING, null, ex);
        } finally {
            try {
                reader.close();
            } catch (IOException ex) {
                LOGGER.log(Level.WARNING, null, ex);
            }
        }
    }

    @Override
    public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
        if ("commands".equals(qName)) { // NOI18N
            assert content == Content.NONE;
            content = Content.COMMANDS;
        } else if ("command".equals(qName)) { // NOI18N
            if (content != Content.COMMANDS) {
                // not interested
                return;
            }
            assert currentCommand == null;
            assert currentUsages == null;
            assert currentDescription == null;
            assert currentHelp == null;
            currentCommand = attributes.getValue("name"); // NOI18N
            currentUsages = new ArrayList<>();
        } else if ("usage".equals(qName)) { // NOI18N
            assert content == Content.COMMANDS : content;
            assert currentUsages != null;
            assert currentDescription == null;
            assert currentHelp == null;
            if (currentCommand != null) {
                content = Content.USAGE;
                currentUsages.add(new StringBuilder());
            }
        } else if ("description".equals(qName)) { // NOI18N
            assert content == Content.COMMANDS : content;
            assert currentDescription == null;
            assert currentHelp == null;
            if (currentCommand != null) {
                // we have more <description> tags
                content = Content.DESCRIPTION;
            }
        } else if ("help".equals(qName)) { // NOI18N
            assert content == Content.COMMANDS : content;
            assert currentHelp == null;
            if (currentCommand != null) {
                content = Content.HELP;
                currentHelp = new StringBuilder();
            }
        }
    }

    @Override
    public void endElement(String uri, String localName, String qName) throws SAXException {
        if ("commands".equals(qName)) { // NOI18N
            content = Content.NONE;
        } else if ("usage".equals(qName)) { // NOI18N
            if (content == Content.USAGE) {
                content = Content.COMMANDS;
            }
        } else if ("description".equals(qName)) { // NOI18N
            if (content == Content.DESCRIPTION) {
                content = Content.COMMANDS;
            }
        } else if ("help".equals(qName)) { // NOI18N
            if (content == Content.HELP) {
                assert currentCommand != null;
                if (currentUsages == null) {
                    currentUsages = Collections.emptyList();
                }
                if (currentDescription == null) {
                    currentDescription = ""; // NOI18N
                }

                commands.add(new SymfonyCommandVO(
                        currentCommand.trim(),
                        currentDescription.trim(),
                        processHelp(currentUsages, currentHelp.toString().trim())));
                currentCommand = null;
                currentUsages = null;
                currentDescription = null;
                currentHelp = null;
                content = Content.COMMANDS;
            }
        }
    }

    @Override
    public void characters(char[] ch, int start, int length) throws SAXException {
        switch (content) {
            case COMMANDS:
                // noop
                break;
            case USAGE:
                currentUsages.get(currentUsages.size() - 1).append(ch, start, length);
                break;
            case DESCRIPTION:
                currentDescription = new String(ch, start, length);
                break;
            case HELP:
                assert currentHelp != null;
                currentHelp.append(ch, start, length);
                break;
            case NONE:
                // noop
                break;
            default:
                assert false : "Unknown content: " + content;
        }
    }

    @Messages("LBL_Usage=Usage:")
    private static String processHelp(List<StringBuilder> usages, String help) {
        StringBuilder result = new StringBuilder();
        boolean titlePrinted = false;
        for (StringBuilder usage : usages) {
            String usg = usage.toString().trim();
            if (StringUtils.hasText(usg)) {
                if (!titlePrinted) {
                    titlePrinted = true;
                    result.append(Bundle.LBL_Usage());
                }
                result.append("<br><i>"); // NOI18N
                result.append(usg
                        .replace("<", "&lt;") // NOI18N
                        .replace(">", "&gt;") // NOI18N
                );
                result.append("</i>"); // NOI18N
            }
        }
        if (titlePrinted) {
            result.append("<br><br>"); // NOI18N
        }
        if (StringUtils.hasText(help)) {
            result.append(help
                    .replace("<info>", "<i>") // NOI18N
                    .replace("</info>", "</i>") // NOI18N
                    .replace("<comment>", "<i>") // NOI18N
                    .replace("</comment>", "</i>") // NOI18N
                    .replace("\n", "<br>") // NOI18N
            );
        }
        if (result.length() == 0) {
            return ""; // NOI18N
        }
        return "<html>" + result.toString(); // NOI18N
    }

    //~ Inner classes

    enum Content {
        NONE,
        COMMANDS,
        USAGE,
        DESCRIPTION,
        HELP,
    };

}
