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

package org.netbeans.modules.web.jsps.parserapi;

import java.io.File;

/**
 * Mark represents a point in the JSP input.
 *
 * @author Anil K. Vijendran
 */
public final class Mark {
    private final int line; // position within current stream
    private final int col; // position within current stream
    private final String fileName; // name of the current file

    public Mark(String filename, int line, int col) {
        this.line = line;
        this.col = col;
        this.fileName = filename;
    }

    // -------------------- Locator interface --------------------

    public int getLineNumber() {
        return line;
    }

    public int getColumnNumber() {
        return col;
    }

    @Override
    public String toString() {
        return getFile() + "(" + line + "," + col + ")"; // NOI18N
    }

    public String getFile() {
        return this.fileName;
    }
    
    public String toShortString() {
        return "(" + line + "," + col + ")"; // NOI18N
    }

    @Override
    public boolean equals(Object other) {
        if (other instanceof Mark) {
            Mark m = (Mark) other;
            return line == m.line
                && col == m.col
                && new File(fileName).equals(new File(m.fileName));
        } 
        return false;
    }
}

