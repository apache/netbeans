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

package org.netbeans.modules.subversion.client.parser;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;
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
        BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(dataFile), StandardCharsets.UTF_8));
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
        BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(dataFile), StandardCharsets.UTF_8));
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