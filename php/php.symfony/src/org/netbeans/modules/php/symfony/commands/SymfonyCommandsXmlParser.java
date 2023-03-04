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

package org.netbeans.modules.php.symfony.commands;

import java.io.IOException;
import java.io.Reader;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.modules.php.api.util.FileUtils;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;

/**
 * @author Tomas Mysik
 */
public final class SymfonyCommandsXmlParser extends DefaultHandler {
    enum Content { NONE, DESCRIPTION };
    private static final Logger LOGGER = Logger.getLogger(SymfonyCommandsXmlParser.class.getName());

    private final XMLReader xmlReader;
    private final List<SymfonyCommandVO> commands;

    private String currentCommand = null;
    private String currentDescription = null;
    private Content content = Content.NONE;

    public SymfonyCommandsXmlParser(List<SymfonyCommandVO> commands) throws SAXException {
        assert commands != null;

        this.commands = commands;
        xmlReader = FileUtils.createXmlReader();
        xmlReader.setContentHandler(this);
    }

    public static void parse(Reader reader, List<SymfonyCommandVO> commands) {
        try {
            SymfonyCommandsXmlParser parser = new SymfonyCommandsXmlParser(commands);
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
        if ("task".equals(qName)) {
            assert currentCommand == null;
            assert currentDescription == null;
            currentCommand = attributes.getValue("id"); // NOI18N
        } else if ("description".equals(qName)) {
            assert content == Content.NONE;
            assert currentDescription == null;
            if (currentCommand != null) {
                // we have more <description> tags
                content = Content.DESCRIPTION;
            }
        }
    }

    @Override
    public void endElement(String uri, String localName, String qName) throws SAXException {
        if ("description".equals(qName)) {
            if (content == Content.DESCRIPTION) {
                assert currentCommand != null;
                // #179717
                if (currentDescription == null) {
                    currentDescription = ""; // NOI18N
                }

                commands.add(new SymfonyCommandVO(currentCommand.trim(), currentDescription.trim()));
                currentCommand = null;
                currentDescription = null;
                content = Content.NONE;
            }
        }
    }

    @Override
    public void characters(char[] ch, int start, int length) throws SAXException {
        if (content == Content.DESCRIPTION) {
            currentDescription = new String(ch, start, length);
        }
    }
}
