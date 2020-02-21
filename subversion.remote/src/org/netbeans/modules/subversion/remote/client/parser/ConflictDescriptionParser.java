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

package org.netbeans.modules.subversion.remote.client.parser;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.modules.subversion.remote.api.SVNConflictDescriptor;
import org.netbeans.modules.subversion.remote.api.SVNConflictVersion;

/**
 *
 * 
 */
class ConflictDescriptionParser {

    private final List<ParserConflictDescriptor> conflicts;
    private static final String TOKEN_CONFLICT = "conflict"; //NOI18N
    private static final String TOKEN_VERSION = "version"; //NOI18N
    private static final char DELIMITER_SPACE = ' ';
    private static final char DELIMITER_OPEN_BRACKET = '(';
    private static final char DELIMITER_CLOSING_BRACKET = ')';

    private static final Logger LOG = Logger.getLogger(ConflictDescriptionParser.class.getName());

    private ConflictDescriptionParser() {
        conflicts = new LinkedList<>();
    }

    static ConflictDescriptionParser parseDescription (String description) {
        ConflictDescriptionParser parser = new ConflictDescriptionParser();
        try {
            parser.parse(description);
        } catch (IOException ex) {
            LOG.log(Level.INFO, "Error parsing: " + description, ex); //NOI18N
        }
        return parser;
    }

    /**
     * Returns all conflicts created while parsing.
     * @return list of parsed conflicts
     */
    List<ParserConflictDescriptor> getConflicts() {
        return conflicts;
    }

    private void parse (String description) throws IOException {
        StringReader sr = new StringReader(description);
        if (sr.read() == DELIMITER_OPEN_BRACKET) {
            while (true) {
                int c;
                while ((c = sr.read()) != -1 && c != DELIMITER_OPEN_BRACKET && c != DELIMITER_CLOSING_BRACKET); // wait for a bracket opening new conflict
                if (c == DELIMITER_CLOSING_BRACKET) { // end of description
                    break;
                } else if (c != DELIMITER_OPEN_BRACKET) { // error
                    throw new IOException("Error parsing description: " + description); //NOI18N
                }
                ParserConflictDescriptor conflict = readConflict(sr);
                conflicts.add(conflict);
            }
        }
    }

    private ParserConflictDescriptor readConflict (Reader input) throws IOException {
        ParserConflictDescriptor conflict = null;
        String startToken = readToken(input, DELIMITER_SPACE);
        if (TOKEN_CONFLICT.equals(startToken)) { // prefix of a conflict
            String fileName = readString(input);
            String nodeKind = readToken(input, DELIMITER_SPACE); // prop or file?
            String operation = readToken(input, DELIMITER_SPACE); // update, merge, switch?
            String action = readToken(input, DELIMITER_SPACE); // action on server
            String reason = readToken(input, DELIMITER_SPACE); // local action
            SVNConflictVersion versionLeft = readVersion(input); // version in repo
            SVNConflictVersion versionRight = readVersion(input); // local base? version
            conflict = new ParserConflictDescriptor(fileName, null,
                    SVNConflictDescriptor.Action.fromString(action),
                    SVNConflictDescriptor.Reason.fromString(reason),
                    SVNConflictDescriptor.Operation.fromString(operation), versionLeft, versionRight);
            readUntil(input, DELIMITER_CLOSING_BRACKET);
        } else {
            throw new IOException("token 'conflict' expected"); //NOI18N
        }
        return conflict;
    }

    private String readString (Reader input) throws IOException {
        int c;
        String s = readToken(input, DELIMITER_SPACE);
        if (Character.isDigit(s.charAt(0))) { // next token is prefixed with its length
            try {
                int len = Integer.parseInt(s);
                s = readToken(input, len);
            } catch (NumberFormatException ex) {
                throw new IOException("Unexpected token, should be a number: " + s, ex); //NOI18N
            }
        }
        return s;
    }

    /**
     * Reads next token bounded by its length
     */
    private String readToken(Reader input, int len) throws IOException {
        StringBuilder sb = new StringBuilder(len);
        for (int i = 0; i < len; ++i) {
            sb.append((char) input.read());
        }
        return sb.toString();
    }

    /**
     * Reads next token bounded with a given delimiter.
     */
    private String readToken (Reader input, char delimiter) throws IOException {
        StringBuilder sb = new StringBuilder();
        int c = skip(input, DELIMITER_SPACE);
        sb.append((char)c);
        while ((c = input.read()) != -1 && c != delimiter) {
            if (c == -1) {
                throw new IOException("Unexpected end of input"); // NOI18N
            }
            sb.append((char)c);
        }
        return sb.toString();
    }

    private SVNConflictVersion readVersion (Reader input) throws IOException {
        SVNConflictVersion version = null;
        readUntil(input, DELIMITER_OPEN_BRACKET);
        String startToken = readToken(input, DELIMITER_SPACE);
        if (TOKEN_VERSION.equals(startToken)) { // prefix
            String url = readString(input); // repository root url
            String s = readString(input); // peg revision prefixed with its length
            long pegRevision;
            try {
                pegRevision = Long.parseLong(s);
            } catch (NumberFormatException ex) {
                throw new IOException("Unexpected token, should be a number: " + s, ex); //NOI18N
            }
            String repoPath = readString(input); // path in repository
            String nodeKind = readToken(input, DELIMITER_CLOSING_BRACKET); // file or dir
            version = new SVNConflictVersion(url, pegRevision, repoPath, SVNConflictVersion.NodeKind.fromString(nodeKind));
        } else {
            throw new IOException("token 'version' expected"); //NOI18N
        }
        return version;
    }

    /**
     * Skips all characters in input until a given breakpoint character is found
     */
    private void readUntil(Reader input, char breakpoint) throws IOException {
        int c;
        while ((c = input.read()) != -1 && c != breakpoint);
        if (c == -1) {
            throw new IOException("Unexpected end of input"); // NOI18N
        }
    }

    /**
     * Skips given character and returns the next character
     */
    private int skip(Reader input, char characterToSkip) throws IOException {
        int c;
        while ((c = input.read()) == characterToSkip); // skip characters
        return c;
    }

    static final class ParserConflictDescriptor extends SVNConflictDescriptor {
        private final String fileName;

        private ParserConflictDescriptor (String fileName, String path, SVNConflictDescriptor.Action action, SVNConflictDescriptor.Reason reason, SVNConflictDescriptor.Operation operation, SVNConflictVersion left, SVNConflictVersion right) {
            super(path, action, reason, operation, left, right);
            this.fileName = fileName;
        }

        public String getFileName () {
            return fileName;
        }
    }
}
