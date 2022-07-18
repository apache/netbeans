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
package org.netbeans.modules.php.analysis.parsers;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.modules.php.analysis.results.Result;
import org.netbeans.modules.php.api.util.FileUtils;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;

/**
 * Parser for mess detector xml report file.
 */
public final class MessDetectorReportParser extends DefaultHandler {

    private static final Logger LOGGER = Logger.getLogger(MessDetectorReportParser.class.getName());

    private final List<Result> results = new ArrayList<>();
    private final XMLReader xmlReader;

    private Result currentResult = null;
    private String currentFile = null;
    private StringBuilder description = null;


    private MessDetectorReportParser() throws SAXException {
        xmlReader = FileUtils.createXmlReader();
    }

    private static MessDetectorReportParser create(Reader reader) throws SAXException, IOException {
        MessDetectorReportParser parser = new MessDetectorReportParser();
        parser.xmlReader.setContentHandler(parser);
        parser.xmlReader.parse(new InputSource(reader));
        return parser;
    }

    @CheckForNull
    public static List<Result> parse(File file) {
        try {
            try (Reader reader = new BufferedReader(new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8))) {
                return create(reader).getResults();
            }
        } catch (IOException | SAXException ex) {
            LOGGER.log(Level.INFO, null, ex);
        }
        return null;
    }

    @Override
    public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
        if ("file".equals(qName)) { // NOI18N
            processFileStart(attributes);
        } else if ("violation".equals(qName)) { // NOI18N
            processResultStart(attributes);
        }
    }

    @Override
    public void endElement(String uri, String localName, String qName) throws SAXException {
        if ("file".equals(qName)) { // NOI18N
            processFileEnd();
        } else if ("violation".equals(qName)) { // NOI18N
            processResultEnd();
        }
    }

    @Override
    public void characters(char[] ch, int start, int length) throws SAXException {
        if (description != null) {
            description.append(new String(ch, start, length));
        }
    }

    private void processFileStart(Attributes attributes) {
        assert currentResult == null : currentResult.getFilePath();
        assert currentFile == null : currentFile;

        currentFile = attributes.getValue("name"); // NOI18N
    }

    private void processFileEnd() {
        assert currentFile != null;
        currentFile = null;
    }

    private void processResultStart(Attributes attributes) {
        assert currentFile != null;
        assert currentResult == null : currentResult.getFilePath();
        assert description == null : description.toString();

        currentResult = new Result(currentFile);
        currentResult.setLine(getInt(attributes, "beginline")); // NOI18N
        currentResult.setCategory(formatCategory(attributes.getValue("ruleset"), attributes.getValue("rule"))); // NOI18N
        description = new StringBuilder(200);
    }

    private void processResultEnd() {
        assert currentResult != null;
        assert description != null;
        currentResult.setDescription(description.toString().trim());
        results.add(currentResult);
        currentResult = null;
        description = null;
    }

    private String formatCategory(String category, String subCategory) {
        return category + ": " + subCategory;
    }

    private int getInt(Attributes attributes, String name) {
        int i = -1;
        try {
            i = Integer.parseInt(attributes.getValue(name));
        } catch (NumberFormatException exc) {
            // ignored
        }
        return i;
    }

    //~ Getters

    public List<Result> getResults() {
        return results;
    }

}
