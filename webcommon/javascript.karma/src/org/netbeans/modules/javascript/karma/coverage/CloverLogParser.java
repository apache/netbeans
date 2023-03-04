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

package org.netbeans.modules.javascript.karma.coverage;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParserFactory;
import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.api.queries.FileEncodingQuery;
import org.netbeans.modules.web.clientproject.api.jstesting.Coverage;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;

/**
 * Parser for Cobertura code coverage XML log files.
 */
public final class CloverLogParser extends DefaultHandler {

    private static final Logger LOGGER = Logger.getLogger(CloverLogParser.class.getName());

    private final File sourceDir;
    final XMLReader xmlReader;
    final List<Coverage.File> files = new ArrayList<>();

    private boolean inWrongPackage = false;
    private String filePath;
    private Coverage.FileMetrics fileMetrics;
    private List<Coverage.Line> lines;


    private CloverLogParser(File sourceDir) throws SAXException {
        assert sourceDir.isDirectory() : sourceDir;
        this.sourceDir = sourceDir;
        xmlReader = createXmlReader();
    }

    private static CloverLogParser create(File sourceDir) throws SAXException {
        CloverLogParser parser = new CloverLogParser(sourceDir);
        parser.xmlReader.setContentHandler(parser);
        return parser;
    }

    @CheckForNull
    public static List<Coverage.File> parse(Reader reader, File sourceDir) {
        try {
            CloverLogParser parser = create(sourceDir);
            parser.xmlReader.parse(new InputSource(reader));
            return Collections.unmodifiableList(parser.files);
        } catch (SAXException ex) {
            // ignore (this can happen e.g. if one interrupts debugging)
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
        return null;
    }

    private static XMLReader createXmlReader() throws SAXException {
        SAXParserFactory factory = SAXParserFactory.newInstance();
        factory.setValidating(false);
        factory.setNamespaceAware(false);
        try {
            return factory.newSAXParser().getXMLReader();
        } catch (ParserConfigurationException ex) {
            throw new SAXException("Cannot create SAX parser", ex);
        }
    }

    @Override
    public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
        if ("package".equals(qName)) { // NOI18N
            processPackageStart(attributes);
        }
        if (inWrongPackage) {
            // ignore
            return;
        }
        if ("file".equals(qName)) { // NOI18N
            processFileStart(attributes);
        } else if ("metrics".equals(qName)) { // NOI18N
            processMetricsStart(attributes);
        } else if ("line".equals(qName)) { // NOI18N
            processLine(attributes);
        }
    }

    @Override
    public void endElement(String uri, String localName, String qName) throws SAXException {
        if (inWrongPackage) {
            // ignore
            return;
        }
        if ("file".equals(qName)) { // NOI18N
            processFileEnd();
        }
    }

    private void processPackageStart(Attributes attributes) {
        String name = getName(attributes);
        inWrongPackage = name.startsWith("bower_components"); // NOI18N
    }

    private void processFileStart(Attributes attributes) {
        assert !inWrongPackage;
        assert filePath == null : filePath;
        assert fileMetrics == null : fileMetrics;
        assert lines == null : lines;
        String path = getPath(attributes);
        if (path.startsWith("./")) { // NOI18N
            filePath = new File(sourceDir, path.substring(2)).getAbsolutePath();
        } else {
            filePath = new File(path).getAbsolutePath();
        }
        lines = new ArrayList<>();
    }

    private void processMetricsStart(Attributes attributes) {
        assert !inWrongPackage;
        if (filePath == null) {
            // metrics for package
            return;
        }
        fileMetrics = new Coverage.FileMetrics(getLineCount(filePath), getStatements(attributes), getCoveredStatements(attributes));
    }

    private void processFileEnd() {
        assert !inWrongPackage;
        assert filePath != null;
        assert fileMetrics != null;
        files.add(new Coverage.File(filePath, fileMetrics, lines));
        filePath = null;
        fileMetrics = null;
        lines = null;
    }

    private void processLine(Attributes attributes) {
        assert !inWrongPackage;
        assert filePath != null : attributes;
        assert lines != null : filePath;
        int number = getNum(attributes);
        int hits = getCount(attributes);
        if (number == -1
                || hits == -1) {
            LOGGER.log(Level.INFO, "Unexpected line number or hits [{0}]", attributes);
            return;
        }
        lines.add(new Coverage.Line(number, hits));
    }

    private String getName(Attributes attributes) {
        return attributes.getValue("name"); // NOI18N
    }

    private String getPath(Attributes attributes) {
        return attributes.getValue("path"); // NOI18N
    }

    private int getLineCount(String filePath) {
        File file = new File(filePath);
        assert file.isFile() : file;
        FileObject fo = FileUtil.toFileObject(file);
        assert fo != null : file;
        try (LineNumberReader lineNumberReader = new LineNumberReader(
                new InputStreamReader(new FileInputStream(file), FileEncodingQuery.getEncoding(fo)))) {
            while (lineNumberReader.readLine() != null) {
                // noop
            }
            return lineNumberReader.getLineNumber();
        } catch (IOException exc) {
            LOGGER.log(Level.WARNING, null, exc);
        }
        return -1;
    }

    private int getStatements(Attributes attributes) {
        return getInt(attributes, "statements"); // NOI18N
    }

    private int getCoveredStatements(Attributes attributes) {
        return getInt(attributes, "coveredstatements"); // NOI18N
    }

    private int getNum(Attributes attributes) {
        return getInt(attributes, "num"); // NOI18N
    }

    private int getCount(Attributes attributes) {
        return getInt(attributes, "count"); // NOI18N
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

}
