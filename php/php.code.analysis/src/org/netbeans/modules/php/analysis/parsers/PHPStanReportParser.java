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
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.api.annotations.common.NullAllowed;
import org.netbeans.modules.php.analysis.results.Result;
import org.netbeans.modules.php.api.util.FileUtils;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;

/**
 * Parser for PHPStan xml report file.
 */
public class PHPStanReportParser extends DefaultHandler {

    private static final String PHP_EXT = ".php"; // NOI18N
    private static final Logger LOGGER = Logger.getLogger(PHPStanReportParser.class.getName());
    private final List<Result> results = new ArrayList<>();
    private final XMLReader xmlReader;

    private Result currentResult = null;
    private String currentFile = null;
    private final FileObject root;
    @NullAllowed
    private final FileObject workDir;

    private PHPStanReportParser(FileObject root, @NullAllowed FileObject workDir) throws SAXException {
        this.xmlReader = FileUtils.createXmlReader();
        this.root = root;
        this.workDir = workDir;
    }

    private static PHPStanReportParser create(Reader reader, FileObject root, @NullAllowed FileObject workDir) throws SAXException, IOException {
        PHPStanReportParser parser = new PHPStanReportParser(root, workDir);
        parser.xmlReader.setContentHandler(parser);
        parser.xmlReader.parse(new InputSource(reader));
        return parser;
    }

    @CheckForNull
    public static List<Result> parse(File file, FileObject root, @NullAllowed FileObject workDir) {
        try {
            sanitizeFile(file);
            try (Reader reader = new BufferedReader(new InputStreamReader(new FileInputStream(file), "UTF-8"))) { // NOI18N
                return create(reader, root, workDir).getResults();
            }
        } catch (IOException | SAXException ex) {
            LOGGER.log(Level.INFO, null, ex);
        }
        return null;
    }

    // sanitize file content (the file can contain progress etc. so then it is not a valid XML file)
    private static void sanitizeFile(File file) throws IOException {
        String fileName = file.getAbsolutePath();
        List<String> newLines = new ArrayList<>();
        boolean content = false;
        List<String> readAllLines = Files.readAllLines(Paths.get(fileName), StandardCharsets.UTF_8);
        if (!readAllLines.isEmpty() && readAllLines.get(0).startsWith("<?xml")) { // NOI18N
            return;
        }
        for (String line : readAllLines) {
            if (!content) {
                if (line.startsWith("<?xml")) { // NOI18N
                    content = true;
                }
                continue;
            }
            if (content) {
                newLines.add(line);
                if (line.equals("</checkstyle>")) { // NOI18N
                    break;
                }
            }
        }
        Files.write(Paths.get(fileName), newLines, StandardCharsets.UTF_8);
    }

    @Override
    public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
        if ("file".equals(qName)) { // NOI18N
            processFileStart(attributes);
        } else if ("error".equals(qName) && currentFile != null) { // NOI18N
            processResultStart(attributes);
        }
    }

    @Override
    public void endElement(String uri, String localName, String qName) throws SAXException {
        if ("file".equals(qName)) { // NOI18N
            processFileEnd();
        } else if ("error".equals(qName)) { // NOI18N
            processResultEnd();
        }
    }

    private void processFileStart(Attributes attributes) {
        assert currentResult == null : currentResult.getFilePath();
        assert currentFile == null : currentFile;

        // NETBEANS-3022 name value can be null
        // e.g.
        // <file>
        //   <error severity="error" message="Ignored error pattern #TEST# was not matched in reported errors." />
        // </file>
        String name = attributes.getValue("name"); // NOI18N
        currentFile = name == null ? null : getCurrentFile(name);
    }

    private void processFileEnd() {
        currentFile = null;
    }

    private void processResultStart(Attributes attributes) {
        assert currentFile != null;
        assert currentResult == null : currentResult.getFilePath();
        if (currentFile == null) {
            return;
        }
        currentResult = new Result(currentFile);
        int lineNumber = getInt(attributes, "line"); // NOI18N
        // line number can be 0
        // e.g. <error line="0" column="1" severity="error"
        // message="Class PhpParser\Builder\ClassTest was not found while trying to analyse it - autoloading is probably not configured properly." />
        if (lineNumber == 0) {
            lineNumber = 1;
        }
        currentResult.setLine(lineNumber);
        currentResult.setColumn(getInt(attributes, "column")); // NOI18N
        String message = attributes.getValue("message"); // NOI18N
        currentResult.setCategory(String.format("%s: %s", attributes.getValue("severity"), message)); // NOI18N
        currentResult.setDescription(message);
    }

    private void processResultEnd() {
        if (currentResult != null) {
            results.add(currentResult);
        }
        currentResult = null;
    }

    private int getInt(Attributes attributes, String name) {
        int i = -1;
        try {
            i = Integer.valueOf(attributes.getValue(name));
        } catch (NumberFormatException exc) {
            // ignored
        }
        return i;
    }

    @CheckForNull
    private String getCurrentFile(String fileName) {
        String sanitizedFileName = sanitizeFileName(fileName);
        // if working directory is null, the file name is the absolute path
        // e.g. <file name="/path/to/MyPHPProject/app/Test1.php">
        File file = new File(sanitizedFileName);
        if (file.isAbsolute()) {
            return file.getAbsolutePath();
        }

        String currentFilePath = sanitizedFileName;
        FileObject rootDirectory = root;
        if (!root.isFolder()) {
            rootDirectory = root.getParent();
        }
        if (rootDirectory.isFolder()) {
            // windows: <file name="app\index.php">
            sanitizedFileName = sanitizedFileName.replace('\\', '/');
            FileObject current = rootDirectory.getFileObject(sanitizedFileName);
            if (current == null) {
                // NETBANS-3022 try checking relative file path from working directory
                if (workDir != null) {
                    rootDirectory = workDir;
                    current = rootDirectory.getFileObject(sanitizedFileName);
                }
            }
            if (current == null) {
                LOGGER.log(Level.WARNING, "Cannot get the current file: file name {0}, root directory {1}", // NOI18N
                        new Object[]{fileName, FileUtil.toFile(rootDirectory).getAbsolutePath()});
                currentFilePath = null;
            } else {
                currentFilePath = FileUtil.toFile(current).getAbsolutePath();
            }
        }
        return currentFilePath;
    }

    private String sanitizeFileName(String fileName) {
        // e.g. PHPStanSupport/vendor/nette/utils/src/Utils/SmartObject.php (in context of class Nette\Bridges\DITracy\ContainerPanel)
        if (!fileName.endsWith(PHP_EXT)) {
            int lastIndexOfPhpExt = fileName.lastIndexOf(PHP_EXT);
            if (lastIndexOfPhpExt != -1) {
                return fileName.substring(0, lastIndexOfPhpExt + PHP_EXT.length());
            }
        }
        return fileName;
    }

    //~ Getters
    public List<Result> getResults() {
        return Collections.unmodifiableList(results);
    }
}
