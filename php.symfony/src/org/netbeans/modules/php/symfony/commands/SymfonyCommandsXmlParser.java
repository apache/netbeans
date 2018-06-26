/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
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
 *
 * Portions Copyrighted 2009 Sun Microsystems, Inc.
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
