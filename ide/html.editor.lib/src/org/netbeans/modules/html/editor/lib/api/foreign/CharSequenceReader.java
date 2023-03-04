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
package org.netbeans.modules.html.editor.lib.api.foreign;

import java.io.IOException;
import java.io.Reader;


public class CharSequenceReader extends Reader {

    protected CharSequence source;
    protected int length;
    protected int next = 0;
    private int mark = 0;
    
    public CharSequenceReader(CharSequence immutableCharSequence) {
        this.source = immutableCharSequence;
        this.length = source.length();
    }

    @Override
    public int read() throws IOException {
        synchronized (lock) {
            if (next >= length) {
                return -1;
            }
            char c = source.charAt(next++);
            return processReadChar(c);
        }
    }
    
    protected char processReadChar(char c) throws IOException {
        return c;
    }

     public int read(char cbuf[], int off, int len) throws IOException {
	synchronized (lock) {
            if ((off < 0) || (off > cbuf.length) || (len < 0) ||
                ((off + len) > cbuf.length) || ((off + len) < 0)) {
                throw new IndexOutOfBoundsException();
            } else if (len == 0) {
                return 0;
            }
	    if (next >= length)
		return -1;
	    int n = Math.min(length - next, len);
            for(int i = 0; i < n; i++) {
//                cbuf[i + off] = source.charAt(next + i);
                cbuf[i + off] = (char)read();
            }
//	    next += n;
	    return n;
	}
    }

    @Override
    public long skip(long ns) throws IOException {
        synchronized (lock) {
            if (next >= length) {
                return 0;
            }
            // Bound skip by beginning and end of the source
            long n = Math.min(length - next, ns);
            n = Math.max(-next, n);
            next += n;
                        
            return n;
        }
    }

    @Override
    public boolean ready() throws IOException {
        synchronized (lock) {
            return true;
        }
    }

    @Override
    public boolean markSupported() {
        return true;
    }
    
    protected void markedAt(int mark) {
    }

    @Override
    public void mark(int readAheadLimit) throws IOException {
        if (readAheadLimit < 0) {
            throw new IllegalArgumentException("Read-ahead limit < 0");
        }
        synchronized (lock) {
            mark = next;
            markedAt(mark);
        }
    }

    protected void inputReset() {
    }
    
    @Override
    public void reset() throws IOException {
        synchronized (lock) {
            next = mark;
            inputReset();
        }
    }

    @Override
    public void close() {
    }
    
}