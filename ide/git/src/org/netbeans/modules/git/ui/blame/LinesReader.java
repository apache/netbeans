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

package org.netbeans.modules.git.ui.blame;

import java.io.Reader;
import java.io.IOException;
import java.util.List;

/**
 * Reader over annotation lines. It uses '\n' as
 * line separator to match Document line separator.
 *
 * @author Petr Kuzel
 */
public final class LinesReader extends  Reader {

    private List lines;
    private int lineIndex;
    private int columnIndex;
    private boolean closed;

    /**
     * Creates reader from list of AnnotateLine objects.
     */
    LinesReader(List lines) {
        this.lines = lines;
    }

    public void close() throws IOException {
        if (closed) throw new IOException("Closed"); // NOI18N
        closed = true;
    }

    public int read(char cbuf[], int off, int len) throws IOException {
        if (closed) throw new IOException("Closed"); // NOI18N

        if (lineIndex >= lines.size()) return -1;

        AnnotateLine aline = (AnnotateLine) lines.get(lineIndex);
        String line = aline.getContent() + "\n"; // NOI18N
        int lineLen = line.length();
        int unread =  lineLen - columnIndex;
        int toRead = Math.min(unread, len);
        line.getChars(columnIndex, columnIndex + toRead, cbuf, off);
        columnIndex += toRead;
        if (columnIndex >= lineLen) {
            columnIndex = 0;
            lineIndex++;
        }
        return toRead;
    }
}
