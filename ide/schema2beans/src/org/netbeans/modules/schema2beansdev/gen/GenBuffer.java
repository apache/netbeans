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

public class GenBuffer extends Writer {
    protected int INITIAL_BUFFER_CAPACITY = 512;
    protected int curOut;
    protected StringBuffer listOut[];
    protected int bufferCount;
    protected Stack selectStack = new Stack();
    private boolean first = false;
    private String separator = null;

    /**
     * @param bufferCount is the number of buffers to create.
     */
    public GenBuffer(int bufferCount) {
        this.bufferCount = bufferCount;
        listOut = new StringBuffer[bufferCount];
        privateInit();
    }
    /**
     * @return a fresh GenBuffer with the configuration duplicated
     *         (number of buffers, etc).  The buffers are NOT
     *         duplicated.
     */
    public GenBuffer(GenBuffer source) {
        bufferCount = source.bufferCount;
        listOut = new StringBuffer[bufferCount];
        INITIAL_BUFFER_CAPACITY = source.INITIAL_BUFFER_CAPACITY;
        curOut = source.curOut;
        first = source.first;
        separator = source.separator;
        privateInit();
    }

    /**
     * Reset the buffers so that you can use it again.
     */
    public void reset() {
        privateInit();
    }
    
    private void privateInit() {
        for (int i = 0; i < bufferCount; i++) {
            listOut[i] = new StringBuffer();
            listOut[i].ensureCapacity(INITIAL_BUFFER_CAPACITY);
        }
    }

    /**
     * Insert some additional buffers.
     * Previous buffers are not adjusted automatically.
     * select() should be called afterwards to reestablish current buffer.
     */
    public void insertAdditionalBuffers(int offset, int count) {
        StringBuffer[] newListOut = new StringBuffer[bufferCount + count];
        // copy before and including offset
        System.arraycopy(listOut, 0, newListOut, 0, offset+1);
        // copy after offset
        System.arraycopy(listOut, offset+1, newListOut, offset + 1 + count, bufferCount - offset - 1);
        // init the new elements
        for (int i = 0; i < count; ++i) {
            newListOut[offset + 1 + i] = new StringBuffer();
            newListOut[offset + 1 + i].ensureCapacity(INITIAL_BUFFER_CAPACITY);
        }
        bufferCount += count;
        listOut = newListOut;
    }
    
    /**
     * This method has no effect.
     */
    public void close() {
    }
    
    /**
     * This does nothing as we're all in memory.
     */
    public void flush() {
    }

    /**
     * Select the current buffer to use as output.
     * Valid range is 0 <= @param bufferNum < bufferCount
     */
    public void select(int bufferNum) {
        if (bufferNum >= bufferCount || bufferNum < 0)
            throw new IllegalArgumentException("Invalid bufferNum "+bufferNum+" out of "+bufferCount);
        curOut = bufferNum;
    }

    public void pushSelect(int bufferNum) {
        int prevOut = curOut;
        // do the select before the push, in case the select throws an exception
        select(bufferNum);
        selectStack.push(Integer.valueOf(prevOut));
    }

    public void popSelect() {
        curOut = ((Integer) selectStack.pop()).intValue();
    }

    /**
     * This method will get called before any write occurs.
     */
    protected void beforeWriteHook() {}
    
    /**
     * Append the parameter to the current buffer.
     */
    public void write(boolean b) throws IOException {
        beforeWriteHook();
        listOut[curOut].append(b);
    }

    /**
     * Append the parameter to the current buffer.
     */
    public void write(char c) throws IOException {
        beforeWriteHook();
        listOut[curOut].append(c);
    }

    /**
     * Append the parameter to the current buffer.
     */
    public void write(char[] str) throws IOException {
        beforeWriteHook();
        listOut[curOut].append(str);
    }

    /**
     * @see Writer
     */
    public void write(char[] cbuf, int off, int len) throws IOException {
        beforeWriteHook();
        listOut[curOut].append(cbuf, off, len);
    }

    /**
     * Append the parameter to the current buffer.
     */
    public void write(double d) throws IOException {
        beforeWriteHook();
        listOut[curOut].append(d);
    }

    /**
     * Append the parameter to the current buffer.
     */
    public void write(float f) throws IOException {
        beforeWriteHook();
        listOut[curOut].append(f);
    }

    private CharArrayWriter caw = null;
    /**
     * Append the parameter to the current buffer *as a character*.
     */
    public void write(int i) throws IOException {
        // A CharArrayWriter is used, because that was the only way I could
        // figure out how to convert an int into a String.
        if (caw == null)
            caw = new CharArrayWriter(2);
        caw.write(i);
        beforeWriteHook();
        listOut[curOut].append(caw.toString());
        caw.reset();
    }

    /**
     * Append the parameter to the current buffer *as a character*.
     */
    public void write(long l) throws IOException {
        write((int)l);
    }

