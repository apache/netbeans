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
package org.netbeans.modules.php.codeception.coverage;

import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.modules.php.api.util.FileUtils;
import org.netbeans.modules.php.codeception.coverage.CoverageImpl.ClassImpl;
import org.netbeans.modules.php.codeception.coverage.CoverageImpl.FileImpl;
import org.netbeans.modules.php.codeception.coverage.CoverageImpl.LineImpl;
import org.openide.filesystems.FileUtil;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;

public final class CodeceptionCoverageLogParser extends DefaultHandler {

    private static final Logger LOGGER = Logger.getLogger(CodeceptionCoverageLogParser.class.getName());

    private enum Content {
        COVERAGE,
        FILE,
        CLASS,
    };

    final XMLReader xmlReader;
    private final CoverageImpl coverage;

    private FileImpl file; // actual file
    private ClassImpl clazz; // actual class
    private Content content = null;


    private CodeceptionCoverageLogParser(CoverageImpl coverage) throws SAXException {
        assert coverage != null;
        this.coverage = coverage;
        xmlReader = FileUtils.createXmlReader();
    }

    public static void parse(Reader reader, CoverageImpl coverage) {
        try {
            CodeceptionCoverageLogParser parser = new CodeceptionCoverageLogParser(coverage);
            parser.xmlReader.setContentHandler(parser);
            parser.xmlReader.parse(new InputSource(reader));
        } catch (SAXException ex) {
            // ignore (this can happen e.g. if one interrupts debugging)
            LOGGER.log(Level.INFO, null, ex);
        } catch (Throwable ex) {
            LOGGER.log(Level.WARNING, null, ex);
        } finally {
            try {
                reader.close();
            } catch (IOException ex) {
                LOGGER.log(Level.INFO, null, ex);
            }
        }
    }

