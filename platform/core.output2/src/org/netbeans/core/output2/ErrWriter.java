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

package org.netbeans.core.output2;

import java.util.logging.Logger;
import org.openide.windows.OutputListener;
import org.openide.windows.OutputWriter;

import java.io.IOException;

/**
 * Wrapper OutputWriter for the standard out which marks its lines as being
 * stderr.
 *
 * @author  Tim Boudreau
 */
class ErrWriter extends OutputWriter {
    private OutWriter wrapped;
    private final NbWriter parent;
    /** Creates a new instance of ErrWriter */
    ErrWriter(OutWriter wrapped, NbWriter parent) {
        super (new OutWriter.DummyWriter());
        this.wrapped = wrapped;
        this.parent = parent;
    }

    synchronized void setWrapped (OutWriter wrapped) {
        this.wrapped = wrapped;
        closed = true;
    }

    public void println(String s, OutputListener l) throws java.io.IOException {
        println(s, l, false);
    }

    @Override
    public void println(String s, OutputListener l, boolean important) throws java.io.IOException {
        closed = false;
        wrapped.print(s, l, important, null, null, OutputKind.ERR, true);
    }

    public void reset() throws IOException {
        Logger.getAnonymousLogger().warning("Do not call reset() on the error io," +
        " only on the output IO.  Reset on the error io does nothing.");
        closed = false;
    }
    
    @Override
    public void close() {
        if (!closed) {
            closed = true;
            parent.notifyErrClosed();
        }
    }

    boolean closed = true;
    boolean isClosed() {
        return closed;
    }

    @Override
    public void flush() {
        wrapped.flush();
    }
    
    @Override
    public boolean checkError() {
        return wrapped.checkError();
    }    
    
    @Override
    public void write(int c) {
        print(String.valueOf(c), false);
    }

    @Override
    public void write(char buf[], int off, int len) {
        print(new OutWriter.CharArrayWrapper(buf, off, len), false);
    }

    @Override
    public void write(String s, int off, int len) {
        print(s.substring(off, off + len), false);
    }

    @Override
    public void println(boolean x) {
        print(x ? "true" : "false", true);
    }

    @Override
    public void println(int i) {
        print(String.valueOf(i), true);
    }

    @Override
    public void println(char c) {
        print(String.valueOf(c), true);
    }

    @Override
    public void println(long l) {
        print(String.valueOf(l), true);
    }

    @Override
    public void println(float x) {
        print(String.valueOf(x), true);
    }

    @Override
    public void println(double x) {
        print(String.valueOf(x), true);
    }

    @Override
    public void println(char buf[]) {
        print(new OutWriter.CharArrayWrapper(buf), true);
    }

    @Override
    public void println(String s) {
        print(s, true);
    }

    @Override
    public void println(Object x) {
        print(String.valueOf(x), true);
    }

    @Override
    public void print(char[] buf) {
        print(new OutWriter.CharArrayWrapper(buf), false);
    }

    @Override
    public void print(Object obj) {
        print(String.valueOf(obj), false);
    }

    @Override
    public void print(char c) {
        print(String.valueOf(c), false);
    }

    @Override
    public void print(int i) {
        print(String.valueOf(i), false);
    }

    @Override
    public void print(String s) {
        print(s, false);
    }

    @Override
    public void print(boolean b) {
        print(b ? "true" : "false", false);
    }

    @Override
    public void println() {
        closed = false;
        wrapped.println();
    }

    private void print(CharSequence s, boolean addLineSep) {
        closed = false;
        wrapped.print(s, null, false, null, null, OutputKind.ERR, addLineSep);
    }
}
