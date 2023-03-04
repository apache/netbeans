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
package org.openide.filesystems;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.ListIterator;
import java.util.NoSuchElementException;
import org.openide.util.Exceptions;

/** Iterator for list of lines.
 * @author Jaroslav Tulach <jtulach@netbeans.org>
 */
final class FileObjectLineIterator implements ListIterator<String> {
    private final String encoding;
    private final FileObjectLines lines;
    private byte[] buffer;
    private BufferedReader reader;
    private String line;
    private int index;

    public FileObjectLineIterator(FileObjectLines lines, String encoding) throws IOException {
        this.lines = lines;
        this.encoding = encoding;
        initReader();
    }

    private FileObjectLineIterator(FileObjectLineIterator orig) {
        this.lines = orig.lines;
        this.encoding = orig.encoding;
        this.buffer = orig.buffer;
        try {
            initReader();
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }
    }

    public synchronized boolean hasNext() {
        if (line == null) {
            try {
                line = reader.readLine();
                if (line == null) {
                    reader.close();
                }
                index++;
            } catch (IOException ex) {
                Exceptions.printStackTrace(ex);
            }
        }
        return line != null;
    }

    public synchronized String next() {
        if (!hasNext()) {
            throw new NoSuchElementException();
        }
        String l = line;
        line = null;
        return l;
    }

    public void remove() {
        throw new UnsupportedOperationException();
    }

    public synchronized boolean hasPrevious() {
        return index > 0;
    }

    public synchronized String previous() {
        if (index == 0) {
            throw new NoSuchElementException();
        }
        try {
            int pos = index - 1;
            initReader();
            String last = null;
            while (index <= pos) {
                last = next();
            }
            index--;
            return last;
        } catch (IOException ex) {
            throw (NoSuchElementException)new NoSuchElementException().initCause(ex);
        }
    }

    public synchronized int nextIndex() {
        return index;
    }

    public synchronized int previousIndex() {
        return index - 1;
    }

    public void set(String e) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void add(String e) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    private void initReader() throws UnsupportedEncodingException, IOException {
        InputStream is;
        if (lines.fo.getSize() < 64 * 1024) {
            if (buffer == null) {
                buffer = lines.fo.asBytes();
            }
            is = new ByteArrayInputStream(buffer);
        } else {
            is = lines.fo.getInputStream();
        }
        this.reader = new BufferedReader(new InputStreamReader(is, encoding));
        this.index = 0;
        while (this.index < index) {
            next();
        }
    }

    final FileObjectLineIterator cloneIterator() {
        return new FileObjectLineIterator(this);
    }
}
