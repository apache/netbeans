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

package org.netbeans.modules.php.analysis.parsers;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.modules.php.analysis.results.Result;
import org.netbeans.modules.php.api.util.FileUtils;
import org.netbeans.modules.php.api.util.StringUtils;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;

public final class CodingStandardsFixerReportParser extends DefaultHandler {

    private static final Logger LOGGER = Logger.getLogger(CodingStandardsFixerReportParser.class.getName());

    private final List<Result> results = new ArrayList<>();
    private final XMLReader xmlReader;

    private Result currentResult = null;
    private String currentFile = null;
    private final List<String> appliedFixers = new ArrayList<>();
    private StringBuilder description = null;
    private final FileObject root;

    private CodingStandardsFixerReportParser(FileObject root) throws SAXException {
        this.xmlReader = FileUtils.createXmlReader();
        this.root = root;
    }

    private static CodingStandardsFixerReportParser create(Reader reader, FileObject root) throws SAXException, IOException {
        CodingStandardsFixerReportParser parser = new CodingStandardsFixerReportParser(root);
        parser.xmlReader.setContentHandler(parser);
        parser.xmlReader.parse(new InputSource(reader));
        return parser;
    }

    @CheckForNull
    public static List<Result> parse(File resultFile, FileObject root) {
        try {
            try (Reader reader = new BufferedReader(new InputStreamReader(new FileInputStream(resultFile), StandardCharsets.UTF_8))) {
                return create(reader, root).getResults();
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
        } else if ("applied_fixer".equals(qName)) { // NOI18N
            appliedFixers.add(attributes.getValue("name")); // NOI18N
        } else if ("diff".equals(qName)) { // NOI18N
            processResultStart(attributes);
        }
    }

    @Override
    public void endElement(String uri, String localName, String qName) throws SAXException {
        if ("file".equals(qName)) { // NOI18N
            processFileEnd();
        } else if ("diff".equals(qName)) { // NOI18N
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
        currentFile = getCurrentFile(attributes.getValue("name")); // NOI18N
    }

    private void processFileEnd() {
        currentFile = null;
    }

    private void processResultStart(Attributes attributes) {
        if (currentFile == null) {
            // #242935
            return;
        }
        assert currentResult == null : currentResult.getFilePath();
        assert description == null : description.toString();

        currentResult = new Result(currentFile);
        currentResult.setLine(1);
        currentResult.setColumn(0);
        currentResult.setCategory(StringUtils.implode(appliedFixers, ",")); // NOI18N
        description = new StringBuilder(200);
    }

    private void processResultEnd() {
        if (currentFile != null) {
            assert currentResult != null;
            assert description != null;
            currentResult.setDescription(formatDescription(description.toString()));
            results.add(currentResult);
        }
        currentResult = null;
        appliedFixers.clear();
        description = null;
    }

    private String formatDescription(String description) {
        String replaced = description.replaceAll("      (.+\n)", "$1"); // NOI18N
        return String.format("<pre>%s</pre>", replaced); // NOI18N
    }

    private String getCurrentFile(String fileName) {
        if (root.isFolder()) {
            FileObject current = root.getFileObject(fileName);
            if (current == null) {
                // #242935
                return null;
            }
            return FileUtil.toFile(current).getAbsolutePath();
        }
        return fileName;
    }

    //~ Getters

    public List<Result> getResults() {
        return Collections.unmodifiableList(results);
    }

}
