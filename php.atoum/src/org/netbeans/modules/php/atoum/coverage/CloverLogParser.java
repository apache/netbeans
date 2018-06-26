/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2014 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2014 Sun Microsystems, Inc.
 */

package org.netbeans.modules.php.atoum.coverage;

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
import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.api.queries.FileEncodingQuery;
import org.netbeans.modules.php.api.util.FileUtils;
import org.netbeans.modules.php.spi.testing.coverage.Coverage;
import org.netbeans.modules.php.spi.testing.coverage.FileMetrics;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;

public final class CloverLogParser extends DefaultHandler {

    private static final Logger LOGGER = Logger.getLogger(CloverLogParser.class.getName());

    final XMLReader xmlReader;
    final List<Coverage.File> files = new ArrayList<>();

    private boolean inFile = false;
    private String filePath;
    private FileMetrics fileMetrics;
    private List<Coverage.Line> lines;


    private CloverLogParser() throws SAXException {
        xmlReader = FileUtils.createXmlReader();
    }

    private static CloverLogParser create() throws SAXException {
        CloverLogParser parser = new CloverLogParser();
        parser.xmlReader.setContentHandler(parser);
        return parser;
    }

    @CheckForNull
    public static List<Coverage.File> parse(Reader reader) {
        try {
            CloverLogParser parser = create();
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

    @Override
    public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
        if ("file".equals(qName)) { // NOI18N
            processFileStart(attributes);
        } else if ("class".equals(qName)) { // NOI18N
            processClassStart();
        } else if ("metrics".equals(qName)) { // NOI18N
            processMetricsStart(attributes);
        } else if ("line".equals(qName)) { // NOI18N
            processLine(attributes);
        }
    }

    @Override
    public void endElement(String uri, String localName, String qName) throws SAXException {
        if ("file".equals(qName)) { // NOI18N
            processFileEnd();
        } else if ("class".equals(qName)) { // NOI18N
            processClassEnd();
        }
    }

    private void processFileStart(Attributes attributes) {
        assert !inFile;
        assert filePath == null : filePath;
        assert fileMetrics == null : fileMetrics;
        assert lines == null : lines;
        inFile = true;
        filePath = getPath(attributes);
        lines = new ArrayList<>();
    }

    private void processClassStart() {
        assert inFile;
        inFile = false;
    }

    private void processMetricsStart(Attributes attributes) {
        if (!inFile) {
            // ignore
            return;
        }
        assert filePath != null;
        fileMetrics = new FileMetricsImpl(getLineCount(filePath), getStatements(attributes), getCoveredStatements(attributes));
    }

    private void processFileEnd() {
        assert inFile;
        assert filePath != null;
        assert fileMetrics != null;
        inFile = false;
        files.add(new CoverageImpl.FileImpl(filePath, fileMetrics, lines));
        filePath = null;
        fileMetrics = null;
        lines = null;
    }

    private void processClassEnd() {
        assert !inFile;
        // back in file
        inFile = true;
    }

    private void processLine(Attributes attributes) {
        if (!inFile) {
            // ignore
            return;
        }
        assert filePath != null : attributes;
        assert lines != null : filePath;
        int number = getNum(attributes);
        int hits = getCount(attributes);
        if (number == -1
                || hits == -1) {
            LOGGER.log(Level.INFO, "Unexpected line number or hits [{0}]", attributes);
            return;
        }
        lines.add(new CoverageImpl.LineImpl(number, hits));
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