    /**
     * Append the parameter to the current buffer.
     * @see StringBuffer
     */
    public void write(Object obj) throws IOException {
        beforeWriteHook();
        listOut[curOut].append(obj);
    }

    /**
     * write @param s to the current buffer
     */
    public void write(String s) throws IOException {
        beforeWriteHook();
        listOut[curOut].append(s);
    }

    /**
     * write @param s to the current buffer
     */
    public void write(StringBuffer s) throws IOException {
        beforeWriteHook();
        listOut[curOut].append(s);
    }

    /**
     * write @param s1 and @param s2 to the current buffer just as if
     * 2 separate writes were done.
     */
    public void write(String s1, String s2) throws IOException {
        beforeWriteHook();
        listOut[curOut].append(s1);
        listOut[curOut].append(s2);
    }

    /**
     * write @param s1, @param s2, and @param s3 to the current buffer
     *  just as if 3 separate writes were done.
     */
    public void write(String s1, String s2, String s3) throws IOException {
        beforeWriteHook();
        listOut[curOut].append(s1);
        listOut[curOut].append(s2);
        listOut[curOut].append(s3);
    }

    /**
     * write @param s1, @param s2, @param s3, and @param s4 to the current buffer
     *  just as if 3 separate writes were done.
     */
    public void write(String s1, String s2, String s3, String s4) throws IOException {
        beforeWriteHook();
        listOut[curOut].append(s1);
        listOut[curOut].append(s2);
        listOut[curOut].append(s3);
        listOut[curOut].append(s4);
    }

    public void write(String str, int bufferNum) throws IOException {
        if (bufferNum >= bufferCount || bufferNum < 0)
            throw new IllegalArgumentException("Invalid bufferNum "+bufferNum+" out of "+bufferCount);
        beforeWriteHook();
        listOut[bufferNum].append(str);
    }

    /**
     * setFirst and writeNext work in together to allow easier generation
     * or lists where the items in the list are separated by some text
     * between each of them.
     * For instance,
     *    setFirst(", ");
     *    if (doBlue) writeNext("blue");
     *    if (doGreen) writeNext("green");
     *    if (doRed) writeNext("red");
     */
    public void setFirst(String separator) {
        first = true;
        this.separator = separator;
    }

    /**
     * Write the next text in the sequence.
     */
    public void writeNext(String msg) throws IOException {
        writeNext();
        write(msg);
    }

    /**
     * Write the next text in the sequence.
     */
    public void writeNext(String msg1, String msg2) throws IOException {
        writeNext();
        write(msg1);
        write(msg2);
    }

    /**
     * Write the next text in the sequence.
     */
    public void writeNext(String msg1, String msg2, String msg3) throws IOException {
        writeNext();
        write(msg1);
        write(msg2);
        write(msg3);
    }

    /**
     * Begin the next in the sequence.
     * Equivalent to writeNext(""), where we'll write out the separator.
     */
    public void writeNext() throws IOException {
        if (first)
            first = false;
        else
            write(separator);
    }

    /**
     * Send buffers to @param out
     */
    public void writeTo(Writer out) throws IOException {
        for (int i = 0; i < bufferCount; i++)
            out.write(listOut[i].toString());
    }

    /**
     * Send buffers to @param out
     */
    public void writeTo(OutputStream out) throws IOException {
        for (int i = 0; i < bufferCount; i++)
            out.write(listOut[i].toString().getBytes());
    }

    public void writeTo(StringBuffer out) {
        for (int i = 0; i < bufferCount; i++)
            out.append(listOut[i]);
    }

    public void writeTo(GenBuffer out) {
        int minInCommonBufferCount = bufferCount;
        if (out.bufferCount < bufferCount)
            minInCommonBufferCount = out.bufferCount;
        for (int i = 0; i < minInCommonBufferCount; i++)
            out.listOut[i].append(listOut[i]);
        if (out.bufferCount < bufferCount) {
            // We've got more buffers than our destination.  Put all
            // of our "extra" ones at the end.
            for (int i = minInCommonBufferCount; i < bufferCount; i++)
                out.listOut[minInCommonBufferCount-1].append(listOut[i]);
        } else {
            out.curOut = curOut;
        }
        out.first = first;
        out.separator = separator;
    }

    /**
     * Has anything actually been written here?
     */
    public boolean anyContent() {
        for (int i = 0; i < bufferCount; i++)
            if (listOut[i].length() > 0)
                return true;
        return false;
    }

    public int getCurrentPosition() {
        return listOut[curOut].length();
    }

    public void truncateAtPosition(int pos) {
        listOut[curOut].setLength(pos);
    }

    /**
     * Return the active StringBuffer
     */
    public StringBuffer getBuffer() {
        return listOut[curOut];
    }

    /**
     * Ensures the capacity of every buffer is at least @param minimumCapacity.
     */
    public void ensureCapacity(int minimumCapacity) {
        for (int i = 0; i < bufferCount; i++)
            listOut[i].ensureCapacity(minimumCapacity);
    }
}