    @Override
    public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
        if ("coverage".equals(qName)) { // NOI18N
            processCoverage(attributes);
        } else if ("file".equals(qName)) { // NOI18N
            processFile(attributes);
        } else if ("class".equals(qName)) { // NOI18N
            processClass(attributes);
        } else if ("metrics".equals(qName)) { // NOI18N
            processMetrics(attributes);
        } else if ("line".equals(qName)) { // NOI18N
            processLine(attributes);
        }
    }

    @Override
    public void endElement(String uri, String localName, String qName) throws SAXException {
        if ("file".equals(qName)) { // NOI18N
            endFile();
        } else if ("class".equals(qName)) { // NOI18N
            endClass();
        }
    }

    private void processCoverage(Attributes attributes) {
        assert content == null;
        content = Content.COVERAGE;
        coverage.setGenerated(getGenerated(attributes));
    }

    private void processFile(Attributes attributes) {
        assert content.equals(Content.COVERAGE);
        assert file == null;
        content = Content.FILE;
        file = new FileImpl(getPath(attributes));
        coverage.addFile(file);
    }

    private void processClass(Attributes attributes) {
        assert content.equals(Content.FILE);
        assert file != null;
        assert clazz == null;
        content = Content.CLASS;
        clazz = new ClassImpl(getName(attributes), getNamespace(attributes));
        file.addClass(clazz);
    }

    private void processMetrics(Attributes attributes) {
        assert content != null;
        switch (content) {
            case COVERAGE:
                assert file == null;
                assert clazz == null;
                coverage.setMetrics(new CoverageMetricsImpl(
                        getFiles(attributes),
                        getLoc(attributes),
                        getNcloc(attributes),
                        getClasses(attributes),
                        getMethods(attributes),
                        getCoveredMethods(attributes),
                        getConditionals(attributes),
                        getCoveredConditionals(attributes),
                        getStatements(attributes),
                        getCoveredStatements(attributes),
                        getElements(attributes),
                        getCoveredElements(attributes)));
                break;
            case FILE:
                assert file != null;
                assert clazz == null;
                file.setMetrics(new FileMetricsImpl(
                        getLoc(attributes),
                        getNcloc(attributes),
                        getClasses(attributes),
                        getMethods(attributes),
                        getCoveredMethods(attributes),
                        getConditionals(attributes),
                        getCoveredConditionals(attributes),
                        getStatements(attributes),
                        getCoveredStatements(attributes),
                        getElements(attributes),
                        getCoveredElements(attributes)));
                break;
            case CLASS:
                assert file != null;
                assert clazz != null;
                clazz.setMetrics(new ClassMetricsImpl(
                        getMethods(attributes),
                        getCoveredMethods(attributes),
                        getConditionals(attributes),
                        getCoveredConditionals(attributes),
                        getStatements(attributes),
                        getCoveredStatements(attributes),
                        getElements(attributes),
                        getCoveredElements(attributes)));
                break;
            default:
                assert false : "Unknown content type: " + content;
                break;
        }
    }

    private void processLine(Attributes attributes) {
        assert file != null;
        assert clazz == null;
        file.addLine(new LineImpl(
                getNum(attributes),
                getType(attributes),
                getName(attributes),
                getCrap(attributes),
                getCount(attributes)));
    }

    private void endFile() {
        assert content.equals(Content.FILE);
        assert file != null;
        file = null;
        content = Content.COVERAGE;
    }

    private void endClass() {
        assert content.equals(Content.CLASS);
        assert clazz != null;
        clazz = null;
        content = Content.FILE;
    }

    private long getGenerated(Attributes attributes) {
        return getLong(attributes, "generated"); // NOI18N
    }

    private String getPath(Attributes attributes) {
        return FileUtil.normalizeFile(new File(attributes.getValue("name"))).getAbsolutePath(); // NOI18N
    }

    private String getName(Attributes attributes) {
        return attributes.getValue("name"); // NOI18N
    }

    private String getNamespace(Attributes attributes) {
        return attributes.getValue("namespace"); // NOI18N
    }

    private int getNum(Attributes attributes) {
        return getInt(attributes, "num"); // NOI18N
    }

    private String getType(Attributes attributes) {
        return attributes.getValue("type"); // NOI18N
    }

    private int getCrap(Attributes attributes) {
        return getInt(attributes, "crap"); // NOI18N
    }

    private int getCount(Attributes attributes) {
        return getInt(attributes, "count"); // NOI18N
    }

    private int getFiles(Attributes attributes) {
        return getInt(attributes, "files"); // NOI18N
    }

    private int getLoc(Attributes attributes) {
        return getInt(attributes, "loc"); // NOI18N
    }

    private int getNcloc(Attributes attributes) {
        return getInt(attributes, "ncloc"); // NOI18N
    }

    private int getClasses(Attributes attributes) {
        return getInt(attributes, "classes"); // NOI18N
    }

    private int getMethods(Attributes attributes) {
        return getInt(attributes, "methods"); // NOI18N
    }

    private int getCoveredMethods(Attributes attributes) {
        return getInt(attributes, "coveredmethods"); // NOI18N
    }

    private int getConditionals(Attributes attributes) {
        return getInt(attributes, "conditionals"); // NOI18N
    }

    private int getCoveredConditionals(Attributes attributes) {
        return getInt(attributes, "coveredconditionals"); // NOI18N
    }

    private int getStatements(Attributes attributes) {
        return getInt(attributes, "statements"); // NOI18N
    }

    private int getCoveredStatements(Attributes attributes) {
        return getInt(attributes, "coveredstatements"); // NOI18N
    }

    private int getElements(Attributes attributes) {
        return getInt(attributes, "elements"); // NOI18N
    }

    private int getCoveredElements(Attributes attributes) {
        return getInt(attributes, "coveredelements"); // NOI18N
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

    private long getLong(Attributes attributes, String name) {
        long l = -1;
        try {
            l = Long.parseLong(attributes.getValue(name));
        } catch (NumberFormatException exc) {
            // ignored
        }
        return l;
    }

}
