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

package org.netbeans.modules.schema2beansdev.gen;

import java.util.*;
import java.io.*;

public class IndentingWriter extends GenBuffer {
    protected boolean crDone[];
    protected int indentLevel[];
    protected String indentString = "\t";

    public IndentingWriter(int bufferCount) {
        super(bufferCount);
        crDone = new boolean[bufferCount];
        indentLevel = new int[bufferCount];
        privateInit();
    }

    public IndentingWriter(IndentingWriter source) {
        super(source);
        indentString = source.indentString;
        crDone = new boolean[bufferCount];
        indentLevel = new int[bufferCount];
        for (int i = 0; i < bufferCount; i++) {
            crDone[i] = source.crDone[i];
            indentLevel[i] = source.indentLevel[i];
        }
    }

    public void reset() {
        super.reset();
        privateInit();
    }

    private void privateInit() {
        for (int i = 0; i < bufferCount; i++) {
            crDone[i] = true;
            indentLevel[i] = 0;
        }
    }

    public void writeTo(GenBuffer o) {
        super.writeTo(o);
        if (o instanceof IndentingWriter) {
            IndentingWriter out = (IndentingWriter) o;
            int minInCommonBufferCount = bufferCount;
            if (out.bufferCount < bufferCount)
                minInCommonBufferCount = out.bufferCount;
            for (int i = 0; i < minInCommonBufferCount; i++) {
                out.crDone[i] = crDone[i];
                out.indentLevel[i] = indentLevel[i];
            }
        }
    }
    
    /**
     * Insert some additional buffers.
     * Previous buffers are not adjusted automatically.
     * select() should be called afterwards to reestablish current buffer.
     */
    public void insertAdditionalBuffers(int offset, int count) {
        boolean[] newCrDone = new boolean[bufferCount + count];
        // copy before and including offset
        System.arraycopy(crDone, 0, newCrDone, 0, offset+1);
        // copy after offset
        System.arraycopy(crDone, offset+1, newCrDone,
                         offset + 1 + count, bufferCount - offset - 1);
        // init the new elements
        for (int i = 0; i < count; ++i) {
            newCrDone[offset + 1 + i] = true;
        }
        crDone = newCrDone;

        int[] newIndentLevel = new int[bufferCount + count];
        // copy before and including offset
        System.arraycopy(indentLevel, 0, newIndentLevel, 0, offset+1);
        // copy after offset
        System.arraycopy(indentLevel, offset+1, newIndentLevel,
                         offset + 1 + count, bufferCount - offset - 1);
        // init the new elements
        for (int i = 0; i < count; ++i) {
            newIndentLevel[offset + 1 + i] = 0;
        }
        indentLevel = newIndentLevel;

        super.insertAdditionalBuffers(offset, count);
    }

    public void setIndent(String indent) {
        this.indentString = indent;
    }

    public String getIndent() {
        return indentString;
    }

    public void cr() throws IOException {
        listOut[curOut].append("\n");
        crDone[curOut] = true; 
    }

    public void write(String str) throws IOException {
        int len = str.length();
        if (len == 0)
            return;
        char lastChar = str.charAt(len-1);
        if (lastChar == '\n') {
            char firstChar = str.charAt(0);
            char secondLastChar = (len <= 1) ? ' ' : str.charAt(len-2);
            if (firstChar == '}' || secondLastChar == '}') {
                indentLeft();
            }
            super.write(str.substring(0, len-1));
            cr();
            if (secondLastChar == '{') {
                indentRight();
            }
        } else {
            super.write(str);
        }
    }

    public void writecr(String str) throws IOException {
        super.write(str);
        cr();
    }

    public void writecr(String s1, String s2) throws IOException {
        super.write(s1, s2);
        cr();
    }

    public void writecr(String s1, String s2, String s3) throws IOException {
        super.write(s1, s2, s3);
        cr();
    }

    public void writecr(String s1, String s2, String s3, String s4) throws IOException {
        super.write(s1, s2, s3, s4);
        cr();
    }

    public void indentRight() {
        ++indentLevel[curOut];
    }

    public void indentLeft() {
        --indentLevel[curOut];
    }

    protected void beforeWriteHook() {
        if (crDone[curOut]) {
            indent();
            crDone[curOut] = false;
        }
    }

    /**
     * Adds the indentString to the current buffer.
     */
    public void indentOneLevel() {
        listOut[curOut].append(indentString);
    }

    /**
     * Adds indentLevel[curOut] number of indentString's to the current
     * buffer.  Put another way, this will indent from the left margin to
     * the current indention level.
     */
    public void indent() {
        // This must not call a write as beforeWriteHook calls us
        for (int i = 0; i < indentLevel[curOut]; ++i)
            indentOneLevel();
    }
}
