/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2011 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2011 Sun Microsystems, Inc.
 */
package org.netbeans.modules.php.doctrine2.commands;

import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
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
 * Doctrine2 XML commands parser.
 */
public final class Doctrine2CommandsXmlParser extends DefaultHandler {

    private static final Logger LOGGER = Logger.getLogger(Doctrine2CommandsXmlParser.class.getName());

    private final XMLReader xmlReader;
    private final List<Doctrine2CommandVO> commands;

    private String currentCommand = null;
    private List<String> currentUsages = new ArrayList<>();
    private StringBuilder currentUsage = null;
    private String currentDescription = null;
    private StringBuilder currentHelp = null;
    private Content content = Content.NONE;


    private Doctrine2CommandsXmlParser(List<Doctrine2CommandVO> commands) throws SAXException {
        assert commands != null;

        this.commands = commands;
        xmlReader = FileUtils.createXmlReader();
    }

    public static void parse(Reader reader, List<Doctrine2CommandVO> commands) {
        try {
            Doctrine2CommandsXmlParser parser = new Doctrine2CommandsXmlParser(commands);
            parser.xmlReader.setContentHandler(parser);
            parser.xmlReader.parse(new InputSource(reader));
        } catch (SAXException ex) {
            // incorrect xml provided by doctrine?
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
        if ("command".equals(qName)) { // NOI18N
            assert currentCommand == null;
            assert currentUsage == null;
            assert currentDescription == null;
            assert currentHelp == null;
            currentCommand = attributes.getValue("name"); // NOI18N
        } else if ("usage".equals(qName)) { // NOI18N
            assert content == Content.NONE;
            assert currentUsage == null;
            assert currentDescription == null;
            assert currentHelp == null;
            if (currentCommand != null) {
                content = Content.USAGE;
                currentUsage = new StringBuilder();
            }
        } else if ("description".equals(qName)) { // NOI18N
            assert content == Content.NONE;
            assert currentDescription == null;
            assert currentHelp == null;
            if (currentCommand != null) {
                // we have more <description> tags
                content = Content.DESCRIPTION;
            }
        } else if ("help".equals(qName)) { // NOI18N
            assert content == Content.NONE;
            assert currentHelp == null;
            if (currentCommand != null) {
                content = Content.HELP;
                currentHelp = new StringBuilder();
            }
        }
    }

    @Override
    public void endElement(String uri, String localName, String qName) throws SAXException {
        if ("usage".equals(qName)) { // NOI18N
            if (content == Content.USAGE) {
                String usage = currentUsage.toString().trim();
                if (StringUtils.hasText(usage)) {
                    currentUsages.add(usage);
                }
                content = Content.NONE;
                currentUsage = null;
            }
        } else if ("description".equals(qName)) { // NOI18N
            if (content == Content.DESCRIPTION) {
                content = Content.NONE;
            }
        } else if ("help".equals(qName)) { // NOI18N
            if (content == Content.HELP) {
                assert currentCommand != null;
                if (currentDescription == null) {
                    currentDescription = ""; // NOI18N
                }

                commands.add(new Doctrine2CommandVO(
                        currentCommand.trim(),
                        currentDescription.trim(),
                        processHelp(currentUsages, currentHelp.toString().trim())));
                currentCommand = null;
                currentDescription = null;
                currentHelp = null;
                currentUsages.clear();
                content = Content.NONE;
            }
        }
    }

    @Override
    public void characters(char[] ch, int start, int length) throws SAXException {
        switch (content) {
            case USAGE:
                currentUsage.append(ch, start, length);
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
    private static String processHelp(List<String> usages, String help) {
        StringBuilder result = new StringBuilder();
        if (!usages.isEmpty()) {
            result.append(Bundle.LBL_Usage());
            result.append("<br>");
            for (String usage : usages) {
                result.append("<i>"); // NOI18N
                result.append(usage.replace("<", "&lt;").replace(">", "&gt;")); // NOI18N
                result.append("</i><br>"); // NOI18N
            }
            result.append("<br>");
        }
        if (StringUtils.hasText(help)) {
            help = help.replace("<info>", "<i>"); // NOI18N
            help = help.replace("</info>", "</i>"); // NOI18N
            help = help.replace("<comment>", "<i>"); // NOI18N
            help = help.replace("</comment>", "</i>"); // NOI18N
            help = help.replace("\n", "<br>"); // NOI18N
            result.append(help);
        }
        if (result.length() == 0) {
            return ""; // NOI18N
        }
        return "<html>" + result.toString(); // NOI18N
    }

    //~ Inner classes

    enum Content {
        NONE,
        USAGE,
        DESCRIPTION,
        HELP,
    };

}
