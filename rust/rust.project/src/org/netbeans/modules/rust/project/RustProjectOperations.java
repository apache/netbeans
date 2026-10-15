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
package org.netbeans.modules.rust.project;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.CharArrayWriter;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.netbeans.api.project.Project;
import org.netbeans.spi.project.CopyOperationImplementation;
import org.netbeans.spi.project.DeleteOperationImplementation;
import org.netbeans.spi.project.MoveOrRenameOperationImplementation;
import org.openide.filesystems.FileLock;
import org.openide.filesystems.FileObject;
import org.tomlj.Toml;
import org.tomlj.TomlParseResult;
import org.tomlj.TomlPosition;


public class RustProjectOperations implements
        MoveOrRenameOperationImplementation,
        CopyOperationImplementation,
        DeleteOperationImplementation {

    private static final String CARGOTOML = "Cargo.toml";

    private final RustProject project;

    public RustProjectOperations(RustProject project) {
        assert project != null;
        this.project = project;
    }

    @Override
    public void notifyDeleted() throws IOException {
    }

    @Override
    public void notifyDeleting() throws IOException {
    }

    @Override
    public List<FileObject> getDataFiles() {
        return Arrays.asList(project.getProjectDirectory().getChildren());
    }

    @Override
    public List<FileObject> getMetadataFiles() {
        return Collections.emptyList();
    }

    @Override
    public void notifyRenaming() throws IOException {
    }

    @Override
    public void notifyRenamed(String newName) throws IOException {
        updateProjectName(newName);
    }

    @Override
    public void notifyMoving() throws IOException {
    }

    @Override
    public void notifyMoved(Project original, File originalPath, String newName) throws IOException {
        if (original != null) {
            updateProjectName(newName);
        }
    }

    @Override
    public void notifyCopying() throws IOException {
    }

    @Override
    public void notifyCopied(Project original, File originalPath, String newName) throws IOException {
        if(original != null) {
            updateProjectName(newName);
        }
    }

    /**
     * Update the project name without modifying the rest of the file
     */
    private  void updateProjectName(String newName) throws IOException {
        FileObject cargotoml = project.getProjectDirectory().getFileObject(CARGOTOML);
        updateProjectName(cargotoml, newName);
    }

    static void updateProjectName(FileObject cargotoml, String newName) throws IOException {
        CharArrayWriter caw = new CharArrayWriter();
        try (Reader is1 = bufferedReader(cargotoml.getInputStream());
                Reader is2 = bufferedReader(cargotoml.getInputStream())) {
            TomlParseResult parseResult = Toml.parse(is1);
            TomlPosition targetPos = parseResult.inputPositionOf("package.name");
            copyUntilPosition(targetPos, is2, caw);
            copyUntilEquals(is2, caw);
            copyWhitespace(is2, caw);
            caw.write(escapeTomlString(newName));
            skipString(is2);
            transfer(is2, caw);
        }
        try(FileLock cargotomlLock = cargotoml.lock()) {
            deleteOldCargoToml(cargotoml);
            cargotoml.copy(cargotoml.getParent(), CARGOTOML, "old");
            try(Writer os = bufferedWriter(cargotoml.getOutputStream(cargotomlLock))) {
                os.write(caw.toCharArray());
            }
            deleteOldCargoToml(cargotoml);
        }
    }

    private static void deleteOldCargoToml(FileObject cargotoml) throws IOException {
        FileObject oldFile = cargotoml.getParent().getFileObject(CARGOTOML, "old");
        if(oldFile != null) {
            oldFile.delete();
        }
    }

    /**
     * Copy the data from input to output
     */
    @SuppressWarnings("NestedAssignment")
    private static void transfer(Reader is, Writer os) throws IOException {
        char[] buffer = new char[10240];
        int read;
        while((read = is.read(buffer)) > 0) {
            os.write(buffer, 0, read);
        }
    }

    /**
     * Escape the supplied string so that it can be used as a plain string
     */
    static String escapeTomlString(String input) {
        StringBuilder sb = new StringBuilder();
        sb.append("\"");
        for(char c: input.toCharArray()) {
            if(c == '\\') {
                sb.append("\\\\");
            } else if (c == '\b') {
                sb.append("\\b");
            } else if (c == '\t') {
                sb.append("\\t");
            } else if (c == '\n') {
                sb.append("\\n");
            } else if (c == '\f') {
                sb.append("\\f");
            } else if (c == '\r') {
                sb.append("\\r");
            } else if (c == '"') {
                sb.append("\\\"");
            } else if (c <= 0x0008 || (c >= 0x000A && c < 0x001F) || (c == 0x007F)) {
                sb.append("\\u");
                if(c <= 0xFFFF) {
                    sb.append(String.format("%04X", (int) c));
                } else {
                    sb.append(String.format("%08X", (int) c));
                }
            } else {
                sb.append(c);
            }
        }
        sb.append("\"");
        return sb.toString();
    }

    /**
     * Copy all characters to the targetPos (excluding it)
     */
    private static void copyUntilPosition(TomlPosition targetPos, Reader is, Writer os) throws IOException {
        if(targetPos.column() == 1 && targetPos.line() == 1) {
            return;
        }
        int row = 1;
        int pos = 1;
        for (int input = is.read(); input >= 0; input = is.read()) {
            if (input < 0) {
                break;
            }
            os.write(input);
            if (input == '\n') {
                row++;
                pos = 1;
            } else {
                pos++;
            }
            if(row == targetPos.line() && pos == targetPos.column()) {
                break;
            }
        }
    }

    /**
     * Copy the "key + equals" part of a TOML key-value construct
     */
    private static void copyUntilEquals(Reader is, Writer os) throws IOException {
        while (true) {
            is.mark(1);
            int lookAhead = is.read();
            is.reset();
            if (lookAhead == -1) {
                break;
            } else if (lookAhead == '=') {
                os.write(is.read());
                break;
            } else if (Character.isWhitespace(lookAhead)) {
                copyWhitespace(is, os);
            } else if (lookAhead == '"') {
                copyQuotedName(is, os);
            } else if (isNameChar(lookAhead)) {
                copyName(is, os);
            } else {
                os.write(is.read());
            }
        }
    }

    /**
     * Copy an unquoted TOML name
     */
    private static void copyName(Reader is, Writer os) throws IOException {
        while (true) {
            is.mark(1);
            int read = is.read();
            if (read < 0 || ! isNameChar(read)) {
                is.reset();
                break;
            } else {
                os.write(read);
            }
        }
    }

    /**
     * Copy a quoted TOML name from input
     */
    private static void copyQuotedName(Reader r, Writer w) throws IOException {
        boolean backslashRead = false;
        for (int input = r.read(); input >= 0; input = r.read()) {
            w.write(input);
            if (backslashRead) {
                backslashRead = false;
            } else {
                if(input == '"') {
                    break;
                } else if (input == '\\') {
                    backslashRead = true;
                }
            }
        }
    }

    /**
     * Copy whitespace from inputstream to outputstream, starting at the current
     * position.
     */
    private static void copyWhitespace(Reader r, Writer w) throws IOException {
        while(true) {
            r.mark(1);
            int read = r.read();
            if(read < 0 || ! Character.isWhitespace(read)) {
                r.reset();
                break;
            } else {
                w.write(read);
            }
        }
    }

    /**
     * Determine if supplied character value is a valid name character in TOML
     */
    private static boolean isNameChar(int c) {
        return (c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z') || (c >= '0' && c <= '9') || c == '-' || c == '_';
    }

    /**
     * Skip over TOML normal, multiline, literal and multiline literal strings
     */
    private static void skipString(Reader r) throws IOException {
        r.mark(3);
        char[] marker = new char[3];
        r.read(marker);
        r.reset();
        if(marker[0] == '"' && marker[1] == '"' && marker[2] == '"') {
            r.read();
            r.read();
            r.read();
            int[] buffer = new int[3];
            int pos = 0;
            while(true) {
                buffer[pos] = r.read();
                if(buffer[pos] < 0) {
                    break;
                }
                if( modVal(buffer, pos - 2) == '"' && modVal(buffer, pos - 1) == '"' && modVal(buffer, pos) == '"') {
                    break;
                }
                pos++;
                pos %= buffer.length;
            }
        } else if (marker[0] == '"') {
            // normal string
            r.read();
            boolean escape = false;
            while(true) {
                int read = r.read();
                if(read < 0) {
                    break;
                }
                if (!escape) {
                    if (read == '"') {
                        break;
                    } else if (read == '\\') {
                        escape = true;
                    }
                } else {
                    escape = false;
                }
            }
        } else if (marker[0] == '\'' && marker[1] == '\'' && marker[2] == '\'') {
            r.read();
            r.read();
            r.read();
            int[] buffer = new int[3];
            int pos = 0;
            while(true) {
                buffer[pos] = r.read();
                if(buffer[pos] < 0) {
                    break;
                }
                if( modVal(buffer, pos - 2) == '\'' && modVal(buffer, pos - 1) == '\'' && modVal(buffer, pos) == '\'') {
                    break;
                }
                pos++;
                pos %= buffer.length;
            }
        } else if (marker[0] == '\'') {
            r.read();
            while(true) {
                int read = r.read();
                if(read == '\'' || read < 0) {
                    break;
                }
            }
        }
    }

    /**
     * Extract value at {@code pos} from buffer. It can handle negative
     * positions.
     */
    private static int modVal(int[] buffer, int pos) {
        if(pos >= 0) {
            return buffer[pos % buffer.length];
        } else {
            return buffer[(pos % 3) + buffer.length];
        }
    }

    /**
     * Create a buffered reader from an inputstream with UTF-8 encoding
     */
    private static Reader bufferedReader(InputStream is) {
        return new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8));
    }

    /**
     * Create a buffered writer from an outputstream with UTF-8 encoding
     */
    private static Writer bufferedWriter(OutputStream os) {
        return new BufferedWriter(new OutputStreamWriter(os, StandardCharsets.UTF_8));
    }
}
