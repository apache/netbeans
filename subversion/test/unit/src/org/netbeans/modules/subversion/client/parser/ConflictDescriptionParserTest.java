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
 * Portions Copyrighted 2010 Sun Microsystems, Inc.
 */

package org.netbeans.modules.subversion.client.parser;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Handler;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.subversion.client.parser.ConflictDescriptionParser.ParserConflictDescriptor;


/**
 *
 * @author ondra
 */
public class ConflictDescriptionParserTest extends NbTestCase {

    public ConflictDescriptionParserTest (String name) {
        super(name);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
    }

    public void testDescriptionParsing () throws Exception {
        File dataDir = getDataDir();
        File dataFile = new File(dataDir, "treeConflictsParser");
        assert dataFile.exists();
        BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(dataFile), "UTF-8"));
        for (String line = br.readLine(); line != null; line = br.readLine()) {
            testConflicts(line);
        }
        br.close();
    }

    /**
     * Must not throw any Exception
     */
    public void testNonsenseDescriptionParsing () throws Exception {
        File dataDir = getDataDir();
        File dataFile = new File(dataDir, "treeConflictsParserCorrupted");
        assert dataFile.exists();
        BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(dataFile), "UTF-8"));
        for (String line = br.readLine(); line != null; line = br.readLine()) {
            ParserLogHandler handler = new ParserLogHandler();
            attachHandler(handler);
            ConflictDescriptionParser parser = ConflictDescriptionParser.parseDescription(line);
            assertEquals(0, parser.getConflicts().size());
            assertEquals(1, handler.allMessages);
            detachHandler(handler);
        }
        br.close();
    }

    /************ PRIVATE ***********/
    
    private void testConflicts (String line) throws Exception {
        String[] elements = line.split("###");
        String description = elements.length > 0 ? elements[0] : "";
        List<List<String>> expectedValues = elements.length > 1 ? parseLine(elements[1]) : Collections.<List<String>>emptyList();
        ParserLogHandler handler = new ParserLogHandler();
        attachHandler(handler);
        ConflictDescriptionParser parser = ConflictDescriptionParser.parseDescription(description);
        List<ConflictDescriptionParser.ParserConflictDescriptor> conflicts = parser.getConflicts();
        assertEquals(0, handler.allMessages);
        assertEquals(expectedValues.size(), conflicts.size());
        int i = 0;
        for (List<String> expected : expectedValues) {
            ConflictDescriptionParser.ParserConflictDescriptor conflict  = conflicts.get(i++);
            assertEquals(expected, conflict);
        }
        detachHandler(handler);
    }

    private void assertEquals (List<String> expectedValues, ConflictDescriptionParser.ParserConflictDescriptor conflict) {
        int i = 0;
        for (String expectedValue : expectedValues) {
            String realValue = getValue(conflict, i++);
            assertEquals(expectedValue, realValue);
        }
    }

    private List<List<String>> parseLine(String line) {
        List<List<String>> values = new LinkedList<List<String>>();
        String[] conflicts = line.split("##");
        for (String conflict : conflicts) {
            String[] conflictValues = conflict.split("#");
            if (conflictValues.length > 1) {
                values.add(Arrays.asList(conflictValues));
            }
        }
        return values;
    }

    private void attachHandler (ParserLogHandler handler) throws Exception {
        Field f = ConflictDescriptionParser.class.getDeclaredField("LOG");
        f.setAccessible(true);
        Logger logger = (Logger) f.get(ConflictDescriptionParser.class);
        logger.addHandler(handler);
    }

    private void detachHandler (ParserLogHandler handler) throws Exception {
        Field f = ConflictDescriptionParser.class.getDeclaredField("LOG");
        f.setAccessible(true);
        Logger logger = (Logger) f.get(ConflictDescriptionParser.class);
        logger.removeHandler(handler);
    }

    private String getValue(ParserConflictDescriptor conflict, int i) {
        switch (i) {
            case 0:
                return conflict.getFileName();
            case 1:
                return String.valueOf(conflict.getOperation());
            case 2:
                return String.valueOf(conflict.getAction());
            case 3:
                return String.valueOf(conflict.getReason());
            case 4:
                return conflict.getSrcLeftVersion().getReposURL();
            case 5:
                return String.valueOf(conflict.getSrcLeftVersion().getPegRevision());
            case 6:
                return conflict.getSrcLeftVersion().getPathInRepos();
            case 7:
                return String.valueOf(conflict.getSrcLeftVersion().getNodeKind());
            case 8:
                return conflict.getSrcRightVersion().getReposURL();
            case 9:
                return String.valueOf(conflict.getSrcRightVersion().getPegRevision());
            case 10:
                return conflict.getSrcRightVersion().getPathInRepos();
            case 11:
                return String.valueOf(conflict.getSrcRightVersion().getNodeKind());
        }
        return "";
    }

    private static class ParserLogHandler extends Handler {

        private int allMessages;

        @Override
        public void publish(LogRecord record) {
            ++allMessages;
        }

        @Override
        public void flush() {
            //
        }

        @Override
        public void close() throws SecurityException {
            //
        }

    }
}